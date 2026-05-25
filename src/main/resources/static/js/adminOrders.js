// ─────────────────────────────────────────────────────────────────────────────
// adminOrders.js  –  BurgerBanditten admin ordrehåndtering
// Endpoints:
//   GET  /api/orders/pending   → hent ventende ordrer
//   GET  /api/orders/active    → hent aktive (accepterede) ordrer
//   PUT  /api/orders/{id}/accept → accepter en ordre
// ─────────────────────────────────────────────────────────────────────────────

// ── Tab-styring ──────────────────────────────────────────────────────────────

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(p   => p.classList.remove('active'));

    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`panel-${tab}`).classList.add('active');

    if (tab === 'statistics') {
        loadSalesStatistics();
    }
}

// ── Toast ────────────────────────────────────────────────────────────────────

function showToast(message, isError = false) {
    const container = document.getElementById('toast-container');

    const toast = document.createElement('div');
    toast.className = 'toast' + (isError ? ' error' : '');
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ── Dato-formatering ─────────────────────────────────────────────────────────

function formatDateTime(isoString) {
    if (!isoString) return '–';
    return new Date(isoString).toLocaleString('da-DK', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

// ── Render ét ordre-kort ─────────────────────────────────────────────────────

function buildOrderCard(order, isPending) {
    const card = document.createElement('div');
    card.className = 'order-card';
    card.id = `order-${order.id}`;

    const itemRows = (order.orderItems || []).map(item => `
        <li>
            <span>${item.quantity}× ${item.product?.name ?? 'Produkt'}</span>
            <span>${(item.price ?? 0).toFixed(2)} kr.</span>
        </li>
    `).join('');

    const statusClass = isPending ? 'pending' : 'accepted';
    const statusLabel = isPending ? 'Ventende' : 'Accepteret';

    const pickUpTimeIso = order.pickUpTime ?? '';

    const footer = isPending
        ? `<button class="btn-accept" id="btn-${order.id}" onclick="acceptOrder(${order.id})">
               <div class="spinner"></div>
               <span class="btn-label">Accepter</span>
           </button>`
        : `<div style="display:flex;align-items:center;gap:8px;">
               <div class="accepted-badge">
                   <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                       <path d="M2 7l3.5 3.5L12 3.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                   </svg>
                   Accepteret
               </div>
               <button class="btn-secondary" onclick="openEditModal(${order.id}, '${pickUpTimeIso}')">Rediger</button>
           </div>`;

    card.innerHTML = `
        <div class="card-header">
            <div class="order-id">Ordre <span>#${order.id}</span></div>
            <span class="status-pill ${statusClass}">${statusLabel}</span>
        </div>
        <div class="card-meta">
            <div><strong>${order.user?.name ?? 'Ukendt'}</strong></div>
            <div>${order.user?.mail ?? ''}</div>
            <div>Afhentning: <strong>${formatDateTime(order.pickUpTime)}</strong></div>
            ${order.comment ? `<div>Kommentar: <em>${order.comment}</em></div>` : ''}
        </div>
        <ul class="order-items">${itemRows || '<li><span>Ingen varer</span></li>'}</ul>
        <div class="divider-line"></div>
        <div class="card-footer">
            <div>
                <span class="total-label">Total</span>
                <span class="total">${(order.price ?? 0).toFixed(2)} kr.</span>
            </div>
            ${footer}
        </div>
    `;

    return card;
}

// ── Render grid ──────────────────────────────────────────────────────────────

function renderGrid(orders, gridId, isPending) {
    const grid = document.getElementById(gridId);
    grid.innerHTML = '';

    if (!orders || orders.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">${isPending ? '🕐' : '✓'}</div>
                <p>${isPending ? 'Ingen ventende ordrer' : 'Ingen aktive ordrer'}</p>
            </div>`;
        return;
    }

    orders.forEach((order, i) => {
        const card = buildOrderCard(order, isPending);
        card.style.animationDelay = `${i * 0.05}s`;
        grid.appendChild(card);
    });
}

// ── Hent ventende ordrer ─────────────────────────────────────────────────────

async function loadPendingOrders() {
    try {
        const response = await fetch('/api/orders/pending');

        if (!response.ok) {
            throw new Error('Kunne ikke hente ventende ordrer');
        }

        const orders = await response.json();

        renderGrid(orders, 'pending-grid', true);
        document.getElementById('pending-count').textContent = orders.length;

    } catch (error) {
        console.error(error);
        document.getElementById('pending-grid').innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">⚠️</div>
                <p>Kunne ikke hente ordrer – prøv igen</p>
            </div>`;
    }
}

// ── Hent aktive ordrer ───────────────────────────────────────────────────────

async function loadActiveOrders() {
    try {
        const response = await fetch('/api/orders/active');

        if (!response.ok) {
            throw new Error('Kunne ikke hente aktive ordrer');
        }

        const orders = await response.json();

        renderGrid(orders, 'active-grid', false);
        document.getElementById('active-count').textContent = orders.length;

    } catch (error) {
        console.error(error);
        document.getElementById('active-grid').innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">⚠️</div>
                <p>Kunne ikke hente aktive ordrer – prøv igen</p>
            </div>`;
    }
}

// ── Accepter ordre ───────────────────────────────────────────────────────────

async function acceptOrder(orderId) {
    const btn = document.getElementById(`btn-${orderId}`);

    // Loading state
    btn.classList.add('loading');

    try {
        const response = await fetch(`/api/orders/${orderId}/accept`, {
            method: 'PUT'
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Noget gik galt');
        }

        // Opdater badge-tæller
        const pendingBadge = document.getElementById('pending-count');
        const activeBadge  = document.getElementById('active-count');
        pendingBadge.textContent = Math.max(0, parseInt(pendingBadge.textContent) - 1);
        activeBadge.textContent  = parseInt(activeBadge.textContent) + 1;

        // Fjern kortet fra ventende med animation
        const card = document.getElementById(`order-${orderId}`);
        card.style.transition = 'opacity 0.3s, transform 0.3s';
        card.style.opacity = '0';
        card.style.transform = 'scale(0.96)';

        setTimeout(() => {
            card.remove();

            // Vis tomt state hvis ingen ventende tilbage
            const grid = document.getElementById('pending-grid');
            if (grid.children.length === 0) {
                grid.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon">🕐</div>
                        <p>Ingen ventende ordrer</p>
                    </div>`;
            }

            // Genindlæs aktive ordrer så kortet dukker op der
            loadActiveOrders();
        }, 300);

        showToast(`✓ Ordre #${orderId} accepteret – kunden er notificeret`);

    } catch (error) {
        console.error(error);
        showToast(error.message, true);
        btn.classList.remove('loading');
    }
}

