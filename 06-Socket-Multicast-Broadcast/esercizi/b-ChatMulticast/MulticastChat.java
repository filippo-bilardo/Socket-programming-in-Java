/**
 * 06-Socket-Multicast-Broadcast/esercizi/b-ChatMulticast/MulticastChat.java
 *
 * Obiettivo: Scheletro dell’esercizio b: progettare una chat con ricezione concorrente e uscita locale.
 * Protocollo: Da implementare secondo README.md: gruppo 239.255.0.3:5001, TTL 1, massimo 1024 byte.
 * Parametri: Il main è intenzionalmente incompleto e segnala il TODO all’avvio.
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java MulticastChat (solo dopo aver completato lo scheletro)
 *
 * Prevedi identità dell’istanza, sequenza, un lettore di console e un thread ricevitore. /quit deve chiudere solo questa istanza.
 */

/** Scheletro da completare; le specifiche e le prove sono in README.md.
 * Il programma deve usare MulticastSupport, un solo lettore da tastiera e
 * un thread ricevitore. /quit è locale: chiude il socket per sbloccare receive.
 */
public class MulticastChat {
    // Pianifica prima il ciclo di vita: apertura, join, avvio della ricezione, input console e arresto locale.
    public static void main(String[] args) {
        // Sostituisci questo segnale esplicito con l’implementazione richiesta dal README: non avviare thread con cicli vuoti.
        throw new UnsupportedOperationException("TODO: implementa la chat seguendo README.md prima di avviarla");
    }
}
