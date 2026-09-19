# Applicazioni Monolitiche e Microservizi 

## Introduzione

Nel corso dell'evoluzione dello sviluppo software, i modelli architetturali si sono trasformati:

1. **Applicazione Monolitica** - Una singola unità deployabile
2. **Architettura a Microservizi** - Molteplici servizi indipendenti
3. **Ibridi e Serverless** - Combinazioni moderne

Questa guida esplora i due approcci principali e quando usarli.

---

## Capitolo 1: Applicazioni Monolitiche

### 1.1 Cos'è un'Applicazione Monolitica?

Un'applicazione monolitica è un'**unica unità software** con tutte le funzionalità raggruppate:

```
┌─────────────────────────────────────────┐
│      Applicazione Monolitica            │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────┐  ┌───────────────┐   │
│  │ Autenticazione│  │ Gestione User │   │
│  └───────────────┘  └───────────────┘   │
│                                         │
│  ┌───────────────┐  ┌───────────────┐   │
│  │ Ordini        │  │ Pagamenti     │   │
│  └───────────────┘  └───────────────┘   │
│                                         │
│  ┌───────────────┐  ┌───────────────┐   │
│  │ Spedizioni    │  │ Inventario    │   │
│  └───────────────┘  └───────────────┘   │
│                                         │
└─────────────────────────────────────────┘
        ▼ Singolo Deploy
     server.jar
```

### 1.2 Caratteristiche Monolitiche

**Vantaggi:**
- ✅ Semplice da sviluppare inizialmente
- ✅ Facile da testare (test end-to-end)
- ✅ Performance elevata (no network overhead)
- ✅ Deployment semplice
- ✅ Debugging facile (singolo processo)
- ✅ Transazioni ACID garantite
- ✅ Condivisione facile di dati

**Svantaggi:**
- ❌ Diventa complesso con la crescita
- ❌ Scalabilità limitata (scale all or nothing)
- ❌ Un bug può abbattere tutto il sistema
- ❌ Difficile usare tecnologie diverse
- ❌ Team grandi condividono lo stesso codebase
- ❌ Deploy rischioso (tutto o niente)
- ❌ Difficile innovare rapidamente

### 1.3 Struttura Monolitica Tipica

```
ecommerce-app/
├── src/
│   ├── com.ecommerce.auth/
│   │   ├── AuthService.java
│   │   ├── User.java
│   │   └── AuthController.java
│   ├── com.ecommerce.orders/
│   │   ├── OrderService.java
│   │   ├── Order.java
│   │   └── OrderController.java
│   ├── com.ecommerce.payments/
│   │   ├── PaymentService.java
│   │   ├── PaymentProcessor.java
│   │   └── PaymentController.java
│   ├── com.ecommerce.shipping/
│   │   ├── ShippingService.java
│   │   ├── Shipment.java
│   │   └── ShippingController.java
│   └── com.ecommerce.inventory/
│       ├── InventoryService.java
│       ├── Product.java
│       └── InventoryController.java
├── resources/
│   └── application.properties
├── pom.xml
└── Dockerfile
```

### 1.4 Esempio di Applicazione Monolitica

#### ECommerceMonolithic.java

