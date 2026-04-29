const outputFile = process.argv[1];
const url = process.argv[2];
const noSandbox = process.argv[3] === 'true';
// Per-operation timeout in milliseconds. 0 means no timeout. Defaults to Puppeteer's 30s default if omitted.
const timeoutMillis = process.argv[4] !== undefined ? parseInt(process.argv[4], 10) : 30000;

console.log('outputFile: ' + outputFile);
console.log('url: ' + url);
console.log('timeoutMillis: ' + timeoutMillis + (timeoutMillis === 0 ? ' (no timeout)' : ''));

const puppeteer = require('puppeteer');

function createArgs() {
    const args = [
        '--disable-gpu',
    ]
    if (noSandbox) {
        args.push('--no-sandbox');
    }
    return args;
}

(async () => {
    const args = createArgs();
    console.log('Running with args: ' + args);

    const browser = await puppeteer.launch({
        args: args,
        headless: 'shell',
        // Caps the duration of any single CDP call (including page.pdf). 0 disables it.
        protocolTimeout: timeoutMillis,
    });
    const page = await browser.newPage();
    // Caps page.goto and other navigations. 0 disables it.
    page.setDefaultNavigationTimeout(timeoutMillis);
    // Caps page.waitForFunction and other waits. 0 disables it.
    page.setDefaultTimeout(timeoutMillis);

    console.log('Connecting to ' + url);
    await page.goto(url);

    console.log('Connected. Waiting for page content.');
    await page.content();

    console.log('Connected. Waiting for page to be ready.');
    await page.waitForFunction('window.isReady()');

    const body = await page.$('body');

    // Plain documents render as a single-page PDF.
    const isSinglePage = await body.evaluate(bodyElement => bodyElement.classList.contains('quarkdown-plain'));
    const singlePageHeightPadding = 100; // Additional height added to single-page PDFs. If not enough, an additional page will be incorrectly generated.
    const singlePageHeightMultiplier = 1.03;

    const pdfOptions = {
        path: outputFile,
        printBackground: true,
        preferCSSPageSize: true,
        timeout: timeoutMillis,
        ...(
            isSinglePage
                ? {height: (await getClientHeight(body)) * singlePageHeightMultiplier + singlePageHeightPadding + 'px'}
                : {}
        ),
    };
    await page.pdf(pdfOptions);

    await browser.close();
})();

async function getClientHeight(body) {
    return body.evaluate(bodyElement => bodyElement.clientHeight);
}