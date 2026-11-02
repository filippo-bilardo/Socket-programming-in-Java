/**
 * 06-Socket-Multicast-Broadcast/esempi/01.02-broadcast-js/broadcast-sender.js
 *
 * Obiettivo: Inviare datagrammi broadcast sulla rete IPv4 selezionata.
 * Protocollo: BROADCAST|numero messaggio|timestamp in UTF-8; default 255.255.255.255:5000.
 * Parametri: porta, intervallo-ms, indirizzo, conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Avvio:
 *   node broadcast-sender.js 5000 500 255.255.255.255 3
 *
 * Il broadcast della subnet dipende dal prefisso. Un invio riuscito non garantisce la ricezione da tutti gli host.
 */

// Le operazioni di rete sono nell’utility comune; questo file configura soltanto il proprio esempio.
const u = require('../udp-cli');
// Esclude eseguibile e nome dello script: a[0] è il primo parametro passato dall’utente.
const a = process.argv.slice(2);
// Configura porta, intervallo, indirizzo e conteggio: la validazione evita valori fuori range prima di aprire il socket.
u.sender({ port: u.integer(a[0], 5000, 1, 65535, 'Porta'), interval: u.integer(a[1], 2000, 1, 2147483647, 'Intervallo'), group: a[2] ?? '255.255.255.255', limit: u.integer(a[3], 0, 0, 1000000, 'Conteggio') });
