// noinspection JSUnresolvedReference

const puppeteer = require('puppeteer');

// File name from arguments, without extension
const fileName = process.argv[2]
    .replace(/\.[^/.]+$/, "")
    .replace('generated_main_', '');

(async () => {
    const browser = await puppeteer.launch({
        args: [
            '--no-sandbox',
            '--disable-gpu',
        ]
    });
    const page = await browser.newPage();
    await page.goto('http://localhost:8080/output/Quarkdown-Mock/index.html');

    await page.waitForFunction('readyState');

    await page.pdf({
        path: `pdf/${fileName}.pdf`,
        preferCSSPageSize: true,
        printBackground: true,
    });
    await browser.close();
})();