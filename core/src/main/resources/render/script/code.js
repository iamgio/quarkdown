executionQueue.push(() => {
    hljs.addPlugin(new CopyButtonPlugin()); // Add copy button to code blocks.
    hljs.highlightAll(); // Highlight all code blocks.
    hljs.initLineNumbersOnLoad(); // Show line numbers on code blocks.
});

// Focuses specific lines in selected code blocks.
function focusCodeLines() {
    document.querySelectorAll('code.focus-lines').forEach((code) => {
        const start = parseInt(code.getAttribute('data-focus-start'));
        const end = parseInt(code.getAttribute('data-focus-end'));
        code.querySelectorAll('.hljs-ln-line').forEach(line => {
            const lineNumber = parseInt(line.getAttribute('data-line-number'));
            // Open range support.
            if ((isNaN(start) || lineNumber >= start) && (isNaN(end) || lineNumber <= end)) {
                line.classList.add('focused');
            }
        });
    });
}

hljs.addPlugin({
    'after:highlight': () => window.setTimeout(focusCodeLines, 1)
});