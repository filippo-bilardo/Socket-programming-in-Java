# b — Dispenser numeri con stato condiviso

Realizza un servizio di distribuzione numeri per una coda. Più client devono poter richiedere numeri contemporaneamente senza duplicati; il negozio deve aggiornare lo stato attraverso una connessione persistente.

## Requisiti e protocollo

JDK 11+, terminale e [sincronizzazione](../../05-Multithreading%20avanzato.md). Porta TCP 7777; richieste e risposte sono righe UTF-8.

- `ultimoNumeroAssegnato` parte da 0; PRENOTA assegna il successivo numero e conclude la connessione.
- `numeroInServizio` è il nome della variabile che memorizza **l'ultimo numero completato**, inizialmente 0. SERVI completa il prossimo numero già distribuito, non quello successivo da distribuire.
- In PRENOTA, ATTESA conta le persone davanti al nuovo cliente: `max(0, numero - ultimoCompletato - 1)`.
- In STATO/SERVI, ATTESA conta tutti i numeri da completare: `ultimoAssegnato - ultimoCompletato`.
- NEGOZIO identifica la sessione del negozio, che resta aperta fino a QUIT o EOF. È un'identificazione didattica, senza autenticazione.
- La versione base risponde ai comandi e non invia notifiche spontanee. Un broadcast richiede client in attesa che mantengano la connessione e un protocollo distinto per gli eventi.

| Richiesta | Risposta |
| --- | --- |
| PRENOTA | `NUMERO:1\|SERVIZIO:0\|ATTESA:0` per il primo cliente |
| STATO | `SERVIZIO:0\|DISTRIBUITI:1\|ATTESA:1` |
| NEGOZIO | `NEGOZIO_OK`, poi altri comandi sulla stessa connessione |
| SERVI | `SERVITO:1\|ATTESA:0`, oppure `ERRORE:Nessun numero in attesa` |
| QUIT | `BYE`, poi chiusura |

Gli aggiornamenti dei due contatori e la costruzione della relativa risposta devono essere un'unica operazione sincronizzata. Ogni gestore possiede il proprio socket: non proteggere la lettura di rete con un lock globale.

## Passaggi

1. Disegna le classi DispenserServer, DispenserClient e NegozioClient e identifica lo stato condiviso.
2. Implementa PRENOTA e STATO, poi aggiungi NEGOZIO/SERVI/QUIT.
3. Avvia due client insieme e controlla l'univocità dei numeri.
4. Confronta la tua implementazione con le classi di riferimento sotto; spiega perché la sincronizzazione copre anche la risposta.

## Implementazione di riferimento

Salva ogni classe nel file omonimo. Ogni socket è chiuso alla fine del gestore, anche in caso di eccezione; la codifica di rete è UTF-8.

### DispenserServer.java

