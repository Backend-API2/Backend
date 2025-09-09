#!/bin/bash

# Script to clean up port 8080 and any Backend processes
# Usage: ./cleanup-ports.sh

echo "🧹 Cleaning up port 8080 and Backend processes..."

# Kill any processes using port 8080
echo "🔍 Checking for processes using port 8080..."
PORT_PIDS=$(lsof -ti:8080 2>/dev/null || echo '')
if [ ! -z "$PORT_PIDS" ]; then
  echo "Found processes using port 8080: $PORT_PIDS"
  for PID in $PORT_PIDS; do
    echo "Killing process $PID..."
    kill -TERM $PID 2>/dev/null || true
  done
  sleep 3
  for PID in $PORT_PIDS; do
    if ps -p $PID > /dev/null 2>&1; then
      echo "Force killing process $PID..."
      kill -KILL $PID 2>/dev/null || true
    fi
  done
else
  echo "No processes found using port 8080"
fi

# Kill any Java Backend processes
echo "🔍 Killing Java Backend processes..."
pkill -f 'java.*Backend' 2>/dev/null || echo 'No Java Backend processes found'

# Clean up PID and log files
echo "🧹 Cleaning up files..."
rm -f app.pid app.log

# Final verification
echo "🔍 Final verification..."
if lsof -ti:8080 >/dev/null 2>&1; then
  echo "⚠️  Port 8080 still in use after cleanup"
  echo "Processes using port 8080:"
  lsof -i:8080 || true
  exit 1
else
  echo "✅ Port 8080 is now free"
fi

echo "✅ Cleanup completed successfully!"
