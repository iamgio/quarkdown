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
    sections.forEach(section => {
        // Empty slides are ignored.
        if (section.childElementCount > 0) {
            slidesDiv.appendChild(section);
        }
    });

    // Copy the content of each global page margin initializer to the background of each section.
    Reveal.addEventListener('ready', function (event) {
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
        });

        updateTotalSlideNumberElements()
        updateCurrentSlideNumberElements(event.indexh);
    });

    Reveal.addEventListener('slidechanged', event => updateCurrentSlideNumberElements(event.indexh));

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

function updateTotalSlideNumberElements() {
    // Inject the total amount of slides into .total-page-number elements.
    const amount = document.querySelectorAll('.reveal .slides > section').length;
    document.querySelectorAll('.total-page-number').forEach(total => {
        total.innerText = amount;
    });
}

function updateCurrentSlideNumberElements(index) {
    // Inject the current slide number into .current-page-number elements every time.
    document.querySelectorAll('.current-page-number').forEach(current => {
        current.innerText = index + 1;
    });
}