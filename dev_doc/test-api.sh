#!/bin/bash
# CatalogForge Backend API Test Script
# Testet alle Endpunkte gegen einen laufenden Server

BASE_URL="${BASE_URL:-http://localhost:8080}"
API="$BASE_URL/api/v1"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Counters
PASSED=0
FAILED=0

# Helper functions
print_header() {
    echo ""
    echo -e "${BOLD}${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BOLD}${BLUE}  $1${NC}"
    echo -e "${BOLD}${BLUE}═══════════════════════════════════════════════════════════════${NC}"
}

print_test() {
    echo ""
    echo -e "${CYAN}▶ $1${NC}"
    echo -e "${YELLOW}  $2${NC}"
}

print_success() {
    echo -e "${GREEN}  ✓ $1${NC}"
    PASSED=$((PASSED + 1))
}

print_error() {
    echo -e "${RED}  ✗ $1${NC}"
    FAILED=$((FAILED + 1))
}

print_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.' 2>/dev/null || echo "$1"
    else
        echo "$1"
    fi
}

# Test function
test_endpoint() {
    local method="$1"
    local endpoint="$2"
    local description="$3"
    local data="$4"
    local expected_status="${5:-200}"
    
    print_test "$description" "$method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$API$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$API$endpoint")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$API$endpoint")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$API$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        print_success "Status: $http_code"
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo -e "${NC}  Response (truncated):"
            print_json "$body" | head -20 | sed 's/^/    /'
            lines=$(echo "$body" | wc -l)
            if [ "$lines" -gt 20 ]; then
                echo -e "    ${YELLOW}... ($(($lines - 20)) more lines)${NC}"
            fi
        fi
    else
        print_error "Expected $expected_status, got $http_code"
        if [ -n "$body" ]; then
            echo -e "${RED}  Response:${NC}"
            print_json "$body" | head -10 | sed 's/^/    /'
        fi
    fi
}

# Check if server is running
check_server() {
    print_header "Server Check"
    echo -e "${CYAN}Checking if server is running at $BASE_URL...${NC}"
    
    if curl -s --connect-timeout 5 "$BASE_URL" > /dev/null 2>&1; then
        print_success "Server is running"
    else
        print_error "Server is not reachable at $BASE_URL"
        echo -e "${YELLOW}Start the server with: ./gradlew bootRun${NC}"
        exit 1
    fi
}

# ============================================================================
# TESTS
# ============================================================================

check_server

# --- Products ---
print_header "Products API"

test_endpoint "GET" "/products" "List all products"
test_endpoint "GET" "/products?page=0&size=2" "List products (paginated)"
test_endpoint "GET" "/products/1" "Get product by ID"
test_endpoint "GET" "/products/999" "Get non-existent product" "" "404"
test_endpoint "GET" "/products/categories" "List all categories"
test_endpoint "GET" "/products/series" "List all series"
test_endpoint "GET" "/products?category=Trucks" "Filter by category"
test_endpoint "GET" "/products/search?q=actros" "Search products"

# --- Skills ---
print_header "Skills API"

test_endpoint "GET" "/skills" "List all skills"
test_endpoint "GET" "/skills/core" "List core skills"
test_endpoint "GET" "/skills/styles" "List style skills"
test_endpoint "GET" "/skills/formats" "List format skills"
test_endpoint "GET" "/skills/prompts/examples" "Get example prompts"

# --- Layouts (ohne Gemini) ---
print_header "Layouts API (Read Operations)"

# Erstelle ein Test-Layout für weitere Tests
echo -e "${CYAN}Note: POST /layouts/generate/* requires GEMINI_API_KEY${NC}"

test_endpoint "GET" "/layouts/nonexistent-id" "Get non-existent layout" "" "404"

# --- Images ---
print_header "Images API"

test_endpoint "GET" "/images/nonexistent-id" "Get non-existent image" "" "404"

# --- PDF ---
print_header "PDF API"

test_endpoint "GET" "/pdf/nonexistent-id/download" "Download non-existent PDF" "" "404"

# --- Error Handling ---
print_header "Error Handling"

test_endpoint "GET" "/nonexistent" "Non-existent endpoint" "" "404"
test_endpoint "POST" "/products" "Invalid method on products" "" "405"

# ============================================================================
# SUMMARY
# ============================================================================

print_header "Test Summary"

TOTAL=$((PASSED + FAILED))
echo ""
echo -e "${GREEN}  Passed: $PASSED${NC}"
echo -e "${RED}  Failed: $FAILED${NC}"
echo -e "${BOLD}  Total:  $TOTAL${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}${BOLD}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}${BOLD}Some tests failed!${NC}"
    exit 1
fi
