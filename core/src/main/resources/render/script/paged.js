// A page margin content initializer is an element that will be copied into each page,
// and is placed on one of the page margins.
let pageMarginInitializers;

function setupPagedHandler() {
    class PagedExecutionHandler extends Paged.Handler {
        beforeParsed(content) {
            // Load page margin content initializers.
            pageMarginInitializers = content.querySelectorAll('.page-margin-content');
            // Initializers are removed from the content before paged.js is launched
            // in order to avoid blank pages.
            pageMarginInitializers.forEach(initializer => initializer.remove());
        }

        afterPreview() {
            executeQueue();
        }
    }

    Paged.registerHandlers(PagedExecutionHandler);
}

// Copies the content of each page margin content initializer to each page.
function setupPageMargins() {
    if (!pageMarginInitializers) {
        console.error('pageMarginInitializers not set');
        return;
    }

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
            pageMargin.classList.add('hasContent');
            // Append the content.
            pageMargin.querySelector('.pagedjs_margin-content').appendChild(marginContent.cloneNode(true));
        });
    });
}

executionQueue.push(setupPageMargins);