#Requires -Version 7.0
<#
.SYNOPSIS
Creates a Windows application image or MSI from the verified Maven fat JAR.
.EXAMPLE
mvn -B -ntp clean verify
./scripts/Package-Windows.ps1
.EXAMPLE
./scripts/Package-Windows.ps1 -PackageType app-image
#>
[CmdletBinding()]
param(
    [string] $Version,
    [ValidateSet('msi', 'app-image')]
    [string] $PackageType = 'msi'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
if (-not $IsWindows) {
    throw 'Windows packaging must run on Windows.'
}

$projectRoot = Split-Path -Parent $PSScriptRoot
[xml] $pom = Get-Content -LiteralPath (Join-Path $projectRoot 'pom.xml') -Raw
$projectVersion = [string] $pom.project.version
if ([string]::IsNullOrWhiteSpace($Version)) {
    $Version = $projectVersion
}
if ($Version -cne $projectVersion) {
    throw "Release version '$Version' must match pom.xml version '$projectVersion'."
}
# MSI ProductVersion has three numeric fields with these upper bounds.
if ($Version -cnotmatch '^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$') {
    throw 'Use a stable numeric version X.Y.Z, without a v prefix or SNAPSHOT suffix.'
}
$parts = $Version.Split('.')
if ([decimal] $parts[0] -gt 255 -or [decimal] $parts[1] -gt 255 -or [decimal] $parts[2] -gt 65535) {
    throw 'Windows MSI version must be within 255.255.65535.'
}

$jdkBin = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin' } else { $null }
$jpackage = if ($jdkBin) {
    (Get-Command (Join-Path $jdkBin 'jpackage.exe') -ErrorAction Stop).Source
} else {
    (Get-Command jpackage.exe -ErrorAction Stop).Source
}
$jpackageVersion = & $jpackage --version
if ($LASTEXITCODE -ne 0) { throw 'Cannot determine jpackage version.' }
Write-Host "Packaging JSA $Version with jpackage $jpackageVersion"

if ($PackageType -eq 'msi') {
    # GitHub windows-2022 includes WiX 3.14; local users may also use WiX 4/5.
    if (-not (Get-Command wix.exe -ErrorAction SilentlyContinue) -and
        -not ((Get-Command candle.exe -ErrorAction SilentlyContinue) -and
              (Get-Command light.exe -ErrorAction SilentlyContinue))) {
        if ($env:WIX) {
            $candidate = Join-Path $env:WIX 'bin'
            if (Test-Path -LiteralPath (Join-Path $candidate 'candle.exe')) {
                $env:PATH = "$candidate;$env:PATH"
            }
        }
        if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
            -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
            throw 'MSI requires WiX on PATH (wix.exe, or candle.exe and light.exe). Use -PackageType app-image to check packaging without WiX.'
        }
    }
}

$jarName = "$($pom.project.artifactId)-$Version-all.jar"
$jarPath = Join-Path $projectRoot "target/$jarName"
if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw "Missing $jarPath. Run mvn -B -ntp clean verify first."
}
$iconPath = Join-Path $projectRoot 'src/main/resources/icons/AppIcon.ico'
if (-not (Test-Path -LiteralPath $iconPath -PathType Leaf)) {
    throw "Missing icon: $iconPath"
}

# A separate staging folder per run avoids stale dependencies and recursive deletion.
$stage = Join-Path $projectRoot ("target/jpackage/" + [guid]::NewGuid().ToString('N'))
$inputDirectory = Join-Path $stage 'input'
$imageDirectory = Join-Path $stage 'image'
New-Item -ItemType Directory -Path $inputDirectory, $imageDirectory | Out-Null
Copy-Item -LiteralPath $jarPath -Destination (Join-Path $inputDirectory 'JSA.jar')
$imageArgs = @(
    '--type', 'app-image',
    '--name', 'JSA',
    '--app-version', $Version,
    '--input', $inputDirectory,
    '--main-jar', 'JSA.jar',
    '--main-class', 'com.mescode.japanese.app.launch.Main',
    '--icon', $iconPath,
    # vendor - đặt tên nhà phát triển (publisher) của app
    '--vendor', 'MES Code',
    '--description', 'JSA - Japanese Studying App',
    '--dest', $imageDirectory
)
& $jpackage @imageArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage app-image failed with exit code $LASTEXITCODE." }

$appImage = Join-Path $imageDirectory 'JSA'
foreach ($relativePath in @('JSA.exe', 'app/JSA.jar', 'app/JSA.cfg', 'runtime/bin/server/jvm.dll')) {
    if (-not (Test-Path -LiteralPath (Join-Path $appImage $relativePath) -PathType Leaf)) {
        throw "Application image is incomplete: $relativePath"
    }
}
Write-Host "Application image: $appImage"
if ($PackageType -eq 'app-image') {
    return
}

$outputDirectory = Join-Path $projectRoot 'target/windows-release'
$installerName = "JSA-$Version-windows-x64.msi"
$installerPath = Join-Path $outputDirectory $installerName
if ((Test-Path -LiteralPath $installerPath) -or
    (Test-Path -LiteralPath "$installerPath.sha256")) {
    throw "Release output already exists: $installerPath. Run mvn clean verify before rebuilding."
}
$msiDirectory = Join-Path $stage 'msi'
New-Item -ItemType Directory -Path $msiDirectory | Out-Null
$msiArgs = @(
    '--type', 'msi',
    '--name', 'JSA',
    '--app-version', $Version,
    '--app-image', $appImage,
    '--vendor', 'MES Code',
    '--description', 'JSA - Japanese Studying App',
    '--dest', $msiDirectory,
    # Tùy chọn này yêu cầu cài cho tài khoản Windows hiện tại.
    # Bộ cài sẽ mặc định cài các tệp cài đặt của app vào:
    # C:\Users\<tên tài khoản>\AppData\Local\JSA
    '--win-per-user-install',
    # --win-dir-chooser, bật màn hình cho người dùng chọn thư mục cài đặt app
    '--win-dir-chooser',
    '--win-menu',
    '--win-menu-group', 'JSA',
    '--win-shortcut',
    # Keep this UUID unchanged across future releases so Windows recognizes upgrades.
    '--win-upgrade-uuid', 'ab783c07-52e9-4a54-9a4f-f249268d965f'
)
& $jpackage @msiArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage MSI failed with exit code $LASTEXITCODE." }
$installers = @(Get-ChildItem -LiteralPath $msiDirectory -Filter '*.msi' -File)
if ($installers.Count -ne 1 -or $installers[0].Length -eq 0) {
    throw 'Expected exactly one nonempty MSI installer.'
}
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
Copy-Item -LiteralPath $installers[0].FullName -Destination $installerPath
$checksum = (Get-FileHash -LiteralPath $installerPath -Algorithm SHA256).Hash.ToLowerInvariant()
Set-Content -LiteralPath "$installerPath.sha256" -Value "$checksum  $installerName" -Encoding utf8
Write-Host "Installer: $installerPath"
