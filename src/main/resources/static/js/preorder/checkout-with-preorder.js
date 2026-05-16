// ═══════════════════════════════════════════════════════════════════════════
// checkout-with-preorder.js  –  Eksempel på integration af forudbestilling
//
// Dette viser hvordan du kan integrere forudbestillingen i dit checkout-flow
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Eksempel: Opret ordre med forudbestilt tidspunkt
 */
async function createOrderWithPreorder(cartItems) {
    // Hent forudbestilt tidspunkt fra sessionStorage (sat af preorder-modal.js)
    const pickupDateTime = sessionStorage.getItem('preorderedPickupTime');

    // Hent brugerinfo (antag at dette er tilgængeligt fra login)
    const userEmail = sessionStorage.getItem('userEmail') || '';
    const userName = sessionStorage.getItem('userName') || '';

    // Build ordre objekt
    const orderData = {
        items: cartItems.map(item => ({
            productId: item.productId,
            quantity: item.quantity,
            selectedIngredients: item.selectedIngredients || []
        })),
        customerEmail: userEmail,
        customerName: userName,
        comment: document.getElementById('orderComment')?.value || '',
        pickupDateTime: pickupDateTime  // ← Forudbestillingen
    };

    try {
        const response = await fetch('/api/orders/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });

        if (response.ok) {
            const order = await response.json();

            // Ryd sessionStorage
            sessionStorage.removeItem('preorderedPickupTime');

            // Vis success
            showOrderConfirmation(order);

            return order;
        } else {
            const error = await response.text();
            showError('Kunne ikke oprette ordre: ' + error);
        }
    } catch (error) {
        console.error('Fejl ved ordrekreation:', error);
        showError('Kunne ikke oprette ordre');
    }
}

/**
 * Eksempel: Vis ordre bekræftelse med afhentningsidspunkt
 */
function showOrderConfirmation(order) {
    const modal = document.createElement('div');
    modal.className = 'order-confirmation-modal';

    const pickupDateTime = order.pickupTime?.pickupDateTime;
    const formattedTime = pickupDateTime
        ? new Date(pickupDateTime).toLocaleString('da-DK')
        : 'Ikke valgt';

    modal.innerHTML = `
        <div class="confirmation-content">
            <h2>✓ Ordre oprettet!</h2>
            <p>Ordre nummer: <strong>#${order.id}</strong></p>
            
            <div class="pickup-info">
                <h3>Afhentning</h3>
                <p>${formattedTime}</p>
            </div>

            <p class="confirmation-message">
                Vi sender dig en bekræftelse på email når ordren er klar.
            </p>

            <button onclick="window.location.href='/menu.html'">
                Tilbage til menuen
            </button>
        </div>
    `;

    document.body.appendChild(modal);
    modal.style.display = 'flex';
}

/**
 * Eksempel: Check om kunde skal vælge forudbestilling før checkout
 */
function checkPreorderBeforeCheckout() {
    const isOpen = sessionStorage.getItem('storeOpen') === 'true';

    if (!isOpen && !sessionStorage.getItem('preorderedPickupTime')) {
        // Butik lukket OG ingen forudbestilling – MUST choose pickup time
        window.preOrderModal.showPreOrderPopup();
        return false;
    }

    // OK – kan gå til checkout
    return true;
}

/**
 * Eksempel: Handler for "Gå til checkout" knap
 */
function handleCheckoutClick() {
    if (!checkPreorderBeforeCheckout()) {
        return;  // Forudbestilling påkrævet
    }

    // Gå til checkout
    window.location.href = '/checkout.html';
}

// ── Integration med menu.js ──────────────────────────────────────────────────

// Hvis du bruger de nuværende menu.js, tilføj dette:
//
// 1. Når siden loader, check om butik er åben:
//    const isOpen = await checkStoreOpenStatus();
//    sessionStorage.setItem('storeOpen', isOpen);
//
// 2. Når "Gå til betaling" klikkes:
//    if (!checkPreorderBeforeCheckout()) return;
//
// 3. Ved ordre submission:
//    await createOrderWithPreorder(cartItems);