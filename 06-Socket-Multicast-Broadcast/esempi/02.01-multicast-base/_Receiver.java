/**
 * 06-Socket-Multicast-Broadcast/esempi/02.01-multicast-base/_Receiver.java
 *
 * Obiettivo: Mantenere un nome alternativo senza duplicare l’implementazione del programma.
 * Protocollo: Quello di Receiver, nella stessa cartella.
 * Parametri: Gli argomenti espliciti vengono inoltrati alla classe delegata.
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java _Receiver
 *
 * Leggi il codice della classe delegata per socket, protocollo e gestione delle risorse.
 */

/** Alias della versione commentata precedente. Vedere README.md. */
public class _Receiver {
    // Delega alla versione completa della stessa cartella; i valori predefiniti, se presenti, sono condivisi dalla coppia base.
    public static void main(String[] args) throws Exception { Receiver.main(args); }
}