```java
import java.io.*;
import java.net.*;

public class DispenserServer {
    private static final int PORTA = 7777;
    
    // Variabili condivise - devono essere sincronizzate
    private static int ultimoNumeroAssegnato = 0;
    private static int numeroInServizio = 0;
    
    public static void main(String[] args) {
        System.out.println("=== SERVER DISPENSER NUMERI ===");
        System.out.println("Server avviato sulla porta " + PORTA);
        System.out.println("In attesa di connessioni...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Gestisci client in thread separato per supporto multi-client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        }
    }
    
    private static void handleClient(Socket socket) {
        try (
            Socket owned = socket;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8)
        ) {
            String clientIP = socket.getInetAddress().getHostAddress();
            System.out.println("[CONNESSIONE] Client da " + clientIP);
            
            String richiesta;
            while ((richiesta = in.readLine()) != null) {
                String risposta = processaRichiesta(richiesta);
                out.println(risposta);
                
                // PRENOTA conclude la richiesta; il negozio resta connesso fino a QUIT/EOF.
                if (richiesta.equals("PRENOTA") || richiesta.equals("QUIT")) {
                    break;
                }
            }
            
            System.out.println("[DISCONNESSIONE] Client disconnesso");
            
        } catch (IOException e) {
            System.err.println("Errore gestione client: " + e.getMessage());
        }
    }
    
    private static synchronized String processaRichiesta(String richiesta) {
        switch (richiesta) {
            case "PRENOTA":
                return prenota();
                
            case "STATO":
                return getStato();
                
            case "NEGOZIO":
                return "NEGOZIO_OK";
                
            case "SERVI":
                return serviCliente();
            case "QUIT":
                return "BYE";
                
            default:
                return "ERRORE: Comando non riconosciuto";
        }
    }
    
    private static String prenota() {
        ultimoNumeroAssegnato++;
        int numeroAssegnato = ultimoNumeroAssegnato;
        
        System.out.println("[PRENOTA] Assegnato numero: " + numeroAssegnato);
        
        int attesa = Math.max(0, numeroAssegnato - numeroInServizio - 1);
        
        // Formato: NUMERO:XX|SERVIZIO:YY|ATTESA:ZZ
        return String.format("NUMERO:%d|SERVIZIO:%d|ATTESA:%d", 
                           numeroAssegnato, numeroInServizio, attesa);
    }
    
    private static String getStato() {
        int attesa = Math.max(0, ultimoNumeroAssegnato - numeroInServizio);
        return String.format("SERVIZIO:%d|DISTRIBUITI:%d|ATTESA:%d",
                           numeroInServizio, ultimoNumeroAssegnato, attesa);
    }
    
    private static String serviCliente() {
        if (numeroInServizio >= ultimoNumeroAssegnato) return "ERRORE:Nessun numero in attesa";
        numeroInServizio++;
        System.out.println("[SERVI] Numero servito aggiornato a: " + numeroInServizio);
        
        int attesa = Math.max(0, ultimoNumeroAssegnato - numeroInServizio);
        
        stampaStatistiche();
        
        return String.format("SERVITO:%d|ATTESA:%d", numeroInServizio, attesa);
    }
    
    private static void stampaStatistiche() {
        System.out.println("\n--- STATISTICHE ---");
        System.out.println("Numeri distribuiti: " + ultimoNumeroAssegnato);
        System.out.println("Ultimo numero completato: " + numeroInServizio);
        System.out.println("Persone in attesa: " + 
                         (ultimoNumeroAssegnato - numeroInServizio));
        System.out.println("-------------------\n");
    }
}
```

### DispenserClient.java

```java
import java.io.*;
import java.net.*;

public class DispenserClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 7777;
    
    public static void main(String[] args) {
        System.out.println("=== CLIENT DISPENSER ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))
        ) {
            System.out.println("Connessione al server riuscita!");
            System.out.println("Richiesta numero in corso...\n");
            
            // Invia richiesta prenota
            out.println("PRENOTA");
            
            // Ricevi risposta
            String risposta = in.readLine();
            
            if (risposta != null) {
                visualizzaRisposta(risposta);
            }
            
            System.out.println("\nGrazie per aver utilizzato il servizio!");
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private static void visualizzaRisposta(String risposta) {
        // Formato: NUMERO:XX|SERVIZIO:YY|ATTESA:ZZ
        String[] parti = risposta.split("\\|");
        
        for (String parte : parti) {
            String[] keyValue = parte.split(":");
            if (keyValue.length == 2) {
                String chiave = keyValue[0];
                String valore = keyValue[1];
                
                switch (chiave) {
                    case "NUMERO":
                        System.out.println("╔════════════════════════════╗");
                        System.out.println("║   IL TUO NUMERO È: " + 
                                         String.format("%3s", valore) + "   ║");
                        System.out.println("╚════════════════════════════╝");
                        break;
                        
                    case "SERVIZIO":
                        System.out.println("\nUltimo numero completato: " + valore);
                        break;
                        
                    case "ATTESA":
                        System.out.println("Persone davanti a te: " + valore);
                        break;
                }
            }
        }
    }
}
```

### NegozioClient.java

