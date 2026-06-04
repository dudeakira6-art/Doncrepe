Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "Git no esta instalado o no esta en PATH. Instale Git for Windows y vuelva a ejecutar este script."
}

git init
git branch -M main
git add .
git commit -m "Version inicial del sistema DonCrepePOS"

Write-Host "Repositorio Git inicializado en $projectRoot"
