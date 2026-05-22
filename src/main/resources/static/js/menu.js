// menu.js

// ── Hent og rendér produkter ─────────────────────────────────────────────────

async function fetchProducts() {
    try {
        const res = await fetch('/api/products/menu', { credentials: 'include' });
        if (!res.ok) throw new Error('Kunne ikke hente produkter');
        renderMenu(await res.json());
    } catch (e) {
        console.error(e);
        document.body.insertAdjacentHTML('beforeend',
            '<p style="text-align:center;padding:2rem;color:var(--muted)">Kunne ikke hente menuen</p>');
    }
}

function renderMenu(products) {
    renderProducts(products.filter(p => p.category === 'BURGER'), 'burger-container');
    renderProducts(products.filter(p => p.category === 'DRINK'),  'drink-container');
    renderProducts(products.filter(p => p.category === 'SIDE'),   'side-container');
}

function renderProducts(products, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '';

    if (!products.length) {
        container.innerHTML = '<div class="empty-state"><p>Ingen produkter her endnu</p></div>';
        return;
    }

    products.forEach(p => {
        const imgContent = p.image
            ? `<img src="${p.image}" alt="${p.name}">`
            : '🍔';

        // Hele kortet er klikbart – ingen Se mere / +Tilføj knapper til kunderne
        container.innerHTML += `
            <div class="product-card product-card--clickable"
                 onclick="showProduct(${p.id})"
                 role="button" tabindex="0"
                 onkeydown="if(event.key==='Enter') showProduct(${p.id})">
                <div class="product-card-img">${imgContent}</div>
                <div class="product-card-body">
                    <div class="product-card-name">${p.name}</div>
                    <div class="product-card-desc">${p.description ?? ''}</div>
                    <div class="product-card-price">${p.price} kr.</div>
                </div>
                <div class="product-card-actions admin-actions" style="display:none;">
                    <button class="btn-sm admin-only hidden"
                            onclick="event.stopPropagation(); showUpdateModal(${p.id})">Rediger</button>
                    <button class="btn-sm btn-danger admin-only hidden"
                            onclick="event.stopPropagation(); deleteProduct(${p.id})">Slet</button>
                </div>
            </div>`;
    });
}

// ── Vis produkt-modal (klik på kort) ─────────────────────────────────────────

async function showProduct(id) {
    try {
        const [productRes, ingredientsRes] = await Promise.all([
            fetch(`/api/products/product/${id}`, { credentials: 'include' }),
            fetch('/api/products/ingredients',   { credentials: 'include' })
        ]);
        if (!productRes.ok) throw new Error();
        const product        = await productRes.json();
        const allIngredients = ingredientsRes.ok ? await ingredientsRes.json() : [];
        showProductModal(product, allIngredients);
    } catch (e) {
        alert('Kunne ikke hente produkt');
    }
}

function showProductModal(product, allIngredients = []) {
    const productIngNames = product.ingredients || [];

    const ingChips = allIngredients.map(ing => `
        <label class="ing-chip">
            <input type="checkbox" name="extra-ing"
                   value="${ing.id}" data-name="${ing.name}"
                   ${productIngNames.includes(ing.name) ? 'checked' : ''}>
            ${ing.name}
        </label>`).join('');

    const modal = document.getElementById('modal');
    modal.querySelector('#modal-content').innerHTML = `
        <div class="modal-header">
            <div class="modal-title">${product.name}</div>
        </div>

        <p class="product-modal-desc">${product.description ?? ''}</p>
        <p class="product-modal-price">${product.price} kr.</p>

        ${allIngredients.length > 0 ? `
        <div class="form-group" style="margin-top:1rem;">
            <label>Tilvalg / Ingredienser</label>
            <div class="ingredients-grid">${ingChips}</div>
        </div>` : ''}

        <div class="form-group" style="margin-top:1rem;">
            <label for="product-comment">Kommentar</label>
            <textarea id="product-comment" class="product-comment-input"
                      placeholder="Fx. ingen løg, ekstra dressing…" rows="2"></textarea>
        </div>

        <div class="modal-footer">
            <button class="btn-secondary" onclick="closeModal()">Tilbage</button>
            <button class="btn-primary"
                    onclick="addToCartFromModal(${product.id}, '${product.name.replace(/'/g, "\\'")}', ${product.price})">
                + Tilføj
            </button>
        </div>
    `;
    modal.classList.add('open');
}

function addToCartFromModal(id, name, price) {
    const selectedIngredients = [...document.querySelectorAll('input[name="extra-ing"]:checked')]
        .map(cb => ({ id: parseInt(cb.value), name: cb.dataset.name }));
    const comment = (document.getElementById('product-comment')?.value || '').trim();
    addToCart(id, name, price, selectedIngredients, comment);
    closeModal();
}

function closeModal() {
    document.getElementById('modal').classList.remove('open');
}

// ── Admin: rediger produkt modal ──────────────────────────────────────────────

