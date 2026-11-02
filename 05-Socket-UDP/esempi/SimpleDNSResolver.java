import java.net.*;
/** Resolver del sistema: può usare cache, hosts e DNS secondo la configurazione OS.
 * Non invia un protocollo testuale inventato a un server DNS.
 */
public class SimpleDNSResolver {
    public static String resolveDomain(String domain) {
        try { return InetAddress.getByName(domain).getHostAddress(); }
        catch (UnknownHostException e) { return "Errore risoluzione: " + e.getMessage(); }
    }
    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "localhost";
        System.out.println(name + " -> " + resolveDomain(name));
    }
}
