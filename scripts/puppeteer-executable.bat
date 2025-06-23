@if defined QD_NPM_PREFIX (
    set "NODE_PATH=%QD_NPM_PREFIX%\node_modules"
)

@if not defined PUPPETEER_EXECUTABLE_PATH (
    if exist "%ProgramFiles%\Google\Chrome\Application\chrome.exe" (
        set "PUPPETEER_EXECUTABLE_PATH=%ProgramFiles%\Google\Chrome\Application\chrome.exe"
    ) else if exist "%ProgramFiles%\Chromium\Application\chrome.exe" (
        set "PUPPETEER_EXECUTABLE_PATH=%ProgramFiles%\Chromium\Application\chrome.exe"
    ) else if exist "%ProgramFiles%\Mozilla Firefox\firefox.exe" (
        set "PUPPETEER_EXECUTABLE_PATH=%ProgramFiles%\Mozilla Firefox\firefox.exe"
        set "PUPPETEER_BROWSER=firefox"
    ) else if exist "%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe" (
        set "PUPPETEER_EXECUTABLE_PATH=%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe"
    ) else if exist "%ProgramFiles(x86)%\Mozilla Firefox\firefox.exe" (
        set "PUPPETEER_EXECUTABLE_PATH=%ProgramFiles(x86)%\Mozilla Firefox\firefox.exe"
        set "PUPPETEER_BROWSER=firefox"
    ) else (
        echo "[!] Could not find a suitable installation of Google Chrome, Chromium or Firefox. PDF generation may not be available."
    )
)
