const outputFile = process.argv[2];
const url = process.argv[3];

console.log('outputFile: ' + outputFile);
console.log('url: ' + url);

const puppeteer = require('puppeteer');

(async () => {
    const browser = await puppeteer.launch({
        args: [
            // '--no-sandbox',
            '--disable-gpu',
        ]
    });
    const page = await browser.newPage();

    console.log('Connecting to ' + url);
    await page.goto(url);

    console.log('Connected. Waiting for page content.');
    await page.content();

    console.log('Connected. Waiting for page to be ready.');
    await page.waitForFunction('isReady()');

    await page.pdf({
        path: outputFile,
        preferCSSPageSize: true,
        printBackground: true,
    });
    await browser.close();
})();