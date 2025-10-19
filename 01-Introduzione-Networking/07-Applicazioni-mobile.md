# Applicazioni per Dispositivi Mobile in Java

## Introduzione

Le applicazioni mobili rappresentano il modo principale in cui miliardi di persone accedono ai servizi digitali. A differenza delle applicazioni desktop e server, le app mobili affrontano sfide uniche legate alla batteria, alla connettività variabile, allo schermo piccolo e ai dati limitati. In questa guida esploreremo come sviluppare applicazioni mobili che comunicano efficacemente con i server, gestendo le limitazioni dei dispositivi mobile.

---

## Capitolo 1: Panorama delle Piattaforme Mobili

### 1.1 Le Principali Piattaforme

Attualmente il mercato mobile è dominato da due ecosistemi: Android e iOS. Android è la piattaforma più diffusa con circa il 70% del mercato globale, mentre iOS detiene circa il 27%. Tuttavia, per uno sviluppatore Java, le scelte diventano più complesse, poiché Java non è il linguaggio nativo di iOS.

**Android** utilizza Java e Kotlin come linguaggi principali. Quando sviluppi un'applicazione Android, lavori direttamente con le classi Java e puoi accedere ai socket TCP/UDP attraverso le stesse API che useresti su un server. L'ambiente di runtime è la Java Virtual Machine (JVM) ottimizzata per dispositivi mobili, anche se negli ultimi anni è stato gradualmente sostituito da ART (Android Runtime).

**iOS** utilizza Swift come linguaggio principale, con Objective-C ancora supportato per scopi di compatibilità. Per uno sviluppatore Java, iOS non è una scelta naturale, a meno che tu non sia disposto a imparare Swift o a usare framework cross-platform.

### 1.2 Architetture di Sviluppo Mobile

Quando si sviluppa un'applicazione mobile, hai fondamentalmente tre scelte architetturali che rappresentano diversi compromessi tra performance, costi di sviluppo e mantenibilità.

```
┌─────────────────────────────────────────┐
│     Native (Android)                    │
│  • Linguaggio: Java/Kotlin              │
│  • Performance: Eccellente              │
│  • Codice riutilizzabile: Solo Android  │
├─────────────────────────────────────────┤
│     Cross-Platform (React Native)       │
│  • Linguaggio: JavaScript               │
│  • Performance: Buona                   │
│  • Codice riutilizzabile: Sì (80%)      │
├─────────────────────────────────────────┤
│     Hybrid (Cordova/Capacitor)          │
│  • Linguaggio: HTML/CSS/JavaScript      │
│  • Performance: Accettabile             │
│  • Codice riutilizzabile: Sì (90%)      │
└─────────────────────────────────────────┘
```

### 1.3 Native vs Cross-Platform

Sviluppare in modo nativo per Android significa sfruttare appieno il framework Android SDK, ottenendo le migliori performance e l'accesso a tutte le feature del dispositivo. Lo svantaggio è che il codice scritto per Android non può essere riutilizzato per iOS, il che significa che dovrai mantenere due codebase completamente separate se desideri supportare entrambe le piattaforme.

Gli approcci cross-platform come React Native, Flutter, o Cordova permettono di scrivere il codice una volta e distribuirlo su più piattaforme. Questo riduce significativamente i costi di sviluppo e manutenzione, ma con il tradeoff di performance leggermente inferiori e accesso limitato alle feature native del dispositivo.

---

## Capitolo 2: Comunicazione Mobile-Server

### 2.1 Sfide Uniche della Comunicazione Mobile

Quando un'applicazione mobile comunica con un server, deve affrontare condizioni molto diverse rispetto a un client desktop. La connessione di rete non è garantita: gli utenti possono perdere il segnale entro un tunnel, passare da WiFi a dati mobili, o semplicemente chiudere l'app e riavviarla ore dopo. Inoltre, i dati mobili sono spesso a pagamento, quindi l'efficienza nella larghezza di banda diventa cruciale. La batteria è un'altra considerazione importante: mantenere una connessione socket aperta consuma energia significativa, mentre piccole richieste HTTP richiedono meno potenza.

