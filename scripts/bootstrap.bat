@echo off

if defined QD_NPM_PREFIX (
    set "NODE_PATH=%QD_NPM_PREFIX%\node_modules"
)

if exist "%ProgramFiles%\Google\Chrome\Application\chrome.exe" (
    set "BROWSER_CHROME=%ProgramFiles%\Google\Chrome\Application\chrome.exe"
)
if exist "%ProgramFiles%\Chromium\Application\chrome.exe" (
    set "BROWSER_CHROMIUM=%ProgramFiles%\Chromium\Application\chrome.exe"
)
if exist "%ProgramFiles%\Mozilla Firefox\firefox.exe" (
    set "BROWSER_FIREFOX=%ProgramFiles%\Mozilla Firefox\firefox.exe"
)
if exist "%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe" (
    set "BROWSER_CHROME=%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe"
)
if exist "%ProgramFiles(x86)%\Mozilla Firefox\firefox.exe" (
    set "BROWSER_FIREFOX=%ProgramFiles(x86)%\Mozilla Firefox\firefox.exe"
)
if exist "%ProgramFiles%\Microsoft\Edge\Application\msedge.exe" (
    set "BROWSER_EDGE=%ProgramFiles%\Microsoft\Edge\Application\msedge.exe"
)
if exist "%ProgramFiles(x86)%\Microsoft\Edge\Application\msedge.exe" (
    set "BROWSER_EDGE=%ProgramFiles(x86)%\Microsoft\Edge\Application\msedge.exe"
)