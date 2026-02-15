"use strict";
(() => {
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __esm = (fn, res) => function __init() {
    return fn && (res = (0, fn[__getOwnPropNames(fn)[0]])(fn = 0)), res;
  };
  var __commonJS = (cb, mod) => function __require() {
    return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
  };

  // src/main/typescript/capabilities.ts
  var capabilities;
  var init_capabilities = __esm({
    "src/main/typescript/capabilities.ts"() {
      "use strict";
      capabilities = {
        /**
         * Whether to include the code highlighter document handler for syntax highlighting in code blocks.
         * @see CodeHighlighter
         */
        code: false,
        /**
         * Whether to include the math document handler for rendering mathematical formulas.
         * @see MathRenderer
         */
        math: false,
        /**
         * Whether to include the Mermaid diagram document handler for rendering diagrams.
         * @see DiagramRenderer
         */
        mermaid: false
      };
    }
  });

  // src/main/typescript/queue/async-execution-queue.ts
  var AsyncExecutionQueue;
  var init_async_execution_queue = __esm({
    "src/main/typescript/queue/async-execution-queue.ts"() {
      "use strict";
      AsyncExecutionQueue = class {
        constructor() {
          /** Array of async functions waiting to be executed */
          this.queue = [];
          /** Callback function executed after all queued functions complete */
          this.onComplete = [];
          /** Flag indicating whether the queue has been executed and completed */
          this.completed = false;
        }
        /**
         * Adds an asynchronous function to the execution queue.
         *
         * @param fn - An async function that returns a Promise<void> to be executed later
         */
        pushAsync(fn) {
          this.queue.push(fn);
        }
        /**
         * Adds a synchronous function to the execution queue.
         *
         * This method wraps the provided synchronous function in an async function
         * that returns a resolved Promise, allowing it to be executed in the same
         * manner as other async functions in the queue.
         *
         * @param fn - A synchronous function to be executed later
         */
        push(fn) {
          this.queue.push(async () => fn());
        }
        /**
         * Registers a callback to be called after all queued functions have executed.
         *
         * @param fn - A function to be called once after `execute()` completes
         */
        addOnComplete(fn) {
          this.onComplete.push(fn);
        }
        /**
         * Executes all queued functions in parallel and clears the queue.
         *
         * This method uses Promise.all() to run all queued functions concurrently,
         * waits for all of them to complete, then clears the queue and calls the
         * onExecute callback. After execution, the queue is marked as completed.
         *
         * @returns A Promise that resolves when all queued functions have completed
         */
        async execute() {
          await Promise.all(this.queue.map(async (fn) => fn()));
          this.queue = [];
          this.onComplete?.forEach((fn) => fn());
          this.completed = true;
        }
        /**
         * Checks whether the queue has been executed and completed.
         *
         * @returns true if `execute()` has been called and completed, false otherwise
         */
        isCompleted() {
          return this.completed;
        }
      };
    }
  });

  // src/main/typescript/queue/execution-queues.ts
  var preRenderingExecutionQueue, postRenderingExecutionQueue;
  var init_execution_queues = __esm({
    "src/main/typescript/queue/execution-queues.ts"() {
      "use strict";
      init_async_execution_queue();
      preRenderingExecutionQueue = new AsyncExecutionQueue();
      postRenderingExecutionQueue = new AsyncExecutionQueue();
    }
  });

  // src/main/typescript/document/document-handler.ts
  function filterConditionalHandlers(handlers) {
    return handlers.filter((handler) => handler instanceof DocumentHandler);
  }
  var DocumentHandler;
  var init_document_handler = __esm({
    "src/main/typescript/document/document-handler.ts"() {
      "use strict";
      init_execution_queues();
      DocumentHandler = class {
        /**
         * @param quarkdownDocument - The document instance this handler manages
         */
        constructor(quarkdownDocument) {
          this.quarkdownDocument = quarkdownDocument;
        }
        /**
         * Pushes this handler's lifecycle methods to the appropriate execution queues.
         * Pre-rendering handlers are added to the pre-rendering queue,
         * post-rendering handlers are added to the post-rendering queue.
         */
        pushToQueue() {
          this.init?.();
          if (this.onPreRendering) {
            preRenderingExecutionQueue.pushAsync(() => this.onPreRendering());
          }
          if (this.onPostRendering) {
            postRenderingExecutionQueue.pushAsync(() => this.onPostRendering());
          }
        }
      };
    }
  });

  // src/main/typescript/navigation/active-tracking.ts
  function initNavigationActiveTracking(navigation) {
    const items = navigation.querySelectorAll("li[data-target-id]");
    if (items.length === 0) return;
    const targetToItem = /* @__PURE__ */ new Map();
    items.forEach((item) => {
      const targetId = item.dataset.targetId;
      if (targetId) {
        targetToItem.set(targetId, item);
      }
    });
    let currentActiveItem = null;
    const visibleHeadings = /* @__PURE__ */ new Map();
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
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
        rootMargin: "-10% 0px -60% 0px",
        threshold: 0
      }
    );
    function updateActiveItem() {
      let topmostId = null;
      let topmostPosition = Infinity;
      visibleHeadings.forEach((position, id) => {
        if (position < topmostPosition) {
          topmostPosition = position;
          topmostId = id;
        }
      });
      if (!topmostId && currentActiveItem) {
        return;
      }
      const newActiveItem = topmostId ? targetToItem.get(topmostId) : null;
      if (newActiveItem && newActiveItem !== currentActiveItem) {
        currentActiveItem?.classList.remove("active");
        newActiveItem.classList.add("active");
        currentActiveItem = newActiveItem;
      }
    }
    targetToItem.forEach((_, targetId) => {
      const heading = document.getElementById(targetId);
      if (heading) {
        observer.observe(heading);
      }
    });
    requestAnimationFrame(() => {
      let closestItem;
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
        closestItem.classList.add("active");
        currentActiveItem = closestItem;
      }
    });
  }
  var init_active_tracking = __esm({
    "src/main/typescript/navigation/active-tracking.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/document/handlers/sidebar.ts
  var Sidebar;
  var init_sidebar = __esm({
    "src/main/typescript/document/handlers/sidebar.ts"() {
      "use strict";
      init_document_handler();
      init_active_tracking();
      Sidebar = class extends DocumentHandler {
        async onPostRendering() {
          const template = document.querySelector("#sidebar-template");
          if (!template) return;
          const sidebar = template.content.firstElementChild?.cloneNode(true);
          if (!sidebar) return;
          sidebar.style.position = "fixed";
          document.body.appendChild(sidebar);
          template.remove();
          initNavigationActiveTracking(sidebar);
        }
      };
    }
  });

  // src/main/typescript/footnotes/footnote-lookup.ts
  function getFootnoteDefinitions(sorted) {
    const definitions = Array.from(document.querySelectorAll(".footnote-definition"));
    if (!sorted) {
      return definitions;
    }
    return definitions.sort((a, b) => {
      const indexA = parseInt(a.dataset.footnoteIndex || "0");
      const indexB = parseInt(b.dataset.footnoteIndex || "0");
      return indexA - indexB;
    });
  }
  function getFootnoteFirstReference(definitionId) {
    return document.querySelector(`.footnote-reference[data-definition="${definitionId}"]`);
  }
  function getFootnoteDefinitionsAndFirstReference(sorted = true) {
    const definitions = getFootnoteDefinitions(sorted);
    return definitions.map((definition) => {
      const reference = getFootnoteFirstReference(definition.id);
      return reference ? { reference, definition } : null;
    }).filter((item) => item !== null);
  }
  var init_footnote_lookup = __esm({
    "src/main/typescript/footnotes/footnote-lookup.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/document/handlers/footnotes/footnotes-document-handler.ts
  var FootnotesDocumentHandler;
  var init_footnotes_document_handler = __esm({
    "src/main/typescript/document/handlers/footnotes/footnotes-document-handler.ts"() {
      "use strict";
      init_document_handler();
      init_footnote_lookup();
      FootnotesDocumentHandler = class extends DocumentHandler {
        constructor() {
          super(...arguments);
          /** Footnote pairs (reference + definition) collected during pre-rendering. */
          this.footnotes = [];
        }
        async onPreRendering() {
          this.footnotes = getFootnoteDefinitionsAndFirstReference();
        }
      };
    }
  });

  // src/main/typescript/document/handlers/footnotes/footnotes-plain.ts
  var FootnotesPlain;
  var init_footnotes_plain = __esm({
    "src/main/typescript/document/handlers/footnotes/footnotes-plain.ts"() {
      "use strict";
      init_plain_document();
      init_footnotes_document_handler();
      FootnotesPlain = class extends FootnotesDocumentHandler {
        /** Sets up listener to re-render footnotes on resize. */
        init() {
          window.addEventListener("resize", () => this.onPostRendering?.());
        }
        /**
         * Calculates the bottom offset of the last definition in the margin area.
         * @param marginArea - The margin area containing footnote definitions
         * @returns The bottom offset in pixels, or the top of the margin area if empty
         */
        getLastDefinitionOffset(marginArea) {
          const lastChild = marginArea.lastElementChild;
          return lastChild ? lastChild.getBoundingClientRect().bottom : marginArea.getBoundingClientRect().top;
        }
        /**
         * Renders footnotes in the right margin area, positioned to align with their references.
         * Removes footnotes from their original locations and repositions them in the margin.
         */
        async onPostRendering() {
          const rightMarginArea = getRightMarginArea();
          if (!rightMarginArea) return;
          rightMarginArea.innerHTML = "";
          this.footnotes.forEach(({ reference, definition }) => {
            definition.remove();
            definition.style.marginTop = Math.max(
              0,
              reference.getBoundingClientRect().top - this.getLastDefinitionOffset(rightMarginArea)
            ) + "px";
            rightMarginArea.appendChild(definition);
          });
        }
      };
    }
  });

  // src/main/typescript/document/type/plain-document.ts
  function getRightMarginArea() {
    return document.querySelector("#margin-area-right");
  }
  var PlainDocument;
  var init_plain_document = __esm({
    "src/main/typescript/document/type/plain-document.ts"() {
      "use strict";
      init_sidebar();
      init_execution_queues();
      init_footnotes_plain();
      PlainDocument = class {
        /**
         * @returns The document element
         */
        getParentViewport(_element) {
          return document.documentElement;
        }
        /** Sets up pre-rendering to execute when DOM content is loaded */
        setupPreRenderingHook() {
          document.addEventListener("DOMContentLoaded", async () => {
            await preRenderingExecutionQueue.execute();
          });
        }
        /** No post-rendering hook needed for plain documents */
        setupPostRenderingHook() {
        }
        /** Executes post-rendering queue since pre- and post-rendering overlap for plain documents */
        initializeRendering() {
          postRenderingExecutionQueue.execute().then();
        }
        getHandlers() {
          return [
            new Sidebar(this),
            new FootnotesPlain(this)
          ];
        }
      };
    }
  });

  // src/main/typescript/document/handlers/inline-collapsibles.ts
  var InlineCollapsibles;
  var init_inline_collapsibles = __esm({
    "src/main/typescript/document/handlers/inline-collapsibles.ts"() {
      "use strict";
      init_document_handler();
      InlineCollapsibles = class extends DocumentHandler {
        async onPostRendering() {
          const collapsibles = document.querySelectorAll(".inline-collapse");
          collapsibles.forEach((span) => {
            span.addEventListener("click", () => this.toggleCollapse(span));
          });
        }
        toggleCollapse(span) {
          const fullText = span.dataset.fullText;
          const collapsedText = span.dataset.collapsedText;
          const collapsed = span.dataset.collapsed === "true";
          const content = collapsed ? fullText : collapsedText;
          if (!content) return;
          span.dataset.collapsed = (!collapsed).toString();
          const isUserDefined = span.closest(".error") === null;
          if (isUserDefined) {
            span.innerHTML = content;
          } else {
            span.textContent = content;
          }
        }
      };
    }
  });

  // src/main/typescript/document/handlers/remaining-height.ts
  var RemainingHeight;
  var init_remaining_height = __esm({
    "src/main/typescript/document/handlers/remaining-height.ts"() {
      "use strict";
      init_document_handler();
      RemainingHeight = class extends DocumentHandler {
        async onPostRendering() {
          const fillHeightElements = document.querySelectorAll(".fill-height");
          fillHeightElements.forEach((element) => {
            const contentArea = this.quarkdownDocument.getParentViewport(element);
            if (!contentArea) return;
            const remainingHeight = contentArea.getBoundingClientRect().bottom - element.getBoundingClientRect().top;
            element.style.setProperty("--viewport-remaining-height", `${remainingHeight}px`);
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/capabilities/math-renderer.ts
  var MathRenderer;
  var init_math_renderer = __esm({
    "src/main/typescript/document/handlers/capabilities/math-renderer.ts"() {
      "use strict";
      init_document_handler();
      MathRenderer = class extends DocumentHandler {
        async onPreRendering() {
          const texMacros = window.texMacros;
          const formulas = document.querySelectorAll("formula");
          formulas.forEach((formula) => {
            const content = formula.textContent;
            const isBlock = formula.dataset.block === "";
            if (!content) return;
            formula.innerHTML = katex.renderToString(content, {
              throwOnError: false,
              displayMode: isBlock,
              macros: texMacros || {}
            });
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/capabilities/code-highlighter.ts
  var CodeHighlighter;
  var init_code_highlighter = __esm({
    "src/main/typescript/document/handlers/capabilities/code-highlighter.ts"() {
      "use strict";
      init_document_handler();
      CodeHighlighter = class extends DocumentHandler {
        init() {
          hljs.addPlugin(new CopyButtonPlugin());
        }
        async onPostRendering() {
          hljs.highlightAll();
          this.initLineNumbers();
          this.focusCodeLines();
        }
        /**
         * Adds line numbers to code blocks with the 'hljs' class, excluding those marked
         * with 'nohljsln' class.
         */
        initLineNumbers() {
          const codeBlocks = document.querySelectorAll("code.hljs:not(.nohljsln)");
          codeBlocks.forEach((code) => {
            hljs.lineNumbersBlockSync(code);
          });
        }
        /**
         * Applies visual focus to specific line ranges in code blocks.
         *
         * This method processes code blocks with the 'focus-lines' class and highlights
         * lines within the specified range using 'data-focus-start' and 'data-focus-end'
         * attributes. Supports open ranges where either start or end can be omitted.
         *
         * Range behavior:
         * - If start is NaN or missing: focuses from beginning up to end
         * - If end is NaN or missing: focuses from start to the last line
         * - If both are specified: focuses the exact range (inclusive)
         *
         * @example
         * ```html
         * <!-- Focus lines 5-10 -->
         * <code class="focus-lines" data-focus-start="5" data-focus-end="10">...</code>
         *
         * <!-- Focus from line 3 to end -->
         * <code class="focus-lines" data-focus-start="3">...</code>
         *
         * <!-- Focus from beginning to line 8 -->
         * <code class="focus-lines" data-focus-end="8">...</code>
         * ```
         */
        focusCodeLines() {
          const focusableCodeBlocks = document.querySelectorAll("code.focus-lines");
          focusableCodeBlocks.forEach((codeBlock) => {
            const focusRange = this.extractFocusRange(codeBlock);
            this.applyFocusToLines(codeBlock, focusRange);
          });
        }
        /**
         * Extracts the focus range from a code block's data attributes.
         *
         * @param codeBlock The code block element to extract range from
         * @returns An object containing the parsed start and end line numbers
         */
        extractFocusRange(codeBlock) {
          const start = parseInt(codeBlock.dataset.focusStart || "0");
          const end = parseInt(codeBlock.dataset.focusEnd || "0");
          return { start, end };
        }
        /**
         * Applies the 'focused' CSS class to lines within the specified range.
         *
         * @param codeBlock The code block containing the lines to focus
         * @param focusRange Object containing start and end line numbers
         */
        applyFocusToLines(codeBlock, focusRange) {
          const lines = codeBlock.querySelectorAll(".hljs-ln-line");
          lines.forEach((line) => {
            const lineNumber = parseInt(line.dataset.lineNumber || "0");
            if (this.isLineInFocusRange(lineNumber, focusRange)) {
              line.classList.add("focused");
            }
          });
        }
        /**
         * Determines if a line number falls within the focus range.
         *
         * Supports open ranges where NaN values indicate unbounded ranges:
         * - NaN start means focus from beginning
         * - NaN end means focus to the end
         *
         * @param lineNumber The line number to check
         * @param focusRange The focus range with start and end boundaries
         * @returns True if the line should be focused, false otherwise
         */
        isLineInFocusRange(lineNumber, focusRange) {
          const { start, end } = focusRange;
          const isAfterStart = isNaN(start) || lineNumber >= start;
          const isBeforeEnd = isNaN(end) || lineNumber <= end;
          return isAfterStart && isBeforeEnd;
        }
      };
    }
  });

  // src/main/typescript/util/hash.ts
  function hashCode(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = (hash << 5) - hash + str.charCodeAt(i);
      hash |= 0;
    }
    return hash.toString();
  }
  var init_hash = __esm({
    "src/main/typescript/util/hash.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/document/handlers/capabilities/mermaid-renderer.ts
  var MermaidRenderer;
  var init_mermaid_renderer = __esm({
    "src/main/typescript/document/handlers/capabilities/mermaid-renderer.ts"() {
      "use strict";
      init_document_handler();
      init_hash();
      MermaidRenderer = class extends DocumentHandler {
        init() {
          mermaid.initialize({ startOnLoad: false });
        }
        /** Processes all Mermaid diagrams in the document. */
        async onPreRendering() {
          const diagrams = document.querySelectorAll(".mermaid:not([data-processed])");
          const renderPromises = Array.from(diagrams).map(
            (element) => this.loadFromCacheOrRender(element)
          );
          await Promise.all(renderPromises);
          this.realignDiagramContents();
        }
        /**
         * Renders a single Mermaid diagram element, using cached results when available.
         *
         * The caching mechanism uses session storage with a hash of the diagram content
         * as the key. This ensures that identical diagrams are only rendered once per
         * browser session, significantly improving performance for documents with
         * repeated or unchanged diagrams.
         *
         * @param element The HTML element containing the Mermaid diagram text
         */
        async loadFromCacheOrRender(element) {
          const code = element.textContent?.trim() || "";
          const id = "mermaid-" + hashCode(code);
          const cachedSvg = sessionStorage.getItem(id);
          element.dataset.processed = "true";
          if (cachedSvg) {
            console.debug("Using cached SVG for diagram:", id);
            element.innerHTML = cachedSvg;
            return;
          }
          console.debug("Rendering diagram:", id);
          const diagram = await mermaid.render(id, code, element);
          console.log(diagram);
          const svg = diagram.svg;
          element.innerHTML = svg;
          sessionStorage.setItem(id, svg);
        }
        /**
         * Calculates an appropriate scale percentage for a diagram based on its aspect ratio.
         *
         * Uses a scaling formula that considers the diagram's width-to-height ratio
         * to determine an optimal display size. Wider diagrams get larger scales while
         * taller diagrams are kept more compact.
         *
         * @param svg The SVG element containing the rendered diagram
         * @returns A percentage value (0-100) representing the optimal scale
         */
        calculateNewDiagramScale(svg) {
          const scaleFactor = 0.2;
          const scaleOffset = 0.4;
          const maxScale = 100;
          const width = svg.viewBox.baseVal.width || svg.clientWidth || 1;
          const height = svg.viewBox.baseVal.height || svg.clientHeight || 1;
          const aspectRatio = width / height;
          const scale = (scaleOffset + scaleFactor * aspectRatio) * maxScale;
          return Math.min(maxScale, scale);
        }
        /**
         * Applies styling adjustments to improve diagram presentation and alignment.
         */
        realignDiagramContents() {
          document.querySelectorAll(".mermaid").forEach((diagram) => {
            diagram.style.width = "100%";
            const svg = diagram.querySelector("svg");
            if (!svg) return;
            svg.style.width = this.calculateNewDiagramScale(svg) + "%";
          });
          document.querySelectorAll(".mermaid foreignObject").forEach((obj) => {
            obj.style.display = "grid";
          });
        }
      };
    }
  });

  // src/main/typescript/document/global-handlers.ts
  function getGlobalHandlers(document2) {
    return [
      new InlineCollapsibles(document2),
      new RemainingHeight(document2),
      capabilities.code && new CodeHighlighter(document2),
      capabilities.math && new MathRenderer(document2),
      capabilities.mermaid && new MermaidRenderer(document2)
    ];
  }
  var init_global_handlers = __esm({
    "src/main/typescript/document/global-handlers.ts"() {
      "use strict";
      init_capabilities();
      init_inline_collapsibles();
      init_remaining_height();
      init_math_renderer();
      init_code_highlighter();
      init_mermaid_renderer();
    }
  });

  // src/main/typescript/document/quarkdown-document.ts
  function prepare(document2) {
    const handlers = filterConditionalHandlers([...document2.getHandlers(), ...getGlobalHandlers(document2)]);
    handlers.forEach((handler) => handler.pushToQueue());
    document2.setupPreRenderingHook();
    document2.setupPostRenderingHook();
    preRenderingExecutionQueue.addOnComplete(() => document2.initializeRendering());
  }
  var init_quarkdown_document = __esm({
    "src/main/typescript/document/quarkdown-document.ts"() {
      "use strict";
      init_document_handler();
      init_execution_queues();
      init_global_handlers();
    }
  });

  // src/main/typescript/live/live-preview.ts
  function notifyLivePreview(event, data = {}) {
    if (!window.parent || window.parent === window) return;
    try {
      window.parent.postMessage(
        {
          source: MESSAGE_SOURCE,
          event,
          data,
          timestamp: Date.now()
        },
        TARGET_ORIGIN
      );
    } catch (e) {
      console.error("Failed to post message to parent", e);
    }
  }
  var MESSAGE_SOURCE, TARGET_ORIGIN;
  var init_live_preview = __esm({
    "src/main/typescript/live/live-preview.ts"() {
      "use strict";
      MESSAGE_SOURCE = "quarkdown";
      TARGET_ORIGIN = "*";
    }
  });

  // src/main/typescript/util/visibility.ts
  function isHidden(element) {
    return element.hasAttribute("data-hidden");
  }
  function isBlank(element) {
    return element.childNodes.length === 0 || Array.from(element.children).every((child) => isHidden(child));
  }
  var init_visibility = __esm({
    "src/main/typescript/util/visibility.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/chunker/page-chunker.ts
  var PageChunker;
  var init_page_chunker = __esm({
    "src/main/typescript/chunker/page-chunker.ts"() {
      "use strict";
      init_visibility();
      PageChunker = class {
        /** Initializes the chunker with the container element to be chunked. */
        constructor(container) {
          this.chunks = [];
          this.container = container;
        }
        /**
         * Generates chunks based on the page break elements.
         * Page break elements are not preserved in the chunked output.
         * @param createElement Function that creates a new chunk element.
         */
        generateChunks(createElement) {
          const chunks = [];
          let currentChunk = createElement();
          Array.from(this.container.children).forEach((child) => {
            const el = child;
            if (el.className === "page-break") {
              chunks.push(currentChunk);
              currentChunk = createElement();
            } else {
              currentChunk.appendChild(child);
            }
          });
          if (currentChunk.childNodes.length > 0) {
            chunks.push(currentChunk);
          }
          this.chunks = chunks;
        }
        /** Applies the generated chunks to the container, replacing its content. */
        apply() {
          this.container.innerHTML = "";
          let queuedElements = [];
          this.chunks.forEach((chunk) => {
            if (isBlank(chunk)) {
              queuedElements.push(...Array.from(chunk.children));
            } else {
              if (queuedElements.length > 0) {
                queuedElements.forEach((element) => chunk.prepend(element));
                queuedElements = [];
              }
              this.container.appendChild(chunk);
            }
          });
          if (queuedElements.length > 0 && this.chunks.length > 0) {
            const last2 = this.container.lastElementChild;
            if (last2) {
              queuedElements.forEach((element) => last2.appendChild(element));
            }
            queuedElements = [];
          }
        }
        /**
         * Chunks the container into sections based on page breaks.
         * Page breaks are not preserved in the output, and empty chunks are ignored.
         * The container's content is replaced with the chunked sections.
         * @param chunkTagName The tag name to use for chunk elements (default is "section").
         */
        chunk(chunkTagName = "section") {
          const createElement = () => {
            const element = document.createElement(chunkTagName);
            element.className = "chunk";
            return element;
          };
          this.generateChunks(createElement);
          this.apply();
        }
      };
    }
  });

  // src/main/typescript/document/handlers/page-margins/page-margins-document-handler.ts
  var PageMarginsDocumentHandler;
  var init_page_margins_document_handler = __esm({
    "src/main/typescript/document/handlers/page-margins/page-margins-document-handler.ts"() {
      "use strict";
      init_document_handler();
      PageMarginsDocumentHandler = class extends DocumentHandler {
        /**
         * @param page The page or element to get initializers from
         * @return An array of page margin initializer elements within the given page
         */
        selectPageMarginInitializers(page) {
          return Array.from(page.querySelectorAll(".page-margin-content"));
        }
        /**
         * Collects all page margin content initializers and hides them from the document.
         * This prevents them from being displayed before proper positioning.
         */
        async onPreRendering() {
          this.selectPageMarginInitializers(document.body).forEach((initializer) => {
            initializer.setAttribute("data-hidden", "true");
            initializer.style.display = "none";
          });
        }
        /**
         * Called after the main rendering process is complete,
         * this function is responsible for injecting page margin content
         * into the document at appropriate locations on each page.
         *
         * It processes each page, and stores active margin initializers.
         * Since #281, a page margin begins appearing from the page where the initializer is defined,
         * and continues to appear on subsequent pages unless overridden.
         */
        async onPostRendering() {
          const activeByPosition = /* @__PURE__ */ new Map();
          this.quarkdownDocument.getPages().forEach((page) => {
            const localInitializers = this.selectPageMarginInitializers(page);
            localInitializers.forEach((initializer) => {
              activeByPosition.set(initializer.className, initializer);
              initializer.remove();
            });
            activeByPosition.forEach((initializer) => {
              const marginPositionName = this.getMarginPositionName(initializer, page);
              if (marginPositionName) {
                this.apply(initializer, page, marginPositionName);
              }
            });
          });
        }
        /**
         * Gets the margin position name for the given page margin initializer, depending on whether the page is left or right.
         * @param initializer The page margin initializer element
         * @param page The page the margin will be applied to
         * @return The margin position name (e.g., "top-left", "bottom-center"), if defined
         */
        getMarginPositionName(initializer, page) {
          const pageType = this.quarkdownDocument.getPageType(page);
          return initializer.getAttribute(`data-on-${pageType}-page`);
        }
        /**
         * Copies the class list from the initializer to the target margin element,
         * adding the specific margin position class.
         * @param target The target margin element to which classes will be added
         * @param initializer The page margin initializer element
         * @param marginPositionName The margin position name (e.g., "top-left", "bottom-center")
         */
        pushMarginClassList(target, initializer, marginPositionName) {
          target.classList.add(
            `page-margin-${marginPositionName}`,
            ...initializer.classList
          );
        }
      };
    }
  });

  // src/main/typescript/document/handlers/page-margins/page-margins-slides.ts
  var PageMarginsSlides;
  var init_page_margins_slides = __esm({
    "src/main/typescript/document/handlers/page-margins/page-margins-slides.ts"() {
      "use strict";
      init_page_margins_document_handler();
      PageMarginsSlides = class extends PageMarginsDocumentHandler {
        /**
         * Copies all page margin initializers to the slide background.
         */
        apply(initializer, page, marginPositionName) {
          const pageMargin = document.createElement("div");
          this.pushMarginClassList(pageMargin, initializer, marginPositionName);
          pageMargin.innerHTML = initializer.innerHTML;
          page.background.appendChild(pageMargin);
        }
      };
    }
  });

  // src/main/typescript/footnotes/footnote-dom.ts
  function getOrCreateFootnoteRule(footnoteArea) {
    const footnoteRuleClassName = "footnote-rule";
    const existingRule = footnoteArea.querySelector(`.${footnoteRuleClassName}`);
    if (existingRule) return existingRule;
    const rule = document.createElement("div");
    rule.className = footnoteRuleClassName;
    footnoteArea.insertAdjacentElement("afterbegin", rule);
    return rule;
  }
  function getOrCreateFootnoteArea(page) {
    const className = "footnote-area";
    let footnoteArea = page.querySelector(`.${className}`);
    if (footnoteArea) return footnoteArea;
    footnoteArea = document.createElement("div");
    footnoteArea.className = className;
    page.appendChild(footnoteArea);
    getOrCreateFootnoteRule(footnoteArea);
    return footnoteArea;
  }
  var init_footnote_dom = __esm({
    "src/main/typescript/footnotes/footnote-dom.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/document/handlers/footnotes/footnotes-slides.ts
  var FootnotesSlides;
  var init_footnotes_slides = __esm({
    "src/main/typescript/document/handlers/footnotes/footnotes-slides.ts"() {
      "use strict";
      init_footnote_dom();
      init_footnotes_document_handler();
      FootnotesSlides = class extends FootnotesDocumentHandler {
        async onPostRendering() {
          this.footnotes.forEach(({ reference, definition }) => {
            const page = this.quarkdownDocument.getParentViewport(reference);
            if (!page) return;
            const footnoteAreaParent = page.classList.contains("pdf-page") ? page.querySelector("section") : page;
            definition.remove();
            getOrCreateFootnoteArea(footnoteAreaParent)?.appendChild(definition);
          });
        }
      };
    }
  });

  // src/main/typescript/util/id.ts
  function getAnchorTargetId(link) {
    const href = link.getAttribute("href");
    if (!href || !href.startsWith("#")) {
      return void 0;
    }
    let decoded;
    try {
      decoded = decodeURIComponent(href);
    } catch {
      return void 0;
    }
    const id = decoded.slice(1);
    return id.length > 0 ? id : void 0;
  }
  var init_id = __esm({
    "src/main/typescript/util/id.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/document/handlers/page-numbers.ts
  var PageNumbers;
  var init_page_numbers = __esm({
    "src/main/typescript/document/handlers/page-numbers.ts"() {
      "use strict";
      init_document_handler();
      init_id();
      PageNumbers = class extends DocumentHandler {
        /**
         * Gets all elements that display the total page count.
         * @returns NodeList of total page number elements (`.total-page-number`)
         */
        getTotalPageNumberElements() {
          return document.querySelectorAll(".total-page-number");
        }
        /**
         * Gets all elements that display the current page number.
         * @param page - The page element to search within
         * @returns NodeList of current page number elements (`.current-page-number`)
         */
        getCurrentPageNumberElements(page) {
          return page.querySelectorAll(".current-page-number");
        }
        /**
         * Finds all page number reset markers contained in the given page.
         */
        getPageNumberResetMarkers(page) {
          return Array.from(page.querySelectorAll(".page-number-reset"));
        }
        /**
         * Updates all total page number elements with the total count of pages.
         */
        updateTotalPageNumbers(pages) {
          const amount = pages.length;
          this.getTotalPageNumberElements().forEach((total) => {
            total.innerText = amount.toString();
          });
        }
        /**
         * Updates all current page number elements with their respective (possibly reset) page numbers.
         */
        updateCurrentPageNumbers(pages) {
          let pageNumber = 1;
          pages.forEach((page) => {
            const resetMarkers = this.getPageNumberResetMarkers(page);
            resetMarkers.forEach((marker) => {
              const requested = parseInt(marker.dataset.start || "1", 10);
              if (Number.isFinite(requested) && requested > 0) {
                pageNumber = requested;
              }
            });
            this.quarkdownDocument.setDisplayPageNumber(page, pageNumber);
            this.getCurrentPageNumberElements(page).forEach((pageNumberElement) => {
              pageNumberElement.innerText = pageNumber.toString();
            });
            pageNumber += 1;
          });
        }
        /**
         * Updates table of contents entries so they display the logical (reset-aware) page numbers.
         */
        updateTableOfContentsPageNumbers() {
          const tocs = document.querySelectorAll('nav[data-role="table-of-contents"]');
          tocs.forEach((nav) => {
            nav.querySelectorAll(':scope a[href^="#"]').forEach((anchor) => {
              const targetId = getAnchorTargetId(anchor);
              const target = targetId ? document.getElementById(targetId) : void 0;
              const displayNumber = target ? this.quarkdownDocument.getPageNumber(this.quarkdownDocument.getPage(target)) : void 0;
              this.setTableOfContentsPageNumber(anchor, displayNumber?.toString());
            });
          });
        }
        /**
         * Sets or updates the page number badge within a table of contents entry.
         * @param anchor - The anchor element representing the TOC entry
         * @param value - The page number to set (if undefined, the badge will be created but left empty)
         */
        setTableOfContentsPageNumber(anchor, value) {
          let badge = anchor.querySelector(".toc-page-number");
          if (!badge) {
            badge = document.createElement("span");
            badge.className = "toc-page-number";
            anchor.appendChild(badge);
          }
          if (value) {
            badge.innerText = value;
          }
        }
        /**
         * Updates both total and current page numbers after rendering completes.
         */
        async onPostRendering() {
          const pages = this.quarkdownDocument.getPages();
          this.updateTotalPageNumbers(pages);
          this.updateCurrentPageNumbers(pages);
          this.updateTableOfContentsPageNumbers();
        }
      };
    }
  });

  // src/main/typescript/document/handlers/persistent-headings.ts
  var MIN_HEADING_LEVEL, MAX_HEADING_LEVEL, PersistentHeadings;
  var init_persistent_headings = __esm({
    "src/main/typescript/document/handlers/persistent-headings.ts"() {
      "use strict";
      init_document_handler();
      MIN_HEADING_LEVEL = 1;
      MAX_HEADING_LEVEL = 6;
      PersistentHeadings = class extends DocumentHandler {
        constructor() {
          super(...arguments);
          /**
           * Array storing the most recent heading HTML content at each depth level.
           * Index 0 corresponds to h1, index 1 to h2, etc.
           */
          this.lastHeadingPerDepth = [];
        }
        /**
         * Scans a page for headings (h1-h6) and updates the internal heading history.
         * Only the last heading of the highest level found is stored, and lower level headings are cleared.
         *
         * @example
         * If the container has:
         * ```html
         * <h2>Title</h2>
         * <h3>Subtitle</h3>
         * <h2>Another Title</h2>
         * ```
         *
         * Then after calling this method, `lastHeadingPerDepth` will be:
         * ```typescript
         * ["", "Another Title", "", "", "", ""] // h1 is empty, h2 is "Another Title", h3 has been cleared
         * ```
         *
         * @param page - The page to scan for headings
         */
        overwriteLastHeadings(page) {
          for (let depth = MIN_HEADING_LEVEL; depth <= MAX_HEADING_LEVEL; depth++) {
            const headings = page.querySelectorAll(`h${depth}:not([data-decorative])`);
            if (headings.length > 0) {
              this.lastHeadingPerDepth[depth - 1] = headings[headings.length - 1].innerHTML;
              this.lastHeadingPerDepth.length = depth;
            }
          }
        }
        /**
         * Applies the stored heading content to elements with the `.last-heading` class
         * within the specified containers. The heading content is determined by the
         * `data-depth` attribute on each `.last-heading` element.
         * @param page - The page containing `.last-heading` elements to update
         */
        applyLastHeadings(page) {
          const lastHeadingElements = page.querySelectorAll(".last-heading");
          lastHeadingElements.forEach((lastHeading) => {
            const depth = parseInt(lastHeading.dataset.depth || "0");
            lastHeading.innerHTML = this.lastHeadingPerDepth[depth - 1] || "";
          });
        }
        async onPostRendering() {
          const pages = this.quarkdownDocument.getPages();
          pages.forEach((page) => {
            this.overwriteLastHeadings(page);
            this.applyLastHeadings(page);
          });
        }
      };
    }
  });

  // src/main/typescript/document/type/slides-document.ts
  var SLIDE_SELECTOR, BACKGROUND_SELECTOR, SlidesDocument;
  var init_slides_document = __esm({
    "src/main/typescript/document/type/slides-document.ts"() {
      "use strict";
      init_execution_queues();
      init_page_chunker();
      init_page_margins_slides();
      init_footnotes_slides();
      init_page_numbers();
      init_persistent_headings();
      SLIDE_SELECTOR = ".reveal .slides > :is(section, .pdf-page)";
      BACKGROUND_SELECTOR = ".reveal :is(.backgrounds, .slides > .pdf-page) > .slide-background";
      SlidesDocument = class {
        /**
         * Retrieves a configuration property from the global configuration (`slidesConfig`).
         * Configuration is injected by Quarkdown's `.slides` function.
         */
        getConfigProperty(property, defaultValue) {
          const config = window.slidesConfig || {};
          return config[property] ?? defaultValue;
        }
        /**
         * @returns The parent slide element of the given element.
         */
        getParentViewport(element) {
          return element.closest(SLIDE_SELECTOR) || void 0;
        }
        getPages() {
          const slides = document.querySelectorAll(SLIDE_SELECTOR);
          const backgrounds = document.querySelectorAll(BACKGROUND_SELECTOR);
          if (!slides || !backgrounds) return [];
          return Array.from(slides).map((slide, index) => {
            const background = backgrounds[index];
            return {
              slide,
              background: background || document.createElement("div"),
              // Fallback for missing background
              querySelectorAll(query) {
                const slideResults = slide.querySelectorAll(query);
                const bgResults = background?.querySelectorAll(query) || [];
                return [...slideResults, ...Array.from(bgResults)];
              }
            };
          });
        }
        getPageNumber(page, includeDisplayNumbers = true) {
          const slide = page.slide;
          const displayNumber = includeDisplayNumbers ? slide.dataset.displayPageNumber : void 0;
          if (displayNumber) {
            return parseInt(displayNumber, 10);
          }
          if (!slide.parentElement) return 0;
          const index = Array.from(slide.parentElement.children).indexOf(slide);
          return index + 1;
        }
        getPageType(page) {
          const pageNumber = this.getPageNumber(page, false);
          return pageNumber % 2 === 0 ? "left" : "right";
        }
        getPage(element) {
          return this.getPages().find((page) => page.slide === this.getParentViewport(element));
        }
        setDisplayPageNumber(page, pageNumber) {
          page.slide.setAttribute("data-display-page-number", pageNumber.toString());
        }
        /** Sets up pre-rendering to execute when DOM content is loaded */
        setupPreRenderingHook() {
          document.addEventListener("DOMContentLoaded", async () => await preRenderingExecutionQueue.execute());
        }
        /** Sets up post-rendering to execute when Reveal.js is ready */
        setupPostRenderingHook() {
          Reveal.addEventListener("ready", () => {
            if (Reveal.isPrintView()) {
              Reveal.addEventListener("pdf-ready", () => postRenderingExecutionQueue.execute());
            } else {
              postRenderingExecutionQueue.execute().then();
            }
          });
        }
        /** Chunks content into slides and initializes Reveal.js */
        initializeRendering() {
          const slidesDiv = document.querySelector(".reveal .slides");
          if (!slidesDiv) return;
          new PageChunker(slidesDiv).chunk();
          Reveal.initialize({
            // If the center property is not explicitly set, it defaults to true unless the `--reveal-center-vertically` CSS variable of `:root` is set to `false`.
            center: this.getConfigProperty(
              "center",
              getComputedStyle(document.documentElement).getPropertyValue("--reveal-center-vertically") !== "false"
            ),
            controls: this.getConfigProperty("showControls", true),
            showNotes: this.getConfigProperty("showNotes", false),
            transition: this.getConfigProperty("transitionStyle", "slide"),
            transitionSpeed: this.getConfigProperty("transitionSpeed", "default"),
            hash: true,
            plugins: [RevealNotes]
          }).then();
        }
        getHandlers() {
          return [
            new PageMarginsSlides(this),
            new PageNumbers(this),
            new PersistentHeadings(this),
            new FootnotesSlides(this)
          ];
        }
      };
    }
  });

  // src/main/typescript/document/handlers/page-margins/page-margins-paged.ts
  var PageMarginsPaged;
  var init_page_margins_paged = __esm({
    "src/main/typescript/document/handlers/page-margins/page-margins-paged.ts"() {
      "use strict";
      init_page_margins_document_handler();
      PageMarginsPaged = class extends PageMarginsDocumentHandler {
        apply(initializer, page, marginPositionName) {
          const pageMargins = page.querySelectorAll(`.pagedjs_margin-${marginPositionName}`);
          pageMargins.forEach((pageMargin) => {
            pageMargin.classList.add("hasContent");
            const container = pageMargin.querySelector(".pagedjs_margin-content");
            if (!container) return;
            this.pushMarginClassList(container, initializer, marginPositionName);
            container.innerHTML = initializer.innerHTML;
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/footnotes/footnotes-paged.ts
  var FootnotesPaged;
  var init_footnotes_paged = __esm({
    "src/main/typescript/document/handlers/footnotes/footnotes-paged.ts"() {
      "use strict";
      init_footnote_dom();
      init_footnotes_document_handler();
      FootnotesPaged = class extends FootnotesDocumentHandler {
        /**
         * This is a hacky workaround for the base paged.js behavior:
         * Any change made after the pagination is done will not be processed by paged.js,
         * hence adding new content (footnotes) will cause content to overflow.
         *
         * This function takes all footnote references and creates a virtual empty space
         * of the size of the footnote definition, reserving space for it.
         * After rendering, `handleFootnotes` will remove this space and place
         * the footnote definition in the footnote area, balancing the layout.
         */
        async onPreRendering() {
          await super.onPreRendering();
          this.footnotes.forEach(({ reference, definition }) => {
            reference.style.display = "block";
            reference.style.height = definition.scrollHeight + "px";
            definition.remove();
            document.body.appendChild(definition);
          });
        }
        /**
         * Moves footnote definitions to their respective footnote areas,
         * and adjusts the layout accordingly.
         *
         * Useful context: https://github.com/pagedjs/pagedjs/issues/292
         */
        async onPostRendering() {
          await super.onPreRendering();
          this.footnotes.forEach(({ reference, definition }) => {
            const pageArea = this.quarkdownDocument.getParentViewport(reference);
            console.log(document);
            if (!pageArea) return;
            const footnoteArea = pageArea.querySelector(".pagedjs_footnote_area > .pagedjs_footnote_content");
            if (!footnoteArea) return;
            const footnoteContent = footnoteArea.querySelector(".pagedjs_footnote_inner_content");
            if (!footnoteContent) return;
            definition.remove();
            footnoteContent.appendChild(definition);
            footnoteArea.classList.remove("pagedjs_footnote_empty");
            footnoteContent.style.columnWidth = "auto";
            pageArea.style.setProperty("--pagedjs-footnotes-height", `${footnoteArea.scrollHeight}px`);
            reference.style.height = "auto";
            reference.style.display = "inline";
            getOrCreateFootnoteRule(footnoteContent);
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/paged/split-code-blocks-fix-paged.ts
  var SplitCodeBlocksFixPaged;
  var init_split_code_blocks_fix_paged = __esm({
    "src/main/typescript/document/handlers/paged/split-code-blocks-fix-paged.ts"() {
      "use strict";
      init_document_handler();
      SplitCodeBlocksFixPaged = class extends DocumentHandler {
        /**
         * Identifies and returns all code blocks that were split due to page breaks.
         *
         * Split code blocks are identified by the presence of a `data-split-from` attribute,
         * which contains the `data-ref` value of the original code block they were split from.
         *
         * @returns An array of split code block pairs, each containing the original block and its split counterpart
         */
        getSplitCodeBlocks() {
          const splitCodeBlocks = [];
          document.querySelectorAll("code[data-split-from]").forEach((split) => {
            const fromRef = split.getAttribute("data-split-from");
            if (!fromRef) return splitCodeBlocks;
            const from = document.querySelector(`code[data-ref="${fromRef}"]`);
            if (!from) return splitCodeBlocks;
            splitCodeBlocks.push({ from, split });
          });
          return splitCodeBlocks;
        }
        /**
         * Fixes the indentation of the first line in split code blocks.
         *
         * When a code block is split, the first line of the split portion often loses
         * its proper indentation. This method extracts the indentation from the last
         * line of the original code block and applies it to the split block.
         *
         * @param splitCodeBlocks Array of split code block pairs to fix
         */
        fixSplitCodeBlockFirstLineIndentation(splitCodeBlocks) {
          splitCodeBlocks.forEach(({ from, split }) => {
            const fromLastLine = from.innerText.split("\n").pop();
            if (!fromLastLine) return;
            const indentation = fromLastLine.match(/\s*$/)?.[0] || "";
            split.innerHTML = indentation + split.innerHTML;
          });
        }
        /**
         * Corrects line numbers in split code blocks to continue from the original block.
         *
         * Split code blocks typically restart their line numbering from 1, but they should
         * continue the numbering sequence from where the original block left off. This method
         * finds the last line number in the original block and adjusts all line numbers
         * in the split block accordingly.
         *
         * @param splitCodeBlocks Array of split code block pairs to fix
         */
        fixSplitCodeBlockLineNumbers(splitCodeBlocks) {
          const lineNumberAttribute = "data-line-number";
          splitCodeBlocks.forEach(({ from, split }) => {
            const lines = from.querySelectorAll(`[${lineNumberAttribute}]`);
            const lastLineNumber = Array.from(lines).pop()?.getAttribute(lineNumberAttribute) || "0";
            split.querySelectorAll(`[${lineNumberAttribute}]`).forEach((line) => {
              const lineNumber = line.getAttribute(lineNumberAttribute);
              if (!lineNumber) return;
              line.setAttribute(lineNumberAttribute, (parseInt(lineNumber) + parseInt(lastLineNumber)).toString());
            });
          });
        }
        /**
         * Executes the split code block fixes after document rendering is complete.
         *
         * This method is called during the post-rendering phase and:
         * 1. Identifies all split code blocks in the document
         * 2. Fixes their line numbering immediately
         * 3. Schedules another line number fix after syntax highlighting completes
         *
         * The setTimeout is necessary because syntax highlighting may modify the DOM
         * after initial rendering, potentially affecting line number attributes.
         */
        async onPostRendering() {
          const splitCodeBlocks = this.getSplitCodeBlocks();
          this.fixSplitCodeBlockFirstLineIndentation(splitCodeBlocks);
          setTimeout(() => this.fixSplitCodeBlockLineNumbers(splitCodeBlocks), 0);
        }
      };
    }
  });

  // src/main/typescript/document/handlers/paged/column-count-paged.ts
  var ColumnCountPaged;
  var init_column_count_paged = __esm({
    "src/main/typescript/document/handlers/paged/column-count-paged.ts"() {
      "use strict";
      init_document_handler();
      ColumnCountPaged = class extends DocumentHandler {
        async onPostRendering() {
          const columnCount = getComputedStyle(document.body).getPropertyValue("--qd-column-count")?.trim();
          if (!columnCount || columnCount === "") return;
          document.querySelectorAll(".pagedjs_page_content > div").forEach((content) => {
            content.style.columnCount = columnCount;
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/show-on-ready.ts
  var ShowOnReady;
  var init_show_on_ready = __esm({
    "src/main/typescript/document/handlers/show-on-ready.ts"() {
      "use strict";
      init_document_handler();
      ShowOnReady = class extends DocumentHandler {
        async onPreRendering() {
          document.body.style.opacity = "0";
        }
        async onPostRendering() {
          document.body.style.opacity = "1";
        }
      };
    }
  });

  // src/main/typescript/document/type/paged-document.ts
  var PagedDocument;
  var init_paged_document = __esm({
    "src/main/typescript/document/type/paged-document.ts"() {
      "use strict";
      init_execution_queues();
      init_sidebar();
      init_page_margins_paged();
      init_footnotes_paged();
      init_split_code_blocks_fix_paged();
      init_column_count_paged();
      init_page_numbers();
      init_show_on_ready();
      init_persistent_headings();
      PagedDocument = class {
        /**
         * @returns The parent page of the given element.
         */
        getParentViewport(element) {
          return element.closest(".pagedjs_area") || void 0;
        }
        getPages() {
          return Array.from(document.querySelectorAll(".pagedjs_page"));
        }
        getPage(element) {
          return element.closest(".pagedjs_page") || void 0;
        }
        getPageNumber(page, includeDisplayNumbers = true) {
          console.log("Getting page number for page:", page.dataset);
          return parseInt(
            (includeDisplayNumbers ? page.dataset.displayPageNumber : void 0) ?? page.dataset.pageNumber ?? "0"
          );
        }
        getPageType(page) {
          return page.classList.contains("pagedjs_right_page") ? "right" : "left";
        }
        setDisplayPageNumber(page, pageNumber) {
          page.setAttribute("data-display-page-number", pageNumber.toString());
        }
        /** Sets up pre-rendering to execute when DOM content is loaded. */
        setupPreRenderingHook() {
          document.addEventListener("DOMContentLoaded", async () => await preRenderingExecutionQueue.execute());
        }
        /** Sets up post-rendering to execute when paged.js is ready. */
        setupPostRenderingHook() {
          class PagedAfterReadyHandler extends Paged.Handler {
            afterRendered() {
              postRenderingExecutionQueue.execute().then();
            }
          }
          Paged.registerHandlers(PagedAfterReadyHandler);
        }
        /** Initializes paged.js rendering. */
        initializeRendering() {
          window.PagedPolyfill?.preview().then();
        }
        getHandlers() {
          return [
            new Sidebar(this),
            new ShowOnReady(this),
            new PageMarginsPaged(this),
            new PageNumbers(this),
            new PersistentHeadings(this),
            new FootnotesPaged(this),
            new ColumnCountPaged(this),
            new SplitCodeBlocksFixPaged(this)
          ];
        }
      };
    }
  });

  // src/main/typescript/document/handlers/page-margins/page-margins-docs.ts
  var HEADER, CONTENT, SIDEBAR_LEFT, SIDEBAR_RIGHT, FOOTER, MARGIN_TARGETS, PageMarginsDocs;
  var init_page_margins_docs = __esm({
    "src/main/typescript/document/handlers/page-margins/page-margins-docs.ts"() {
      "use strict";
      init_document_handler();
      HEADER = "body > header";
      CONTENT = "body > .content-wrapper";
      SIDEBAR_LEFT = `${CONTENT} > aside:first-child`;
      SIDEBAR_RIGHT = `${CONTENT} > aside:last-child`;
      FOOTER = `${CONTENT} > main > footer`;
      MARGIN_TARGETS = {
        // Header
        "top-left-corner": `${HEADER} > aside:first-child`,
        "top-left": `${HEADER} > aside:first-child`,
        "top-center": `${HEADER} > main`,
        "top-right-corner": `${HEADER} > aside:last-child`,
        "top-right": `${HEADER} > aside:last-child`,
        // Left sidebar
        "left-top": `${SIDEBAR_LEFT} > .position-top`,
        "left-middle": `${SIDEBAR_LEFT} > .position-middle`,
        "left-bottom": `${SIDEBAR_LEFT} > .position-bottom`,
        "bottom-left-corner": `${SIDEBAR_LEFT} > .position-bottom`,
        // Right sidebar
        "right-top": `${SIDEBAR_RIGHT} > .position-top`,
        "right-middle": `${SIDEBAR_RIGHT} > .position-middle`,
        "right-bottom": `${SIDEBAR_RIGHT} > .position-bottom`,
        "bottom-right-corner": `${SIDEBAR_RIGHT} > .position-bottom`,
        // Footer
        "bottom-left": `${FOOTER} > .position-left`,
        "bottom-center": `${FOOTER} > .position-center`,
        "bottom-right": `${FOOTER} > .position-right`
      };
      PageMarginsDocs = class extends DocumentHandler {
        async onPostRendering() {
          document.querySelectorAll(".page-margin-content").forEach((initializer) => {
            const position = this.getMarginPosition(initializer);
            if (!position) return;
            initializer.remove();
            const selector = MARGIN_TARGETS[position];
            if (!selector) return;
            const container = document.querySelector(selector);
            if (!container) return;
            container.appendChild(this.createWrapper(initializer, position));
          });
        }
        /**
         * Creates a wrapper element with the appropriate classes and content.
         */
        createWrapper(initializer, position) {
          const wrapper = document.createElement("div");
          wrapper.classList.add(`page-margin-${position}`, ...initializer.classList);
          wrapper.innerHTML = initializer.innerHTML;
          return wrapper;
        }
        /**
         * Gets the margin position name from the initializer element.
         * Docs don't have left/right pages, so either data attribute works.
         */
        getMarginPosition(initializer) {
          return initializer.dataset.onLeftPage ?? initializer.dataset.onRightPage ?? null;
        }
      };
    }
  });

  // src/main/typescript/document/handlers/docs/search-field-focus.ts
  var SEARCH_SHORTCUT_KEY, SearchFieldFocus;
  var init_search_field_focus = __esm({
    "src/main/typescript/document/handlers/docs/search-field-focus.ts"() {
      "use strict";
      init_document_handler();
      SEARCH_SHORTCUT_KEY = "/";
      SearchFieldFocus = class extends DocumentHandler {
        async onPostRendering() {
          document.addEventListener("keydown", (event) => {
            if (event.key !== SEARCH_SHORTCUT_KEY) return;
            const activeElement = document.activeElement;
            if (activeElement instanceof HTMLInputElement || activeElement instanceof HTMLTextAreaElement) {
              return;
            }
            event.preventDefault();
            document.getElementById("search-input")?.focus();
          });
        }
      };
    }
  });

  // node_modules/minisearch/dist/es/index.js
  var ENTRIES, KEYS, VALUES, LEAF, TreeIterator, last$1, fuzzySearch, recurse, SearchableMap, trackDown, lookup, createPath, remove, cleanup, merge, last, OR, AND, AND_NOT, MiniSearch, getOwnProperty, combinators, defaultBM25params, calcBM25Score, termToQuerySpec, defaultOptions, defaultSearchOptions, defaultAutoSuggestOptions, defaultVacuumOptions, defaultVacuumConditions, defaultAutoVacuumOptions, assignUniqueTerm, assignUniqueTerms, byScore, createMap, objectToNumericMap, objectToNumericMapAsync, wait, SPACE_OR_PUNCTUATION;
  var init_es = __esm({
    "node_modules/minisearch/dist/es/index.js"() {
      ENTRIES = "ENTRIES";
      KEYS = "KEYS";
      VALUES = "VALUES";
      LEAF = "";
      TreeIterator = class {
        constructor(set, type) {
          const node = set._tree;
          const keys = Array.from(node.keys());
          this.set = set;
          this._type = type;
          this._path = keys.length > 0 ? [{ node, keys }] : [];
        }
        next() {
          const value = this.dive();
          this.backtrack();
          return value;
        }
        dive() {
          if (this._path.length === 0) {
            return { done: true, value: void 0 };
          }
          const { node, keys } = last$1(this._path);
          if (last$1(keys) === LEAF) {
            return { done: false, value: this.result() };
          }
          const child = node.get(last$1(keys));
          this._path.push({ node: child, keys: Array.from(child.keys()) });
          return this.dive();
        }
        backtrack() {
          if (this._path.length === 0) {
            return;
          }
          const keys = last$1(this._path).keys;
          keys.pop();
          if (keys.length > 0) {
            return;
          }
          this._path.pop();
          this.backtrack();
        }
        key() {
          return this.set._prefix + this._path.map(({ keys }) => last$1(keys)).filter((key) => key !== LEAF).join("");
        }
        value() {
          return last$1(this._path).node.get(LEAF);
        }
        result() {
          switch (this._type) {
            case VALUES:
              return this.value();
            case KEYS:
              return this.key();
            default:
              return [this.key(), this.value()];
          }
        }
        [Symbol.iterator]() {
          return this;
        }
      };
      last$1 = (array) => {
        return array[array.length - 1];
      };
      fuzzySearch = (node, query, maxDistance) => {
        const results = /* @__PURE__ */ new Map();
        if (query === void 0)
          return results;
        const n = query.length + 1;
        const m = n + maxDistance;
        const matrix = new Uint8Array(m * n).fill(maxDistance + 1);
        for (let j = 0; j < n; ++j)
          matrix[j] = j;
        for (let i = 1; i < m; ++i)
          matrix[i * n] = i;
        recurse(node, query, maxDistance, results, matrix, 1, n, "");
        return results;
      };
      recurse = (node, query, maxDistance, results, matrix, m, n, prefix) => {
        const offset = m * n;
        key: for (const key of node.keys()) {
          if (key === LEAF) {
            const distance = matrix[offset - 1];
            if (distance <= maxDistance) {
              results.set(prefix, [node.get(key), distance]);
            }
          } else {
            let i = m;
            for (let pos = 0; pos < key.length; ++pos, ++i) {
              const char = key[pos];
              const thisRowOffset = n * i;
              const prevRowOffset = thisRowOffset - n;
              let minDistance = matrix[thisRowOffset];
              const jmin = Math.max(0, i - maxDistance - 1);
              const jmax = Math.min(n - 1, i + maxDistance);
              for (let j = jmin; j < jmax; ++j) {
                const different = char !== query[j];
                const rpl = matrix[prevRowOffset + j] + +different;
                const del = matrix[prevRowOffset + j + 1] + 1;
                const ins = matrix[thisRowOffset + j] + 1;
                const dist = matrix[thisRowOffset + j + 1] = Math.min(rpl, del, ins);
                if (dist < minDistance)
                  minDistance = dist;
              }
              if (minDistance > maxDistance) {
                continue key;
              }
            }
            recurse(node.get(key), query, maxDistance, results, matrix, i, n, prefix + key);
          }
        }
      };
      SearchableMap = class _SearchableMap {
        /**
         * The constructor is normally called without arguments, creating an empty
         * map. In order to create a {@link SearchableMap} from an iterable or from an
         * object, check {@link SearchableMap.from} and {@link
         * SearchableMap.fromObject}.
         *
         * The constructor arguments are for internal use, when creating derived
         * mutable views of a map at a prefix.
         */
        constructor(tree = /* @__PURE__ */ new Map(), prefix = "") {
          this._size = void 0;
          this._tree = tree;
          this._prefix = prefix;
        }
        /**
         * Creates and returns a mutable view of this {@link SearchableMap},
         * containing only entries that share the given prefix.
         *
         * ### Usage:
         *
         * ```javascript
         * let map = new SearchableMap()
         * map.set("unicorn", 1)
         * map.set("universe", 2)
         * map.set("university", 3)
         * map.set("unique", 4)
         * map.set("hello", 5)
         *
         * let uni = map.atPrefix("uni")
         * uni.get("unique") // => 4
         * uni.get("unicorn") // => 1
         * uni.get("hello") // => undefined
         *
         * let univer = map.atPrefix("univer")
         * univer.get("unique") // => undefined
         * univer.get("universe") // => 2
         * univer.get("university") // => 3
         * ```
         *
         * @param prefix  The prefix
         * @return A {@link SearchableMap} representing a mutable view of the original
         * Map at the given prefix
         */
        atPrefix(prefix) {
          if (!prefix.startsWith(this._prefix)) {
            throw new Error("Mismatched prefix");
          }
          const [node, path] = trackDown(this._tree, prefix.slice(this._prefix.length));
          if (node === void 0) {
            const [parentNode, key] = last(path);
            for (const k of parentNode.keys()) {
              if (k !== LEAF && k.startsWith(key)) {
                const node2 = /* @__PURE__ */ new Map();
                node2.set(k.slice(key.length), parentNode.get(k));
                return new _SearchableMap(node2, prefix);
              }
            }
          }
          return new _SearchableMap(node, prefix);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/clear
         */
        clear() {
          this._size = void 0;
          this._tree.clear();
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/delete
         * @param key  Key to delete
         */
        delete(key) {
          this._size = void 0;
          return remove(this._tree, key);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/entries
         * @return An iterator iterating through `[key, value]` entries.
         */
        entries() {
          return new TreeIterator(this, ENTRIES);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/forEach
         * @param fn  Iteration function
         */
        forEach(fn) {
          for (const [key, value] of this) {
            fn(key, value, this);
          }
        }
        /**
         * Returns a Map of all the entries that have a key within the given edit
         * distance from the search key. The keys of the returned Map are the matching
         * keys, while the values are two-element arrays where the first element is
         * the value associated to the key, and the second is the edit distance of the
         * key to the search key.
         *
         * ### Usage:
         *
         * ```javascript
         * let map = new SearchableMap()
         * map.set('hello', 'world')
         * map.set('hell', 'yeah')
         * map.set('ciao', 'mondo')
         *
         * // Get all entries that match the key 'hallo' with a maximum edit distance of 2
         * map.fuzzyGet('hallo', 2)
         * // => Map(2) { 'hello' => ['world', 1], 'hell' => ['yeah', 2] }
         *
         * // In the example, the "hello" key has value "world" and edit distance of 1
         * // (change "e" to "a"), the key "hell" has value "yeah" and edit distance of 2
         * // (change "e" to "a", delete "o")
         * ```
         *
         * @param key  The search key
         * @param maxEditDistance  The maximum edit distance (Levenshtein)
         * @return A Map of the matching keys to their value and edit distance
         */
        fuzzyGet(key, maxEditDistance) {
          return fuzzySearch(this._tree, key, maxEditDistance);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/get
         * @param key  Key to get
         * @return Value associated to the key, or `undefined` if the key is not
         * found.
         */
        get(key) {
          const node = lookup(this._tree, key);
          return node !== void 0 ? node.get(LEAF) : void 0;
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/has
         * @param key  Key
         * @return True if the key is in the map, false otherwise
         */
        has(key) {
          const node = lookup(this._tree, key);
          return node !== void 0 && node.has(LEAF);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/keys
         * @return An `Iterable` iterating through keys
         */
        keys() {
          return new TreeIterator(this, KEYS);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/set
         * @param key  Key to set
         * @param value  Value to associate to the key
         * @return The {@link SearchableMap} itself, to allow chaining
         */
        set(key, value) {
          if (typeof key !== "string") {
            throw new Error("key must be a string");
          }
          this._size = void 0;
          const node = createPath(this._tree, key);
          node.set(LEAF, value);
          return this;
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/size
         */
        get size() {
          if (this._size) {
            return this._size;
          }
          this._size = 0;
          const iter = this.entries();
          while (!iter.next().done)
            this._size += 1;
          return this._size;
        }
        /**
         * Updates the value at the given key using the provided function. The function
         * is called with the current value at the key, and its return value is used as
         * the new value to be set.
         *
         * ### Example:
         *
         * ```javascript
         * // Increment the current value by one
         * searchableMap.update('somekey', (currentValue) => currentValue == null ? 0 : currentValue + 1)
         * ```
         *
         * If the value at the given key is or will be an object, it might not require
         * re-assignment. In that case it is better to use `fetch()`, because it is
         * faster.
         *
         * @param key  The key to update
         * @param fn  The function used to compute the new value from the current one
         * @return The {@link SearchableMap} itself, to allow chaining
         */
        update(key, fn) {
          if (typeof key !== "string") {
            throw new Error("key must be a string");
          }
          this._size = void 0;
          const node = createPath(this._tree, key);
          node.set(LEAF, fn(node.get(LEAF)));
          return this;
        }
        /**
         * Fetches the value of the given key. If the value does not exist, calls the
         * given function to create a new value, which is inserted at the given key
         * and subsequently returned.
         *
         * ### Example:
         *
         * ```javascript
         * const map = searchableMap.fetch('somekey', () => new Map())
         * map.set('foo', 'bar')
         * ```
         *
         * @param key  The key to update
         * @param initial  A function that creates a new value if the key does not exist
         * @return The existing or new value at the given key
         */
        fetch(key, initial) {
          if (typeof key !== "string") {
            throw new Error("key must be a string");
          }
          this._size = void 0;
          const node = createPath(this._tree, key);
          let value = node.get(LEAF);
          if (value === void 0) {
            node.set(LEAF, value = initial());
          }
          return value;
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/values
         * @return An `Iterable` iterating through values.
         */
        values() {
          return new TreeIterator(this, VALUES);
        }
        /**
         * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/@@iterator
         */
        [Symbol.iterator]() {
          return this.entries();
        }
        /**
         * Creates a {@link SearchableMap} from an `Iterable` of entries
         *
         * @param entries  Entries to be inserted in the {@link SearchableMap}
         * @return A new {@link SearchableMap} with the given entries
         */
        static from(entries) {
          const tree = new _SearchableMap();
          for (const [key, value] of entries) {
            tree.set(key, value);
          }
          return tree;
        }
        /**
         * Creates a {@link SearchableMap} from the iterable properties of a JavaScript object
         *
         * @param object  Object of entries for the {@link SearchableMap}
         * @return A new {@link SearchableMap} with the given entries
         */
        static fromObject(object) {
          return _SearchableMap.from(Object.entries(object));
        }
      };
      trackDown = (tree, key, path = []) => {
        if (key.length === 0 || tree == null) {
          return [tree, path];
        }
        for (const k of tree.keys()) {
          if (k !== LEAF && key.startsWith(k)) {
            path.push([tree, k]);
            return trackDown(tree.get(k), key.slice(k.length), path);
          }
        }
        path.push([tree, key]);
        return trackDown(void 0, "", path);
      };
      lookup = (tree, key) => {
        if (key.length === 0 || tree == null) {
          return tree;
        }
        for (const k of tree.keys()) {
          if (k !== LEAF && key.startsWith(k)) {
            return lookup(tree.get(k), key.slice(k.length));
          }
        }
      };
      createPath = (node, key) => {
        const keyLength = key.length;
        outer: for (let pos = 0; node && pos < keyLength; ) {
          for (const k of node.keys()) {
            if (k !== LEAF && key[pos] === k[0]) {
              const len = Math.min(keyLength - pos, k.length);
              let offset = 1;
              while (offset < len && key[pos + offset] === k[offset])
                ++offset;
              const child2 = node.get(k);
              if (offset === k.length) {
                node = child2;
              } else {
                const intermediate = /* @__PURE__ */ new Map();
                intermediate.set(k.slice(offset), child2);
                node.set(key.slice(pos, pos + offset), intermediate);
                node.delete(k);
                node = intermediate;
              }
              pos += offset;
              continue outer;
            }
          }
          const child = /* @__PURE__ */ new Map();
          node.set(key.slice(pos), child);
          return child;
        }
        return node;
      };
      remove = (tree, key) => {
        const [node, path] = trackDown(tree, key);
        if (node === void 0) {
          return;
        }
        node.delete(LEAF);
        if (node.size === 0) {
          cleanup(path);
        } else if (node.size === 1) {
          const [key2, value] = node.entries().next().value;
          merge(path, key2, value);
        }
      };
      cleanup = (path) => {
        if (path.length === 0) {
          return;
        }
        const [node, key] = last(path);
        node.delete(key);
        if (node.size === 0) {
          cleanup(path.slice(0, -1));
        } else if (node.size === 1) {
          const [key2, value] = node.entries().next().value;
          if (key2 !== LEAF) {
            merge(path.slice(0, -1), key2, value);
          }
        }
      };
      merge = (path, key, value) => {
        if (path.length === 0) {
          return;
        }
        const [node, nodeKey] = last(path);
        node.set(nodeKey + key, value);
        node.delete(nodeKey);
      };
      last = (array) => {
        return array[array.length - 1];
      };
      OR = "or";
      AND = "and";
      AND_NOT = "and_not";
      MiniSearch = class _MiniSearch {
        /**
         * @param options  Configuration options
         *
         * ### Examples:
         *
         * ```javascript
         * // Create a search engine that indexes the 'title' and 'text' fields of your
         * // documents:
         * const miniSearch = new MiniSearch({ fields: ['title', 'text'] })
         * ```
         *
         * ### ID Field:
         *
         * ```javascript
         * // Your documents are assumed to include a unique 'id' field, but if you want
         * // to use a different field for document identification, you can set the
         * // 'idField' option:
         * const miniSearch = new MiniSearch({ idField: 'key', fields: ['title', 'text'] })
         * ```
         *
         * ### Options and defaults:
         *
         * ```javascript
         * // The full set of options (here with their default value) is:
         * const miniSearch = new MiniSearch({
         *   // idField: field that uniquely identifies a document
         *   idField: 'id',
         *
         *   // extractField: function used to get the value of a field in a document.
         *   // By default, it assumes the document is a flat object with field names as
         *   // property keys and field values as string property values, but custom logic
         *   // can be implemented by setting this option to a custom extractor function.
         *   extractField: (document, fieldName) => document[fieldName],
         *
         *   // tokenize: function used to split fields into individual terms. By
         *   // default, it is also used to tokenize search queries, unless a specific
         *   // `tokenize` search option is supplied. When tokenizing an indexed field,
         *   // the field name is passed as the second argument.
         *   tokenize: (string, _fieldName) => string.split(SPACE_OR_PUNCTUATION),
         *
         *   // processTerm: function used to process each tokenized term before
         *   // indexing. It can be used for stemming and normalization. Return a falsy
         *   // value in order to discard a term. By default, it is also used to process
         *   // search queries, unless a specific `processTerm` option is supplied as a
         *   // search option. When processing a term from a indexed field, the field
         *   // name is passed as the second argument.
         *   processTerm: (term, _fieldName) => term.toLowerCase(),
         *
         *   // searchOptions: default search options, see the `search` method for
         *   // details
         *   searchOptions: undefined,
         *
         *   // fields: document fields to be indexed. Mandatory, but not set by default
         *   fields: undefined
         *
         *   // storeFields: document fields to be stored and returned as part of the
         *   // search results.
         *   storeFields: []
         * })
         * ```
         */
        constructor(options) {
          if ((options === null || options === void 0 ? void 0 : options.fields) == null) {
            throw new Error('MiniSearch: option "fields" must be provided');
          }
          const autoVacuum = options.autoVacuum == null || options.autoVacuum === true ? defaultAutoVacuumOptions : options.autoVacuum;
          this._options = {
            ...defaultOptions,
            ...options,
            autoVacuum,
            searchOptions: { ...defaultSearchOptions, ...options.searchOptions || {} },
            autoSuggestOptions: { ...defaultAutoSuggestOptions, ...options.autoSuggestOptions || {} }
          };
          this._index = new SearchableMap();
          this._documentCount = 0;
          this._documentIds = /* @__PURE__ */ new Map();
          this._idToShortId = /* @__PURE__ */ new Map();
          this._fieldIds = {};
          this._fieldLength = /* @__PURE__ */ new Map();
          this._avgFieldLength = [];
          this._nextId = 0;
          this._storedFields = /* @__PURE__ */ new Map();
          this._dirtCount = 0;
          this._currentVacuum = null;
          this._enqueuedVacuum = null;
          this._enqueuedVacuumConditions = defaultVacuumConditions;
          this.addFields(this._options.fields);
        }
        /**
         * Adds a document to the index
         *
         * @param document  The document to be indexed
         */
        add(document2) {
          const { extractField, stringifyField, tokenize, processTerm, fields, idField } = this._options;
          const id = extractField(document2, idField);
          if (id == null) {
            throw new Error(`MiniSearch: document does not have ID field "${idField}"`);
          }
          if (this._idToShortId.has(id)) {
            throw new Error(`MiniSearch: duplicate ID ${id}`);
          }
          const shortDocumentId = this.addDocumentId(id);
          this.saveStoredFields(shortDocumentId, document2);
          for (const field of fields) {
            const fieldValue = extractField(document2, field);
            if (fieldValue == null)
              continue;
            const tokens = tokenize(stringifyField(fieldValue, field), field);
            const fieldId = this._fieldIds[field];
            const uniqueTerms = new Set(tokens).size;
            this.addFieldLength(shortDocumentId, fieldId, this._documentCount - 1, uniqueTerms);
            for (const term of tokens) {
              const processedTerm = processTerm(term, field);
              if (Array.isArray(processedTerm)) {
                for (const t of processedTerm) {
                  this.addTerm(fieldId, shortDocumentId, t);
                }
              } else if (processedTerm) {
                this.addTerm(fieldId, shortDocumentId, processedTerm);
              }
            }
          }
        }
        /**
         * Adds all the given documents to the index
         *
         * @param documents  An array of documents to be indexed
         */
        addAll(documents) {
          for (const document2 of documents)
            this.add(document2);
        }
        /**
         * Adds all the given documents to the index asynchronously.
         *
         * Returns a promise that resolves (to `undefined`) when the indexing is done.
         * This method is useful when index many documents, to avoid blocking the main
         * thread. The indexing is performed asynchronously and in chunks.
         *
         * @param documents  An array of documents to be indexed
         * @param options  Configuration options
         * @return A promise resolving to `undefined` when the indexing is done
         */
        addAllAsync(documents, options = {}) {
          const { chunkSize = 10 } = options;
          const acc = { chunk: [], promise: Promise.resolve() };
          const { chunk, promise } = documents.reduce(({ chunk: chunk2, promise: promise2 }, document2, i) => {
            chunk2.push(document2);
            if ((i + 1) % chunkSize === 0) {
              return {
                chunk: [],
                promise: promise2.then(() => new Promise((resolve) => setTimeout(resolve, 0))).then(() => this.addAll(chunk2))
              };
            } else {
              return { chunk: chunk2, promise: promise2 };
            }
          }, acc);
          return promise.then(() => this.addAll(chunk));
        }
        /**
         * Removes the given document from the index.
         *
         * The document to remove must NOT have changed between indexing and removal,
         * otherwise the index will be corrupted.
         *
         * This method requires passing the full document to be removed (not just the
         * ID), and immediately removes the document from the inverted index, allowing
         * memory to be released. A convenient alternative is {@link
         * MiniSearch#discard}, which needs only the document ID, and has the same
         * visible effect, but delays cleaning up the index until the next vacuuming.
         *
         * @param document  The document to be removed
         */
        remove(document2) {
          const { tokenize, processTerm, extractField, stringifyField, fields, idField } = this._options;
          const id = extractField(document2, idField);
          if (id == null) {
            throw new Error(`MiniSearch: document does not have ID field "${idField}"`);
          }
          const shortId = this._idToShortId.get(id);
          if (shortId == null) {
            throw new Error(`MiniSearch: cannot remove document with ID ${id}: it is not in the index`);
          }
          for (const field of fields) {
            const fieldValue = extractField(document2, field);
            if (fieldValue == null)
              continue;
            const tokens = tokenize(stringifyField(fieldValue, field), field);
            const fieldId = this._fieldIds[field];
            const uniqueTerms = new Set(tokens).size;
            this.removeFieldLength(shortId, fieldId, this._documentCount, uniqueTerms);
            for (const term of tokens) {
              const processedTerm = processTerm(term, field);
              if (Array.isArray(processedTerm)) {
                for (const t of processedTerm) {
                  this.removeTerm(fieldId, shortId, t);
                }
              } else if (processedTerm) {
                this.removeTerm(fieldId, shortId, processedTerm);
              }
            }
          }
          this._storedFields.delete(shortId);
          this._documentIds.delete(shortId);
          this._idToShortId.delete(id);
          this._fieldLength.delete(shortId);
          this._documentCount -= 1;
        }
        /**
         * Removes all the given documents from the index. If called with no arguments,
         * it removes _all_ documents from the index.
         *
         * @param documents  The documents to be removed. If this argument is omitted,
         * all documents are removed. Note that, for removing all documents, it is
         * more efficient to call this method with no arguments than to pass all
         * documents.
         */
        removeAll(documents) {
          if (documents) {
            for (const document2 of documents)
              this.remove(document2);
          } else if (arguments.length > 0) {
            throw new Error("Expected documents to be present. Omit the argument to remove all documents.");
          } else {
            this._index = new SearchableMap();
            this._documentCount = 0;
            this._documentIds = /* @__PURE__ */ new Map();
            this._idToShortId = /* @__PURE__ */ new Map();
            this._fieldLength = /* @__PURE__ */ new Map();
            this._avgFieldLength = [];
            this._storedFields = /* @__PURE__ */ new Map();
            this._nextId = 0;
          }
        }
        /**
         * Discards the document with the given ID, so it won't appear in search results
         *
         * It has the same visible effect of {@link MiniSearch.remove} (both cause the
         * document to stop appearing in searches), but a different effect on the
         * internal data structures:
         *
         *   - {@link MiniSearch#remove} requires passing the full document to be
         *   removed as argument, and removes it from the inverted index immediately.
         *
         *   - {@link MiniSearch#discard} instead only needs the document ID, and
         *   works by marking the current version of the document as discarded, so it
         *   is immediately ignored by searches. This is faster and more convenient
         *   than {@link MiniSearch#remove}, but the index is not immediately
         *   modified. To take care of that, vacuuming is performed after a certain
         *   number of documents are discarded, cleaning up the index and allowing
         *   memory to be released.
         *
         * After discarding a document, it is possible to re-add a new version, and
         * only the new version will appear in searches. In other words, discarding
         * and re-adding a document works exactly like removing and re-adding it. The
         * {@link MiniSearch.replace} method can also be used to replace a document
         * with a new version.
         *
         * #### Details about vacuuming
         *
         * Repetite calls to this method would leave obsolete document references in
         * the index, invisible to searches. Two mechanisms take care of cleaning up:
         * clean up during search, and vacuuming.
         *
         *   - Upon search, whenever a discarded ID is found (and ignored for the
         *   results), references to the discarded document are removed from the
         *   inverted index entries for the search terms. This ensures that subsequent
         *   searches for the same terms do not need to skip these obsolete references
         *   again.
         *
         *   - In addition, vacuuming is performed automatically by default (see the
         *   `autoVacuum` field in {@link Options}) after a certain number of
         *   documents are discarded. Vacuuming traverses all terms in the index,
         *   cleaning up all references to discarded documents. Vacuuming can also be
         *   triggered manually by calling {@link MiniSearch#vacuum}.
         *
         * @param id  The ID of the document to be discarded
         */
        discard(id) {
          const shortId = this._idToShortId.get(id);
          if (shortId == null) {
            throw new Error(`MiniSearch: cannot discard document with ID ${id}: it is not in the index`);
          }
          this._idToShortId.delete(id);
          this._documentIds.delete(shortId);
          this._storedFields.delete(shortId);
          (this._fieldLength.get(shortId) || []).forEach((fieldLength, fieldId) => {
            this.removeFieldLength(shortId, fieldId, this._documentCount, fieldLength);
          });
          this._fieldLength.delete(shortId);
          this._documentCount -= 1;
          this._dirtCount += 1;
          this.maybeAutoVacuum();
        }
        maybeAutoVacuum() {
          if (this._options.autoVacuum === false) {
            return;
          }
          const { minDirtFactor, minDirtCount, batchSize, batchWait } = this._options.autoVacuum;
          this.conditionalVacuum({ batchSize, batchWait }, { minDirtCount, minDirtFactor });
        }
        /**
         * Discards the documents with the given IDs, so they won't appear in search
         * results
         *
         * It is equivalent to calling {@link MiniSearch#discard} for all the given
         * IDs, but with the optimization of triggering at most one automatic
         * vacuuming at the end.
         *
         * Note: to remove all documents from the index, it is faster and more
         * convenient to call {@link MiniSearch.removeAll} with no argument, instead
         * of passing all IDs to this method.
         */
        discardAll(ids) {
          const autoVacuum = this._options.autoVacuum;
          try {
            this._options.autoVacuum = false;
            for (const id of ids) {
              this.discard(id);
            }
          } finally {
            this._options.autoVacuum = autoVacuum;
          }
          this.maybeAutoVacuum();
        }
        /**
         * It replaces an existing document with the given updated version
         *
         * It works by discarding the current version and adding the updated one, so
         * it is functionally equivalent to calling {@link MiniSearch#discard}
         * followed by {@link MiniSearch#add}. The ID of the updated document should
         * be the same as the original one.
         *
         * Since it uses {@link MiniSearch#discard} internally, this method relies on
         * vacuuming to clean up obsolete document references from the index, allowing
         * memory to be released (see {@link MiniSearch#discard}).
         *
         * @param updatedDocument  The updated document to replace the old version
         * with
         */
        replace(updatedDocument) {
          const { idField, extractField } = this._options;
          const id = extractField(updatedDocument, idField);
          this.discard(id);
          this.add(updatedDocument);
        }
        /**
         * Triggers a manual vacuuming, cleaning up references to discarded documents
         * from the inverted index
         *
         * Vacuuming is only useful for applications that use the {@link
         * MiniSearch#discard} or {@link MiniSearch#replace} methods.
         *
         * By default, vacuuming is performed automatically when needed (controlled by
         * the `autoVacuum` field in {@link Options}), so there is usually no need to
         * call this method, unless one wants to make sure to perform vacuuming at a
         * specific moment.
         *
         * Vacuuming traverses all terms in the inverted index in batches, and cleans
         * up references to discarded documents from the posting list, allowing memory
         * to be released.
         *
         * The method takes an optional object as argument with the following keys:
         *
         *   - `batchSize`: the size of each batch (1000 by default)
         *
         *   - `batchWait`: the number of milliseconds to wait between batches (10 by
         *   default)
         *
         * On large indexes, vacuuming could have a non-negligible cost: batching
         * avoids blocking the thread for long, diluting this cost so that it is not
         * negatively affecting the application. Nonetheless, this method should only
         * be called when necessary, and relying on automatic vacuuming is usually
         * better.
         *
         * It returns a promise that resolves (to undefined) when the clean up is
         * completed. If vacuuming is already ongoing at the time this method is
         * called, a new one is enqueued immediately after the ongoing one, and a
         * corresponding promise is returned. However, no more than one vacuuming is
         * enqueued on top of the ongoing one, even if this method is called more
         * times (enqueuing multiple ones would be useless).
         *
         * @param options  Configuration options for the batch size and delay. See
         * {@link VacuumOptions}.
         */
        vacuum(options = {}) {
          return this.conditionalVacuum(options);
        }
        conditionalVacuum(options, conditions) {
          if (this._currentVacuum) {
            this._enqueuedVacuumConditions = this._enqueuedVacuumConditions && conditions;
            if (this._enqueuedVacuum != null) {
              return this._enqueuedVacuum;
            }
            this._enqueuedVacuum = this._currentVacuum.then(() => {
              const conditions2 = this._enqueuedVacuumConditions;
              this._enqueuedVacuumConditions = defaultVacuumConditions;
              return this.performVacuuming(options, conditions2);
            });
            return this._enqueuedVacuum;
          }
          if (this.vacuumConditionsMet(conditions) === false) {
            return Promise.resolve();
          }
          this._currentVacuum = this.performVacuuming(options);
          return this._currentVacuum;
        }
        async performVacuuming(options, conditions) {
          const initialDirtCount = this._dirtCount;
          if (this.vacuumConditionsMet(conditions)) {
            const batchSize = options.batchSize || defaultVacuumOptions.batchSize;
            const batchWait = options.batchWait || defaultVacuumOptions.batchWait;
            let i = 1;
            for (const [term, fieldsData] of this._index) {
              for (const [fieldId, fieldIndex] of fieldsData) {
                for (const [shortId] of fieldIndex) {
                  if (this._documentIds.has(shortId)) {
                    continue;
                  }
                  if (fieldIndex.size <= 1) {
                    fieldsData.delete(fieldId);
                  } else {
                    fieldIndex.delete(shortId);
                  }
                }
              }
              if (this._index.get(term).size === 0) {
                this._index.delete(term);
              }
              if (i % batchSize === 0) {
                await new Promise((resolve) => setTimeout(resolve, batchWait));
              }
              i += 1;
            }
            this._dirtCount -= initialDirtCount;
          }
          await null;
          this._currentVacuum = this._enqueuedVacuum;
          this._enqueuedVacuum = null;
        }
        vacuumConditionsMet(conditions) {
          if (conditions == null) {
            return true;
          }
          let { minDirtCount, minDirtFactor } = conditions;
          minDirtCount = minDirtCount || defaultAutoVacuumOptions.minDirtCount;
          minDirtFactor = minDirtFactor || defaultAutoVacuumOptions.minDirtFactor;
          return this.dirtCount >= minDirtCount && this.dirtFactor >= minDirtFactor;
        }
        /**
         * Is `true` if a vacuuming operation is ongoing, `false` otherwise
         */
        get isVacuuming() {
          return this._currentVacuum != null;
        }
        /**
         * The number of documents discarded since the most recent vacuuming
         */
        get dirtCount() {
          return this._dirtCount;
        }
        /**
         * A number between 0 and 1 giving an indication about the proportion of
         * documents that are discarded, and can therefore be cleaned up by vacuuming.
         * A value close to 0 means that the index is relatively clean, while a higher
         * value means that the index is relatively dirty, and vacuuming could release
         * memory.
         */
        get dirtFactor() {
          return this._dirtCount / (1 + this._documentCount + this._dirtCount);
        }
        /**
         * Returns `true` if a document with the given ID is present in the index and
         * available for search, `false` otherwise
         *
         * @param id  The document ID
         */
        has(id) {
          return this._idToShortId.has(id);
        }
        /**
         * Returns the stored fields (as configured in the `storeFields` constructor
         * option) for the given document ID. Returns `undefined` if the document is
         * not present in the index.
         *
         * @param id  The document ID
         */
        getStoredFields(id) {
          const shortId = this._idToShortId.get(id);
          if (shortId == null) {
            return void 0;
          }
          return this._storedFields.get(shortId);
        }
        /**
         * Search for documents matching the given search query.
         *
         * The result is a list of scored document IDs matching the query, sorted by
         * descending score, and each including data about which terms were matched and
         * in which fields.
         *
         * ### Basic usage:
         *
         * ```javascript
         * // Search for "zen art motorcycle" with default options: terms have to match
         * // exactly, and individual terms are joined with OR
         * miniSearch.search('zen art motorcycle')
         * // => [ { id: 2, score: 2.77258, match: { ... } }, { id: 4, score: 1.38629, match: { ... } } ]
         * ```
         *
         * ### Restrict search to specific fields:
         *
         * ```javascript
         * // Search only in the 'title' field
         * miniSearch.search('zen', { fields: ['title'] })
         * ```
         *
         * ### Field boosting:
         *
         * ```javascript
         * // Boost a field
         * miniSearch.search('zen', { boost: { title: 2 } })
         * ```
         *
         * ### Prefix search:
         *
         * ```javascript
         * // Search for "moto" with prefix search (it will match documents
         * // containing terms that start with "moto" or "neuro")
         * miniSearch.search('moto neuro', { prefix: true })
         * ```
         *
         * ### Fuzzy search:
         *
         * ```javascript
         * // Search for "ismael" with fuzzy search (it will match documents containing
         * // terms similar to "ismael", with a maximum edit distance of 0.2 term.length
         * // (rounded to nearest integer)
         * miniSearch.search('ismael', { fuzzy: 0.2 })
         * ```
         *
         * ### Combining strategies:
         *
         * ```javascript
         * // Mix of exact match, prefix search, and fuzzy search
         * miniSearch.search('ismael mob', {
         *  prefix: true,
         *  fuzzy: 0.2
         * })
         * ```
         *
         * ### Advanced prefix and fuzzy search:
         *
         * ```javascript
         * // Perform fuzzy and prefix search depending on the search term. Here
         * // performing prefix and fuzzy search only on terms longer than 3 characters
         * miniSearch.search('ismael mob', {
         *  prefix: term => term.length > 3
         *  fuzzy: term => term.length > 3 ? 0.2 : null
         * })
         * ```
         *
         * ### Combine with AND:
         *
         * ```javascript
         * // Combine search terms with AND (to match only documents that contain both
         * // "motorcycle" and "art")
         * miniSearch.search('motorcycle art', { combineWith: 'AND' })
         * ```
         *
         * ### Combine with AND_NOT:
         *
         * There is also an AND_NOT combinator, that finds documents that match the
         * first term, but do not match any of the other terms. This combinator is
         * rarely useful with simple queries, and is meant to be used with advanced
         * query combinations (see later for more details).
         *
         * ### Filtering results:
         *
         * ```javascript
         * // Filter only results in the 'fiction' category (assuming that 'category'
         * // is a stored field)
         * miniSearch.search('motorcycle art', {
         *   filter: (result) => result.category === 'fiction'
         * })
         * ```
         *
         * ### Wildcard query
         *
         * Searching for an empty string (assuming the default tokenizer) returns no
         * results. Sometimes though, one needs to match all documents, like in a
         * "wildcard" search. This is possible by passing the special value
         * {@link MiniSearch.wildcard} as the query:
         *
         * ```javascript
         * // Return search results for all documents
         * miniSearch.search(MiniSearch.wildcard)
         * ```
         *
         * Note that search options such as `filter` and `boostDocument` are still
         * applied, influencing which results are returned, and their order:
         *
         * ```javascript
         * // Return search results for all documents in the 'fiction' category
         * miniSearch.search(MiniSearch.wildcard, {
         *   filter: (result) => result.category === 'fiction'
         * })
         * ```
         *
         * ### Advanced combination of queries:
         *
         * It is possible to combine different subqueries with OR, AND, and AND_NOT,
         * and even with different search options, by passing a query expression
         * tree object as the first argument, instead of a string.
         *
         * ```javascript
         * // Search for documents that contain "zen" and ("motorcycle" or "archery")
         * miniSearch.search({
         *   combineWith: 'AND',
         *   queries: [
         *     'zen',
         *     {
         *       combineWith: 'OR',
         *       queries: ['motorcycle', 'archery']
         *     }
         *   ]
         * })
         *
         * // Search for documents that contain ("apple" or "pear") but not "juice" and
         * // not "tree"
         * miniSearch.search({
         *   combineWith: 'AND_NOT',
         *   queries: [
         *     {
         *       combineWith: 'OR',
         *       queries: ['apple', 'pear']
         *     },
         *     'juice',
         *     'tree'
         *   ]
         * })
         * ```
         *
         * Each node in the expression tree can be either a string, or an object that
         * supports all {@link SearchOptions} fields, plus a `queries` array field for
         * subqueries.
         *
         * Note that, while this can become complicated to do by hand for complex or
         * deeply nested queries, it provides a formalized expression tree API for
         * external libraries that implement a parser for custom query languages.
         *
         * @param query  Search query
         * @param searchOptions  Search options. Each option, if not given, defaults to the corresponding value of `searchOptions` given to the constructor, or to the library default.
         */
        search(query, searchOptions = {}) {
          const { searchOptions: globalSearchOptions } = this._options;
          const searchOptionsWithDefaults = { ...globalSearchOptions, ...searchOptions };
          const rawResults = this.executeQuery(query, searchOptions);
          const results = [];
          for (const [docId, { score, terms, match }] of rawResults) {
            const quality = terms.length || 1;
            const result = {
              id: this._documentIds.get(docId),
              score: score * quality,
              terms: Object.keys(match),
              queryTerms: terms,
              match
            };
            Object.assign(result, this._storedFields.get(docId));
            if (searchOptionsWithDefaults.filter == null || searchOptionsWithDefaults.filter(result)) {
              results.push(result);
            }
          }
          if (query === _MiniSearch.wildcard && searchOptionsWithDefaults.boostDocument == null) {
            return results;
          }
          results.sort(byScore);
          return results;
        }
        /**
         * Provide suggestions for the given search query
         *
         * The result is a list of suggested modified search queries, derived from the
         * given search query, each with a relevance score, sorted by descending score.
         *
         * By default, it uses the same options used for search, except that by
         * default it performs prefix search on the last term of the query, and
         * combine terms with `'AND'` (requiring all query terms to match). Custom
         * options can be passed as a second argument. Defaults can be changed upon
         * calling the {@link MiniSearch} constructor, by passing a
         * `autoSuggestOptions` option.
         *
         * ### Basic usage:
         *
         * ```javascript
         * // Get suggestions for 'neuro':
         * miniSearch.autoSuggest('neuro')
         * // => [ { suggestion: 'neuromancer', terms: [ 'neuromancer' ], score: 0.46240 } ]
         * ```
         *
         * ### Multiple words:
         *
         * ```javascript
         * // Get suggestions for 'zen ar':
         * miniSearch.autoSuggest('zen ar')
         * // => [
         * //  { suggestion: 'zen archery art', terms: [ 'zen', 'archery', 'art' ], score: 1.73332 },
         * //  { suggestion: 'zen art', terms: [ 'zen', 'art' ], score: 1.21313 }
         * // ]
         * ```
         *
         * ### Fuzzy suggestions:
         *
         * ```javascript
         * // Correct spelling mistakes using fuzzy search:
         * miniSearch.autoSuggest('neromancer', { fuzzy: 0.2 })
         * // => [ { suggestion: 'neuromancer', terms: [ 'neuromancer' ], score: 1.03998 } ]
         * ```
         *
         * ### Filtering:
         *
         * ```javascript
         * // Get suggestions for 'zen ar', but only within the 'fiction' category
         * // (assuming that 'category' is a stored field):
         * miniSearch.autoSuggest('zen ar', {
         *   filter: (result) => result.category === 'fiction'
         * })
         * // => [
         * //  { suggestion: 'zen archery art', terms: [ 'zen', 'archery', 'art' ], score: 1.73332 },
         * //  { suggestion: 'zen art', terms: [ 'zen', 'art' ], score: 1.21313 }
         * // ]
         * ```
         *
         * @param queryString  Query string to be expanded into suggestions
         * @param options  Search options. The supported options and default values
         * are the same as for the {@link MiniSearch#search} method, except that by
         * default prefix search is performed on the last term in the query, and terms
         * are combined with `'AND'`.
         * @return  A sorted array of suggestions sorted by relevance score.
         */
        autoSuggest(queryString, options = {}) {
          options = { ...this._options.autoSuggestOptions, ...options };
          const suggestions = /* @__PURE__ */ new Map();
          for (const { score, terms } of this.search(queryString, options)) {
            const phrase = terms.join(" ");
            const suggestion = suggestions.get(phrase);
            if (suggestion != null) {
              suggestion.score += score;
              suggestion.count += 1;
            } else {
              suggestions.set(phrase, { score, terms, count: 1 });
            }
          }
          const results = [];
          for (const [suggestion, { score, terms, count }] of suggestions) {
            results.push({ suggestion, terms, score: score / count });
          }
          results.sort(byScore);
          return results;
        }
        /**
         * Total number of documents available to search
         */
        get documentCount() {
          return this._documentCount;
        }
        /**
         * Number of terms in the index
         */
        get termCount() {
          return this._index.size;
        }
        /**
         * Deserializes a JSON index (serialized with `JSON.stringify(miniSearch)`)
         * and instantiates a MiniSearch instance. It should be given the same options
         * originally used when serializing the index.
         *
         * ### Usage:
         *
         * ```javascript
         * // If the index was serialized with:
         * let miniSearch = new MiniSearch({ fields: ['title', 'text'] })
         * miniSearch.addAll(documents)
         *
         * const json = JSON.stringify(miniSearch)
         * // It can later be deserialized like this:
         * miniSearch = MiniSearch.loadJSON(json, { fields: ['title', 'text'] })
         * ```
         *
         * @param json  JSON-serialized index
         * @param options  configuration options, same as the constructor
         * @return An instance of MiniSearch deserialized from the given JSON.
         */
        static loadJSON(json, options) {
          if (options == null) {
            throw new Error("MiniSearch: loadJSON should be given the same options used when serializing the index");
          }
          return this.loadJS(JSON.parse(json), options);
        }
        /**
         * Async equivalent of {@link MiniSearch.loadJSON}
         *
         * This function is an alternative to {@link MiniSearch.loadJSON} that returns
         * a promise, and loads the index in batches, leaving pauses between them to avoid
         * blocking the main thread. It tends to be slower than the synchronous
         * version, but does not block the main thread, so it can be a better choice
         * when deserializing very large indexes.
         *
         * @param json  JSON-serialized index
         * @param options  configuration options, same as the constructor
         * @return A Promise that will resolve to an instance of MiniSearch deserialized from the given JSON.
         */
        static async loadJSONAsync(json, options) {
          if (options == null) {
            throw new Error("MiniSearch: loadJSON should be given the same options used when serializing the index");
          }
          return this.loadJSAsync(JSON.parse(json), options);
        }
        /**
         * Returns the default value of an option. It will throw an error if no option
         * with the given name exists.
         *
         * @param optionName  Name of the option
         * @return The default value of the given option
         *
         * ### Usage:
         *
         * ```javascript
         * // Get default tokenizer
         * MiniSearch.getDefault('tokenize')
         *
         * // Get default term processor
         * MiniSearch.getDefault('processTerm')
         *
         * // Unknown options will throw an error
         * MiniSearch.getDefault('notExisting')
         * // => throws 'MiniSearch: unknown option "notExisting"'
         * ```
         */
        static getDefault(optionName) {
          if (defaultOptions.hasOwnProperty(optionName)) {
            return getOwnProperty(defaultOptions, optionName);
          } else {
            throw new Error(`MiniSearch: unknown option "${optionName}"`);
          }
        }
        /**
         * @ignore
         */
        static loadJS(js, options) {
          const { index, documentIds, fieldLength, storedFields, serializationVersion } = js;
          const miniSearch = this.instantiateMiniSearch(js, options);
          miniSearch._documentIds = objectToNumericMap(documentIds);
          miniSearch._fieldLength = objectToNumericMap(fieldLength);
          miniSearch._storedFields = objectToNumericMap(storedFields);
          for (const [shortId, id] of miniSearch._documentIds) {
            miniSearch._idToShortId.set(id, shortId);
          }
          for (const [term, data] of index) {
            const dataMap = /* @__PURE__ */ new Map();
            for (const fieldId of Object.keys(data)) {
              let indexEntry = data[fieldId];
              if (serializationVersion === 1) {
                indexEntry = indexEntry.ds;
              }
              dataMap.set(parseInt(fieldId, 10), objectToNumericMap(indexEntry));
            }
            miniSearch._index.set(term, dataMap);
          }
          return miniSearch;
        }
        /**
         * @ignore
         */
        static async loadJSAsync(js, options) {
          const { index, documentIds, fieldLength, storedFields, serializationVersion } = js;
          const miniSearch = this.instantiateMiniSearch(js, options);
          miniSearch._documentIds = await objectToNumericMapAsync(documentIds);
          miniSearch._fieldLength = await objectToNumericMapAsync(fieldLength);
          miniSearch._storedFields = await objectToNumericMapAsync(storedFields);
          for (const [shortId, id] of miniSearch._documentIds) {
            miniSearch._idToShortId.set(id, shortId);
          }
          let count = 0;
          for (const [term, data] of index) {
            const dataMap = /* @__PURE__ */ new Map();
            for (const fieldId of Object.keys(data)) {
              let indexEntry = data[fieldId];
              if (serializationVersion === 1) {
                indexEntry = indexEntry.ds;
              }
              dataMap.set(parseInt(fieldId, 10), await objectToNumericMapAsync(indexEntry));
            }
            if (++count % 1e3 === 0)
              await wait(0);
            miniSearch._index.set(term, dataMap);
          }
          return miniSearch;
        }
        /**
         * @ignore
         */
        static instantiateMiniSearch(js, options) {
          const { documentCount, nextId, fieldIds, averageFieldLength, dirtCount, serializationVersion } = js;
          if (serializationVersion !== 1 && serializationVersion !== 2) {
            throw new Error("MiniSearch: cannot deserialize an index created with an incompatible version");
          }
          const miniSearch = new _MiniSearch(options);
          miniSearch._documentCount = documentCount;
          miniSearch._nextId = nextId;
          miniSearch._idToShortId = /* @__PURE__ */ new Map();
          miniSearch._fieldIds = fieldIds;
          miniSearch._avgFieldLength = averageFieldLength;
          miniSearch._dirtCount = dirtCount || 0;
          miniSearch._index = new SearchableMap();
          return miniSearch;
        }
        /**
         * @ignore
         */
        executeQuery(query, searchOptions = {}) {
          if (query === _MiniSearch.wildcard) {
            return this.executeWildcardQuery(searchOptions);
          }
          if (typeof query !== "string") {
            const options2 = { ...searchOptions, ...query, queries: void 0 };
            const results2 = query.queries.map((subquery) => this.executeQuery(subquery, options2));
            return this.combineResults(results2, options2.combineWith);
          }
          const { tokenize, processTerm, searchOptions: globalSearchOptions } = this._options;
          const options = { tokenize, processTerm, ...globalSearchOptions, ...searchOptions };
          const { tokenize: searchTokenize, processTerm: searchProcessTerm } = options;
          const terms = searchTokenize(query).flatMap((term) => searchProcessTerm(term)).filter((term) => !!term);
          const queries = terms.map(termToQuerySpec(options));
          const results = queries.map((query2) => this.executeQuerySpec(query2, options));
          return this.combineResults(results, options.combineWith);
        }
        /**
         * @ignore
         */
        executeQuerySpec(query, searchOptions) {
          const options = { ...this._options.searchOptions, ...searchOptions };
          const boosts = (options.fields || this._options.fields).reduce((boosts2, field) => ({ ...boosts2, [field]: getOwnProperty(options.boost, field) || 1 }), {});
          const { boostDocument, weights, maxFuzzy, bm25: bm25params } = options;
          const { fuzzy: fuzzyWeight, prefix: prefixWeight } = { ...defaultSearchOptions.weights, ...weights };
          const data = this._index.get(query.term);
          const results = this.termResults(query.term, query.term, 1, query.termBoost, data, boosts, boostDocument, bm25params);
          let prefixMatches;
          let fuzzyMatches;
          if (query.prefix) {
            prefixMatches = this._index.atPrefix(query.term);
          }
          if (query.fuzzy) {
            const fuzzy = query.fuzzy === true ? 0.2 : query.fuzzy;
            const maxDistance = fuzzy < 1 ? Math.min(maxFuzzy, Math.round(query.term.length * fuzzy)) : fuzzy;
            if (maxDistance)
              fuzzyMatches = this._index.fuzzyGet(query.term, maxDistance);
          }
          if (prefixMatches) {
            for (const [term, data2] of prefixMatches) {
              const distance = term.length - query.term.length;
              if (!distance) {
                continue;
              }
              fuzzyMatches === null || fuzzyMatches === void 0 ? void 0 : fuzzyMatches.delete(term);
              const weight = prefixWeight * term.length / (term.length + 0.3 * distance);
              this.termResults(query.term, term, weight, query.termBoost, data2, boosts, boostDocument, bm25params, results);
            }
          }
          if (fuzzyMatches) {
            for (const term of fuzzyMatches.keys()) {
              const [data2, distance] = fuzzyMatches.get(term);
              if (!distance) {
                continue;
              }
              const weight = fuzzyWeight * term.length / (term.length + distance);
              this.termResults(query.term, term, weight, query.termBoost, data2, boosts, boostDocument, bm25params, results);
            }
          }
          return results;
        }
        /**
         * @ignore
         */
        executeWildcardQuery(searchOptions) {
          const results = /* @__PURE__ */ new Map();
          const options = { ...this._options.searchOptions, ...searchOptions };
          for (const [shortId, id] of this._documentIds) {
            const score = options.boostDocument ? options.boostDocument(id, "", this._storedFields.get(shortId)) : 1;
            results.set(shortId, {
              score,
              terms: [],
              match: {}
            });
          }
          return results;
        }
        /**
         * @ignore
         */
        combineResults(results, combineWith = OR) {
          if (results.length === 0) {
            return /* @__PURE__ */ new Map();
          }
          const operator = combineWith.toLowerCase();
          const combinator = combinators[operator];
          if (!combinator) {
            throw new Error(`Invalid combination operator: ${combineWith}`);
          }
          return results.reduce(combinator) || /* @__PURE__ */ new Map();
        }
        /**
         * Allows serialization of the index to JSON, to possibly store it and later
         * deserialize it with {@link MiniSearch.loadJSON}.
         *
         * Normally one does not directly call this method, but rather call the
         * standard JavaScript `JSON.stringify()` passing the {@link MiniSearch}
         * instance, and JavaScript will internally call this method. Upon
         * deserialization, one must pass to {@link MiniSearch.loadJSON} the same
         * options used to create the original instance that was serialized.
         *
         * ### Usage:
         *
         * ```javascript
         * // Serialize the index:
         * let miniSearch = new MiniSearch({ fields: ['title', 'text'] })
         * miniSearch.addAll(documents)
         * const json = JSON.stringify(miniSearch)
         *
         * // Later, to deserialize it:
         * miniSearch = MiniSearch.loadJSON(json, { fields: ['title', 'text'] })
         * ```
         *
         * @return A plain-object serializable representation of the search index.
         */
        toJSON() {
          const index = [];
          for (const [term, fieldIndex] of this._index) {
            const data = {};
            for (const [fieldId, freqs] of fieldIndex) {
              data[fieldId] = Object.fromEntries(freqs);
            }
            index.push([term, data]);
          }
          return {
            documentCount: this._documentCount,
            nextId: this._nextId,
            documentIds: Object.fromEntries(this._documentIds),
            fieldIds: this._fieldIds,
            fieldLength: Object.fromEntries(this._fieldLength),
            averageFieldLength: this._avgFieldLength,
            storedFields: Object.fromEntries(this._storedFields),
            dirtCount: this._dirtCount,
            index,
            serializationVersion: 2
          };
        }
        /**
         * @ignore
         */
        termResults(sourceTerm, derivedTerm, termWeight, termBoost, fieldTermData, fieldBoosts, boostDocumentFn, bm25params, results = /* @__PURE__ */ new Map()) {
          if (fieldTermData == null)
            return results;
          for (const field of Object.keys(fieldBoosts)) {
            const fieldBoost = fieldBoosts[field];
            const fieldId = this._fieldIds[field];
            const fieldTermFreqs = fieldTermData.get(fieldId);
            if (fieldTermFreqs == null)
              continue;
            let matchingFields = fieldTermFreqs.size;
            const avgFieldLength = this._avgFieldLength[fieldId];
            for (const docId of fieldTermFreqs.keys()) {
              if (!this._documentIds.has(docId)) {
                this.removeTerm(fieldId, docId, derivedTerm);
                matchingFields -= 1;
                continue;
              }
              const docBoost = boostDocumentFn ? boostDocumentFn(this._documentIds.get(docId), derivedTerm, this._storedFields.get(docId)) : 1;
              if (!docBoost)
                continue;
              const termFreq = fieldTermFreqs.get(docId);
              const fieldLength = this._fieldLength.get(docId)[fieldId];
              const rawScore = calcBM25Score(termFreq, matchingFields, this._documentCount, fieldLength, avgFieldLength, bm25params);
              const weightedScore = termWeight * termBoost * fieldBoost * docBoost * rawScore;
              const result = results.get(docId);
              if (result) {
                result.score += weightedScore;
                assignUniqueTerm(result.terms, sourceTerm);
                const match = getOwnProperty(result.match, derivedTerm);
                if (match) {
                  match.push(field);
                } else {
                  result.match[derivedTerm] = [field];
                }
              } else {
                results.set(docId, {
                  score: weightedScore,
                  terms: [sourceTerm],
                  match: { [derivedTerm]: [field] }
                });
              }
            }
          }
          return results;
        }
        /**
         * @ignore
         */
        addTerm(fieldId, documentId, term) {
          const indexData = this._index.fetch(term, createMap);
          let fieldIndex = indexData.get(fieldId);
          if (fieldIndex == null) {
            fieldIndex = /* @__PURE__ */ new Map();
            fieldIndex.set(documentId, 1);
            indexData.set(fieldId, fieldIndex);
          } else {
            const docs = fieldIndex.get(documentId);
            fieldIndex.set(documentId, (docs || 0) + 1);
          }
        }
        /**
         * @ignore
         */
        removeTerm(fieldId, documentId, term) {
          if (!this._index.has(term)) {
            this.warnDocumentChanged(documentId, fieldId, term);
            return;
          }
          const indexData = this._index.fetch(term, createMap);
          const fieldIndex = indexData.get(fieldId);
          if (fieldIndex == null || fieldIndex.get(documentId) == null) {
            this.warnDocumentChanged(documentId, fieldId, term);
          } else if (fieldIndex.get(documentId) <= 1) {
            if (fieldIndex.size <= 1) {
              indexData.delete(fieldId);
            } else {
              fieldIndex.delete(documentId);
            }
          } else {
            fieldIndex.set(documentId, fieldIndex.get(documentId) - 1);
          }
          if (this._index.get(term).size === 0) {
            this._index.delete(term);
          }
        }
        /**
         * @ignore
         */
        warnDocumentChanged(shortDocumentId, fieldId, term) {
          for (const fieldName of Object.keys(this._fieldIds)) {
            if (this._fieldIds[fieldName] === fieldId) {
              this._options.logger("warn", `MiniSearch: document with ID ${this._documentIds.get(shortDocumentId)} has changed before removal: term "${term}" was not present in field "${fieldName}". Removing a document after it has changed can corrupt the index!`, "version_conflict");
              return;
            }
          }
        }
        /**
         * @ignore
         */
        addDocumentId(documentId) {
          const shortDocumentId = this._nextId;
          this._idToShortId.set(documentId, shortDocumentId);
          this._documentIds.set(shortDocumentId, documentId);
          this._documentCount += 1;
          this._nextId += 1;
          return shortDocumentId;
        }
        /**
         * @ignore
         */
        addFields(fields) {
          for (let i = 0; i < fields.length; i++) {
            this._fieldIds[fields[i]] = i;
          }
        }
        /**
         * @ignore
         */
        addFieldLength(documentId, fieldId, count, length) {
          let fieldLengths = this._fieldLength.get(documentId);
          if (fieldLengths == null)
            this._fieldLength.set(documentId, fieldLengths = []);
          fieldLengths[fieldId] = length;
          const averageFieldLength = this._avgFieldLength[fieldId] || 0;
          const totalFieldLength = averageFieldLength * count + length;
          this._avgFieldLength[fieldId] = totalFieldLength / (count + 1);
        }
        /**
         * @ignore
         */
        removeFieldLength(documentId, fieldId, count, length) {
          if (count === 1) {
            this._avgFieldLength[fieldId] = 0;
            return;
          }
          const totalFieldLength = this._avgFieldLength[fieldId] * count - length;
          this._avgFieldLength[fieldId] = totalFieldLength / (count - 1);
        }
        /**
         * @ignore
         */
        saveStoredFields(documentId, doc) {
          const { storeFields, extractField } = this._options;
          if (storeFields == null || storeFields.length === 0) {
            return;
          }
          let documentFields = this._storedFields.get(documentId);
          if (documentFields == null)
            this._storedFields.set(documentId, documentFields = {});
          for (const fieldName of storeFields) {
            const fieldValue = extractField(doc, fieldName);
            if (fieldValue !== void 0)
              documentFields[fieldName] = fieldValue;
          }
        }
      };
      MiniSearch.wildcard = Symbol("*");
      getOwnProperty = (object, property) => Object.prototype.hasOwnProperty.call(object, property) ? object[property] : void 0;
      combinators = {
        [OR]: (a, b) => {
          for (const docId of b.keys()) {
            const existing = a.get(docId);
            if (existing == null) {
              a.set(docId, b.get(docId));
            } else {
              const { score, terms, match } = b.get(docId);
              existing.score = existing.score + score;
              existing.match = Object.assign(existing.match, match);
              assignUniqueTerms(existing.terms, terms);
            }
          }
          return a;
        },
        [AND]: (a, b) => {
          const combined = /* @__PURE__ */ new Map();
          for (const docId of b.keys()) {
            const existing = a.get(docId);
            if (existing == null)
              continue;
            const { score, terms, match } = b.get(docId);
            assignUniqueTerms(existing.terms, terms);
            combined.set(docId, {
              score: existing.score + score,
              terms: existing.terms,
              match: Object.assign(existing.match, match)
            });
          }
          return combined;
        },
        [AND_NOT]: (a, b) => {
          for (const docId of b.keys())
            a.delete(docId);
          return a;
        }
      };
      defaultBM25params = { k: 1.2, b: 0.7, d: 0.5 };
      calcBM25Score = (termFreq, matchingCount, totalCount, fieldLength, avgFieldLength, bm25params) => {
        const { k, b, d } = bm25params;
        const invDocFreq = Math.log(1 + (totalCount - matchingCount + 0.5) / (matchingCount + 0.5));
        return invDocFreq * (d + termFreq * (k + 1) / (termFreq + k * (1 - b + b * fieldLength / avgFieldLength)));
      };
      termToQuerySpec = (options) => (term, i, terms) => {
        const fuzzy = typeof options.fuzzy === "function" ? options.fuzzy(term, i, terms) : options.fuzzy || false;
        const prefix = typeof options.prefix === "function" ? options.prefix(term, i, terms) : options.prefix === true;
        const termBoost = typeof options.boostTerm === "function" ? options.boostTerm(term, i, terms) : 1;
        return { term, fuzzy, prefix, termBoost };
      };
      defaultOptions = {
        idField: "id",
        extractField: (document2, fieldName) => document2[fieldName],
        stringifyField: (fieldValue, fieldName) => fieldValue.toString(),
        tokenize: (text) => text.split(SPACE_OR_PUNCTUATION),
        processTerm: (term) => term.toLowerCase(),
        fields: void 0,
        searchOptions: void 0,
        storeFields: [],
        logger: (level, message) => {
          if (typeof (console === null || console === void 0 ? void 0 : console[level]) === "function")
            console[level](message);
        },
        autoVacuum: true
      };
      defaultSearchOptions = {
        combineWith: OR,
        prefix: false,
        fuzzy: false,
        maxFuzzy: 6,
        boost: {},
        weights: { fuzzy: 0.45, prefix: 0.375 },
        bm25: defaultBM25params
      };
      defaultAutoSuggestOptions = {
        combineWith: AND,
        prefix: (term, i, terms) => i === terms.length - 1
      };
      defaultVacuumOptions = { batchSize: 1e3, batchWait: 10 };
      defaultVacuumConditions = { minDirtFactor: 0.1, minDirtCount: 20 };
      defaultAutoVacuumOptions = { ...defaultVacuumOptions, ...defaultVacuumConditions };
      assignUniqueTerm = (target, term) => {
        if (!target.includes(term))
          target.push(term);
      };
      assignUniqueTerms = (target, source) => {
        for (const term of source) {
          if (!target.includes(term))
            target.push(term);
        }
      };
      byScore = ({ score: a }, { score: b }) => b - a;
      createMap = () => /* @__PURE__ */ new Map();
      objectToNumericMap = (object) => {
        const map = /* @__PURE__ */ new Map();
        for (const key of Object.keys(object)) {
          map.set(parseInt(key, 10), object[key]);
        }
        return map;
      };
      objectToNumericMapAsync = async (object) => {
        const map = /* @__PURE__ */ new Map();
        let count = 0;
        for (const key of Object.keys(object)) {
          map.set(parseInt(key, 10), object[key]);
          if (++count % 1e3 === 0) {
            await wait(0);
          }
        }
        return map;
      };
      wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
      SPACE_OR_PUNCTUATION = /[\n\r\p{Z}\p{P}]+/u;
    }
  });

  // src/main/typescript/search/search.ts
  function createSearch(index, options) {
    const parsedIndex = typeof index === "string" ? JSON.parse(index) : index;
    return new DocumentSearch(parsedIndex, options);
  }
  var DEFAULT_OPTIONS, DocumentSearch;
  var init_search = __esm({
    "src/main/typescript/search/search.ts"() {
      "use strict";
      init_es();
      DEFAULT_OPTIONS = {
        fuzzy: 0.2,
        prefix: true,
        titleBoost: 3,
        headingsBoost: 2,
        keywordsBoost: 2.5,
        descriptionBoost: 1.5,
        contentBoost: 1
      };
      DocumentSearch = class {
        /**
         * Creates a new DocumentSearch instance.
         *
         * @param index - The search index to use
         * @param options - Search configuration options
         */
        constructor(index, options = {}) {
          this.options = { ...DEFAULT_OPTIONS, ...options };
          this.entriesMap = /* @__PURE__ */ new Map();
          this.miniSearch = this.createMiniSearch();
          this.indexEntries(index.entries);
        }
        createMiniSearch() {
          return new MiniSearch({
            fields: ["title", "description", "keywords", "content", "headings"],
            storeFields: ["url"],
            searchOptions: {
              boost: {
                title: this.options.titleBoost,
                headings: this.options.headingsBoost,
                keywords: this.options.keywordsBoost,
                description: this.options.descriptionBoost,
                content: this.options.contentBoost
              },
              fuzzy: this.options.fuzzy,
              prefix: this.options.prefix
            }
          });
        }
        indexEntries(entries) {
          const documents = entries.map((entry, index) => {
            this.entriesMap.set(index, entry);
            return {
              id: index,
              url: entry.url,
              title: entry.title ?? "",
              description: entry.description ?? "",
              keywords: entry.keywords.join(" "),
              content: entry.content,
              headings: entry.headings.map((h) => h.text).join(" ")
            };
          });
          this.miniSearch.addAll(documents);
        }
        /**
         * Performs a fuzzy search on the index.
         *
         * @param query - The search query string
         * @param searchOptions - Optional per-search options to override defaults
         * @returns An array of search results sorted by relevance score
         */
        search(query, searchOptions) {
          if (!query.trim()) {
            return [];
          }
          const results = this.miniSearch.search(query, searchOptions);
          const seenUrls = /* @__PURE__ */ new Set();
          const mappedResults = results.map((result) => {
            const entry = this.entriesMap.get(result.id);
            if (!entry) return null;
            if (seenUrls.has(entry.url)) return null;
            seenUrls.add(entry.url);
            return {
              entry,
              score: result.score,
              matchedTerms: result.terms,
              matchedFields: result.match
            };
          }).filter((result) => result !== null);
          if (this.options.maxResults !== void 0) {
            return mappedResults.slice(0, this.options.maxResults);
          }
          return mappedResults;
        }
        /**
         * Gets autocomplete suggestions for a partial query.
         *
         * @param query - The partial search query
         * @param maxSuggestions - Maximum number of suggestions to return. Default: 5
         * @returns An array of suggested search terms
         */
        suggest(query, maxSuggestions = 5) {
          if (!query.trim()) {
            return [];
          }
          return this.miniSearch.autoSuggest(query, { fuzzy: this.options.fuzzy, prefix: this.options.prefix }).slice(0, maxSuggestions).map((suggestion) => suggestion.suggestion);
        }
        /**
         * Returns the number of indexed entries.
         */
        get entryCount() {
          return this.miniSearch.documentCount;
        }
      };
    }
  });

  // src/main/typescript/util/escape.ts
  function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
  }
  function escapeRegExp(text) {
    return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }
  var init_escape = __esm({
    "src/main/typescript/util/escape.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/search/search-highlight.ts
  function trimTitleFromContent(content, title) {
    if (!title || !content) return content;
    const trimmedContent = content.trimStart();
    if (trimmedContent.toLowerCase().startsWith(title.toLowerCase())) {
      return trimmedContent.slice(title.length).trimStart();
    }
    return content;
  }
  function extractPreviewAroundMatch(content, matchedTerms, maxLength = 300) {
    if (content.length <= maxLength) return content;
    const lowerContent = content.toLowerCase();
    let firstMatchIndex = -1;
    for (const term of matchedTerms) {
      const index = lowerContent.indexOf(term.toLowerCase());
      if (index !== -1 && (firstMatchIndex === -1 || index < firstMatchIndex)) {
        firstMatchIndex = index;
      }
    }
    if (firstMatchIndex === -1) {
      return content.slice(0, maxLength).trimEnd() + "\u2026";
    }
    const halfLength = Math.floor(maxLength / 2);
    let start = Math.max(0, firstMatchIndex - halfLength);
    let end = Math.min(content.length, start + maxLength);
    if (end === content.length) {
      start = Math.max(0, end - maxLength);
    }
    let preview = content.slice(start, end);
    if (start > 0) preview = "\u2026" + preview.trimStart();
    if (end < content.length) preview = preview.trimEnd() + "\u2026";
    return preview;
  }
  function highlightTerms(text, matchedTerms) {
    if (matchedTerms.length === 0) return escapeHtml(text);
    const sortedTerms = [...matchedTerms].sort((a, b) => b.length - a.length);
    const pattern = new RegExp(`(${sortedTerms.map((t) => escapeRegExp(t)).join("|")})`, "gi");
    const parts = text.split(pattern);
    return parts.map((part) => {
      const isMatch = sortedTerms.some((term) => part.toLowerCase() === term.toLowerCase());
      const escaped = escapeHtml(part);
      return isMatch ? `<strong>${escaped}</strong>` : escaped;
    }).join("");
  }
  var init_search_highlight = __esm({
    "src/main/typescript/search/search-highlight.ts"() {
      "use strict";
      init_escape();
    }
  });

  // src/main/typescript/search/search-result-expander.ts
  function expandResult(result) {
    const { entry, matchedFields } = result;
    const items = [];
    items.push({
      url: entry.url,
      title: entry.title ?? entry.url,
      description: getHighlightedDescription(result)
    });
    const headingTerms = getTermsForField(matchedFields, "headings");
    if (headingTerms.length > 0) {
      const matchingHeadings = findMatchingHeadings(entry.headings, headingTerms, entry.title);
      for (const heading of matchingHeadings) {
        items.push({
          url: `${entry.url}#${heading.anchor}`,
          title: heading.text,
          description: "",
          parentTitle: entry.title ?? entry.url
        });
      }
    }
    return items;
  }
  function getTermsForField(matchedFields, field) {
    return Object.entries(matchedFields).filter(([, fields]) => fields.includes(field)).map(([term]) => term);
  }
  function findMatchingHeadings(headings, terms, title) {
    const normalizedTitle = title?.toLowerCase();
    return headings.filter(
      (heading) => heading.text.toLowerCase() !== normalizedTitle && terms.some((term) => heading.text.toLowerCase().includes(term.toLowerCase()))
    );
  }
  function isTitleOrHeadingMatch(matchedFields) {
    const flattened = Object.values(matchedFields).flat();
    return flattened.includes("title") || flattened.includes("headings");
  }
  function getHighlightedDescription(result) {
    const { entry, matchedTerms, matchedFields } = result;
    const text = entry.description ?? trimTitleFromContent(entry.content, entry.title);
    if (!text) return "";
    if (isTitleOrHeadingMatch(matchedFields)) {
      const preview2 = extractPreviewAroundMatch(text, []);
      return escapeHtml(preview2);
    }
    const preview = extractPreviewAroundMatch(text, matchedTerms);
    return highlightTerms(preview, matchedTerms);
  }
  var init_search_result_expander = __esm({
    "src/main/typescript/search/search-result-expander.ts"() {
      "use strict";
      init_search_highlight();
    }
  });

  // src/main/typescript/search/search-result-renderer.ts
  function renderResultItem(item, index) {
    const className = item.parentTitle ? "search-result search-result-heading" : "search-result";
    const titleHtml = item.parentTitle ? `${escapeHtml(item.parentTitle)}<span class="search-result-chevron"></span>${escapeHtml(item.title)}` : escapeHtml(item.title);
    return `<a href="${escapeHtml(item.url)}" class="${className}" role="option" data-index="${index}">
        <div class="search-result-title">${titleHtml}</div>
        ${item.description ? `<div class="search-result-description">${item.description}</div>` : ""}
    </a>`;
  }
  function renderResultItems(items) {
    return items.map((item, index) => renderResultItem(item, index)).join("");
  }
  var init_search_result_renderer = __esm({
    "src/main/typescript/search/search-result-renderer.ts"() {
      "use strict";
      init_search_highlight();
    }
  });

  // src/main/typescript/util/meta.ts
  function getMetaContent(name) {
    const meta = document.querySelector(`meta[name="${name}"]`);
    return meta?.getAttribute("content") ?? null;
  }
  function getRootPath() {
    return getMetaContent(ROOT_PATH_META) || "/";
  }
  var ROOT_PATH_META;
  var init_meta = __esm({
    "src/main/typescript/util/meta.ts"() {
      "use strict";
      ROOT_PATH_META = "quarkdown:root-path";
    }
  });

  // src/main/typescript/document/handlers/docs/search-field.ts
  var SEARCH_INPUT_ID, SEARCH_RESULTS_ID, SEARCH_INDEX_META_NAME, DEBOUNCE_MS, MAX_RESULTS, SearchField;
  var init_search_field = __esm({
    "src/main/typescript/document/handlers/docs/search-field.ts"() {
      "use strict";
      init_document_handler();
      init_search();
      init_search_result_expander();
      init_search_result_renderer();
      init_meta();
      SEARCH_INPUT_ID = "search-input";
      SEARCH_RESULTS_ID = "search-results";
      SEARCH_INDEX_META_NAME = "quarkdown:search-index";
      DEBOUNCE_MS = 150;
      MAX_RESULTS = 10;
      SearchField = class extends DocumentHandler {
        constructor() {
          super(...arguments);
          this.search = null;
          this.input = null;
          this.resultsContainer = null;
          this.debounceTimeout = null;
          this.selectedIndex = -1;
        }
        async onPostRendering() {
          this.input = document.getElementById(SEARCH_INPUT_ID);
          if (!this.input) return;
          const indexPath = this.getSearchIndexPath();
          if (!indexPath) return;
          await this.initializeSearch(indexPath);
          this.createResultsContainer();
          this.bindEvents();
        }
        /**
         * Retrieves the search index path from the meta tag.
         * @returns The path to the search index JSON, or null if not found
         */
        getSearchIndexPath() {
          return getMetaContent(SEARCH_INDEX_META_NAME);
        }
        /**
         * Fetches and initializes the search index from the given path.
         * @param indexPath - URL path to the search index JSON file
         */
        async initializeSearch(indexPath) {
          const response = await fetch(indexPath);
          if (!response.ok) return;
          const index = await response.json();
          this.search = createSearch(index, { maxResults: MAX_RESULTS });
        }
        /**
         * Creates the results dropdown container and appends it to the search wrapper.
         */
        createResultsContainer() {
          const wrapper = this.input.closest(".search-wrapper");
          if (!wrapper) return;
          this.resultsContainer = document.createElement("div");
          this.resultsContainer.id = SEARCH_RESULTS_ID;
          this.resultsContainer.setAttribute("role", "listbox");
          this.resultsContainer.hidden = true;
          wrapper.appendChild(this.resultsContainer);
        }
        /**
         * Binds event listeners for search input, keyboard navigation, and blur handling.
         */
        bindEvents() {
          this.input.addEventListener("input", () => this.onInputChange());
          this.input.addEventListener("keydown", (e) => this.onKeyDown(e));
          this.input.addEventListener("blur", () => this.hideResultsDelayed());
          this.resultsContainer.addEventListener("mousedown", (e) => e.preventDefault());
        }
        /**
         * Handles input changes with debouncing to avoid excessive searches.
         */
        onInputChange() {
          if (this.debounceTimeout) clearTimeout(this.debounceTimeout);
          this.debounceTimeout = setTimeout(() => this.performSearch(), DEBOUNCE_MS);
        }
        /**
         * Executes the search query and renders the results.
         */
        performSearch() {
          const query = this.input.value.trim();
          if (!query || !this.search) {
            this.hideResults();
            return;
          }
          const results = this.search.search(query);
          this.renderResults(results);
        }
        /**
         * Renders search results into the dropdown container.
         * @param results - Array of search results to display
         */
        renderResults(results) {
          if (results.length === 0) {
            this.hideResults();
            return;
          }
          const displayItems = results.flatMap((result) => expandResult(result)).map((item) => ({ ...item, url: this.resolveUrl(item.url) }));
          this.selectedIndex = -1;
          this.resultsContainer.innerHTML = renderResultItems(displayItems);
          this.resultsContainer.hidden = false;
        }
        /**
         * Resolves a URL to be relative to the current page's parent directory.
         * URLs starting with '/' are converted to relative paths.
         * @param url - The URL to resolve
         * @returns The resolved relative URL
         */
        resolveUrl(url) {
          if (!url.startsWith("/")) return url;
          return getRootPath() + url;
        }
        /**
         * Handles keyboard navigation within the search results.
         * @param event - The keyboard event
         */
        onKeyDown(event) {
          if (event.key === "Escape") {
            this.hideResults();
            this.input.blur();
            return;
          }
          if (this.resultsContainer.hidden) return;
          const items = this.resultsContainer.querySelectorAll(".search-result");
          if (items.length === 0) return;
          const lastIndex = items.length - 1;
          switch (event.key) {
            case "ArrowDown":
              event.preventDefault();
              this.selectItem(items, this.selectedIndex < lastIndex ? this.selectedIndex + 1 : 0);
              break;
            case "ArrowUp":
              event.preventDefault();
              this.selectItem(items, this.selectedIndex > 0 ? this.selectedIndex - 1 : lastIndex);
              break;
            case "Enter":
              if (this.selectedIndex >= 0) {
                event.preventDefault();
                items[this.selectedIndex].click();
              }
              break;
          }
        }
        /**
         * Updates the selected item in the results list.
         * @param items - The list of result elements
         * @param index - The index to select
         */
        selectItem(items, index) {
          items.forEach((item, i) => item.classList.toggle("selected", i === index));
          this.selectedIndex = index;
          items[index].scrollIntoView({ block: "nearest" });
        }
        /**
         * Hides the results dropdown and resets selection.
         */
        hideResults() {
          this.resultsContainer.hidden = true;
          this.selectedIndex = -1;
        }
        /**
         * Hides results after a short delay to allow click events to fire.
         */
        hideResultsDelayed() {
          setTimeout(() => this.hideResults(), 150);
        }
      };
    }
  });

  // src/main/typescript/document/handlers/footnotes/footnotes-docs.ts
  var FootnotesDocs;
  var init_footnotes_docs = __esm({
    "src/main/typescript/document/handlers/footnotes/footnotes-docs.ts"() {
      "use strict";
      init_footnotes_document_handler();
      FootnotesDocs = class extends FootnotesDocumentHandler {
        async onPostRendering() {
          const footnoteArea = document.getElementById("footnote-area");
          if (!footnoteArea) return;
          if (this.footnotes.length === 0) {
            footnoteArea.remove();
            return;
          }
          this.footnotes.forEach(({ definition }) => {
            definition.remove();
            footnoteArea.appendChild(definition);
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/docs/util/page-list-analyzer.ts
  var PAGE_LIST_NAV_SELECTOR, CURRENT_PAGE_SELECTOR, PageListAnalyzer;
  var init_page_list_analyzer = __esm({
    "src/main/typescript/document/handlers/docs/util/page-list-analyzer.ts"() {
      "use strict";
      PAGE_LIST_NAV_SELECTOR = 'nav[data-role="page-list"]';
      CURRENT_PAGE_SELECTOR = 'a[aria-current="page"]';
      PageListAnalyzer = class {
        constructor() {
          this.nav = document.querySelector(PAGE_LIST_NAV_SELECTOR);
          this.currentPageAnchor = this.nav?.querySelector(CURRENT_PAGE_SELECTOR) ?? null;
        }
        /**
         * Gets all anchor elements within the page list in document order,
         * excluding anchor links within the current page.
         */
        getAllLinks() {
          if (!this.nav) return [];
          return Array.from(this.nav.querySelectorAll("a")).filter((link) => !this.isSamePageAnchor(link));
        }
        /**
         * Checks if a link is an anchor within the current page.
         */
        isSamePageAnchor(link) {
          const href = link.getAttribute("href");
          if (!href || href.startsWith("#")) return true;
          if (link.pathname === location.pathname && link.hash !== "") return true;
          const currentPageHref = this.currentPageAnchor?.getAttribute("href");
          return !!(currentPageHref && href.startsWith(currentPageHref + "#"));
        }
        /**
         * Finds the index of the current page link within all links.
         */
        getCurrentIndex(links) {
          if (!this.currentPageAnchor) return null;
          const index = links.indexOf(this.currentPageAnchor);
          return index === -1 ? null : index;
        }
        /**
         * Returns the next page link, or null if there is no next page.
         */
        getNextPageLink() {
          const links = this.getAllLinks();
          const currentIndex = this.getCurrentIndex(links);
          if (currentIndex === null || currentIndex >= links.length - 1) return null;
          return links[currentIndex + 1];
        }
        /**
         * Returns the previous page link, or null if there is no previous page.
         */
        getPreviousPageLink() {
          const links = this.getAllLinks();
          const currentIndex = this.getCurrentIndex(links);
          if (currentIndex === null || currentIndex === 0) return null;
          return links[currentIndex - 1];
        }
      };
    }
  });

  // src/main/typescript/document/handlers/docs/sibling-pages-buttons.ts
  var BUTTON_AREA_ID, PREVIOUS_LINK_ID, NEXT_LINK_ID, PREVIOUS_ICON_CLASS, NEXT_ICON_CLASS, SiblingPagesButtons;
  var init_sibling_pages_buttons = __esm({
    "src/main/typescript/document/handlers/docs/sibling-pages-buttons.ts"() {
      "use strict";
      init_document_handler();
      init_page_list_analyzer();
      BUTTON_AREA_ID = "sibling-pages-button-area";
      PREVIOUS_LINK_ID = "previous-page-anchor";
      NEXT_LINK_ID = "next-page-anchor";
      PREVIOUS_ICON_CLASS = "bi bi-arrow-left";
      NEXT_ICON_CLASS = "bi bi-arrow-right";
      SiblingPagesButtons = class extends DocumentHandler {
        async onPostRendering() {
          const buttonArea = document.getElementById(BUTTON_AREA_ID);
          if (!buttonArea) return;
          const analyzer = new PageListAnalyzer();
          const previousLink = analyzer.getPreviousPageLink();
          if (previousLink) {
            buttonArea.appendChild(this.createAnchor(previousLink, PREVIOUS_LINK_ID, PREVIOUS_ICON_CLASS, "start"));
          }
          const nextLink = analyzer.getNextPageLink();
          if (nextLink) {
            buttonArea.appendChild(this.createAnchor(nextLink, NEXT_LINK_ID, NEXT_ICON_CLASS, "end"));
          }
        }
        /**
         * Creates a cloned anchor element with an icon.
         * @param anchor - The anchor element to clone
         * @param anchorId - The ID to assign to the anchor
         * @param iconClass - The Bootstrap icon class
         * @param iconPosition - Where to place the icon ("start" or "end")
         */
        createAnchor(anchor, anchorId, iconClass, iconPosition) {
          const clonedAnchor = anchor.cloneNode(true);
          clonedAnchor.id = anchorId;
          const icon = document.createElement("i");
          icon.className = iconClass;
          if (iconPosition === "start") {
            clonedAnchor.insertBefore(icon, clonedAnchor.firstChild);
          } else {
            clonedAnchor.appendChild(icon);
          }
          return clonedAnchor;
        }
      };
    }
  });

  // src/main/typescript/util/browser.ts
  function isSafari() {
    return /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
  }
  var init_browser = __esm({
    "src/main/typescript/util/browser.ts"() {
      "use strict";
    }
  });

  // src/main/typescript/document/handlers/docs/page-list-autoscroll.ts
  var PAGE_LIST_SELECTOR, CURRENT_PAGE_SELECTOR2, STORAGE_KEY, PageListAutoscroll;
  var init_page_list_autoscroll = __esm({
    "src/main/typescript/document/handlers/docs/page-list-autoscroll.ts"() {
      "use strict";
      init_document_handler();
      init_browser();
      PAGE_LIST_SELECTOR = 'nav[data-role="page-list"]';
      CURRENT_PAGE_SELECTOR2 = "[aria-current]";
      STORAGE_KEY = "qd-page-list-scroll";
      PageListAutoscroll = class extends DocumentHandler {
        async onPostRendering() {
          const pageList = document.querySelector(PAGE_LIST_SELECTOR);
          if (!pageList) return;
          const currentPage = pageList.querySelector(CURRENT_PAGE_SELECTOR2);
          if (!currentPage) return;
          const aside = currentPage.closest("aside");
          if (!aside) return;
          this.restoreScrollPosition(aside);
          if (!isSafari()) {
            this.scrollToCurrentPage(aside, currentPage);
          }
          this.saveScrollPositionOnScroll(aside);
        }
        restoreScrollPosition(aside) {
          const savedScrollTop = sessionStorage.getItem(STORAGE_KEY);
          if (savedScrollTop !== null) {
            aside.scrollTop = parseFloat(savedScrollTop);
          }
        }
        scrollToCurrentPage(aside, currentPage) {
          const asideRect = aside.getBoundingClientRect();
          const currentRect = currentPage.getBoundingClientRect();
          const targetScrollTop = Math.max(0, currentRect.top - asideRect.top + aside.scrollTop - aside.clientHeight / 4);
          aside.scrollTo({
            top: targetScrollTop,
            behavior: "smooth"
          });
        }
        saveScrollPositionOnScroll(aside) {
          aside.addEventListener("scroll", () => {
            sessionStorage.setItem(STORAGE_KEY, aside.scrollTop.toString());
          });
        }
      };
    }
  });

  // src/main/typescript/document/handlers/docs/toc-active-tracking.ts
  var TOC_SELECTOR, TocActiveTracking;
  var init_toc_active_tracking = __esm({
    "src/main/typescript/document/handlers/docs/toc-active-tracking.ts"() {
      "use strict";
      init_document_handler();
      init_active_tracking();
      TOC_SELECTOR = 'aside nav[data-role="table-of-contents"]';
      TocActiveTracking = class extends DocumentHandler {
        async onPostRendering() {
          const toc = document.querySelector(TOC_SELECTOR);
          if (!toc) return;
          initNavigationActiveTracking(toc);
        }
      };
    }
  });

  // src/main/typescript/document/type/docs-document.ts
  var DocsDocument;
  var init_docs_document = __esm({
    "src/main/typescript/document/type/docs-document.ts"() {
      "use strict";
      init_plain_document();
      init_page_margins_docs();
      init_search_field_focus();
      init_search_field();
      init_footnotes_docs();
      init_sibling_pages_buttons();
      init_page_list_autoscroll();
      init_toc_active_tracking();
      DocsDocument = class extends PlainDocument {
        getHandlers() {
          return [
            new SearchFieldFocus(this),
            new SearchField(this),
            new SiblingPagesButtons(this),
            new PageMarginsDocs(this),
            new FootnotesDocs(this),
            new PageListAutoscroll(this),
            new TocActiveTracking(this)
          ];
        }
      };
    }
  });

  // src/main/typescript/index.ts
  var require_index = __commonJS({
    "src/main/typescript/index.ts"() {
      init_capabilities();
      init_execution_queues();
      init_plain_document();
      init_quarkdown_document();
      init_live_preview();
      init_slides_document();
      init_paged_document();
      init_docs_document();
      function isReady() {
        return preRenderingExecutionQueue.isCompleted() && postRenderingExecutionQueue.isCompleted();
      }
      postRenderingExecutionQueue.addOnComplete(() => notifyLivePreview("postRenderingCompleted"));
      var context = window;
      context.isReady = isReady;
      context.quarkdownCapabilities = capabilities;
      context.prepare = prepare;
      context.PlainDocument = PlainDocument;
      context.PagedDocument = PagedDocument;
      context.SlidesDocument = SlidesDocument;
      context.DocsDocument = DocsDocument;
    }
  });
  require_index();
})();
//# sourceMappingURL=quarkdown.js.map
