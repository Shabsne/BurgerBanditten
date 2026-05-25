// admin.js — al admin-logik. Ingen styling her, kun funktioner.

// ── Fetch wrapper ────────────────────────────────
function api(url, opts = {}) {
    return fetch(url, {
        ...opts,
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) }
    });
}

function showToast(msg, isError = false) {
    const t = document.createElement('div');
    t.className = 'toast' + (isError ? ' error' : '');
    t.textContent = msg;
    document.getElementById('toast-container').appendChild(t);
    setTimeout(() => { t.style.opacity = '0'; t.style.transition = 'opacity 0.3s'; setTimeout(() => t.remove(), 300); }, 3000);
}

function formatDT(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('da-DK', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
}

// ── Navigation ───────────────────────────────────
function showScreen(id, btn) {
    document.querySelectorAll('.admin-screen').forEach(s => s.classList.remove('visible'));
    document.querySelectorAll('.nav-tab').forEach(b => b.classList.remove('active'));
    document.getElementById('screen-' + id).classList.add('visible');
    if (btn) btn.classList.add('active');
    if (id === 'catalog'     && !allProducts.length)     loadCatalog();
    if (id === 'statistics') loadSalesStatistics();
    if (id === 'ingredients' && !ingredientsLoaded)      loadIngredientList();
    if (id === 'hours') { loadWeeklySchedule(); loadHolidays(); }
}

// ── Opening status ───────────────────────────────
async function checkOpeningStatus() {
    try {
        const data = await api('/opening-hours/next').then(r => r.json());
        document.getElementById('status-dot').className = 'status-dot ' + (data.openNow ? 'open' : 'closed');
        document.getElementById('status-text').textContent = data.openNow ? 'Åben' : 'Lukket';
    } catch (e) {}
}

// ── Bestillings-switch ───────────────────────────
async function loadOrderingStatus() {
    try {
        const data = await api('/api/orders/status').then(r => r.json());
        updateToggleBtn(data.open);
    } catch (e) {
        console.error('Kunne ikke hente bestillingsstatus', e);
    }
}

async function toggleOrdering() {
    try {
        const data = await api('/api/orders/admin/toggle', { method: 'POST' }).then(r => r.json());
        updateToggleBtn(data.open);
        showToast(data.message);
    } catch (e) {
        showToast('Kunne ikke skifte bestillingsstatus', true);
    }
}

function updateToggleBtn(isOpen) {
    const btn = document.getElementById('ordering-toggle');
    if (!btn) return;
    btn.textContent      = isOpen ? '🟢 Bestillinger åbne' : '🔴 Bestillinger lukket';
    btn.style.background = isOpen ? 'var(--success, #4CAF7D)' : 'var(--error, #FF5C5C)';
    btn.style.color      = 'white';
    btn.style.border     = 'none';
}

// ── Order tabs ───────────────────────────────────
function switchOrderTab(tab) {
    document.querySelectorAll('.sub-tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.order-panel').forEach(p => p.classList.remove('visible'));
    document.getElementById('subtab-' + tab).classList.add('active');
    document.getElementById('panel-' + tab).classList.add('visible');

    if (tab === 'history') loadAdminHistory('all');
}

// ── Build order card ─────────────────────────────
function buildOrderCard(order, isPending) {
    const card = document.createElement('div');
    card.className = 'order-card ' + (isPending ? 'new-order' : 'active-order');
    card.id = 'order-' + order.id;

    const items = (order.orderItems || [])
        .map(i => `<li><span>${i.quantity}× ${i.product?.name ?? 'Produkt'}</span><span>${(i.price ?? 0).toFixed(0)} kr.</span></li>`)
        .join('') || '<li><span>Ingen varer</span></li>';

    const statusBadge = isPending
        ? `<span class="status-pill pending">Ventende</span>`
        : `<span class="status-pill accepted">Accepteret</span>`;

    const footer = isPending
        ? `<button class="btn-accept" id="btn-${order.id}" onclick="acceptOrder(${order.id})">✓ Accepter</button>`
        : `<div style="display:flex; gap:0.5rem; align-items:center;">
           <button class="btn-sm btn-success" onclick="completeOrder(${order.id})">✓ Fuldfør</button>
           <button class="btn-sm" onclick="openEditOrderModal(${order.id})">✏️ Rediger</button>
       </div>`;

    card.innerHTML = `
        <div class="card-header">
            <div>
                <div class="order-id">Ordre <span>#${order.id}</span></div>
                <div class="card-meta">
                    <span>${order.user?.name ?? 'Gæst'}</span>
                    <span>${order.user?.mail ?? ''}</span>
                    <span>Afhentning: <strong>${formatDT(order.pickUpTime)}</strong></span>
                    ${order.comment ? `<span><em>${order.comment}</em></span>` : ''}
                </div>
            </div>
            <div>${statusBadge}</div>
        </div>
        <ul class="order-items-list">${items}</ul>
        <div class="divider-line"></div>
        <div class="card-footer">
            <div>
                <span class="total-label">Total</span>
                <span class="total-amount">${(order.price ?? 0).toFixed(0)} kr.</span>
            </div>
            ${footer}
        </div>`;
    return card;
}

function renderOrderGrid(orders, gridId, isPending) {
    const grid = document.getElementById(gridId);
    grid.innerHTML = '';
    if (!orders?.length) {
        grid.innerHTML = `<div class="empty-state"><div class="empty-icon">${isPending ? '🕐' : '✓'}</div>
            <p>${isPending ? 'Ingen ventende ordrer' : 'Ingen aktive ordrer'}</p></div>`;
        return;
    }
    orders.forEach((o, i) => {
        const c = buildOrderCard(o, isPending);
        c.style.animationDelay = i * 0.04 + 's';
        grid.appendChild(c);
    });
}

async function completeOrder(orderId) {
    if (!confirm(`Marker ordre #${orderId} som fuldført?`)) return;
    try {
        const res = await fetch(`/api/orders/${orderId}/complete`, {
            method: 'PUT',
            credentials: 'include'
        });
        if (res.ok) {
            // Fjern kortet fra aktive
            document.getElementById('order-' + orderId)?.remove();

            // Opdater badge-tæller
            const badge = document.getElementById('active-count');
            if (badge) {
                const current = parseInt(badge.textContent) || 0;
                badge.textContent = Math.max(0, current - 1);
            }

            showToast(`Ordre #${orderId} fuldført ✓`);
        } else {
            const msg = await res.text();
            alert('Fejl: ' + msg);
        }
    } catch (e) {
        alert('Netværksfejl');
    }
}

async function loadPendingOrders() {
    try {
        const res = await api('/api/orders/pending');
        if (res.status === 403) { showToast('Ikke autoriseret som admin', true); return; }
        const orders = await res.json();
        renderOrderGrid(orders, 'pending-grid', true);
        document.getElementById('pending-count').textContent = orders.length;
    } catch (e) {
        document.getElementById('pending-grid').innerHTML =
            `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Kunne ikke hente ordrer</p></div>`;
    }
}

async function loadActiveOrders() {
    try {
        const orders = await api('/api/orders/active').then(r => r.json());
        renderOrderGrid(orders, 'active-grid', false);
        document.getElementById('active-count').textContent = orders.length;
    } catch (e) {}
}

async function acceptOrder(orderId) {
    const btn = document.getElementById('btn-' + orderId);
    if (btn) { btn.classList.add('loading'); btn.textContent = '...'; }
    try {
        const res = await api(`/api/orders/${orderId}/accept`, { method: 'PUT' });
        if (!res.ok) throw new Error(await res.text());

        const pendingBadge = document.getElementById('pending-count');
        const activeBadge  = document.getElementById('active-count');
        pendingBadge.textContent = Math.max(0, parseInt(pendingBadge.textContent) - 1);
        activeBadge.textContent  = parseInt(activeBadge.textContent) + 1;

        const card = document.getElementById('order-' + orderId);
        card.style.transition = 'opacity 0.3s, transform 0.3s';
        card.style.opacity = '0';
        card.style.transform = 'scale(0.96)';
        setTimeout(() => { card.remove(); loadActiveOrders(); }, 300);
        showToast(`✓ Ordre #${orderId} accepteret`);
    } catch (e) {
        showToast(e.message || 'Fejl ved accept', true);
        if (btn) { btn.classList.remove('loading'); btn.textContent = '✓ Accepter'; }
    }
}

async function rejectOrder(orderId) {
    if (!confirm(`Afvis ordre #${orderId}?`)) return;
    const btn = document.getElementById('btn-reject-' + orderId);
    if (btn) { btn.classList.add('loading'); btn.textContent = '...'; }
    try {
        const res = await api(`/api/orders/${orderId}/reject`, { method: 'PUT' });
        if (!res.ok) throw new Error(await res.text());

        const pendingBadge = document.getElementById('pending-count');
        pendingBadge.textContent = Math.max(0, parseInt(pendingBadge.textContent) - 1);

        const card = document.getElementById('order-' + orderId);
        card.style.transition = 'opacity 0.3s, transform 0.3s';
        card.style.opacity = '0';
        card.style.transform = 'scale(0.96)';
        setTimeout(() => card.remove(), 300);
        showToast(`✕ Ordre #${orderId} afvist`);
    } catch (e) {
        showToast(e.message || 'Fejl ved afvisning', true);
        if (btn) { btn.classList.remove('loading'); btn.textContent = '✕ Afvis'; }
    }
}

// ── Rediger ordre modal ──────────────────────────
let editingOrderId = null;

async function openEditOrderModal(orderId) {
    editingOrderId = orderId;
    try {
        const res = await api(`/api/orders/${orderId}`);
        if (!res.ok) throw new Error('Kunne ikke hente ordre');
        const order = await res.json();

        document.getElementById('edit-order-id').textContent   = '#' + orderId;
        document.getElementById('edit-comment').value          = order.comment ?? '';
        document.getElementById('edit-pickup-time').value      = order.pickUpTime
            ? order.pickUpTime.substring(0, 16)
            : '';

        const itemsContainer = document.getElementById('edit-order-items');
        itemsContainer.innerHTML = (order.orderItems || []).map(item => `
            <div class="edit-item-row" data-product-id="${item.product?.id}">
                <span>${item.product?.name ?? 'Produkt'}</span>
                <div style="display:flex; align-items:center; gap:0.5rem;">
                    <label>Antal:</label>
                    <input type="number"
                           class="edit-item-qty"
                           value="${item.quantity}"
                           min="1"
                           style="width:60px; padding:0.25rem; background:var(--input-bg); border:1px solid var(--border); border-radius:4px; color:var(--text);">
                </div>
            </div>
        `).join('');

        document.getElementById('edit-order-modal-overlay').classList.add('open');
    } catch (e) {
        showToast('Kunne ikke hente ordredetaljer', true);
    }
}

function closeEditOrderModal() {
    document.getElementById('edit-order-modal-overlay').classList.remove('open');
    editingOrderId = null;
}

// ── Catalog ──────────────────────────────────────
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
        tableBody.innerHTML = '<tr><td colspan="3">Ingen salg i perioden</td></tr>';
        return;
    }

    statistics.productSales.forEach((product, index) => {
        const row = document.createElement('tr');
        const rankCell = document.createElement('td');
        const productCell = document.createElement('td');
        const quantityCell = document.createElement('td');

        rankCell.textContent = `#${index + 1}`;
        productCell.textContent = product.productName;
        quantityCell.textContent = product.quantitySold;
        quantityCell.className = 'quantity-cell';

        row.append(rankCell, productCell, quantityCell);
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
        const res = await api(`/api/orders/statistics?${params.toString()}`);
        if (!res.ok) throw new Error('Kunne ikke hente salgsstatistik');
        renderSalesStatistics(await res.json());
    } catch (e) {
        document.getElementById('product-sales-body').innerHTML =
            '<tr><td colspan="3">Kunne ikke hente statistik - prov igen</td></tr>';
        showToast(e.message || 'Kunne ikke hente statistik', true);
    }
}

