# Compile and run my3DEngine (PowerShell)
$src = "$PSScriptRoot\\src"
$bin = "$PSScriptRoot\\bin"
if (Test-Path $bin) { Remove-Item -Recurse -Force $bin }
New-Item -ItemType Directory -Path $bin | Out-Null
javac -d $bin $src\\*.java
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed"; exit 1 }
Write-Host "Starting game..."
java -cp $bin RaycastingEngine3D
