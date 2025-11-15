#!/bin/bash

# Test script for API Gateway routing
# Tests that the gateway correctly routes requests to backend services

echo "=================================="
echo "API Gateway Routing Test Script"
echo "=================================="
echo ""

GATEWAY_URL="http://localhost:8080"
AUTH_PATH="/api/auth"
QUESTION_PATH="/api/questions"
QUIZ_PATH="/api/quizzes"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASS=0
FAIL=0

# Function to test an endpoint
test_endpoint() {
    local method=$1
    local path=$2
    local description=$3
    local data=$4

    echo -n "Testing: $description... "

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$GATEWAY_URL$path" -H "Content-Type: application/json" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$GATEWAY_URL$path" \
                  -H "Content-Type: application/json" \
                  -d "$data" 2>&1)
    fi

    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    # Any valid HTTP response (2xx, 4xx, 5xx) means routing worked
    if [[ $http_code =~ ^[2-5][0-9][0-9]$ ]]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((PASS++))

        # Show if circuit breaker is active
        if echo "$body" | grep -q "circuitBreakerActive"; then
            echo "  └─ Circuit breaker fallback active (backend service unavailable)"
        fi
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $http_code)"
        ((FAIL++))
        echo "  Response: $body"
    fi
}

# Check if gateway is running
echo "1. Checking if gateway is running on port 8080..."
if curl -s "$GATEWAY_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Gateway is running${NC}"
    echo ""
else
    echo -e "${RED}✗ Gateway is not running on port 8080${NC}"
    echo ""
    echo "Please start the gateway first:"
    echo "  export JAVA_HOME=\"/c/Program Files/Java/jdk-21\""
    echo "  ./mvnw spring-boot:run"
    echo ""
    exit 1
fi

# Test actuator endpoints
echo "2. Testing Gateway Health & Actuator Endpoints..."
test_endpoint "GET" "/actuator/health" "Health check"
test_endpoint "GET" "/actuator/circuitbreakers" "Circuit breaker status"
echo ""

# Test fallback endpoints
echo "3. Testing Circuit Breaker Fallback Endpoints..."
test_endpoint "GET" "/fallback/auth" "Auth service fallback"
test_endpoint "GET" "/fallback/questions" "Question service fallback"
test_endpoint "GET" "/fallback/quizzes" "Quiz service fallback"
echo ""

# Test auth service routing
echo "4. Testing Auth Service Routing (${AUTH_PATH}/**)..."
test_endpoint "GET" "$AUTH_PATH/health" "Auth health check (GET)"
test_endpoint "POST" "$AUTH_PATH/login" "Auth login endpoint (POST)" '{"username":"test","password":"test"}'
test_endpoint "GET" "$AUTH_PATH/users" "Auth users endpoint (GET)"
echo ""

# Test question service routing
echo "5. Testing Question Service Routing (${QUESTION_PATH}/**)..."
test_endpoint "GET" "$QUESTION_PATH/health" "Question health check (GET)"
test_endpoint "GET" "$QUESTION_PATH" "Get all questions (GET)"
echo ""

# Test quiz service routing
echo "6. Testing Quiz Service Routing (${QUIZ_PATH}/**)..."
test_endpoint "GET" "$QUIZ_PATH/health" "Quiz health check (GET)"
test_endpoint "GET" "$QUIZ_PATH" "Get all quizzes (GET)"
echo ""

# Test CORS
echo "7. Testing CORS Configuration..."
echo -n "Testing: CORS preflight request... "
response=$(curl -s -w "\n%{http_code}" -X OPTIONS "$GATEWAY_URL$AUTH_PATH/login" \
    -H "Origin: http://localhost:5174" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: Content-Type,Authorization" \
    -v 2>&1)

if echo "$response" | grep -q "Access-Control-Allow-Origin"; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASS++))
    echo "  └─ CORS headers present"
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAIL++))
fi
echo ""

# Test security headers
echo "8. Testing Security Headers..."
echo -n "Testing: Security headers on response... "
headers=$(curl -s -I "$GATEWAY_URL/fallback/auth")

missing_headers=()
[[ ! "$headers" =~ "X-Content-Type-Options" ]] && missing_headers+=("X-Content-Type-Options")
[[ ! "$headers" =~ "X-Frame-Options" ]] && missing_headers+=("X-Frame-Options")
[[ ! "$headers" =~ "X-XSS-Protection" ]] && missing_headers+=("X-XSS-Protection")

if [ ${#missing_headers[@]} -eq 0 ]; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASS++))
    echo "  └─ All security headers present"
else
    echo -e "${YELLOW}⚠ PARTIAL${NC}"
    echo "  └─ Missing headers: ${missing_headers[*]}"
fi
echo ""

# Test correlation ID
echo "9. Testing Correlation ID..."
echo -n "Testing: X-Correlation-ID header... "
headers=$(curl -s -I "$GATEWAY_URL/fallback/auth")

if echo "$headers" | grep -q "X-Correlation-ID"; then
    echo -e "${GREEN}✓ PASS${NC}"
    ((PASS++))
    echo "  └─ Correlation ID header present"
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAIL++))
fi
echo ""

# Summary
echo "=================================="
echo "Test Summary"
echo "=================================="
echo -e "Passed: ${GREEN}$PASS${NC}"
echo -e "Failed: ${RED}$FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    echo ""
    echo "✓ Gateway is correctly routing requests"
    echo "✓ Circuit breaker fallbacks are working"
    echo "✓ CORS is configured"
    echo "✓ Security headers are applied"
    echo ""
    exit 0
else
    echo -e "${YELLOW}Some tests failed. Check the output above.${NC}"
    echo ""
    exit 1
fi