function clearStatisticsFilter() {
    document.getElementById('statistics-from').value = '';
    document.getElementById('statistics-to').value = '';
    loadSalesStatistics();
}

let allProducts = [], allIngredients = [], activeFilter = 'ALL', editingId = null;

async function loadCatalog() {
    try {
        allProducts = await api('/api/products/menu').then(r => r.json());
        renderCatalog();
    } catch (e) {
        document.getElementById('catalog-list').innerHTML =
            '<div class="empty-state"><p>Kunne ikke hente produkter</p></div>';
    }
}

async function loadIngredients() {
    try { allIngredients = await api('/api/products/ingredients').then(r => r.json()); } catch (e) {}
}

function filterCatalog(cat, btn) {
    activeFilter = cat;
    document.querySelectorAll('.cat-filter').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    renderCatalog();
}

function renderCatalog() {
    const list     = document.getElementById('catalog-list');
    const filtered = activeFilter === 'ALL' ? allProducts : allProducts.filter(p => p.category === activeFilter);
    if (!filtered.length) {
        list.innerHTML = '<div class="empty-state"><p>Ingen produkter</p></div>';
        return;
    }
    list.innerHTML = filtered.map(p => `
        <div class="catalog-row">
            <div class="product-thumb">${p.image ? `<img src="${p.image}" alt="${p.name}">` : '🍔'}</div>
            <div>
                <div class="product-name">${p.name}</div>
                <div class="product-desc">${p.description ?? ''}</div>
            </div>
            <div><span class="cat-pill">${p.category}</span></div>
            <div>${p.price} kr.</div>
            <div class="row-actions">
                <button class="btn-sm" onclick="openProductModal(${p.id})">Rediger</button>
                <button class="btn-sm btn-danger" onclick="deleteProduct(${p.id})">✕</button>
            </div>
        </div>`).join('');
}

