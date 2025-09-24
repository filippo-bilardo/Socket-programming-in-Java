# 1. Setup Ambiente di Sviluppo

## Introduzione
Un ambiente di sviluppo ben configurato è fondamentale per lo sviluppo efficace di applicazioni di rete. Questa guida vi aiuterà a configurare tutto il necessario per il corso di Socket Programming in Java.

## Teoria

### Requisiti Sistema

#### Java Development Kit (JDK)
```bash
# Verifica versione Java installata
java -version
javac -version

# Installazione su Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk

# Installazione su CentOS/RHEL
sudo yum install java-11-openjdk-devel
```

**Versioni consigliate**: JDK 11+ (LTS) per compatibilità ottimale.

#### Variabili d'Ambiente
```bash
# Aggiungi al ~/.bashrc o ~/.profile
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

### Configurazione IDE

#### IntelliJ IDEA
✅ **Configurazione consigliata**:
- **Project SDK**: Java 11+
- **Compiler compliance level**: 11
- **Encoding**: UTF-8
- **Line separator**: Unix (\n)

#### Visual Studio Code
📦 **Estensioni essenziali**:
- Extension Pack for Java
- Java Test Runner  
- Maven for Java
- Gradle for Java

#### Eclipse
⚙️ **Configurazioni importanti**:
- **Compiler compliance**: Java 11+
- **Text file encoding**: UTF-8
- **New line character**: Unix

### Strumenti Riga di Comando

#### Bash Shell Essentials
```bash
# Installazione di utilità di rete
sudo apt install net-tools iputils-ping telnet curl wget lsof
# installazione nc
sudo apt install netcat

# Navigazione efficace
alias ll='ls -la'
alias grep='grep --color=auto'
alias ports='netstat -tuln'

# Funzioni utili per networking
function port_check() {
    nc -zv $1 $2 2>&1
}

function find_process_on_port() {
    lsof -i :$1
}
```

#### Compilazione e Esecuzione
```bash
# Compilazione singolo file
javac NomeClasse.java

# Compilazione con classpath
javac -cp ./lib/* src/*.java

# Esecuzione
java NomeClasse

# Esecuzione con debugging
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 NomeClasse
```

### Strumenti di Rete Essenziali

#### Verifica Connettività
```bash
# Ping - testa raggiungibilità host
ping google.com
ping -c 4 192.168.1.1

# Telnet - testa porte TCP
telnet google.com 80
telnet localhost 8080

# Netcat - Swiss army knife di rete
nc -zv google.com 80        # Port scan
nc -l 8080                  # Listen su porta
echo "test" | nc localhost 8080  # Invia dati
```

#### Monitoraggio Rete
```bash
# Netstat - connessioni attive
netstat -tuln              # Porte in ascolto
netstat -tupln             # Con PID processi
netstat -i                 # Statistiche interfacce

# SS (sostituto moderno di netstat)
ss -tuln                   # Porte in ascolto
ss -tp                     # Connessioni con processi

# Lsof - file aperti (inclusi socket)
lsof -i :8080              # Processo su porta 8080
lsof -i tcp                # Tutte connessioni TCP
```

## 🔗 Esempi di Riferimento

- [Esempio 01: Test Ambiente](./esempi/TestAmbiente.java) - Verifica configurazione Java e rete
- [Esempio 02: Network Utils](./esempi/NetworkUtils.java) - Utilities per debugging di rete

## 💡 Best Practices, Tips & Tricks

- **Versioni JDK**: Usa versioni LTS (8, 11, 17, 21) per stabilità
- **Encoding**: Sempre UTF-8 per evitare problemi di caratteri
- **Debugging**: Configura remote debugging per applicazioni server
- **Logging**: Usa java.util.logging o SLF4J per tracciare operazioni di rete
- **Firewall**: Configura eccezioni per le porte di sviluppo

⚠️ **Errori Comuni da Evitare**:
- Non impostare JAVA_HOME correttamente
- Usare versioni Java diverse tra compilazione ed esecuzione
- Non configurare il proxy aziendale per Maven/Gradle
- Dimenticare di aprire porte nel firewall per testing

## 🧠 Verifica dell'Apprendimento

### Domande a Scelta Multipla

1. **Quale comando verifica se una porta TCP è aperta?**  
    a) ping hostname porta  
    b) telnet hostname porta  
    c) ssh hostname porta

2. **Dove si imposta JAVA_HOME su Linux?**  
    a) /etc/java/config  
    b) ~/.bashrc o ~/.profile  
    c) /var/java/settings

3. **Quale comando mostra i processi in ascolto su porte?**  
    a) netstat -tuln  
    b) ps aux  
    c) top

### Risposte alle Domande
1. **Risposta corretta: b)** Telnet può testare connessioni TCP verso porte specifiche.
2. **Risposta corretta: b)** Le variabili d'ambiente utente si impostano in ~/.bashrc o ~/.profile.
3. **Risposta corretta: a)** netstat -tuln mostra tutte le porte in ascolto (TCP e UDP).

### Proposte di Esercizi
- **Esercizio 1 (Facile)**: Configura il tuo ambiente e verifica che tutti i tool siano installati.
- **Esercizio 2 (Intermedio)**: Crea script bash per automatizzare compilazione ed esecuzione di progetti Java.
- **Esercizio 3 (Avanzato)**: Configura un ambiente di debugging remoto per applicazioni server.

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [➡️ Guida Successiva](02-Classi-Networking-Java.md)