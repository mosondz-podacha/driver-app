@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set GRADLE_HOME=C:\Users\рс\.gradle\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13
set ANDROID_HOME=C:\Users\рс\AppData\Local\Android\Sdk

echo JAVA_HOME=%JAVA_HOME%
echo GRADLE_HOME=%GRADLE_HOME%
echo ANDROID_HOME=%ANDROID_HOME%

cd /d "%~dp0android"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo ERROR: Gradle not found at %GRADLE_HOME%\bin\gradle.bat
    pause
    exit /b 1
)

echo Building APK...
"%GRADLE_HOME%\bin\gradle.bat" assembleDebug --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====== BUILD SUCCESSFUL ======
    echo APK location: "%~dp0android\app\build\outputs\apk\debug\"
    dir "%~dp0android\app\build\outputs\apk\debug\*.apk" 2>nul
) else (
    echo.
    echo ====== BUILD FAILED with code %ERRORLEVEL% ======
)

pause