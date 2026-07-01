param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$buildDir = Join-Path $ProjectRoot 'build\classes'

if (Test-Path $buildDir) {
    Remove-Item -Recurse -Force $buildDir
}

New-Item -ItemType Directory -Force $buildDir | Out-Null

$libJars = Get-ChildItem (Join-Path $ProjectRoot 'lib') -Filter '*.jar' | ForEach-Object { $_.FullName }
$classpath = ($libJars -join ';')
$sources = Get-ChildItem -Path (Join-Path $ProjectRoot 'src') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }

javac -encoding UTF-8 -source 1.8 -target 1.8 -cp $classpath -d $buildDir $sources
