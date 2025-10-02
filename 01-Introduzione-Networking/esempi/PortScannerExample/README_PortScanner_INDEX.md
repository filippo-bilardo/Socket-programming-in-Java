# 📁 Port Scanner Multithread - Indice File

## 📋 Struttura Progetto

```
esempi/
├── TestConnettivita.java           # 🔄 Codice originale (single thread)
├── PortScannerMultithread.java     # 🚀 Port scanner multithread principale  
├── PortScannerExample.java         # 📖 Esempi di utilizzo
├── test_scanner.sh                 # 🧪 Script di test automatico
├── README_PortScanner.md          # 📚 Guida utente completa
├── TECHNICAL_DOCS.md              # 🔧 Documentazione tecnica
└── INDEX.md                       # 📁 Questo file
```

## 🎯 Descrizione File

### File Principali

#### `PortScannerMultithread.java`
- **Tipo**: Applicazione Java principale
- **Funzione**: Port scanner multithread completo
- **Utilizzo**: `java PortScannerMultithread <host> [start] [end] [threads]`
- **Caratteristiche**: 
  - Multithreading con pool configurabile
  - Progress reporting in tempo reale
  - Riconoscimento servizi automatico
  - Statistiche performance dettagliate

#### `TestConnettivita.java`  
- **Tipo**: Codice originale di riferimento
- **Funzione**: Test connettività singolo thread
- **Utilizzo**: Per confronto e apprendimento
- **Caratteristiche**: Implementazione base e semplice

### File di Supporto

#### `PortScannerExample.java`
- **Tipo**: Classe di esempio
- **Funzione**: Mostra casi d'uso comuni
- **Utilizzo**: `java PortScannerExample`
- **Contenuto**: Esempi pratici e suggerimenti

#### `test_scanner.sh`
- **Tipo**: Script Bash
- **Funzione**: Test automatizzati
- **Utilizzo**: `./test_scanner.sh`
- **Caratteristiche**: Test progressivi con esempi

### Documentazione

#### `README_PortScanner.md`
- **Tipo**: Guida utente
- **Contenuto**: 
  - Installazione e compilazione
  - Esempi di utilizzo completi
  - Parametri e configurazioni
  - Troubleshooting

#### `TECHNICAL_DOCS.md`
- **Tipo**: Documentazione tecnica
- **Contenuto**:
  - Architettura del sistema
  - Pattern di design utilizzati
  - Ottimizzazioni performance
  - Considerazioni sicurezza

## 🚀 Quick Start

### 1. Compilazione
```bash
javac PortScannerMultithread.java
```

### 2. Test Rapido
```bash
java PortScannerMultithread localhost 80 85 10
```

### 3. Test Completo
```bash
./test_scanner.sh
```

### 4. Esempi Avanzati
```bash
java PortScannerExample
```

## 📊 Comparazione Implementazioni

| Caratteristica | TestConnettivita | PortScannerMultithread |
|----------------|------------------|------------------------|
| **Threading** | Single thread | Multithread (pool configurabile) |
| **Performance** | ~1-5 porte/sec | ~100-500 porte/sec |
| **Scalabilità** | Limitata | Alta (1-65535 porte) |
| **Progress** | No | Sì (tempo reale) |
| **Statistiche** | Base | Dettagliate |
| **Servizi** | No | Riconoscimento automatico |
| **CLI** | Hardcoded | Parametri flessibili |
| **Gestione errori** | Base | Avanzata |

## 🎓 Obiettivi Didattici

### Concetti Appresi
1. **Multithreading in Java**
   - ExecutorService e Thread Pool
   - Callable e Future
   - Thread-safe collections

2. **Network Programming**
   - Socket TCP connection testing  
   - Timeout e gestione errori
   - Performance optimization

3. **Concurrent Programming**
   - AtomicInteger per contatori thread-safe
   - Synchronized collections
   - Producer-Consumer pattern

4. **Software Design**
   - Modularità e separazione responsabilità
   - CLI design e usabilità
   - Error handling robusто

## 🔧 Possibili Estensioni

### Livello Principiante
- [ ] Aggiungere più servizi riconosciuti
- [ ] Migliorare output formattato
- [ ] Aggiungere logging file

### Livello Intermedio  
- [ ] Implementare UDP scanning
- [ ] Service detection avanzata
- [ ] Export risultati (XML/JSON/CSV)

### Livello Avanzato
- [ ] Raw socket implementation (SYN scan)
- [ ] OS fingerprinting
- [ ] Distributed scanning
- [ ] Integration con Nmap

## 📚 Riferimenti Corso

- **Modulo**: 01-Introduzione-Networking
- **Guida**: 02-Introduzione-ai-Socket.md  
- **Prerequisiti**: Concetti base TCP/IP e Java
- **Livello**: Intermedio-Avanzato

## 🔗 Link Utili

- [Java Socket Documentation](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html)
- [ExecutorService Guide](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)
- [Port Numbers Registry](https://www.iana.org/assignments/service-names-port-numbers/)
- [Nmap Port Scanning Guide](https://nmap.org/book/man-port-scanning-basics.html)

---
*Creato per il corso "Socket Programming in Java" - ITES "Alessandro Volta"*