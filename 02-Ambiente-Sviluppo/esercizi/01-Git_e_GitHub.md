# Esercizio a: Git e GitHub — Socket Programming

## Obiettivo

Creare un repository personale che contenga **l'intera cartella `socket`**, con
tutti i programmi, le sottocartelle e le risorse del corso di programmazione socket in Java.
L'unità di lavoro è l'intera raccolta di esercizi, non soltanto il file Hello World.

Su Web4Student la cartella di lavoro è **`~/socket` nella home dello studente**,
per esempio `/home/username/socket`. Il simbolo `~` indica la home dell'utente
sul computer in cui stai eseguendo il comando.

Successivamente dovrai **collegarti a GitHub sia dal PC di casa sia dal PC di
scuola**, clonare il repository creato e continuare il lavoro da entrambe le
postazioni. In questa esercitazione imparerai quindi anche a creare una copia
locale in ciascun PC e a scambiare gli aggiornamenti tramite `git pull` e
`git push`. Avrai tre copie di lavoro: una su Web4Student, una a casa e una a
scuola, collegate allo stesso repository GitHub.

Il nome suggerito è **Socket Programming**: nel campo del nome del
repository su GitHub usa **`Socket-Programming`**, con il trattino al
posto dello spazio. La cartella locale resterà `socket`: il nome locale e
quello del repository remoto possono essere diversi.

## Prima di iniziare: prepara il documento da consegnare

Crea un **documento Google** intitolato
**`Cognome Nome - Classe - Git e GitHub per socket`**. Sarà il progetto
da consegnare al docente al termine dell'esercitazione.

Inserisci subito nome, cognome, classe e data. Predisponi una sezione per ogni
passo, una sezione finale per le risposte di autovalutazione e uno spazio per
il collegamento al repository GitHub. **Raccogli gli screenshot mentre lavori**,
senza aspettare la fine: devono documentare i risultati ottenuti nei vari passi.

Per ogni screenshot indica il numero del passo, la postazione usata
(**Web4Student, PC di casa o PC di scuola**) e una breve didascalia che spieghi
il comando eseguito e il risultato. Le immagini devono mostrare testo leggibile
e l'esito dell'operazione, non soltanto il comando prima dell'esecuzione.
Non includere password, passphrase, token o il contenuto delle chiavi private.