```java
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ECommerceMonolithic {
    private HttpServer httpServer;
    private ExecutorService threadPool;

    public ECommerceMonolithic(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        threadPool = Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPool);
    }

    public void start() {
        // Endpoint Autenticazione
        httpServer.createContext("/api/auth/login", exchange -> {
            handleAuth(exchange);
        });

        // Endpoint Ordini
        httpServer.createContext("/api/orders/create", exchange -> {
            handleCreateOrder(exchange);
        });

        // Endpoint Pagamenti
        httpServer.createContext("/api/payments/process", exchange -> {
            handlePayment(exchange);
        });

        // Endpoint Inventario
        httpServer.createContext("/api/inventory/check", exchange -> {
            handleInventory(exchange);
        });

        httpServer.start();
        System.out.println("🟡 Monolith avviato su porta 8080");
    }

    private void handleAuth(HttpExchange exchange) throws IOException {
        String response = "✅ Autenticazione completata";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private void handleCreateOrder(HttpExchange exchange) throws IOException {
        // 1. Autentica l'utente
        boolean authenticated = authenticateUser();
        if (!authenticated) {
            sendError(exchange, "❌ Non autenticato");
            return;
        }

        // 2. Verifica inventario
        boolean inStock = checkInventory("PROD123");
        if (!inStock) {
            sendError(exchange, "❌ Prodotto non disponibile");
            return;
        }

        // 3. Processa il pagamento
        boolean paymentOk = processPayment(49.99);
        if (!paymentOk) {
            sendError(exchange, "❌ Pagamento fallito");
            return;
        }

        // 4. Crea l'ordine
        createOrder("USER1", "PROD123", 1);

        // 5. Crea la spedizione
        createShipment("ORD123");

        String response = "✅ Ordine creato con successo";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private void handlePayment(HttpExchange exchange) throws IOException {
        String response = processPayment(99.99) ? "✅ Pagamento OK" : "❌ Pagamento fallito";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private void handleInventory(HttpExchange exchange) throws IOException {
        String response = checkInventory("PROD123") ? "✅ In stock" : "❌ Non disponibile";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    // ===== Metodi di Business Logic =====
    private boolean authenticateUser() {
        System.out.println("🔐 Autenticazione user...");
        return true;
    }

    private boolean checkInventory(String productId) {
        System.out.println("📦 Verifica inventario " + productId);
        return true;
    }

    private boolean processPayment(double amount) {
        System.out.println("💳 Processo pagamento €" + amount);
        return true;
    }

    private void createOrder(String userId, String productId, int qty) {
        System.out.println("📝 Crea ordine per " + userId);
    }

    private void createShipment(String orderId) {
        System.out.println("🚚 Crea spedizione " + orderId);
    }

    private void sendError(HttpExchange exchange, String message) throws IOException {
        exchange.sendResponseHeaders(400, message.length());
        exchange.getResponseBody().write(message.getBytes());
        exchange.close();
    }

    public static void main(String[] args) throws IOException {
        new ECommerceMonolithic(8080).start();
    }
}
```

**Problema:** Se il servizio pagamenti è lento, **tutto il sistema** rallenta!

---

## Capitolo 2: Architettura a Microservizi

### 2.1 Cos'è un Microservizio?

Un microservizio è un **servizio indipendente** che:
- Gestisce **una responsabilità singola**
- Ha il **proprio database**
- Comunica tramite **API REST/gRPC**
- Può essere **deployato indipendentemente**
- Scala in modo **indipendente**

```
┌──────────────────┐  ┌──────────────────┐
│  Auth Service    │  │  Order Service   │
│ :3001            │  │ :3002            │
│ ┌────────────┐   │  │ ┌────────────┐   │
│ │  Auth DB   │   │  │ │ Order DB   │   │
│ └────────────┘   │  │ │            │   │
└──────────────────┘  └──────────────────┘
         △                    △
         │ REST API            │ REST API
         └────────┬────────────┘
                  │
            ┌─────▼────────┐
            │  API Gateway │
            │  :8080       │
            └──────────────┘
```

### 2.2 Caratteristiche

**Vantaggi:**
- ✅ Scalabilità selettiva
- ✅ Indipendenza deployment
- ✅ Fault isolation (un errore non abbatte tutto)
- ✅ Libertà tecnologica (ogni servizio può usare linguaggio diverso)
- ✅ Team autonomi
- ✅ Aggiornamenti rapidi
- ✅ Evoluzione indipendente

**Svantaggi:**
- ❌ Complessità operazionale
- ❌ Network overhead
- ❌ Debugging difficile (distribuito)
- ❌ Transazioni distribuite complesse
- ❌ Inconsistenza temporanea di dati
- ❌ DevOps skills obbligatori
- ❌ Versioning API complicato

### 2.3 Struttura a Microservizi

