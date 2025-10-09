class SlidesDocument extends QuarkdownDocument {
    copyPageMarginInitializers() {
        super.copyPageMarginInitializers();
        // Copy the content of each global page margin initializer to the background of each section.
        // <div class="page-margin-content-initializer page-margin-bottom-center">Hello</div>
        // will be copied to each section as:
        // <div class="page-margin-content page-margin-bottom-center">Hello</div>
        this.pageMarginInitializers.forEach(pageMarginInitializer => {
            const pageMargin = document.createElement('div');
            pageMargin.className = pageMarginInitializer.className;
            pageMargin.innerHTML = pageMarginInitializer.innerHTML;

            // Append the page margin to each slide background.
            const slideBackgrounds = document.querySelectorAll('.slide-background');
            slideBackgrounds.forEach(slideBackground => {
                slideBackground.appendChild(pageMargin.cloneNode(true));
            });
        });
    }

    copyPersistentHeadings() {
        // TODO this is a prototype. Reuse for paged documents as well
        const lastHeadingPerDepth = [];
        const minLevel = 1;
        const maxLevel = 6;

        const slides = document.querySelectorAll('.reveal .slides > :is(section, div)');
        const backgrounds = document.querySelectorAll('.reveal .backgrounds > .slide-background');

        slides.forEach((slide, index) => {
            // Find the highest level heading in the slide (h1 to h6).
            for (let level = minLevel; level <= maxLevel; level++) {
                const headings = slide.querySelectorAll('h' + level);
                if (headings.length > 0) {
                    lastHeadingPerDepth[level - 1] = headings[headings.length - 1].innerHTML;
                    lastHeadingPerDepth.length = level; // Remove lower level headings.
                }
            }

            const background = backgrounds[index];
            if (!background) return;
            const lastHeadingElements = Array.of(...background.querySelectorAll('.last-heading'), ...slide.querySelectorAll('.last-heading'));
            lastHeadingElements.forEach(lastHeading => {
                const depth = parseInt(lastHeading.dataset.depth);
                lastHeading.innerHTML = lastHeadingPerDepth[depth - 1] || '';
            });
        });
    }

    handleFootnotes(footnotes) {
        footnotes.forEach(({definition, reference}) => {
            const page = this.getParentViewport(reference);
            if (!page) return;

            definition.remove();
            getOrCreateFootnoteArea(page)?.appendChild(definition);
        });
    }

    getParentViewport(element) {
        return element.closest('.reveal .slides > :is(section, .pdf-page)');
    }

    updatePageNumberElements() {
        const slides = document.querySelectorAll('.reveal .slides > :is(section, div)');

        // Inject the total amount of slides into .total-page-number elements.
        document.querySelectorAll('.total-page-number').forEach(total => {
            total.innerText = slides.length;
        });

        // Inject the current slide number into .current-page-number elements.
        // This approach allows for a static evaluation of the number, without relying on Reveal's dynamic events.
        document.querySelectorAll('.current-page-number').forEach(current => {
            // The page counter can be either in a slide or a background.
            const container = current.closest('.reveal > :is(.slides, .backgrounds)');
            const ownSlide = current.closest('.reveal > :is(.slides, .backgrounds) > :is(section, div)');

            current.innerText = Array.from(container.children).indexOf(ownSlide) + 1;
        });
    }

    usesNavigationSidebar() {
        return false;
    }

    beforeReady(content) {
        const slidesDiv = document.querySelector('.reveal .slides');
        if (!slidesDiv) return;

        super.beforeReady(content);
        super.removeAllPageMarginInitializers();

        new PageChunker(slidesDiv).chunk();
        this.initialize()
    }

    initialize() {
        // Used to check if a property was injected (see below).
        const undef = 'undefined';

        // Initialize RevealJS with the updated DOM.
        // slides_X properties are optionally set by the SlidesSettingsInitializer invisible node.
        Reveal.initialize({
            // If the center property is not explicitly set, it defaults to true unless the `--reveal-center-vertically` CSS variable of `:root` is set to `false`.
            center: typeof slides_center !== undef ? slides_center : getComputedStyle(document.documentElement).getPropertyValue('--reveal-center-vertically') !== 'false',
            controls: typeof slides_showControls !== undef ? slides_showControls : true,
            showNotes: typeof slides_showNotes !== undef ? slides_showNotes : false,
            transition: typeof slides_transitionStyle !== undef ? slides_transitionStyle : 'slide',
            transitionSpeed: typeof slides_transitionSpeed !== undef ? slides_transitionSpeed : 'default',
            hash: true,
            plugins: [RevealNotes],
        });
    }

    setupAfterReadyHook() {
        Reveal.addEventListener('ready', () => {
            if (Reveal.isPrintView()) {
                Reveal.addEventListener('pdf-ready', () => postRenderingExecutionQueue.execute());
            } else {
                postRenderingExecutionQueue.execute().then();
            }
        });
    }
}