Nella sezione [Consegna e autovalutazione](#consegna-e-autovalutazione) trovi
l'elenco delle prove da raccogliere e le domande a cui rispondere. Alla fine
consegnerai il **link al documento Google**, condiviso con il docente con
permesso di lettura, attraverso il canale indicato dal docente.

## Prerequisiti e risultato finale

Prepara il tuo account GitHub e le credenziali di accesso a Web4Student.
Tutti i comandi, dopo il collegamento SSH, si eseguono nel terminale
**Web4Student**, salvo diversa indicazione. Per questo esercizio servono Git,
un JDK 11 o successivo (`java` e `javac`) e un editor di testo, per esempio `nano`.

Dal Passo 11 userai invece il **terminale locale del PC di casa o di scuola**.
Prima di svolgere le operazioni locali, verifica i prerequisiti seguenti
**separatamente sul PC di casa e sul PC di scuola**.

### Prerequisiti sui PC di casa e di scuola

Se Git non è ancora disponibile, usa la [distribuzione ufficiale Git for Windows](https://git-scm.com/install/windows).
A casa puoi usare l'installer; a scuola, dove non sei amministratore, puoi
estrarre **Portable Git** in una cartella personale, per esempio
`%USERPROFILE%\Tools\PortableGit`, e avviare `git-bash.exe` dalla cartella
estratta. Se la scuola blocca l'esecuzione, rivolgiti al docente.
Su Linux o macOS usa Git già predisposto per la postazione.

Per Java sul PC completa l'[esercizio b: Java e VS Code senza amministratore](02-Java_e_VSCode_senza_amministratore.md)
prima dei Passi 11–14. I Passi 1–10 si svolgono invece sul server, con il JDK
lì disponibile. In questo esercizio su Windows usa **Git Bash**, anche se
nell'esercizio b usi PowerShell: aprilo nuovamente dopo aver configurato il JDK.

| Requisito | A cosa serve e cosa verificare |
|---|---|
| Account personale sul PC e cartella scrivibile | Devi poter salvare il clone in `~/socket` e le chiavi in `~/.ssh`, mantenendone l'accesso riservato al tuo account. A scuola verifica con il docente se i file vengono conservati tra le sessioni. |
| Account GitHub accessibile | Devi poter accedere allo stesso account usato per creare il repository, completare l'eventuale autenticazione a due fattori e registrare la chiave pubblica del PC di scuola. A casa riutilizzerai quella di Web4Student, già registrata. |
| Git | Deve essere installato e richiamabile dal terminale per clonare, creare commit, fare pull e push. |
| Client SSH, `ssh-keygen` e `scp` | Servono per verificare l'accesso a GitHub, generare la chiave del PC di scuola e trasferire da Web4Student al PC di casa la chiave già esistente. |
| Terminale adatto ai comandi | Su Windows usa Git Bash; su Linux o macOS usa il terminale. Aprilo localmente quando svolgi i Passi 11–14. |
| JDK 11 o successivo | Deve rendere disponibili `javac` per compilare `HelloSocket.java` e `java` per eseguire la classe `HelloSocket`. Per i PC Windows segui l'esercizio b e verifica i comandi anche da Git Bash. |
| Editor di testo | Serve per modificare `README.md`, i sorgenti e la configurazione SSH. Se `nano` non è disponibile, usa un editor locale mantenendo i nomi e le estensioni richiesti. |
| Strumento per acquisire screenshot | Devi poter catturare terminale e browser e inserire immagini leggibili nel documento Google. |

Dal terminale locale di **ciascun PC** controlla gli strumenti:

```bash
git --version
ssh -V
command -v ssh-keygen
command -v scp
java -version
javac -version
```

`command -v ssh-keygen`, eseguito in Git Bash o nel terminale Linux/macOS,
deve mostrare il percorso del comando. `java -version` e `javac -version`
devono indicare il JDK che intendi usare, con la stessa versione principale.
Anche `command -v scp` deve mostrare il percorso del programma di copia tramite SSH.
Inserisci uno screenshot di queste verifiche per ciascun PC nel progetto.

Se manca uno strumento, predisponilo prima di iniziare il lavoro locale.
Sul PC di scuola concorda con il docente eventuali installazioni, l'uso della
cartella personale e possibili limitazioni di rete; non modificare le
impostazioni di altri utenti. Il programma Java di questa esercitazione può
essere compilato ed eseguito sia su Windows sia su Linux: non serve WSL.
I comandi Linux per il monitoraggio della rete, come `ss`, andranno invece
eseguiti su Web4Student quando richiesti dagli esercizi successivi.

Prima del **clone**, devono inoltre essere completati la creazione del
repository GitHub e il primo push (Passi 7–8), oltre alla configurazione e
alla verifica dell'accesso SSH del PC corrente (Passo 11). Configurare Git e
SSH su Web4Student non configura automaticamente i computer locali.

### Risultato finale

La struttura da ottenere è:

```text
~/socket/
├── .git/
├── .gitignore
├── README.md
├── tmp/                    file temporanei, esclusi da Git
├── es01a-hello-world/
│   └── HelloSocket.java
└── ... tutte le altre cartelle e risorse degli esercizi sui socket
```

Il repository viene inizializzato direttamente in `~/socket`: su GitHub
comparirà il suo contenuto, senza un ulteriore livello `socket`.
I programmi si eseguono dal terminale; questa cartella non è l'area del sito web.

## Passo 1 — Collegarsi e verificare gli strumenti

Dal computer locale:

```bash
ssh -p 2222 username@w4s.filippobilardo.it
```

Nel terminale remoto:

```bash
git --version
java -version
javac -version
mkdir -p ~/socket
ls -la ~/socket
find ~/socket -name .git -print
```

`mkdir -p` crea la cartella se manca e conserva il contenuto se esiste già.
Il percorso guidato parte da una cartella che non contiene già repository Git.
Se l'ultimo comando elenca una `.git`, individua il repository esistente prima
di procedere. Se `~/socket` è già un repository, riutilizzalo; non creare
repository annidati nelle cartelle dei singoli esercizi.

## Passo 2 — Preparare tmp e il programma es01a-hello-world

Entra nella cartella `socket` del tuo account:

```bash
cd ~/socket
pwd
ls -la
```

Il percorso mostrato da `pwd` deve terminare con `/socket`.
Questa sarà la cartella di lavoro per gli esercizi del corso.

Crea la sottocartella per i file temporanei:

```bash
mkdir -p tmp
ls -ld tmp
```

Useremo `tmp` per dati di prova che non devono essere versionati. Al passo 5
creerai `.gitignore` per escluderne tutto il contenuto. Da ora lavora
direttamente sui sorgenti in `~/socket`.

### Creare il programma dell'esercizio a

Sempre da `~/socket`, crea la sottocartella e apri il nuovo file:

```bash
mkdir -p es01a-hello-world
nano es01a-hello-world/HelloSocket.java
```

Inserisci il codice seguente, sostituendo `Mario` con il tuo nome:

```java
public class HelloSocket {
    public static void main(String[] args) {
        String nome = "Mario";
        String messaggio = "Hello World dell'esercizio a.";

        System.out.println("Hello, World!");
        System.out.println("Studente: " + nome);
        System.out.println(messaggio);
        System.out.println("PID del processo: " + ProcessHandle.current().pid());
    }
}
```

Salva con `Ctrl+O`, conferma con `Invio` ed esci con `Ctrl+X`.
Compila il sorgente ed esegui la classe:

```bash
javac -encoding UTF-8 es01a-hello-world/HelloSocket.java
java -cp es01a-hello-world HelloSocket
```

**Verifica:** compaiono **Hello, World!**, il tuo nome, il messaggio
**Hello World dell'esercizio a.** e il PID del processo. Il nuovo programma
entrerà nel primo commit insieme a tutti gli altri sorgenti di `socket`.
`javac` produce `HelloSocket.class`, che escluderemo con `.gitignore`.
Il file deve chiamarsi esattamente `HelloSocket.java`, come la classe pubblica.
`java -cp es01a-hello-world HelloSocket` cerca la classe compilata nella
sottocartella indicata dal classpath.

## Passo 3 — Configurare l'identità Git

Sul proprio account Web4Student, configura nome ed email per i commit:

```bash
git config --global user.name "Nome Cognome"
git config --global user.email "tua.email@example.com"
git config --global init.defaultBranch main
git config --global --get user.name
git config --global --get user.email
```

Usa l'email associata all'account GitHub oppure il tuo indirizzo `noreply`
fornito dalle impostazioni GitHub. Questi dati identificano l'autore dei commit;
non sono credenziali di accesso. Sono impostazioni globali: valgono per tutti i repository Git sul server Web4Student.

## Passo 4 — Preparare l'autenticazione SSH da Web4Student verso GitHub

**Dove:** terminale remoto Web4Student, nel tuo account personale.
Questo passo configura la chiave `w4s_github_ed25519` usata dal server per
accedere a GitHub. Nell'esempio la chiave è senza passphrase: mantieni
l'accesso ai file riservato al tuo account.

Le tre procedure si riferiscono ad ambienti diversi:

| Ambiente | Procedura |
|---|---|
| Web4Student | Passo 4: prepara la chiave e registra la pubblica su GitHub. |
| PC di scuola | Passo 4 bis: prepara una chiave distinta, protetta da passphrase. |
| PC di casa | Passo 4 ter: riutilizza la chiave di Web4Student, senza generarne una nuova. |

Completa ora il Passo 4 e prosegui con il Passo 5 su Web4Student. Riprenderai
i Passi 4 bis e 4 ter quando configurerai i computer locali al Passo 11.

### Perché creare una chiave SSH?

Quando esegui `git push`, GitHub deve riconoscere il tuo account e verificare
che possa scrivere nel repository. Il nome e l'email configurati nel passo 3
identificano l'autore dei commit, ma **non permettono di accedere a GitHub**.

La chiave SSH serve ad autenticare la connessione tra Web4Student e GitHub.
Dopo la configurazione puoi usare comandi come `git push` e `git pull` con
l'URL SSH del repository, senza inserire ogni volta le credenziali GitHub.
`git commit`, invece, registra una versione locale e non richiede una connessione
a GitHub. Riferimento: [autenticazione SSH su GitHub](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/about-ssh).

### Una coppia di chiavi con due compiti diversi

`ssh-keygen` crea due file collegati tra loro:

| File | Dove rimane | A cosa serve |
|---|---|---|
| `w4s_github_ed25519` — chiave privata | Nella tua cartella `~/.ssh` su Web4Student; al Passo 4 ter ne copierai una versione sul tuo PC di casa | Permette al client SSH di dimostrare il possesso della chiave |
| `w4s_github_ed25519.pub` — chiave pubblica | Viene copiata nelle impostazioni del tuo account GitHub | Permette a GitHub di verificare l'autenticazione e associarla al tuo account |

La chiave privata non va incollata su GitHub né aggiunta al repository.
Registrare quella pubblica non concede nuovi permessi: puoi fare push soltanto
nei repository su cui il tuo account ha già il diritto di scrittura.

### Perché la chiave viene creata su Web4Student?

In questo esercizio i comandi Git vengono eseguiti sul server Web4Student:
è quindi quel server a dover presentare la tua chiave a GitHub. Una chiave
presente soltanto sul tuo computer non viene automaticamente usata dalla
sessione remota.

Le connessioni sono distinte:

| Collegamento | Account e porta |
|---|---|
| Computer → Web4Student | `username`, porta 2222 |
| Web4Student → GitHub | Utente SSH `git`, normalmente porta 22 |

Il primo accesso ti porta nel tuo account Web4Student; il secondo ti identifica
su GitHub tramite la chiave. Nell'indirizzo `git@github.com` l'utente SSH è
`git`: il tuo account GitHub viene riconosciuto dalla chiave registrata.

### Generare la coppia di chiavi

Prepara la cartella `~/.ssh`, se manca, e controlla le chiavi presenti:

```bash
mkdir -p ~/.ssh
chmod 700 ~/.ssh
ls -la ~/.ssh
```

Se non hai già una chiave adatta a GitHub, creala, il nome del file è libero:

```bash
ssh-keygen -t ed25519 -C "tua.email@example.com" -f ~/.ssh/w4s_github_ed25519 -N ""
```

| Opzione | Significato |
|---|---|
| `-t ed25519` | Sceglie l'algoritmo della coppia di chiavi |
| `-C "tua.email@example.com"` | Aggiunge un'etichetta per riconoscere la chiave; non esegue l'accesso con quell'email |
| `-f ~/.ssh/w4s_github_ed25519` | Sceglie il nome della chiave privata; la pubblica avrà lo stesso nome seguito da `.pub` |
| `-N ""` | Imposta una passphrase vuota, come previsto per il collegamento automatico di questo esercizio |

Non sovrascrivere una chiave già utilizzata. La
[guida GitHub alla generazione delle chiavi](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent)
approfondisce coppia di chiavi, nomi dei file e passphrase.

### Registrare la chiave pubblica su GitHub

Visualizza la chiave pubblica:

```bash
cat ~/.ssh/w4s_github_ed25519.pub
```

Il file con estensione **`.pub`** contiene la chiave pubblica da registrare su
GitHub. La chiave privata resta nel tuo ambiente.

**Copia l'intera chiave pubblica** (inizia con `ssh-ed25519...`) e aggiungila su GitHub:

1. Vai su [https://github.com/settings/keys](https://github.com/settings/keys)
2. Clicca su "New SSH key"
3. Dai un titolo (es: "Web4Student user")
4. Incolla la chiave nel campo "Key"
5. Clicca "Add SSH key"

### Indicare a SSH quale chiave usare

Nel terminale remoto apri `nano ~/.ssh/config` e inserisci questo blocco;
se esiste già un blocco per GitHub, integra quello esistente:

```sshconfig
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/w4s_github_ed25519
    IdentitiesOnly yes
```

Salva e verifica:

> SSH userà direttamente la chiave indicata da `IdentityFile`, senza chiedere
una passphrase e senza richiedere l'avvio di `ssh-agent` a ogni sessione.
Qui “automatico” riguarda l'autenticazione: per inviare le modifiche dovrai
comunque creare i commit ed eseguire `git push`.

```bash
chmod 600 ~/.ssh/config ~/.ssh/w4s_github_ed25519
ssh -T git@github.com
```

Alla prima connessione confronta l'impronta presentata con le
[impronte ufficiali GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/githubs-ssh-key-fingerprints).
Il messaggio di successo deve identificare il tuo account e indicare
l'autenticazione riuscita; GitHub non fornisce una shell interattiva. In questo
test il codice di uscita 1 può accompagnare l'autenticazione riuscita, come
spiegato nella [guida al test SSH](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/testing-your-ssh-connection).


Prosegui con il Passo 5 su Web4Student; le configurazioni dei PC serviranno al Passo 11.

## Passo 4 bis — Creare una chiave con passphrase per il PC della scuola

**Dove:** terminale locale del PC di scuola, nel tuo account personale.
Svolgi questa procedura quando il Passo 11 ti chiede di configurare la
postazione scolastica. Qui userai una chiave distinta da quella di Web4Student,
protetta da passphrase. La configurazione del server resta quella del Passo 4.

### Perché proteggere la chiave con una password?

La chiave privata permette di autenticarsi su GitHub. Se non ha una passphrase,
chi ne ottiene una copia può usarla senza dover sbloccare il file. La passphrase
cifra invece la chiave privata salvata: per utilizzarla serve anche conoscere
questa password. Scegli una frase lunga, difficile da indovinare e personale.

La passphrase non è la password di GitHub o di Web4Student: serve a sbloccare
**localmente il file della chiave privata**. Il nome del file e la chiave pubblica
possono essere conosciuti da altri; è la chiave privata che deve restare riservata.
Vedi la [guida GitHub alle passphrase SSH](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/working-with-ssh-key-passphrases).

### 1. Scegliere il terminale e un nome riconoscibile

Esegui i comandi nel terminale **locale del PC di scuola**, per esempio Git Bash
su Windows. Se sei collegato a Web4Student, usa `exit` per tornare al terminale
locale. La chiave resterà nella cartella `~/.ssh` del tuo account sul PC:
collegarsi al server dalla scuola non trasferisce automaticamente le chiavi.

Nel nome della nuova chiave inserisci **il tuo nome e cognome** e il suffisso
**`_pass`**. Nell'esempio usiamo `mario_rossi`: sostituiscilo in tutti i comandi
e nella configurazione con il tuo nome, senza spazi.

```bash
mkdir -p ~/.ssh
ls -la ~/.ssh
ssh-keygen -t ed25519 -C "Mario Rossi - PC scuola - tua.email@example.com" -f ~/.ssh/pc_github_mario_rossi_ed25519_pass
```

Personalizza anche il commento dopo `-C`. Non aggiungere `-N ""`: in questa
procedura devi impostare una passphrase non vuota. Se il nome del file è già
occupato, scegli un nome diverso senza sovrascrivere una chiave esistente.

### 2. Inserire e confermare la passphrase

Il programma chiede:

```text
Enter passphrase (empty for no passphrase):
Enter same passphrase again:
```

Digita la passphrase alla prima richiesta e ripetila alla seconda. I caratteri
digitati non vengono mostrati nel terminale. Non scriverla nel comando, nel
README o nel repository.

Otterrai due file:

| File | Uso |
|---|---|
| `~/.ssh/pc_github_mario_rossi_ed25519_pass` | Chiave privata cifrata, da mantenere nel proprio ambiente |
| `~/.ssh/pc_github_mario_rossi_ed25519_pass.pub` | Chiave pubblica da registrare su GitHub |

Il suffisso `_pass` aiuta a riconoscere la variante: la protezione dipende dalla
passphrase impostata, non dal nome scelto.

### 3. Registrare la nuova chiave pubblica su GitHub

```bash
cat ~/.ssh/pc_github_mario_rossi_ed25519_pass.pub
```

Copia l'intera chiave pubblica e, nel browser GitHub:

1. Apri **Settings → SSH and GPG keys → [New SSH key](https://github.com/settings/keys)**.
2. Scegli il tipo **Authentication Key**.
3. Usa un titolo riconoscibile, per esempio `Mario Rossi - PC scuola - pass`.
4. Incolla la chiave pubblica nel campo **Key** e conferma con **Add SSH key**.

Se hai creato questa nuova coppia, devi registrare la sua chiave pubblica:
quella della variante senza passphrase appartiene a una coppia diversa.

### 4. Configurare SSH per usare la chiave protetta

Apri `~/.ssh/config` con un editor: puoi usare `nano ~/.ssh/config` nel terminale
oppure Blocco note su Windows, salvando il file come `config`, senza `.txt`.
Inserisci il blocco seguente oppure sostituisci il precedente blocco
`Host github.com`, così la connessione usa la chiave protetta:

```sshconfig
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/pc_github_mario_rossi_ed25519_pass
    IdentitiesOnly yes
    IdentityAgent none
```

`IdentityFile` seleziona la chiave con il tuo nome e `_pass`. Per questa prova
su un PC condiviso, `IdentityAgent none` evita di usare chiavi già sbloccate
in un agente SSH: la passphrase verrà richiesta quando usi il file.
L'opzione è descritta nel [manuale OpenSSH](https://man.openbsd.org/ssh_config#IdentityAgent).

### 5. Verificare la connessione

Nel terminale dell'ambiente in cui hai configurato la chiave, esegui:

```bash
ssh -T git@github.com
```

Quando compare una richiesta simile alla seguente, digita la passphrase:

```text
Enter passphrase for key '.../pc_github_mario_rossi_ed25519_pass':
```

**Verifica:** dopo lo sblocco GitHub riconosce il tuo account. Anche `git push`
e `git pull` tramite SSH richiederanno lo sblocco della chiave con questa
configurazione. Torna al Passo 11 per completare la preparazione del PC di scuola.

La passphrase protegge il file salvato, non una sessione già accessibile ad
altre persone: al termine del lavoro chiudi le sessioni ed esci dal tuo account.

## Passo 4 ter — Riutilizzare sul PC di casa la chiave di Web4Student

**Dove:** terminale locale del PC di casa, nel tuo account personale protetto.
Svolgi questa procedura quando arrivi alla configurazione locale del Passo 11.
Devi aver completato il Passo 4 e poter accedere al tuo account Web4Student.

**Non serve generare una nuova chiave:** puoi utilizzare la stessa
`w4s_github_ed25519` già registrata su GitHub. Se è presente soltanto sul server,
devi prima copiarla sul PC di casa: indicarne il nome nel file `config` non
consente al client locale di leggere automaticamente il file remoto.

### 1. Preparare la cartella locale e controllare le chiavi

Apri Git Bash su Windows o il terminale su Linux/macOS. Se sei nella sessione
SSH Web4Student, torna al terminale locale con `exit`.

```bash
mkdir -p ~/.ssh
ls -la ~/.ssh
```

Se i file `w4s_github_ed25519` e `w4s_github_ed25519.pub` sono già la copia della
chiave Web4Student, passa direttamente alla configurazione. Non sovrascrivere
chiavi diverse con lo stesso nome: in quel caso scegli nomi locali distinti
per la copia e adatta anche `IdentityFile`.

### 2. Copiare la chiave da Web4Student al PC di casa

Esegui dal **PC di casa** i comandi seguenti, sostituendo `username` con il tuo
utente Web4Student. Sono previsti quando i file di destinazione non esistono:

```bash
scp -P 2222 username@w4s.filippobilardo.it:.ssh/w4s_github_ed25519 ~/.ssh/w4s_github_ed25519
scp -P 2222 username@w4s.filippobilardo.it:.ssh/w4s_github_ed25519.pub ~/.ssh/w4s_github_ed25519.pub
```

`scp` trasferisce i file attraverso SSH. L'opzione **`-P` maiuscola** indica
la porta 2222; il percorso dopo i due punti è relativo alla cartella personale
sul server. La destinazione `~/.ssh/…` è invece sul PC di casa.
Se viene richiesta una password per il collegamento, è quella di Web4Student.

Occorre la **chiave privata** per autenticarsi; copiare soltanto il file `.pub`
non basta. Trasferiscila direttamente con `scp`, senza incollarne il contenuto
in chat, email, repository o documento Google. La copia deve restare nel tuo
account personale; per il PC di scuola usa la chiave separata del Passo 4 bis.

La chiave pubblica è già registrata sul tuo account GitHub: **non devi
aggiungerla di nuovo**. Web4Student e PC di casa useranno la stessa identità
SSH; revocare quella chiave su GitHub ne impedirà l'uso da entrambi.

### 3. Configurare il client SSH locale

Apri `~/.ssh/config` sul PC di casa con un editor. Inserisci questo blocco
oppure integra quello esistente per `github.com`, senza duplicarlo:

```sshconfig
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/w4s_github_ed25519
    IdentitiesOnly yes
```

Salva il file con nome `config`, senza estensione `.txt`. Nel terminale:

```bash
chmod 700 ~/.ssh
chmod 600 ~/.ssh/config ~/.ssh/w4s_github_ed25519
```

Su Windows usa la cartella personale del tuo account; se SSH segnala permessi
troppo aperti, verifica anche i permessi del file nelle proprietà di sicurezza
di Windows. La chiave copiata conserva la passphrase della chiave originale:
se hai seguito il Passo 4 con `-N ""`, non ne verrà richiesta una.

### 4. Verificare l'accesso dal PC di casa

```bash
ssh -T git@github.com
```

Al primo collegamento verifica l'impronta di GitHub come indicato nel Passo 4.
**Verifica:** il messaggio riconosce lo stesso account GitHub usato da
Web4Student. Inserisci nella relazione lo screenshot di questo risultato,
senza mostrare il contenuto della chiave privata. Torna al Passo 11 e prosegui
poi con il clone sul PC di casa al Passo 12.

## Passo 5 — Documentare il progetto e scegliere i file da tracciare

Torna alla cartella `socket`, che sarà la radice del repository:

```bash
cd ~/socket
nano README.md
```

Scrivi almeno:

```markdown
# Socket Programming

Esercitazioni di programmazione socket in Java di Nome Cognome, classe ...

Questo repository contiene i programmi, gli esercizi e le risorse sui socket in Java.
Il programma dell'esercizio Git e GitHub è es01a-hello-world/HelloSocket.java.

Su Web4Student lavoro nella cartella personale ~/socket.
Per compilare Hello World uso: javac -encoding UTF-8 es01a-hello-world/HelloSocket.java
Per eseguirlo uso: java -cp es01a-hello-world HelloSocket
La sottocartella tmp contiene file temporanei e viene esclusa da Git.
I sorgenti .java sono versionati; i file compilati .class vengono rigenerati.
```

Crea il file `.gitignore` nella stessa cartella:

```bash
nano .gitignore
```

Inserisci queste regole; se il file esiste già, integrale con quelle presenti:

```gitignore
/tmp/
*.class
/out/
/build/
/target/
*.log
*.tmp
*.swp
*~
.env
.env.*
!.env.example
```

La regola `/tmp/` esclude la cartella temporanea nella radice del repository
e tutto il suo contenuto. Le altre regole escludono file Java compilati,
cartelle di build, temporanei e file di ambiente con credenziali reali.
Gli altri sorgenti di `socket`, incluse eventuali sottocartelle e risorse,
devono entrare nel repository. `.gitignore` non rimuove file già tracciati.

Per verificare l'esclusione, crea un file di prova nella cartella `tmp`:

```bash
touch tmp/prova.txt
```

Lo controllerai dopo l'inizializzazione del repository.

## Passo 6 — Creare il repository locale e il primo commit

Esegui questi comandi **dentro `~/socket`**, non nella sottocartella del singolo
esercizio.

Se `~/socket` è già un repository, riutilizzalo senza ripetere `git init`;
controlla anche il branch corrente con `git branch --show-current`.
Gli esempi usano `main`: se il repository esistente usa un altro branch,
adatta i comandi senza rinominarlo automaticamente.

```bash
cd ~/socket
git init -b main                   # inizializza il repository con branch main
git rev-parse --show-toplevel      # mostra la radice del repository
git check-ignore -v tmp/prova.txt  # verifica l'esclusione del file temporaneo
git add .                         # aggiunge tutti i file non ignorati
git status                        # mostra i file preparati per il commit
git diff --cached --stat           # mostra le modifiche preparate
git diff --cached                  # mostra il contenuto delle modifiche preparate
```

**Verifica:** la radice termina con `/socket`; `git check-ignore` indica la regola
`/tmp/`. Nell'elenco dei file compaiono tutti i sorgenti e le risorse previste,
non soltanto Hello World, mentre `tmp/prova.txt` e i file `.class` non compaiono
tra i file preparati per il commit. Il file `.gitignore` deve invece essere incluso.

Se l'elenco è corretto:

```bash
git commit -m "Importa l'intera cartella socket del corso di programmazione socket in Java"
git log --oneline
git ls-files
```

L'ultimo comando elenca i file effettivamente tracciati. La cartella
`tmp` resta disponibile sul server ma, essendo ignorata, non viene trasferita
con un clone: potrai ricrearla con `mkdir -p tmp`.
Git registra file, non cartelle vuote. Vedi i manuali di
[git init](https://git-scm.com/docs/git-init) e [git add](https://git-scm.com/docs/git-add).

## Passo 7 — Creare il repository vuoto su GitHub

**Dove:** browser, nel tuo account GitHub.

1. Crea un nuovo repository: il nome suggerito è **Socket Programming**, da scrivere come **`Socket-Programming`** nel campo **Repository name**.
2. Scegli la visibilità secondo le indicazioni del corso.
3. Non aggiungere da GitHub README, `.gitignore` o licenza: il repository remoto deve partire vuoto.
4. Copia l'URL SSH, nella forma `git@github.com:TUO-ACCOUNT/Socket-Programming.git`.

`TUO-ACCOUNT` è l'account GitHub, che può essere diverso dallo username Web4Student.
Se scegli un nome diverso, usa il corrispondente URL nei comandi successivi.
Questa procedura segue la [guida GitHub all'importazione di codice locale](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github).

## Passo 8 — Collegare il remoto e fare push

Nel terminale Web4Student, dalla cartella `~/socket`:

```bash
cd ~/socket
git remote add origin git@github.com:TUO-ACCOUNT/Socket-Programming.git
git remote -v
git push -u origin main
```

Se `origin` è già configurato, controllalo con `git remote -v` prima di
ripetere `git remote add origin`: riutilizza il collegamento al tuo repository.

Aggiorna la pagina GitHub del repository. Devi vedere `README.md`, `.gitignore`
e **tutto il contenuto versionato di `socket`**, con `es01a-hello-world`
direttamente alla radice. Apri `es01a-hello-world/HelloSocket.java`
e confrontalo con il tuo file.
La cartella `tmp` e i suoi file non devono comparire.

`git push` invia i commit: non invia automaticamente modifiche non ancora
registrate. `java -cp es01a-hello-world HelloSocket` esegue invece la classe
compilata presente in `~/socket`, indipendentemente dal push. Dopo aver salvato
una modifica al sorgente, ricompila con `javac` per eseguirne la nuova versione.


## Passo 9 — Registrare una modifica

Modifica direttamente il programma, sempre da `~/socket`:

```bash
nano es01a-hello-world/HelloSocket.java
```

Cambia il valore del messaggio in:

```java
String messaggio = "Hello World sotto controllo versione con Git.";
```

Poi controlla, registra e invia:

```bash
javac -encoding UTF-8 es01a-hello-world/HelloSocket.java
git diff -- es01a-hello-world/HelloSocket.java
git add .
git commit -m "Aggiorna il messaggio di Hello World"
git push
git log --oneline -2
```

**Verifica:** se sei partito da un repository nuovo, GitHub mostra due commit
e il nuovo messaggio nel sorgente.


## Passo 10 — Verificare il programma e i file esclusi

Il file modificato si trova già nella cartella di lavoro. Non devi copiarlo
in un'altra directory. Esegui nuovamente:

```bash
cd ~/socket
javac -encoding UTF-8 es01a-hello-world/HelloSocket.java
java -cp es01a-hello-world HelloSocket
```

Ora deve comparire il nuovo messaggio.

Controlla anche lo stato del repository e l'esclusione del file temporaneo:

```bash
git status
git check-ignore -v tmp/prova.txt
git ls-files -- tmp
```

L'ultimo comando non deve elencare file. `.gitignore` esclude `tmp` dal controllo
di versione, ma non protegge i file sul server: non usarla per conservare
credenziali o dati riservati.

## Passo 11 — Preparare l'accesso a GitHub dai due PC

**Dove:** prima sul PC di casa, poi sul PC di scuola, nel tuo account personale.
Ripeti la preparazione su ciascuna postazione.

Prima di proseguire, completa le verifiche della sezione
[Prerequisiti sui PC di casa e di scuola](#prerequisiti-sui-pc-di-casa-e-di-scuola)
e documentale nel documento Google. Il repository su GitHub deve già contenere
i commit creati su Web4Student.

Apri un nuovo terminale **locale**. Se sei ancora nella sessione SSH di
Web4Student, usa `exit` per tornare al terminale del PC. I comandi di questo
passo devono operare sui file del computer che stai usando.

Verifica Git e configura la tua identità:

```bash
git --version
git config --global user.name "Nome Cognome"
git config --global user.email "tua.email@example.com"
```

Usa la stessa identità del Passo 3. Le impostazioni globali valgono per l'utente
del computer corrente: quelle configurate su Web4Student non vengono trasferite
automaticamente al PC.

Prepara la cartella delle chiavi, se non esiste:

```bash
mkdir -p ~/.ssh
ls -la ~/.ssh
```

Configura poi l'autenticazione seguendo le procedure già illustrate:

- **PC di casa:** segui il Passo 4 ter per riutilizzare la chiave Web4Student;
  non occorre generare una nuova coppia o registrare nuovamente la pubblica.
- **PC di scuola:** segui il Passo 4 bis, usando una chiave con il tuo nome,
  il suffisso `_pass` e una passphrase personale.

Esegui quelle procedure nel terminale locale. La chiave di Web4Student,
riutilizzata a casa, e la chiave distinta del PC di scuola devono essere
associate allo **stesso account GitHub**. Usa titoli riconoscibili nelle
impostazioni GitHub, per esempio `Nome Cognome - Web4Student e casa` e
`Nome Cognome - PC scuola`. Se una chiave adatta è già configurata nella
postazione, verifica l'accesso senza rigenerarla o sovrascriverla.

Verifica l'accesso da ciascun PC:

```bash
ssh -T git@github.com
```

**Verifica:** il messaggio identifica il tuo account GitHub. L'accesso SSH a
Web4Student e l'autenticazione SSH verso GitHub sono due collegamenti distinti:
per clonare sul PC userai direttamente GitHub.

## Passo 12 — Clonare il repository sul PC di casa

**Dove:** terminale locale del PC di casa, dopo la preparazione del Passo 11.

Dal browser apri il repository creato al Passo 7, scegli **Code → SSH** e copia
il suo URL. Sostituisci `TUO-ACCOUNT` nei comandi con il tuo account GitHub e,
se hai scelto un nome diverso, adatta anche il nome del repository.

Spostati nella tua cartella personale e clona:

```bash
cd ~
git clone git@github.com:TUO-ACCOUNT/Socket-Programming.git socket
cd ~/socket
git remote -v
git status
git log --oneline -2
```

Il comando `git clone` scarica i file versionati e la cronologia, crea la
cartella locale **`socket`** e configura automaticamente il remoto `origin`.
L'ultimo argomento sceglie il nome della cartella: senza `socket`, Git userebbe
il nome `Socket-Programming`.

Non eseguire `git init` o `git remote add origin` dopo il clone: il repository
locale e il collegamento al remoto sono già pronti. Esegui il clone una sola
volta per postazione. Se `~/socket` esiste già ed è una cartella non vuota,
controllane il contenuto prima di procedere: se è già la copia di questo
repository, riutilizzala e aggiornala con il Passo 14; altrimenti scegli una
cartella di destinazione diversa, senza cancellare i file presenti.

Ricrea la cartella temporanea, compila ed esegui l’esempio:

```bash
mkdir -p tmp
javac -encoding UTF-8 es01a-hello-world/HelloSocket.java
java -cp es01a-hello-world HelloSocket
```

Il clone contiene il sorgente `.java`, mentre il file `.class` viene
rigenerato con `javac` nella postazione corrente. Se `java` o `javac` non sono
riconosciuti, torna alle verifiche del JDK dell’esercizio b e riapri Git Bash.

**Verifica:** `origin` punta al tuo repository GitHub, la cronologia contiene
i commit già inviati e Hello World mostra il messaggio aggiornato. `tmp` non
era presente nel clone perché è esclusa da Git.

## Passo 13 — Clonare il repository sul PC di scuola

**Dove:** terminale locale del PC di scuola, nel tuo account personale e dopo
aver configurato la chiave con passphrase al Passo 11.

Clona lo **stesso repository GitHub** usato a casa:

```bash
cd ~
git clone git@github.com:TUO-ACCOUNT/Socket-Programming.git socket
cd ~/socket
git remote -v
git status
git log --oneline -2
mkdir -p tmp
```

Quando SSH richiede la passphrase della chiave locale, inseriscila nel terminale.
Anche qui, se `~/socket` contiene già una copia del repository, passa
all'aggiornamento del Passo 14 senza ripetere il clone.

Apri `es01a-hello-world/HelloSocket.java` e verifica che contenga il messaggio inviato
su GitHub. Compilalo ed eseguilo anche dal PC di scuola:

```bash
javac -encoding UTF-8 es01a-hello-world/HelloSocket.java
java -cp es01a-hello-world HelloSocket
```

**Verifica:** ora hai una copia su ciascuna postazione. Ogni copia possiede la
propria cartella `.git`, ma tutte usano lo stesso remoto GitHub. Le modifiche
salvate su un PC non compaiono automaticamente sugli altri.

## Passo 14 — Continuare il lavoro tra casa, scuola e Web4Student

**All'inizio di ogni sessione**, dalla copia su cui stai per lavorare:

```bash
cd ~/socket
git status
git pull --ff-only
```

Prima del pull verifica che non ci siano modifiche locali da completare e
registrare. `git pull --ff-only` scarica e applica gli aggiornamenti quando
la cronologia locale può avanzare senza creare un commit di merge. Se segnala
storie divergenti, confronta le modifiche prima di integrarle.

Per provare il passaggio tra postazioni, **dal PC di casa** apri `README.md`
con il tuo editor e aggiungi una riga, per esempio:

```text
Ho clonato il repository e verificato il programma sul PC di casa.
```

Controlla, registra e invia la modifica:

```bash
git diff -- README.md
git add README.md
git commit -m "Documenta la verifica dal PC di casa"
git push
```

**Dal PC di scuola**, entra nella sua copia e ricevi l'aggiornamento:

```bash
cd ~/socket
git status
git pull --ff-only
git log --oneline -3
```

Apri `README.md`: deve comparire la riga aggiunta a casa. Aggiungi una seconda
riga che documenti la verifica dal PC di scuola, poi esegui di nuovo
`git diff -- README.md`, `git add README.md`, un commit con un messaggio
appropriato e `git push`.

Alla successiva sessione sul PC di casa o su Web4Student, esegui il pull nella
rispettiva cartella `~/socket` e verifica la presenza di entrambe le righe.
Per aggiornare la copia Web4Student devi prima collegarti al server tramite SSH,
come nel Passo 1.

**Alla fine di ogni sessione**, controlla le modifiche, crea i commit del lavoro
completato e fai push prima di cambiare postazione. Il solo salvataggio o il
solo commit non trasferiscono il lavoro su GitHub. Al termine del lavoro a
scuola chiudi le sessioni ed esci dal tuo account.

## Comandi da usare durante il lavoro

| Azione | Comando da `~/socket` |
|---|---|
| Controllare lo stato | `git status` |
| Leggere modifiche non ancora preparate | `git diff` |
| Preparare modifiche all'intera raccolta di esercizi | `git add .` |
| Registrare una versione | `git commit -m "Descrizione della modifica"` |
| Inviare i commit | `git push` |
| Leggere la cronologia | `git log --oneline` |
| Ricevere aggiornamenti senza merge automatici | `git pull --ff-only` con la cartella di lavoro pulita, prima di iniziare nuove modifiche |

Se un push viene rifiutato perché il remoto è avanzato, controlla lo stato
e ricevi gli aggiornamenti. Se `--ff-only` segnala storie divergenti, confronta
le modifiche prima di integrare: non serve un push forzato per questo esercizio.

## Consegna e autovalutazione

### Documento Google da consegnare

Completa il **documento Google creato all'inizio**, intitolato
**`Cognome Nome - Classe - Git e GitHub per socket`**. Organizzalo così:

1. **Dati dello studente:** nome, cognome, classe, data e sistemi operativi usati a casa e a scuola.
2. **Repository:** URL GitHub e identificativi dei primi due commit e dei commit di verifica da casa e da scuola.
3. **Svolgimento:** sezioni numerate per i passi, con screenshot dei risultati e didascalie. Distingui sempre la postazione su cui hai lavorato.
4. **Problemi incontrati:** descrivi eventuali errori, la causa individuata e come li hai risolti; se non ne hai incontrati, dichiaralo.
5. **Autovalutazione:** risposte personali alle domande riportate sotto, con esempi tratti dal lavoro svolto.

### Screenshot e prove del completamento

Usa la tabella come elenco di controllo. Puoi raccogliere più risultati in una
sola immagine se restano leggibili; usa più immagini quando serve. Documenta
il Passo 4 su Web4Student, il Passo 4 bis sul PC di scuola e il Passo 4 ter
sul PC di casa. Se riutilizzi chiavi già configurate, mostra l'esito della
verifica senza rigenerarle soltanto per produrre gli screenshot.

| Passo | Cosa mostrare nel documento Google |
|---|---|
| Prerequisiti locali | Verifica di Git, SSH, `ssh-keygen`, `scp`, Java e javac dal PC di casa e dal PC di scuola, indicando il sistema operativo di ciascuno. |
| 1 — Collegamento e strumenti | Terminale Web4Student con versioni di Git, Java e javac ed elenco della cartella `~/socket`. |
| 2 — Primo programma | Compilazione di `HelloSocket.java` ed esecuzione della classe `HelloSocket`, con saluto, nome, messaggio e PID visibili. |
| 3 — Identità Git | Risultato di `git config --global --get user.name` e `git config --global --get user.email`. |
| 4 — Autenticazione da Web4Student | Esito di `ssh -T git@github.com` sul server che riconosce il tuo account. |
| 4 bis — Chiave del PC di scuola | Configurazione `IdentityFile` con il nome della chiave protetta ed esito del test SSH locale; non mostrare la passphrase o il contenuto privato. |
| 4 ter — Riutilizzo della chiave a casa | Configurazione locale `IdentityFile` ed esito del test SSH dal PC di casa. Spiega che usi la chiave già registrata per Web4Student; non mostrare il contenuto privato. |
| 5 — Documentazione ed esclusioni | Contenuto del README e delle regole di `.gitignore`, con l'esclusione di `/tmp/` visibile. |
| 6 — Primo commit | Radice del repository, primo commit nella cronologia e risultato di `git check-ignore -v tmp/prova.txt`. |
| 7 — Repository GitHub | Pagina del repository creato con nome e account proprietario riconoscibili. |
| 8 — Primo push | Esito del push e pagina GitHub con README, `.gitignore` e sorgenti caricati. |
| 9 — Modifica versionata | Differenza del messaggio, secondo commit e conferma del push. |
| 10 — Verifica finale sul server | Nuovo output di Hello World, `git status`, verifica dell'esclusione e `git ls-files -- tmp` senza file elencati. |
| 11 — Accesso dai due PC | Esito dell'autenticazione SSH a GitHub da casa e da scuola, con didascalie distinte. |
| 12 — Clone a casa | Esito del clone, percorso locale, `git remote -v`, cronologia ed esecuzione di Hello World sul PC di casa. |
| 13 — Clone a scuola | Esito del clone, percorso locale, `git remote -v`, cronologia ed esecuzione di Hello World sul PC di scuola. |
| 14 — Sincronizzazione | Commit e push delle modifiche al README dalle due postazioni; pull e README aggiornato sulla postazione ricevente. Dopo la sincronizzazione, mostra `git remote -v` e `git log --oneline -4` da ciascun PC e documenta l'aggiornamento anche su Web4Student. |

Se riutilizzi un repository o un clone già esistente, scrivilo nella didascalia
e mostra le verifiche e l'aggiornamento eseguiti, senza ricrearlo. Se un passo
non è stato completato, indicalo esplicitamente con il motivo: una schermata
di errore documenta il tentativo, non il completamento.

Prima della consegna, verifica che il repository contenga anche gli altri
sorgenti e le risorse presenti in `socket`. Controlla che le immagini siano
leggibili e accompagnate da spiegazioni, che gli URL siano cliccabili e che
non compaiano credenziali o chiavi private.

**Consegna il link al documento Google** tramite il canale indicato dal docente.
Condividilo con l'account del docente con permesso di lettura e verifica che
sia abilitato ad aprirlo; non è necessario renderlo pubblico. Se il repository
GitHub è privato, assicurati anche che il docente possa accedervi secondo le
indicazioni del corso.

### Spiegazioni e autovalutazione

Rispondi nel documento Google con parole tue. Collega le spiegazioni ai comandi,
ai risultati e agli screenshot della tua esercitazione:

1. Quale cartella costituisce la radice del repository e dove vengono modificati i programmi?
2. Che cosa cambia tra salvare un file, fare commit e fare push?
3. Perché il programma ricompilato ed eseguito da `~/socket` mostra una modifica salvata anche prima del push? Che cosa succede se salvi il sorgente ma non lo ricompili?
4. Quale file della coppia SSH viene registrato su GitHub?
5. Quale regola esclude `tmp` e come puoi verificarne l'effetto?
6. Che cosa crea `git clone` e perché non devi eseguire nuovamente `git init`?
7. Perché ogni postazione deve avere una propria configurazione di accesso a GitHub?
8. Quali comandi usi per trasferire il lavoro dal PC di casa a quello di scuola?
9. Qual è la differenza tra il terminale locale del PC e la sessione SSH Web4Student? Dove vengono creati i file quando lavori nei due ambienti?
10. Che cosa rappresentano `origin` e `main`? Quale comando mostra l'indirizzo del repository remoto?
11. Perché la cartella locale può chiamarsi `socket` mentre il repository su GitHub si chiama `Socket-Programming`? Quale argomento di `git clone` sceglie il nome locale?
12. A cosa serve la passphrase della chiave privata e in che cosa differisce dalla password di GitHub? Perché è utile sulla postazione scolastica?
13. Perché `tmp` e i file `.class` non vengono scaricati con il clone? Che cosa succede se aggiungi a `.gitignore` un file già tracciato?
14. Che cosa controlli prima di eseguire `git pull --ff-only` e perché conviene aggiornare la copia locale prima di iniziare nuove modifiche?
15. Se fai commit sul PC di casa ma dimentichi il push, che cosa troverai sul PC di scuola dopo un pull? Come recuperi il lavoro?
16. Quali risultati dimostrano che le tre copie sono aggiornate agli stessi commit? Spiega perché vedere lo stesso nome di file non basta a verificarlo.
17. Perché a casa puoi riutilizzare la chiave Web4Student senza registrarla di nuovo su GitHub? Perché copiare soltanto il file `.pub` non basta e cosa succede a entrambi gli accessi se revochi la chiave condivisa?

---

[Indice del capitolo](../README.md) · [Esercizio successivo: Java e VS Code](02-Java_e_VSCode_senza_amministratore.md)
