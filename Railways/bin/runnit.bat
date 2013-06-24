@echo off
rem Batch file to run on Windows
rem $Id: runnit.bat,v 1.2 2005/10/17 22:37:19 marco Exp $

rem Remove "rem" from following two lines, if you'd like to use j2sdk.
rem set JAVA_HOME=C:\j2sdk1.4.2_08
rem set PATH=%JAVA_HOME%\bin

rem run
cd ..
start javaw -jar "lib/railways.jar"
IF ERRORLEVEL 2 goto noJavaw
goto end


:noJavaw
echo.
echo Failed to run java.
echo Java runtime environment is required to run Railways.
echo Setup Java environment at first.
echo.
echo Railways tries to run javaw. It should be in PATH system environment variable.
echo.
echo If you would like to run java in your specified folder, you can edit runnit.bat
echo like followings and set your JAVA_HOME.
echo     before:
echo       rem set JAVA_HOME=C:\j2sdk1.4.2_08
echo       rem set PATH=%JAVA_HOME%\bin
echo     after:
echo       set JAVA_HOME=C:\j2sdk1.4.2_08
echo       set PATH=%JAVA_HOME%\bin
echo.
echo.
pause
goto end

:end