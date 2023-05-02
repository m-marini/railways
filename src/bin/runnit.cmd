@echo off
rem Batch file to run on Windows

rem Remove "rem" from following two lines, if you'd like to use j2sdk.
rem set JAVA_HOME=...
rem set PATH=%JAVA_HOME%\bin

rem run
cd ..
echo 
start javaw -jar "lib/${pom.build.finalName}.jar"
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
echo       rem set JAVA_HOME=...
echo       rem set PATH=%JAVA_HOME%\bin
echo     after:
echo       set JAVA_HOME=...
echo       set PATH=%JAVA_HOME%\bin
echo.
echo.
pause
goto end

:end