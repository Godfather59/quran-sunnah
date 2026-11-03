<#
.SYNOPSIS
  Captures frame timing around physical tab swipes on devices that block adb input.

.DESCRIPTION
  Resets app gfxinfo, waits while the user physically swipes, then saves the
  recent frame history and aggregate Android frame counters. Does not install
  or launch the app.
#>
[CmdletBinding()]
param(
    [string] $Serial,
    [string] $PackageName = "com.godfather59.quransunnah",
    [string] $OutputDirectory
)

$ErrorActionPreference = "Stop"

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCommand) {
    $adbPath = $adbCommand.Source
} else {
    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
    if ($env:LOCALAPPDATA) { $sdkRoots += (Join-Path $env:LOCALAPPDATA "Android\Sdk") }
    $sdkAdb = @($sdkRoots | Where-Object { $_ } | ForEach-Object {
        Join-Path $_ "platform-tools\adb.exe"
    } | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1)
    if ($sdkAdb.Count -eq 0) { throw "adb was not found on PATH or in the standard Windows Android SDK locations." }
    $adbPath = $sdkAdb[0]
}

$devices = @(& $adbPath devices 2>&1)
if ($LASTEXITCODE -ne 0) { throw "Could not query adb devices: $($devices -join ' ')" }
$connected = @($devices | Select-Object -Skip 1 | ForEach-Object {
    if ($_ -match '^\s*(\S+)\s+device\s*$') { $Matches[1] }
})
if ([string]::IsNullOrWhiteSpace($Serial)) {
    if ($connected.Count -ne 1) { throw "Expected one adb device, found $($connected.Count). Pass -Serial from 'adb devices'." }
    $Serial = $connected[0]
} elseif ($connected -notcontains $Serial) {
    throw "Device '$Serial' is not connected and authorized in adb devices."
}

$process = & $adbPath -s $Serial shell pidof $PackageName
if ($LASTEXITCODE -ne 0 -or -not ($process -join " ").Trim()) {
    throw "The app ($PackageName) is not running. Open it before starting capture."
}

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $OutputDirectory = Join-Path (Get-Location) "artifacts/tab-swipe-benchmarks/manual-$stamp"
}
$null = New-Item -ItemType Directory -Path $OutputDirectory -Force

$null = & $adbPath -s $Serial shell dumpsys gfxinfo $PackageName reset
if ($LASTEXITCODE -ne 0) { throw "Could not reset gfxinfo counters." }
Write-Host "Frame counters reset for $PackageName on $Serial."
Write-Host "Physically swipe across Home, Quran, and Sunnah several times. Keep the swipes moving, then return here and press Enter."
$null = Read-Host "Press Enter when the swipes are complete"

$frames = & $adbPath -s $Serial shell dumpsys gfxinfo $PackageName framestats
if ($LASTEXITCODE -ne 0) { throw "Could not read gfxinfo framestats." }
$frames | Set-Content -LiteralPath (Join-Path $OutputDirectory "framestats.txt") -Encoding UTF8
$gfx = & $adbPath -s $Serial shell dumpsys gfxinfo $PackageName
if ($LASTEXITCODE -ne 0) { throw "Could not read gfxinfo summary." }
$gfx | Set-Content -LiteralPath (Join-Path $OutputDirectory "gfxinfo-summary.txt") -Encoding UTF8

$durations = [System.Collections.Generic.List[double]]::new()
foreach ($line in $frames) {
    $trimmed = ([string]$line).Trim()
    if ($trimmed -notmatch '^\d+,') { continue }
    $fields = $trimmed.Split(',')
    # gfxinfo framestats CSV: IntendedVsync is column 2 and FrameCompleted is column 17.
    if ($fields.Count -le 17) { continue }
    $intendedVsync = 0L; $frameCompleted = 0L
    if ([long]::TryParse($fields[2], [ref]$intendedVsync) -and
        [long]::TryParse($fields[17], [ref]$frameCompleted) -and
        $intendedVsync -gt 0 -and $frameCompleted -ge $intendedVsync) {
        $durations.Add(($frameCompleted - $intendedVsync) / 1000000.0)
    }
}

$ordered = @($durations | Sort-Object)
function Get-FramePercentile {
    param([double] $Percentile)
    if ($ordered.Count -eq 0) { return 0.0 }
    $idx = [Math]::Max(0, [Math]::Min([Math]::Ceiling($Percentile * $ordered.Count) - 1, $ordered.Count - 1))
    return [double]$ordered[$idx]
}
$report = [pscustomobject]@{
    Device = $Serial
    Package = $PackageName
    CaptureMethod = "Physical user swipes"
    FrameSamples = $durations.Count
    FrameDurationP50Ms = [Math]::Round((Get-FramePercentile 0.50), 1)
    FrameDurationP90Ms = [Math]::Round((Get-FramePercentile 0.90), 1)
    FrameDurationP95Ms = [Math]::Round((Get-FramePercentile 0.95), 1)
    FrameDurationMaxMs = [Math]::Round((Get-FramePercentile 1.0), 1)
    FramesOver32Ms = @($durations | Where-Object { $_ -gt 32 }).Count
    FramesOver50Ms = @($durations | Where-Object { $_ -gt 50 }).Count
    FramesOver100Ms = @($durations | Where-Object { $_ -gt 100 }).Count
    Note = "Recent frame history is bounded and does not associate a frame with a specific tab transition."
}
$report | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $OutputDirectory "frame-duration-summary.json") -Encoding UTF8
$gfx | Select-String -Pattern '^\s*(Total frames rendered|Janky frames|50th percentile|90th percentile|95th percentile|99th percentile|Number Missed Vsync|Number Slow UI thread|Number Frame deadline missed):' | Set-Content -LiteralPath (Join-Path $OutputDirectory "summary-metrics.txt") -Encoding UTF8
$report | Format-List | Out-Host
Write-Host "Results: $OutputDirectory"
