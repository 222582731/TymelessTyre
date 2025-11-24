#!/bin/bash

# Admin User Creation Script for TymelessTyre Application
# This script provides easy commands to create admin users

BASE_URL="http://localhost:8080"

echo "🔧 TymelessTyre Admin User Creation Tool"
echo "========================================"

# Function to check admin setup status
check_admin_status() {
    echo "📊 Checking admin setup status..."
    curl -s -X GET "$BASE_URL/api/admin-setup/status" \
        -H "Content-Type: application/json" | jq '.' 2>/dev/null || echo "Failed to check status"
    echo ""
}

# Function to get setup instructions
get_instructions() {
    echo "📚 Getting setup instructions..."
    curl -s -X GET "$BASE_URL/api/admin-setup/instructions" \
        -H "Content-Type: application/json" | jq '.' 2>/dev/null || echo "Failed to get instructions"
    echo ""
}

# Function to create initial admin
create_initial_admin() {
    local name="${1:-Admin}"
    local surname="${2:-User}"
    local username="${3:-admin}"
    local email="${4:-admin@tymelesstyre.com}"
    local password="${5:-AdminPass123!}"
    local phone="${6:-0123456789}"
    
    echo "👤 Creating initial admin user..."
    echo "Name: $name $surname"
    echo "Username: $username"
    echo "Email: $email"
    echo "Phone: $phone"
    echo ""
    
    curl -X POST "$BASE_URL/api/admin-setup/create-initial" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$name\",
            \"surname\": \"$surname\",
            \"username\": \"$username\",
            \"email\": \"$email\",
            \"password\": \"$password\",
            \"phoneNumber\": \"$phone\"
        }" | jq '.' 2>/dev/null || echo "Failed to create admin"
    echo ""
}

# Function to create admin via regular registration
create_admin_via_registration() {
    local name="${1:-Admin2}"
    local surname="${2:-User2}"
    local username="${3:-admin2}"
    local email="${4:-admin2@tymelesstyre.com}"
    local password="${5:-AdminPass123!}"
    local phone="${6:-0987654321}"
    
    echo "👤 Creating admin via registration endpoint..."
    echo "Name: $name $surname"
    echo "Username: $username"
    echo "Email: $email"
    echo "Phone: $phone"
    echo ""
    
    curl -X POST "$BASE_URL/user/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$name\",
            \"surname\": \"$surname\",
            \"username\": \"$username\",
            \"email\": \"$email\",
            \"password\": \"$password\",
            \"phoneNumber\": \"$phone\",
            \"role\": \"ADMIN\",
            \"street\": \"123 Admin Street\",
            \"city\": \"Cape Town\",
            \"state\": \"Western Cape\",
            \"postalCode\": 8001,
            \"country\": \"South Africa\",
            \"addressType\": \"WORK\"
        }" | jq '.' 2>/dev/null || echo "Failed to create admin"
    echo ""
}

# Function to login and get token
login_admin() {
    local username="${1:-admin}"
    local password="${2:-AdminPass123!}"
    
    echo "🔑 Logging in as admin..."
    echo "Username: $username"
    
    response=$(curl -s -X POST "$BASE_URL/user/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$username\",
            \"password\": \"$password\"
        }")
    
    token=$(echo "$response" | jq -r '.token' 2>/dev/null)
    
    if [ "$token" != "null" ] && [ "$token" != "" ]; then
        echo "✅ Login successful!"
        echo "🎫 Token: $token"
        echo ""
        echo "💡 Use this token for authenticated requests:"
        echo "Authorization: Bearer $token"
    else
        echo "❌ Login failed!"
        echo "Response: $response"
    fi
    echo ""
}

# Main menu
show_menu() {
    echo "Available commands:"
    echo "1. check-status     - Check admin setup status"
    echo "2. instructions     - Get setup instructions"
    echo "3. create-initial   - Create initial admin user"
    echo "4. create-via-reg   - Create admin via registration"
    echo "5. login            - Login as admin"
    echo "6. help             - Show this menu"
    echo ""
}

# Command handling
case "${1:-help}" in
    "check-status")
        check_admin_status
        ;;
    "instructions")
        get_instructions
        ;;
    "create-initial")
        create_initial_admin "$2" "$3" "$4" "$5" "$6" "$7"
        ;;
    "create-via-reg")
        create_admin_via_registration "$2" "$3" "$4" "$5" "$6" "$7"
        ;;
    "login")
        login_admin "$2" "$3"
        ;;
    "help"|*)
        show_menu
        ;;
esac

echo "🏁 Done! For more options, run: $0 help"