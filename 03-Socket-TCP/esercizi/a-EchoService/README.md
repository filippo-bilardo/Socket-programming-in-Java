# Esercizio a — EchoService con framing binario

Questa cartella contiene un client e un server completi. Il server riceve
stringhe e risponde con `Echo from server : ` seguito dal messaggio.
La porta predefinita è **8765**. Il messaggio `quit` viene confermato con l'echo,
poi chiude sia il client sia il server; anche EOF da tastiera chiude il client.

## Compilazione e prova

Dalla cartella dell'esempio, con un JDK 11 o successivo:

```bash
javac -encoding UTF-8 TcpServer.java TcpClient.java
```

Nel terminale A avvia il server, poi in B avvia il client:

| Operazione | PowerShell Windows / Bash Linux |
|---|---|
| Server A | `java TcpServer` |
| Client B sullo stesso computer | `java TcpClient` |
| Server su una porta diversa | `java TcpServer 8766` |
| Client verso quella porta | `java TcpClient 127.0.0.1 8766` |

Se lavori su Web4Student usa due sessioni SSH sullo stesso server. Per collegare
un client da un altro computer, passa come primo argomento l'IP o il nome del
server, verificando con il docente la raggiungibilità della porta.

Prova una frase, una stringa vuota e `quit`. Ripeti anche la prova con EOF da
tastiera: Ctrl+D su Linux/Git Bash, Ctrl+Z e Invio nel terminale Windows quando
supportato. Il client deve terminare senza una NullPointerException.

## Protocollo

`DataOutputStream.writeUTF` scrive una lunghezza di due byte e il testo in
**UTF modificato Java**; `DataInputStream.readUTF` legge lo stesso formato.
Ogni messaggio può occupare al massimo 65535 byte nella codifica modificata.
Non è un protocollo a righe: browser, curl e netcat non sostituiscono questo
client. I due endpoint devono concordare lo stesso framing.

Il server serve un solo client e termina alla fine della sessione. Avvialo di
nuovo per una seconda prova. Il client limita l'apertura a 5 secondi e l'attesa
della risposta a 10 secondi; l'input da tastiera non ha quel timeout.

## Attività

Descrivi il framing e verifica la chiusura ordinata e quella per EOF.
Come estensione, aggiungi al server un ciclo per servire più sessioni in sequenza.

[Indice del modulo](../../README.md)
