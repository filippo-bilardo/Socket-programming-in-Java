# ES02 - Raccoglitore Dati da Sensori Multithreaded

## Descrizione dell'Esercitazione

Sviluppare un'applicazione **multithreaded** che:

1. **Server** - Riceve misurazioni da sensori client su una porta dedicata
2. **Archiviazione** - Salva i dati in un file CSV su disco
3. **Web Interface** - Quando contattato dal browser, visualizza tutti i dati raccolti in HTML
4. **Sincronizzazione** - Gestisce l'accesso concorrente ai file

## Obiettivi Didattici

- ✅ Multithreading con socket TCP
- ✅ Persistenza su file
- ✅ Sincronizzazione tra thread
- ✅ HTTP server semplice
- ✅ Parsing e generazione HTML
- ✅ Gestione CSV

---

## Struttura del Progetto

```
ES02-RaccoglitoreDatiSensori/
├── src/
│   ├── Server.java                    # Server principale
│   ├── ClientHandler.java             # Gestore connessione sensore
│   ├── HttpHandler.java               # Gestore richieste HTTP
│   ├── DataManager.java               # Gestione dati e file
│   └── SensorClient.java              # Client sensore di test
├── data/
│   └── sensori.csv                    # File dati (creato automaticamente)
├── doc/
│   └── ESERCITAZIONE.md              # Questa guida
└── README.md                          # Quick start

```

---

## Parte 1: DataManager - Gestione Centralizzata

Questa classe gestisce l'accesso sincronizzato ai file.

### DataManager.java

```java
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataManager {
    private String filepath;
    private Object lock = new Object();
    private static final String HEADER = "Timestamp,Sensore,Tipo,Valore,Unita\n";
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DataManager(String filepath) {
        this.filepath = filepath;
        initializeFile();
    }

    private void initializeFile() {
        synchronized (lock) {
            File file = new File(filepath);
            
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(HEADER);
                    System.out.println("✅ File creato: " + filepath);
                } catch (IOException e) {
                    System.err.println("❌ Errore creazione file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Salva una misurazione nel file CSV
     * @param sensorId ID del sensore
     * @param sensorType Tipo di sensore (Temperatura, Umidità, Pressione)
     * @param value Valore misurato
     * @param unit Unità di misura
     */
    public void saveMeasurement(String sensorId, String sensorType, 
                                double value, String unit) {
        synchronized (lock) {
            try (FileWriter fw = new FileWriter(filepath, true)) {
                String timestamp = LocalDateTime.now().format(formatter);
                String line = String.format("%s,%s,%s,%.2f,%s\n", 
                    timestamp, sensorId, sensorType, value, unit);
                fw.write(line);
                
                System.out.println("💾 Salvato: " + sensorId + " = " + value + " " + unit);
            } catch (IOException e) {
                System.err.println("❌ Errore scrittura: " + e.getMessage());
            }
        }
    }

    /**
     * Legge tutti i dati dal file
     * @return Lista di stringhe (righe del CSV)
     */
    public List<String> getAllData() {
        synchronized (lock) {
            List<String> data = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    data.add(line);
                }
            } catch (IOException e) {
                System.err.println("❌ Errore lettura: " + e.getMessage());
            }
            return data;
        }
    }

    /**
     * Genera tabella HTML con i dati
     */
    public String generateHtmlTable() {
        List<String> data = getAllData();
        StringBuilder html = new StringBuilder();
        
        html.append("<table border='1' cellpadding='10'>\n");
        
        // Header
        html.append("<tr style='background-color: #4CAF50; color: white;'>\n");
        for (String header : HEADER.split(",")) {
            html.append("<th>").append(header.trim()).append("</th>\n");
        }
        html.append("</tr>\n");
        
        // Righe dati
        for (int i = 1; i < data.size(); i++) {
            html.append("<tr>\n");
            String[] fields = data.get(i).split(",");
            for (String field : fields) {
                html.append("<td>").append(field.trim()).append("</td>\n");
            }
            html.append("</tr>\n");
        }
        
        html.append("</table>\n");
        return html.toString();
    }

    public int getTotalMeasurements() {
        return getAllData().size() - 1; // Escludi header
    }
}
```

