param(
    [Parameter(Mandatory = $false)]
    [ValidateSet("status", "commit", "log", "pull", "push", "tag")]
    [string]$Action = "status",

    [Parameter(Mandatory = $false)]
    [string]$Message = "Actualiza proyecto DonCrepePOS",

    [Parameter(Mandatory = $false)]
    [string]$Tag = "v1.0"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "Git no esta instalado o no esta en PATH."
}

switch ($Action) {
    "status" {
        git status
    }
    "commit" {
        git add .
        git commit -m $Message
    }
    "log" {
        git log --oneline --graph --decorate
    }
    "pull" {
        git pull origin main
    }
    "push" {
        git push origin main
    }
    "tag" {
        git tag -a $Tag -m "Lanzamiento $Tag"
        git push origin $Tag
    }
}
