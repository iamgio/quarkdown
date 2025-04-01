executionQueue.push(() => {
    MathJax = {
        svg: {
            fontCache: 'global'
        },
        options: {
            // Letting MathJax render formulas from <formula> tags.
            // https://stackoverflow.com/questions/62089234/execute-mathjax-for-custom-tags-without-delimiters
            renderActions: {
                find: [10, function (doc) {
                    for (const node of document.querySelectorAll('formula')) {
                        const isBlock = node.dataset.block === '';
                        const math = new doc.options.MathItem(node.textContent, doc.inputJax[0], isBlock);
                        const text = document.createTextNode('');
                        node.parentNode.replaceChild(text, node);
                        math.start = {node: text, delim: '', n: 0};
                        math.end = {node: text, delim: '', n: 0};
                        doc.math.push(math);
                    }
                }, '']
            }
        }
    };

    // Script that enables MathJax rendering.
    let script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js';
    script.async = true;
    script.id = 'MathJax-script';
    document.body.appendChild(script);
});