---

## Parte 2: ClientHandler - Gestore Sensori

Gestisce la connessione di un singolo sensore.

### ClientHandler.java

```java
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataManager dataManager;
    private static int clientCounter = 0;
    private int clientID;

    public ClientHandler(Socket socket, DataManager dataManager) {
        this.socket = socket;
        this.dataManager = dataManager;
        this.clientID = ++clientCounter;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            System.out.println("✅ Sensore #" + clientID + " connesso da " + 
                socket.getInetAddress().getHostAddress());

            out.println("🔌 Benvenuto! Formato: SENSORE|TIPO|VALORE|UNITA");
            
            String request;
            while ((request = in.readLine()) != null) {
                handleRequest(request, out);
            }

            System.out.println("❌ Sensore #" + clientID + " disconnesso");

        } catch (IOException e) {
            System.err.println("❌ Errore sensore #" + clientID + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(String request, PrintWriter out) {
        try {
            // Formato: SENSORE|TIPO|VALORE|UNITA
            // Esempio: Stanza1|Temperatura|22.5|°C
            
            String[] parts = request.split("\\|");
            
            if (parts.length != 4) {
                out.println("❌ Errore: formato non valido!");
                out.println("   Usa: SENSORE|TIPO|VALORE|UNITA");
                return;
            }

            String sensorId = parts[0].trim();
            String sensorType = parts[1].trim();
            double value = Double.parseDouble(parts[2].trim());
            String unit = parts[3].trim();

            // Salva nel file
            dataManager.saveMeasurement(sensorId, sensorType, value, unit);
            
            out.println("✅ Misurazione salvata!");

        } catch (NumberFormatException e) {
            System.out.println("⚠️  Valore non numerico");
        } catch (Exception e) {
            System.err.println("❌ Errore elaborazione: " + e.getMessage());
        }
    }
}
```

---

## Parte 3: HttpHandler - Web Interface

Gestisce le richieste HTTP dal browser.

### HttpHandler.java

```java
import java.io.*;
import java.net.Socket;

public class HttpHandler implements Runnable {
    private Socket socket;
    private DataManager dataManager;

    public HttpHandler(Socket socket, DataManager dataManager) {
        this.socket = socket;
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

            // Leggi richiesta HTTP
            String requestLine = in.readLine();
            
            if (requestLine == null) {
                return;
            }

            System.out.println("📡 Richiesta HTTP: " + requestLine);

            // Salta gli header
            while (in.ready() && in.readLine().length() > 0) ;

            // Genera risposta HTML
            String htmlResponse = generateHtmlResponse();

            // Invia header HTTP
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Content-Length: " + htmlResponse.length());
            out.println();
            
            // Invia body
            out.println(htmlResponse);
            out.flush();

            System.out.println("✅ Pagina inviata");

        } catch (IOException e) {
            System.err.println("❌ Errore HTTP: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateHtmlResponse() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("  <title>Dati Sensori</title>\n");
        html.append("  <meta charset='UTF-8'>\n");
        html.append("  <style>\n");
        html.append("    body { font-family: Arial; margin: 20px; }\n");
        html.append("    h1 { color: #333; }\n");
        html.append("    table { border-collapse: collapse; width: 100%; }\n");
        html.append("    th, td { padding: 10px; text-align: left; border: 1px solid #ddd; }\n");
        html.append("    th { background-color: #4CAF50; color: white; }\n");
        html.append("    tr:hover { background-color: #f5f5f5; }\n");
        html.append("    .stats { background-color: #e8f4f8; padding: 10px; margin-bottom: 20px; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        html.append("  <h1>📊 Raccoglitore Dati da Sensori</h1>\n");
        
        // Statistiche
        int totalMeasurements = dataManager.getTotalMeasurements();
        html.append("  <div class='stats'>\n");
        html.append("    <p><strong>Totale misurazioni:</strong> ").append(totalMeasurements).append("</p>\n");
        html.append("  </div>\n");
        
        // Tabella dati
        html.append("  <h2>Dati Raccolti</h2>\n");
        html.append(dataManager.generateHtmlTable());
        
        // Link refresh
        html.append("  <br><p><a href='/'>🔄 Aggiorna</a></p>\n");
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
}
```

