executionQueue.push(() => {
    MathJax = {
        tex: {
            displayMath: [['__QD_BLOCK_MATH__$', '$__QD_BLOCK_MATH__']],
            inlineMath: [['__QD_INLINE_MATH__$', '$__QD_INLINE_MATH__']]
        },
        svg: {
            fontCache: 'global'
        }
    };

    // Script that enables MathJax rendering.
    let script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js';
    script.async = true;
    script.id = 'MathJax-script';
    document.body.appendChild(script);
});