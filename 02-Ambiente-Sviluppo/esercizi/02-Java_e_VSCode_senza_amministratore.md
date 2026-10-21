# Esercizio 02: Java e VS Code su Windows senza amministratore

## Obiettivo

Prepara il tuo account Windows per scrivere, compilare ed eseguire programmi
Java, anche se **non sei amministratore del PC**. Al termine dovrai riuscire a:

- Usare il supporto Java in Visual Studio Code.
- Eseguire `javac` e `java` dal terminale PowerShell.
- Compilare ed eseguire un piccolo programma con entrambi gli strumenti.

Servono due componenti con compiti diversi:

| Componente | Funzione |
|---|---|
| **JDK (Java Development Kit)** | Contiene `javac`, il compilatore, e `java`, il comando per eseguire i programmi |
| **Extension Pack for Java** di Microsoft | Aggiunge a VS Code completamento, controllo degli errori, esecuzione e debug |

L'estensione può utilizzare un runtime interno per i propri servizi: questo
non configura automaticamente `javac` nel terminale Windows. Installerai quindi
un JDK nella tua cartella personale e ne aggiungerai `bin` al `Path` utente.

## Prerequisiti e ambiente di lavoro

Lavora **sul PC Windows locale**, con il tuo account studente e una connessione
Internet. Apri PowerShell normalmente dal menu Start. I comandi seguenti sono
per PowerShell, anche quando usi il terminale integrato di VS Code.

Questo esercizio riguarda il PC locale: la sessione SSH Web4Student
dell'[esercizio 01](01-Git_e_GitHub.md) è un ambiente distinto. In VS Code usa
una finestra locale, senza connessioni Remote SSH o WSL.