---

## Parte 4: Server - Applicazione Principale

Server multithreaded che gestisce sensori e browser.

### Server.java

```java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocketSensori;
    private ServerSocket serverSocketHttp;
    private DataManager dataManager;
    private static final int PORTA_SENSORI = 5000;
    private static final int PORTA_HTTP = 8080;
    private static final String FILE_DATI = "data/sensori.csv";

    public Server() throws IOException {
        dataManager = new DataManager(FILE_DATI);
        serverSocketSensori = new ServerSocket(PORTA_SENSORI);
        serverSocketHttp = new ServerSocket(PORTA_HTTP);
        
        System.out.println("╔═════════════════════════════════════════╗");
        System.out.println("║  Raccoglitore Dati da Sensori           ║");
        System.out.println("╠═════════════════════════════════════════╣");
        System.out.println("║ 🔌 Porta Sensori: " + PORTA_SENSORI + "              ║");
        System.out.println("║ 🌐 Porta HTTP: " + PORTA_HTTP + "                ║");
        System.out.println("║ 💾 File Dati: " + FILE_DATI + "         ║");
        System.out.println("║                                         ║");
        System.out.println("║ 📍 Accedi a: http://localhost:" + PORTA_HTTP + "/   ║");
        System.out.println("╚═════════════════════════════════════════╝");
    }

    public void start() {
        // Thread per sensori
        Thread sensorThread = new Thread(() -> acceptSensorConnections());
        sensorThread.setName("SensorListener");
        sensorThread.start();

        // Thread per HTTP
        Thread httpThread = new Thread(() -> acceptHttpConnections());
        httpThread.setName("HttpListener");
        httpThread.start();

        System.out.println("✅ Server avviato");
    }

    private void acceptSensorConnections() {
        while (true) {
            try {
                Socket socket = serverSocketSensori.accept();
                new Thread(new ClientHandler(socket, dataManager)).start();
            } catch (IOException e) {
                System.err.println("❌ Errore connessione sensore: " + e.getMessage());
            }
        }
    }

    private void acceptHttpConnections() {
        while (true) {
            try {
                Socket socket = serverSocketHttp.accept();
                new Thread(new HttpHandler(socket, dataManager)).start();
            } catch (IOException e) {
                System.err.println("❌ Errore connessione HTTP: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
        
        // Mantieni il programma in esecuzione
        System.out.println("\nPremi CTRL+C per fermare il server");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

---

## Parte 5: SensorClient - Client di Test

Simula un sensore che invia dati.

### SensorClient.java

```java
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class SensorClient {
    private String sensorId;
    private String sensorType;
    private double minValue;
    private double maxValue;
    private String unit;
    private Socket socket;
    private PrintWriter out;

    public SensorClient(String sensorId, String sensorType, 
                        double minValue, double maxValue, String unit) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        String welcome = in.readLine();
        System.out.println("📡 " + sensorId + ": " + welcome);

        // Invia misurazioni periodiche
        sendMeasurements();

        socket.close();
    }

    private void sendMeasurements() throws IOException {
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            double value = minValue + (maxValue - minValue) * random.nextDouble();
            String message = String.format("%s|%s|%.2f|%s", 
                sensorId, sensorType, value, unit);
            
            out.println(message);
            System.out.println("📤 " + sensorId + " inviato: " + value);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Crea tre sensori diversi
        new Thread(() -> {
            try {
                new SensorClient("Stanza1", "Temperatura", 18, 25, "°C")
                    .connect("localhost", 5000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(500);
                new SensorClient("Stanza1", "Umidita", 40, 70, "%")
                    .connect("localhost", 5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                new SensorClient("Esterno", "Pressione", 990, 1020, "hPa")
                    .connect("localhost", 5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

---

## Istruzioni di Esecuzione

### 1. Compilazione

```bash
cd src
javac *.java
```

### 2. Avvia il Server

```bash
java Server
```

Output atteso:
```
╔═════════════════════════════════════════╗
║  Raccoglitore Dati da Sensori           ║
╠═════════════════════════════════════════╣
║ 🔌 Porta Sensori: 5000              ║
║ 🌐 Porta HTTP: 8080                ║
║ 💾 File Dati: data/sensori.csv         ║
║                                         ║
║ 📍 Accedi a: http://localhost:8080/   ║
╚═════════════════════════════════════════╝
✅ Server avviato
```

### 3. Avvia i Client Sensori (in altro terminale)

```bash
java SensorClient
```

Vedrai i dati inviati dai sensori.

### 4. Accedi al Browser

Apri il browser e vai a: **http://localhost:8080**

Vedrai una tabella con tutti i dati raccolti!

---

## Domande di Autovalutazione

### Domanda 1
Perché è necessario sincronizzare l'accesso al file CSV?

A) Per migliorare la performance  
B) Per evitare race condition tra thread  
C) Per criptare i dati  
D) Non è necessario  

**Risposta corretta: B**

Più thread potrebbero accedere al file contemporaneamente. La sincronizzazione (`synchronized`) previene corruzione dei dati.

---

### Domanda 2
Quanti ServerSocket sono necessari?

A) Uno solo  
B) Due (uno per sensori, uno per HTTP)  
C) Tre  
D) Dipende dai sensori  