```
ecommerce-microservices/
├── api-gateway/
│   ├── src/main/java/gateway/
│   │   └── ApiGateway.java
│   └── pom.xml
├── auth-service/
│   ├── src/main/java/auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   └── AuthRepository.java
│   ├── pom.xml
│   └── Dockerfile
├── order-service/
│   ├── src/main/java/order/
│   │   ├── OrderController.java
│   │   ├── OrderService.java
│   │   └── OrderRepository.java
│   ├── pom.xml
│   └── Dockerfile
├── payment-service/
│   ├── src/main/java/payment/
│   │   ├── PaymentController.java
│   │   ├── PaymentService.java
│   │   └── PaymentRepository.java
│   ├── pom.xml
│   └── Dockerfile
├── inventory-service/
│   ├── src/main/java/inventory/
│   │   ├── InventoryController.java
│   │   ├── InventoryService.java
│   │   └── InventoryRepository.java
│   ├── pom.xml
│   └── Dockerfile
└── docker-compose.yml
```

### 2.4 Implementazione Microservizi

#### ApiGateway.java

```java
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.URI;
import java.net.*;

public class ApiGateway {
    private HttpServer server;

    public ApiGateway(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void start() {
        // Route a servizi diversi
        server.createContext("/api/auth/", exchange -> {
            forwardRequest(exchange, "http://localhost:3001");
        });

        server.createContext("/api/orders/", exchange -> {
            forwardRequest(exchange, "http://localhost:3002");
        });

        server.createContext("/api/payments/", exchange -> {
            forwardRequest(exchange, "http://localhost:3003");
        });

        server.createContext("/api/inventory/", exchange -> {
            forwardRequest(exchange, "http://localhost:3004");
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("🚪 API Gateway avviato su porta 8080");
    }

    private void forwardRequest(HttpExchange exchange, String serviceUrl) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        
        String fullUrl = serviceUrl + path + (query != null ? "?" + query : "");
        System.out.println("📤 Forward: " + exchange.getRequestMethod() + " " + fullUrl);

        // Invia il request al servizio
        // (In produzione usare HttpClient)
        String response = "✅ Richiesta inoltrata a " + serviceUrl;
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    public static void main(String[] args) throws IOException {
        new ApiGateway(8080).start();
    }
}
```

#### AuthService.java (Microservizio)

```java
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class AuthService {
    private HttpServer server;
    private Map<String, String> users = new HashMap<>();

    public AuthService(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Database in memoria
        users.put("alice", "pass123");
        users.put("bob", "pass456");
    }

    public void start() {
        server.createContext("/api/auth/login", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("🔐 Auth Service: Login request");
            
            String response = "✅ Token: ABC123DEF456";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext("/api/auth/validate", exchange -> {
            System.out.println("✔️ Auth Service: Validate token");
            String response = "✅ Token valido";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(5));
        server.start();
        System.out.println("🔐 Auth Service avviato su porta 3001");
    }

    public static void main(String[] args) throws IOException {
        new AuthService(3001).start();
    }
}
```

#### OrderService.java (Microservizio)

```java
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class OrderService {
    private HttpServer server;

    public OrderService(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void start() {
        server.createContext("/api/orders/create", exchange -> {
            System.out.println("📝 Order Service: Create order");
            
            String response = "✅ Ordine ORD123 creato";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext("/api/orders/list", exchange -> {
            System.out.println("📋 Order Service: List orders");
            String response = "✅ Ordini: [ORD001, ORD002, ORD003]";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(5));
        server.start();
        System.out.println("📝 Order Service avviato su porta 3002");
    }

    public static void main(String[] args) throws IOException {
        new OrderService(3002).start();
    }
}
```

#### PaymentService.java (Microservizio)

```java
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class PaymentService {
    private HttpServer server;

    public PaymentService(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void start() {
        server.createContext("/api/payments/process", exchange -> {
            System.out.println("💳 Payment Service: Process payment");
            
            // Simula elaborazione lenta (può scalare indipendentemente)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String response = "✅ Pagamento PAY123 processato";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(5));
        server.start();
        System.out.println("💳 Payment Service avviato su porta 3003");
    }

    public static void main(String[] args) throws IOException {
        new PaymentService(3003).start();
    }
}
```

