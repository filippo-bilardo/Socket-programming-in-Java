#!/bin/bash

# Script per eseguire gli esempi SSL/TLS del corso Socket Programming in Java
# Gestisce compilazione, generazione certificati e avvio client/server

set -e  # Exit on error

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configurazione
KEYSTORE_FILE="server.jks"
KEYSTORE_PASS="password123"
SERVER_HOST="localhost"
SERVER_PORT="8443"

echo -e "${BLUE}=== ESEMPI SSL/TLS - SOCKET PROGRAMMING JAVA ===${NC}"
echo

# Funzione per stampare separatori
print_separator() {
    echo -e "${BLUE}================================================${NC}"
}

# Funzione per controllare Java
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java non trovato! Installa Java JDK 11+${NC}"
        exit 1
    fi
    
    if ! command -v javac &> /dev/null; then
        echo -e "${RED}❌ javac non trovato! Installa Java JDK${NC}"
        exit 1
    fi
    
    if ! command -v keytool &> /dev/null; then
        echo -e "${RED}❌ keytool non trovato! Keytool è incluso nel JDK${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Java environment OK${NC}"
}

# Funzione per compilare tutti i file Java
compile_all() {
    echo -e "${YELLOW}Compilazione esempi SSL...${NC}"
    
    local java_files=(
        "SimpleSSLServer.java"
        "SimpleSSLClient.java" 
        "CertificateGenerator.java"
        "SecureChatServer.java"
        "SecureChatClient.java"
    )
    
    for file in "${java_files[@]}"; do
        if [[ -f "$file" ]]; then
            echo "Compilando $file..."
            javac "$file"
            echo -e "${GREEN}✓ $file compilato${NC}"
        else
            echo -e "${YELLOW}⚠️  $file non trovato${NC}"
        fi
    done
    
    echo -e "${GREEN}✓ Compilazione completata${NC}"
}

# Funzione per generare certificati se non esistono
generate_certificates() {
    if [[ ! -f "$KEYSTORE_FILE" ]]; then
        echo -e "${YELLOW}Generando certificati SSL...${NC}"
        
        if [[ -f "CertificateGenerator.class" ]]; then
            java CertificateGenerator
        else
            echo -e "${YELLOW}Usando keytool direttamente...${NC}"
            keytool -genkeypair \
                -alias serverkey \
                -keyalg RSA \
                -keysize 2048 \
                -validity 365 \
                -keystore "$KEYSTORE_FILE" \
                -storepass "$KEYSTORE_PASS" \
                -keypass "$KEYSTORE_PASS" \
                -dname "CN=localhost,OU=SSL Demo,O=Socket Programming Course,C=IT"
        fi
        
        echo -e "${GREEN}✓ Certificati generati${NC}"
    else
        echo -e "${GREEN}✓ Certificati già presenti${NC}"
    fi
}

# Funzione per eseguire server SSL semplice
run_simple_ssl_server() {
    print_separator
    echo -e "${BLUE}AVVIO SIMPLE SSL SERVER${NC}"
    
    if [[ ! -f "SimpleSSLServer.class" ]]; then
        echo -e "${RED}❌ SimpleSSLServer non compilato${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Server in ascolto su https://$SERVER_HOST:$SERVER_PORT${NC}"
    echo -e "${YELLOW}Usa Ctrl+C per fermare il server${NC}"
    echo
    
    java SimpleSSLServer "$SERVER_PORT"
}

# Funzione per eseguire client SSL semplice
run_simple_ssl_client() {
    print_separator
    echo -e "${BLUE}AVVIO SIMPLE SSL CLIENT${NC}"
    
    if [[ ! -f "SimpleSSLClient.class" ]]; then
        echo -e "${RED}❌ SimpleSSLClient non compilato${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Connessione a $SERVER_HOST:$SERVER_PORT${NC}"
    echo
    
    java SimpleSSLClient "$SERVER_HOST" "$SERVER_PORT"
}

# Funzione per eseguire chat server sicuro
run_secure_chat_server() {
    print_separator
    echo -e "${BLUE}AVVIO SECURE CHAT SERVER${NC}"
    
    if [[ ! -f "SecureChatServer.class" ]]; then
        echo -e "${RED}❌ SecureChatServer non compilato${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Chat server in ascolto su $SERVER_HOST:$SERVER_PORT${NC}"
    echo -e "${YELLOW}Usa Ctrl+C per fermare il server${NC}"
    echo
    
    java SecureChatServer "$SERVER_PORT"
}

# Funzione per eseguire chat client sicuro
run_secure_chat_client() {
    print_separator
    echo -e "${BLUE}AVVIO SECURE CHAT CLIENT${NC}"
    
    if [[ ! -f "SecureChatClient.class" ]]; then
        echo -e "${RED}❌ SecureChatClient non compilato${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Connessione a chat server $SERVER_HOST:$SERVER_PORT${NC}"
    echo
    
    java SecureChatClient "$SERVER_HOST" "$SERVER_PORT"
}

