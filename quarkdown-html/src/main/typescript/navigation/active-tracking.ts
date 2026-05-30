/**
 * Initializes active state tracking for navigation items.
 * Uses IntersectionObserver for efficient scroll-based highlighting.
 *
 * @param navigation - The navigation element containing items with `data-target-id` attributes
 */
export function initNavigationActiveTracking(navigation: HTMLElement): void {
    const items = navigation.querySelectorAll<HTMLLIElement>('li[data-target-id], li[data-location]');
    if (items.length === 0) return;

    const NAVIGATION_CONTAINER_SELECTOR = 'nav[role="doc-toc"], nav[data-role="table-of-contents"]';
    const GENERATED_ID_PREFIX = 'qd-location-';

    const escapeForAttributeSelector = (value: string): string =>
        value.replace(/\\/g, '\\\\').replace(/"/g, '\\"');

    const toLocationToken = (location: string): string =>
        location.toLowerCase().replace(/[^a-z0-9_-]+/g, '-').replace(/^-+|-+$/g, '');

    const getAvailableId = (preferred: string): string => {
        if (!document.getElementById(preferred)) {
            return preferred;
        }

        let suffix = 2;
        while (document.getElementById(`${preferred}-${suffix}`)) {
            suffix += 1;
        }
        return `${preferred}-${suffix}`;
    };

    const ensureUniqueTargetId = (target: HTMLElement, location: string): string => {
        const hasUsableId = target.id.length > 0 && document.getElementById(target.id) === target;
        if (hasUsableId) {
            return target.id;
        }

        const locationToken = toLocationToken(location) || 'item';
        const generatedId = getAvailableId(`${GENERATED_ID_PREFIX}${locationToken}`);
        target.id = generatedId;
        return generatedId;
    };

    const resolveTargetByLocation = (location: string): HTMLElement | undefined => {
        const escapedLocation = escapeForAttributeSelector(location);
        const candidates = document.querySelectorAll<HTMLElement>(`[data-location="${escapedLocation}"]`);
        return Array.from(candidates).find(element => !element.closest(NAVIGATION_CONTAINER_SELECTOR));
    };

    const resolveNavigationTargetId = (item: HTMLLIElement): string | undefined => {
        const location = item.dataset.location;
        if (location) {
            const target = resolveTargetByLocation(location);
            if (target) {
                const targetId = ensureUniqueTargetId(target, location);
                item.dataset.targetId = targetId;
                item.querySelector<HTMLAnchorElement>('a[href^="#"]')?.setAttribute('href', `#${targetId}`);
                return targetId;
            }
        }

        return item.dataset.targetId;
    };

    // Map target IDs to their corresponding navigation items
    const targetToItem = new Map<string, HTMLLIElement>();
    items.forEach(item => {
        const targetId = resolveNavigationTargetId(item);
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
