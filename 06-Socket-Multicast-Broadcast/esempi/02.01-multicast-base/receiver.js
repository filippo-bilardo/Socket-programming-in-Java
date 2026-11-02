const u = require('../udp-cli');
const a = process.argv.slice(2);
u.receiver({multicast: true, group: u.multicastGroup(a[1]), port: u.integer(a[0], 9876, 1, 65535, 'Porta'), limit: u.integer(a[2], 0, 0, 1000000, 'Conteggio')});
