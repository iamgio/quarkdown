// noinspection JSUnresolvedReference

const puppeteer = require('puppeteer');

(async () => {
    console.log('starting');

    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    //await page.goto('http://localhost:8089');
    await page.goto('http://localhost:8080/.github/workflows/Quarkdown-Mock/index.html');

    console.log('waiting for ready');

    await page.waitForFunction('readyState');

    await page.pdf({ path: 'output/mock.pdf', format: 'A4' });
    await browser.close();

    console.log('finished');
})();