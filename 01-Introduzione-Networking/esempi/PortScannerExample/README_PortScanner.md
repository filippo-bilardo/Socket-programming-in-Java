# 🔍 Port Scanner Multithread

## Descrizione
Port scanner avanzato che utilizza multithreading per scansionare rapidamente le porte aperte di un host specificato. Supporta sia indirizzi IP che nomi host.

## Caratteristiche
- ⚡ **Multithreading**: Utilizza pool di thread per scansioni veloci
- 📊 **Progress in tempo reale**: Mostra avanzamento durante la scansione
- 🎯 **Range personalizzabile**: Scansiona porte specifiche o range completo
- 🔍 **Riconoscimento servizi**: Identifica servizi comuni sulle porte aperte
- 📈 **Statistiche dettagliate**: Report completo con performance metrics

## Utilizzo

### Sintassi
```bash
java PortScannerMultithread <host> [porta_inizio] [porta_fine] [num_thread]
```

### Parametri
- **host** (obbligatorio): IP o nome host da scansionare
- **porta_inizio** (opzionale): Porta iniziale (default: 1)
- **porta_fine** (opzionale): Porta finale (default: 65535)
- **num_thread** (opzionale): Numero di thread concorrenti (default: 100)

### Esempi di Utilizzo

#### 1. Scansione completa con parametri di default
```bash
java PortScannerMultithread google.com
```
Scansiona tutte le porte (1-65535) su google.com con 100 thread.

#### 2. Scansione range specifico
```bash
java PortScannerMultithread 192.168.1.1 1 1000
```
Scansiona le porte 1-1000 su 192.168.1.1.

#### 3. Scansione veloce con più thread
```bash
java PortScannerMultithread localhost 8000 9000 200
```
Scansiona le porte 8000-9000 su localhost usando 200 thread.

#### 4. Test porte comuni
```bash
java PortScannerMultithread scanme.nmap.org 20 100 50
```
Scansiona le porte 20-100 su scanme.nmap.org con 50 thread.

## Compilazione e Test

### Compilazione
```bash
javac PortScannerMultithread.java
```

### Test Automatico
Esegui lo script di test incluso:
```bash
./test_scanner.sh
```

## Output

### Durante la Scansione
```
🔍 Avvio scansione porte
==================================================
🎯 Target: google.com
📊 Range porte: 1-1000 (1000 porte totali)
🧵 Thread concorrenti: 100
⏱️  Timeout per porta: 1000 ms
==================================================

✅ APERTA: google.com:80 (HTTP)
✅ APERTA: google.com:443 (HTTPS)
📊 Progress: 45.2% (452/1000) - Aperte: 2
```

### Risultati Finali
```
============================================================
📋 RISULTATI SCANSIONE COMPLETA
============================================================
🎯 Host: google.com
📊 Range: 1-1000
⏱️  Durata: 12.34 secondi
🔢 Porte totali: 1000
✅ Porte aperte: 2
❌ Porte chiuse: 998

🔓 PORTE APERTE TROVATE:
------------------------------
  80 (HTTP)
  443 (HTTPS)

⚡ Performance: 81.0 porte/secondo
============================================================
```

## Servizi Riconosciuti

Il programma identifica automaticamente i servizi comuni:

| Porta | Servizio | Descrizione |
|-------|----------|-------------|
| 21 | FTP | File Transfer Protocol |
| 22 | SSH | Secure Shell |
| 23 | Telnet | Telnet Protocol |
| 25 | SMTP | Simple Mail Transfer Protocol |
| 53 | DNS | Domain Name System |
| 80 | HTTP | Hypertext Transfer Protocol |
| 110 | POP3 | Post Office Protocol v3 |
| 143 | IMAP | Internet Message Access Protocol |
| 443 | HTTPS | HTTP Secure |
| 993 | IMAPS | IMAP over SSL |
| 995 | POP3S | POP3 over SSL |
| 3306 | MySQL | MySQL Database |
| 5432 | PostgreSQL | PostgreSQL Database |
| 6379 | Redis | Redis Database |
| 8080 | HTTP Alt | HTTP Alternative |
| 8443 | HTTPS Alt | HTTPS Alternative |

## Configurazioni

### Performance Tuning
- **Thread Count**: Più thread = scansione più veloce, ma maggior carico CPU
- **Timeout**: Timeout più basso = scansione più veloce, ma possibili false negative
- **Range**: Range più piccoli per test rapidi, range completi per sicurezza

### Raccomandazioni
- **LAN**: 200-500 thread per reti locali veloci
- **WAN**: 50-100 thread per host remoti
- **Localhost**: 100-200 thread per test locali

## Limitazioni
- Alcuni firewall potrebbero rilevare la scansione
- Host con rate limiting potrebbero bloccare connessioni eccessive
- Timeout fisso (1000ms) potrebbe essere troppo basso per connessioni lente

## Esempi Target di Test
- **scanme.nmap.org**: Host pubblico per test di port scanning
- **localhost**: Per testare servizi locali
- **192.168.1.1**: Router di rete tipico
- **google.com**: Test servizi web pubblici

## Note di Sicurezza
⚠️ **Importante**: Utilizzare solo su host di propria proprietà o con esplicita autorizzazione. Il port scanning non autorizzato può essere considerato illegale in alcune giurisdizioni.