```
Device Mobile              Rete Mobile          Server
┌─────────┐               ┌────────┐            ┌──────┐
│ Client  │──HTTP/REST───→│ Carrier│───────────→│Server│
│  App    │←──JSON────────│ 4G/5G  │←───────────│      │
└─────────┘               │        │            └──────┘
   • Batteria limitata     • Instabile      • Sempre online
   • Banda limitata        • Latenza var.   • Infinita energia
   • Schermo piccolo       • Intermittente  • Banda illimitata
```

### 2.2 Socket TCP vs HTTP/REST

In linea teorica, potresti aprire una connessione socket TCP dal tuo dispositivo mobile a un server e mantenere quella connessione aperta per tutta la sessione dell'utente. Tuttavia, nella pratica questo non è consigliabile per le applicazioni mobili. I router e i gateway di rete mobile hanno timeout che disconnettono le connessioni inattive. Inoltre, mantenere una socket aperta consuma batteria costantemente.

HTTP REST è la scelta molto più pratica. Ogni richiesta HTTP apre una connessione, invia i dati, riceve la risposta e chiude la connessione. Sebbene questo comporti overhead di connessione, il vantaggio è che la connessione viene chiusa quando non in uso, risparmiando batteria. HTTP inoltre passa attraverso proxy e firewall molto più facilmente di una socket TCP raw.

```java
// SCONSIGLIATO per mobile - consuma troppa batteria
Socket socket = new Socket("server.com", 5000);
InputStream in = socket.getInputStream();
OutputStream out = socket.getOutputStream();
// Mantieni socket aperta... male per batteria!

// CONSIGLIATO per mobile - HTTP request
HttpURLConnection conn = (HttpURLConnection) 
    new URL("https://api.server.com/data").openConnection();
conn.setRequestMethod("GET");
int responseCode = conn.getResponseCode();
// Chiudi subito dopo - efficiente per batteria
conn.disconnect();
```

### 2.3 Architettura Tipica Client-Server Mobile

L'architettura standard per un'applicazione mobile prevede l'app sul dispositivo che comunica con uno o più servizi backend tramite HTTP/REST. Il backend gestisce la logica di business, l'accesso ai database e l'autenticazione. L'app si concentra sulla presentazione dell'interfaccia utente e sulla gestione della cache locale.

```
┌──────────────────────┐
│   Mobile App         │
│                      │
│  UI Layer            │
│  ├─ Activities       │
│  ├─ Fragments        │
│  └─ Views            │
│                      │
│  Business Logic      │
│  ├─ ViewModels       │
│  ├─ Repositories     │
│  └─ Services         │
│                      │
│  Data Layer          │
│  ├─ Local SQLite DB  │
│  ├─ Shared Prefs     │
│  └─ File System      │
└────────────┬─────────┘
             │ HTTP/REST
             ▼
┌──────────────────────┐
│   Backend Server     │
│                      │
│  API Layer           │
│  └─ REST Endpoints   │
│                      │
│  Business Layer      │
│  ├─ Services         │
│  ├─ Repositories     │
│  └─ Controllers      │
│                      │
│  Database            │
│  └─ PostgreSQL/MySQL │
└──────────────────────┘
```

---

## Capitolo 3: Sviluppo Android Nativo

### 3.1 Struttura di un'Applicazione Android

Un'applicazione Android segue un'architettura basata su componenti. La classe principale è l'**Activity**, che rappresenta una singola schermata con cui l'utente interagisce. Quando avvii l'app, un'Activity viene creata e visualizzata. Quando l'utente preme il pulsante indietro, quella Activity viene distrutta.

Per mantenere i dati quando l'Activity viene ricreata (ad esempio durante una rotazione dello schermo), usiamo **ViewModel**. Il ViewModel sopravvive ai cambiamenti di configurazione dell'Activity, il che significa che i dati rimangono disponibili anche se lo schermo viene ruotato.