async function showUpdateModal(id) {
    try {
        const [productRes, categoriesRes, ingredientsRes] = await Promise.all([
            fetch(`/api/products/product/${id}`,    { credentials: 'include' }),
            fetch('/api/products/categories',       { credentials: 'include' }),
            fetch('/api/products/ingredients',      { credentials: 'include' })
        ]);
        const product     = await productRes.json();
        const categories  = await categoriesRes.json();
        const ingredients = await ingredientsRes.json();

        const catOptions    = categories.map(c =>
            `<option value="${c}" ${c === product.category ? 'selected' : ''}>${c}</option>`).join('');
        const ingCheckboxes = ingredients.map(ing => `
            <label class="ing-chip">
                <input type="checkbox" name="ingredients" value="${ing.id}"
                    ${product.ingredients?.includes(ing.name) ? 'checked' : ''}>
                ${ing.name}
            </label>`).join('');

        const modal = document.getElementById('update-modal');
        modal.querySelector('#update-modal-content').innerHTML = `
            <div class="modal-header">
                <div class="modal-title">Rediger produkt</div>
                <button class="modal-close" onclick="closeUpdateModal()">&times;</button>
            </div>
            <div class="form-group"><label>Navn</label>
                <input type="text" id="update-name" value="${product.name}"></div>
            <div class="form-group"><label>Beskrivelse</label>
                <input type="text" id="update-description" value="${product.description}"></div>
            <div class="form-group"><label>Pris (kr)</label>
                <input type="number" id="update-price" value="${product.price}"></div>
            <div class="form-group"><label>Kategori</label>
                <select id="update-category">${catOptions}</select></div>
            <div class="form-group">
                <label class="toggle-switch">
                    <input type="checkbox" id="update-lunchOffer" ${product.lunchOffer ? 'checked' : ''}>
                    <div class="toggle-track"></div>
                    <span>Frokosttilbud</span>
                </label>
            </div>
            <div class="form-group"><label>Ingredienser</label>
                <div class="ingredients-grid">${ingCheckboxes}</div></div>
            <div class="form-group"><label>Nyt billede (valgfrit)</label>
                <input type="file" id="update-image" accept="image/*"></div>
            <div class="modal-footer">
                <button class="btn-primary" onclick="updateProduct(${product.id})">Gem ændringer</button>
                <button class="btn-secondary" onclick="closeUpdateModal()">Annuller</button>
            </div>`;
        modal.classList.add('open');
    } catch (e) {
        alert('Kunne ikke hente produkt');
    }
}

async function updateProduct(id) {
    const fileInput = document.getElementById('update-image');
    const file      = fileInput.files[0];
    const image     = file ? await new Promise(res => {
        const r = new FileReader(); r.onloadend = () => res(r.result); r.readAsDataURL(file);
    }) : null;

    const payload = {
        name:        document.getElementById('update-name').value,
        description: document.getElementById('update-description').value,
        price:       parseFloat(document.getElementById('update-price').value),
        category:    document.getElementById('update-category').value,
        ingredients: [...document.querySelectorAll('input[name="ingredients"]:checked')].map(cb => parseInt(cb.value)),
        lunchOffer:  document.getElementById('update-lunchOffer').checked,
        image
    };
    try {
        const res = await fetch(`/api/products/admin/product/update/${id}`, {
            method: 'PUT', credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error();
        closeUpdateModal();
        fetchProducts();
    } catch (e) { alert('Kunne ikke opdatere produkt'); }
}

function closeUpdateModal() {
    document.getElementById('update-modal').classList.remove('open');
}

async function deleteProduct(id) {
    if (!confirm('Er du sikker?')) return;
    try {
        const res = await fetch(`/api/products/admin/product/delete/${id}`,
            { method: 'DELETE', credentials: 'include' });
        if (!res.ok) throw new Error();
        fetchProducts();
    } catch (e) { alert('Kunne ikke slette produkt'); }
}

// ── Auth helpers ──────────────────────────────────────────────────────────────

async function checkAdmin() {
    try {
        const res      = await fetch('/api/users/is-admin', { credentials: 'include' });
        const isAdmin  = await res.json();
        const loggedIn = sessionStorage.getItem('loggedIn') === 'true';

        if (isAdmin) {
            document.getElementById('admin-link').classList.remove('hidden');
            document.querySelectorAll('.admin-only').forEach(el => el.classList.remove('hidden'));
            // Vis admin-knapper på kortene
            document.querySelectorAll('.admin-actions').forEach(el => el.style.display = 'flex');
        }
        if (loggedIn) {
            document.getElementById('login-btn').classList.add('hidden');
            document.getElementById('logout-btn').classList.remove('hidden');
        }
    } catch (e) { /* ignore */ }
}

async function doLogout() {
    try {
        await fetch('/api/users/logout', { method: 'POST', credentials: 'include' });
    } finally {
        sessionStorage.clear();
        window.location.href = '/menu.html';
    }
}

document.getElementById('modal').addEventListener('click', function(e) {
    if (e.target === this) closeModal();
});
document.getElementById('update-modal').addEventListener('click', function(e) {
    if (e.target === this) closeUpdateModal();
});

fetchProducts();
checkAdmin();