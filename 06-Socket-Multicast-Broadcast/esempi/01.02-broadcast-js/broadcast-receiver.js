/**
 * 06-Socket-Multicast-Broadcast/esempi/01.02-broadcast-js/broadcast-receiver.js
 *
 * Obiettivo: Ricevere messaggi UDP, inclusi quelli broadcast diretti alla porta di laboratorio.
 * Protocollo: Payload UTF-8 di massimo 1024 byte; porta predefinita 5000.
 * Parametri: porta e conteggio (facoltativi; conteggio 0 = continuo).
 *
 * Avvio:
 *   node broadcast-receiver.js 5000 3
 *
 * Avvia un ricevitore per host/porta; per più ricevitori locali usa gli esempi multicast.
 */

// Le operazioni di rete sono nell’utility comune; questo file configura soltanto il proprio esempio.
const u = require('../udp-cli');
// Esclude eseguibile e nome dello script: a[0] è il primo parametro passato dall’utente.
const a = process.argv.slice(2);
// Seleziona la porta locale e il conteggio: zero mantiene il ricevitore in ascolto fino alla chiusura.
u.receiver({port: u.integer(a[0], 5000, 1, 65535, 'Porta'), limit: u.integer(a[1], 0, 0, 1000000, 'Conteggio')});
