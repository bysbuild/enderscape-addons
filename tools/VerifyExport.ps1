param(
    [Parameter(Mandatory = $true)][string]$OriginalJarDirectory,
    [string]$Repository = (Split-Path $PSScriptRoot -Parent)
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$exports = @(
    @{ Name = 'enderscape-expansion'; File = 'enderscape-expansion-neoforge-1.21.1-1.0.11.jar' },
    @{ Name = 'enderscape-trim-bridge'; File = 'enderscape-trim-bridge-neoforge-1.21.1-1.0.0.jar' }
)
$lines = [Collections.Generic.List[string]]::new()
$lines.Add('# Source export verification')
$lines.Add('')
$lines.Add('Decompiler: Vineflower 1.11.2; Java target: 21.')
$lines.Add('Java syntax was checked with tools/VerifyJavaSyntax.java. No full compilation or Minecraft runtime test was performed.')
$lines.Add('')
foreach ($export in $exports) {
    $inputFile = Join-Path $OriginalJarDirectory $export.File
    $hashBefore = (Get-FileHash -LiteralPath $inputFile -Algorithm SHA256).Hash
    $archive = [IO.Compression.ZipFile]::OpenRead($inputFile)
    $classes = 0
    $resources = 0
    $jsonFiles = 0
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName.EndsWith('/')) { continue }
            if ($entry.FullName.EndsWith('.class')) {
                $sourceName = ($entry.FullName -replace '\$[^/]*\.class$', '.class') -replace '\.class$', '.java'
                $sourcePath = Join-Path "$Repository/$($export.Name)/src/main/java" $sourceName
                if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) { throw "Missing source: $sourceName" }
                $source = [IO.File]::ReadAllText($sourcePath)
                $expectedPackage = ($sourceName.Substring(0, $sourceName.LastIndexOf('/'))) -replace '/', '.'
                if ($source -notmatch ('(?m)^package ' + [regex]::Escape($expectedPackage) + ';')) { throw "Wrong package: $sourceName" }
                $classes++
            } else {
                $resourcePath = Join-Path "$Repository/$($export.Name)/src/main/resources" $entry.FullName
                if (-not (Test-Path -LiteralPath $resourcePath -PathType Leaf)) { throw "Missing resource: $($entry.FullName)" }
                $stream = $entry.Open()
                try { $originalHash = [Convert]::ToHexString($sha.ComputeHash($stream)) } finally { $stream.Dispose() }
                $exportHash = (Get-FileHash -LiteralPath $resourcePath -Algorithm SHA256).Hash
                if ($originalHash -ne $exportHash) { throw "Resource changed: $($entry.FullName)" }
                if ($entry.FullName -match '\.(json|mcmeta)$') {
                    $null = [IO.File]::ReadAllText($resourcePath) | ConvertFrom-Json -Depth 100 -AsHashtable
                    $jsonFiles++
                }
                $resources++
            }
        }
    } finally { $archive.Dispose(); $sha.Dispose() }
    if ($hashBefore -ne (Get-FileHash -LiteralPath $inputFile -Algorithm SHA256).Hash) { throw 'Input JAR changed' }
    $sourceCount = @(Get-ChildItem -LiteralPath "$Repository/$($export.Name)/src/main/java" -Recurse -Filter '*.java').Count
    $resourceCount = @(Get-ChildItem -LiteralPath "$Repository/$($export.Name)/src/main/resources" -Recurse -File).Count
    if ($resourceCount -ne $resources) { throw 'Unexpected additional resource files' }
    $lines.Add("## $($export.Name)")
    $lines.Add('')
    $lines.Add("- Input: $($export.File)")
    $lines.Add("- SHA-256: $hashBefore")
    $lines.Add("- Java source files: $sourceCount")
    $lines.Add("- Original class files covered by source: $classes")
    $lines.Add("- Byte-identical extracted resources: $resources")
    $lines.Add("- Parsed JSON / pack metadata: $jsonFiles")
    $lines.Add('- Source package paths: PASS')
    $lines.Add('- Input JAR unchanged: PASS')
    $lines.Add('')
}
$lines.Add('Inner and anonymous classes are represented inside their enclosing Java source files.')
[IO.File]::WriteAllLines("$Repository/EXPORT-REPORT.md", $lines, [Text.UTF8Encoding]::new($false))
$lines
