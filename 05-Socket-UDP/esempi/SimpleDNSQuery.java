/**
 * 05-Socket-UDP/esempi/SimpleDNSQuery.java
 *
 * Obiettivo: Costruire e leggere una query DNS binaria A/IN tramite UDP.
 * Protocollo: DNS standard: header, domanda, record e nomi eventualmente compressi.
 * Parametri: dominio, indirizzo DNS, porta (default: example.com, 8.8.8.8, 53).
 *
 * Compilazione dalla cartella del file: javac -encoding UTF-8 *.java
 * Avvio:
 *   java SimpleDNSQuery example.com 8.8.8.8 53
 *
 * Richiede un DNS raggiungibile. È un codec didattico: segnala TC, senza fallback TCP o validazione DNSSEC.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Query DNS A/IN via UDP. Rifiuta risposte troncate; non implementa fallback TCP o DNSSEC. */
public class SimpleDNSQuery {
    // Costruisce una domanda A/IN; gli interi di DataOutputStream sono scritti in ordine di rete, big endian.
    public static byte[] buildDNSQuery(String domain, int id) throws IOException {
        String ascii;
        // Converte eventuali caratteri internazionali in etichette ASCII compatibili con il formato DNS.
        try { ascii = IDN.toASCII(domain.endsWith(".") ? domain.substring(0, domain.length() - 1) : domain); }
        catch (IllegalArgumentException e) { throw new IOException("Dominio non valido", e); }
        if (ascii.isEmpty() || ascii.length() > 253) throw new IOException("Dominio non valido");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        // Header: ID della transazione, RD attivo e una domanda; i conteggi delle altre sezioni partono da zero.
        out.writeShort(id); out.writeShort(0x0100); out.writeShort(1);
        out.writeShort(0); out.writeShort(0); out.writeShort(0);
        // QNAME è una sequenza di etichette precedute dalla lunghezza, non una stringa con punti sul filo.
        for (String label : ascii.split("\\.", -1)) {
            byte[] data = label.getBytes(StandardCharsets.US_ASCII);
            if (data.length < 1 || data.length > 63) throw new IOException("Etichetta DNS non valida");
            out.writeByte(data.length); out.write(data);
        }
        // Chiude QNAME e imposta QTYPE=1 (IPv4 A), QCLASS=1 (Internet).
        out.writeByte(0); out.writeShort(1); out.writeShort(1);
        return bytes.toByteArray();
    }
    // Legge due byte non firmati, verificando prima che appartengano alla parte ricevuta del pacchetto.
    private static int u16(byte[] data, int pos, int end) throws IOException {
        if (pos < 0 || pos + 2 > end) throw new IOException("Pacchetto DNS incompleto");
        return ((data[pos] & 255) << 8) | (data[pos + 1] & 255);
    }
    // Trova la fine di un nome anche con puntatori di compressione; conserva dove continuare nella sezione originale.
    private static int skipName(byte[] data, int pos, int end) throws IOException {
        int after = -1;
        // Rileva riferimenti ciclici: un pacchetto malformato non deve causare un ciclo infinito nel parser.
        Set<Integer> visited = new HashSet<>();
        while (pos < end && visited.add(pos)) {
            int n = data[pos] & 255;
            // I due bit alti a 1 indicano un puntatore: i rimanenti 14 bit sono l’offset nel messaggio DNS.
            if ((n & 0xc0) == 0xc0) {
                int pointer = u16(data, pos, end) & 0x3fff;
                // Dopo aver seguito il puntatore, la lettura del record riprenderà dopo i due byte del puntatore stesso.
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
    // Analizza soltanto length byte ricevuti e correla l’ID alla richiesta, prima di cercare un record A.
    public static String parseDNSResponse(byte[] data, int length, int expectedId) throws IOException {
        if (length < 12 || length > data.length) throw new IOException("Header DNS incompleto");
        if (u16(data, 0, length) != (expectedId & 65535)) throw new IOException("ID DNS non corrispondente");
        // Verifica QR e OPCODE; TC richiede fallback TCP, mentre i quattro bit RCODE descrivono l’esito DNS.
        int flags = u16(data, 2, length);
        if ((flags & 0x8000) == 0 || (flags & 0x7800) != 0) throw new IOException("Non è una risposta DNS standard");
        if ((flags & 0x0200) != 0) throw new IOException("Risposta troncata: necessario fallback TCP (non implementato)");
        if ((flags & 15) != 0) throw new IOException("Errore DNS RCODE=" + (flags & 15));
        int questions = u16(data, 4, length), answers = u16(data, 6, length), pos = 12;
        // Supera le domande: dopo ciascun nome seguono QTYPE e QCLASS, due byte ciascuno.
        for (int i = 0; i < questions; i++) {
            pos = skipName(data, pos, length);
            u16(data, pos + 2, length); pos += 4;
        }
        // Ogni risposta contiene nome, TYPE/CLASS/TTL/RDLENGTH e poi i byte RDATA indicati dalla lunghezza.
        for (int i = 0; i < answers; i++) {
            pos = skipName(data, pos, length);
            int type = u16(data, pos, length), clazz = u16(data, pos + 2, length);
            int size = u16(data, pos + 8, length); pos += 10;
            if (pos + size > length) throw new IOException("RDATA DNS incompleto");
            // Un record A/IN contiene quattro byte IPv4; altri record, come CNAME, vengono superati.
            if (type == 1 && clazz == 1 && size == 4) {
                byte[] ip = Arrays.copyOfRange(data, pos, pos + 4);
                return InetAddress.getByAddress(ip).getHostAddress();
            }
            pos += size; // Per esempio CNAME prima del record A.
        }
        throw new IOException("Nessun record A nella risposta");
    }
    public static String resolve(String domain, String server, int port) throws IOException {
        // Un ID casuale permette di correlare questa risposta alla domanda, senza usare sempre lo stesso numero.
        int id = ThreadLocalRandom.current().nextInt(65536);
        byte[] query = buildDNSQuery(domain, id);
        // Una porta locale effimera è sufficiente per inviare; try-with-resources chiude il socket anche su errore.
        try (DatagramSocket socket = new DatagramSocket()) {
            // La query non è vuota: connect seleziona il peer UDP e filtra le risposte degli altri endpoint.
            socket.connect(InetAddress.getByName(server), port); socket.setSoTimeout(2000);
            socket.send(new DatagramPacket(query, query.length));
            byte[] buffer = new byte[4096];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length); socket.receive(reply);
            // Passa la lunghezza reale della risposta, non l’intera capacità del buffer.
            return parseDNSResponse(reply.getData(), reply.getLength(), id);
        }
    }
    // Legge i parametri della console e avvia il ruolo previsto da questa classe.
    public static void main(String[] args) throws Exception {
        String domain = args.length > 0 ? args[0] : "example.com";
        String server = args.length > 1 ? args[1] : "8.8.8.8";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 53;
        System.out.println(domain + " -> " + resolve(domain, server, port));
    }
}
