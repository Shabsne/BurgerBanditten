/* ═══════════════════════════════════════════════
   orderHistory.js — bruges af menu.html + admin
   ═══════════════════════════════════════════════ */

// ── Kunde: Mine ordrer ────────────────────────────
async function toggleCustomerOrders() {
    const overlay = document.getElementById('customer-orders-overlay');
    const isOpen  = overlay.classList.contains('open');

    if (isOpen) {
        overlay.classList.remove('open');
        return;
    }

    overlay.classList.add('open');
    const list = document.getElementById('customer-orders-list');
    list.innerHTML = '<div class="history-empty"><div class="empty-icon">⏳</div><p>Henter dine ordrer…</p></div>';

    try {
        const res = await fetch('/api/orders/my-orders', { credentials: 'include' });

        if (res.status === 401) {
            list.innerHTML = '<div class="history-empty"><p>Du skal være logget ind for at se dine ordrer.</p></div>';
            return;
        }

        const orders = await res.json();

        if (!orders.length) {
            list.innerHTML = '<div class="history-empty"><div class="empty-icon">🍔</div><p>Du har ingen ordrer endnu.</p></div>';
            return;
        }

        list.innerHTML = orders.map(buildCustomerCard).join('');

    } catch (err) {
        list.innerHTML = '<div class="history-empty"><p>Kunne ikke hente ordrer.</p></div>';
    }
}

function buildCustomerCard(order) {
    const items = (order.orderItems || [])
        .map(i => `<li><span>${i.quantity}× ${i.product?.name ?? 'Vare'}</span><span>${(i.price ?? 0).toFixed(0)} kr.</span></li>`)
        .join('');

    const statusClass = {
        PENDING:   'status-pill pending',
        ACTIVE:    'status-pill accepted',
        COMPLETED: 'status-pill completed',
        CANCELLED: 'status-pill cancelled',
    }[order.orderStatus] ?? 'status-pill';

    const statusLabel = {
        PENDING:   'Ventende',
        ACTIVE:    'Under tilberedning',
        COMPLETED: 'Afhentet',
        CANCELLED: 'Annulleret',
    }[order.orderStatus] ?? order.orderStatus;

    return `
    <div class="order-card history-card">
        <div class="card-header">
            <div class="order-id">Ordre <span>#${order.id}</span></div>
            <span class="${statusClass}">${statusLabel}</span>
        </div>
        <div class="card-meta">
            <span>${formatHistoryDate(order.createdAt)}</span>
            ${order.pickUpTime ? `<span>Afhentning: <strong>${formatHistoryDate(order.pickUpTime)}</strong></span>` : ''}
        </div>
        <ul class="order-items-list">${items}</ul>
        <div class="divider-line"></div>
        <div class="card-footer">
            <div>
                <span class="total-label">Total</span>
                <span class="total-amount">${(order.price ?? 0).toFixed(0)} kr.</span>
            </div>
        </div>
    </div>`;
}

// ── Admin: Historik-panel ─────────────────────────
async function loadAdminHistory(statusFilter = 'all') {
    const grid = document.getElementById('history-grid');
    if (!grid) return;

    grid.innerHTML = `
        <div class="skeleton"><div class="skel-line wide"></div><div class="skel-line full"></div></div>
        <div class="skeleton"><div class="skel-line wide"></div><div class="skel-line full"></div></div>`;

    const url = statusFilter === 'all'
        ? '/api/orders/history'
        : `/api/orders/history?status=${statusFilter}`;

    try {
        const res    = await fetch(url, { credentials: 'include' });
        const orders = await res.json();

        grid.innerHTML = '';

        if (!orders.length) {
            grid.innerHTML = `<div class="empty-state"><div class="empty-icon">📋</div><p>Ingen ordrer i denne kategori.</p></div>`;
            return;
        }

        orders.forEach(o => grid.appendChild(buildAdminHistoryCard(o)));

    } catch (err) {
        grid.innerHTML = `<div class="empty-state"><p>Fejl ved hentning af historik.</p></div>`;
    }
}

function buildAdminHistoryCard(order) {
    const card = document.createElement('div');
    const isCompleted = order.orderStatus === 'COMPLETED';

    card.className = `order-card ${isCompleted ? 'history-completed' : 'history-cancelled'}`;

    const items = (order.orderItems || [])
        .map(i => `<li><span>${i.quantity}× ${i.product?.name ?? 'Vare'}</span><span>${(i.price ?? 0).toFixed(0)} kr.</span></li>`)
        .join('') || '<li><span>Ingen varer</span></li>';

    card.innerHTML = `
        <div class="card-header">
            <div>
                <div class="order-id">Ordre <span>#${order.id}</span></div>
                <div class="card-meta">
                    <span>${order.user?.name ?? 'Gæst'}</span>
                    <span>${order.user?.mail ?? ''}</span>
                    <span>${formatHistoryDate(order.createdAt)}</span>
                </div>
            </div>
            <span class="status-pill ${isCompleted ? 'completed' : 'cancelled'}">
                ${isCompleted ? '✓ Afhentet' : '✕ Annulleret'}
            </span>
        </div>
        <ul class="order-items-list">${items}</ul>
        <div class="divider-line"></div>
        <div class="card-footer">
            <div>
                <span class="total-label">Total</span>
                <span class="total-amount">${(order.price ?? 0).toFixed(0)} kr.</span>
            </div>
        </div>`;
    return card;
}

// ── Hjælpefunktion ────────────────────────────────
function formatHistoryDate(dt) {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('da-DK', {
        day: '2-digit', month: 'short',
        hour: '2-digit', minute: '2-digit'
    });
}