**Risposta corretta: B**

Uno per ascoltare i sensori (porta 5000) e uno per le richieste HTTP dal browser (porta 8080).

---

### Domanda 3
Come il server gestisce più sensori contemporaneamente?

A) Con un unico thread  
B) Con un thread per sensore  
C) Con una coda  
D) Non è possibile  

**Risposta corretta: B**

Ogni sensore crea un nuovo thread (`ClientHandler`) che elabora indipendentemente.

---

### Domanda 4
Cosa accade quando il browser contatta il server?

A) Viene disconnesso  
B) Viene gestito da un thread `HttpHandler`  
C) Viene ignora to  
D) Errore di connessione  

**Risposta corretta: B**

Un nuovo thread `HttpHandler` elabora la richiesta HTTP e invia la pagina HTML.

---

### Domanda 5
Dove vengono salvati i dati?

A) Solo in memoria  
B) In un file CSV su disco  
C) Su un database  
D) Non vengono salvati  

**Risposta corretta: B**

File `data/sensori.csv` con sincronizzazione per accesso sicuro.

---

## Risposte Corrette

| Q | Risposta | Perché |
|---|----------|--------|
| 1 | B | Prevenire race condition con `synchronized` |
| 2 | B | Porta 5000 sensori, 8080 HTTP |
| 3 | B | Un thread `ClientHandler` per sensore |
| 4 | B | Thread `HttpHandler` genera HTML |
| 5 | B | File CSV con sincronizzazione |

---

## Estensioni Possibili

1. **Database** - Sostituisci CSV con SQLite
2. **Autenticazione** - Aggiungi username/password per sensori
3. **Grafico** - Visualizza dati con grafici (Chart.js)
4. **Filter** - Filtra dati per sensore o data
5. **Export** - Esporta dati in JSON/Excel
6. **Real-time** - WebSocket per aggiornamento live
7. **Allarmi** - Notifica se valore esce da range

---

## Conclusione

Questa esercitazione copre:
- ✅ Multithreading avanzato
- ✅ Sincronizzazione tra thread
- ✅ Persistenza su file
- ✅ HTTP server semplice
- ✅ Parsing e generazione HTML
- ✅ Pattern Client-Server

Perfetto per comprendere architetture real-time di raccolta dati!