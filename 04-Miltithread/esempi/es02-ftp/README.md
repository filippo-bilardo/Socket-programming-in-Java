# Helper di trasferimento file

`FileClient.sendFile(Socket, String)` e `FileServer.receiveFile(Socket, String)` sono metodi di supporto, senza main. Il chiamante crea la connessione e stabilisce il nome del file ricevuto. Compilazione: `javac -encoding UTF-8 FileClient.java FileServer.java`.

Il ricevitore legge fino a EOF; il mittente deve chiudere l'invio al termine. Questi helper chiudono lo stream e quindi il socket: non prevedono altri messaggi sulla stessa connessione. Il protocollo non include nome, dimensione o checksum. Per un esempio completo con metadata e controllo integrità vedi [FileTransfer](../FileTransfer.java) e le [istruzioni degli esempi](../README.md).
