/**
 * Nome dell'Esempio: File Transfer TCP
 * Guida di Riferimento: 02-Comunicazione-Dati-TCP.md
 * 
 * Obiettivo: Dimostrare trasferimento file e gestione stream binari via TCP.
 * 
 * Spiegazione:
 * 1. Server che riceve file e li salva
 * 2. Client che invia file con progress tracking
 * 3. Protocollo semplice per metadata e controllo integrità
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileTransfer {
    
    /**
     * Server per ricezione file
     */
    public static class Server {
        private final int port;
        private final String saveDirectory;
        
        public Server(int port, String saveDirectory) {
            this.port = port;
            this.saveDirectory = saveDirectory;
        }
        
        public void start() throws IOException {
            // Crea directory se non existe
            Files.createDirectories(Paths.get(saveDirectory));
            
            System.out.println("📁 File Transfer Server avviato");
            System.out.println("   Porta: " + port);
            System.out.println("   Directory: " + new File(saveDirectory).getAbsolutePath());
            System.out.println("🛑 Premi Ctrl+C per fermare");
            System.out.println("=" .repeat(50));
            
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                
                while (true) {
                    try (Socket clientSocket = serverSocket.accept()) {
                        clientSocket.setSoTimeout(30000);
                        System.out.println("\n🔗 Client connesso: " + clientSocket.getRemoteSocketAddress());
                        handleFileTransfer(clientSocket);
                        
                    } catch (IOException e) {
                        System.err.println("❌ Errore durante il trasferimento: " + e.getMessage());
                    }
                }
            }
        }
        
        private void handleFileTransfer(Socket clientSocket) throws IOException {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            
            try {
                // Legge metadata file
                String fileName = in.readUTF();
                long fileSize = in.readLong();
                String checksum = in.readUTF();
                if (fileSize < 0 || fileName.isBlank() || fileName.equals(".") || fileName.equals("..")
                        || fileName.contains("/") || fileName.contains("\\") || fileName.contains(":") || fileName.indexOf(0) >= 0) {
                    out.writeBoolean(false); out.writeUTF("Nome o dimensione non validi"); out.flush();
                    return;
                }
                
                System.out.println("📄 File: " + fileName);
                System.out.println("📏 Dimensione: " + formatFileSize(fileSize));
                System.out.println("🔐 Checksum MD5: " + checksum);
                
                // Prepara file di destinazione
                Path directory = Paths.get(saveDirectory).toAbsolutePath().normalize();
                Path filePath = directory.resolve(fileName).normalize();
                if (!filePath.startsWith(directory)) throw new IOException("Percorso non valido");
                File destFile = filePath.toFile();
                
                // Controlla se file esiste
                if (destFile.exists()) {
                    System.out.println("⚠️ File già esistente, verrà sovrascritto");
                }
                
                // Riceve file con progress
                long totalReceived = 0;
                byte[] buffer = new byte[8192]; // Buffer 8KB
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                
                Path partial = Files.createTempFile(directory, ".upload-", ".part");
                try {
                    try (FileOutputStream fos = new FileOutputStream(partial.toFile());
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    
                        System.out.println("📥 Ricezione in corso...");
                        long lastProgress = 0;
                    
                        while (totalReceived < fileSize) {
                            int bytesToRead = (int) Math.min(buffer.length, fileSize - totalReceived);
                            int bytesRead = in.read(buffer, 0, bytesToRead);
                        
                            if (bytesRead == -1) {
                                throw new IOException("Connessione interrotta durante il trasferimento");
                            }
                        
                            bos.write(buffer, 0, bytesRead);
                            md5.update(buffer, 0, bytesRead);
                            totalReceived += bytesRead;
                        
                            // Progress ogni 10%
                            long progress = (totalReceived * 100) / fileSize;
                            if (progress >= lastProgress + 10) {
                                System.out.println("   Progress: " + progress + "% (" +
                                                 formatFileSize(totalReceived) + "/" +
                                                 formatFileSize(fileSize) + ")");
                                lastProgress = progress;
                            }
                        }
                    }
                
                    // Verifica checksum
                    String receivedChecksum = bytesToHex(md5.digest());
                    boolean checksumValid = checksum.equalsIgnoreCase(receivedChecksum);
                
                    System.out.println("✅ Trasferimento completato: " + formatFileSize(totalReceived));
                    System.out.println("🔍 Verifica integrità: " + (checksumValid ? "OK" : "FALLITA"));
                
                    if (!checksumValid) {
                        System.out.println("   Atteso: " + checksum);
                        System.out.println("   Ricevuto: " + receivedChecksum);
                    }
                
                    if (checksumValid) {
                        Files.move(partial, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    // Risposta al client
                    out.writeBoolean(checksumValid);
                    out.writeUTF(checksumValid ? "File ricevuto correttamente" : "Errore checksum");
                    out.flush();
                
                    if (checksumValid) System.out.println("📁 File salvato: " + destFile.getAbsolutePath());
                } finally { Files.deleteIfExists(partial); }
                
            } catch (InvalidPathException e) {
                throw new IOException("Nome file non valido", e);
            } catch (NoSuchAlgorithmException e) {
                throw new IOException("Algoritmo MD5 non disponibile", e);
            }
        }
    }
    
    /**
     * Client per invio file
     */
    public static class Client {
        private final String serverHost;
        private final int serverPort;
        
        public Client(String serverHost, int serverPort) {
            this.serverHost = serverHost;
            this.serverPort = serverPort;
        }
        
        public boolean sendFile(String filePath) throws IOException {
            File file = new File(filePath);
            
            if (!file.exists()) {
                throw new FileNotFoundException("File non trovato: " + filePath);
            }
            
            if (!file.isFile()) {
                throw new IOException("Percorso non valido (non è un file): " + filePath);
            }
            
            System.out.println("📤 Invio file: " + file.getName());
            System.out.println("📏 Dimensione: " + formatFileSize(file.length()));
            System.out.println("🎯 Destinazione: " + serverHost + ":" + serverPort);
            
            // Calcola checksum MD5
            System.out.println("🔐 Calcolo checksum...");
            String checksum = calculateMD5(file);
            System.out.println("   MD5: " + checksum);
            
            try (Socket socket = new Socket(serverHost, serverPort);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                
                // Invia metadata
                out.writeUTF(file.getName());
                out.writeLong(file.length());
                out.writeUTF(checksum);
                out.flush();
                
                // Invia file con progress
                byte[] buffer = new byte[8192];
                long totalSent = 0;
                long lastProgress = 0;
                
                System.out.println("📡 Trasferimento in corso...");
                
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalSent += bytesRead;
                    
                    // Progress ogni 10%
                    long progress = (totalSent * 100) / file.length();
                    if (progress >= lastProgress + 10) {
                        System.out.println("   Progress: " + progress + "% (" + 
                                         formatFileSize(totalSent) + "/" + 
                                         formatFileSize(file.length()) + ")");
                        lastProgress = progress;
                    }
                }
                
                out.flush();
                System.out.println("✅ Trasferimento completato: " + formatFileSize(totalSent));
                
                // Riceve risposta server
                boolean success = in.readBoolean();
                String message = in.readUTF();
                
                System.out.println("📨 Risposta server: " + message);
                return success;
                
            } catch (ConnectException e) {
                throw new ConnectException("Impossibile connettersi al server " + 
                                         serverHost + ":" + serverPort);
            }
        }
        
        private String calculateMD5(File file) throws IOException {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        md5.update(buffer, 0, bytesRead);
                    }
                }
                
                return bytesToHex(md5.digest());
                
            } catch (InvalidPathException e) {
                throw new IOException("Nome file non valido", e);
            } catch (NoSuchAlgorithmException e) {
                throw new IOException("Algoritmo MD5 non disponibile", e);
            }
        }
    }
    
    // Utility methods
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("📁 File Transfer TCP - Server e Client");
            System.out.println("Utilizzo:");
            System.out.println("  java FileTransfer server <porta> [directory]");
            System.out.println("  java FileTransfer client <host> <porta> <file-path>");
            System.out.println();
            System.out.println("Esempi:");
            System.out.println("  java FileTransfer server 9999 ./uploads");
            System.out.println("  java FileTransfer client localhost 9999 ./document.pdf");
            return;
        }
        
        String mode = args[0].toLowerCase();
        
        if ("server".equals(mode)) {
            if (args.length < 2) {
                System.err.println("❌ Porta richiesta per il server");
                return;
            }
            
            try {
                int port = Integer.parseInt(args[1]);
                String directory = args.length >= 3 ? args[2] : "./received_files";
                
                Server server = new Server(port, directory);
                server.start();
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[1]);
            } catch (IOException e) {
                System.err.println("💥 Errore server: " + e.getMessage());
            }
            
        } else if ("client".equals(mode)) {
            if (args.length < 4) {
                System.err.println("❌ Host, porta e file richiesti per il client");
                return;
            }
            
            try {
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                String filePath = args[3];
                
                Client client = new Client(host, port);
                boolean success = client.sendFile(filePath);
                
                System.out.println(success ? "🎉 File inviato con successo!" : "💥 Invio fallito");
                System.exit(success ? 0 : 1);
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Porta non valida: " + args[2]);
            } catch (IOException e) {
                System.err.println("💥 Errore trasferimento: " + e.getMessage());
                System.exit(1);
            }
            
        } else {
            System.err.println("❌ Modalità non riconosciuta: " + mode);
        }
    }
}