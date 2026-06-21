# Build APK script
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:ANDROID_HOME = "C:\Users\рс\AppData\Local\Android\Sdk"

Set-Location "$PSScriptRoot\android"

& "C:\Users\рс\.gradle\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat" assembleDebug --no-daemon

pause