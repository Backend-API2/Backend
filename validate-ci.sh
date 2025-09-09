#!/bin/bash

echo "🚀 Validating CI/CD Pipeline Locally"
echo "====================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
        exit 1
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

echo "📋 Step 1: Validating Maven project..."
mvn validate -f Backend/pom.xml
print_status $? "Maven validation"

echo "📋 Step 2: Compiling code..."
mvn compile -f Backend/pom.xml
print_status $? "Code compilation"

echo "📋 Step 3: Running tests..."
mvn test -f Backend/pom.xml
print_status $? "Unit tests"

echo "📋 Step 4: Packaging application..."
mvn package -DskipTests -f Backend/pom.xml
print_status $? "Application packaging"

echo "📋 Step 5: Verifying JAR creation..."
if [ -f "Backend/target/Backend-0.0.1-SNAPSHOT.jar" ]; then
    JAR_SIZE=$(ls -lh Backend/target/Backend-0.0.1-SNAPSHOT.jar | awk '{print $5}')
    echo -e "${GREEN}✅ JAR file created successfully (Size: $JAR_SIZE)${NC}"
else
    echo -e "${RED}❌ JAR file not found${NC}"
    exit 1
fi

echo "📋 Step 6: Testing JAR execution..."
echo "Starting application in background..."
nohup java -jar Backend/target/Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
APP_PID=$!

echo "Waiting for application to start..."
sleep 15

echo "Testing health endpoint..."
if curl -f http://localhost:8080/api/health/check > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Health endpoint working${NC}"
else
    echo -e "${RED}❌ Health endpoint failed${NC}"
    echo "Application logs:"
    cat app.log
    kill $APP_PID 2>/dev/null || true
    exit 1
fi

echo "Testing login endpoint..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo -e "${GREEN}✅ Login endpoint working${NC}"
else
    echo -e "${RED}❌ Login endpoint failed${NC}"
    echo "Response: $LOGIN_RESPONSE"
    kill $APP_PID 2>/dev/null || true
    exit 1
fi

echo "Cleaning up..."
kill $APP_PID 2>/dev/null || true
sleep 2

echo "📋 Step 7: Code quality checks..."
mvn spotbugs:check -f Backend/pom.xml 2>/dev/null || print_warning "SpotBugs found issues (non-blocking)"
mvn checkstyle:check -f Backend/pom.xml 2>/dev/null || print_warning "Checkstyle found issues (non-blocking)"

echo ""
echo "🎉 CI/CD Validation Complete!"
echo "============================="
echo -e "${GREEN}✅ All critical checks passed${NC}"
echo -e "${GREEN}✅ Application is ready for deployment${NC}"
echo ""
echo "Next steps:"
echo "1. Commit your changes: git add . && git commit -m 'Add CI/CD workflows'"
echo "2. Push to trigger CI: git push origin new-workflow"
echo "3. Check GitHub Actions tab for pipeline status"
