#!/bin/bash

# Script to start the Backend application
# Usage: ./start.sh

APP_DIR="/home/ubuntu/app"
JAR_FILE=$(find $APP_DIR -name "Backend-*.jar" -type f | head -1)
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: No JAR file found in $APP_DIR"
    echo "Available files:"
    ls -la $APP_DIR/
    exit 1
fi

# Check if application is already running
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "⚠️  Application is already running with PID $PID"
        exit 1
    else
        echo "🧹 Removing stale PID file"
        rm -f "$PID_FILE"
    fi
fi

# Start the application
echo "🚀 Starting Backend application..."
echo "JAR file: $JAR_FILE"
echo "Log file: $LOG_FILE"

# Start with production profile
nohup java -jar -Dspring.profiles.active=prod "$JAR_FILE" > "$LOG_FILE" 2>&1 &
APP_PID=$!

# Save PID
echo $APP_PID > "$PID_FILE"

# Wait a moment and check if it started successfully
sleep 5

if ps -p $APP_PID > /dev/null 2>&1; then
    echo "✅ Application started successfully with PID $APP_PID"
    echo "📋 Logs: tail -f $LOG_FILE"
    echo "🔍 Status: ./status.sh"
    echo "🛑 Stop: ./stop.sh"
else
    echo "❌ Failed to start application"
    echo "📋 Check logs: tail -20 $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
