// ═══════════════════════════════════════════════════
// login.js – Logik til login siden
// FIX: credentials: 'include' på alle fetch-kald så
//      Spring Security session-cookie sendes med.
// ═══════════════════════════════════════════════════

const form          = document.getElementById('loginForm');
const emailInput    = document.getElementById('email');
const passwordInput = document.getElementById('password');
const submitBtn     = document.getElementById('submitBtn');

// ── Hjælpefunktioner ────────────────────────────────

function setError(input, msgEl, message) {
    input.classList.add('error');
    msgEl.textContent = message;
    msgEl.classList.add('visible');
}

function clearError(input, msgEl) {
    input.classList.remove('error');
    msgEl.classList.remove('visible');
}

// ── Login status ─────────────────────────────────────

function checkLoginStatus() {
    const isLoggedIn = sessionStorage.getItem('loggedIn');
    const logoutBtn  = document.getElementById('logoutBtn');
    if (isLoggedIn && logoutBtn) logoutBtn.style.display = 'flex';
}

// ── Logout ───────────────────────────────────────────

async function logout() {
    try {
        const response = await fetch('/api/users/logout', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });
        if (response.ok) {
            sessionStorage.clear();
            window.location.href = '/menu.html';
        }
    } catch (err) {
        console.error('Logout fejlede:', err);
    }
}

// ── Live validering ──────────────────────────────────

emailInput.addEventListener('blur', () => {
    const el = document.getElementById('emailError');
    if (!emailInput.value.trim()) setError(emailInput, el, 'Email må ikke være tom');
    else clearError(emailInput, el);
});

passwordInput.addEventListener('blur', () => {
    const el = document.getElementById('passwordError');
    if (!passwordInput.value.trim()) setError(passwordInput, el, 'Adgangskode må ikke være tom');
    else clearError(passwordInput, el);
});

// ── Login formular ───────────────────────────────────

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const emailError    = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    let valid = true;

    if (!emailInput.value.trim())    { setError(emailInput, emailError, 'Email må ikke være tom'); valid = false; }
    if (!passwordInput.value.trim()) { setError(passwordInput, passwordError, 'Adgangskode må ikke være tom'); valid = false; }
    if (!valid) return;

    submitBtn.classList.add('loading');
    try {
        const response = await fetch('/api/users/login', {
            method: 'POST',
            credentials: 'include',   // ← KRITISK: sender session-cookie
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                mail:     emailInput.value.trim(),
                password: passwordInput.value
            })
        });

        if (response.ok) {
            const user = await response.json();

            sessionStorage.setItem('loggedIn', 'true');

            const userRole = (user.role || user.authority || '').toUpperCase();

            if (userRole === 'ADMIN' || userRole === 'ROLE_ADMIN') {
                sessionStorage.setItem('userRole', 'ADMIN');
                window.location.href = '/admin.html';
            } else {
                sessionStorage.setItem('userRole', 'CUSTOMER');
                window.location.href = '/menu.html';
            }
        } else {
            const msg = await response.text();
            setError(emailInput, emailError, msg || 'Forkert email eller adgangskode');
            setError(passwordInput, passwordError, ' ');
        }
    } catch (err) {
        setError(emailInput, document.getElementById('emailError'), 'Noget gik galt – prøv igen');
    } finally {
        submitBtn.classList.remove('loading');
    }
});

// ── Glemt adgangskode modal ──────────────────────────

function openForgotModal() {
    document.getElementById('forgotModal').classList.add('visible');
}

function closeForgotModal() {
    document.getElementById('forgotModal').classList.remove('visible');
    document.getElementById('forgotSuccess').style.display = 'none';
    document.getElementById('forgotEmail').value = '';
}

async function sendResetLink() {
    const forgotEmail      = document.getElementById('forgotEmail');
    const forgotEmailError = document.getElementById('forgotEmailError');
    const forgotSuccess    = document.getElementById('forgotSuccess');

    if (!forgotEmail.value.trim()) {
        setError(forgotEmail, forgotEmailError, 'Email må ikke være tom');
        return;
    }

    try {
        const response = await fetch('/api/users/forgot-password', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mail: forgotEmail.value.trim() })
        });

        if (response.ok) {
            forgotSuccess.style.display = 'block';
            forgotEmailError.classList.remove('visible');
        } else {
            const msg = await response.text();
            setError(forgotEmail, forgotEmailError, msg);
        }
    } catch (err) {
        setError(forgotEmail, forgotEmailError, 'Noget gik galt – prøv igen');
    }
}

// ── Kør ved sideindlæsning ───────────────────────────
checkLoginStatus();