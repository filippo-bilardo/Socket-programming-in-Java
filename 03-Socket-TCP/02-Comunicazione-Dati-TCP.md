# 2. Comunicazione Dati TCP

## Introduzione
Una volta stabilita la connessione TCP, la comunicazione avviene tramite stream di input e output. Comprendere come gestire questi flussi è essenziale per lo scambio efficace di dati.

## Teoria

TCP trasporta un flusso ordinato di byte: una scrittura non corrisponde
necessariamente a una lettura o a un singolo segmento. Il protocollo deve
indicare dove termina un messaggio: riga, lunghezza prefissata o EOF.
Scegli un solo tipo di reader per ciascuna direzione del flusso: non alternare
reader a caratteri, stream binari e buffer diversi sugli stessi byte.

### InputStream e OutputStream

```java
Socket socket = new Socket("localhost", 8080);

// Stream di base (byte)
InputStream in = socket.getInputStream();
OutputStream out = socket.getOutputStream();

// Alternativa per dati binari con buffer (scegli questa oppure gli stream per testo)
BufferedInputStream bufferedIn = new BufferedInputStream(in);
BufferedOutputStream bufferedOut = new BufferedOutputStream(out);

// Alternativa per testo: non usare contemporaneamente bufferedIn e reader
BufferedReader reader = new BufferedReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
PrintWriter writer = new PrintWriter(out, true, java.nio.charset.StandardCharsets.UTF_8); // auto-flush
```
![immagine](img_02_stream.png)

### Invio e Ricezione Messaggi

#### Invio Dati Testuali
```java
PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
out.println("Hello Server!");
out.println("Secondo messaggio");
```

#### Ricezione Dati Testuali
```java
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
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

### Framing binario Java

L'[esempio EchoService](esercizi/a-EchoService/README.md) usa `writeUTF` e
`readUTF`: una lunghezza di due byte seguita dal testo in UTF modificato Java,
con un massimo di 65535 byte per messaggio. Questo formato è diverso da una
riga UTF-8 inviata con `println`: i due endpoint devono usare lo stesso framing.

### Serializzazione Oggetti

```java
// Invio oggetti serializzabili
ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
Person person = new Person("Mario", 30);
objOut.writeObject(person);
objOut.flush();

// Ricezione oggetti
ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
Person received = (Person) objIn.readObject();
```

`Person` deve essere una classe disponibile a entrambi i programmi e implementare
`Serializable`. Se entrambi inviano e ricevono oggetti, crea e scarica con flush
l'`ObjectOutputStream` prima di costruire l'`ObjectInputStream`, così non
rimangono entrambi in attesa dell'header del peer.

### Pattern Comunicazione

#### Request-Response
```java
// Client
PrintWriter out = new PrintWriter(socket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));

out.println("LEGGI dati"); // Protocollo di esempio a righe, non HTTP
String response = in.readLine();
System.out.println("Response: " + response);
```

#### Streaming Continuo
```java
// Server - invio dati continuo
PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
while (isActive) {
    String data = generateData();
    out.println(data);
    Thread.sleep(1000);
}
```

## 🔗 Esempi di Riferimento

- [Esempio a: Echo Client-Server](esercizi/a-EchoService/README.md) - Comunicazione bidirezionale
- [Approfondimento: File Transfer](../03-Socket-TCP/esempi/FileTransfer.java) - Trasferimento file via TCP

## 💡 Best Practices

- **Buffer i/o**: Usa sempre BufferedReader/Writer per testo
- **Flush esplicito**: Assicurati che i dati vengano inviati
- **Protocollo definito**: Stabilisci format e delimitatori chiari
- **Gestione EOF**: Controlla sempre null per fine stream

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](01-Creazione-Socket-TCP.md)
- [➡️ Guida Successiva](03-Chiusura-Connessioni.md)
