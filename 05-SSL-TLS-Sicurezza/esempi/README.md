# Esempi Pratici SSL/TLS

Questa cartella contiene esempi pratici per imparare la programmazione SSL/TLS con socket Java.

## 📁 Contenuto

### Esempi Base
- **`SimpleSSLServer.java`** - Server SSL di base con supporto HTTP-like
- **`SimpleSSLClient.java`** - Client SSL per test connessioni sicure
- **`CertificateGenerator.java`** - Utility per generazione certificati demo

### Esempi Avanzati  
- **`SecureChatServer.java`** - Server chat multi-client con SSL
- **`SecureChatClient.java`** - Client chat interattivo sicuro

### Utilities
- **`run_ssl_examples.sh`** - Script automatico per compilazione ed esecuzione
- **`README.md`** - Questa guida

## 🚀 Avvio Rapido

### Modalità Automatica
```bash
# Rendi eseguibile lo script (una volta sola)
chmod +x run_ssl_examples.sh

# Menu interattivo
./run_ssl_examples.sh

# Oppure comandi diretti
./run_ssl_examples.sh compile    # Compila tutto
./run_ssl_examples.sh server     # Avvia server SSL
./run_ssl_examples.sh client     # Avvia client SSL
./run_ssl_examples.sh test       # Test automatico
```

### Modalità Manuale

#### 1. Generazione Certificati
```bash
# Compila il generatore
javac CertificateGenerator.java

# Genera certificati demo
java CertificateGenerator
```

#### 2. Server SSL Semplice
```bash
# Compila e avvia
javac SimpleSSLServer.java
java SimpleSSLServer 8443

# Test con browser: https://localhost:8443
```

#### 3. Client SSL Semplice
```bash
# In un altro terminale
javac SimpleSSLClient.java
java SimpleSSLClient localhost 8443
```

#### 4. Chat Server Sicuro
```bash
# Compila e avvia chat server
javac SecureChatServer.java
java SecureChatServer 8443
```

#### 5. Chat Client Sicuro
```bash
# Client chat (multipli supportati)
javac SecureChatClient.java
java SecureChatClient localhost 8443
```

## 🔧 Requisiti

- **Java JDK 11+** (per SSL moderno)
- **keytool** (incluso nel JDK)
- **Terminale** con supporto ANSI (per colori)

## 📋 Comandi Chat

Nel client chat sono disponibili:

- **Messaggi normali**: digita e premi INVIO
- **`/users`** - Lista utenti connessi  
- **`/help`** - Mostra aiuto comandi
- **`/private [utente] [messaggio]`** - Messaggio privato
- **`/quit`** - Disconnetti e esci
- **`/clear`** - Pulisci schermo (locale)
- **`/status`** - Info connessione SSL

## 🔒 Sicurezza

### Certificati Demo
- **Keystore**: `server.jks` (password: `password123`)
- **Algoritmo**: RSA 2048-bit
- **Validità**: 365 giorni
- **CN**: localhost (per test locali)

⚠️ **ATTENZIONE**: I certificati generati sono solo per DEMO! 
Per produzione usa certificati firmati da CA attendibili.

### Configurazione SSL
- **Protocolli**: TLS 1.2 e 1.3
- **Cipher Suites**: AES-GCM prioritarie
- **Validazione**: Bypassata per localhost (demo mode)

## 🧪 Test e Debug

### Test Automatici
```bash
# Test completo server+client
./run_ssl_examples.sh test

# Test multi-client (da Java)
java SecureChatClient localhost 8443
# In altri terminali, ripeti il comando
```

### Debug SSL
Aggiungi a comando Java per debug SSL:
```bash
java -Djavax.net.debug=ssl SimpleSSLServer 8443
```

### Test Prestazioni
```bash
# Test carico con curl (se server running)
for i in {1..10}; do
    curl -k https://localhost:8443 &
done
wait
```

## 📚 Spiegazione Esempi

### SimpleSSLServer
- Server HTTP-like su SSL
- Gestione multipli client
- Auto-generazione keystore
- Response HTML di base
- Logging connessioni

**Caratteristiche**:
- Porta configurabile
- SSL context automatico  
- Gestione errori robusta
- Shutdown graceful

### SimpleSSLClient
- Client per test connessioni SSL
- Bypass certificati per demo
- Info dettagliate handshake
- Test con server esterni

**Caratteristiche**:
- Validazione certificati configurabile
- Timing handshake SSL
- Report cipher suites
- Test HTTPS generico

### SecureChatServer  
- Chat room multi-utente
- Autenticazione SSL client
- Rate limiting anti-spam
- Comandi amministrativi

**Caratteristiche**:
- Thread pool gestione client
- Broadcast messaggi
- Lista utenti dinamica
- Logging attività

### SecureChatClient
- Chat client interattivo
- Riconnessione automatica
- Comandi chat avanzati
- UI terminale colorata

**Caratteristiche**:
- Thread ricezione asincrona
- Comandi locali (/clear, /status)
- Gestione disconnessioni
- Input validation

## 🐛 Risoluzione Problemi

### Errore "SSL handshake failed"
```bash
# Verifica certificati
keytool -list -keystore server.jks -storepass password123

# Rigenera certificati
rm server.jks
java CertificateGenerator
```

### Errore "Address already in use"
```bash
# Trova processo su porta
lsof -i :8443
# Termina processo
kill -9 [PID]
```

### Errore "Certificate not trusted"
- Per test: usa bypass nel client
- Per produzione: installa certificato in truststore

### Client non si connette
1. Verifica server in ascolto: `netstat -an | grep 8443`
2. Test connessione base: `telnet localhost 8443`
3. Controlla firewall/iptables

## 💡 Esercizi Suggeriti

1. **Modifica cipher suites** in SimpleSSLServer
2. **Aggiungi autenticazione client** con certificati
3. **Implementa file transfer** sicuro
4. **Crea proxy SSL** con inoltro richieste  
5. **Aggiungi logging avanzato** con rotazione file

## 📖 Riferimenti

- [Java SSL Documentation](https://docs.oracle.com/en/java/javase/11/security/java-secure-socket-extension-jsse-reference-guide.html)
- [Keytool Documentation](https://docs.oracle.com/en/java/javase/11/tools/keytool.html)
- [TLS RFCs](https://tools.ietf.org/html/rfc8446) (TLS 1.3)

## 📝 Note

- Tutti gli esempi includono gestione errori completa
- Codice commentato per scopo didattico
- Configurazioni SSL sicure di default
- Supporto sia IPv4 che IPv6
- Compatibilità JDK 8+ (con adattamenti)

Buono studio con SSL/TLS! 🔐