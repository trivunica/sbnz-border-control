import * as d3 from 'd3';
import { DecisionNode } from '../models';

const NODE_BORDER: Record<string, string> = {
    DECISION:    '#94a3b8',
    ENTITY:      '#3b82f6',
    RISK_FACTOR: '#ef4444',
    EVIDENCE:    '#22c55e',
};

export function renderDecisionTree(svg: d3.Selection<any, any, any, any>, rootData: DecisionNode): void {
    const margin = { top: 10, right: 10, bottom: 10, left: 10 };
    const width = 750;

    const root = d3.hierarchy<DecisionNode>(rootData, d => d.children);

    const orderedNodes: d3.HierarchyNode<DecisionNode>[] = [];
    function traverse(node: d3.HierarchyNode<DecisionNode>) {
        orderedNodes.push(node);
        if (node.children) {
            node.children.forEach(child => traverse(child));
        }
    }
    traverse(root);

    const rowHeight = 45;
    const indentWidth = 25;
    const height = (orderedNodes.length * rowHeight) + margin.top + margin.bottom;

    svg
        .attr('viewBox', `0 0 ${width} ${height}`)
        .attr('width', '100%')
        .attr('height', '100%')
        .style('overflow', 'hidden');

    const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`);

    orderedNodes.forEach((node, index) => {
        node.x = node.depth * indentWidth;
        node.y = index * rowHeight;
    });

    g.append('g')
        .attr('fill', 'none')
        .attr('stroke', '#cbd5e1')
        .attr('stroke-width', 1.5)
        .selectAll('path')
        .data(orderedNodes.filter(d => d.parent))
        .join('path')
        .attr('d', d => {
            // @ts-ignore
            const parentY = d.parent!.y + (rowHeight / 2);
            // @ts-ignore
            const currentY = d.y + (rowHeight / 2);
            // @ts-ignore
            const parentX = d.parent!.x + 10;
            const currentX = d.x;

            return `M${parentX},${parentY} V${currentY} H${currentX}`;
        });

    const nodeContainers = g.append('g')
        .selectAll('g')
        .data(orderedNodes)
        .join('g')
        .attr('transform', d => `translate(${d.x!},${d.y!})`);

    const rectHeight = 34;

    nodeContainers.append('foreignObject')
        .attr('x', 5)
        .attr('y', (rowHeight - rectHeight) / 2)
        .attr('width', d => width - d.x! - 30)
        .attr('height', rectHeight)
        .append('xhtml:div')
        .attr('style', d => `
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: transparent;
        border: 1.5px solid ${NODE_BORDER[d.data.type] ?? '#64748b'};
        border-radius: 6px;
        color: rgba(255,255,255,0.88);
        font-family: system-ui, -apple-system, sans-serif;
        padding: 0 12px;
        height: 100%;
        box-sizing: border-box;
    `)
        .html(d => {
            const label = d.data.label || '';
            const detail = d.data.detail && d.data.type === 'RISK_FACTOR'
                ? `<span style="font-size:10px; opacity:0.6; margin-left:6px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${d.data.detail}</span>`
                : '';
            const scoreHtml = d.data.score > 0
                ? `<span style="font-weight:700; font-size:11px; margin-left:10px; white-space:nowrap; opacity:0.8;">+${d.data.score}</span>`
                : '';

            return `
            <span style="font-weight:600; font-size:11px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; flex:1;">
                ${label}
            </span>
            ${detail}
            ${scoreHtml}
        `;
        });
}
