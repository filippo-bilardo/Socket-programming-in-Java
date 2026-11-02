const u = require('../udp-cli');
const a = process.argv.slice(2);
u.sender({multicast: true, group: u.multicastGroup(a[0]), port: u.integer(a[1], 5000, 1, 65535, 'Porta'), interval: u.integer(a[2], 2000, 1, 2147483647, 'Intervallo'), ttl: u.integer(a[3], 1, 0, 255, 'TTL'), limit: u.integer(a[4], 0, 0, 1000000, 'Conteggio')});
