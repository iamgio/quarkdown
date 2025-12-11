import {afterEach, beforeEach, describe, expect, it} from 'vitest';
import {createSidebar} from '../sidebar';

describe('createSidebar', () => {
    beforeEach(() => {
        document.body.innerHTML = '';
    });

    afterEach(() => {
        document.body.innerHTML = '';
    });

    describe('sidebar container', () => {
        it('creates a sidebar element with correct class and style', () => {
            const sidebar = createSidebar();

            expect(sidebar.className).toBe('sidebar');
            expect(sidebar.style.position).toBe('fixed');
        });

        it('appends the sidebar to the document body', () => {
            createSidebar();

            const sidebar = document.querySelector('.sidebar');
            expect(sidebar).not.toBeNull();
            expect(sidebar?.parentElement).toBe(document.body);
        });

        it('contains an ordered list element', () => {
            const sidebar = createSidebar();

            const list = sidebar.querySelector('ol');
            expect(list).not.toBeNull();
        });
    });

    describe('navigation items generation', () => {
        it('creates navigation items for h1 elements', () => {
            document.body.innerHTML = '<h1 id="title">Main Title</h1>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].classList.contains('h1')).toBe(true);
            expect(items[0].querySelector('a')?.getAttribute('href')).toBe('#title');
            expect(items[0].querySelector('span')?.textContent).toBe('Main Title');
        });

        it('creates navigation items for h2 elements', () => {
            document.body.innerHTML = '<h2 id="section">Section Header</h2>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].classList.contains('h2')).toBe(true);
            expect(items[0].querySelector('a')?.getAttribute('href')).toBe('#section');
            expect(items[0].querySelector('span')?.textContent).toBe('Section Header');
        });

        it('creates navigation items for h3 elements', () => {
            document.body.innerHTML = '<h3 id="subsection">Subsection</h3>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].classList.contains('h3')).toBe(true);
            expect(items[0].querySelector('a')?.getAttribute('href')).toBe('#subsection');
            expect(items[0].querySelector('span')?.textContent).toBe('Subsection');
        });

        it('ignores h4, h5, h6 elements', () => {
            document.body.innerHTML = `
                <h4 id="h4">H4 Title</h4>
                <h5 id="h5">H5 Title</h5>
                <h6 id="h6">H6 Title</h6>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(0);
        });

        it('creates items for mixed heading levels in document order', () => {
            document.body.innerHTML = `
                <h1 id="title">Title</h1>
                <h2 id="section1">Section 1</h2>
                <h3 id="subsection1">Subsection 1.1</h3>
                <h2 id="section2">Section 2</h2>
                <h3 id="subsection2">Subsection 2.1</h3>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(5);

            expect(items[0].classList.contains('h1')).toBe(true);
            expect(items[0].querySelector('span')?.textContent).toBe('Title');

            expect(items[1].classList.contains('h2')).toBe(true);
            expect(items[1].querySelector('span')?.textContent).toBe('Section 1');

            expect(items[2].classList.contains('h3')).toBe(true);
            expect(items[2].querySelector('span')?.textContent).toBe('Subsection 1.1');

            expect(items[3].classList.contains('h2')).toBe(true);
            expect(items[3].querySelector('span')?.textContent).toBe('Section 2');

            expect(items[4].classList.contains('h3')).toBe(true);
            expect(items[4].querySelector('span')?.textContent).toBe('Subsection 2.1');
        });
    });

    describe('data-decorative heading filtering', () => {
        it('excludes h1 with data-decorative attribute', () => {
            document.body.innerHTML = `
                <h1 id="regular">Regular Title</h1>
                <h1 id="decorative" data-decorative>Decorative Title</h1>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Regular Title');
        });

        it('excludes h2 with data-decorative attribute', () => {
            document.body.innerHTML = `
                <h2 id="regular">Regular Section</h2>
                <h2 id="decorative" data-decorative>Decorative Section</h2>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Regular Section');
        });

        it('excludes h3 with data-decorative attribute', () => {
            document.body.innerHTML = `
                <h3 id="regular">Regular Subsection</h3>
                <h3 id="decorative" data-decorative>Decorative Subsection</h3>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Regular Subsection');
        });

        it('excludes all decorative headings in mixed environments', () => {
            document.body.innerHTML = `
                <h1 id="title" data-decorative>Decorative Title</h1>
                <h2 id="section1">Section 1</h2>
                <h3 id="subsection1" data-decorative>Decorative Subsection</h3>
                <h2 id="section2" data-decorative>Decorative Section</h2>
                <h3 id="subsection2">Subsection 2</h3>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(2);
            expect(items[0].querySelector('span')?.textContent).toBe('Section 1');
            expect(items[1].querySelector('span')?.textContent).toBe('Subsection 2');
        });

        it('handles data-decorative with empty string value', () => {
            document.body.innerHTML = `
                <h1 id="title" data-decorative="">Decorative Title</h1>
                <h2 id="section">Regular Section</h2>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Regular Section');
        });

        it('handles data-decorative with truthy value', () => {
            document.body.innerHTML = `
                <h1 id="title" data-decorative="true">Decorative Title</h1>
                <h2 id="section">Regular Section</h2>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Regular Section');
        });
    });

    describe('edge cases', () => {
        it('creates empty sidebar when no headings exist', () => {
            document.body.innerHTML = '<p>Some content without headings</p>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(0);
        });

        it('creates empty sidebar when all headings are decorative', () => {
            document.body.innerHTML = `
                <h1 id="title" data-decorative>Decorative Title</h1>
                <h2 id="section" data-decorative>Decorative Section</h2>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(0);
        });

        it('handles headings with empty text content', () => {
            document.body.innerHTML = '<h1 id="empty"></h1>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('');
        });

        it('handles headings with nested HTML content', () => {
            document.body.innerHTML = '<h1 id="nested"><strong>Bold</strong> and <em>italic</em></h1>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Bold and italic');
        });

        it('handles headings with special characters', () => {
            document.body.innerHTML = '<h1 id="special">Title with &amp; "quotes" &apos;apostrophes&apos;</h1>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('span')?.textContent).toBe('Title with & "quotes" \'apostrophes\'');
        });

        it('handles headings without id attribute', () => {
            document.body.innerHTML = '<h1>No ID Title</h1>';
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(1);
            expect(items[0].querySelector('a')?.getAttribute('href')).toBe('#');
        });

        it('handles multiple calls creating multiple sidebars', () => {
            document.body.innerHTML = '<h1 id="title">Title</h1>';

            createSidebar();
            createSidebar();

            const sidebars = document.querySelectorAll('.sidebar');
            expect(sidebars.length).toBe(2);
        });
    });

    describe('complex document structures', () => {
        it('processes headings inside nested containers', () => {
            document.body.innerHTML = `
                <div class="container">
                    <article>
                        <h1 id="title">Title</h1>
                        <section>
                            <h2 id="section">Section</h2>
                        </section>
                    </article>
                </div>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(2);
            expect(items[0].querySelector('span')?.textContent).toBe('Title');
            expect(items[1].querySelector('span')?.textContent).toBe('Section');
        });

        it('handles headings scattered across multiple containers', () => {
            document.body.innerHTML = `
                <header>
                    <h1 id="main-title">Main Title</h1>
                </header>
                <main>
                    <h2 id="section1">Section 1</h2>
                </main>
                <footer>
                    <h3 id="footer-heading">Footer Heading</h3>
                </footer>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(3);
        });

        it('maintains document order regardless of DOM depth', () => {
            document.body.innerHTML = `
                <h1 id="first">First</h1>
                <div>
                    <div>
                        <h2 id="second">Second</h2>
                    </div>
                </div>
                <h3 id="third">Third</h3>
            `;
            const sidebar = createSidebar();

            const items = sidebar.querySelectorAll('li');
            expect(items.length).toBe(3);
            expect(items[0].querySelector('span')?.textContent).toBe('First');
            expect(items[1].querySelector('span')?.textContent).toBe('Second');
            expect(items[2].querySelector('span')?.textContent).toBe('Third');
        });
    });
});
