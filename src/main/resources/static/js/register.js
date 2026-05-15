// ═══════════════════════════════════════════════════
// register.js – Logik til registreringssiden
// ═══════════════════════════════════════════════════

const form          = document.getElementById('registerForm');
const nameInput     = document.getElementById('name');
const emailInput    = document.getElementById('email');
const passwordInput = document.getElementById('password');
const confirmInput  = document.getElementById('confirmPassword');
const submitBtn     = document.getElementById('submitBtn');
const successBanner = document.getElementById('successBanner');

// ── Hjælpefunktioner ────────────────────────────────

function setError(input, msgEl, message) {
    input.classList.add('error');
    input.classList.remove('success');
    msgEl.textContent = message;
    msgEl.classList.add('visible');
}

function setSuccess(input, msgEl) {
    input.classList.remove('error');
    input.classList.add('success');
    msgEl.classList.remove('visible');
}

// ── Adgangskode styrke ───────────────────────────────

passwordInput.addEventListener('input', () => {
    const val   = passwordInput.value;
    const bar   = document.getElementById('strengthBar');
    const label = document.getElementById('strengthLabel');

    if (val.length === 0) {
        bar.className = 'strength-bar';
        label.textContent = '';
        return;
    }

    let score = 0;
    if (val.length >= 8) score++;
    if (/[A-Z]/.test(val) && /[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    if (score === 1) { bar.className = 'strength-bar weak';   label.textContent = 'Svag'; }
    if (score === 2) { bar.className = 'strength-bar medium';  label.textContent = 'Medium'; }
    if (score === 3) { bar.className = 'strength-bar strong';  label.textContent = 'Stærk'; }
});

// ── Live validering ──────────────────────────────────

nameInput.addEventListener('blur', () => {
    const el = document.getElementById('nameError');
    if (nameInput.value.trim().length < 2) setError(nameInput, el, 'Navn skal udfyldes');
    else setSuccess(nameInput, el);
});

emailInput.addEventListener('blur', () => {
    const el    = document.getElementById('emailError');
    const valid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailInput.value);
    if (!valid) setError(emailInput, el, 'Indtast en gyldig email');
    else setSuccess(emailInput, el);
});

confirmInput.addEventListener('blur', () => {
    const el = document.getElementById('confirmError');
    if (confirmInput.value !== passwordInput.value) setError(confirmInput, el, 'Adgangskoderne stemmer ikke overens');
    else setSuccess(confirmInput, el);
});

// ── Registrering formular ────────────────────────────

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const nameError     = document.getElementById('nameError');
    const emailError    = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    const confirmError  = document.getElementById('confirmError');
    let valid = true;

    if (nameInput.value.trim().length < 2)                    { setError(nameInput, nameError, 'Navn skal udfyldes'); valid = false; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailInput.value)) { setError(emailInput, emailError, 'Indtast en gyldig email'); valid = false; }
    if (passwordInput.value.length < 8)                       { setError(passwordInput, passwordError, 'Adgangskode skal være mindst 8 tegn'); valid = false; }
    if (confirmInput.value !== passwordInput.value)           { setError(confirmInput, confirmError, 'Adgangskoderne stemmer ikke overens'); valid = false; }
    if (!valid) return;

    submitBtn.classList.add('loading');
    try {
        const response = await fetch('/api/users/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name:     nameInput.value.trim(),
                mail:     emailInput.value.trim(),
                password: passwordInput.value
            })
        });

        if (response.ok) {
            successBanner.style.display = 'block';
            setTimeout(() => {
                window.location.href = '/menu.html';
            }, 2000);
            form.reset();
            [nameInput, emailInput, passwordInput, confirmInput].forEach(i => {
                i.classList.remove('success', 'error');
            });
            document.getElementById('strengthBar').className = 'strength-bar';
            document.getElementById('strengthLabel').textContent = '';
        } else {
            const msg = await response.text();
            setError(emailInput, emailError, msg);
        }
    } catch (err) {
        setError(emailInput, document.getElementById('emailError'), 'Noget gik galt – prøv igen');
    } finally {
        submitBtn.classList.remove('loading');
    }
});