**Vantaggio:** Se il servizio pagamenti è lento, posso scalare solo quello!

---

## Capitolo 3: Comunicazione tra Microservizi

### 3.1 Sincronizzazione REST (Request-Response)

```java
// Order Service chiama Payment Service
public class OrderServiceSyncCommunication {
    
    public boolean createOrderWithPayment(String userId, String productId, double amount) {
        try {
            // 1. Chiama Auth Service
            if (!validateUser(userId)) {
                return false;
            }

            // 2. Chiama Payment Service in modo SINCRONO
            if (!processPaymentSync(amount)) {
                return false;
            }

            // 3. Crea l'ordine
            createOrder(userId, productId);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Errore: " + e.getMessage());
            return false;
        }
    }

    private boolean validateUser(String userId) {
        // HTTP GET http://localhost:3001/api/auth/validate
        System.out.println("🔐 Validazione utente " + userId);
        return true;
    }

    private boolean processPaymentSync(double amount) {
        // HTTP POST http://localhost:3003/api/payments/process
        System.out.println("💳 Pagamento sincrono €" + amount);
        return true;
    }

    private void createOrder(String userId, String productId) {
        System.out.println("📝 Ordine creato");
    }
}
```

**Problema:** Se Payment Service cade, l'ordine fallisce!

### 3.2 Asincronia con Message Queue

```java
// Order Service pubblica evento
public class OrderServiceAsyncCommunication {
    
    public void createOrderAsync(String userId, String productId, double amount) {
        try {
            // 1. Crea l'ordine localmente
            String orderId = createOrder(userId, productId);
            
            // 2. Pubblica evento nella coda (NON aspetta risposta)
            publishToMessageQueue("PAYMENT_REQUESTED", 
                "orderId=" + orderId + "&amount=" + amount);
            
            System.out.println("✅ Ordine creato, pagamento in elaborazione...");
            
        } catch (Exception e) {
            System.err.println("❌ Errore: " + e.getMessage());
        }
    }

    private String createOrder(String userId, String productId) {
        System.out.println("📝 Ordine creato localmente");
        return "ORD123";
    }

    private void publishToMessageQueue(String event, String payload) {
        System.out.println("📤 Evento pubblicato: " + event + " → " + payload);
        // In produzione: RabbitMQ, Kafka, AWS SQS
    }
}

// Payment Service ascolta dalla coda
public class PaymentServiceAsyncListener {
    
    public void listenToPaymentRequests() {
        System.out.println("👂 Ascolta eventi di pagamento...");
        
        // Simula ricezione evento
        String event = "PAYMENT_REQUESTED";
        String payload = "orderId=ORD123&amount=49.99";
        
        handlePaymentRequest(payload);
    }

    private void handlePaymentRequest(String payload) {
        System.out.println("💳 Elabora richiesta di pagamento: " + payload);
        System.out.println("✅ Pagamento processato, evento PAYMENT_COMPLETED pubblicato");
    }
}
```

**Vantaggio:** Se Payment Service cade, l'ordine rimane in coda e verrà processato dopo!

---

## Capitolo 4: Pattern Importanti

### 4.1 Service Discovery

Problema: Come scoprire la porta/host di un servizio?

```java
// Registry centralizzato
public class ServiceRegistry {
    private Map<String, ServiceInfo> services = new HashMap<>();

    public void register(String serviceName, String host, int port) {
        services.put(serviceName, new ServiceInfo(host, port));
        System.out.println("📝 Registrato: " + serviceName + " @ " + host + ":" + port);
    }

    public ServiceInfo lookup(String serviceName) {
        return services.get(serviceName);
    }
}

class ServiceInfo {
    public String host;
    public int port;

    public ServiceInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
```

### 4.2 Circuit Breaker

Previene cascading failures:

