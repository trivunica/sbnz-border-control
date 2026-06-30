import { Component, Input, ElementRef, AfterViewInit, OnChanges } from '@angular/core';
import * as d3 from 'd3';
import { DecisionNode } from '../../core/models';
import { renderDecisionTree } from '../../core/utils/decision-tree-renderer';

@Component({
    selector: 'app-decision-tree',
    template: ` <svg #treesvg></svg>`,
    standalone: true,
    styles: [
        `
            svg {
                width: 100%;
                overflow: visible;
            }
        `,
    ],
})
export class DecisionTreeComponent implements AfterViewInit, OnChanges {
    @Input() root!: DecisionNode;

    constructor(private el: ElementRef) {}

    ngAfterViewInit() {
        this.render();
    }
    ngOnChanges() {
        this.render();
    }

    private render() {
        const svg = d3.select(this.el.nativeElement.querySelector('svg'));
        svg.selectAll('*').remove();
        if (!this.root) return;
        renderDecisionTree(svg, this.root);
    }
}