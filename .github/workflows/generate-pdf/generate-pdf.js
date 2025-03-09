// noinspection JSUnresolvedReference

const puppeteer = require('puppeteer');

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
        path: 'pdf/mock.pdf',
        preferCSSPageSize: true,
    });
    await browser.close();
})();