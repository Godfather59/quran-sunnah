$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$oraclePath = Join-Path $scriptDir "oracle\appstrings.json"
$androidResDir = Join-Path $scriptDir "..\androidApp\src\main\res"
$iosStringsPath = Join-Path $scriptDir "..\iosApp\Localizable.xcstrings"

function Write-Utf8NoBom([string] $Path, [string] $Text) {
    $encoding = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText((Resolve-Path -LiteralPath (Split-Path -Parent $Path)).Path + [System.IO.Path]::DirectorySeparatorChar + (Split-Path -Leaf $Path), $Text, $encoding)
}

function Xml-Escape([string] $Value) {
    return [System.Security.SecurityElement]::Escape($Value)
}

function Json-String([string] $Value) {
    return ($Value | ConvertTo-Json -Compress)
}

$oracle = Get-Content -Raw -Encoding UTF8 $oraclePath | ConvertFrom-Json
$strings = $oracle.strings
$keys = @($strings.PSObject.Properties.Name | Sort-Object)
$locales = @("ar", "en", "fr")

if ($keys.Count -ne [int] $oracle._meta.count) {
    throw "Oracle count mismatch: meta=$($oracle._meta.count), actual=$($keys.Count)"
}

foreach ($key in $keys) {
    foreach ($locale in $locales) {
        $value = $strings.$key.$locale
        if ([string]::IsNullOrWhiteSpace($value)) {
            throw "Missing localization: $key/$locale"
        }
    }
    if ($strings.$key.ar -match "[A-Za-z]") {
        throw "Arabic string contains Latin letters: $key"
    }
}

function Write-AndroidStrings([string] $FolderName, [string] $Locale) {
    $dir = Join-Path $androidResDir $FolderName
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add('<?xml version="1.0" encoding="utf-8"?>')
    $lines.Add('<!-- Generated from kmp/l10n/oracle/appstrings.json. DO NOT EDIT. -->')
    # UnusedResources is intentionally muted here: the 224-key set is the
    # trilingual parity contract with iOS Localizable.xcstrings (verified by
    # key count), not dead weight — screens adopt keys incrementally.
    $lines.Add('<resources xmlns:tools="http://schemas.android.com/tools" tools:ignore="UnusedResources">')
    foreach ($key in $keys) {
        $value = Xml-Escape $strings.$key.$Locale
        $lines.Add("    <string name=`"$key`">$value</string>")
    }
    $lines.Add('</resources>')
    Write-Utf8NoBom (Join-Path $dir "strings.xml") (($lines -join [Environment]::NewLine) + [Environment]::NewLine)
}

Write-AndroidStrings "values" "ar"
Write-AndroidStrings "values-en" "en"
Write-AndroidStrings "values-fr" "fr"

$xc = New-Object System.Collections.Generic.List[string]
$xc.Add('{')
$xc.Add('  "sourceLanguage" : "ar",')
$xc.Add('  "strings" : {')
for ($i = 0; $i -lt $keys.Count; $i++) {
    $key = $keys[$i]
    $keyComma = if ($i -eq $keys.Count - 1) { "" } else { "," }
    $xc.Add("    $(Json-String $key) : {")
    $xc.Add('      "localizations" : {')
    for ($j = 0; $j -lt $locales.Count; $j++) {
        $locale = $locales[$j]
        $localeComma = if ($j -eq $locales.Count - 1) { "" } else { "," }
        $xc.Add("        `"$locale`" : {")
        $xc.Add('          "stringUnit" : {')
        $xc.Add('            "state" : "translated",')
        $xc.Add("            `"value`" : $(Json-String $strings.$key.$locale)")
        $xc.Add('          }')
        $xc.Add("        }$localeComma")
    }
    $xc.Add('      }')
    $xc.Add("    }$keyComma")
}
$xc.Add('  },')
$xc.Add('  "version" : "1.0"')
$xc.Add('}')
Write-Utf8NoBom $iosStringsPath (($xc -join [Environment]::NewLine) + [Environment]::NewLine)

Write-Host "Generated $($keys.Count) strings for Android and iOS."