```java
public class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private State state = State.CLOSED;
    private int failureCount = 0;
    private int threshold = 3;

    public String call(String endpoint) {
        if (state == State.OPEN) {
            System.out.println("⚠️  Circuit OPEN - fallback");
            return "❌ Servizio non disponibile";
        }

        try {
            String response = callService(endpoint);
            
            if (state == State.HALF_OPEN) {
                state = State.CLOSED;
                failureCount = 0;
                System.out.println("✅ Circuit CLOSED");
            }
            
            return response;
        } catch (Exception e) {
            failureCount++;
            
            if (failureCount >= threshold) {
                state = State.OPEN;
                System.out.println("🔴 Circuit OPEN - troppi errori");
            }
            
            return "❌ Errore";
        }
    }

    private String callService(String endpoint) {
        // Simula chiamata al servizio
        return "✅ Successo";
    }
}
```

### 4.3 Retry Logic

```java
public class RetryPolicy {
    
    public static String callWithRetry(String endpoint, int maxRetries) {
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                System.out.println("📤 Tentativo " + (attempt + 1) + " ...");
                return callService(endpoint);
            } catch (Exception e) {
                attempt++;
                
                if (attempt < maxRetries) {
                    System.out.println("⏳ Retry dopo 2 secondi...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("❌ Max retries raggiunto");
                    return null;
                }
            }
        }
        
        return null;
    }

    private static String callService(String endpoint) {
        return "✅ Successo";
    }
}
```

---

## Capitolo 5: Comparazione Dettagliata

### 5.1 Tabella Comparativa

| Aspetto | Monolitico | Microservizi |
|---------|-----------|-------------|
| **Complessità iniziale** | Bassa | Alta |
| **Velocity iniziale** | Veloce | Lenta |
| **Scalabilità** | Limitata | Eccellente |
| **Indipendenza deployment** | No | Sì |
| **Fault isolation** | No | Sì |
| **Testabilità** | Facile | Difficile |
| **Debugging** | Facile | Difficile |
| **Data consistency** | ACID garantito | Eventual consistency |
| **DevOps overhead** | Basso | Alto |
| **Costi infrastruttura** | Bassi | Alti |
| **Team agility** | Bassa | Alta |
| **Versioning** | Centralizzato | Complesso |

### 5.2 Quando Usare Cosa?

**Monolitico è meglio per:**
- ✅ MVP (Minimum Viable Product)
- ✅ Progetti piccoli/medi
- ✅ Team piccoli
- ✅ Quando performance è critica
- ✅ Quando transazioni ACID sono essenziali
- ✅ Quando data consistency è necessaria

**Microservizi sono meglio per:**
- ✅ Applicazioni enterprise
- ✅ Sistemi ad altissima scalabilità
- ✅ Team grandi e distribuiti
- ✅ Quando parti scalano indipendentemente
- ✅ Quando serve innovazione rapida
- ✅ Quando parti muoiono e ripartono spesso
- ✅ Quando usi cloud (AWS, GCP, Azure)

---

## Capitolo 6: Evoluzione da Monolith a Microservizi

### 6.1 Strangler Pattern

Migrare gradualmente strangolando il monolite:

```
Fase 1: Monolith puro
    Client → Monolith (tutto)

Fase 2: Estrarre Auth Service
    Client → API Gateway → Auth Service
                      ↘ Monolith (ordini, pagamenti)

Fase 3: Estrarre Payment Service
    Client → API Gateway ↗ Auth Service
                      → Payment Service
                      ↘ Monolith (ordini)

Fase 4: Completo
    Client → API Gateway ↗ Auth Service
                      → Payment Service
                      → Order Service
                      → Inventory Service
```

### 6.2 Canary Deployment

Rilascia il 10% di traffico al nuovo servizio prima del 100%:

```java
public class CanaryDeployment {
    
    public String routeToService(String userId) {
        int hash = userId.hashCode() % 100;
        
        if (hash < 10) {
            // 10% al nuovo servizio
            return callService("http://localhost:3003-v2");
        } else {
            // 90% al vecchio servizio
            return callService("http://localhost:3003");
        }
    }

    private String callService(String endpoint) {
        return "✅ Risposta";
    }
}
```

