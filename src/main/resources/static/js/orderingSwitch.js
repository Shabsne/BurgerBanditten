

// ── Hent nuværende status ved sideindlæsning ────────
async function loadOrderingStatus() {
    try {
        const response = await fetch('/api/orders/status');
        const data = await response.json();
        updateSwitchUI(data.open);
    } catch (err) {
        console.error('Kunne ikke hente bestillingsstatus:', err);
    }
}

// ── Toggle bestillinger ──────────────────────────────
async function toggleOrdering() {
    try {
        const response = await fetch('/api/orders/admin/toggle', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const data = await response.json();
        updateSwitchUI(data.open);
        showStatusMessage(data.message);
    } catch (err) {
        console.error('Kunne ikke skifte bestillingsstatus:', err);
    }
}

// ── Opdater UI ───────────────────────────────────────
function updateSwitchUI(isOpen) {
    const toggle    = document.getElementById('ordering-toggle');
    const statusText = document.getElementById('ordering-status-text');

    if (!toggle || !statusText) return;

    if (isOpen) {
        toggle.classList.add('active');
        toggle.setAttribute('aria-checked', 'true');
        statusText.textContent = 'Bestillinger er åbne';
        statusText.style.color = 'var(--success)';
    } else {
        toggle.classList.remove('active');
        toggle.setAttribute('aria-checked', 'false');
        statusText.textContent = 'Bestillinger er lukkede';
        statusText.style.color = 'var(--error)';
    }
}

// ── Vis statusbesked ─────────────────────────────────
function showStatusMessage(message) {
    const msg = document.getElementById('switch-message');
    if (!msg) return;
    msg.textContent = message;
    msg.style.opacity = '1';
    setTimeout(() => msg.style.opacity = '0', 3000);
}

// ── Kør ved sideindlæsning ───────────────────────────
loadOrderingStatus();