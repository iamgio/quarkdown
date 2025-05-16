document.addEventListener('DOMContentLoaded', function () {
    const NAVBAR_HIDE_SCROLL_DELTA = 40; // Scroll delta to hide navbar
    const NAVBAR_SHOW_SCROLL_DELTA = -20; // Scroll delta to show navbar
    const VARIANT_CHANGE_SCROLL_THRESHOLD = 80; // Scroll threshold for changing the navbar variant
    const RESPONSIVE_WIDTH_QUERY = '(max-width: 767px)'; // Media query for responsive behavior
    const THROTTLE_DELAY = 30; // Delay in ms for the throttle function

    const navbar = document.getElementById('nav'); // Select navbar
    const stickySections = document.querySelectorAll('[data-sticky-section]'); // Select sticky sections
    const originalVariant = navbar.getAttribute('variant'); // Store the original value
    let lastScrollPosition = 0; // Track of the last scroll position
    let cumulativeScrollDelta = 0; // Track cumulative scroll distance
    let isAnchorLink = window.location.hash; // Check if URL contains an anchor link
    let initialLoad = true; // Flag to track initial load

    // Function to check and update the navbar variant on load
    function checkAndUpdateNavbarVariantOnLoad() {
        let initialScrollPosition = window.scrollY || document.documentElement.scrollTop;
        if (initialScrollPosition > VARIANT_CHANGE_SCROLL_THRESHOLD) {
            navbar.setAttribute('variant', 'null');
            navbar.setAttribute('scrolled', 'true');  // Set 'scrolled' to true when navbar variant changes to null
        } else {
            navbar.setAttribute('variant', originalVariant);
            navbar.setAttribute('scrolled', 'false');  // Set 'scrolled' to false when variant is reset
        }
    }

    // Function to update sticky sections based on navbar visibility
    function updateStickySections(hidden) {
        stickySections.forEach(section => {
            if (hidden) {
                section.classList.add('navbar-hidden-sticky');
            } else {
                section.classList.remove('navbar-hidden-sticky');
            }
        });
    }

    // Function to handle scroll events
    function handleScroll() {
        let currentScrollPosition = window.scrollY || document.documentElement.scrollTop;
        let scrollDelta = currentScrollPosition - lastScrollPosition;

        if ((cumulativeScrollDelta > 0 && scrollDelta < 0) || (cumulativeScrollDelta < 0 && scrollDelta > 0)) {
            cumulativeScrollDelta = scrollDelta;
        } else {
            cumulativeScrollDelta += scrollDelta;
        }

        // Apply style changes only if screen width is below threshold
        if (window.matchMedia(RESPONSIVE_WIDTH_QUERY).matches) {
            if (!initialLoad || !isAnchorLink) {
                applyNavbarStyleChanges();
            }
        }

        // After first scroll, set initialLoad to false
        initialLoad = false;

        // Update the last scroll position
        lastScrollPosition = currentScrollPosition;

        // Check scroll position for variant attribute change
        if (currentScrollPosition > VARIANT_CHANGE_SCROLL_THRESHOLD) {
            navbar.setAttribute('variant', 'null');
            navbar.setAttribute('scrolled', 'true');  // Set 'scrolled' to true when navbar variant changes to null
        } else {
            navbar.setAttribute('variant', originalVariant);
            navbar.setAttribute('scrolled', 'false');  // Set 'scrolled' to false when variant is reset
        }
    }

    // Function to apply style changes to the navbar
    function applyNavbarStyleChanges() {
        if (cumulativeScrollDelta > NAVBAR_HIDE_SCROLL_DELTA && window.scrollY > 50) {
            navbar.classList.add('navbar-hidden'); // Update navbar position
            updateStickySections(true); // Update sticky position
            cumulativeScrollDelta = 0; // Reset cumulative scroll
        } else if (cumulativeScrollDelta < NAVBAR_SHOW_SCROLL_DELTA) {
            navbar.classList.remove('navbar-hidden'); // Update navbar position
            updateStickySections(false); // Update sticky position
            cumulativeScrollDelta = 0; // Reset cumulative scroll
        }
    }

    // Throttle function to limit the rate of function execution
    function throttle(func, limit) {
        let lastFunc;
        let lastRan;
        return function () {
            const context = this;
            const args = arguments;
            if (!lastRan) {
                func.apply(context, args);
                lastRan = Date.now();
            } else {
                clearTimeout(lastFunc);
                lastFunc = setTimeout(function () {
                    if ((Date.now() - lastRan) >= limit) {
                        func.apply(context, args);
                        lastRan = Date.now();
                    }
                }, limit - (Date.now() - lastRan));
            }
        };
    }

    // Call function to check and update navbar variant on initial load
    checkAndUpdateNavbarVariantOnLoad();

    // Add scroll event listener with throttled handler
    window.addEventListener('scroll', throttle(handleScroll, THROTTLE_DELAY));
});


