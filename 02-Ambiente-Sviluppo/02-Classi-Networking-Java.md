# 2. Classi Networking Java

## Introduzione
Il package `java.net` fornisce tutte le classi necessarie per la programmazione di rete in Java. Conoscere le classi principali e le loro relazioni è fondamentale per sviluppare applicazioni di rete efficaci.

## Teoria

### Panoramica java.net

Il package `java.net` contiene oltre 40 classi organizzate in categorie funzionali:

#### Classi Socket TCP
- **Socket**: Client TCP
- **ServerSocket**: Server TCP  
- **SocketAddress**: Astrazione endpoint
- **InetSocketAddress**: Implementazione concreta

#### Classi Socket UDP
- **DatagramSocket**: Socket UDP base
- **MulticastSocket**: Socket multicast (estende DatagramSocket)
- **DatagramPacket**: Contenitore dati UDP

#### Gestione Indirizzi
- **InetAddress**: Rappresenta indirizzi IP
- **Inet4Address**: Indirizzi IPv4 specifici
- **Inet6Address**: Indirizzi IPv6 specifici
- **NetworkInterface**: Interfacce di rete del sistema

#### Utility e URL
- **URL**: Uniform Resource Locator
- **URLConnection**: Connessione basata su URL
- **URI**: Uniform Resource Identifier

### Gerarchia Classi Socket

```
Object
├── Socket
├── ServerSocket  
├── DatagramSocket
│   └── MulticastSocket
├── SocketAddress (abstract)
│   └── InetSocketAddress
└── InetAddress (abstract)
    ├── Inet4Address
    └── Inet6Address
```

## 🔗 Esempi di Riferimento

- [Esempio 01: Panoramica API](./esempi/PanoramicaAPI.java) - Esplora le principali classi java.net

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](01-Setup-Ambiente-Sviluppo.md)
- [➡️ Guida Successiva](03-Strumenti-Debug-Networking.md)