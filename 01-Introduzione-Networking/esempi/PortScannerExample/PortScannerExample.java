/**
 * Esempio di utilizzo avanzato del PortScannerMultithread
 * 
 * Questo file mostra come integrare il port scanner in altre applicazioni
 * e come personalizzare il comportamento per esigenze specifiche.
 */

public class PortScannerExample {
    
    public static void main(String[] args) {
        System.out.println("🚀 ESEMPI UTILIZZO PORT SCANNER MULTITHREAD");
        System.out.println("=" .repeat(50));
        
        // Esempio 1: Scansione porte comuni web
        System.out.println("\n📝 Esempio 1: Porte comuni web");
        System.out.println("Comando: java PortScannerMultithread google.com 80 443 10");
        System.out.println("Scopo: Verificare se i servizi web sono attivi");
        
        // Esempio 2: Scansione database comuni  
        System.out.println("\n📝 Esempio 2: Porte database comuni");
        System.out.println("Comando: java PortScannerMultithread 192.168.1.100 3306 3306 1");
        System.out.println("Scopo: Verificare se MySQL è in ascolto");
        System.out.println("Altre porte DB: 5432 (PostgreSQL), 27017 (MongoDB), 6379 (Redis)");
        
        // Esempio 3: Scansione servizi SSH/Telnet
        System.out.println("\n📝 Esempio 3: Servizi di amministrazione");
        System.out.println("Comando: java PortScannerMultithread router.local 22 23 5");
        System.out.println("Scopo: Verificare servizi SSH/Telnet su router");
        
        // Esempio 4: Scansione range applicazioni web
        System.out.println("\n📝 Esempio 4: Range applicazioni web");
        System.out.println("Comando: java PortScannerMultithread localhost 8000 8100 50");
        System.out.println("Scopo: Trovare applicazioni di sviluppo in ascolto");
        
        // Esempio 5: Scansione veloce rete locale
        System.out.println("\n📝 Esempio 5: Discovery servizi LAN");
        System.out.println("Comando: java PortScannerMultithread 192.168.1.1 1 1000 100");
        System.out.println("Scopo: Scoprire servizi su gateway/router");
        
        // Esempio 6: Test sicurezza (porte privilegiate)
        System.out.println("\n📝 Esempio 6: Audit porte privilegiate");
        System.out.println("Comando: java PortScannerMultithread server.company.com 1 1024 200");
        System.out.println("Scopo: Audit porte privilegiate per sicurezza");
        
        System.out.println("\n" + "=" .repeat(50));
        System.out.println("💡 SUGGERIMENTI:");
        System.out.println("• Usa meno thread per host remoti (50-100)");
        System.out.println("• Usa più thread per localhost (100-300)");
        System.out.println("• Range piccoli per test rapidi");
        System.out.println("• Range completi per audit di sicurezza");
        System.out.println("• Rispetta le politiche di rete aziendale");
    }
    
    /**
     * Metodo helper per comuni combinazioni host/porta
     */
    public static void scanCommonServices(String host) {
        System.out.println("🔍 Scansione servizi comuni su: " + host);
        
        // Porte web comuni
        runScan(host, 80, 80, "HTTP");
        runScan(host, 443, 443, "HTTPS");
        runScan(host, 8080, 8080, "HTTP-Alt");
        
        // Servizi amministrazione
        runScan(host, 22, 22, "SSH");
        runScan(host, 23, 23, "Telnet");
        
        // Database comuni
        runScan(host, 3306, 3306, "MySQL");
        runScan(host, 5432, 5432, "PostgreSQL");
        
        // Email
        runScan(host, 25, 25, "SMTP");
        runScan(host, 110, 110, "POP3");
        runScan(host, 143, 143, "IMAP");
    }
    
    private static void runScan(String host, int port, int endPort, String service) {
        System.out.printf("Testando %s (%d): ", service, port);
        // Qui potresti integrare direttamente la logica del scanner
        // o chiamare il programma principale
        System.out.println("java PortScannerMultithread " + host + " " + port + " " + endPort + " 1");
    }
}