```java
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class NegozioClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 7777;
    
    public static void main(String[] args) {
        System.out.println("=== NEGOZIO - GESTIONE SERVIZIO ===");
        
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connessione al server come Negozio...\n");
            
            // Identifica come negozio
            out.println("NEGOZIO");
            String conferma = in.readLine();
            
            if (!"NEGOZIO_OK".equals(conferma)) {
                System.err.println("Errore autenticazione negozio");
                return;
            }
            
            System.out.println("Autenticazione riuscita!");
            mostraMenu();
            
            // Loop comandi
            while (true) {
                System.out.print("\n> ");
                String comando = scanner.nextLine().trim().toUpperCase();
                
                if (comando.equals("QUIT")) {
                    out.println("QUIT");
                    in.readLine();
                    System.out.println("Disconnessione...");
                    break;
                }
                
                if (comando.equals("SERVI") || comando.equals("STATO")) {
                    out.println(comando);
                    String risposta = in.readLine();
                    
                    if (risposta != null) {
                        if (risposta.startsWith("ERRORE:")) System.out.println(risposta);
                        else visualizzaRisposta(comando, risposta);
                    }
                } else {
                    System.out.println("Comando non riconosciuto. Usa SERVI, STATO o QUIT");
                }
            }
            
        } catch (IOException e) {
            System.err.println("Errore connessione: " + e.getMessage());
        }
    }
    
    private static void mostraMenu() {
        System.out.println("\nComandi disponibili:");
        System.out.println("  SERVI - Segna come servito e passa al prossimo");
        System.out.println("  STATO - Visualizza stato corrente");
        System.out.println("  QUIT  - Disconnetti");
    }
    
    private static void visualizzaRisposta(String comando, String risposta) {
        String[] parti = risposta.split("\\|");
        
        if (comando.equals("SERVI")) {
            System.out.println("\n✓ Cliente servito!");
            for (String parte : parti) {
                String[] kv = parte.split(":");
                if (kv.length == 2) {
                    if (kv[0].equals("SERVITO")) {
                        System.out.println("  Numero servito: " + kv[1]);
                    } else if (kv[0].equals("ATTESA")) {
                        System.out.println("  Persone in attesa: " + kv[1]);
                    }
                }
            }
        } else if (comando.equals("STATO")) {
            System.out.println("\n--- STATO SISTEMA ---");
            for (String parte : parti) {
                String[] kv = parte.split(":");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "SERVIZIO":
                            System.out.println("Ultimo numero completato: " + kv[1]);
                            break;
                        case "DISTRIBUITI":
                            System.out.println("Numeri distribuiti: " + kv[1]);
                            break;
                        case "ATTESA":
                            System.out.println("Persone in attesa: " + kv[1]);
                            break;
                    }
                }
            }
            System.out.println("--------------------");
        }
    }
}
```

## Compilazione ed esecuzione

Dalla cartella in cui hai salvato i tre file:

```sh
javac -encoding UTF-8 DispenserServer.java DispenserClient.java NegozioClient.java
java DispenserServer
```

In terminali separati esegui `java DispenserClient` e `java NegozioClient`. Per provare STATO prima di prenotare usa un client TCP a righe; il DispenserClient di riferimento invia solo PRENOTA. Arresta il server con Ctrl+C.

## Prove richieste

- Due richieste PRENOTA simultanee restituiscono numeri diversi e consecutivi.
- NEGOZIO → STATO → SERVI → STATO funziona sulla stessa connessione; dopo due PRENOTA, distribuiti=2.
- SERVI non supera il numero distribuito; sulla coda vuota restituisce errore e lascia invariati i contatori.
- QUIT produce BYE e chiusura; una disconnessione improvvisa non arresta il listener.
- Una richiesta inviata in più scritture e più comandi inviati insieme rispettano comunque il protocollo a righe.

## Consegna e valutazione

Consegna sorgenti, README con comandi di avvio, descrizione del protocollo ed evidenze delle prove concorrenti. Indica il tuo repository e i risultati effettivi; la scadenza è quella comunicata dal docente. Valutazione: protocollo 30%, sincronizzazione 30%, gestione risorse/errori 20%, prove e documentazione 20%.

Approfondimenti facoltativi: [negozio PHP](README_avanzato.md), limite di connessioni, timeout e shutdown controllato. Non modificare il significato di ATTESA tra client e server.

[Indice esercizi](../README.md) · [Prima: chat](../a-ChatServer-Multithreaded/README.md) · [Dopo: prenotazioni](../c-Prenotazioni/README.md)
