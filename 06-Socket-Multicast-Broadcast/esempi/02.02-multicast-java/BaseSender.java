/**
 * 06-Socket-Multicast-Broadcast/esempi/02.02-multicast-java/BaseSender.java
 *
 * Obiettivo: Mantenere un nome alternativo senza duplicare l’implementazione del programma.
 * Protocollo: Quello di MulticastSender. BaseSender/BaseReceiver usano 239.255.0.1:19876 e tre messaggi.
 * Parametri: Gli argomenti espliciti vengono inoltrati alla classe delegata.
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java BaseSender
 *
 * Leggi il codice della classe delegata per socket, protocollo e gestione delle risorse.
 */

/** Variante finita: tre datagrammi, stesso gruppo e stessa porta su entrambi i lati. */
public class BaseSender {
    // Delega alla versione completa della stessa cartella; i valori predefiniti, se presenti, sono condivisi dalla coppia base.
    public static void main(String[] args) throws Exception { MulticastSender.main(args.length == 0 ? new String[]{"239.255.0.1", "19876", "100", "1", "3"} : args); }
}
