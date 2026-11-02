/**
 * 05-Socket-UDP/esempi/SimpleDNSResolver.java
 *
 * Obiettivo: Risolvere un nome usando il resolver configurato nel sistema operativo.
 * Protocollo: La risoluzione è delegata a InetAddress: può coinvolgere hosts, cache e DNS.
 * Parametri: nome da risolvere (default: localhost).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java SimpleDNSResolver localhost
 *
 * Non costruisce un datagramma DNS: confrontalo con SimpleDNSQuery.java.
 */

import java.net.*;
/** Resolver del sistema: può usare cache, hosts e DNS secondo la configurazione OS.
 * Non invia un protocollo testuale inventato a un server DNS.
 */
public class SimpleDNSResolver {
    // Delega al resolver del sistema: può utilizzare file hosts, cache o DNS secondo la configurazione locale.
    public static String resolveDomain(String domain) {
        try { return InetAddress.getByName(domain).getHostAddress(); }
        catch (UnknownHostException e) { return "Errore risoluzione: " + e.getMessage(); }
    }
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "localhost";
        System.out.println(name + " -> " + resolveDomain(name));
    }
}
