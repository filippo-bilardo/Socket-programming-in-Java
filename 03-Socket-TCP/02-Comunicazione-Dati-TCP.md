# 2. Comunicazione Dati TCP

## Introduzione
Una volta stabilita la connessione TCP, la comunicazione avviene tramite stream di input e output. Comprendere come gestire questi flussi è essenziale per lo scambio efficace di dati.

## Teoria

### InputStream e OutputStream

```java
Socket socket = new Socket("localhost", 8080);

// Stream di base (byte)
InputStream in = socket.getInputStream();
OutputStream out = socket.getOutputStream();

// Stream con buffer (più efficienti)
BufferedInputStream bufferedIn = new BufferedInputStream(in);
BufferedOutputStream bufferedOut = new BufferedOutputStream(out);

// Stream per testo
BufferedReader reader = new BufferedReader(new InputStreamReader(in));
PrintWriter writer = new PrintWriter(out, true); // auto-flush
```

### Invio e Ricezione Messaggi

#### Invio Dati Testuali
```java
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
out.println("Hello Server!");
out.println("Secondo messaggio");
```

#### Ricezione Dati Testuali
```java
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
String message = in.readLine();
while (message != null) {
    System.out.println("Ricevuto: " + message);
    message = in.readLine();
}
```

#### Invio Dati Binari
```java
OutputStream out = socket.getOutputStream();
byte[] data = {1, 2, 3, 4, 5};
out.write(data);
out.flush();
```

### Serializzazione Oggetti

```java
// Invio oggetti serializzabili
ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
Person person = new Person("Mario", 30);
objOut.writeObject(person);

// Ricezione oggetti
ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
Person received = (Person) objIn.readObject();
```

### Pattern Comunicazione

#### Request-Response
```java
// Client
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

out.println("GET /data");
String response = in.readLine();
System.out.println("Response: " + response);
```

#### Streaming Continuo
```java
// Server - invio dati continuo
PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
while (isActive) {
    String data = generateData();
    out.println(data);
    Thread.sleep(1000);
}
```

## 🔗 Esempi di Riferimento

- [Esempio 01: Echo Client-Server](./esempi/EchoClientServer.java) - Comunicazione bidirezionale
- [Esempio 02: File Transfer](./esempi/FileTransfer.java) - Trasferimento file via TCP

## 💡 Best Practices

- **Buffer i/o**: Usa sempre BufferedReader/Writer per testo
- **Flush esplicito**: Assicurati che i dati vengano inviati
- **Protocollo definito**: Stabilisci format e delimitatori chiari
- **Gestione EOF**: Controlla sempre null per fine stream

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](01-Creazione-Socket-TCP.md)
- [➡️ Guida Successiva](03-Chiusura-Connessioni.md)