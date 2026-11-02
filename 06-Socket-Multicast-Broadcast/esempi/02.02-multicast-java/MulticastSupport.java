import java.io.*;
import java.net.*;
import java.util.*;
/** Selezione esplicita dell'interfaccia IPv4. MULTICAST_IFACE può contenere il suo indirizzo IPv4. */
public final class MulticastSupport {
    private MulticastSupport() { }
    public static InetAddress group(String value) throws IOException {
        InetAddress group = InetAddress.getByName(value);
        if (!(group instanceof Inet4Address) || !group.isMulticastAddress() || value.equals("224.0.0.0"))
            throw new IOException("Richiesto un gruppo multicast IPv4 valido");
        return group;
    }
    public static NetworkInterface networkInterface() throws IOException {
        String selected = System.getenv("MULTICAST_IFACE");
        if (selected != null && !selected.isBlank()) {
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(selected));
            if (ni == null || !ni.isUp() || (!ni.supportsMulticast() && !ni.isLoopback())) throw new IOException("Interfaccia multicast non disponibile: " + selected);
            return ni;
        }
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) {
                for (InetAddress address : Collections.list(ni.getInetAddresses()))
                    if (address instanceof Inet4Address) return ni;
            }
        }
        throw new IOException("Nessuna interfaccia IPv4 multicast attiva; imposta MULTICAST_IFACE");
    }
    public static MulticastSocket receiver(InetAddress group, int port, NetworkInterface ni) throws IOException {
        MulticastSocket socket = new MulticastSocket(null);
        try {
            socket.setReuseAddress(true); // Prima del bind, per più ricevitori locali.
            socket.bind(new InetSocketAddress(port));
            socket.setNetworkInterface(ni);
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            socket.joinGroup(new InetSocketAddress(group, port), ni);
            return socket;
        } catch (IOException | RuntimeException e) { socket.close(); throw e; }
    }
}
