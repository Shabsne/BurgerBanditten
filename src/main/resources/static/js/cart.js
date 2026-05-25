// cart.js – hovering cart widget med collapse/expand
let cart = JSON.parse(localStorage.getItem('burgerCart')) || [];
let cartExpanded = false;

// ── Expand / Collapse ────────────────────────────────────────────────────────

function expandCart() {
    cartExpanded = true;
    const panel = document.getElementById('cart-panel');
    const bar   = document.getElementById('cart-bar');
    if (panel) panel.classList.add('open');
    if (bar)   bar.classList.add('hidden');
}

function collapseCart() {
    cartExpanded = false;
    const panel = document.getElementById('cart-panel');
    const bar   = document.getElementById('cart-bar');
    if (panel) panel.classList.remove('open');
    if (bar)   bar.classList.remove('hidden');
}

// Bagud-kompatibel toggle (bruges stadig fra checkout-knap mv.)
function toggleCart() {
    if (cartExpanded) collapseCart();
    else expandCart();
}

// ── Toast ────────────────────────────────────────────────────────────────────

function showToast() {
    const toast = document.getElementById('cart-toast');
    if (toast) {
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 2000);
    }
}

// ── addToCart – understøtter nu selectedIngredients + comment ────────────────

/**
 * @param {number} productId
 * @param {string} name
 * @param {number} price
 * @param {Array}  selectedIngredients  – [{ id, name }, …]  (valgfrit)
 * @param {string} comment              – fri kommentar       (valgfrit)
 */
function addToCart(productId, name, price, selectedIngredients = [], comment = '') {
    // Samme produkt uden tilvalg/kommentar slås sammen; ellers ny linje
    const existing = cart.find(item =>
        item.productId === productId &&
        JSON.stringify(item.selectedIngredients || []) === JSON.stringify(selectedIngredients) &&
        (item.comment || '') === comment
    );

    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({ productId, name, price, quantity: 1, selectedIngredients, comment });
    }

    localStorage.setItem('burgerCart', JSON.stringify(cart));
    renderCart();
    showToast();
    expandCart(); // åbn kurven automatisk ved tilføjelse
}

// ── removeFromCart ────────────────────────────────────────────────────────────

function removeFromCart(idx) {
    if (idx < 0 || idx >= cart.length) return;
    if (cart[idx].quantity > 1) {
        cart[idx].quantity -= 1;
    } else {
        cart.splice(idx, 1);
    }
    localStorage.setItem('burgerCart', JSON.stringify(cart));
    renderCart();
}

// ── renderCart ────────────────────────────────────────────────────────────────

function renderCart() {
    const itemsEl = document.getElementById('cart-drawer-items');
    const totalEl = document.getElementById('cart-total');
    const countEl = document.getElementById('cart-count');

    if (!itemsEl || !totalEl || !countEl) return;

    itemsEl.innerHTML = '';
    let total = 0, totalItems = 0;

    if (cart.length === 0) {
        itemsEl.innerHTML = `
            <div class="cart-empty">
                <div class="cart-empty-icon">🛒</div>
                <p>Kurven er tom</p>
                <small>Tilføj lækre burgere fra menuen</small>
            </div>`;
        totalEl.innerText = '0';
        countEl.innerText = '0';
        return;
    }

    cart.forEach((item, idx) => {
        const itemTotal = item.price * item.quantity;
        total      += itemTotal;
        totalItems += item.quantity;

        const ingLine = item.selectedIngredients?.length
            ? `<span class="cart-item-extras">+ ${item.selectedIngredients.map(i => i.name).join(', ')}</span>`
            : '';
        const commentLine = item.comment
            ? `<span class="cart-item-comment">💬 ${item.comment}</span>`
            : '';

        itemsEl.innerHTML += `
            <div class="cart-item">
                <div class="cart-item-img-placeholder">🍔</div>
                <div class="cart-item-info">
                    <span class="cart-item-name">${item.name}</span>
                    <span class="cart-item-price">${itemTotal} kr.</span>
                    <span class="cart-item-unit-price">${item.quantity}x á ${item.price} kr.</span>
                    ${ingLine}
                    ${commentLine}
                </div>
                <div class="cart-item-right">
                    <span class="cart-item-qty-badge">${item.quantity}x</span>
                    <button class="cart-remove-btn" onclick="removeFromCart(${idx})">✕</button>
                </div>
            </div>`;
    });

    totalEl.innerText = total;
    countEl.innerText = totalItems;
}

// ── checkout ──────────────────────────────────────────────────────────────────

async function checkout() {
    if (cart.length === 0) return alert('Din kurv er tom!');

    const isLoggedIn = sessionStorage.getItem('loggedIn') === 'true';
    if (isLoggedIn) {
        await sendUserOrder();
    } else {
        window.location.href = '/checkout.html';
    }
}

async function sendUserOrder() {
    try {
        const orderRequest = {
            comment: '',   // kan udvides med en kommentar-boks senere
            pickUpTime: null,
            items: cart.map(item => ({
                productId:            item.productId,
                quantity:             item.quantity,
                selectedIngredients:  (item.selectedIngredients || []).map(i => i.id)
            }))
        };

        const response = await fetch('/api/orders/checkout', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderRequest)
        });

        if (response.status === 401 || response.status === 403) {
            sessionStorage.removeItem('loggedIn');
            window.location.href = '/checkout.html';
            return;
        }

        if (response.ok) {
            alert('Ordre modtaget! Velbekomme.');
            localStorage.removeItem('burgerCart');
            cart = [];
            renderCart();
            collapseCart();
        } else {
            const msg = await response.text();
            alert('Fejl: ' + msg);
        }
    } catch (error) {
        console.error('Fejl:', error);
    }
}

renderCart();