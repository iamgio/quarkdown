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
    await page.goto(url);

    await page.waitForFunction('readyState');

    await page.pdf({
        path: outputFile,
        preferCSSPageSize: true,
        printBackground: true,
    });
    await browser.close();
})();