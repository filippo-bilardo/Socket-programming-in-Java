#!/usr/bin/env python3
"""
LABORATORIO SOCKET TCP - Esercizi Guidati
==========================================

Questo file contiene esercizi pratici progressivi per l'apprendimento
della programmazione socket TCP. Ogni esercizio include:
- Descrizione del problema
- Template di codice da completare  
- Test per verificare la correttezza
- Soluzioni commentate

ISTRUZIONI:
1. Leggi attentamente la descrizione di ogni esercizio
2. Completa il codice nelle sezioni marcate con TODO
3. Esegui i test per verificare la soluzione
4. Confronta con la soluzione fornita

PREREQUISITI:
- Conoscenza base Python
- Aver letto 01-Introduzione-ai-Socket.md
- Server TCP funzionante sulla porta 12345
"""

import socket
import threading
import time
import json
import hashlib
import select
import sys

print("🧪 LABORATORIO SOCKET TCP")
print("=" * 50)

# =============================================================================
# ESERCIZIO 1: SOCKET INSPECTOR
# =============================================================================

def esercizio_1_socket_inspector():
    """
    OBIETTIVO: Creare un ispettore di proprietà socket
    
    DESCRIZIONE:
    Implementa una funzione che crea diversi tipi di socket e ne analizza
    le proprietà. Deve mostrare informazioni come file descriptor,
    famiglia di indirizzi, tipo di socket, e opzioni configurate.
    
    COMPETENZE:
    - Creazione socket con diverse opzioni
    - Lettura proprietà socket  
    - Gestione errori socket
    """
    
    print("\n📍 ESERCIZIO 1: Socket Inspector")
    print("-" * 30)
    
    def socket_inspector():
        """TODO: Implementa l'ispettore socket"""
        
        # TODO 1.1: Crea un socket TCP IPv4
        # tcp_sock = ...
        
        # TODO 1.2: Stampa le proprietà del socket TCP
        # - File descriptor (fileno())
        # - Famiglia indirizzi (family)  
        # - Tipo socket (type)
        # - Nome socket locale (getsockname() se bound)
        
        # TODO 1.3: Crea un socket UDP IPv4 
        # udp_sock = ...
        
        # TODO 1.4: Stampa le proprietà del socket UDP
        
        # TODO 1.5: Testa alcune socket options
        # - SO_REUSEADDR
        # - SO_KEEPALIVE (solo TCP)
        # - Buffer sizes (SO_RCVBUF, SO_SNDBUF)
        
        # TODO 1.6: Cleanup - chiudi i socket
        
        pass
    
    # SOLUZIONE:
    def socket_inspector_soluzione():
        """Soluzione completa dell'esercizio 1"""
        
        print("🔍 Analisi Socket Properties:")
        
        # 1.1: Socket TCP
        tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print(f"\n📡 TCP Socket:")
        print(f"   FD: {tcp_sock.fileno()}")
        print(f"   Family: {tcp_sock.family} (AF_INET={socket.AF_INET})")
        print(f"   Type: {tcp_sock.type} (SOCK_STREAM={socket.SOCK_STREAM})")
        
        # 1.2: Socket UDP  
        udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        print(f"\n📡 UDP Socket:")
        print(f"   FD: {udp_sock.fileno()}")
        print(f"   Family: {udp_sock.family} (AF_INET={socket.AF_INET})")
        print(f"   Type: {udp_sock.type} (SOCK_DGRAM={socket.SOCK_DGRAM})")
        
        # 1.3: Test socket options
        print(f"\n⚙️ Socket Options (TCP):")
        
        # SO_REUSEADDR
        tcp_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        reuse = tcp_sock.getsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR)
        print(f"   SO_REUSEADDR: {reuse}")
        
        # Buffer sizes
        rcvbuf = tcp_sock.getsockopt(socket.SOL_SOCKET, socket.SO_RCVBUF)
        sndbuf = tcp_sock.getsockopt(socket.SOL_SOCKET, socket.SO_SNDBUF)
        print(f"   SO_RCVBUF: {rcvbuf:,} bytes")
        print(f"   SO_SNDBUF: {sndbuf:,} bytes")
        
        # 1.4: Informazioni sistema
        print(f"\n🖥️ System Info:")
        print(f"   Hostname: {socket.gethostname()}")
        print(f"   FQDN: {socket.getfqdn()}")
        
        try:
            local_ip = socket.gethostbyname(socket.gethostname())
            print(f"   Local IP: {local_ip}")
        except:
            print(f"   Local IP: Unable to resolve")
        
        # 1.5: Cleanup
        tcp_sock.close()
        udp_sock.close()
        print(f"\n✅ Socket chiusi correttamente")
    
    # Esegui la tua implementazione
    print("🔧 La tua implementazione:")
    try:
        socket_inspector()
    except Exception as e:
        print(f"❌ Errore nella tua implementazione: {e}")
    
    # Mostra soluzione
    print("\n💡 Soluzione:")
    socket_inspector_soluzione()

