# Copy this file to run-supabase.ps1 and fill in local secrets there.
# Do not commit run-supabase.ps1.

$ErrorActionPreference = "Stop"

# Supabase Postgres. Use the direct JDBC URL when IPv6 works locally, or the
# Session Pooler URL when you need IPv4 compatibility. Keep sslmode=require.
$env:DB_URL = "jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME = "postgres.<project-ref>"
$env:DB_PASSWORD = "<database-password>"

# Spring Boot JWT signing key for local runtime only.
$env:JWT_SECRET = "<local-jwt-secret-at-least-32-characters>"

# Storage. Use "local" if you only need database/API smoke tests.
$env:STORAGE_PROVIDER = "supabase"
$env:SUPABASE_URL = "https://<project-ref>.supabase.co"
$env:SUPABASE_SERVICE_ROLE_KEY = "<service-role-key>"

$placeholderValues = @(
    $env:DB_URL,
    $env:DB_USERNAME,
    $env:DB_PASSWORD,
    $env:JWT_SECRET,
    $env:SUPABASE_URL,
    $env:SUPABASE_SERVICE_ROLE_KEY
)

if ($placeholderValues | Where-Object { $_ -match "<[^>]+>" }) {
    throw "Fill local values in run-supabase.ps1 before starting the backend."
}

Push-Location $PSScriptRoot
try {
    mvn spring-boot:run
}
finally {
    Pop-Location
}
