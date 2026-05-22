// cart.js – kurv-logik med credentials: 'include' fix
let cart = JSON.parse(localStorage.getItem('burgerCart')) || [];

function toggleCart() {
    const drawer  = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-overlay');
    if (drawer && overlay) {
        drawer.classList.toggle('open');
        overlay.classList.toggle('open');
    }
}

function showToast() {
    const toast = document.getElementById('cart-toast');
    if (toast) {
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 2000);
    }
}

function addToCart(productId, name, price) {
    const existing = cart.find(item => item.productId === productId);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({ productId, name, price, quantity: 1 });
    }
    localStorage.setItem('burgerCart', JSON.stringify(cart));
    renderCart();
    showToast();
}

function removeFromCart(productId) {
    const idx = cart.findIndex(item => item.productId === productId);
    if (idx > -1) {
        if (cart[idx].quantity > 1) {
            cart[idx].quantity -= 1;
        } else {
            cart.splice(idx, 1);
        }
    }
    localStorage.setItem('burgerCart', JSON.stringify(cart));
    renderCart();
}

function renderCart() {
    const itemsEl  = document.getElementById('cart-drawer-items');
    const totalEl  = document.getElementById('cart-total');
    const countEl  = document.getElementById('cart-count');

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

    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        totalItems += item.quantity;

        itemsEl.innerHTML += `
            <div class="cart-item">
                <div class="cart-item-img-placeholder">🍔</div>
                <div class="cart-item-info">
                    <span class="cart-item-name">${item.name}</span>
                    <span class="cart-item-price">${itemTotal} kr.</span>
                    <span class="cart-item-unit-price">${item.quantity}x á ${item.price} kr.</span>
                </div>
                <div class="cart-item-right">
                    <span class="cart-item-qty-badge">${item.quantity}x</span>
                    <button class="cart-remove-btn" onclick="removeFromCart(${item.productId})">✕</button>
                </div>
            </div>`;
    });

    totalEl.innerText = total;
    countEl.innerText = totalItems;
}

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
        const response = await fetch('/api/orders/checkout', {
            method: 'POST',
            credentials: 'include',            // ← FIX
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cart)
        });

        if (response.ok) {
            alert('Ordre modtaget! Velbekomme.');
            localStorage.removeItem('burgerCart');
            cart = [];
            renderCart();
            toggleCart();
        } else {
            alert('Der skete en fejl under bestillingen.');
        }
    } catch (error) {
        console.error('Fejl:', error);
    }
}

renderCart();