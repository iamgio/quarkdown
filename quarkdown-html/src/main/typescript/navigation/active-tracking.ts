/**
 * Initializes active state tracking for navigation items.
 * Uses IntersectionObserver for efficient scroll-based highlighting.
 *
 * @param navigation - The navigation element containing items with `data-target-id` attributes
 */
export function initNavigationActiveTracking(navigation: HTMLElement): void {
    const items = navigation.querySelectorAll<HTMLLIElement>('li[data-target-id]');
    if (items.length === 0) return;

    // Map target IDs to their corresponding navigation items
    const targetToItem = new Map<string, HTMLLIElement>();
    items.forEach(item => {
        const targetId = item.dataset.targetId;
        if (targetId) {
            targetToItem.set(targetId, item);
        }
    });

    let currentActiveItem: HTMLLIElement | null = null;

    // Track which headings are currently visible and their positions
    const visibleHeadings = new Map<string, number>();

    const observer = new IntersectionObserver(
        (entries) => {
            entries.forEach(entry => {
                const id = entry.target.id;
                if (entry.isIntersecting) {
                    visibleHeadings.set(id, entry.boundingClientRect.top);
                } else {
                    visibleHeadings.delete(id);
                }
            });

            updateActiveItem();
        },
        {
            rootMargin: '-10% 0px -60% 0px',
            threshold: 0,
        }
    );

    function updateActiveItem(): void {
        // Find the topmost visible heading
        let topmostId: string | null = null;
        let topmostPosition = Infinity;

        visibleHeadings.forEach((position, id) => {
            if (position < topmostPosition) {
                topmostPosition = position;
                topmostId = id;
            }
        });

        // If no visible headings, keep the last active or find the one above viewport
        if (!topmostId && currentActiveItem) {
            return;
        }

        const newActiveItem = topmostId ? targetToItem.get(topmostId) : null;

        if (newActiveItem && newActiveItem !== currentActiveItem) {
            currentActiveItem?.classList.remove('active');
            newActiveItem.classList.add('active');
            currentActiveItem = newActiveItem;
        }
    }

    // Observe all target headings
    targetToItem.forEach((_, targetId) => {
        const heading = document.getElementById(targetId);
        if (heading) {
            observer.observe(heading);
        }
    });

    // Initial update based on scroll position
    requestAnimationFrame(() => {
        // Find heading closest to top of viewport for initial state
        let closestItem: HTMLLIElement | undefined;
        let closestDistance = Infinity;

        for (const [targetId, item] of targetToItem) {
            const heading = document.getElementById(targetId);
            if (heading) {
                const rect = heading.getBoundingClientRect();
                const distance = Math.abs(rect.top);
                if (rect.top <= window.innerHeight * 0.4 && distance < closestDistance) {
                    closestDistance = distance;
                    closestItem = item;
                }
            }
        }

        if (closestItem) {
            closestItem.classList.add('active');
            currentActiveItem = closestItem;
        }
    });
}
