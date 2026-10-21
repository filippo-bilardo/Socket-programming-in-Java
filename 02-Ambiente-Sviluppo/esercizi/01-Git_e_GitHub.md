# Esercizio a: Git e GitHub — Socket Programming

## Obiettivo

Crea la cartella **`socket` nella tua home su Web4Student** e un repository
personale su GitHub per il progetto **Socket Programming**. Userai questa cartella
per raccogliere e versionare gli esercizi del corso di programmazione socket.

Lavorerai in **`~/socket`**, dove `~` indica la home del tuo account studente.
Il repository comprenderà il contenuto di questa cartella.

Su GitHub usa il nome **`Socket-Programming`** e scrivi **Socket Programming**
come titolo del README. Il trattino sostituisce lo spazio, che non è ammesso
nei nomi dei repository. Vedi le [regole GitHub per la creazione dei repository](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-new-repository).

## Prerequisiti e risultato finale

Devi avere un account Web4Student con accesso SSH e un account GitHub personale.
Dopo il collegamento SSH, esegui tutti i comandi nel terminale **Web4Student**,
salvo diversa indicazione. Sostituisci `username`, `TUO-ACCOUNT`, nome, classe ed
email con i tuoi dati: gli account Web4Student e GitHub possono avere nomi diversi.

La struttura da ottenere è:

```text
~/
└── socket/
    ├── .git/
    ├── .gitignore
    ├── README.md
    └── tmp/             file temporanei, esclusi da Git
```

## Passo 1 — Collegarsi a Web4Student

Dal terminale del computer locale:

```bash
ssh -p 2222 username@w4s.filippobilardo.it
```

Nel terminale remoto verifica Git e la tua home:

```bash
git --version
ls -ld ~
git -C ~ rev-parse --show-toplevel
```

Se l'ultimo comando segnala che la cartella non appartiene a un repository,
puoi procedere. Se mostra una radice, la tua home appartiene già a un repository:
concorda con il docente la separazione dei due progetti
prima di inizializzare `socket`: non creare accidentalmente repository annidati
e non cancellare la `.git` esistente.

## Passo 2 — Creare la cartella socket

```bash
mkdir -p ~/socket
cd ~/socket
pwd
mkdir -p tmp
```

**Verifica:** il percorso mostrato da `pwd` corrisponde alla tua home seguita da
`/socket`, ad esempio `/home/username/socket`.
Se la cartella contiene già file, conservali e integra quelli previsti dall'esercizio.

Questa cartella raccoglierà sorgenti, consegne e appunti. I programmi Java
verranno compilati ed eseguiti dal terminale.

## Passo 3 — Configurare l'identità Git

```bash
git config --global user.name "Nome Cognome"
git config --global user.email "tua.email@example.com"
git config --global --get user.name
git config --global --get user.email
```

Usa l'email associata a GitHub oppure il tuo indirizzo `noreply` disponibile nelle
impostazioni GitHub. Nome ed email identificano l'autore dei commit; l'accesso
a GitHub verrà configurato nel passo successivo.

## Passo 4 — Preparare l'autenticazione SSH verso GitHub

Il collegamento computer → Web4Student e quello Web4Student → GitHub sono
distinti. Poiché esegui `git push` su Web4Student, la chiave per GitHub deve
essere disponibile nel tuo account sul server.

Se hai già configurato l'accesso a GitHub da Web4Student, riutilizzalo e passa
alla verifica `ssh -T git@github.com`. Altrimenti controlla prima le chiavi presenti:

```bash
mkdir -p ~/.ssh
chmod 700 ~/.ssh
ls -la ~/.ssh
```

Se il nome `w4s_github_ed25519` è libero, genera una nuova coppia:

```bash
ssh-keygen -t ed25519 -C "tua.email@example.com" -f ~/.ssh/w4s_github_ed25519
```

Scegli una passphrase quando richiesta; SSH potrà richiederla durante il push.
Non sovrascrivere una chiave esistente. Puoi approfondire nella
[guida GitHub alla generazione delle chiavi SSH](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent).

Visualizza la **chiave pubblica**:

```bash
cat ~/.ssh/w4s_github_ed25519.pub
```

Copia l'intera riga e, nel browser, apri le [impostazioni delle chiavi SSH di GitHub](https://github.com/settings/keys).
Seleziona **New SSH key**, assegna un titolo come `Web4Student`, scegli il tipo
**Authentication Key** e incolla la chiave pubblica. Il file senza `.pub` è la
chiave privata: deve restare in `~/.ssh`, fuori dal repository.
Riferimento: [aggiungere una chiave al proprio account GitHub](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account).

Apri `nano ~/.ssh/config` e aggiungi questo blocco; se ne esiste già uno per
GitHub, integra quello esistente indicando la chiave che hai scelto:

```sshconfig
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/w4s_github_ed25519
    IdentitiesOnly yes
```

Salva e verifica:

```bash
chmod 600 ~/.ssh/config ~/.ssh/w4s_github_ed25519
ssh -T git@github.com
```

