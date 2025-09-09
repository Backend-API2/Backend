#!/bin/bash

# Script to stop the Backend application
# Usage: ./stop.sh

APP_DIR="/home/ubuntu/app"
PID_FILE="$APP_DIR/app.pid"

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    echo "⚠️  No PID file found. Application might not be running."
    exit 0
fi

# Read PID
PID=$(cat "$PID_FILE")

# Check if process is running
if ! ps -p $PID > /dev/null 2>&1; then
    echo "⚠️  Process with PID $PID is not running. Removing stale PID file."
    rm -f "$PID_FILE"
    exit 0
fi

echo "🛑 Stopping Backend application (PID: $PID)..."

# Try graceful shutdown first
kill -TERM $PID

# Wait for graceful shutdown (max 30 seconds)
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "✅ Application stopped gracefully"
        rm -f "$PID_FILE"
        exit 0
    fi
    echo "⏳ Waiting for graceful shutdown... ($i/30)"
    sleep 1
done

# Force kill if still running
echo "⚠️  Graceful shutdown failed. Force killing..."
kill -KILL $PID

# Wait a moment and verify
sleep 2
if ! ps -p $PID > /dev/null 2>&1; then
    echo "✅ Application force stopped"
    rm -f "$PID_FILE"
else
    echo "❌ Failed to stop application"
    exit 1
fi
