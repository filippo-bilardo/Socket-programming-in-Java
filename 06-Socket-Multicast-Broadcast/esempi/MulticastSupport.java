/**
 * 06-Socket-Multicast-Broadcast/esempi/MulticastSupport.java
 *
 * Obiettivo: Condividere la configurazione IPv4 multicast fra i programmi della stessa cartella.
 * Protocollo: Gruppo e porta del chiamante; interfaccia scelta tramite MULTICAST_IFACE o ricerca locale.
 * Parametri: È un’utility senza main: viene richiamata dagli esempi e dagli esercizi.
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   Compila insieme alla classe che utilizza MulticastSupport.
 *
 * Il gruppo è la destinazione; l’indirizzo dell’interfaccia è locale. Le due informazioni non sono intercambiabili.
 */

import java.io.*;
import java.net.*;
import java.util.*;
/** Selezione esplicita dell'interfaccia IPv4. MULTICAST_IFACE può contenere il suo indirizzo IPv4. */
public final class MulticastSupport {
    private MulticastSupport() { }
    // Accetta un gruppo IPv4 multicast, evitando di usare un normale indirizzo unicast come destinazione di gruppo.
    public static InetAddress group(String value) throws IOException {
        InetAddress group = InetAddress.getByName(value);
        if (!(group instanceof Inet4Address) || !group.isMulticastAddress() || value.equals("224.0.0.0"))
            throw new IOException("Richiesto un gruppo multicast IPv4 valido");
        return group;
    }
    public static NetworkInterface networkInterface() throws IOException {
        // La variabile contiene l’IPv4 della scheda locale: non l’indirizzo del gruppo.
        String selected = System.getenv("MULTICAST_IFACE");
        // Una scelta esplicita prevale su quella automatica; il loopback può servire per prove sul solo host.
        if (selected != null && !selected.isBlank()) {
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(selected));
            if (ni == null || !ni.isUp() || (!ni.supportsMulticast() && !ni.isLoopback())) throw new IOException("Interfaccia multicast non disponibile: " + selected);
            return ni;
        }
        // In assenza di configurazione, cerca una scheda attiva con IPv4 e capacità multicast.
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) {
                for (InetAddress address : Collections.list(ni.getInetAddresses()))
                    if (address instanceof Inet4Address) return ni;
            }
        }
        throw new IOException("Nessuna interfaccia IPv4 multicast attiva; imposta MULTICAST_IFACE");
    }
    public static MulticastSocket receiver(InetAddress group, int port, NetworkInterface ni) throws IOException {
        // Crea il socket ancora non associato: SO_REUSEADDR deve essere impostato prima del bind.
        MulticastSocket socket = new MulticastSocket(null);
        try {
            socket.setReuseAddress(true); // Prima del bind, per più ricevitori locali.
            socket.bind(new InetSocketAddress(port));
            socket.setNetworkInterface(ni);
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            // Aderire al gruppo abilita la ricezione sulla scheda indicata; non avvia un mittente.
            socket.joinGroup(new InetSocketAddress(group, port), ni);
            return socket;
        // Se bind o join falliscono, libera comunque il socket parzialmente inizializzato.
        } catch (IOException | RuntimeException e) { socket.close(); throw e; }
    }
}