```java
// MainActivity.java - L'Activity principale
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    private WeatherViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Ottieni il ViewModel - sopravvive a ricreazione Activity
        viewModel = new ViewModelProvider(this)
            .get(WeatherViewModel.class);
        
        // Osserva i dati - quando cambiano, aggiorna UI
        viewModel.getWeatherData().observe(this, weather -> {
            updateUI(weather);
        });
    }

    private void updateUI(WeatherData weather) {
        findViewById(R.id.tempText)
            .setText("Temperatura: " + weather.getTemperature() + "°C");
    }
}
```

### 3.2 Effettuare Richieste HTTP da Android

Per comunicare con il server, Android fornisce diverse opzioni. La classe `HttpURLConnection` è integrata nel framework, ma è verbosa. Molte applicazioni usano librerie di terze parti come **Retrofit** o **OkHttp** che semplificano significativamente il codice.

```java
// WeatherService.java - Servizio per comunicare con il server
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

public class WeatherService {
    private static final String API_URL = 
        "https://api.openweathermap.org/data/2.5/weather";
    
    public static WeatherData getWeather(String city) throws Exception {
        // Crea URL con parametri
        String urlString = API_URL + "?q=" + city + "&appid=YOUR_KEY";
        URL url = new URL(urlString);
        
        // Apri connessione
        HttpURLConnection conn = 
            (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        // Leggi risposta
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        
        // Chiudi risorse
        reader.close();
        conn.disconnect();
        
        // Parsa JSON e ritorna dato
        JSONObject json = new JSONObject(response.toString());
        double temp = json.getJSONObject("main")
            .getDouble("temp");
        
        return new WeatherData(temp, city);
    }
}
```

Nota che il codice sopra non dovrebbe mai essere eseguito sul thread principale di Android, poiché blocca l'interfaccia utente. Android richiede che tutte le operazioni di rete avvengano su thread separati.

```java
// Corretto: effettua la richiesta su un thread di background
new Thread(() -> {
    try {
        WeatherData weather = WeatherService.getWeather("Rome");
        runOnUiThread(() -> {
            TextView tempView = findViewById(R.id.tempText);
            tempView.setText("Temperatura: " + weather.getTemperature() 
                + "°C");
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
}).start();
```

### 3.3 Retrofit: Libreria Moderna per HTTP

Retrofit semplifica drasticamente le richieste HTTP permettendoti di definire le API come interfacce Java. La libreria genera automaticamente il codice necessario per effettuare le richieste.

```java
// WeatherApi.java - Definisci l'API come interfaccia
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherResponse> getWeather(
        @Query("q") String city,
        @Query("appid") String apiKey
    );
}

// Utilizzo in ViewModel
public class WeatherViewModel extends ViewModel {
    private final MutableLiveData<WeatherData> weatherData = 
        new MutableLiveData<>();
    
    public void fetchWeather(String city) {
        // Crea istanza Retrofit
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        
        WeatherApi api = retrofit.create(WeatherApi.class);
        
        // Effettua richiesta asincrona
        api.getWeather(city, "YOUR_KEY").enqueue(
            new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, 
                        Response<WeatherResponse> response) {
                    if (response.isSuccessful()) {
                        WeatherData data = convertToWeatherData(
                            response.body());
                        weatherData.setValue(data);
                    }
                }
                
                @Override
                public void onFailure(Call<WeatherResponse> call, 
                        Throwable t) {
                    // Gestisci errore
                    t.printStackTrace();
                }
            }
        );
    }
    
    public LiveData<WeatherData> getWeatherData() {
        return weatherData;
    }
}
```

---

## Capitolo 4: Gestione della Persistenza Dati

### 4.1 Database Locale con SQLite

Molte applicazioni mobili devono funzionare anche offline o con connessione lenta. Per questo motivo, è comune memorizzare i dati in un database locale SQLite. Android fornisce SQLite gratuitamente come parte del framework.

