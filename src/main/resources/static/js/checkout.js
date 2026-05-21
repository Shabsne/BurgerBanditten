// checkout.js
const cart  = JSON.parse(localStorage.getItem('burgerCart')) || [];
const total = cart.reduce((s, i) => s + i.price * i.quantity, 0);

// ── Render ordreoversigt ──────────────────────────────────────────────────────
const itemsEl = document.getElementById('checkout-items');
cart.forEach(item => {
    const row = document.createElement('div');
    row.className = 'order-summary-row';

    let details = `<span>${item.quantity}× ${item.name}</span><span>${item.price * item.quantity} kr.</span>`;
    row.innerHTML = details;
    itemsEl.appendChild(row);

    // Vis ekstra ingredienser og kommentar hvis valgt
    if (item.selectedIngredients?.length) {
        const extRow = document.createElement('div');
        extRow.className = 'order-summary-row order-summary-extra';
        extRow.innerHTML = `<span style="padding-left:1rem;font-size:0.78rem;">+ ${item.selectedIngredients.map(i => i.name).join(', ')}</span>`;
        itemsEl.appendChild(extRow);
    }
    if (item.comment) {
        const cmtRow = document.createElement('div');
        cmtRow.className = 'order-summary-row order-summary-extra';
        cmtRow.innerHTML = `<span style="padding-left:1rem;font-size:0.78rem;">💬 ${item.comment}</span>`;
        itemsEl.appendChild(cmtRow);
    }
});
document.getElementById('checkout-total').textContent = total + ' kr.';

// Sæt min-dato for forudbestilling til i dag
const today = new Date().toISOString().split('T')[0];
document.getElementById('pickup-date').min = today;

// ── Forudbestilling toggle ────────────────────────────────────────────────────
function onDeliveryTimeChange(radio) {
    const fields = document.getElementById('preorder-fields');
    if (radio.value === 'preorder') {
        fields.classList.add('open');
    } else {
        fields.classList.remove('open');
    }
}

// ── Validering helpers ────────────────────────────────────────────────────────
function setError(input, el, msg) {
    input.classList.add('error');
    el.textContent = msg;
    el.classList.add('visible');
}
function clearError(input, el) {
    input.classList.remove('error');
    el.classList.remove('visible');
}

// ── Form submit ───────────────────────────────────────────────────────────────
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

    // Forudbestilling valgt? Valider dato + tid
    const isPreorder = document.querySelector('input[name="delivery-time"]:checked')?.value === 'preorder';
    let pickupDateTime = null;

    if (isPreorder) {
        const dateInput = document.getElementById('pickup-date');
        const timeInput = document.getElementById('pickup-time');
        const dateErr   = document.getElementById('dateError');
        const timeErr   = document.getElementById('timeError');

        if (!dateInput.value) { setError(dateInput, dateErr, 'Vælg en dato'); valid = false; }
        else clearError(dateInput, dateErr);

        if (!timeInput.value) { setError(timeInput, timeErr, 'Vælg et tidspunkt'); valid = false; }
        else clearError(timeInput, timeErr);

        if (dateInput.value && timeInput.value) {
            pickupDateTime = `${dateInput.value}T${timeInput.value}:00`;
            // Kontrollér at tidspunktet er i fremtiden
            if (new Date(pickupDateTime) <= new Date()) {
                setError(timeInput, timeErr, 'Tidspunktet skal være i fremtiden');
                valid = false;
            }
        }
    }

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
                customerName:  nameInput.value.trim(),
                phone:         phoneInput.value.trim(),
                items:         cart,
                pickupDateTime // null hvis "Hurtigst mulig", ISO-streng hvis forudbestilling
            })
        });

        if (res.ok) {
            localStorage.removeItem('burgerCart');
            const msg = pickupDateTime
                ? `Forudbestilling modtaget! Afhentes: ${new Date(pickupDateTime).toLocaleString('da-DK')} 🍔`
                : 'Bestilling modtaget! Vi er i gang. 🍔';
            alert(msg);
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