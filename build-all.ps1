param(
    [string[]] $Versions = @("1.21","1.21.1","1.21.2","1.21.3","1.21.4","1.21.5","1.21.6","1.21.7","1.21.8"),
    [ValidateSet("bukkit","velocity","bungeecord")]
    [string[]] $Platforms,
    [string] $MavenExe
)

$ErrorActionPreference = 'Stop'

function Resolve-MavenExe {
    param([string] $Preferred)
    if ($Preferred) { return $Preferred }
    if (Test-Path "$PSScriptRoot/mvnw.cmd") { return "$PSScriptRoot/mvnw.cmd" }
    if (Test-Path "$PSScriptRoot/mvnw") { return "$PSScriptRoot/mvnw" }
    $candidates = @("mvn.cmd","mvn")
    foreach ($c in $candidates) {
        $found = (Get-Command $c -ErrorAction SilentlyContinue)
        if ($found) { return $found.Source }
    }
    $commonRoots = @("C:\\Program Files","C:\\Program Files (x86)")
    foreach ($root in $commonRoots) {
        $mvnBin = Join-Path $root "Apache\maven\bin\mvn.cmd"
        if (Test-Path $mvnBin) { return $mvnBin }
        $matches = Get-ChildItem -Path $root -Directory -Filter "apache-maven*" -ErrorAction SilentlyContinue
        foreach ($dir in $matches) {
            $candidate = Join-Path $dir.FullName "bin\mvn.cmd"
            if (Test-Path $candidate) { return $candidate }
        }
    }
    throw "Maven non trovato. Installa Maven e aggiungilo al PATH, oppure usa -MavenExe per specificare il percorso (es. 'C:\\Program Files\\apache-maven-3.9.7\\bin\\mvn.cmd')."
}

function Invoke-Maven {
    param(
        [string] $WorkingDirectory,
        [hashtable] $Properties
    )

    $exe = Resolve-MavenExe -Preferred $MavenExe
    $propArgs = @()
    foreach ($k in $Properties.Keys) {
        $propArgs += ("-D{0}={1}" -f $k,$Properties[$k])
    }

    Push-Location $WorkingDirectory
    try {
        $output = & $exe clean package @propArgs 2>&1
        $exitCode = $LASTEXITCODE
        $warns = $output | Where-Object { $_ -match '^\[WARNING\]' -or $_ -match '^\[WARN\]' }
        $errors = $output | Where-Object { $_ -match '^\[ERROR\]' }
        return @{ Success = ($exitCode -eq 0 -and $errors.Count -eq 0); Output = $output; Warnings = $warns; Errors = $errors }
    } finally {
        Pop-Location
    }
}

