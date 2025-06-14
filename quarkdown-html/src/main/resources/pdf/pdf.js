const outputFile = process.argv[1];
const url = process.argv[2];
const noSandbox = process.argv[3] === 'true';

console.log('outputFile: ' + outputFile);
console.log('url: ' + url);

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

    const browser = await puppeteer.launch({args: args});
    const page = await browser.newPage();

    console.log('Connecting to ' + url);
    await page.goto(url);

    console.log('Connected. Waiting for page content.');
    await page.content();

    console.log('Connected. Waiting for page to be ready.');
    await page.waitForFunction('isReady()');

    const body = await page.$('body');

    // Plain documents render as a single-page PDF.
    const isSinglePage = await body.evaluate(bodyElement => bodyElement.classList.contains('quarkdown-plain'));
    const singlePageHeightPadding = 100; // Additional height added to single-page PDFs. If not enough, an additional page will be incorrectly generated.
    const singlePageHeightMultiplier = 1.03;

    const pdfOptions = {
        path: outputFile,
        printBackground: true,
        preferCSSPageSize: true,
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