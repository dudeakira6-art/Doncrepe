param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$cp = ((Get-ChildItem (Join-Path $ProjectRoot 'lib') -Filter *.jar | ForEach-Object { $_.FullName }) -join ';')
$cp = $cp + ';' + (Join-Path $ProjectRoot 'build\classes') + ';' + (Join-Path $ProjectRoot 'build\test\classes')

& 'C:\Program Files\Java\jdk-25\bin\java.exe' -cp $cp tdd.CoberturaTDD
