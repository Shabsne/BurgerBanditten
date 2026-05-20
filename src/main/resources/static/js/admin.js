// Admin logik [cite: 140-147]
/*
async function fetchPendingOrders() {
    try {
        // Bemærk at jeg har rettet URL'en til /api/orders, som beskrevet tidligere
        const response = await fetch('/api/orders/pending');
        if (!response.ok) throw new Error("Kunne ikke hente ordrer");

        const orders = await response.json();
        renderPendingOrders(orders);
    } catch (error) {
        console.error(error);
        document.getElementById('pending-orders-container').innerHTML = `<p>Fejl ved indlæsning af ordrer.</p>`;
    }
}*/

async function fetchPendingOrders() {
    try {
        const response = await fetch('/api/orders/pending');

        if (!response.ok) {
            // Log the status to help identify if it's a 404, 403, or 500 error
            throw new Error(`Kunne ikke hente ordrer (Status: ${response.status})`);
        }

        const orders = await response.json();
        renderPendingOrders(orders);
    } catch (error) {
        console.error("Fetch error:", error);
        document.getElementById('pending-orders-container').innerHTML = `<p>Fejl ved indlæsning af ordrer.</p>`;
    }
}


function renderPendingOrders(orders) {
    const container = document.getElementById('pending-orders-container');
    container.innerHTML = '';

    if (orders.length === 0) {
        container.innerHTML = '<p>Ingen ventende ordrer lige nu.</p>';
        return;
    }

    orders.forEach(order => {
        // Blander wireframens card-design med dit data [cite: 97]
        const orderCard = `
            <div class="card" id="order-${order.id}">
                <div style="display:flex; justify-content: space-between; margin-bottom: 10px;">
                    <div>
                        <strong>#${order.id}</strong> 
                        <div style="font-size: 12px; color: gray;">Oprettet: ${new Date().toLocaleTimeString()}</div>
                    </div>
                    <div style="font-weight: bold;">
                        ${order.totalPrice || '???'} kr.
                    </div>
                </div>
                
                <div style="margin-bottom: 15px;">
                    Se detaljer i databasen for denne ordre.
                </div>
                
                <div>
                    <button class="action-btn btn-accept" onclick="acceptOrder(${order.id})">✓ Accepter Ordre</button>
                </div>
            </div>
        `;
        container.innerHTML += orderCard;
    });
}

// Kalder din PUT /api/orders/{orderId}/accept [cite: 142]
async function acceptOrder(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}/accept`, {
            method: 'PUT'
        });

        if (response.ok) {
            // Fjern ordren fra listen visuelt
            document.getElementById(`order-${orderId}`).style.display = 'none';
            alert(`Ordre #${orderId} accepteret!`);
        } else {
            alert('Kunne ikke acceptere ordre.');
        }
    } catch (error) {
        console.error("Fejl:", error);
    }
}

// Skift mellem faneblade (Wireframe logik [cite: 98, 101])
function showScreen(screenId, btnElement) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('visible'));
    document.querySelectorAll('.wf-btn').forEach(b => b.classList.remove('active'));

    document.getElementById('screen-' + screenId).classList.add('visible');
    btnElement.classList.add('active');

    // Hent data når skærmen åbnes
    if(screenId === 'adminorders') {
        fetchPendingOrders();
    }
}

// Start appen
fetchPendingOrders();