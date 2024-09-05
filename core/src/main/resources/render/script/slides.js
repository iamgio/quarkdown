// Utility that replaces page breaks with RevealJS sections.
// Example:
// Input:
// <div class="reveal">
//     <div class="slides">
//         <p>First</p>
//         <div class="page-break"></div>
//         <p>Second</p>
//     </div>
// </div>
//
// Output:
// <div class="reveal">
//     <div class="slides">
//         <section>
//             <p>First</p>
//          </section>
//         <section>
//             <p>Second</p>
//         </section>
//     </div>
// </div>
document.addEventListener('DOMContentLoaded', function () {
    const slidesDiv = document.querySelector('.reveal .slides');
    if (!slidesDiv) return;

    // A page margin content initializer is an element that will be copied into each section,
    // and is placed on one of the page margins.
    const pageMarginInitializers = document.querySelectorAll('.page-margin-content');
    pageMarginInitializers.forEach(initializer => initializer.remove());

    const children = Array.from(slidesDiv.childNodes);
    let sections = [];
    let currentSection = document.createElement('section');

    children.forEach(child => {
        if (child.className === 'page-break') {
            // If we hit a page break, finalize the current section and start a new one.
            sections.push(currentSection);
            currentSection = document.createElement('section');
        } else {
            // Otherwise, add the child to the current section.
            currentSection.appendChild(child);
        }
    });

    // Add the last section if it has any content.
    if (currentSection.childNodes.length > 0) {
        sections.push(currentSection);
    }

    // Clear out the original slides div and add the new sections.
    slidesDiv.innerHTML = '';
    // Elements that are not part of a section yet and will be added to the next one.
    let queuedElements = [];

    sections.forEach(section => {
        // Empty slides are ignored.
        if (isBlank(section)) {
            // If the section is blank and NOT empty,
            // meaning all its children are hidden (e.g. a marker produced by the .marker function),
            // they are added to the queued elements in order to be added to the next section
            // and not produce an empty slide.
            queuedElements.push(...section.children);
        } else {
            // If there are any queued elements, they are added to the beginning of the new section.
            if (queuedElements.length > 0) {
                queuedElements.forEach(element => section.prepend(element));
                queuedElements = [];
            }
            slidesDiv.appendChild(section);
        }
    });

    // If there are any queued elements left, they are added to the last visible section.
    if (queuedElements.length > 0 && sections.length > 0) {
        queuedElements.forEach(element => slidesDiv.lastChild.appendChild(element));
        queuedElements = [];
    }

    // Copy the content of each global page margin initializer to the background of each section.
    Reveal.addEventListener('ready', function () {
        // <div class="page-margin-content-initializer page-margin-bottom-center">Hello</div>
        // will be copied to each section as:
        // <div class="page-margin-content page-margin-bottom-center">Hello</div>
        pageMarginInitializers.forEach(pageMarginInitializer => {
            const pageMargin = document.createElement('div');
            pageMargin.className = pageMarginInitializer.className;
            pageMargin.innerHTML = pageMarginInitializer.innerHTML;

            // Append the page margin to each slide background.
            const slideBackgrounds = document.querySelectorAll('.slide-background');
            slideBackgrounds.forEach(slideBackground => {
                slideBackground.appendChild(pageMargin.cloneNode(true));
            });

            updateSlideNumberElements();
        });
    });

    // Used to check if a property was injected (see below).
    const undef = 'undefined';

    // Initialize RevealJS with the updated DOM.
    // slides_X properties are optionally set by the SlidesSettingsInitializer invisible node.
    Reveal.initialize({
        // If the center property is not explicitly set, it defaults to true unless the `--reveal-center-vertically` CSS variable of `:root` is set to `false`.
        center: typeof slides_center !== undef ? slides_center : getComputedStyle(document.documentElement).getPropertyValue('--reveal-center-vertically') !== 'false',
        controls: typeof slides_showControls !== undef ? slides_showControls : true,
        transition: typeof slides_transitionStyle !== undef ? slides_transitionStyle : 'slide',
        transitionSpeed: typeof slides_transitionSpeed !== undef ? slides_transitionSpeed : 'default',
        hash: true,
    });
});

// Sets the current and total slide number to the elements with the classes .current-page-number and .total-page-number
function updateSlideNumberElements() {
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

// Whether an element is not visible in the document.
function isHidden(element) {
    return element.hasAttribute('data-hidden');
}

// Whether a slide has no visible content.
function isBlank(slide) {
    return slide.childNodes.length === 0 || Array.from(slide.children).every(isHidden);
}