```java
// WeatherDatabase.java - Definisci il database
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {WeatherEntity.class}, version = 1)
public abstract class WeatherDatabase extends RoomDatabase {
    public abstract WeatherDao weatherDao();
}

// WeatherEntity.java - Definisci un'entità
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather")
public class WeatherEntity {
    @PrimaryKey
    public long id;
    
    public String city;
    public double temperature;
    public long timestamp;
}

// WeatherDao.java - Data Access Object
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface WeatherDao {
    @Insert
    void insert(WeatherEntity weather);
    
    @Query("SELECT * FROM weather WHERE city = :city")
    WeatherEntity getWeatherForCity(String city);
    
    @Query("SELECT * FROM weather")
    List<WeatherEntity> getAllWeather();
}
```

### 4.2 Sincronizzazione con il Server

L'approccio più robusto per gestire la sincronizzazione è il pattern "Offline-First". L'idea è che l'app sempre scrive nel database locale prima di tentare di sincronizzare con il server. Se la sincronizzazione fallisce, i dati rimangono locali e verranno sincronizzati quando la rete tornerà disponibile.

```java
// WeatherRepository.java - Gestisce logica di sincronizzazione
public class WeatherRepository {
    private final WeatherDao dao;
    private final WeatherApi api;
    private final ConnectivityManager connectivity;
    
    public void saveWeather(WeatherData data) {
        // 1. Salva sempre nel database locale
        WeatherEntity entity = convertToEntity(data);
        dao.insert(entity);
        
        // 2. Prova a sincronizzare col server se online
        if (isNetworkAvailable()) {
            syncWithServer(entity);
        } else {
            // Marca come "pending sync" per sincronizzare dopo
            markForSync(entity);
        }
    }
    
    private void syncWithServer(WeatherEntity entity) {
        api.updateWeather(entity).enqueue(
            new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, 
                        Response<Void> response) {
                    if (response.isSuccessful()) {
                        entity.synced = true;
                        dao.update(entity);
                    }
                }
                
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Riprova dopo
                    scheduleResync();
                }
            }
        );
    }
    
    private boolean isNetworkAvailable() {
        NetworkInfo activeNetwork = 
            connectivity.getActiveNetworkInfo();
        return activeNetwork != null && 
            activeNetwork.isConnectedOrConnecting();
    }
}
```

---

## Capitolo 5: Ottimizzazione della Batteria e della Rete

### 5.1 Best Practices per Risparmiare Batteria

La batteria è una risorsa limitata sui dispositivi mobili. Ogni operazione che effettui consuma energia, e le operazioni di rete sono tra le più dispendiose. Ecco le strategie principali per ottimizzare i consumi.

La prima strategia è **raggruppare le richieste di rete**. Invece di effettuare molte piccole richieste, è meglio fare poche richieste più grandi. Questo perché il raggio trasmettitore della radio richiede tempo per accendersi e spegnersi, e rimane in uno stato ad alta energia per un po' dopo che la trasmissione è completata.

```java
// MALE - troppe piccole richieste
for (String city : cities) {
    getWeatherForCity(city);  // 5 richieste separate
}

// BENE - una richiesta che ritorna tutto
getWeatherForMultipleCities(cities);  // 1 richiesta
```

La seconda strategia è **utilizzare throttling e debouncing**. Se l'utente digita in una barra di ricerca, non vuoi effettuare una richiesta di rete ad ogni keypress. Usa debouncing per aspettare che l'utente finisca di digitare.

```java
// Debouncing di una query di ricerca
private Handler handler = new Handler(Looper.getMainLooper());
private Runnable searchRunnable;
private static final long SEARCH_DELAY = 500; // ms

public void onSearchTextChanged(String query) {
    // Cancella il search precedente
    handler.removeCallbacks(searchRunnable);
    
    // Pianifica un nuovo search dopo il delay
    searchRunnable = () -> performSearch(query);
    handler.postDelayed(searchRunnable, SEARCH_DELAY);
}
```

La terza strategia è **usare compressionye caching**. Comprimi le risposte JSON e memorizza in cache i dati che probabilmente verranno richiesti di nuovo.

