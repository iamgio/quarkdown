class PlainDocument extends QuarkdownDocument {
    constructor() {
        super();
        window.addEventListener('resize', () => {
            this.handleFootnotes(getFootnoteDefinitionsAndFirstReference());
        });
    }

    // In plain documents, footnotes are placed in the right margin area.
    handleFootnotes(footnotes) {
        const rightMarginArea = document.querySelector('#margin-area-right');
        rightMarginArea.innerHTML = '';

        // Y position of the last footnote.
        const lastDefinitionOffset = () => {
            if (rightMarginArea.children.length === 0) {
                return rightMarginArea.getBoundingClientRect().top;
            }
            const lastChild = rightMarginArea.lastElementChild;
            return lastChild.getBoundingClientRect().bottom;
        }

        footnotes.forEach(({definition, reference}) => {
            definition.remove();
            definition.style.marginTop = Math.max(0, reference.getBoundingClientRect().top - lastDefinitionOffset()) + 'px';
            rightMarginArea.appendChild(definition);
        });
    }
}