if (-not $Platforms -or $Platforms.Count -eq 0) {
    Write-Host "Seleziona le piattaforme da compilare (separa con virgola): bukkit, velocity, bungeecord" -ForegroundColor Yellow
    $input = Read-Host "Piattaforme"
    $Platforms = $input.Split(",") | ForEach-Object { $_.Trim().ToLower() } | Where-Object { $_ -in @("bukkit","velocity","bungeecord") }
    if ($Platforms.Count -eq 0) { throw "Nessuna piattaforma valida selezionata." }
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$outDir = Join-Path $root "build\output"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

foreach ($v in $Versions) {
    Write-Host ("=== Building for Minecraft {0} ===" -f $v) -ForegroundColor Cyan

    if ($Platforms -contains "bukkit") {
        $spigotVer = if ($v -eq "1.21") { "1.21-R0.1-SNAPSHOT" } else { "$v-R0.1-SNAPSHOT" }
        $result = Invoke-Maven -WorkingDirectory (Join-Path $root "bukkit") -Properties @{ "spigotApiVersion" = $spigotVer }

        Write-Host "[LOGS]" -ForegroundColor Gray
        $result.Output | ForEach-Object { Write-Host $_ }
        Write-Host ""  # riga vuota

        $bukkitJar = Get-ChildItem -Path (Join-Path $root "bukkit\target") -Filter "*.jar" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($result.Success -and $bukkitJar) {
            $bukkitOut = Join-Path $outDir ("$v\bukkit")
            New-Item -ItemType Directory -Force -Path $bukkitOut | Out-Null
            $dest = Join-Path $bukkitOut ("EchoCore-Bukkit-$v.jar")
            Copy-Item $bukkitJar.FullName $dest -Force
            Write-Host ("[SUCCESS] Bukkit {0} -> {1}. Warn: {2}, Errori: {3}" -f $v,$dest,$result.Warnings.Count,$result.Errors.Count) -ForegroundColor Green
        } else {
            $lastErrors = $result.Errors | Select-Object -Last 5
            if (-not $lastErrors -or $lastErrors.Count -eq 0) { $lastErrors = @("Compilazione fallita.") }
            Write-Host ("[FAIL] Bukkit {0}. Errori: {1}" -f $v,$result.Errors.Count) -ForegroundColor Red
            $lastErrors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
    }

    if ($Platforms -contains "velocity") {
        $result = Invoke-Maven -WorkingDirectory (Join-Path $root "velocity") -Properties @{}

        Write-Host "[LOGS]" -ForegroundColor Gray
        $result.Output | ForEach-Object { Write-Host $_ }
        Write-Host ""

        $velocityJar = Get-ChildItem -Path (Join-Path $root "velocity\target") -Filter "*.jar" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($result.Success -and $velocityJar) {
            $velocityOut = Join-Path $outDir ("$v\velocity")
            New-Item -ItemType Directory -Force -Path $velocityOut | Out-Null
            $dest = Join-Path $velocityOut ("EchoCore-Velocity-$v.jar")
            Copy-Item $velocityJar.FullName $dest -Force
            Write-Host ("[SUCCESS] Velocity {0} -> {1}. Warn: {2}, Errori: {3}" -f $v,$dest,$result.Warnings.Count,$result.Errors.Count) -ForegroundColor Green
        } else {
            $lastErrors = $result.Errors | Select-Object -Last 5
            if (-not $lastErrors -or $lastErrors.Count -eq 0) { $lastErrors = @("Compilazione fallita.") }
            Write-Host ("[FAIL] Velocity {0}. Errori: {1}" -f $v,$result.Errors.Count) -ForegroundColor Red
            $lastErrors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
    }

    if ($Platforms -contains "bungeecord") {
        $result = Invoke-Maven -WorkingDirectory (Join-Path $root "bungeecord") -Properties @{}

        Write-Host "[LOGS]" -ForegroundColor Gray
        $result.Output | ForEach-Object { Write-Host $_ }
        Write-Host ""

        $bungeeJar = Get-ChildItem -Path (Join-Path $root "bungeecord\target") -Filter "*.jar" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($result.Success -and $bungeeJar) {
            $bungeeOut = Join-Path $outDir ("$v\bungeecord")
            New-Item -ItemType Directory -Force -Path $bungeeOut | Out-Null
            $dest = Join-Path $bungeeOut ("EchoCore-BungeeCord-$v.jar")
            Copy-Item $bungeeJar.FullName $dest -Force
            Write-Host ("[SUCCESS] BungeeCord {0} -> {1}. Warn: {2}, Errori: {3}" -f $v,$dest,$result.Warnings.Count,$result.Errors.Count) -ForegroundColor Green
        } else {
            $lastErrors = $result.Errors | Select-Object -Last 5
            if (-not $lastErrors -or $lastErrors.Count -eq 0) { $lastErrors = @("Compilazione fallita.") }
            Write-Host ("[FAIL] BungeeCord {0}. Errori: {1}" -f $v,$result.Errors.Count) -ForegroundColor Red
            $lastErrors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        }
    }
}

Write-Host ("Compilazione completata. Output in {0}" -f $outDir) -ForegroundColor Green