# Funzione per test completo automatico
run_full_test() {
    print_separator
    echo -e "${BLUE}TEST AUTOMATICO COMPLETO${NC}"
    
    echo -e "${YELLOW}1. Avvio SimpleSSLServer in background...${NC}"
    java SimpleSSLServer "$SERVER_PORT" &
    SERVER_PID=$!
    sleep 3
    
    echo -e "${YELLOW}2. Test SimpleSSLClient...${NC}"
    java SimpleSSLClient "$SERVER_HOST" "$SERVER_PORT"
    
    echo -e "${YELLOW}3. Terminazione server...${NC}"
    kill $SERVER_PID 2>/dev/null || true
    sleep 2
    
    echo -e "${YELLOW}4. Avvio SecureChatServer in background...${NC}"
    java SecureChatServer "$SERVER_PORT" &
    CHAT_PID=$!
    sleep 3
    
    echo -e "${YELLOW}5. Test SecureChatClient automatico...${NC}"
    java -cp . SecureChatClient "$SERVER_HOST" "$SERVER_PORT" &
    CLIENT_PID=$!
    
    sleep 5
    
    echo -e "${YELLOW}6. Cleanup...${NC}"
    kill $CHAT_PID 2>/dev/null || true
    kill $CLIENT_PID 2>/dev/null || true
    
    echo -e "${GREEN}✓ Test completo terminato${NC}"
}

# Funzione per pulire file temporanei
cleanup() {
    echo -e "${YELLOW}Pulizia file temporanei...${NC}"
    rm -f *.class
    echo -e "${GREEN}✓ Cleanup completato${NC}"
}

# Funzione per mostrare info certificati
show_cert_info() {
    if [[ -f "$KEYSTORE_FILE" ]]; then
        echo -e "${BLUE}INFORMAZIONI CERTIFICATO${NC}"
        keytool -list -v -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PASS"
    else
        echo -e "${YELLOW}Nessun keystore trovato${NC}"
    fi
}

# Funzione per mostrare menu
show_menu() {
    print_separator
    echo -e "${BLUE}ESEMPI SSL/TLS - MENU PRINCIPALE${NC}"
    echo
    echo "1) Compila tutti gli esempi"
    echo "2) Genera certificati SSL"
    echo "3) Avvia SimpleSSLServer"
    echo "4) Avvia SimpleSSLClient" 
    echo "5) Avvia SecureChatServer"
    echo "6) Avvia SecureChatClient"
    echo "7) Test automatico completo"
    echo "8) Mostra info certificati"
    echo "9) Cleanup file .class"
    echo "0) Esci"
    echo
    echo -n "Scelta: "
}

# Main execution
main() {
    # Controllo prerequisiti
    check_java
    
    # Se parametri da linea comando
    case "${1:-}" in
        "compile")
            compile_all
            exit 0
            ;;
        "certs")
            generate_certificates
            exit 0
            ;;
        "server")
            compile_all
            generate_certificates
            run_simple_ssl_server
            exit 0
            ;;
        "client")
            compile_all
            generate_certificates
            run_simple_ssl_client
            exit 0
            ;;
        "chat-server")
            compile_all
            generate_certificates
            run_secure_chat_server
            exit 0
            ;;
        "chat-client")
            compile_all
            generate_certificates
            run_secure_chat_client
            exit 0
            ;;
        "test")
            compile_all
            generate_certificates
            run_full_test
            exit 0
            ;;
        "cleanup")
            cleanup
            exit 0
            ;;
        "help"|"-h"|"--help")
            echo "Uso: $0 [comando]"
            echo "Comandi disponibili:"
            echo "  compile      - Compila tutti gli esempi"
            echo "  certs        - Genera certificati SSL"
            echo "  server       - Avvia SimpleSSLServer"
            echo "  client       - Avvia SimpleSSLClient"
            echo "  chat-server  - Avvia SecureChatServer"
            echo "  chat-client  - Avvia SecureChatClient"
            echo "  test         - Esegui test automatico"
            echo "  cleanup      - Rimuovi file .class"
            echo "  help         - Mostra questo aiuto"
            echo
            echo "Senza parametri: mostra menu interattivo"
            exit 0
            ;;
    esac
    
    # Menu interattivo
    while true; do
        show_menu
        read -r choice
        
        case $choice in
            1)
                compile_all
                ;;
            2)
                generate_certificates
                ;;
            3)
                compile_all
                generate_certificates
                run_simple_ssl_server
                ;;
            4)
                compile_all
                generate_certificates
                run_simple_ssl_client
                ;;
            5)
                compile_all
                generate_certificates
                run_secure_chat_server
                ;;
            6)
                compile_all
                generate_certificates
                run_secure_chat_client
                ;;
            7)
                compile_all
                generate_certificates
                run_full_test
                ;;
            8)
                show_cert_info
                ;;
            9)
                cleanup
                ;;
            0)
                echo -e "${GREEN}Arrivederci!${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}❌ Scelta non valida${NC}"
                ;;
        esac
        
        echo
        echo -e "${YELLOW}Premi INVIO per continuare...${NC}"
        read -r
    done
}

# Gestione segnali per cleanup
trap 'echo -e "\n${YELLOW}Interruzione rilevata, cleanup...${NC}"; cleanup; exit 1' INT TERM

# Avvia main
main "$@"