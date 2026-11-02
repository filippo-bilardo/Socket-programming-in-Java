const u = require('../udp-cli');
const a = process.argv.slice(2);
u.receiver({multicast: true, group: u.multicastGroup(a[0]), port: u.integer(a[1], 5000, 1, 65535, 'Porta'), limit: u.integer(a[2], 0, 0, 1000000, 'Conteggio')});
