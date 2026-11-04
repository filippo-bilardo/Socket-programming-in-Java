#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
# Gli artefatti rimangono nel tmp del modulo.
build_dir="../../tmp/serverweb-build"
mkdir -p "$build_dir"
javac -encoding UTF-8 -d "$build_dir" TcpClient.java
java -cp "$build_dir" network.TcpClient