Al primo collegamento confronta l'impronta mostrata con le
[impronte ufficiali GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/githubs-ssh-key-fingerprints).
Il messaggio finale deve riconoscere il tuo account e confermare l'autenticazione.
GitHub non offre una shell interattiva: in questo test il codice di uscita `1`
è previsto anche in caso di successo. Vedi la [guida al test SSH](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/testing-your-ssh-connection).

## Passo 5 — Scrivere README e .gitignore

```bash
cd ~/socket
nano README.md
```

Inserisci e personalizza:

```markdown
# Socket Programming

Esercizi di programmazione socket in Java di Nome Cognome, classe 5A.

Lavoro su Web4Student nella cartella ~/socket, nella mia home personale.

Il repository raccoglierà sorgenti, consegne e appunti del corso.
La cartella tmp contiene file temporanei esclusi dal controllo di versione.
```

In `nano`, salva con `Ctrl+O`, conferma con `Invio` ed esci con `Ctrl+X`.

Crea `.gitignore` nella stessa cartella, integrando eventuali regole già presenti:

```bash
nano .gitignore
```

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

I sorgenti `.java` devono essere versionati; i file compilati `.class` e i
temporanei possono essere rigenerati. `.gitignore` non rimuove file già tracciati.

## Passo 6 — Inizializzare Git e creare il primo commit

Esegui i comandi dalla radice `~/socket`. Se questa cartella è già un
repository, riutilizzalo senza ripetere `git init`.

```bash
cd ~/socket
git init -b main
git rev-parse --show-toplevel
touch tmp/prova.txt
git check-ignore -v tmp/prova.txt
git add .
git status
git diff --cached
```

**Verifica:** la radice corrisponde a `~/socket`; tra i file preparati compaiono
`README.md` e `.gitignore`, mentre `tmp/prova.txt` è escluso.
Quando l'elenco è corretto:

```bash
git commit -m "Crea il progetto Socket Programming"
git log --oneline
git ls-files
```

## Passo 7 — Creare il repository Socket Programming su GitHub

Nel browser, accedi al tuo account GitHub e crea un nuovo repository:

1. Imposta **Repository name** a `Socket-Programming`.
2. Inserisci come descrizione `Socket Programming - Esercizi di programmazione socket in Java`.
3. Scegli la visibilità indicata dal docente.
4. Lascia disattivate le opzioni per creare README, `.gitignore` e licenza: il remoto deve partire vuoto.
5. Crea il repository e copia il suo URL **SSH**.

L'URL avrà questa forma:

```text
git@github.com:TUO-ACCOUNT/Socket-Programming.git
```

Il README già creato mostrerà il titolo **Socket Programming**. La procedura
segue la [guida GitHub per pubblicare codice locale](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github).

## Passo 8 — Collegare il remoto e fare push

Nel terminale Web4Student:

```bash
cd ~/socket
git remote add origin git@github.com:TUO-ACCOUNT/Socket-Programming.git
git remote -v
git push -u origin main
```

Se esiste già un remoto `origin`, controllalo con `git remote -v` prima di
aggiungerlo o modificarlo. Aggiorna la pagina GitHub e verifica che i file siano
direttamente alla radice del repository, senza un ulteriore livello `socket`.
La cartella `tmp` non deve comparire: Git non registra cartelle vuote e i suoi
file sono ignorati.

## Passo 9 — Aggiornare il README e pubblicare un secondo commit

Apri `nano README.md` e aggiungi una sezione sullo stato del progetto:

```markdown
## Stato del progetto

Ho configurato Git e collegato il repository a GitHub.
La cartella di lavoro è ~/socket nella mia home su Web4Student.
```

Salva il file, poi controlla e registra la modifica:

```bash
git diff -- README.md
git add README.md
git commit -m "Documenta la configurazione del repository"
git push
git log --oneline -2
git status
```

Su GitHub verifica il README aggiornato e i due commit. Il file nella tua home
cambia quando lo salvi; GitHub riceve soltanto i commit inviati con `git push`.

## Consegna e autovalutazione

Consegna:

- URL del repository GitHub `Socket-Programming`.
- Percorso completo della cartella di lavoro, ottenuto con `pwd` da `~/socket`.
- Identificativi dei due commit, ottenuti con `git log --oneline -2`.
- Output di `git status` e `git check-ignore -v tmp/prova.txt`.

Rispondi alle seguenti domande:

1. Qual è il percorso della cartella `socket` e quale cartella è la radice del repository?
2. Che differenza c'è tra salvare un file, eseguire `git add`, fare commit e fare push?
3. Quando una modifica al README salvata in `~/socket` diventa visibile anche su GitHub?
4. Quale chiave SSH viene registrata su GitHub e dove rimane quella privata?
5. Perché i file `.java` vengono versionati mentre i `.class` sono esclusi?
6. Perché il repository si chiama `Socket-Programming` mentre il titolo è Socket Programming?

---

[Torna all'indice del capitolo](../README.md)