```java
// Configurare OkHttp per compressione e caching
OkHttpClient client = new OkHttpClient.Builder()
    .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
    .addInterceptor(new HttpLoggingInterceptor())
    .build();

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(API_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

### 5.2 Carico dei Dati Adattivo

Un'altra strategia intelligente è il **carico adattivo**, dove la qualità e la quantità dei dati scaricati variano in base alla connessione di rete disponibile.

```java
// AdaptiveDataLoader.java
public class AdaptiveDataLoader {
    
    public LoadQuality determineLoadQuality() {
        Network network = getActiveNetwork();
        
        if (network == null) {
            return LoadQuality.OFFLINE;
        }
        
        NetworkCapabilities capabilities = 
            connectivity.getNetworkCapabilities(network);
        
        if (capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI)) {
            return LoadQuality.HIGH;  // WiFi: scarica tutto
        } else if (capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR)) {
            // Mobile: scarica meno dati
            if (capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                return LoadQuality.MEDIUM;
            } else {
                return LoadQuality.LOW;  // Dati a pagamento
            }
        }
        
        return LoadQuality.LOW;
    }
    
    public int getImageQuality(LoadQuality quality) {
        switch (quality) {
            case HIGH: return 100;      // WiFi
            case MEDIUM: return 75;     // Dati illimitati
            case LOW: return 50;        // Dati a pagamento
            case OFFLINE: return 0;     // Offline
            default: return 50;
        }
    }
}
```

---

## Capitolo 6: Approcci Cross-Platform

### 6.1 React Native

React Native permette di scrivere applicazioni mobili utilizzando JavaScript e React. Il codice viene compilato a componenti native, fornendo performance molto vicine alle applicazioni native, mentre consenti il riutilizzo di gran parte del codice tra iOS e Android.

```javascript
// App.js - Applicazione React Native
import React, { useState, useEffect } from 'react';
import { View, Text, ActivityIndicator, StyleSheet } from 'react-native';

const App = () => {
  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Effettua la richiesta al server
    fetch('https://api.openweathermap.org/data/2.5/weather?q=Rome&appid=KEY')
      .then(response => response.json())
      .then(data => {
        setWeather(data);
        setLoading(false);
      })
      .catch(error => {
        console.error(error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <ActivityIndicator size="large" />;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Weather</Text>
      <Text style={styles.temp}>
        {Math.round(weather.main.temp - 273.15)}°C
      </Text>
      <Text style={styles.description}>
        {weather.weather[0].description}
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  temp: {
    fontSize: 48,
    fontWeight: 'bold',
    marginVertical: 20,
  },
  description: {
    fontSize: 16,
    textTransform: 'capitalize',
  },
});

export default App;
```

### 6.2 Flutter

Flutter è un framework sviluppato da Google che permette di creare applicazioni per iOS, Android, web e desktop da una singola codebase. Usa il linguaggio Dart e fornisce performance eccellente perché compila direttamente a codice nativo.

```dart
// main.dart - Applicazione Flutter
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Weather App',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const WeatherScreen(),
    );
  }
}

class WeatherScreen extends StatefulWidget {
  const WeatherScreen({Key? key}) : super(key: key);

  @override
  State<WeatherScreen> createState() => _WeatherScreenState();
}

class _WeatherScreenState extends State<WeatherScreen> {
  late Future<Weather> futureWeather;

  @override
  void initState() {
    super.initState();
    futureWeather = fetchWeather();
  }

  Future<Weather> fetchWeather() async {
    final response = await http.get(Uri.parse(
      'https://api.openweathermap.org/data/2.5/weather?q=Rome&appid=KEY',
    ));

    if (response.statusCode == 200) {
      return Weather.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to load weather');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Weather')),
      body: Center(
        child: FutureBuilder<Weather>(
          future: futureWeather,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '${(snapshot.data!.temp - 273.15).toStringAsFixed(1)}°C',
                    style: const TextStyle(fontSize: 48),
                  ),
                  Text(
                    snapshot.data!.description,
                    style: const TextStyle(fontSize: 16),
                  ),
                ],
              );
            } else if (snapshot.hasError) {
              return Text('${snapshot.error}');
            }
            return const CircularProgressIndicator();
          },
        ),
      ),
    );
  }
}

