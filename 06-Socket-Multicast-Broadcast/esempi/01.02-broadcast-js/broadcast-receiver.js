const u = require('../udp-cli');
const a = process.argv.slice(2);
u.receiver({port: u.integer(a[0], 5000, 1, 65535, 'Porta'), limit: u.integer(a[1], 0, 0, 1000000, 'Conteggio')});