---

## Capitolo 7: Esercizi Proposti

### Esercizio 1: Monolith to Microservices
Prendi l'ECommerceMonolithic e:
- Estrai AuthService
- Estrai PaymentService
- Crea API Gateway
- Implementa comunicazione REST

### Esercizio 2: Circuit Breaker
Implementa un circuit breaker che:
- Traccia errori
- Apre dopo N fallimenti
- Recupera dopo timeout
- Implementa exponential backoff

### Esercizio 3: Message Queue
Aggiungi:
- RabbitMQ o Kafka
- Publish ORDER_CREATED event
- Subscribe in PaymentService
- Implementa Dead Letter Queue

### Esercizio 4: Service Discovery
Crea un service registry dove:
- Servizi si registrano al startup
- API Gateway chiede il registro
- Supporta load balancing
- Heartbeat per salute check

---

## Capitolo 8: Domande di Autovalutazione

### Domanda 1
Qual è il vantaggio principale del monolite?

A) Scalabilità infinita  
B) Semplicità e performance  
C) Facile distribuire su più macchine  
D) No dependencies  

**Risposta corretta: B**

Un monolite è semplice da sviluppare, testare e deployare. Niente overhead di rete, tutte le operazioni in-process sono veloci.

---

### Domanda 2
Qual è il problema principale di un monolite?

A) Troppo lento  
B) Troppo costoso  
C) Scale all or nothing - non puoi scalare singoli componenti  
D) Non supporta multi-thread  

**Risposta corretta: C**

Se il servizio ordini è bottleneck, devi scalare l'INTERO monolite, non solo quella parte.

---

### Domanda 3
Cosa sono i microservizi?

A) Versioni piccole dei servizi  
B) Servizi indipendenti con responsabilità singola  
C) Copie di un servizio grande  
D) Test automatici  

**Risposta corretta: B**

Microservizi sono servizi indipendenti, ognuno con il proprio database, deployabili separatamente e comunicano tramite API.

---
### Domanda 4
Qual è il maggior svantaggio dei microservizi?

A) Sono troppo lenti  
B) Complessità operazionale (deployment, monitoring, logging distribuito)  
C) Non scalano  
D) Costano poco  

**Risposta corretta: B**

Microservizi aggiungono complessità significativa: debugging distribuito è difficile, DevOps skills obbligatori, bisogna gestire network failures.

---

### Domanda 5
Quando i microservizi sono la scelta giusta?

A) Sempre  
B) Per progetti piccoli  
C) Solo per sistemi enterprise ad altissima scalabilità  
D) Mai  

**Risposta corretta: C**

Microservizi hanno senso quando: app è grande, team è distribuito, parti diverse scalano indipendentemente, e il budget DevOps è disponibile.

---

### Risposte Corrette

| Q | Risposta | Spiegazione |
|---|----------|-------------|
| 1 | B | Monolite è semplice e performante, deployment facile |
| 2 | C | Devi scalare TUTTO anche se bottleneck è su una parte |
| 3 | B | Servizi indipendenti con responsabilità singola |
| 4 | B | Complessità operazionale è il costo maggiore |
| 5 | C | Microservizi per sistemi enterprise ad alta scalabilità |

---

## Capitolo 9: Tools e Framework

### 9.1 Framework per Microservizi

**Spring Boot**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

**Quarkus** - Microservizi ultra-veloci
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-resteasy</artifactId>
</dependency>
```

**Helidon** - Microframework Oracle
```xml
<dependency>
    <groupId>io.helidon.webserver</groupId>
    <artifactId>helidon-webserver</artifactId>
</dependency>
```

### 9.2 Orchestrazione Container

**Docker** - Container per ogni servizio

```dockerfile
# AuthService/Dockerfile
FROM openjdk:11
COPY target/auth-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose** - Orchestrazione locale

