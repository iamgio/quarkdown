class PagedDocument extends QuarkdownDocument {
    populateExecutionQueue() {
        preRenderingExecutionQueue.push(() => document.body.style.opacity = '0');
        postRenderingExecutionQueue.push(() => document.body.style.opacity = '1');

        super.populateExecutionQueue();
        preRenderingExecutionQueue.push(() => this.handleFootnotesPreRendering(getFootnoteDefinitionsAndFirstReference(false)));
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

    /**
     * This is a hacky workaround for the base paged.js behavior:
     * Any change made after the pagination is done will not be processed by paged.js,
     * hence adding new content (footnotes) will cause content to overflow.
     *
     * This function takes all footnote references and creates a virtual empty space
     * of the size of the footnote definition, reserving space for it.
     * After rendering, `handleFootnotes` will remove this space and place
     * the footnote definition in the footnote area, balancing the layout.
     * @param {{definition: Element, reference: Element}[]} footnotes the footnote definitions and their first non-null reference element.
     */
    handleFootnotesPreRendering(footnotes) {
        footnotes.forEach(({definition, reference}) =>{
            reference.style.display = 'block';
            reference.style.height = definition.scrollHeight + 'px';

            // Moves the footnote definition out of the page, to keep the layout intact.
            definition.remove();
            document.body.appendChild(definition);
        });
    }

    // In paged documents, footnotes are placed in a special area at the bottom of each page reserved by paged.js.
    // Useful context: https://github.com/pagedjs/pagedjs/issues/292
    handleFootnotes(footnotes) {
        footnotes.forEach(({definition, reference}) => {
            const pageArea = this.getParentViewport(reference);
            if (!pageArea) return;
            const footnoteArea = pageArea.querySelector('.pagedjs_footnote_area > .pagedjs_footnote_content');
            if (!footnoteArea) return;
            const footnoteContent = footnoteArea.querySelector('.pagedjs_footnote_inner_content');
            if (!footnoteContent) return;

            // Moves the footnote definition to the footnote area.
            definition.remove();
            footnoteContent.appendChild(definition);

            footnoteArea.classList.remove('pagedjs_footnote_empty');
            footnoteContent.style.columnWidth = 'auto';
            pageArea.style.setProperty('--pagedjs-footnotes-height', `${footnoteArea.scrollHeight}px`);

            // Resets the temp properties set in handleFootnotesPreRendering.
            reference.style.height = 'auto';
            reference.style.display = 'inline';

            getOrCreateFootnoteRule(footnoteContent);
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

    beforeReady(content) {
        super.beforeReady(content);
        super.removeAllPageMarginInitializers()
        window.PagedPolyfill.preview()
    }

    setupAfterReadyHook() {
        class PagedAfterReadyHandler extends Paged.Handler {
            afterPageLayout(page) {
                if (storedScrollY && page.getBoundingClientRect().top > storedScrollY) {
                    restoreScrollPosition()
                }
            }

            afterRendered() {
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
