// Render KaTeX inside <formula> tags.
preRenderingExecutionQueue.push(() => {
    const formulas = document.querySelectorAll('formula');
    formulas.forEach((formula) => {
        const isBlock = formula.dataset.block === '';
        const math = formula.textContent;
        formula.innerHTML = katex.renderToString(math, {
            throwOnError: false,
            displayMode: isBlock,
            macros: texMacros || {},
        });
    });
});