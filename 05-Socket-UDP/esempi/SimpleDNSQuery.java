import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Query DNS A/IN via UDP. Rifiuta risposte troncate; non implementa fallback TCP o DNSSEC. */
public class SimpleDNSQuery {
    public static byte[] buildDNSQuery(String domain, int id) throws IOException {
        String ascii;
        try { ascii = IDN.toASCII(domain.endsWith(".") ? domain.substring(0, domain.length() - 1) : domain); }
        catch (IllegalArgumentException e) { throw new IOException("Dominio non valido", e); }
        if (ascii.isEmpty() || ascii.length() > 253) throw new IOException("Dominio non valido");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(id); out.writeShort(0x0100); out.writeShort(1);
        out.writeShort(0); out.writeShort(0); out.writeShort(0);
        for (String label : ascii.split("\\.", -1)) {
            byte[] data = label.getBytes(StandardCharsets.US_ASCII);
            if (data.length < 1 || data.length > 63) throw new IOException("Etichetta DNS non valida");
            out.writeByte(data.length); out.write(data);
        }
        out.writeByte(0); out.writeShort(1); out.writeShort(1);
        return bytes.toByteArray();
    }
    private static int u16(byte[] data, int pos, int end) throws IOException {
        if (pos < 0 || pos + 2 > end) throw new IOException("Pacchetto DNS incompleto");
        return ((data[pos] & 255) << 8) | (data[pos + 1] & 255);
    }
    private static int skipName(byte[] data, int pos, int end) throws IOException {
        int after = -1;
        Set<Integer> visited = new HashSet<>();
        while (pos < end && visited.add(pos)) {
            int n = data[pos] & 255;
            if ((n & 0xc0) == 0xc0) {
                int pointer = u16(data, pos, end) & 0x3fff;
                if (after < 0) after = pos + 2;
                pos = pointer;
            } else if ((n & 0xc0) != 0 || n > 63) throw new IOException("Nome DNS non valido");
            else if (n == 0) return after < 0 ? pos + 1 : after;
            else {
                if (pos + 1 + n > end) throw new IOException("Nome DNS incompleto");
                pos += n + 1;
            }
        }
        throw new IOException("Compressione DNS ciclica o incompleta");
    }
    public static String parseDNSResponse(byte[] data, int length, int expectedId) throws IOException {
        if (length < 12 || length > data.length) throw new IOException("Header DNS incompleto");
        if (u16(data, 0, length) != (expectedId & 65535)) throw new IOException("ID DNS non corrispondente");
        int flags = u16(data, 2, length);
        if ((flags & 0x8000) == 0 || (flags & 0x7800) != 0) throw new IOException("Non è una risposta DNS standard");
        if ((flags & 0x0200) != 0) throw new IOException("Risposta troncata: necessario fallback TCP (non implementato)");
        if ((flags & 15) != 0) throw new IOException("Errore DNS RCODE=" + (flags & 15));
        int questions = u16(data, 4, length), answers = u16(data, 6, length), pos = 12;
        for (int i = 0; i < questions; i++) {
            pos = skipName(data, pos, length);
            u16(data, pos + 2, length); pos += 4;
        }
        for (int i = 0; i < answers; i++) {
            pos = skipName(data, pos, length);
            int type = u16(data, pos, length), clazz = u16(data, pos + 2, length);
            int size = u16(data, pos + 8, length); pos += 10;
            if (pos + size > length) throw new IOException("RDATA DNS incompleto");
            if (type == 1 && clazz == 1 && size == 4) {
                byte[] ip = Arrays.copyOfRange(data, pos, pos + 4);
                return InetAddress.getByAddress(ip).getHostAddress();
            }
            pos += size; // Per esempio CNAME prima del record A.
        }
        throw new IOException("Nessun record A nella risposta");
    }
    public static String resolve(String domain, String server, int port) throws IOException {
        int id = ThreadLocalRandom.current().nextInt(65536);
        byte[] query = buildDNSQuery(domain, id);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(server), port); socket.setSoTimeout(2000);
            socket.send(new DatagramPacket(query, query.length));
            byte[] buffer = new byte[4096];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length); socket.receive(reply);
            return parseDNSResponse(reply.getData(), reply.getLength(), id);
        }
    }
    public static void main(String[] args) throws Exception {
        String domain = args.length > 0 ? args[0] : "example.com";
        String server = args.length > 1 ? args[1] : "8.8.8.8";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 53;
        System.out.println(domain + " -> " + resolve(domain, server, port));
    }
}
