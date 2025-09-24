# Modulo 05: SSL/TLS e Sicurezza

## 📝 Descrizione
Questo modulo si concentra sulla **sicurezza delle comunicazioni di rete** in Java, coprendo SSL/TLS, certificati, autenticazione e best practices per applicazioni sicure. La sicurezza è fondamentale per proteggere dati sensibili durante la trasmissione.

## 🎯 Obiettivi di Apprendimento
Al termine di questo modulo sarete in grado di:
- ✅ Implementare connessioni SSL/TLS sicure
- ✅ Gestire certificati e trust store
- ✅ Configurare autenticazione client-server
- ✅ Applicare pattern di sicurezza per socket
- ✅ Debuggare problemi SSL/TLS comuni

## 📚 Contenuti Teorici

### [1. Fondamenti SSL/TLS](01-Fondamenti-SSL-TLS.md)
- Protocolli di sicurezza trasporto
- Handshake SSL/TLS
- Certificati digitali e PKI
- Cipher suites e algoritmi
- Java Secure Socket Extension (JSSE)

### [2. Implementazione SSL Socket](02-Implementazione-SSL-Socket.md)
- SSLSocket e SSLServerSocket
- Configurazione SSL context
- Trust store e key store
- Validazione certificati
- Client authentication

### [3. Sicurezza Avanzata](03-Sicurezza-Avanzata.md)
- Perfect Forward Secrecy
- Certificate pinning
- OCSP e revocation
- Security hardening
- Monitoring e logging sicurezza

## 🎯 Esempi Pratici

### 📁 Esempi SSL Base
- **SimpleSSLServer.java** - Server SSL/TLS di base
- **SimpleSSLClient.java** - Client SSL con validazione certificati
- **CertificateManager.java** - Gestione certificati e keystore

### 📁 Esempi SSL Avanzati
- **SecureChatServer.java** - Chat server con autenticazione client
- **HTTPSProxy.java** - Proxy HTTPS con intercettazione sicura
- **SSLPerformanceTest.java** - Benchmark performance SSL vs plain socket

> **💡 Nota:** Gli esempi richiedono certificati SSL - verranno generati automaticamente

## 🔧 Esercitazioni Pratiche

### Esercizio 1: Secure File Transfer
Implementa un sistema di trasferimento file con crittografia end-to-end.

**Requisiti:**
- SSL/TLS per transport security
- Autenticazione client certificate
- Verifica integrità file
- Logging eventi sicurezza

### Esercizio 2: Secure API Gateway
Crea un gateway API con terminazione SSL e autenticazione.

**Requisiti:**
- Multiple SSL certificates (SNI)
- JWT token validation
- Rate limiting per security
- Audit logging completo

### Esercizio 3: Certificate Authority
Sviluppa una CA semplice per gestire certificati interni.

**Requisiti:**
- Generazione certificati
- Revocation list (CRL)
- OCSP responder
- Web interface gestione

## 🧪 Test e Validazione

### Test SSL/TLS
```bash
# Test connessione SSL
java SimpleSSLServer 8443
java SimpleSSLClient localhost 8443

# Test performance SSL vs plain
java SSLPerformanceTest benchmark localhost 8443 8080

# Verifica certificati
keytool -list -keystore server.jks
openssl x509 -text -in server.crt
```

### Security Checklist
- **Protocolli**: TLS 1.2+ (no SSL 3.0/TLS 1.0)
- **Cipher Suites**: Strong encryption (AES, ChaCha20)
- **Certificati**: Validità, catena, revocation
- **Perfect Forward Secrecy**: ECDHE key exchange
- **Certificate Pinning**: Per applicazioni critiche

## 🔗 Collegamenti
- **Modulo Precedente:** [04 - Socket UDP](../04-Socket-UDP/README.md)
- **Modulo Successivo:** [06 - Multithreading Network](../06-Multithreading-Network/README.md)
- **Corso Principale:** [Socket Programming in Java](../README.md)

## 📖 Risorse Aggiuntive
- [RFC 8446 - Transport Layer Security (TLS) 1.3](https://tools.ietf.org/html/rfc8446)
- [RFC 5280 - Internet X.509 PKI Certificate](https://tools.ietf.org/html/rfc5280)
- [Oracle JSSE Reference Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html)
- [OWASP Transport Layer Protection](https://owasp.org/www-project-cheat-sheets/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html)

---
**🔒 Prossimo Step:** Passa al [Modulo 06 - Multithreading Network](../06-Multithreading-Network/README.md) per le tecniche di concorrenza avanzate.