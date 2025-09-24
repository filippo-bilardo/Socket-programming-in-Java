/**
 * Nome dell'Esempio: Test Ambiente
 * Guida di Riferimento: 01-Setup-Ambiente-Sviluppo.md
 * 
 * Obiettivo: Verificare che l'ambiente di sviluppo sia configurato correttamente.
 * 
 * Spiegazione:
 * 1. Verifica la versione Java e le proprietà di sistema
 * 2. Testa la connettività di rete di base
 * 3. Controlla le interfacce di rete disponibili
 * 
 * @author Socket Programming Course
 * @version 1.0
 */

import java.net.*;
import java.util.Enumeration;
import java.util.Properties;

public class TestAmbiente {
    
    /**
     * Verifica l'ambiente Java
     */
    public static void verificaAmbienteJava() {
        System.out.println("☕ Informazioni Ambiente Java");
        System.out.println("=" .repeat(40));
        
        Properties props = System.getProperties();
        
        System.out.println("Java Version: " + props.getProperty("java.version"));
        System.out.println("Java Vendor: " + props.getProperty("java.vendor"));
        System.out.println("Java Home: " + props.getProperty("java.home"));
        System.out.println("OS Name: " + props.getProperty("os.name"));
        System.out.println("OS Version: " + props.getProperty("os.version"));
        System.out.println("User Name: " + props.getProperty("user.name"));
        System.out.println("Working Directory: " + props.getProperty("user.dir"));
        
        // Memoria JVM
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        
        System.out.println("\n💾 Memoria JVM:");
        System.out.println("Max Memory: " + maxMemory + " MB");
        System.out.println("Total Memory: " + totalMemory + " MB");
        System.out.println("Free Memory: " + freeMemory + " MB");
    }
    
    /**
     * Testa connettività di rete base
     */
    public static void testaConnettivita() {
        System.out.println("\n🌐 Test Connettività di Rete");
        System.out.println("=" .repeat(40));
        
        // Test connessioni verso siti noti
        String[] testHosts = {
            "google.com:80",
            "github.com:443",
            "stackoverflow.com:80"
        };
        
        for (String hostPort : testHosts) {
            String[] parts = hostPort.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 3000);
                System.out.println("✅ " + hostPort + " - Raggiungibile");
                socket.close();
            } catch (Exception e) {
                System.out.println("❌ " + hostPort + " - Non raggiungibile: " + e.getMessage());
            }
        }
    }
    
    /**
     * Mostra interfacce di rete disponibili
     */
    public static void mostraInterfacceRete() {
        System.out.println("\n🔌 Interfacce di Rete");
        System.out.println("=" .repeat(40));
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                
                System.out.println("\n📡 " + ni.getName() + " (" + ni.getDisplayName() + ")");
                System.out.println("   Up: " + ni.isUp());
                System.out.println("   Loopback: " + ni.isLoopback());
                System.out.println("   Virtual: " + ni.isVirtual());
                
                // Indirizzi IP associati
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String type = addr instanceof Inet4Address ? "IPv4" : "IPv6";
                    System.out.println("   " + type + ": " + addr.getHostAddress());
                }
                
                // MTU (se disponibile)
                try {
                    System.out.println("   MTU: " + ni.getMTU());
                } catch (Exception e) {
                    System.out.println("   MTU: N/A");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nel recupero interfacce: " + e.getMessage());
        }
    }
    
    /**
     * Testa risoluzione DNS
     */
    public static void testaRisoluzioneDNS() {
        System.out.println("\n🔍 Test Risoluzione DNS");
        System.out.println("=" .repeat(40));
        
        String[] testHosts = {
            "google.com",
            "github.com", 
            "localhost"
        };
        
        for (String host : testHosts) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                System.out.println("🌐 " + host + ":");
                for (InetAddress addr : addresses) {
                    String type = addr instanceof Inet4Address ? "IPv4" : "IPv6";
                    System.out.println("   " + type + ": " + addr.getHostAddress());
                }
            } catch (Exception e) {
                System.out.println("❌ " + host + ": " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Test Configurazione Ambiente di Sviluppo");
        System.out.println("Questo tool verifica che l'ambiente sia configurato correttamente");
        System.out.println("per lo sviluppo di applicazioni socket in Java.\n");
        
        // Esegue tutti i test
        verificaAmbienteJava();
        testaConnettivita();
        mostraInterfacceRete();
        testaRisoluzioneDNS();
        
        System.out.println("\n✅ Test completato!");
        System.out.println("Se tutti i test sono passati, l'ambiente è pronto per il corso.");
    }
}