Useremo **Eclipse Temurin JDK 25 LTS**, distribuito gratuitamente da Adoptium,
in formato **ZIP**. Il formato archivio permette di estrarre il JDK in una
cartella scrivibile dal proprio utente, come documentato nella
[guida Adoptium](https://adoptium.net/installation/archives/).

Le cartelle previste sono:

```text
C:\Users\NOME-UTENTE\
├── Tools\
│   └── Java\
│       └── jdk-25\
│           ├── bin\
│           │   ├── java.exe
│           │   └── javac.exe
│           └── ...
└── socket\
    └── es02-ambiente-java\
        └── HelloSocket.java
```

`NOME-UTENTE` rappresenta il tuo nome utente Windows. Nei comandi PowerShell,
`$env:USERPROFILE` indica automaticamente la cartella personale corretta.
Il JDK rimane separato dai sorgenti degli esercizi.

## Passo 1 — Verificare o installare VS Code per il proprio utente

Se VS Code è già presente e si avvia con il tuo account, riutilizzalo.
Altrimenti apri il [sito ufficiale di VS Code](https://code.visualstudio.com/download)
e scarica **User Installer** per Windows, scegliendo l'architettura del PC.
Esegui l'installazione per il tuo utente, con la destinazione proposta nella
cartella personale. Il [User Setup di VS Code](https://code.visualstudio.com/docs/setup/windows)
non richiede privilegi amministrativi.

Se il laboratorio blocca l'installazione o l'esecuzione di applicazioni personali,
comunicalo al docente: queste limitazioni richiedono l'intervento della scuola.

## Passo 2 — Scaricare il JDK in formato ZIP

1. Apri la [pagina dei download Eclipse Temurin](https://adoptium.net/temurin/releases/).
2. Seleziona **Version: 25 - LTS**, **Operating System: Windows** e **Package: JDK**.
3. Seleziona **x64** per i comuni PC con processore Intel/AMD a 64 bit, oppure
   **aarch64/ARM64** se il tuo PC Windows usa un processore ARM.
   Puoi controllare il tipo di sistema in **Impostazioni → Sistema → Informazioni**.
4. Scarica il pacchetto **`.zip`** della versione scelta.

Controlla di avere selezionato **JDK**: serve il compilatore `javac`.
Conserva il file ZIP nella cartella Download fino alla verifica del passo 3.

## Passo 3 — Estrarre il JDK nella cartella personale

1. In Esplora file, digita `%USERPROFILE%` nella barra degli indirizzi.
2. Crea la cartella `Tools`, poi la sottocartella `Java`.
3. Sullo ZIP scaricato usa **Estrai tutto**.
4. Sposta la cartella del JDK estratto, che avrà un nome simile a
   `jdk-25.0.x+...`, dentro `%USERPROFILE%\Tools\Java` e rinominala `jdk-25`.

Apri `jdk-25`: al suo interno deve esserci direttamente la cartella `bin`.
Evita un ulteriore livello del tipo `jdk-25\jdk-25.0.x\bin`.

In PowerShell verifica gli eseguibili usando il percorso completo:

```powershell
Test-Path "$env:USERPROFILE\Tools\Java\jdk-25\bin\javac.exe"
& "$env:USERPROFILE\Tools\Java\jdk-25\bin\java.exe" -version
& "$env:USERPROFILE\Tools\Java\jdk-25\bin\javac.exe" -version
```

**Verifica:** il primo comando restituisce `True`; gli altri mostrano Java e
javac versione `25` con l'eventuale numero di aggiornamento. Il simbolo `&`
permette a PowerShell di eseguire un programma il cui percorso è tra virgolette.

## Passo 4 — Configurare JAVA_HOME e Path nelle variabili utente

Dal menu Start cerca **Modifica le variabili di ambiente per l'account**.
Nella finestra aperta lavora nella sezione **Variabili utente per ...**.

### Impostare JAVA_HOME

Crea la variabile `JAVA_HOME`, oppure modifica quella del tuo utente se esiste:

| Campo | Valore |
|---|---|
| Nome variabile | `JAVA_HOME` |
| Valore variabile | Il percorso completo della cartella `jdk-25`, ad esempio `C:\Users\mario\Tools\Java\jdk-25` |

Usa il tuo percorso reale, senza virgolette. `JAVA_HOME` indica la radice del
JDK e **non deve terminare con `\bin`**.

### Aggiungere il compilatore al Path utente

1. Seleziona **Path** nella sezione delle variabili **utente** e scegli **Modifica**.
2. Aggiungi una nuova voce con il percorso completo della cartella `bin`, ad
   esempio `C:\Users\mario\Tools\Java\jdk-25\bin`, senza virgolette.
3. Porta questa voce prima delle altre eventuali voci Java del tuo Path utente.
4. Conserva tutte le altre voci del Path e conferma le finestre con **OK**.

Le modifiche riguardano il tuo account. La distinzione tra variabili utente,
di sistema e del processo è descritta nella
[documentazione Microsoft sulle variabili d'ambiente](https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_environment_variables).

### Verificare da un nuovo terminale

Chiudi PowerShell e **tutte le finestre di VS Code**, poi riaprili dal menu Start:
i programmi già aperti possono conservare i vecchi valori delle variabili.
In una nuova PowerShell esegui:

```powershell
$env:JAVA_HOME
java -version
javac -version
where.exe java
where.exe javac
```

**Verifica:** `JAVA_HOME` indica il JDK estratto; `java` e `javac` mostrano la
versione `25`. Il primo percorso restituito dai due comandi `where.exe` deve
puntare alla tua cartella `jdk-25\bin`.

### Se Windows trova prima un'altra versione di Java

Un Java presente nel Path di sistema può avere precedenza sul Path utente.
Per usare il tuo JDK nella **PowerShell corrente**, esegui:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\Tools\Java\jdk-25"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

La modifica del Path qui vale per questa sessione e per i processi avviati da
essa. Se necessario ripetila nel terminale integrato di VS Code e nelle nuove
sessioni. Puoi sempre richiamare gli eseguibili tramite il percorso completo,
come nel passo 3.

## Passo 5 — Installare Extension Pack for Java in VS Code

1. Apri VS Code e premi **Ctrl+Shift+X** per aprire le estensioni.
2. Cerca **Extension Pack for Java**.
3. Scegli il pacchetto pubblicato da **Microsoft**, identificativo
   **`vscjava.vscode-java-pack`**, e premi **Install**.
4. Attendi che vengano installate le estensioni del pacchetto.

Puoi riconoscerlo dalla [pagina ufficiale nel Marketplace](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).
Il pacchetto comprende strumenti per il linguaggio, il debug, i test e la gestione
dei progetti. Le estensioni si installano nel profilo dell'utente, come spiegato
nella [guida alle estensioni di VS Code](https://code.visualstudio.com/docs/configure/extensions/extension-marketplace).

## Passo 6 — Indicare a VS Code il JDK del progetto

1. Premi **Ctrl+Shift+P**.
2. Cerca **Preferences: Open User Settings (JSON)** e aprilo.
3. Aggiungi le proprietà seguenti all'oggetto JSON esistente, sostituendo `mario`
   con il tuo nome utente Windows. Se una proprietà esiste già, aggiornala.

```json
{
    "java.jdt.ls.java.home": "C:/Users/mario/Tools/Java/jdk-25",
    "java.configuration.runtimes": [
        {
            "name": "JavaSE-25",
            "path": "C:/Users/mario/Tools/Java/jdk-25",
            "default": true
        }
    ]
}
```

Se ci sono già altre impostazioni, mantienile e separa le proprietà con una
virgola. I percorsi JSON possono usare `/` anche su Windows; indicano la radice
del JDK, senza `bin`.

`java.jdt.ls.java.home` seleziona il Java per il servizio di supporto al linguaggio
quando l'estensione usa un JDK esterno; le versioni dell'estensione per Windows
possono includere il proprio runtime. `java.configuration.runtimes` indica il
JDK dei progetti e il valore `default` vale per cartelle senza Maven o Gradle.
Queste impostazioni non cambiano il Path del terminale.
Riferimenti: [configurazione del JDK nell'estensione Java](https://marketplace.visualstudio.com/items?itemName=redhat.java)
e [gestione dei runtime dei progetti](https://code.visualstudio.com/docs/java/java-project).

## Passo 7 — Creare e compilare HelloSocket da terminale

In PowerShell crea una cartella di lavoro locale:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\socket\es02-ambiente-java"
Set-Location "$env:USERPROFILE\socket\es02-ambiente-java"
```

In VS Code usa **File → Apri cartella** e apri questa cartella. Crea il file
**`HelloSocket.java`**, inserisci il codice seguente e salvalo:

```java
public class HelloSocket {
    public static void main(String[] args) {
        System.out.println("Ambiente Java pronto per Socket Programming!");
        System.out.println("Versione Java: " + System.getProperty("java.version"));
    }
}
```

Il nome del file deve coincidere con il nome della classe pubblica, comprese le
maiuscole. Controlla che il file non sia stato salvato come `HelloSocket.java.txt`.

Torna alla PowerShell aperta nella cartella dell'esercizio ed esegui:

```powershell
javac HelloSocket.java
Get-ChildItem HelloSocket.*
java -cp . HelloSocket
```

**Verifica:** `javac` termina senza messaggi di errore e produce
`HelloSocket.class`. Il programma stampa:

```text
Ambiente Java pronto per Socket Programming!
Versione Java: 25...
```

Il numero completo dipende dall'aggiornamento installato. Con `javac` passi il
file `.java`; con `java` passi il nome della classe, senza estensione.
`-cp .` indica di cercare la classe nella cartella corrente.

## Passo 8 — Eseguire il programma da VS Code

Con `HelloSocket.java` aperto, attendi il caricamento del supporto Java.
Premi **Ctrl+Shift+P**, esegui **Java: Configure Java Runtime** e controlla
che il JDK del progetto punti alla tua cartella `jdk-25`.

Clicca **Run** sopra `main` oppure esegui **Run Java** dal menu del file.
Verifica lo stesso messaggio e la stessa versione Java ottenuti da PowerShell.

Apri anche **Terminale → Nuovo terminale**, scegliendo **PowerShell** come profilo,
e ripeti:

```powershell
Set-Location "$env:USERPROFILE\socket\es02-ambiente-java"
javac -version
javac HelloSocket.java
java -cp . HelloSocket
```

Se compare una versione errata, applica la configurazione della sessione del
passo 4 anche in questo terminale. La [guida introduttiva ufficiale](https://code.visualstudio.com/docs/java/java-tutorial)
approfondisce esecuzione e debug in VS Code.

## Problemi frequenti

| Problema | Controllo da effettuare |
|---|---|
| `javac` non è riconosciuto | Verifica l'esistenza di `bin\javac.exe`, il Path utente e il riavvio completo del terminale e di VS Code |
| `java` funziona ma `javac` no | Potresti usare un runtime senza compilatore: controlla il JDK ZIP e l'output di `where.exe` |
| Compare una vecchia versione | Controlla `where.exe java` e `where.exe javac`; usa la configurazione della sessione del passo 4 |
| `Test-Path` restituisce `False` | Verifica di non avere una cartella del JDK annidata dentro `jdk-25` |
| VS Code non mostra Run o segnala JDK mancante | Controlla estensioni installate, percorsi nelle impostazioni utente e comando Java: Configure Java Runtime |
| `Could not find or load main class HelloSocket` | Compila il file, controlla la cartella corrente ed esegui `java -cp . HelloSocket` |
| `UnsupportedClassVersionError` | Il `java` usato è più vecchio del compilatore: seleziona il JDK 25 per entrambi e ricompila |
| Download, estensioni o eseguibili sono bloccati dalla scuola | Comunica al docente il messaggio visualizzato per verificare le autorizzazioni del laboratorio |

## Consegna e autovalutazione

Consegna al docente:

- Il sorgente `HelloSocket.java`.
- L'output di `java -version`, `javac -version`, `where.exe java` e `where.exe javac`.
- Una schermata del programma eseguito da PowerShell e una dell'esecuzione da VS Code.
- Il percorso del JDK e una schermata dell'Extension Pack for Java installato.

Se inserisci il sorgente nel repository **Socket-Programming** dell'esercizio 01,
trasferisci soltanto i file del progetto: il JDK rimane nella cartella `Tools`
del PC. Il `.gitignore` dell'esercizio 01 esclude già i file `.class`.

Rispondi:

1. A cosa servono il JDK e l'Extension Pack for Java?
2. Qual è la differenza tra `JAVA_HOME` e `Path`?
3. Perché questa procedura può essere svolta da un utente senza privilegi amministrativi?
4. Che cosa producono `javac HelloSocket.java` e `java -cp . HelloSocket`?
5. Perché installare l'estensione di VS Code non garantisce che `javac` funzioni nel terminale?
6. Quali modifiche rimangono nell'account e quali valgono soltanto nella PowerShell corrente?

---

[Esercizio precedente: Git e GitHub](01-Git_e_GitHub.md) · [Indice del capitolo](../README.md)
