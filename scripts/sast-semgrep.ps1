param(
    [Parameter(Mandatory = $false)]
    [string[]]$Configs = @("p/java", "p/security-audit"),

    [Parameter(Mandatory = $false)]
    [string[]]$Targets = @("src", "test"),

    [Parameter(Mandatory = $false)]
    [string]$OutputDir = "reportes\sast"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not (Get-Command semgrep -ErrorAction SilentlyContinue)) {
    Write-Error "Semgrep no esta instalado o no esta en PATH. Instale Semgrep y vuelva a ejecutar este script."
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$jsonReport = Join-Path $OutputDir "semgrep-$timestamp.json"
$txtReport = Join-Path $OutputDir "semgrep-$timestamp.txt"

$args = @("scan", "--metrics=off", "--json", "--output", $jsonReport)
foreach ($config in $Configs) {
    $args += @("--config", $config)
}
$args += $Targets

Write-Host "Ejecutando Semgrep en $projectRoot..."
& semgrep @args | Out-Null
$exitCode = $LASTEXITCODE

if (-not (Test-Path $jsonReport)) {
    throw "Semgrep no genero el reporte JSON esperado en $jsonReport."
}

$json = Get-Content -Raw $jsonReport | ConvertFrom-Json
$results = @($json.results)

$lineas = New-Object System.Collections.Generic.List[string]
$lineas.Add("Reporte SAST - Semgrep")
$lineas.Add("Proyecto: DonCrepePOS")
$lineas.Add("Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
$lineas.Add("Rutas analizadas: $($Targets -join ', ')")
$lineas.Add("Configs usadas: $($Configs -join ', ')")
$lineas.Add("Findings: $($results.Count)")
$lineas.Add("")

if ($results.Count -gt 0) {
    $lineas.Add("Hallazgos:")
    foreach ($item in $results) {
        $sev = if ($item.extra.severity) { $item.extra.severity } else { "unknown" }
        $mensaje = if ($item.extra.message) { $item.extra.message } else { "" }
        $lineas.Add("- [$sev] $($item.path):$($item.start.line) $($item.check_id)")
        if ($mensaje) {
            $lineas.Add("  $mensaje")
        }
    }
} else {
    $lineas.Add("No se detectaron hallazgos con las reglas seleccionadas.")
}

$lineas | Set-Content -Path $txtReport -Encoding UTF8

Write-Host ""
Write-Host "Reporte JSON : $jsonReport"
Write-Host "Reporte texto: $txtReport"
Write-Host "Hallazgos    : $($results.Count)"

if ($exitCode -eq 2) {
    throw "Semgrep termino con error. Revise el reporte."
}

exit $exitCode