```yaml
version: '3'
services:
  api-gateway:
    image: api-gateway:1.0
    ports:
      - "8080:8080"
  
  auth-service:
    image: auth-service:1.0
    ports:
      - "3001:3001"
    environment:
      - DB_URL=jdbc:mysql://db:3306/auth
  
  payment-service:
    image: payment-service:1.0
    ports:
      - "3003:3003"
    environment:
      - DB_URL=jdbc:mysql://db:3306/payment
  
  db:
    image: mysql:8
    environment:
      - MYSQL_ROOT_PASSWORD=root
```

**Kubernetes** - Orchestrazione produzione

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: auth-service:1.0
        ports:
        - containerPort: 3001
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

### 9.3 Message Brokers

**RabbitMQ** - AMQP

```java
ConnectionFactory factory = new ConnectionFactory();
factory.setHost("localhost");
Connection connection = factory.newConnection();
Channel channel = connection.createChannel();

channel.basicPublish("", "queue_name", null, message.getBytes());
```

**Apache Kafka** - Event streaming

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);
producer.send(new ProducerRecord<>("orders", "key", "value"));
```

### 9.4 Service Mesh

**Istio** - Gestisce comunicazione tra microservizi

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: payment-service
spec:
  hosts:
  - payment-service
  http:
  - match:
    - uri:
        prefix: "/api/payments"
    route:
    - destination:
        host: payment-service
        port:
          number: 3003
      weight: 90
    - destination:
        host: payment-service-v2
        port:
          number: 3003
      weight: 10
```

---

## Capitolo 10: Esempio Reale - Dall'Idea al Deploy

### 10.1 Fase 1: Prototipo Monolitico (Giorno 1-7)

```
Team: 1-2 developer
Deploy: MVP in 1 settimana
```

### 10.2 Fase 2: Primo Scaling (Settimana 3)

L'app cresce, monolite rallenta:
- Pagamenti lenti
- Inventario sovraccarico
- Utenti si lamentano

**Soluzione:** Estrarre con Strangler Pattern

### 10.3 Fase 3: Microservizi (Mese 2-3)

```
┌────────────────────────────┐
│   API Gateway (Nginx)      │
├────────────────────────────┤
│                            │
├─ Auth Service (3001)       │
├─ Payment Service (3003)    │
├─ Order Service (3002)      │
├─ Inventory Service (3004)  │
├─ Shipping Service (3005)   │
│                            │
├─ RabbitMQ Message Queue    │
├─ Prometheus Monitoring     │
├─ ELK Stack Logging         │
│                            │
└────────────────────────────┘
```

### 10.4 Fase 4: Production Ready (Mese 4-6)

```
├─ Kubernetes Cluster
├─ Service Mesh (Istio)
├─ Circuit Breaker Pattern
├─ Distributed Tracing (Jaeger)
├─ Centralized Logging (ELK)
├─ Prometheus + Grafana Metrics
├─ Blue-Green Deployment
├─ Canary Deployments
└─ Multi-region Replication
```

---

## Capitolo 11: Anti-Patterns da Evitare

### 11.1 Distributed Monolith

```
❌ SBAGLIATO:
Tanti "microservizi" che sono fortemente accoppiati
- Sincronizzazione stretta tra servizi
- Dipendenze circolari
- Un servizio scende, tutta l'app cade
- È il peggio di entrambi i mondi
```

**Soluzione:** Rendere i servizi veramente indipendenti.

### 11.2 API Chatty

```
❌ SBAGLIATO:
Order Service chiama:
  1. Auth Service → /validate
  2. Inventory Service → /check
  3. Payment Service → /process
  4. Shipping Service → /create

Totale 4 network calls per ordine
```

**Soluzione:** API più grandi, meno frequenti. GraphQL per aggregare.

### 11.3 Shared Database

```
❌ SBAGLIATO:
┌─ Auth Service    ┐
├─ Order Service   ├─→ Shared DB
├─ Payment Service │
└─ Inventory Srv   ┘
Violazione del principio database-per-servizio
```

**Soluzione:** Database separati, comunicazione tramite API/events.

