@echo off
chcp 65001 > nul
title my-sub-is-private : Presentation Demo
cd /d "%~dp0"

where java > nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH.
    echo         Install JDK 17+ from https://adoptium.net and try again.
    pause
    exit /b 1
)

java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -cp "MySubIsPrivate.jar" Main
pause
