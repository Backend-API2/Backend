#!/bin/bash

# Script to restart the Backend application
# Usage: ./restart.sh

echo "🔄 Restarting Backend application..."

# Stop the application
./stop.sh

# Wait a moment
sleep 3

# Start the application
./start.sh
