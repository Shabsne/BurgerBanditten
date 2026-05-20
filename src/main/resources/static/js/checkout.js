// checkout.js
const cart  = JSON.parse(localStorage.getItem('burgerCart')) || [];
const total = cart.reduce((s, i) => s + i.price * i.quantity, 0);

// Render ordreoversigt
const itemsEl = document.getElementById('checkout-items');
cart.forEach(item => {
    const row = document.createElement('div');
    row.className = 'order-summary-row';
    row.innerHTML = `<span>${item.quantity}× ${item.name}</span><span>${item.price * item.quantity} kr.</span>`;
    itemsEl.appendChild(row);
});
document.getElementById('checkout-total').textContent = total + ' kr.';

// Validering
function setError(input, el, msg) {
    input.classList.add('error');
    el.textContent = msg;
    el.classList.add('visible');
}
function clearError(input, el) {
    input.classList.remove('error');
    el.classList.remove('visible');
}

document.getElementById('checkoutForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const nameInput  = document.getElementById('fullName');
    const phoneInput = document.getElementById('phone');
    const nameErr    = document.getElementById('nameError');
    const phoneErr   = document.getElementById('phoneError');
    let valid = true;

    if (!nameInput.value.trim())  { setError(nameInput,  nameErr,  'Navn er påkrævet'); valid = false; }
    else clearError(nameInput, nameErr);

    if (!phoneInput.value.trim()) { setError(phoneInput, phoneErr, 'Telefonnummer er påkrævet'); valid = false; }
    else clearError(phoneInput, phoneErr);

    if (!valid) return;
    if (cart.length === 0) { alert('Din kurv er tom!'); window.location.href = '/menu.html'; return; }

    const btn = document.getElementById('submitOrderBtn');
    btn.classList.add('loading');

    try {
        const res = await fetch('/api/orders/guest/checkout', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerName: nameInput.value.trim(),
                phone:        phoneInput.value.trim(),
                items:        cart
            })
        });

        if (res.ok) {
            localStorage.removeItem('burgerCart');
            alert('Bestilling modtaget! Vi er i gang. 🍔');
            window.location.href = '/menu.html';
        } else {
            alert('Der skete en fejl. Prøv igen.');
        }
    } catch (err) {
        alert('Ingen forbindelse til serveren.');
    } finally {
        btn.classList.remove('loading');
    }
});