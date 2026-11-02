/**
 * 06-Socket-Multicast-Broadcast/esempi/02.03-multicast-js/multicast-sender.js
 *
 * Obiettivo: Inviare periodicamente messaggi multicast con parametri configurabili.
 * Protocollo: MULTICAST|numero messaggio|timestamp|TTL in UTF-8; default 239.255.0.1:5000.
 * Parametri: gruppo, porta, intervallo-ms, TTL, conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Avvio:
 *   node multicast-sender.js 239.255.0.1 5000 100 1 3
 *
 * TTL limita gli hop, non una regione geografica. Sender e receiver devono usare lo stesso gruppo e la stessa porta.
 */

// Le operazioni di rete sono nell’utility comune; questo file configura soltanto il proprio esempio.
const u = require('../udp-cli');
// Esclude eseguibile e nome dello script: a[0] è il primo parametro passato dall’utente.
const a = process.argv.slice(2);
// Configura gruppo e porta del destinatario; TTL e conteggio controllano estensione e durata degli invii.
u.sender({multicast: true, group: u.multicastGroup(a[0]), port: u.integer(a[1], 5000, 1, 65535, 'Porta'), interval: u.integer(a[2], 2000, 1, 2147483647, 'Intervallo'), ttl: u.integer(a[3], 1, 0, 255, 'TTL'), limit: u.integer(a[4], 0, 0, 1000000, 'Conteggio')});
