import {describe, expect, it} from 'vitest';
import {getPageSize, isPagedLike} from '../paged-like-quarkdown-document';

const rect = (width: number, height: number) => ({width, height} as DOMRect);

const pagedDocument = (...pages: DOMRect[]) => ({
    getPages: () => pages.map(r => ({getBoundingClientRect: () => r, querySelectorAll: () => [] as any})),
} as any);

const plainDocument = () => ({getParentViewport: () => undefined} as any);

describe('isPagedLike', () => {
    it('recognizes documents exposing pages', () => {
        expect(isPagedLike(pagedDocument())).toBe(true);
        expect(isPagedLike(plainDocument())).toBe(false);
    });
});

describe('getPageSize', () => {
    it('measures the first page', () => {
        expect(getPageSize(pagedDocument(rect(794, 1123), rect(1, 1)))).toEqual({width: 794, height: 1123});
    });

    it('is undefined without pages', () => {
        expect(getPageSize(pagedDocument())).toBeUndefined();
    });

    it('is undefined for non-paged documents', () => {
        expect(getPageSize(plainDocument())).toBeUndefined();
    });
});
