function setupPageMargins() {
    // A page margin content initializer is an element that will be copied into each section,
    // and is placed on one of the page margins.
    const pageMarginInitializers = document.querySelectorAll('.page-margin-content');
    // <div class="page-margin-content-initializer page-margin-bottom-center">Hello</div>
    // will be copied to each page as:
    // <div class="pagedjs_margin-content">Hello</div>
    // contained in the .pagedjs_margin pagedjs_margin-top-left div.
    pageMarginInitializers.forEach(initializer => {
        const marginContent = document.createElement('div');
        marginContent.className = 'pagedjs_margin-content';
        marginContent.innerHTML = initializer.innerHTML;

        // Given the initializer with class "page-margin-content-initializer page-margin-bottom-center",
        // the margin class will be "pagedjs_margin-bottom-center".
        const pageMargins = document.querySelectorAll('.pagedjs_margin-' + initializer.className.split('page-margin-').pop());
        pageMargins.forEach(pageMargin => {
            console.log(pageMargin);
            pageMargin.classList.add('hasContent');
            // Append the content.
            pageMargin.querySelector('.pagedjs_margin-content').appendChild(marginContent.cloneNode(true));
        });

        initializer.remove();
    });
}

executionQueue.push(setupPageMargins);