# =============================================================================
# ESERCIZIO 2: CLIENT ECHO SEMPLICE  
# =============================================================================

def esercizio_2_echo_client():
    """
    OBIETTIVO: Implementare un client echo TCP basico
    
    DESCRIZIONE:
    Crea un client che si connette al server echo sulla porta 12345,
    invia un messaggio, riceve la risposta e la stampa.
    Il client deve gestire errori base e chiudere pulitamente.
    
    COMPETENZE:
    - Connessione TCP
    - Invio/ricezione dati
    - Gestione errori base
    - Cleanup risorse
    """
    
    print("\n📍 ESERCIZIO 2: Echo Client Semplice")
    print("-" * 35)
    
    def echo_client(message="Hello from lab!"):
        """TODO: Implementa client echo"""
        
        # TODO 2.1: Crea socket TCP
        # sock = ...
        
        # TODO 2.2: Connetti al server localhost:12345
        # sock.connect(...)
        
        # TODO 2.3: Invia messaggio (ricorda encoding UTF-8)
        # sock.send(...)
        
        # TODO 2.4: Ricevi risposta  
        # response = sock.recv(...)
        
        # TODO 2.5: Stampa risposta (ricorda decoding)
        # print(...)
        
        # TODO 2.6: Chiudi socket
        # sock.close()
        
        pass
    
    # SOLUZIONE:
    def echo_client_soluzione(message="Hello from lab solution!"):
        """Soluzione completa dell'esercizio 2"""
        
        try:
            # 2.1: Crea socket
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            
            # 2.2: Connetti  
            print(f"🔗 Connessione a localhost:12345...")
            sock.connect(('localhost', 12345))
            print(f"✅ Connesso!")
            
            # 2.3: Invia messaggio
            data = message.encode('utf-8')
            sock.send(data)
            print(f"📤 Inviato: '{message}' ({len(data)} bytes)")
            
            # 2.4: Ricevi risposta
            response_data = sock.recv(1024)
            response = response_data.decode('utf-8')
            print(f"📥 Ricevuto: '{response}' ({len(response_data)} bytes)")
            
            # 2.5: Chiudi
            sock.close()
            print(f"🔐 Connessione chiusa")
            
        except ConnectionRefusedError:
            print(f"❌ Errore: Server non disponibile su localhost:12345")
        except socket.error as e:
            print(f"❌ Errore socket: {e}")
        except Exception as e:
            print(f"❌ Errore generico: {e}")
    
    # Test implementazione studente
    print("🔧 La tua implementazione:")
    try:
        echo_client("Test message from student")
    except Exception as e:
        print(f"❌ Errore nella tua implementazione: {e}")
    
    # Mostra soluzione
    print("\n💡 Soluzione:")
    echo_client_soluzione()

# =============================================================================
# ESERCIZIO 3: CLIENT CON TIMEOUT E RETRY
# =============================================================================

