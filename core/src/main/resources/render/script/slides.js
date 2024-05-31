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
document.addEventListener('DOMContentLoaded', function() {
    const slidesDiv = document.querySelector('.reveal .slides');
    if (!slidesDiv) return;

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
    sections.forEach(section => slidesDiv.appendChild(section));

    // Initialize RevealJS with the updated DOM.
    Reveal.initialize()
});
