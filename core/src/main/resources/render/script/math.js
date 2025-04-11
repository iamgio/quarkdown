// Render KaTeX inside <formula> tags.
postRenderingExecutionQueue.push(() => {
    const formulas = document.querySelectorAll('formula');
    formulas.forEach((formula) => {
        const isBlock = formula.dataset.block === '';
        const math = formula.textContent;
        formula.outerHTML = katex.renderToString(math, {
            throwOnError: false,
            displayMode: isBlock,
            macros: texMacros || {},
        });
    });
});