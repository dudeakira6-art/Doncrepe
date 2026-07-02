param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$buildClasses = Join-Path $ProjectRoot 'build\classes'
$testClasses = Join-Path $ProjectRoot 'build\test\classes'

foreach ($dir in @($buildClasses, $testClasses)) {
    if (Test-Path $dir) {
        Remove-Item -Recurse -Force $dir
    }
    New-Item -ItemType Directory -Force $dir | Out-Null
}

$cp = ((Get-ChildItem (Join-Path $ProjectRoot 'lib') -Filter *.jar | ForEach-Object { $_.FullName }) -join ';')
$sources = Get-ChildItem -Path (Join-Path $ProjectRoot 'src') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
$tests = Get-ChildItem -Path (Join-Path $ProjectRoot 'test') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }

javac -encoding UTF-8 -source 1.8 -target 1.8 -cp $cp -d $buildClasses $sources
javac -encoding UTF-8 -source 1.8 -target 1.8 -cp ($cp + ';' + $buildClasses) -d $testClasses $tests
