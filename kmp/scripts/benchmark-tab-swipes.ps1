<#
.SYNOPSIS
  Captures repeatable Android tab-swipe latency and frame-jank samples.

.DESCRIPTION
  Alternates Home -> Quran -> Sunnah -> Quran -> Home, resetting gfxinfo
  frame statistics before each swipe. This uses adb input and is intended for
  emulators or devices that allow injected input. It does not install the app.
#>
[CmdletBinding()]
param(
    [string] $Serial,
    [string] $PackageName = "com.godfather59.quransunnah",
    [ValidateRange(1, 100)] [int] $Iterations = 5,
    [ValidateRange(100, 1200)] [int] $SwipeDurationMs = 300,
    [ValidateRange(100, 3000)] [int] $SettleMs = 700,
    [string] $OutputDirectory
)

$ErrorActionPreference = "Stop"

function Invoke-Adb {
    param([string[]] $AdbArgs)
    $result = & $script:AdbPath -s $script:DeviceSerial @AdbArgs 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "adb $($AdbArgs -join ' ') failed: $($result -join ' ')"
    }
    return $result
}

function Get-Percentile {
    param([double[]] $Values, [double] $Percentile)
    if (-not $Values -or $Values.Count -eq 0) { return 0.0 }
    $ordered = @($Values | Sort-Object)
    $index = [Math]::Ceiling($Percentile * $ordered.Count) - 1
    return [double]$ordered[[Math]::Max(0, [Math]::Min($index, $ordered.Count - 1))]
}

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCommand) {
    $script:AdbPath = $adbCommand.Source
} else {
    $sdkRoots = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
    if ($env:LOCALAPPDATA) { $sdkRoots += (Join-Path $env:LOCALAPPDATA "Android\Sdk") }
    $sdkAdb = @($sdkRoots | Where-Object { $_ } | ForEach-Object {
        Join-Path $_ "platform-tools\adb.exe"
    } | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1)
    if ($sdkAdb.Count -eq 0) { throw "adb was not found on PATH or in the standard Windows Android SDK locations." }
    $script:AdbPath = $sdkAdb[0]
}

$deviceLines = @(& $script:AdbPath devices 2>&1)
if ($LASTEXITCODE -ne 0) { throw "Could not query adb devices: $($deviceLines -join ' ')" }
$connected = @($deviceLines | Select-Object -Skip 1 | ForEach-Object {
    if ($_ -match '^\s*(\S+)\s+device\s*$') { $Matches[1] }
})
if ([string]::IsNullOrWhiteSpace($Serial)) {
    if ($connected.Count -ne 1) {
        throw "Expected exactly one adb device, found $($connected.Count). Pass -Serial from 'adb devices'."
    }
    $script:DeviceSerial = $connected[0]
} else {
    if ($connected -notcontains $Serial) { throw "Device '$Serial' is not connected and authorized in adb devices." }
    $script:DeviceSerial = $Serial
}

$sizeOutput = Invoke-Adb -AdbArgs @("shell", "wm", "size")
$sizeText = $sizeOutput -join "`n"
if ($sizeText -notmatch '(?m)(\d+)x(\d+)\s*$') { throw "Could not determine screen size from: $sizeText" }
$width = [int]$Matches[1]
$height = [int]$Matches[2]
$startX = [int]($width * 0.88)
$endX = [int]($width * 0.12)
$swipeY = [int]($height * 0.52)

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $OutputDirectory = Join-Path (Get-Location) "artifacts/tab-swipe-benchmarks/$stamp"
}
$null = New-Item -ItemType Directory -Path $OutputDirectory -Force
$csvPath = Join-Path $OutputDirectory "swipes.csv"
$rawPath = Join-Path $OutputDirectory "gfxinfo-raw.txt"
$devicePath = Join-Path $OutputDirectory "device.txt"

$fingerprint = (Invoke-Adb -AdbArgs @("shell", "getprop", "ro.build.fingerprint") | Out-String).Trim()
$refreshRate = (Invoke-Adb -AdbArgs @("shell", "dumpsys", "display") | Select-String -Pattern 'refreshRate=([\d.]+)' | Select-Object -First 1)
$refreshText = if ($refreshRate -and $refreshRate.Matches.Count -gt 0) { $refreshRate.Matches[0].Groups[1].Value } else { "unknown" }
@(
    "Serial: $script:DeviceSerial"
    "Package: $PackageName"
    "Fingerprint: $fingerprint"
    "Resolution: ${width}x${height}"
    "Refresh rate: $refreshText Hz"
    "Swipe: ($startX,$swipeY) -> ($endX,$swipeY), ${SwipeDurationMs}ms"
    "Settle: ${SettleMs}ms"
) | Set-Content -Path $devicePath -Encoding UTF8

$activityOutput = Invoke-Adb -AdbArgs @("shell", "pidof", $PackageName)
if (-not ($activityOutput -join " ").Trim()) {
    throw "The app ($PackageName) is not running. Open it on the device, make sure Home is selected, then run the benchmark."
}

$directions = @("left", "left", "right", "right")
$rows = [System.Collections.Generic.List[object]]::new()
$allFrames = [System.Collections.Generic.List[double]]::new()
$totalSwipeMs = [System.Collections.Generic.List[double]]::new()

Write-Host "Warming up one Home -> Quran -> Sunnah -> Home cycle (excluded from results)..."
foreach ($direction in @("left", "left", "right", "right")) {
    if ($direction -eq "left") {
        $fromX = $startX; $toX = $endX
    } else {
        $fromX = $endX; $toX = $startX
    }
    $null = Invoke-Adb -AdbArgs @("shell", "input", "swipe", "$fromX", "$swipeY", "$toX", "$swipeY", "$SwipeDurationMs")
    Start-Sleep -Milliseconds $SettleMs
}

