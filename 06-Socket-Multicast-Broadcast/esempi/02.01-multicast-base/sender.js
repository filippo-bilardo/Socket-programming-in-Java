const u = require('../udp-cli');
const a = process.argv.slice(2);
u.sender({multicast: true, group: u.multicastGroup(a[2]), port: u.integer(a[1], 9876, 1, 65535, 'Porta'), interval: 100, ttl: 1, limit: 1, message: a[0] ?? 'Messaggio da Sender: è UDP'});