/* START - NO SCROLL WHEN MOBILE MENU IS OPEN + COMBO CLASS FOR TRANSPARENT STYLING */
// Set overflow:hidden on body when clicking burger menu
document.querySelector('.navbar_burger-icon-wrapper').addEventListener('click', function () {
    var element = this;
    var clicks = element.getAttribute('data-clicks') === 'true'; // because data attributes return as string

    if (clicks) {
        document.body.style.overflow = 'auto';
        document.querySelector('#nav').classList.remove('is-open')
    } else {
        document.body.style.overflow = 'hidden';
        document.querySelector('#nav').classList.add('is-open')
    }

    element.setAttribute('data-clicks', !clicks);
});

// Enable scroll on anchor link click
navLinkItems = document.querySelectorAll('.navbar a');

for (let navLink of navLinkItems) {
    navLink.addEventListener('click', function () {
        document.querySelector('body').style.overflow = 'auto';
        document.querySelector('#nav').classList.remove('is-open');
    });
}
/* END - NO SCROLL WHEN MOBILE MENU IS OPEN + COMBO CLASS FOR TRANSPARENT STYLING */


/* START - NAVLINK BOLD ON HOVER */
// Automatically assign an attribute to each navbar link with same value as the link-text
let navTopLinks = document.querySelectorAll('.navbar_dropdown-toggle .paragraph-medium')
for (let topLink of navTopLinks) {
    topLink.setAttribute('title', `${topLink.innerText}`)
}
/* END - NAVLINK BOLD ON HOVER */

/*START - Play all features video when hovering card*/
document.addEventListener('DOMContentLoaded', () => {
    const videoContainer = document.querySelector('#nav-video');

    // Ensure the video container exists before adding event listeners
    if (videoContainer) {
        const navVideo = videoContainer.querySelector('video');

        // Ensure the video element exists within the container
        if (navVideo) {
            videoContainer.addEventListener('mouseenter', function () {
                navVideo.play();
            });

            videoContainer.addEventListener('mouseleave', function () {
                navVideo.pause();
                navVideo.currentTime = 0; // Rewind the video when not hovered
            });
        }
    }
});
/*END - Play all features video when hovering card*/

/*START - Show background when dropdown menu is open */
document.addEventListener('DOMContentLoaded', () => {
    // Select the navbar menu items wrapper
    const menuWrapper = document.querySelector('.navbar_menu-items-wrapper');

    // Select all dropdowns within the menu wrapper
    const dropdownWrappers = menuWrapper.querySelectorAll('.w-dropdown');
    const overlay = menuWrapper.querySelector('.navbar_bg-overlay');

    // Function to check if any dropdown is open by checking the child elements
    function checkDropdownState() {
        let anyOpen = false;
        dropdownWrappers.forEach(wrapper => {
            const dropdownChild = wrapper.querySelector('.w-dropdown-toggle'); // Assuming the child has this class
            if (dropdownChild && dropdownChild.classList.contains('w--open')) {
                anyOpen = true;
            }
        });

        // Toggle the .is-visible class on the overlay
        if (anyOpen) {
            overlay.classList.add('is-visible');
        } else {
            overlay.classList.remove('is-visible');
        }
    }

    // Set up MutationObserver configuration
    const observerConfig = {attributes: true, attributeFilter: ['class']};

    // Function to handle mutations
    const mutationCallback = (mutationsList) => {
        for (let mutation of mutationsList) {
            if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                checkDropdownState();
            }
        }
    };

    // Create a new MutationObserver instance
    const observer = new MutationObserver(mutationCallback);

    // Observe the child of each dropdown element for changes in its class within the menu wrapper
    dropdownWrappers.forEach(wrapper => {
        const dropdownChild = wrapper.querySelector('.w-dropdown-list'); // Assuming the child has this class
        if (dropdownChild) {
            observer.observe(dropdownChild, observerConfig);
        }
    });

    // Initial check for already open dropdowns on page load
    checkDropdownState();
});

/*END - Show background when dropdown menu is open */

function showToast() {
    const toast = document.getElementById("toast");
    toast.classList.add("show");  // 显示 Toast

    // 3秒后隐藏 Toast
    setTimeout(function () {
        toast.classList.remove("show");
    }, 3000);
}