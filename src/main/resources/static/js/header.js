
// ── Indsæt header CSS ───────────────────────────────
const headerStyle = document.createElement('style');
headerStyle.textContent = `
    .header {
        position: sticky;
        top: 0;
        z-index: 50;
        background: var(--card);
        border-bottom: 1px solid var(--border);
        padding: 1rem 2rem;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .header-logo {
        font-family: 'Bebas Neue', sans-serif;
        font-size: 1.6rem;
        letter-spacing: 0.04em;
        color: var(--text);
        text-decoration: none;
    }

    .header-logo span { color: var(--red); }

    .header-right {
        display: flex;
        align-items: center;
        gap: 1rem;
    }

    .btn-header-login {
        height: 38px;
        padding: 0 1.2rem;
        background: var(--red);
        border: none;
        border-radius: 6px;
        color: white;
        font-family: 'DM Sans', sans-serif;
        font-size: 0.85rem;
        cursor: pointer;
        transition: background 0.2s;
        text-decoration: none;
        display: flex;
        align-items: center;
    }

    .btn-header-login:hover { background: var(--red-hover); }

    .btn-header-logout {
        height: 38px;
        padding: 0 1.2rem;
        background: transparent;
        border: 1.5px solid var(--border);
        border-radius: 6px;
        color: var(--muted);
        font-family: 'DM Sans', sans-serif;
        font-size: 0.85rem;
        cursor: pointer;
        transition: border-color 0.2s, color 0.2s;
        display: flex;
        align-items: center;
    }

    .btn-header-logout:hover {
        border-color: var(--error);
        color: var(--error);
    }
`;
document.head.appendChild(headerStyle);

// ── Byg header HTML ──────────────────────────────────
function buildHeader() {
    const header = document.getElementById('main-header');
    if (!header) return;

    const isLoggedIn = sessionStorage.getItem('loggedIn');

    header.innerHTML = `
        <a href="/menu.html" class="header-logo">Burger<span>Banditten</span></a>
        <div class="header-right">
            ${isLoggedIn
        ? `<button class="btn-header-logout" onclick="headerLogout()">&#x2192; Log ud</button>`
        : `<a href="/login.html" class="btn-header-login">Log ind</a>`
    }
        </div>
    `;
}

// ── Logout ───────────────────────────────────────────
async function headerLogout() {
    try {
        const response = await fetch('/api/users/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        if (response.ok) {
            sessionStorage.removeItem('loggedIn');
            sessionStorage.removeItem('userRole');
            window.location.href = '/menu.html';
        }
    } catch (err) {
        console.error('Logout fejlede:', err);
    }
}

// ── Kør ved sideindlæsning ───────────────────────────
buildHeader();