for ($iteration = 1; $iteration -le $Iterations; $iteration++) {
    for ($step = 0; $step -lt $directions.Count; $step++) {
        $direction = $directions[$step]
        if ($direction -eq "left") {
            $fromX = $startX; $toX = $endX
        } else {
            $fromX = $endX; $toX = $startX
        }
        $transition = switch ($step) {
            0 { "Home->Quran" }
            1 { "Quran->Sunnah" }
            2 { "Sunnah->Quran" }
            3 { "Quran->Home" }
        }

        $null = Invoke-Adb -AdbArgs @("shell", "dumpsys", "gfxinfo", $PackageName, "reset")
        $timer = [System.Diagnostics.Stopwatch]::StartNew()
        $null = Invoke-Adb -AdbArgs @("shell", "input", "swipe", "$fromX", "$swipeY", "$toX", "$swipeY", "$SwipeDurationMs")
        $timer.Stop()
        $wallMs = [Math]::Round($timer.Elapsed.TotalMilliseconds, 1)
        Start-Sleep -Milliseconds $SettleMs

        $gfx = Invoke-Adb -AdbArgs @("shell", "dumpsys", "gfxinfo", $PackageName, "framestats")
        Add-Content -Path $rawPath -Value "`n===== iteration=$iteration transition=$transition adbSwipeMs=$wallMs =====" -Encoding UTF8
        Add-Content -Path $rawPath -Value $gfx -Encoding UTF8

        $frameDurations = [System.Collections.Generic.List[double]]::new()
        foreach ($line in $gfx) {
            $trimmed = ([string]$line).Trim()
            if ($trimmed -notmatch '^\d+,') { continue }
            $fields = $trimmed.Split(',')
            if ($fields.Count -le 17) { continue }
            $flags = 0L; $intendedVsync = 0L; $frameCompleted = 0L
            if ([long]::TryParse($fields[0], [ref]$flags) -and
                [long]::TryParse($fields[2], [ref]$intendedVsync) -and
                [long]::TryParse($fields[17], [ref]$frameCompleted) -and
                $intendedVsync -gt 0 -and $frameCompleted -ge $intendedVsync) {
                $frameDurations.Add(($frameCompleted - $intendedVsync) / 1000000.0)
            }
        }

        foreach ($frame in $frameDurations) { $allFrames.Add($frame) }
        $totalSwipeMs.Add($wallMs)
        $row = [pscustomobject]@{
            Iteration = $iteration
            Transition = $transition
            AdbSwipeMs = $wallMs
            Frames = $frameDurations.Count
            FrameP50Ms = [Math]::Round((Get-Percentile -Values $frameDurations.ToArray() -Percentile 0.50), 1)
            FrameP90Ms = [Math]::Round((Get-Percentile -Values $frameDurations.ToArray() -Percentile 0.90), 1)
            FrameMaxMs = [Math]::Round((($frameDurations | Measure-Object -Maximum).Maximum), 1)
            FramesOver32Ms = @($frameDurations | Where-Object { $_ -gt 32 }).Count
            FramesOver50Ms = @($frameDurations | Where-Object { $_ -gt 50 }).Count
            FramesOver100Ms = @($frameDurations | Where-Object { $_ -gt 100 }).Count
            StallDetected = [bool](@($frameDurations | Where-Object { $_ -gt 100 }).Count -gt 0)
        }
        $rows.Add($row)
        Write-Host ("{0}/{1} {2}: swipe {3}ms, frames {4}, p90 {5}ms, >100ms {6}" -f `
            (($iteration - 1) * 4 + $step + 1), ($Iterations * 4), $transition, $row.AdbSwipeMs, `
            $row.Frames, $row.FrameP90Ms, $row.FramesOver100Ms)
    }
}

$rows | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8
$summary = [pscustomobject]@{
    Device = $script:DeviceSerial
    Samples = $rows.Count
    SwipeLatencyP50Ms = [Math]::Round((Get-Percentile -Values $totalSwipeMs.ToArray() -Percentile 0.50), 1)
    SwipeLatencyP90Ms = [Math]::Round((Get-Percentile -Values $totalSwipeMs.ToArray() -Percentile 0.90), 1)
    FrameDurationP50Ms = [Math]::Round((Get-Percentile -Values $allFrames.ToArray() -Percentile 0.50), 1)
    FrameDurationP90Ms = [Math]::Round((Get-Percentile -Values $allFrames.ToArray() -Percentile 0.90), 1)
    FrameMaxMs = [Math]::Round((($allFrames | Measure-Object -Maximum).Maximum), 1)
    FramesOver32Ms = @($allFrames | Where-Object { $_ -gt 32 }).Count
    FramesOver50Ms = @($allFrames | Where-Object { $_ -gt 50 }).Count
    FramesOver100Ms = @($allFrames | Where-Object { $_ -gt 100 }).Count
    DetectedStalls = @($rows | Where-Object StallDetected).Count
}
$summaryPath = Join-Path $OutputDirectory "summary.json"
$summary | ConvertTo-Json -Depth 4 | Set-Content -Path $summaryPath -Encoding UTF8

Write-Host ""
Write-Host "Summary:"
$summary | Format-List | Out-Host
Write-Host "Per-swipe CSV: $csvPath"
Write-Host "Raw frame data: $rawPath"
Write-Host "Device info: $devicePath"
if ($allFrames.Count -eq 0) {
    Write-Warning "No frame durations parsed. Keep gfxinfo-raw.txt and check its CSV format; wall swipe timings are still recorded."
}
