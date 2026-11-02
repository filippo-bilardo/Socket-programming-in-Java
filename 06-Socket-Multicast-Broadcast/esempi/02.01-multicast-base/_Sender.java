/**
 * 06-Socket-Multicast-Broadcast/esempi/02.01-multicast-base/_Sender.java
 *
 * Obiettivo: Mantenere un nome alternativo senza duplicare l’implementazione del programma.
 * Protocollo: Quello di Sender, nella stessa cartella.
 * Parametri: Gli argomenti espliciti vengono inoltrati alla classe delegata.
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java _Sender
 *
 * Leggi il codice della classe delegata per socket, protocollo e gestione delle risorse.
 */

/** Alias della versione commentata precedente. Vedere README.md. */
public class _Sender {
    // Delega alla versione completa della stessa cartella; i valori predefiniti, se presenti, sono condivisi dalla coppia base.
    public static void main(String[] args) throws Exception { Sender.main(args); }
}
