// Hent kurv fra localStorage eller opret en tom
let cart = JSON.parse(localStorage.getItem('burgerCart')) || [];

// Åben og lukke logik til din Drawer
function toggleCart() {
    const drawer = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-overlay');
    if (drawer && overlay) {
        drawer.classList.toggle('open');
        overlay.classList.toggle('open');
    }
}

// Vis en hurtig popup-toast når noget tilføjes
function showToast() {
    const toast = document.getElementById('cart-toast');
    if (toast) {
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
        }, 2000);
    }
}

// Tilføj produkt til kurven
function addToCart(productId, name, price) {
    const existingItem = cart.find(item => item.productId === productId);

    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ productId, name, price, quantity: 1 });
    }

    localStorage.setItem('burgerCart', JSON.stringify(cart));
    console.log("Kurv opdateret:", cart);

    renderCart();
    showToast();
}

// Fjern eller reducer antallet af en vare
function removeFromCart(productId) {
    const itemIndex = cart.findIndex(item => item.productId === productId);

    if (itemIndex > -1) {
        if (cart[itemIndex].quantity > 1) {
            cart[itemIndex].quantity -= 1;
        } else {
            cart.splice(itemIndex, 1);
        }
    }

    localStorage.setItem('burgerCart', JSON.stringify(cart));
    renderCart();
}

// Tegn indholdet af din mørke Drawer container
function renderCart() {
    const cartItemsContainer = document.getElementById('cart-drawer-items');
    const cartTotalContainer = document.getElementById('cart-total');
    const cartCountContainer = document.getElementById('cart-count');

    if (!cartItemsContainer || !cartTotalContainer || !cartCountContainer) return;

    cartItemsContainer.innerHTML = "";
    let total = 0;
    let totalItems = 0;

    if (cart.length === 0) {
        cartItemsContainer.innerHTML = `
            <div class="cart-empty">
                <div class="cart-empty-icon">🛒</div>
                <p>Kurven er tom</p>
                <small>Tilføj lækre burgere fra menuen</small>
            </div>`;
        cartTotalContainer.innerText = "0";
        cartCountContainer.innerText = "0";
        return;
    }

    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        totalItems += item.quantity;

        cartItemsContainer.innerHTML += `
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
            </div>
        `;
    });

    cartTotalContainer.innerText = total;
    cartCountContainer.innerText = totalItems;
}

async function checkout() {
    if (cart.length === 0) return alert("Din kurv er tom!");

    const isLoggedIn = sessionStorage.getItem('loggedIn') === 'true';

    if (isLoggedIn) {
        // Hvis brugeren er logget ind, kan vi sende ordren med det samme (eller sende dem til en lukket checkout-side)
        await sendUserOrder();
    } else {
        // Hvis de er gæst, sender vi dem til formularen, så de kan udfylde navn og adresse
        window.location.href = '/checkout.html';
    }
}

// Ekstra funktion til registrerede brugere
async function sendUserOrder() {
    try {
        const response = await fetch('/api/orders/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cart)
        });

        if (response.ok) {
            alert("Ordre modtaget! Velbekomme.");
            localStorage.removeItem('burgerCart');
            cart = [];
            renderCart();
            toggleCart();
        } else {
            alert("Der skete en fejl under bestillingen.");
        }
    } catch (error) {
        console.error("Fejl:", error);
    }
}

// Sørg for at indlæse data med det samme ved refresh
renderCart();