$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$out = Join-Path $root "build\classes"
$lombok = "C:\Users\kosa\Downloads\lombok.jar"
$ojdbc = "C:\app\kosa\product\21c\dbhomeXE\jdbc\lib\ojdbc8.jar"
$sourcePath = Join-Path $root "src"
$classpath = "$sourcePath;$lombok;$ojdbc"

New-Item -ItemType Directory -Force -Path $out | Out-Null

$javaFiles = Get-ChildItem -Path $sourcePath -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp $classpath -d $out $javaFiles

java -cp "$out;$sourcePath;$ojdbc" Program
