const fs = require('fs');
const path = require('path');

const PROJECT_DIR = 'mock';
const MAIN_FILE = PROJECT_DIR + '/main.qd';
const WORKFLOW_DIR = '.github/workflows/generate-pdf';
const COLOR_FILE = WORKFLOW_DIR + '/color.txt';
const LAYOUT_FILE = WORKFLOW_DIR + '/layout.txt';

function getLines(file) {
    return fs.readFileSync(file, 'utf8')
        .split('\n')
        .map(l => l.trim())
        .filter(l => l.length > 0);
}

function createNewFileName(color, layout) {
    return `generated_main_${color}_${layout}.qd`;
}

function generateThemeVariants() {
    try {
        const colors = getLines(COLOR_FILE);
        const layouts = getLines(LAYOUT_FILE);

        const mainContent = fs.readFileSync(MAIN_FILE, 'utf8');

        // Generate all combinations
        for (const color of colors) {
            for (const layout of layouts) {
                const newFile = `${PROJECT_DIR}/${createNewFileName(color, layout)}`;

                // Create new content with theme line
                const themeLine = `.theme color:{${color}} layout:{${layout}}`;
                const newNameLine = `.docname {${color}_${layout}}`;
                const newContent = mainContent + '\n\n' + newNameLine + '\n' + themeLine;

                // Write to file
                fs.writeFileSync(newFile, newContent);
                console.log(`Created: ${newFile}`);
            }
        }

        console.log(`Successfully generated ${colors.length * layouts.length} theme variants.`);
    } catch (error) {
        console.error('Error:', error.message);
        process.exit(1);
    }
}

generateThemeVariants();