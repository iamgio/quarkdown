#Requires -Version 5.1

<#
.SYNOPSIS
Installs chrome-headless-shell, the headless browser Quarkdown requires for PDF export,
at the version pinned by the chrome.properties file shipped with this distribution,
verifying the downloaded archive against its pinned SHA-256 checksum.

.DESCRIPTION
The browser is extracted to <TargetDir>\chrome-headless-shell-win64\,
and the absolute path of its executable is written to the output stream
(status messages go to the host), to be assigned to the QD_CHROME_PATH
environment variable or the --chrome-path option.

.PARAMETER TargetDir
Directory to install the browser into.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$TargetDir
)

$ErrorActionPreference = "Stop"

$PropertiesFile = Join-Path $PSScriptRoot "chrome.properties"
if (-not (Test-Path $PropertiesFile)) {
    Write-Error "Browser properties file not found at $PropertiesFile"
}

# Reads a `key=value` property from the properties file.
function Get-ChromeProperty {
    param([string]$Key)

    $Line = Get-Content $PropertiesFile | Where-Object { $_ -match "^$Key=" } | Select-Object -First 1
    if (-not $Line) {
        Write-Error "Property '$Key' not found in $PropertiesFile"
    }
    return ($Line -split '=', 2)[1].Trim()
}

$ChromeVersion = Get-ChromeProperty -Key "version"
$ExpectedSha = Get-ChromeProperty -Key "win64"

Write-Host "Installing chrome-headless-shell $ChromeVersion (required for PDF export)..."

$TmpZip = Join-Path ([System.IO.Path]::GetTempPath()) ([System.Guid]::NewGuid().ToString() + ".zip")
try {
    Invoke-WebRequest `
        -Uri "https://storage.googleapis.com/chrome-for-testing-public/$ChromeVersion/win64/chrome-headless-shell-win64.zip" `
        -OutFile $TmpZip -UseBasicParsing

    # Verify the archive against the pinned checksum before extracting it.
    $ActualSha = (Get-FileHash -Path $TmpZip -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($ActualSha -ne $ExpectedSha.ToLowerInvariant()) {
        Write-Error "SHA-256 mismatch for the downloaded browser archive. Expected: $ExpectedSha, actual: $ActualSha"
    }

    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
    Expand-Archive -Path $TmpZip -DestinationPath $TargetDir -Force
}
finally {
    if (Test-Path $TmpZip) {
        Remove-Item -Path $TmpZip -Force -ErrorAction SilentlyContinue
    }
}

$BrowserPath = Join-Path (Resolve-Path $TargetDir) "chrome-headless-shell-win64\chrome-headless-shell.exe"
if (-not (Test-Path $BrowserPath)) {
    Write-Error "Browser executable not found at $BrowserPath after extraction"
}

Write-Output $BrowserPath