async function deleteProduct(id) {
    if (!confirm('Slet dette produkt?')) return;
    try {
        await api(`/api/products/admin/product/delete/${id}`, { method: 'DELETE' });
        allProducts = allProducts.filter(p => p.id !== id);
        renderCatalog();
        showToast('Produkt slettet');
    } catch (e) { showToast('Kunne ikke slette', true); }
}

// ── Product modal ────────────────────────────────
async function openProductModal(productId) {
    editingId = productId;
    document.getElementById('modal-title-text').textContent = productId ? 'Rediger produkt' : 'Nyt produkt';
    ['p-name', 'p-description', 'p-price'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('p-category').value   = 'BURGER';
    document.getElementById('p-lunchOffer').value = 'false';
    document.getElementById('img-preview').classList.remove('shown');
    document.getElementById('img-placeholder').style.display = 'block';
    document.getElementById('p-image').value = '';

    if (!allIngredients.length) await loadIngredients();
    renderIngredientGrid([]);

    if (productId) {
        try {
            const p = await api(`/api/products/product/${productId}`).then(r => r.json());
            document.getElementById('p-name').value        = p.name ?? '';
            document.getElementById('p-description').value = p.description ?? '';
            document.getElementById('p-price').value       = p.price ?? '';
            document.getElementById('p-category').value    = p.category ?? 'BURGER';
            document.getElementById('p-lunchOffer').value  = p.lunchOffer ? 'true' : 'false';
            renderIngredientGrid(p.ingredients ?? []);
            if (p.image) {
                const img = document.getElementById('img-preview');
                img.src = p.image;
                img.classList.add('shown');
                document.getElementById('img-placeholder').style.display = 'none';
            }
        } catch (e) { showToast('Kunne ikke hente produkt', true); }
    }
    document.getElementById('product-modal-overlay').classList.add('open');
}

function renderIngredientGrid(selectedNames) {
    const g = document.getElementById('ingredients-grid');
    g.innerHTML = !allIngredients.length
        ? '<span>Ingen ingredienser tilgængelige</span>'
        : allIngredients.map(ing => `
            <label class="ing-chip">
                <input type="checkbox" name="ing" value="${ing.id}" ${selectedNames.includes(ing.name) ? 'checked' : ''}>
                ${ing.name}
            </label>`).join('');
}

function closeProductModal() {
    document.getElementById('product-modal-overlay').classList.remove('open');
    editingId = null;
}

function previewImage(input) {
    const file = input.files[0];
    if (!file) return;
    const r = new FileReader();
    r.onloadend = () => {
        const img = document.getElementById('img-preview');
        img.src = r.result;
        img.classList.add('shown');
        document.getElementById('img-placeholder').style.display = 'none';
    };
    r.readAsDataURL(file);
}

async function saveProduct() {
    const name        = document.getElementById('p-name').value.trim();
    const description = document.getElementById('p-description').value.trim();
    const price       = parseFloat(document.getElementById('p-price').value);
    const category    = document.getElementById('p-category').value;
    const lunchOffer  = document.getElementById('p-lunchOffer').value === 'true';
    const ingredients = [...document.querySelectorAll('input[name="ing"]:checked')].map(c => parseInt(c.value));

    if (!name || isNaN(price)) { showToast('Udfyld navn og pris', true); return; }

    let image = null;
    const fileInput = document.getElementById('p-image');
    if (fileInput.files[0]) {
        image = await new Promise(res => {
            const r = new FileReader(); r.onloadend = () => res(r.result); r.readAsDataURL(fileInput.files[0]);
        });
    } else if (editingId) {
        const img = document.getElementById('img-preview');
        if (img.classList.contains('shown')) image = img.src;
    }

    const url    = editingId ? `/api/products/admin/product/update/${editingId}` : '/api/products/admin/product/create';
    const method = editingId ? 'PUT' : 'POST';

    try {
        const res = await api(url, { method, body: JSON.stringify({ name, description, price, category, lunchOffer, ingredients, image }) });
        if (!res.ok) throw new Error('Status ' + res.status);
        showToast(editingId ? 'Produkt opdateret ✓' : 'Produkt oprettet ✓');
        closeProductModal();
        allProducts = [];
        loadCatalog();
    } catch (e) { showToast('Fejl: ' + e.message, true); }
}

// ── Ingredients ──────────────────────────────────
let ingredientsLoaded = false;

async function loadIngredientList() {
    try {
        const ingredients = await api('/api/ingredients').then(r => r.json());
        renderIngredientList(ingredients);
        ingredientsLoaded = true;
    } catch (e) {
        document.getElementById('ingredient-list').innerHTML =
            '<div class="empty-state"><p>Kunne ikke hente ingredienser</p></div>';
    }
}

function renderIngredientList(ingredients) {
    const list = document.getElementById('ingredient-list');
    if (!ingredients.length) {
        list.innerHTML = '<div class="empty-state"><div class="empty-icon">🥬</div><p>Ingen ingredienser endnu</p></div>';
        return;
    }
    list.innerHTML = ingredients.map(ing => `
        <div class="ingredient-row" id="ing-row-${ing.id}">
            <div>${ing.name}${ing.addOn ? ' <span class="cat-pill">tilkøb</span>' : ''}</div>
            <div>${ing.price} kr.</div>
            <div>${ing.inventory}</div>
            <div>
                <button class="btn-sm" onclick="openEditIngredient(${ing.id}, '${ing.name}', ${ing.price}, ${ing.inventory}, ${ing.addOn})">Rediger</button>
                <button class="btn-sm btn-danger" onclick="deleteIngredient(${ing.id})">✕</button>
               </div>
            </div>`).join('');

}

async function createIngredient() {
    const name      = document.getElementById('ing-name').value.trim();
    const price     = parseFloat(document.getElementById('ing-price').value) || 0;
    const inventory = parseInt(document.getElementById('ing-inventory').value)  || 100;
    const addOn     = document.getElementById('ing-addon').checked;

    if (!name) { showToast('Indtast et navn til ingrediensen', true); return; }

    try {
        const res = await api('/api/ingredients', {
            method: 'POST',
            body: JSON.stringify({ name, price, inventory, addOn })
        });
        if (!res.ok) throw new Error('Status ' + res.status);

        showToast('Ingrediens oprettet ✓');
        document.getElementById('ing-name').value      = '';
        document.getElementById('ing-price').value     = '';
        document.getElementById('ing-inventory').value = '';

        ingredientsLoaded = false;
        allIngredients    = [];
        loadIngredientList();
    } catch (e) { showToast('Kunne ikke oprette ingrediens: ' + e.message, true); }
}

async function deleteIngredient(id) {
    if (!confirm('Slet denne ingrediens? Den fjernes fra alle produkter der bruger den.')) return;
    try {
        const res = await api(`/api/ingredients/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        document.getElementById('ing-row-' + id)?.remove();
        allIngredients = [];
        showToast('Ingrediens slettet');
    } catch (e) { showToast('Kunne ikke slette ingrediens', true); }
}

let editingIngredientId = null;

function openEditIngredient(id, name, price, inventory, addOn) {
    editingIngredientId = id;
    document.getElementById('edit-ing-name').value      = name;
    document.getElementById('edit-ing-price').value     = price;
    document.getElementById('edit-ing-inventory').value = inventory;
    document.getElementById('edit-ing-addon').checked   = addOn;
    document.getElementById('edit-ingredient-modal-overlay').classList.add('open');
}

function closeEditIngredient() {
    document.getElementById('edit-ingredient-modal-overlay').classList.remove('open');
    editingIngredientId = null;
}

async function saveEditIngredient() {
    const name      = document.getElementById('edit-ing-name').value.trim();
    const price     = parseFloat(document.getElementById('edit-ing-price').value) || 0;
    const inventory = parseInt(document.getElementById('edit-ing-inventory').value) || 0;
    const addOn     = document.getElementById('edit-ing-addon').checked;

    try {
        const res = await api(`/api/ingredients/${editingIngredientId}`, {
            method: 'PUT',
            body: JSON.stringify({ name, price, inventory, addOn })
        });
        if (!res.ok) throw new Error('Status ' + res.status);

        showToast('Ingrediens opdateret ✓');
        closeEditIngredient();
        ingredientsLoaded = false;
        allIngredients = [];
        loadIngredientList();
    } catch (e) {
        showToast('Kunne ikke opdatere ingrediens', true);
    }
}

// ── Opening Hours ────────────────────────────────
const DAYS = { MONDAY: 'Mandag', TUESDAY: 'Tirsdag', WEDNESDAY: 'Onsdag', THURSDAY: 'Torsdag', FRIDAY: 'Fredag', SATURDAY: 'Lørdag', SUNDAY: 'Søndag' };

async function loadWeeklySchedule() {
    try {
        const schedule = await api('/admin/opening-hours/api/weekly').then(r => r.json());
        document.getElementById('weekly-schedule-body').innerHTML = schedule.map(row => `
            <div class="hours-row" id="row-${row.dayOfWeek}">
                <div class="day-name">${DAYS[row.dayOfWeek] ?? row.dayOfWeek}</div>
                <input type="time" value="${row.openTime ?? '12:00'}" data-field="open">
                <input type="time" value="${row.closeTime ?? '22:00'}" data-field="close">
                <label class="toggle-switch">
                    <input type="checkbox" ${row.active ? 'checked' : ''} data-field="active">
                    <div class="toggle-track"></div>
                </label>
                <button class="btn-sm" onclick="saveHoursRow('${row.dayOfWeek}', this)">Gem</button>
            </div>`).join('');
    } catch (e) {}
}

async function saveHoursRow(day, btn) {
    const row    = document.getElementById('row-' + day);
    const params = new URLSearchParams({
        dayOfWeek: day,
        openTime:  row.querySelector('[data-field="open"]').value,
        closeTime: row.querySelector('[data-field="close"]').value,
        active:    row.querySelector('[data-field="active"]').checked
    });
    try {
        const res = await fetch('/admin/opening-hours/api/weekly', {
            method: 'POST', credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });
        if (!res.ok) throw new Error();
        btn.textContent = '✓'; btn.classList.add('btn-success');
        setTimeout(() => { btn.textContent = 'Gem'; btn.classList.remove('btn-success'); }, 2000);
        checkOpeningStatus();
    } catch (e) { showToast('Fejl ved gem', true); }
}

async function loadHolidays() {
    try {
        const holidays = await api('/admin/opening-hours/api/holidays').then(r => r.json());
        const list = document.getElementById('holiday-list');
        if (!holidays.length) {
            list.innerHTML = '<p>Ingen særdage registreret</p>';
            return;
        }
        list.innerHTML = `<div class="hours-wrap">
            <div class="hours-table-head">Registrerede særdage</div>
            ${holidays.map(h => `
                <div class="hours-row">
                    <div class="day-name">${h.description}</div>
                    <div>${h.date}</div>
                    <div>${h.openTime}</div>
                    <div>${h.closeTime}</div>
                    <span class="status-pill ${h.active ? 'accepted' : 'pending'}">${h.active ? 'Aktiv' : 'Inaktiv'}</span>
                    <button class="btn-sm btn-danger" onclick="deleteHoliday(${h.id})">✕</button>
                </div>`).join('')}
        </div>`;
    } catch (e) {}
}

async function addHoliday() {
    const desc   = document.getElementById('hol-name').value.trim();
    const date   = document.getElementById('hol-date').value;
    const open   = document.getElementById('hol-open').value;
    const close  = document.getElementById('hol-close').value;
    const active = document.getElementById('hol-active').checked;
    if (!desc || !date || !open || !close) { showToast('Udfyld alle felter', true); return; }
    const params = new URLSearchParams({ description: desc, date, openTime: open, closeTime: close, active });
    try {
        const res = await fetch('/admin/opening-hours/api/holidays', {
            method: 'POST', credentials: 'include',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });
        if (!res.ok) throw new Error();
        showToast('Særdag tilføjet ✓');
        ['hol-name', 'hol-date', 'hol-open', 'hol-close'].forEach(id => document.getElementById(id).value = '');
        loadHolidays();
    } catch (e) { showToast('Fejl ved tilføjelse', true); }
}

async function deleteHoliday(id) {
    if (!confirm('Slet denne særdag?')) return;
    try {
        await api(`/admin/opening-hours/api/holidays/${id}`, { method: 'DELETE' });
        showToast('Slettet');
        loadHolidays();
    } catch (e) { showToast('Fejl ved sletning', true); }
}

// ── Logout ───────────────────────────────────────
async function doLogout() {
    try { await api('/api/users/logout', { method: 'POST' }); } finally {
        sessionStorage.clear();
        window.location.href = '/login.html';
    }
}

function setHistoryFilter(filter) {
    document.querySelectorAll('.history-filter-btn').forEach(b => b.classList.remove('active'));
    document.getElementById('hf-' + filter).classList.add('active');
    loadAdminHistory(filter);
}

// ── Modal overlays close on backdrop click ───────
document.getElementById('product-modal-overlay').addEventListener('click', function (e) {
    if (e.target === this) closeProductModal();
});

// Forhindrer fejl hvis edit-order overlay ikke findes i HTML endnu
const orderOverlay = document.getElementById('edit-order-modal-overlay');
if (orderOverlay) {
    orderOverlay.addEventListener('click', function (e) {
        if (e.target === this) closeEditOrderModal();
    });
}

// ── Init ─────────────────────────────────────────
(async () => {
    try {
        const isAdmin = await api('/api/users/is-admin').then(r => r.json());
        if (!isAdmin) { window.location.href = '/login.html'; return; }
    } catch (e) { window.location.href = '/login.html'; return; }

    checkOpeningStatus();
    loadOrderingStatus();
    loadPendingOrders();
    loadActiveOrders();
    loadIngredients();
    setInterval(() => { loadPendingOrders(); loadActiveOrders(); }, 30000);
})();
