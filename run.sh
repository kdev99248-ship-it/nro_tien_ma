#!/usr/bin/env bash
#
# run.sh - Start the NRO server (server.jar) in the background with nohup.
#
# The server loads its config/data via RELATIVE paths (e.g. data/config/config.properties),
# so we must cd into this script's directory before launching java.
#
# Usage:
#   ./run.sh            # start the server (background, via nohup)
#   JAVA_XMX=4g ./run.sh   # override max heap
#

set -euo pipefail

# --- Always run from the directory this script lives in (server root) ---
cd "$(dirname "$0")"

# --- Config (override via environment if needed) ---
JAVA_BIN="${JAVA_BIN:-java}"
MAIN_CLASS="${MAIN_CLASS:-server.ServerManager}"
JAVA_XMS="${JAVA_XMS:-512m}"
JAVA_XMX="${JAVA_XMX:-2g}"
JAVA_OPTS="${JAVA_OPTS:--Xms${JAVA_XMS} -Xmx${JAVA_XMX} -Dfile.encoding=UTF-8}"

# server.jar is a fat jar, but include lib/* too in case some deps are external.
CLASSPATH="server.jar:lib/*"

LOG_DIR="logs"
LOG_FILE="${LOG_DIR}/server.log"
PID_FILE="server.pid"

mkdir -p "$LOG_DIR"

# --- Refuse to start if already running ---
if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "Server already running (PID $(cat "$PID_FILE")). Stop it first." >&2
    exit 1
fi

# --- Sanity checks ---
if ! command -v "$JAVA_BIN" >/dev/null 2>&1; then
    echo "Error: '$JAVA_BIN' not found on PATH. Install a JDK/JRE or set JAVA_BIN." >&2
    exit 1
fi
if [[ ! -f server.jar ]]; then
    echo "Error: server.jar not found in $(pwd)." >&2
    exit 1
fi

# --- Launch ---
echo "Starting server: $JAVA_BIN $JAVA_OPTS -cp \"$CLASSPATH\" $MAIN_CLASS"
nohup "$JAVA_BIN" $JAVA_OPTS -cp "$CLASSPATH" "$MAIN_CLASS" >> "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
echo "Started (PID $(cat "$PID_FILE")). Logs: $LOG_FILE"
echo "Follow logs with:  tail -f $LOG_FILE"
