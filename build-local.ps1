$ErrorActionPreference = 'Stop'
$javaDirectory = Join-Path $PSScriptRoot '.tools\java\jdk-17.0.20.1+1'
$previousJavaHome = $env:JAVA_HOME
if (Test-Path -LiteralPath (Join-Path $javaDirectory 'bin\java.exe')) {
    $env:JAVA_HOME = $javaDirectory
}
Push-Location $PSScriptRoot
try {
    & .\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug '-Pkotlin.incremental=false'
    $buildResult = $LASTEXITCODE
} finally {
    Pop-Location
    $env:JAVA_HOME = $previousJavaHome
}
exit $buildResult