class Weather {
  final double temp;
  final String description;

  Weather({required this.temp, required this.description});

  factory Weather.fromJson(Map<String, dynamic> json) {
    return Weather(
      temp: json['main']['temp'].toDouble(),
      description: json['weather'][0]['description'],
    );
  }
}
```

---

## Capitolo 7: Sicurezza nelle Applicazioni Mobili

### 7.1 Autenticazione e Autorizzazione

Le applicazioni mobili devono autenticare gli utenti e proteggersi da accessi non autorizzati. Il metodo standard è utilizzare token JWT (JSON Web Tokens) che vengono inviati con ogni richiesta.

```java
// AuthenticationManager.java
public class AuthenticationManager {
    private static final String KEY_TOKEN = "auth_token";
    private SharedPreferences prefs;
    
    public void login(String username, String password) 
            throws Exception {
        // Effettua login al server
        LoginRequest request = new LoginRequest(username, password);
        LoginResponse response = apiService.login(request).execute()
            .body();
        
        if (response != null && response.getToken() != null) {
            // Salva il token in modo sicuro
            saveTokenSecurely(response.getToken());
        }
    }
    
    private void saveTokenSecurely(String token) {
        // Usa EncryptedSharedPreferences per proteggere il token
        EncryptedSharedPreferences.create(...)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply();
    }
    
    public String getToken() {
        return EncryptedSharedPreferences.create(...)
            .getString(KEY_TOKEN, null);
    }
    
    public void logout() {
        // Cancella il token
        EncryptedSharedPreferences.create(...)
            .edit()
            .remove(KEY_TOKEN)
            .apply();
    }
}

// Usa il token in ogni richiesta
public class AuthInterceptor implements Interceptor {
    private final AuthenticationManager authManager;
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = authManager.getToken();
        Request originalRequest = chain.request();
        
        Request.Builder builder = originalRequest.newBuilder();
        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        
        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }
}
```

### 7.2 Protezione della Comunicazione

Tutte le comunicazioni tra l'app mobile e il server devono utilizzare HTTPS per proteggere i dati in transito. Inoltre, è importante validare il certificato del server per prevenire attacchi di type man-in-the-middle.

```java
// CertificatePinning.java - Verifica che comunichi con il server giusto
public class CertificatePinning {
    