def esercizio_3_robust_client():
    """
    OBIETTIVO: Client robusto con gestione timeout e retry
    
    DESCRIZIONE:
    Migliora il client echo aggiungendo:
    - Timeout per connessione e operazioni
    - Retry automatico su fallimenti
    - Logging degli eventi
    - Gestione errori specifica
    
    COMPETENZE:
    - Socket timeout
    - Retry logic
    - Error handling avanzato
    - Logging
    """
    
    print("\n📍 ESERCIZIO 3: Robust Client")
    print("-" * 30)
    
    def robust_echo_client(message, max_retries=3, timeout=5):
        """TODO: Client robusto con retry"""
        
        # TODO 3.1: Loop retry
        # for attempt in range(1, max_retries + 1):
        
            # TODO 3.2: Crea socket con timeout
            # sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # sock.settimeout(timeout)
            
            # TODO 3.3: Tentativo connessione con gestione errori
            # try:
            #     sock.connect(('localhost', 12345))
            # except socket.timeout:
            #     # Gestisci timeout
            # except ConnectionRefused:
            #     # Gestisci rifiuto connessione
            
            # TODO 3.4: Se connesso, invia/ricevi con timeout
            
            # TODO 3.5: Se successo, return. Se fallisce, retry con delay
            
        # TODO 3.6: Se tutti i retry falliscono, solleva eccezione
        
        pass
    
    # SOLUZIONE:
    def robust_echo_client_soluzione(message, max_retries=3, timeout=5):
        """Soluzione completa dell'esercizio 3"""
        
        for attempt in range(1, max_retries + 1):
            print(f"🔄 Tentativo {attempt}/{max_retries}")
            
            try:
                # Socket con timeout
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.settimeout(timeout)
                
                # Connessione
                sock.connect(('localhost', 12345))
                print(f"✅ Connesso al tentativo {attempt}")
                
                # Comunicazione
                sock.send(message.encode('utf-8'))
                response = sock.recv(1024).decode('utf-8')
                
                sock.close()
                print(f"📥 Risposta: '{response}'")
                return response
                
            except socket.timeout:
                print(f"⏰ Timeout al tentativo {attempt}")
            except ConnectionRefusedError:
                print(f"❌ Connessione rifiutata al tentativo {attempt}")
            except Exception as e:
                print(f"❌ Errore al tentativo {attempt}: {e}")
            
            # Delay tra tentativi (eccetto ultimo)
            if attempt < max_retries:
                delay = 2 ** (attempt - 1)  # Backoff esponenziale
                print(f"⏳ Attesa {delay}s prima del prossimo tentativo...")
                time.sleep(delay)
        
        raise Exception(f"Falliti tutti i {max_retries} tentativi")
    
    # Test implementazione studente  
    print("🔧 La tua implementazione:")
    try:
        robust_echo_client("Robust test message")
    except Exception as e:
        print(f"❌ Errore nella tua implementazione: {e}")
    
    # Mostra soluzione
    print("\n💡 Soluzione:")
    try:
        robust_echo_client_soluzione("Robust solution message")
    except Exception as e:
        print(f"ℹ️ Soluzione: {e}")

# =============================================================================
# ESERCIZIO 4: SERVER ECHO MULTITHREAD
# =============================================================================

def esercizio_4_multithread_server():
    """
    OBIETTIVO: Server echo che gestisce client multipli
    
    DESCRIZIONE:  
    Implementa un server che può gestire più client contemporaneamente
    usando threading. Ogni client deve essere servito in un thread separato.
    
    COMPETENZE:
    - Threading per server concorrenti
    - Condivisione stato tra thread
    - Sincronizzazione e thread safety
    - Gestione lifecycle thread
    """
    
    print("\n📍 ESERCIZIO 4: Multithread Server")
    print("-" * 35)
    
    def multithread_server(host='localhost', port=12346):  # Porta diversa per test
        """TODO: Server multithread"""
        
        # TODO 4.1: Crea socket server
        # server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        # server_sock.bind((host, port))
        # server_sock.listen(5)
        
        # TODO 4.2: Funzione per gestire singolo client  
        # def handle_client(client_sock, client_addr):
        #     # Ricevi messaggio
        #     # Invia risposta echo
        #     # Chiudi socket client
        
        # TODO 4.3: Loop principale accept
        # while True:
        #     client_sock, client_addr = server_sock.accept()
        #     # Crea thread per gestire client
        #     # thread = threading.Thread(target=handle_client, args=(client_sock, client_addr))
        #     # thread.daemon = True  # Thread daemon per cleanup automatico
        #     # thread.start()
        
        pass
    
    # SOLUZIONE:
    def multithread_server_soluzione(host='localhost', port=12346):
        """Soluzione completa dell'esercizio 4"""
        
        # Statistiche condivise (thread-safe con lock)
        stats = {'connections': 0, 'active_threads': 0}
        stats_lock = threading.Lock()
        
        def handle_client(client_sock, client_addr):
            """Gestisce singolo client in thread separato"""
            
            # Aggiorna statistiche
            with stats_lock:
                stats['connections'] += 1
                stats['active_threads'] += 1
                conn_id = stats['connections']
            
            print(f"🔗 [Thread-{conn_id}] Client connesso: {client_addr}")
            
            try:
                while True:
                    # Ricevi dati (con timeout per evitare hang)
                    client_sock.settimeout(30)
                    data = client_sock.recv(1024)
                    
                    if not data:
                        break  # Client ha chiuso connessione
                    
                    message = data.decode('utf-8')
                    print(f"📥 [Thread-{conn_id}] Ricevuto: '{message.strip()}'")
                    
                    # Echo response
                    response = f"Echo [{conn_id}]: {message}"
                    client_sock.send(response.encode('utf-8'))
                    print(f"📤 [Thread-{conn_id}] Inviato echo")
                    
            except socket.timeout:
                print(f"⏰ [Thread-{conn_id}] Timeout client")
            except Exception as e:
                print(f"❌ [Thread-{conn_id}] Errore: {e}")
            finally:
                client_sock.close()
                
                # Aggiorna statistiche  
                with stats_lock:
                    stats['active_threads'] -= 1
                    
                print(f"🔐 [Thread-{conn_id}] Client disconnesso")
                print(f"📊 Thread attivi: {stats['active_threads']}")
        
        # Setup server
        server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server_sock.bind((host, port))
        server_sock.listen(5)
        
        print(f"🚀 Server multithread avviato su {host}:{port}")
        print(f"📡 In attesa di connessioni... (Ctrl+C per fermare)")
        
        try:
            while True:
                client_sock, client_addr = server_sock.accept()
                
                # Thread per gestire client
                thread = threading.Thread(
                    target=handle_client,
                    args=(client_sock, client_addr),
                    daemon=True  # Termina automaticamente con processo principale
                )
                thread.start()
                
        except KeyboardInterrupt:
            print(f"\n⚠️ Server interrotto da utente")
        finally:
            server_sock.close()
            print(f"🔐 Server chiuso")
    
    # Nota: Per testare questo server, devi avviarlo in un processo separato
    print("🔧 Per testare il server multithread:")
    print("   1. Avvia il server in un terminale separato")
    print("   2. Connetti multiple client contemporaneamente") 
    print("   3. Osserva la gestione concorrente")
    
    print("\n💡 Soluzione disponibile (non eseguita automaticamente):")
    print("   multithread_server_soluzione() - Avvia server sulla porta 12346")

