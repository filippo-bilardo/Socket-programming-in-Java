const u = require('../udp-cli');
const a = process.argv.slice(2);
u.sender({port: u.integer(a[0], 5000, 1, 65535, 'Porta'), interval: u.integer(a[1], 2000, 1, 2147483647, 'Intervallo'), group: a[2] ?? '255.255.255.255', limit: u.integer(a[3], 0, 0, 1000000, 'Conteggio')});
