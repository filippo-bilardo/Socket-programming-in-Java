/**
 * 06-Socket-Multicast-Broadcast/esempi/02.03-multicast-js/multicast-receiver.js
 *
 * Obiettivo: Ricevere datagrammi aderendo al gruppo sulla corretta interfaccia locale.
 * Protocollo: Testo UTF-8, massimo 1024 byte; default 239.255.0.1:5000.
 * Parametri: gruppo, porta, conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Avvio:
 *   node multicast-receiver.js 239.255.0.1 5000 3
 *
 * bye termina questo ricevitore. Un conteggio finito attiva il timeout; UDP può perdere messaggi.
 */

// Le operazioni di rete sono nell’utility comune; questo file configura soltanto il proprio esempio.
const u = require('../udp-cli');
// Esclude eseguibile e nome dello script: a[0] è il primo parametro passato dall’utente.
const a = process.argv.slice(2);
// Passa gruppo, porta e conteggio all’utility, che esegue bind e join nell’ordine corretto.
u.receiver({multicast: true, group: u.multicastGroup(a[0]), port: u.integer(a[1], 5000, 1, 65535, 'Porta'), limit: u.integer(a[2], 0, 0, 1000000, 'Conteggio')});
