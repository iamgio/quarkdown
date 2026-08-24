REM Always prefer the bundled JRE when present, ignoring any system JAVA_HOME.
REM This guarantees the JVM version Quarkdown was built and tested against.
REM
REM Node module resolution and browser detection used to live here. They are now resolved by
REM Quarkdown itself, so that they behave identically whether Quarkdown is launched through this
REM script or as a native binary, which has no wrapper script.
set "SCRIPT_DIR=%~dp0"
if exist "%SCRIPT_DIR%..\runtime\bin\java.exe" (
    set "JAVA_HOME=%SCRIPT_DIR%..\runtime"
)