// ── Init ─────────────────────────────────────────────────────────────────────

function formatCurrency(amount) {
    return new Intl.NumberFormat('da-DK', {
        style: 'currency',
        currency: 'DKK'
    }).format(amount ?? 0);
}

function renderSalesStatistics(statistics) {
    document.getElementById('total-revenue').textContent = formatCurrency(statistics.totalRevenue);

    const tableBody = document.getElementById('product-sales-body');
    tableBody.innerHTML = '';

    if (!statistics.productSales || statistics.productSales.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 2;
        cell.textContent = 'Ingen produkter fundet';
        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
    }

    statistics.productSales.forEach(product => {
        const row = document.createElement('tr');
        const productCell = document.createElement('td');
        const quantityCell = document.createElement('td');

        productCell.textContent = product.productName;
        quantityCell.textContent = product.quantitySold;
        quantityCell.className = 'quantity-cell';

        row.append(productCell, quantityCell);
        tableBody.appendChild(row);
    });
}

async function loadSalesStatistics() {
    const from = document.getElementById('statistics-from').value;
    const to = document.getElementById('statistics-to').value;
    const params = new URLSearchParams();

    if (from) params.set('from', from);
    if (to) params.set('to', to);

    try {
        const response = await fetch(`/api/orders/statistics?${params.toString()}`);

        if (!response.ok) {
            throw new Error('Kunne ikke hente salgsstatistik');
        }

        renderSalesStatistics(await response.json());
    } catch (error) {
        console.error(error);
        document.getElementById('product-sales-body').innerHTML = `
            <tr>
                <td colspan="2">Kunne ikke hente statistik - prov igen</td>
            </tr>`;
    }
}

function clearStatisticsFilter() {
    document.getElementById('statistics-from').value = '';
    document.getElementById('statistics-to').value = '';
    loadSalesStatistics();
}

// ── Rediger afhentingstidspunkt ───────────────────────────────────────────────

let _editOrderId = null;

function openEditModal(orderId, currentPickUpTime) {
    _editOrderId = orderId;
    const input = document.getElementById('edit-pickup-input');
    // datetime-local input expects "YYYY-MM-DDTHH:MM"
    input.value = currentPickUpTime ? currentPickUpTime.substring(0, 16) : '';
    document.getElementById('edit-order-modal').classList.add('open');
}

function closeEditModal(event) {
    if (event && event.target !== document.getElementById('edit-order-modal')) return;
    document.getElementById('edit-order-modal').classList.remove('open');
    _editOrderId = null;
}

async function savePickUpTime() {
    const input = document.getElementById('edit-pickup-input');
    if (!input.value) {
        showToast('Vælg et afhentingstidspunkt', true);
        return;
    }

    // Backend expects full ISO string e.g. "2024-01-15T14:30:00"
    const pickUpTime = input.value.length === 16 ? input.value + ':00' : input.value;

    try {
        const response = await fetch(`/api/orders/${_editOrderId}/update`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ pickUpTime })
        });

        if (!response.ok) {
            const msg = await response.text();
            throw new Error(msg || 'Noget gik galt');
        }

        document.getElementById('edit-order-modal').classList.remove('open');
        _editOrderId = null;
        showToast('Afhentingstidspunkt opdateret');
        loadActiveOrders();
    } catch (error) {
        showToast(error.message, true);
    }
}

loadPendingOrders();
loadActiveOrders();
loadSalesStatistics();