# =============================================================================
# ESERCIZIO 5: PROTOCOLLO CHAT SEMPLICE
# =============================================================================

def esercizio_5_chat_protocol():
    """
    OBIETTIVO: Implementare protocollo chat con comandi
    
    DESCRIZIONE:
    Estendi il server per supportare un protocollo chat con comandi:
    - JOIN <nickname> - Unisciti alla chat
    - MSG <messaggio> - Invia messaggio a tutti  
    - LIST - Lista utenti connessi
    - QUIT - Disconnetti
    
    COMPETENZE:
    - Design di protocolli applicativi
    - Parsing comandi
    - Broadcast messaging
    - State management distribuito
    """
    
    print("\n📍 ESERCIZIO 5: Chat Protocol")
    print("-" * 30)
    
    def chat_protocol_parser(command_line):
        """TODO: Parser per comandi chat"""
        
        # TODO 5.1: Split comando e argomenti
        # parts = command_line.strip().split(' ', 1)
        # command = parts[0].upper()
        # args = parts[1] if len(parts) > 1 else ""
        
        # TODO 5.2: Return comando strutturato
        # return {'command': command, 'args': args}
        
        pass
    
    def chat_server_handler():
        """TODO: Handler per server chat"""
        
        # TODO 5.3: Mantieni lista utenti connessi
        # connected_users = {}  # {socket: nickname}
        
        # TODO 5.4: Implementa broadcast_message(sender, message)
        
        # TODO 5.5: Implementa gestione comandi:
        # - JOIN: aggiungi utente
        # - MSG: broadcast messaggio  
        # - LIST: invia lista utenti
        # - QUIT: rimuovi utente
        
        pass
    
    # SOLUZIONE:
    def chat_protocol_parser_soluzione(command_line):
        """Soluzione parser comandi chat"""
        
        parts = command_line.strip().split(' ', 1)
        command = parts[0].upper() if parts else ""
        args = parts[1] if len(parts) > 1 else ""
        
        return {
            'command': command,
            'args': args,
            'valid': command in ['JOIN', 'MSG', 'LIST', 'QUIT']
        }
    
    class ChatServer:
        """Soluzione completa server chat"""
        
        def __init__(self):
            self.users = {}  # {socket: nickname}
            self.users_lock = threading.Lock()
        
        def broadcast_message(self, sender_nick, message, exclude_sender=True):
            """Invia messaggio a tutti gli utenti connessi"""
            
            with self.users_lock:
                disconnected = []
                
                for user_sock, nickname in self.users.items():
                    if exclude_sender and nickname == sender_nick:
                        continue
                    
                    try:
                        broadcast_msg = f"[{sender_nick}]: {message}"
                        user_sock.send(broadcast_msg.encode('utf-8'))
                    except:
                        # Utente disconnesso, rimuovi dopo
                        disconnected.append(user_sock)
                
                # Cleanup utenti disconnessi
                for sock in disconnected:
                    if sock in self.users:
                        del self.users[sock]
        
        def handle_command(self, client_sock, command_data):
            """Gestisce comando ricevuto da client"""
            
            cmd = command_data['command']
            args = command_data['args']
            
            if cmd == 'JOIN':
                nickname = args.strip()
                if not nickname:
                    client_sock.send(b"ERROR: Nickname richiesto")
                    return False
                
                with self.users_lock:
                    self.users[client_sock] = nickname
                
                client_sock.send(f"OK: Benvenuto {nickname}!".encode('utf-8'))
                self.broadcast_message("SERVER", f"{nickname} si è unito alla chat")
                return True
                
            elif cmd == 'MSG':
                if client_sock not in self.users:
                    client_sock.send(b"ERROR: Devi prima fare JOIN")
                    return False
                
                if not args.strip():
                    client_sock.send(b"ERROR: Messaggio vuoto")
                    return False
                
                nickname = self.users[client_sock]
                self.broadcast_message(nickname, args)
                client_sock.send(b"OK: Messaggio inviato")
                return True
                
            elif cmd == 'LIST':
                with self.users_lock:
                    user_list = list(self.users.values())
                
                response = f"USERS: {', '.join(user_list) if user_list else 'Nessuno online'}"
                client_sock.send(response.encode('utf-8'))
                return True
                
            elif cmd == 'QUIT':
                if client_sock in self.users:
                    nickname = self.users[client_sock]
                    with self.users_lock:
                        del self.users[client_sock]
                    
                    self.broadcast_message("SERVER", f"{nickname} ha abbandonato la chat")
                
                client_sock.send(b"BYE: Arrivederci!")
                return False  # Disconnetti client
                
            else:
                client_sock.send(b"ERROR: Comando sconosciuto")
                return False
    
    # Test parser
    print("🧪 Test parser comandi:")
    test_commands = [
        "JOIN Alice",
        "MSG Ciao a tutti!",
        "LIST", 
        "QUIT",
        "INVALID command"
    ]
    
    for cmd in test_commands:
        result = chat_protocol_parser_soluzione(cmd)
        print(f"  '{cmd}' → {result}")
    
    print("\n💡 Soluzione completa disponibile nella classe ChatServer")
    print("   Per testare: istanzia ChatServer e usa handle_command()")