### 11.4 Mancanza di Fallback

```
❌ SBAGLIATO:
Payment Service è down
Order Service crasha tutto
Nessun fallback
```

**Soluzione:** Circuit breaker, retry, queue, cache.

---

## Capitolo 12: Monitoring e Debugging

### 12.1 Distributed Tracing

```java
import io.opentelemetry.api.trace.Tracer;

public class OrderServiceWithTracing {
    private Tracer tracer;

    public void createOrder(String orderId) {
        Span span = tracer.spanBuilder("create-order")
            .setAttribute("order.id", orderId)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            callAuthService();
            callInventoryService();
            callPaymentService();
        } finally {
            span.end();
        }
    }
}
```

### 12.2 Structured Logging

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLogging {
    private static Logger logger = LoggerFactory.getLogger(ServiceLogging.class);

    public void processOrder(String orderId, String userId) {
        logger.info("Processing order", 
            "order_id", orderId,
            "user_id", userId,
            "timestamp", System.currentTimeMillis(),
            "service", "order-service");
    }
}
```

### 12.3 Health Checks

```java
public class HealthCheck {
    
    @GetMapping("/health")
    public HealthResponse getHealth() {
        return new HealthResponse(
            "UP",
            "v1.0.0",
            getDatabaseHealth(),
            getCacheHealth(),
            getQueueHealth()
        );
    }

    private String getDatabaseHealth() {
        try {
            dbConnection.getConnection();
            return "UP";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}
```

---

## Capitolo 13: Sicurezza

### 13.1 API Gateway Authentication

```java
public class AuthenticationFilter {
    
    public boolean authenticate(HttpExchange exchange) {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (token == null) {
            return false;
        }
        
        return validateToken(token);
    }

    private boolean validateToken(String token) {
        // JWT verification
        return true;
    }
}
```

### 13.2 Service-to-Service Authentication

```java
// Service A chiama Service B
public class ServiceAuthenticatedCall {
    
    public String callServiceB() {
        String token = generateServiceToken("service-a");
        
        return callWithAuth("http://service-b:3002/api/data", token);
    }

    private String generateServiceToken(String serviceName) {
        // mTLS o JWT con service identity
        return "token_for_" + serviceName;
    }
}
```

---

## Conclusione

### Riepilogo

**Monolite:**
- ✅ Usa per MVP e progetti piccoli
- ❌ Scale orizzontale difficile
- ❌ Deploy rischioso (tutto o niente)

**Microservizi:**
- ✅ Scala indipendente
- ✅ Deployment granulare
- ❌ Complessità operazionale
- ❌ Non per progetti piccoli

**Il viaggio típico:**

```
Monolite (MVP) 
    ↓ (crescita)
Monolite asfittico
    ↓ (Strangler pattern)
Microservizi embrionali
    ↓ (maturità)
Microservizi maturi + Service Mesh
```

### Decisione Finale

Non è "monolite VERSUS microservizi", è **"monolite ORA, microservizi DOPO"**.

Inizia monolitico, scala quando necessario. La complessità deve essere guadagnata, non presunta.

**Quote celebri:**
> "Don't start with microservices. Start with a monolith and extract services as you scale." - Sam Newman

> "Premature distribution is the root of all evil." - Distributed Systems Principles

---

## Risorse Consigliate

- **Libro:** "Building Microservices" di Sam Newman
- **Libro:** "Designing Data-Intensive Applications" di Martin Kleppmann
- **Framework:** Spring Boot / Quarkus / Helidon
- **Orchestrazione:** Docker Compose (dev), Kubernetes (prod)
- **Messaging:** RabbitMQ, Kafka
- **Service Mesh:** Istio, Linkerd
- **Monitoring:** Prometheus, Grafana, Jaeger
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)

---

## Navigazione del Corso
- [📑 Torna all'Indice del Corso](../README.md)
- [⬅️ Guida Precedente](04-Altre-Architetture-di-Rete.md)
- [➡️ Guida Successiva](06-JavaNetworking.md)