import {describe, expect, it, vi} from 'vitest';
import {MathRenderer} from "../capabilities/math-renderer";

// mock global katex
globalThis.katex = {renderToString: vi.fn(() => '<span class="katex">OK</span>')} as any;

class DummyDoc {
}

describe('MathRenderer', () => {
    it('renders inline and block formulas with macros', async () => {
        // @ts-ignore
        globalThis.window = globalThis.window || ({} as any);
        (window as any).texMacros = {R: "\\mathbb{R}"};

        document.body.innerHTML = `
      <div>
        <formula>1+1</formula>
        <formula data-block>2+2</formula>
      </div>`;

        const h = new MathRenderer(new DummyDoc() as any);
        await h.onPreRendering();

        const formulas = document.querySelectorAll('formula');
        expect(formulas[0].innerHTML).toContain('katex');
        expect(formulas[1].innerHTML).toContain('katex');
        expect((globalThis as any).katex.renderToString).toHaveBeenCalled();
    });
});
