// checkout.js
const cart  = JSON.parse(localStorage.getItem('burgerCart')) || [];
const total = cart.reduce((s, i) => s + i.price * i.quantity, 0);

// ── Render ordreoversigt ──────────────────────────────────────────────────────
const itemsEl = document.getElementById('checkout-items');
cart.forEach(item => {
    const row = document.createElement('div');
    row.className = 'order-summary-row';

    row.innerHTML = `<span>${item.quantity}× ${item.name}</span><span>${item.price * item.quantity} kr.</span>`;
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
const dateInputEl = document.getElementById('pickup-date');
if (dateInputEl) {
    dateInputEl.min = today;
}

// ── Generer tids-slots fra 12:00 til 22:00 (Hver halve time) ──────────────────
function generateTimeSlots() {
    const container = document.getElementById('time-slots-container');
    if (!container) return;

    container.innerHTML = ''; // Rens beholderen

    const startHour = 12;
    const endHour = 22;

    for (let hour = startHour; hour <= endHour; hour++) {
        // Stop efter kl. 22:00, så vi ikke får 22:30 med
        const minutesOptions = (hour === endHour) ? [0] : [0, 30];

        minutesOptions.forEach(min => {
            const timeStr = `${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`;

            container.innerHTML += `
                <label class="time-slot-btn">
                    <input type="radio" name="pickup-time-slot" value="${timeStr}">
                    <span>${timeStr}</span>
                </label>
            `;
        });
    }
}

// Kør genereringen med det samme
generateTimeSlots();

// ── Forudbestilling toggle ────────────────────────────────────────────────────
// Vi binder den også til vinduet, så den med sikkerhed kan kaldes fra dit HTML-radioinput
window.onDeliveryTimeChange = function(radio) {
    const fields = document.getElementById('preorder-fields');
    if (!fields) return;

    if (radio.value === 'preorder') {
        fields.classList.add('open');
    } else {
        fields.classList.remove('open');
    }
};

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

    // Forudbestilling valgt? Valider dato + tids-slot
    const isPreorder = document.querySelector('input[name="delivery-time"]:checked')?.value === 'preorder';
    let pickupDateTime = null;

    if (isPreorder) {
        const dateInput = document.getElementById('pickup-date');
        const timeContainer = document.getElementById('time-slots-container');
        const selectedTimeRadio = document.querySelector('input[name="pickup-time-slot"]:checked');

        const dateErr = document.getElementById('dateError');
        const timeErr = document.getElementById('timeError');

        // Valider dato
        if (!dateInput.value) {
            setError(dateInput, dateErr, 'Vælg en dato');
            valid = false;
        } else {
            clearError(dateInput, dateErr);
        }

        // Valider tids-slot radio knap
        if (!selectedTimeRadio) {
            setError(timeContainer, timeErr, 'Vælg et tidspunkt');
            valid = false;
        } else {
            clearError(timeContainer, timeErr);
        }

        // Hvis begge er udfyldt, tjek om det er i fremtiden
        if (dateInput.value && selectedTimeRadio) {
            pickupDateTime = `${dateInput.value}T${selectedTimeRadio.value}:00`;

            if (new Date(pickupDateTime) <= new Date()) {
                setError(timeContainer, timeErr, 'Tidspunktet skal være i fremtiden');
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
                items:         cart.map(item => ({
                    productId:           item.productId,
                    quantity:            item.quantity,
                    selectedIngredients: (item.selectedIngredients || []).map(i => i.id)
                })),
                pickupDateTime // null hvis "Hurtigst mulig", ISO-streng hvis forudbestilling
            })
        });

        if (res.ok) {
            localStorage.removeItem('burgerCart');
            const msg = pickupDateTime
                ? `Forudbestilling modtaget! Afhentes: ${new Date(pickupDateTime).toLocaleString('da-DK', { dateStyle: 'short', timeStyle: 'short' })} 🍔`
                : 'Bestilling modtaget! Vi er i gang. 🍔';
            alert(msg);
            window.location.href = '/menu.html';
        } else {
            const errorMsg = await res.text();
            alert(errorMsg || 'Der skete en fejl. Prøv igen.');
        }
    } catch (err) {
        alert('Ingen forbindelse til serveren.');
    } finally {
        btn.classList.remove('loading');
    }
});