# =============================================================================
# MENU PRINCIPALE
# =============================================================================

def main_menu():
    """Menu principale laboratorio"""
    
    exercises = {
        '1': ("Socket Inspector", esercizio_1_socket_inspector),
        '2': ("Echo Client Semplice", esercizio_2_echo_client),
        '3': ("Client Robusto (Timeout/Retry)", esercizio_3_robust_client),
        '4': ("Server Multithread", esercizio_4_multithread_server),
        '5': ("Chat Protocol", esercizio_5_chat_protocol),
    }
    
    while True:
        print(f"\n🎯 MENU LABORATORIO SOCKET TCP")
        print("=" * 40)
        
        for key, (name, _) in exercises.items():
            print(f"  {key}. {name}")
        print(f"  0. Esci")
        
        choice = input(f"\nScegli esercizio (0-5): ").strip()
        
        if choice == '0':
            print("👋 Arrivederci!")
            break
        elif choice in exercises:
            name, func = exercises[choice]
            print(f"\n🚀 Avvio: {name}")
            try:
                func()
            except KeyboardInterrupt:
                print(f"\n⚠️ Esercizio interrotto")
            except Exception as e:
                print(f"\n❌ Errore durante esercizio: {e}")
            
            input(f"\n⏸️ Premi ENTER per continuare...")
        else:
            print("❌ Scelta non valida")

if __name__ == "__main__":
    """
    ISTRUZIONI PER L'USO:
    
    1. Assicurati che il server TCP sia in esecuzione:
       python3 01-simple_tcp_server.py
       
    2. Esegui questo laboratorio:
       python3 laboratorio_socket_tcp.py
       
    3. Seleziona gli esercizi dal menu
    
    4. Completa il codice nelle sezioni TODO
    
    5. Confronta con le soluzioni fornite
    
    SUGGERIMENTI:
    - Inizia dall'esercizio 1 e procedi in ordine
    - Leggi attentamente i commenti TODO  
    - Testa ogni implementazione prima di passare al prossimo
    - Usa le soluzioni come riferimento, non come scorciatoia
    - Sperimenta con variazioni e miglioramenti
    """
    
    try:
        main_menu()
    except KeyboardInterrupt:
        print(f"\n👋 Laboratorio terminato dall'utente")
    except Exception as e:
        print(f"\n❌ Errore critico: {e}")
        sys.exit(1)