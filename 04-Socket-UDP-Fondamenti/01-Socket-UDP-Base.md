# 1. Socket UDP Base

## Introduzione
I socket UDP offrono comunicazione veloce e senza connessione. Comprendere DatagramSocket e DatagramPacket è essenziale per applicazioni che privilegiano la velocità rispetto all'affidabilità.

## Teoria

### DatagramSocket

```java
// Creazione socket UDP
DatagramSocket socket = new DatagramSocket();        // Porta casuale
DatagramSocket socket = new DatagramSocket(8080);    // Porta specifica
DatagramSocket socket = new DatagramSocket(8080, 
    InetAddress.getByName("192.168.1.100"));        // IP e porta specifici
```

### DatagramPacket

```java
// Packet per invio
byte[] data = "Hello UDP".getBytes();
InetAddress address = InetAddress.getByName("localhost");
DatagramPacket packet = new DatagramPacket(data, data.length, address, 8080);

// Packet per ricezione
byte[] buffer = new byte[1024];
DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
```

### Invio e Ricezione

```java
// Client UDP
DatagramSocket socket = new DatagramSocket();
String message = "Hello Server";
byte[] data = message.getBytes();

DatagramPacket packet = new DatagramPacket(data, data.length, 
    InetAddress.getByName("localhost"), 8080);
socket.send(packet);

// Server UDP
DatagramSocket socket = new DatagramSocket(8080);
byte[] buffer = new byte[1024];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet); // Bloccante

String received = new String(packet.getData(), 0, packet.getLength());
InetAddress clientAddress = packet.getAddress();
int clientPort = packet.getPort();
```

## 🔗 Esempi di Riferimento

- [Esempio 01: Echo UDP](./esempi/EchoUDP.java) - Client/Server UDP base

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [➡️ Guida Successiva](02-Gestione-Eccezioni-UDP.md)