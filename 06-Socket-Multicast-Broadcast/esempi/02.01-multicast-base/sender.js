/**
 * 06-Socket-Multicast-Broadcast/esempi/02.01-multicast-base/sender.js
 *
 * Obiettivo: Inviare un singolo datagramma a un gruppo multicast IPv4.
 * Protocollo: Testo UTF-8, massimo 1024 byte; default 239.255.0.1:9876, TTL 1.
 * Parametri: messaggio, porta, gruppo (facoltativi).
 *
 * Avvio:
 *   node sender.js "Caffè e multicast" 9876 239.255.0.1
 *
 * Avvia prima i ricevitori; imposta MULTICAST_IFACE se necessario. Per inviare non occorre il join.
 */

// Le operazioni di rete sono nell’utility comune; questo file configura soltanto il proprio esempio.
const u = require('../udp-cli');
// Esclude eseguibile e nome dello script: a[0] è il primo parametro passato dall’utente.
const a = process.argv.slice(2);
// Configura gruppo e porta del destinatario; TTL e conteggio controllano estensione e durata degli invii.
u.sender({multicast: true, group: u.multicastGroup(a[2]), port: u.integer(a[1], 9876, 1, 65535, 'Porta'), interval: 100, ttl: 1, limit: 1, message: a[0] ?? 'Messaggio da Sender: è UDP'});
