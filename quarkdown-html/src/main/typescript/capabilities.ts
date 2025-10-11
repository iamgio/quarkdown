/**
 * This configuration defines the document handlers to enable,
 * and it's affected by the HTML wrapper based on the included features.
 * For instance, if math equations are not used, the math handler is not included.
 *
 * @see getGlobalHandlers
 * @see QuarkdownDocument.getHandlers
 */
export const capabilities = {
    /**
     * Whether to include the math document handler for rendering mathematical formulas.
     * @see MathRenderer
     */
    math: false,
}