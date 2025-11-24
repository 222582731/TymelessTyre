# Admin User Creation Script for TymelessTyre Application (PowerShell)
# This script provides easy commands to create admin users

param(
    [Parameter(Position=0)]
    [string]$Command = "help",
    
    [Parameter(Position=1)]
    [string]$Name = "Admin",
    
    [Parameter(Position=2)]
    [string]$Surname = "User",
    
    [Parameter(Position=3)]
    [string]$Username = "admin",
    
    [Parameter(Position=4)]
    [string]$Email = "admin@tymelesstyre.com",
    
    [Parameter(Position=5)]
    [SecureString]$Password = (Read-Host "Enter admin password" -AsSecureString),
    
    [Parameter(Position=6)]
    [string]$Phone = "0123456789"
)

$BaseUrl = "http://localhost:8080"

Write-Host "🔧 TymelessTyre Admin User Creation Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

function Get-AdminStatus {
    Write-Host "📊 Checking admin setup status..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/admin-setup/status" -Method GET -ContentType "application/json"
        $response | ConvertTo-Json -Depth 3
    }
    catch {
        Write-Host "❌ Failed to check status: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Function to get setup instructions
function Get-Instructions {
    Write-Host "📚 Getting setup instructions..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/admin-setup/instructions" -Method GET -ContentType "application/json"
        $response | ConvertTo-Json -Depth 5
    }
    catch {
        Write-Host "❌ Failed to get instructions: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

function New-InitialAdmin {
    param($Name, $Surname, $Username, $Email, $Password, $Phone)
    
    Write-Host "👤 Creating initial admin user..." -ForegroundColor Green
    Write-Host "Name: $Name $Surname" -ForegroundColor Gray
    Write-Host "Username: $Username" -ForegroundColor Gray
    Write-Host "Email: $Email" -ForegroundColor Gray
    Write-Host "Phone: $Phone" -ForegroundColor Gray
    Write-Host ""
    
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
    $body = @{ 
        name = $Name
        surname = $Surname
        username = $Username
        email = $Email
        password = $plainPassword
        phoneNumber = $Phone
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/admin-setup/create-initial" -Method POST -ContentType "application/json" -Body $body
        Write-Host "✅ Admin created successfully!" -ForegroundColor Green
        $response | ConvertTo-Json -Depth 3
    }
    catch {
        Write-Host "❌ Failed to create admin: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorResponse = $reader.ReadToEnd()
            Write-Host "Error details: $errorResponse" -ForegroundColor Red
        }
    }
    Write-Host ""
}

function New-AdminViaRegistration {
    param($Name, $Surname, $Username, $Email, $Password, $Phone)
    
    Write-Host "👤 Creating admin via registration endpoint..." -ForegroundColor Green
    Write-Host "Name: $Name $Surname" -ForegroundColor Gray
    Write-Host "Username: $Username" -ForegroundColor Gray
    Write-Host "Email: $Email" -ForegroundColor Gray
    Write-Host "Phone: $Phone" -ForegroundColor Gray
    Write-Host ""
    
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
    $body = @{
        name = $Name
        surname = $Surname
        username = $Username
        email = $Email
        password = $plainPassword
        phoneNumber = $Phone
        role = "ADMIN"
        street = "123 Admin Street"
        city = "Cape Town"
        state = "Western Cape"
        postalCode = 8001
        country = "South Africa"
        addressType = "WORK"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/user/register" -Method POST -ContentType "application/json" -Body $body
        Write-Host "✅ Admin created successfully!" -ForegroundColor Green
        $response | ConvertTo-Json -Depth 3
    }
    catch {
        Write-Host "❌ Failed to create admin: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorResponse = $reader.ReadToEnd()
            Write-Host "Error details: $errorResponse" -ForegroundColor Red
        }
    }
    Write-Host ""
}

function Get-AdminCredential {
    param($Username, $Password)
    
    Write-Host "🔑 Logging in as admin..." -ForegroundColor Blue
    Write-Host "Username: $Username" -ForegroundColor Gray
    
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
    $body = @{
        username = $Username
        password = $plainPassword
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/user/login" -Method POST -ContentType "application/json" -Body $body
        
        if ($response.token) {
            Write-Host "✅ Login successful!" -ForegroundColor Green
            Write-Host "🎫 Token: $($response.token)" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "💡 Use this token for authenticated requests:" -ForegroundColor Cyan
            Write-Host "Authorization: Bearer $($response.token)" -ForegroundColor Gray
        } else {
            Write-Host "❌ Login failed - no token received" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorResponse = $reader.ReadToEnd()
            Write-Host "Error details: $errorResponse" -ForegroundColor Red
        }
    }
    Write-Host ""
}

# Function to show menu
function Show-AdminMenu {
    Write-Host "Available commands:" -ForegroundColor Cyan
    Write-Host "check-status     - Check admin setup status" -ForegroundColor White
    Write-Host "instructions     - Get setup instructions" -ForegroundColor White
    Write-Host "create-initial   - Create initial admin user" -ForegroundColor White
    Write-Host "create-via-reg   - Create admin via registration" -ForegroundColor White
    Write-Host "login            - Login as admin" -ForegroundColor White
    Write-Host "help             - Show this menu" -ForegroundColor White
    Write-Host ""
    Write-Host "Usage examples:" -ForegroundColor Yellow
    Write-Host ".\create-admin.ps1 create-initial" -ForegroundColor Gray
    Write-Host ".\create-admin.ps1 create-initial 'John' 'Doe' 'john.admin' 'john@company.com' 'SecurePass123!' '0123456789'" -ForegroundColor Gray
    Write-Host ".\create-admin.ps1 login" -ForegroundColor Gray
    Write-Host ".\create-admin.ps1 check-status" -ForegroundColor Gray
    Write-Host ""
}

# Command execution
switch ($Command.ToLower()) {
    "check-status" {
        Get-AdminStatus
    }
    "instructions" {
        Get-Instructions
    }
    "create-initial" {
        New-InitialAdmin -Name $Name -Surname $Surname -Username $Username -Email $Email -Password $Password -Phone $Phone
    }
    "create-via-reg" {
        New-AdminViaRegistration -Name $Name -Surname $Surname -Username $Username -Email $Email -Password $Password -Phone $Phone
    }
    "login" {
        Get-AdminCredential -Username $Username -Password $Password
    }
    default {
        Show-AdminMenu
    }
}

Write-Host "🏁 Done! For more options, run: .\create-admin.ps1 help" -ForegroundColor Green