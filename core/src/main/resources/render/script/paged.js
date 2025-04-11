class PagedDocument extends QuarkdownDocument {
    populateExecutionQueue() {
        super.populateExecutionQueue();
        postRenderingExecutionQueue.push(setColumnCount);
    }

    copyPageMarginInitializers() {
        super.copyPageMarginInitializers();
        // <div class="page-margin-content-initializer page-margin-bottom-center">Hello</div>
        // will be copied to each page as:
        // <div class="pagedjs_margin-content">Hello</div>
        // contained in the .pagedjs_margin pagedjs_margin-top-left div.
        this.pageMarginInitializers.forEach(initializer => {
            const marginContent = document.createElement('div');
            marginContent.className = 'pagedjs_margin-content';
            marginContent.innerHTML = initializer.innerHTML;

            // Given the initializer with class "page-margin-content-initializer page-margin-bottom-center",
            // the margin class will be "pagedjs_margin-bottom-center".
            const pageMargins = document.querySelectorAll('.pagedjs_margin-' + initializer.className.split('page-margin-').pop());
            pageMargins.forEach(pageMargin => {
                pageMargin.classList.add('hasContent'); // Required by paged.js to show the content.

                // Append the content.
                const container = pageMargin.querySelector('.pagedjs_margin-content');
                container.classList.add(...initializer.classList); // Copy the classes, allows for styling.
                container.innerHTML = initializer.innerHTML; // Copy the content of the initializer to each page.
            });
        });
    }

    getParentViewport(element) {
        return element.closest('.pagedjs_area');
    }

    updatePageNumberElements() {
        const pages = document.querySelectorAll('.pagedjs_page')
        // Inject the total amount of pages into .total-page-number elements.
        const amount = pages.length;
        document.querySelectorAll('.total-page-number').forEach(total => {
            total.innerText = amount;
        });

        pages.forEach(page => {
            // Inject the current page number into .current-page-number elements.
            const number = page.getAttribute('data-page-number');
            page.querySelectorAll('.current-page-number').forEach(pageNumber => {
                pageNumber.innerText = number;
            });
        });
    }

    async onInitialDocumentReady() {
        await super.onInitialDocumentReady();
        await window.PagedPolyfill.preview()
    }

    beforeReady(content) {
        super.beforeReady(content);
        super.removeAllPageMarginInitializers()
    }

    setupBeforeReadyHook() {
        class PagedBeforeReadyHandler extends Paged.Handler {
            beforeParsed(content) {
                doc.beforeReady(content);
            }
        }
        Paged.registerHandlers(PagedBeforeReadyHandler);
    }

    setupAfterReadyHook() {
        class PagedAfterReadyHandler extends Paged.Handler {
            afterPreview() {
                postRenderingExecutionQueue.execute().then();
            }
        }
        Paged.registerHandlers(PagedAfterReadyHandler);
    }
}

// Sets the column count of each page.
// For some unknown reason, this has to be applied after the page is rendered to avoid visual glitches.
// For non-paged documents, the column count is applied directly via CSS instead (see global.css).
function setColumnCount() {
    const columnCount = getComputedStyle(document.body).getPropertyValue('--property-column-count')?.trim()
    if (!columnCount || columnCount === '') return; // No value set.

    document.querySelectorAll('.pagedjs_page_content > div').forEach(content => {
        content.style.columnCount = columnCount;
    });
}
