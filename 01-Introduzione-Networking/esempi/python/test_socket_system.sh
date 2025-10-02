#!/bin/bash
"""
Script di test per il sistema server-client TCP

Questo script facilita il testing e l'apprendimento:
- Avvia il server in background
- Esegue test automatici con il client
- Mostra esempi d'uso pratici
- Cleanup automatico

Uso:
    ./test_socket_system.sh [opzioni]
    
Opzioni:
    start_server    - Avvia solo il server
    test_client     - Testa solo il client
    full_test       - Test completo (default)
    cleanup         - Termina processi rimasti
"""

# Configurazione
SERVER_SCRIPT="01-simple_tcp_server.py"
CLIENT_SCRIPT="02-simple_tcp_client.py"
SERVER_HOST="localhost"
SERVER_PORT="12345"
TEST_DIR="/home/git-projects/SISTEMI_E_RETI_3_MY/A-Socket_programming/corso socket2/01-Basi_dei_Socket_TCP/esempi"

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}    TEST SISTEMA SOCKET TCP COMPLETO${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_section() {
    echo -e "\n${YELLOW}>>> $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

check_python() {
    if ! command -v python3 &> /dev/null; then
        print_error "Python3 non trovato!"
        exit 1
    fi
    print_success "Python3 disponibile: $(python3 --version)"
}

check_files() {
    cd "$TEST_DIR" || {
        print_error "Directory test non trovata: $TEST_DIR"
        exit 1
    }
    
    for file in "$SERVER_SCRIPT" "$CLIENT_SCRIPT"; do
        if [[ ! -f "$file" ]]; then
            print_error "File non trovato: $file"
            exit 1
        fi
    done
    print_success "File script trovati"
}

kill_existing_servers() {
    print_section "Cleanup processi esistenti"
    
    # Cerca processi Python che usano la porta
    local pids=$(lsof -ti:$SERVER_PORT 2>/dev/null || true)
    
    if [[ -n "$pids" ]]; then
        print_info "Terminando processi sulla porta $SERVER_PORT..."
        echo "$pids" | xargs kill -9 2>/dev/null || true
        sleep 1
        print_success "Processi terminati"
    else
        print_info "Nessun processo da terminare sulla porta $SERVER_PORT"
    fi
}

start_server() {
    print_section "Avvio Server TCP"
    
    # Avvia server in background
    python3 "$SERVER_SCRIPT" &
    local server_pid=$!
    
    print_info "Server avviato con PID: $server_pid"
    
    # Attendi che server sia pronto
    print_info "Attendo che il server sia pronto..."
    for i in {1..10}; do
        if netstat -ln 2>/dev/null | grep -q ":$SERVER_PORT "; then
            print_success "Server pronto sulla porta $SERVER_PORT"
            return 0
        fi
        sleep 1
        echo -n "."
    done
    
    print_error "Server non si è avviato correttamente"
    kill $server_pid 2>/dev/null || true
    return 1
}

wait_for_server() {
    print_info "Verifica disponibilità server..."
    
    for i in {1..5}; do
        if nc -z $SERVER_HOST $SERVER_PORT 2>/dev/null; then
            print_success "Server risponde"
            return 0
        fi
        print_info "Tentativo $i/5..."
        sleep 2
    done
    
    print_error "Server non risponde"
    return 1
}

test_client_basic() {
    print_section "Test Client - Modalità Automatica"
    
    # Test messaggi predefiniti
    local test_messages=(
        "Hello World"
        "Test message 123" 
        "PING_TEST"
        "Messaggio con ñ àccénti"
        "quit"
    )
    
    print_info "Invio messaggi di test..."
    
    for msg in "${test_messages[@]}"; do
        print_info "Invio: '$msg'"
        echo "$msg" | python3 "$CLIENT_SCRIPT" $SERVER_HOST $SERVER_PORT
        sleep 1
    done
    
    print_success "Test messaggi completato"
}

test_client_interactive() {
    print_section "Test Client - Modalità Interattiva"
    
    print_info "Avvio client interattivo..."
    print_info "Usa 'quit' per uscire, 'stats' per statistiche"
    
    python3 "$CLIENT_SCRIPT" $SERVER_HOST $SERVER_PORT
}

test_concurrent_clients() {
    print_section "Test Client Concorrenti"
    
    print_info "Avvio 5 client concorrenti..."
    
    for i in {1..5}; do
        (
            echo "Client_${i}_Message" | python3 "$CLIENT_SCRIPT" $SERVER_HOST $SERVER_PORT
        ) &
    done
    
    wait  # Attendi tutti i client
    print_success "Test concorrenza completato"
}

performance_test() {
    print_section "Test Performance"
    
    print_info "Test rapidità connessioni..."
    
    local start_time=$(date +%s.%N)
    
    for i in {1..20}; do
        echo "PERF_TEST_$i" | python3 "$CLIENT_SCRIPT" $SERVER_HOST $SERVER_PORT > /dev/null &
        
        # Limita connessioni concorrenti
        if (( i % 5 == 0 )); then
            wait
        fi
    done
    
    wait  # Attendi tutti
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc -l)
    
    print_success "20 connessioni completate in ${duration}s"
}

run_full_test() {
    print_header
    
    print_section "Pre-controlli"
    check_python
    check_files
    kill_existing_servers
    
    # Avvia server
    if start_server; then
        sleep 2  # Stabilizzazione
        
        # Test vari
        test_client_basic
        test_concurrent_clients
        performance_test
        
        print_section "Test completati"
        print_info "Server ancora attivo per test manuali"
        print_info "Usa 'kill \$(lsof -ti:$SERVER_PORT)' per terminarlo"
        
        # Opzione test interattivo
        read -p "Vuoi avviare il client interattivo? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            test_client_interactive
        fi
        
    else
        print_error "Impossibile avviare il server"
        exit 1
    fi
}

# Gestione argomenti
case "${1:-full_test}" in
    "start_server")
        print_header
        check_python
        check_files
        kill_existing_servers
        start_server
        print_info "Server avviato. Premi Ctrl+C per terminare"
        wait
        ;;
    
    "test_client")
        print_header
        check_python 
        check_files
        if wait_for_server; then
            test_client_interactive
        fi
        ;;
    
    "cleanup")
        kill_existing_servers
        ;;
    
    "full_test")
        run_full_test
        ;;
    
    "help"|"-h"|"--help")
        echo "Uso: $0 [start_server|test_client|full_test|cleanup|help]"
        echo
        echo "Comandi:"
        echo "  start_server  - Avvia solo il server TCP"
        echo "  test_client   - Avvia solo il client (server deve essere già attivo)"
        echo "  full_test     - Esegue test completo (default)"
        echo "  cleanup       - Termina processi sulla porta $SERVER_PORT"
        echo "  help          - Mostra questo aiuto"
        ;;
    
    *)
        print_error "Comando sconosciuto: $1"
        echo "Usa '$0 help' per vedere i comandi disponibili"
        exit 1
        ;;
esac