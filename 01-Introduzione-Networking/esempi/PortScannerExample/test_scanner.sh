#!/bin/bash

# Script per testare il PortScannerMultithread
# Esempi di utilizzo del port scanner multithread

echo "🔍 TEST PORT SCANNER MULTITHREAD"
echo "=================================="

# Compila il programma
echo "📦 Compilazione del programma..."
javac PortScannerMultithread.java

if [ $? -ne 0 ]; then
    echo "❌ Errore di compilazione"
    exit 1
fi

echo "✅ Compilazione completata"
echo ""

# Test 1: Scansione veloce porte comuni su localhost
echo "🧪 TEST 1: Scansione porte comuni localhost"
echo "Comando: java PortScannerMultithread localhost 20 90 20"
java PortScannerMultithread localhost 20 90 20
echo ""

# Test 2: Scansione su Google (solo porte comuni)
echo "🧪 TEST 2: Scansione porte web Google"
echo "Comando: java PortScannerMultithread google.com 80 443 10"
java PortScannerMultithread google.com 80 443 10
echo ""

# Test 3: Scansione range limitato con molti thread
echo "🧪 TEST 3: Scansione veloce range 8000-8100"
echo "Comando: java PortScannerMultithread localhost 8000 8100 50"
java PortScannerMultithread localhost 8000 8100 50
echo ""

# Test 4: Test help
echo "🧪 TEST 4: Visualizzazione help"
echo "Comando: java PortScannerMultithread"
java PortScannerMultithread
echo ""

# Test 5: Solo se l'utente vuole (scansione più lunga)
read -p "Vuoi eseguire una scansione più ampia (1-1000)? [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧪 TEST 5: Scansione ampia localhost (1-1000)"
    echo "Comando: java PortScannerMultithread localhost 1 1000 100"
    java PortScannerMultithread localhost 1 1000 100
fi

echo ""
echo "✅ Test completati!"
echo ""
echo "💡 Altri esempi di utilizzo:"
echo "   java PortScannerMultithread 192.168.1.1"
echo "   java PortScannerMultithread scanme.nmap.org 1 200 50" 
echo "   java PortScannerMultithread www.github.com 1 1000"
echo ""
echo "📖 Per esempi dettagliati:"
echo "   javac PortScannerExample.java && java PortScannerExample"