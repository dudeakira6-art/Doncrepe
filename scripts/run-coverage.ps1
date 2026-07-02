param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$cp = ((Get-ChildItem (Join-Path $ProjectRoot 'lib') -Filter *.jar | ForEach-Object { $_.FullName }) -join ';')
$cp = $cp + ';' + (Join-Path $ProjectRoot 'build\classes') + ';' + (Join-Path $ProjectRoot 'build\test\classes')

java -cp $cp tdd.PruebasTDD
