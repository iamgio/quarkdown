/**
 * Creates the main sidebar container element with initial styling.
 *
 * @returns The sidebar container element
 */
function createSidebarContainer(): HTMLDivElement {
    const sidebar = document.createElement('div');
    sidebar.className = 'sidebar';
    sidebar.style.position = 'fixed';
    return sidebar;
}

/**
 * Creates the ordered list element that will contain the navigation items.
 *
 * @returns The sidebar list element
 */
function createSidebarList(): HTMLOListElement {
    return document.createElement('ol');
}

/**
 * Creates a navigation list item for a header element.
 *
 * @param header - The header element to create a navigation item for
 * @returns The created list item element
 */
function createNavigationItem(header: Element): HTMLLIElement {
    const listItem = document.createElement('li');
    listItem.className = header.tagName.toLowerCase();
    listItem.innerHTML = `<a href="#${header.id}"><span>${header.textContent}</span></a>`;
    return listItem;
}

/**
 * Creates an active state checker function for a specific header and list item pair.
 *
 * @param header - The header element to monitor
 * @param listItem - The corresponding list item to update
 * @param getCurrentActiveItem - Function to get the currently active item
 * @param setCurrentActiveItem - Function to set the currently active item
 * @returns A function that checks and updates the active state
 */
function createActiveStateChecker(
    header: Element,
    listItem: HTMLLIElement,
    getCurrentActiveItem: () => HTMLLIElement | null,
    setCurrentActiveItem: (item: HTMLLIElement) => void
): () => void {
    return function checkForActive() {
        const rect = header.getBoundingClientRect();
        if (rect.top <= window.innerHeight * 0.5 && rect.top + rect.height >= 0) {
            const currentActive = getCurrentActiveItem();
            currentActive?.classList.remove('active');
            setCurrentActiveItem(listItem);
            listItem.classList.add('active');
        }
    };
}

/**
 * Retrieves all h1, h2, and h3 header elements that are not marked as decorative.
 *
 * @returns An array of header elements
 */
function getHeadings(): HTMLElement[] {
    const selection = document.querySelectorAll<HTMLElement>('h1, h2, h3');
    return Array.from(selection)
        .filter(header => !header.hasAttribute('data-decorative'));
}

/**
 * Processes all header elements and creates navigation items with active state tracking.
 *
 * @param sidebarList - The list element to append navigation items to
 */
function populateNavigationItems(sidebarList: HTMLOListElement): void {
    let currentActiveListItem: HTMLLIElement | null = null;

    // Getter and setter functions for the current active item
    const getCurrentActiveItem = () => currentActiveListItem;
    const setCurrentActiveItem = (item: HTMLLIElement) => {
        currentActiveListItem = item;
    };

    getHeadings().forEach(header => {
        const listItem = createNavigationItem(header);
        sidebarList.appendChild(listItem);

        const checkForActive = createActiveStateChecker(
            header,
            listItem,
            getCurrentActiveItem,
            setCurrentActiveItem
        );

        // Initial check and scroll/resize event listener
        checkForActive();
        window.addEventListener('scroll', checkForActive);
        window.addEventListener('resize', checkForActive);
    });
}

/**
 * Attaches the sidebar to the document body.
 *
 * @param sidebar - The sidebar element to attach
 */
function attachSidebarToDocument(sidebar: HTMLDivElement): void {
    document.body.appendChild(sidebar);
}

/**
 * Creates and initializes a navigation sidebar for the document.
 *
 * This function creates a fixed-position sidebar containing navigation links
 * for all h1, h2, and h3 elements in the document. It automatically tracks
 * which section is currently visible and highlights the corresponding navigation item.
 *
 * @returns The created sidebar element
 */
export function createSidebar(): HTMLDivElement {
    const sidebar = createSidebarContainer();
    const sidebarList = createSidebarList();

    populateNavigationItems(sidebarList);

    sidebar.appendChild(sidebarList);
    attachSidebarToDocument(sidebar);

    return sidebar;
}