    public static OkHttpClient getSecureClient() {
        // Crea un set di certificati pinned (fidati)
        CertificatePinner certificatePinner = 
            new CertificatePinner.Builder()
                .add("api.example.com", 
                    "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .add("api.example.com", 
                    "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
                .build();
        
        return new OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .build();
    }
}
```

---

## Capitolo 8: Esercizi Proposti

### Esercizio 1: Applicazione Meteo Android

Sviluppa un'app Android che:
- Effettua richieste HTTP a un'API meteo pubblica
- Visualizza il meteo per multiple città
- Salva i dati localmente in SQLite
- Funziona offline mostrando i dati in cache
- Implementa refresh manuale con pull-to-refresh

### Esercizio 2: Chat Mobile

Crea un'applicazione mobile (Android o React Native) che:
- Si connette a un server WebSocket
- Permette di inviare e ricevere messaggi
- Mostra la lista di utenti online
- Persiste i messaggi localmente
- Notifica l'utente di nuovi messaggi

### Esercizio 3: App di Sincronizzazione

Realizza un'app che:
- Consente la modifica offline di note/todo
- Sincronizza automaticamente quando online
- Gestisce i conflitti (modifche locali vs server)
- Mostra lo stato di sincronizzazione
- Implementa retry automatico su fallimento

### Esercizio 4: App con Autenticazione

Sviluppa un'app che:
- Implementa login e registrazione
- Salva i token in modo sicuro
- Effettua richieste autenticate
- Gestisce l'expired token con refresh automatico
- Mostra informazioni del profilo utente

---

## Capitolo 9: Domande di Autovalutazione

### Domanda 1
Perché le applicazioni mobili preferiscono HTTP/REST alle socket TCP?

A) HTTP è più veloce delle socket  
B) Le socket consumano molta più batteria e hanno problemi con i timeout di rete mobile  
C) HTTP supporta la crittografia, le socket no  
D) Android non supporta le socket  

**Risposta corretta: B**

Le connessioni socket rimangono aperte continuamente e consumano molta energia. HTTP apre la connessione, invia il dato, riceve la risposta e chiude la connessione, risparmiando batteria. Inoltre, i provider di rete mobile disconnettono le socket inattive.

---

### Domanda 2
Cosa è il pattern "Offline-First"?

A) Disabilitare la rete sull'app  
B) Salvare sempre i dati localmente prima di sincronizzare col server  
C) Non sincronizzare mai  
D) Usare solo il database locale  

**Risposta corretta: B**

Offline-First significa che l'app scrive nel database locale per primo, garantendo che i dati siano sempre disponibili anche offline. La sincronizzazione col server avviene quando possibile, ma non è un prerequisito per salvare i dati.

---

### Domanda 3
Quale libreria rende più semplice effettuare richieste HTTP in Android?

A) Socket  
B) HttpURLConnection  
C) Retrofit  
D) JDBC  

**Risposta corretta: C**

Retrofit astrae i dettagli delle richieste HTTP permettendo di definire le API come interfacce Java. La libreria genera automaticamente il codice necessario.

---

### Domanda 4
Perché è importante il throttling/debouncing nelle app mobili?

A) Per migliorare il design dell'interfaccia  
B) Per risparmiare batteria riducendo il numero di richieste di rete  
C) Per rendere l'app più veloce  
D) Per sincronizzazione dei dati  

**Risposta corretta: B**

Il throttling e debouncing riducono il numero di richieste di rete non necessarie. Ad esempio, quando l'utente digita in una barra di ricerca, non vuoi effettuare una richiesta ad ogni carattere, ma aspettare che finisca.

---

### Domanda 5
Quando usare React Native invece di sviluppo nativo Android?

A) Sempre, è sempre meglio  
B) Quando hai bisogno di supportare solo Android  
C) Quando devi supportare iOS e Android e vuoi riutilizzare il codice  
D) Mai, il nativo è sempre meglio  

**Risposta corretta: C**

React Native ha senso quando devi supportare multiple piattaforme e vuoi riutilizzare il codice. Lo svantaggio è performance leggermente inferiori. Se supporti solo Android, lo sviluppo nativo è la scelta migliore.

---

## Risposte Corrette

| Q | Risposta | Spiegazione |
|---|----------|-------------|
| 1 | B | Socket consumano batteria, HTTP è più efficiente |
| 2 | B | Offline-First: salva localmente, sincronizza dopo |
| 3 | C | Retrofit semplifica drasticamente le richieste HTTP |
| 4 | B | Riducono il numero di richieste di rete non necessarie |
| 5 | C | React Native per multi-platform, nativo per performance |

---

## Conclusione

Le applicazioni mobili rappresentano il futuro della computing, ma hanno vincoli molto diversi dalle applicazioni desktop e server. Lo sviluppatore mobile deve considerare attentamente la batteria, la larghezza di banda, la connettività variabile e il calore generato dal dispositivo.

La comunicazione efficiente con i server backend è fondamentale. Preferisci HTTP/REST alle socket TCP, raggruppa le richieste, implementa caching e sincronizzazione intelligente, e comprendi come la rete mobile differisce dalla rete fissa.

Che tu scelga di sviluppare nativamente con Android SDK, o di usare framework cross-platform come React Native o Flutter, i principi rimangono gli stessi: costruisci app che funzionano offline, che sincronizzano intelligentemente, che rispettano la batteria dell'utente, e che comunicano in modo efficiente con i tuoi server backend.