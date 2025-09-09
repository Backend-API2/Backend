#!/bin/bash

# Script to check the status of the Backend application
# Usage: ./status.sh

APP_DIR="/home/ubuntu/app"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"

echo "🔍 Backend Application Status"
echo "=============================="

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    echo "❌ Status: Not running (no PID file)"
    exit 1
fi

# Read PID
PID=$(cat "$PID_FILE")

# Check if process is running
if ps -p $PID > /dev/null 2>&1; then
    echo "✅ Status: Running (PID: $PID)"
    
    # Get process info
    echo "📊 Process Info:"
    ps -p $PID -o pid,ppid,cmd,etime,pcpu,pmem
    
    # Check if port is listening
    if netstat -tlnp 2>/dev/null | grep -q ":$PID.*:8080"; then
        echo "🌐 Port 8080: Listening"
    else
        echo "⚠️  Port 8080: Not listening"
    fi
    
    # Health check
    echo "🏥 Health Check:"
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Health: OK"
    else
        echo "❌ Health: FAILED"
    fi
    
    # Show recent logs
    echo ""
    echo "📋 Recent Logs (last 10 lines):"
    echo "--------------------------------"
    tail -10 "$LOG_FILE" 2>/dev/null || echo "No logs available"
    
else
    echo "❌ Status: Not running (stale PID file)"
    echo "🧹 Removing stale PID file"
    rm -f "$PID_FILE"
    exit 1
fi
