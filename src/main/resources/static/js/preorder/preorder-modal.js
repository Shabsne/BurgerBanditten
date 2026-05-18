class PreOrderModal {
    constructor() {
        this.modal = null;
        this.overlayElement = null;
        this.selectedDateTime = null;
        this.isOpen = false;
    }

    /**
     * Initialiser modalen (kaldes når siden loader)
     */
    async init() {
        await this.checkIfClosed();
    }

    /**
     * Check om butikken er lukket og vis popup hvis nødvendigt
     */
    async checkIfClosed() {
        try {
            const response = await fetch('/opening-hours/next');
            const data = await response.json();

            // Hvis åbent nu, skjul popup
            if (data.openNow) {
                return;
            }

            // Vi er lukket – vis forudbestillingsudfordring
            this.showPreOrderPopup(data);

        } catch (error) {
            console.error('Fejl ved check af åbningstider:', error);
        }
    }

    /**
     * Lyt efter tilbud om at forudbestille når man lukker
     */
    onCheckoutClick() {
        // Denne funktion bliver kaldt når kunden klikker "Bestil nu" på menuen
        // Hvis lukket, vis modal før vi går til checkout
        if (!this.isStoreOpen()) {
            this.showPreOrderPopup();
        }
    }

    /**
     * Vis forudbestillingspopup'en
     */
    showPreOrderPopup(nextOpeningData = null) {
        if (this.isOpen) return;

        // Opret overlay
        this.overlayElement = document.createElement('div');
        this.overlayElement.className = 'preorder-overlay';
        this.overlayElement.id = 'preorderOverlay';

        // Opret modal
        this.modal = document.createElement('div');
        this.modal.className = 'preorder-modal fade-up';

        let contentHTML = `
            <div class="preorder-header">
                <div class="preorder-icon">🕐</div>
                <h2>Vi er lukket lige nu</h2>
                <p>Men du kan forudbestille din orden!</p>
            </div>

            <div class="preorder-content">
        `;

        if (nextOpeningData) {
            contentHTML += `
                <div class="next-opening-info">
                    <p class="info-label">Næste åbning:</p>
                    <p class="info-value">${nextOpeningData.message}</p>
                </div>
            `;
        }

        contentHTML += `
                <!-- Dato vælger -->
                <div class="form-group">
                    <label for="pickupDate">Vælg dato</label>
                    <input 
                        type="date" 
                        id="pickupDate"
                        class="preorder-input"
                        min="${this.getTodayString()}"
                    >
                </div>

                <!-- Tid vælger -->
                <div class="form-group">
                    <label for="pickupTime">Vælg tidspunkt</label>
                    <input 
                        type="time" 
                        id="pickupTime"
                        class="preorder-input"
                    >
                </div>

                <!-- Error besked -->
                <div class="preorder-error" id="preorderError" style="display: none;"></div>

                <!-- Info besked -->
                <div class="preorder-info" id="preorderInfo"></div>
            </div>

            <div class="preorder-actions">
                <button class="btn-preorder-confirm" id="confirmPreorderBtn">
                    Bekræft afhentning
                </button>
                <button class="btn-preorder-cancel" id="cancelPreorderBtn">
                    Fortsæt som gæst
                </button>
            </div>
        `;

        this.modal.innerHTML = contentHTML;

        // Tilføj til DOM
        this.overlayElement.appendChild(this.modal);
        document.body.appendChild(this.overlayElement);

        // Event listeners
        this.attachEventListeners();
        this.setDefaultDate();

        this.isOpen = true;
    }

    /**
     * Bind event listeners til modal
     */
    attachEventListeners() {
        const dateInput = document.getElementById('pickupDate');
        const timeInput = document.getElementById('pickupTime');
        const confirmBtn = document.getElementById('confirmPreorderBtn');
        const cancelBtn = document.getElementById('cancelPreorderBtn');
        const overlay = this.overlayElement;

        // Luk ved klik uden for modal (ikke på cancel knap)
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                this.close();
            }
        });

        // Validering når dato/tid ændres
        dateInput.addEventListener('change', () => this.validateDateTime());
        timeInput.addEventListener('change', () => this.validateDateTime());

        // Bekræft
        confirmBtn.addEventListener('click', () => this.confirmPreOrder());

        // Afbryd
        cancelBtn.addEventListener('click', () => this.close());
    }

    /**
     * Sæt defaultværdier (næste mulige tidsværdier)
     */
    async setDefaultDate() {
        try {
            const response = await fetch('/api/preorder/next-available');
            const data = await response.json();

            const dateInput = document.getElementById('pickupDate');
            const timeInput = document.getElementById('pickupTime');

            if (data.nextDate) {
                dateInput.value = data.nextDate;
                timeInput.value = data.nextOpenTime || '12:00';

                // Vis info
                const infoEl = document.getElementById('preorderInfo');
                infoEl.textContent = `Vi åbner ${data.nextDate} kl. ${data.nextOpenTime}`;
            }
        } catch (error) {
            console.error('Fejl ved hentning af næste åbning:', error);
        }
    }

    /**
     * Valider valgt dato og tid
     */
    async validateDateTime() {
        const dateInput = document.getElementById('pickupDate');
        const timeInput = document.getElementById('pickupTime');
        const errorEl = document.getElementById('preorderError');
        const confirmBtn = document.getElementById('confirmPreorderBtn');

        if (!dateInput.value || !timeInput.value) {
            confirmBtn.disabled = true;
            return;
        }

        // Kombiner dato + tid
        const dateTimeString = `${dateInput.value}T${timeInput.value}:00`;
        this.selectedDateTime = new Date(dateTimeString);

        try {
            const response = await fetch('/api/preorder/validate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    pickupDateTime: dateTimeString,
                    customerEmail: '', // Fyld hvis bruger er logget ind
                    customerName: ''
                })
            });

            const result = await response.json();

            if (result.valid) {
                errorEl.style.display = 'none';
                confirmBtn.disabled = false;
            } else {
                errorEl.textContent = result.message;
                errorEl.style.display = 'block';
                confirmBtn.disabled = true;
            }
        } catch (error) {
            console.error('Valideringsfejl:', error);
            errorEl.textContent = 'Kunne ikke validere tidspunkt';
            errorEl.style.display = 'block';
            confirmBtn.disabled = true;
        }
    }

    /**
     * Bekræft forudbestilling
     */
    async confirmPreOrder() {
        if (!this.selectedDateTime) return;

        // Gem valgt tidspunkt i sessionStorage for senere brug ved checkout
        sessionStorage.setItem('preorderedPickupTime', this.selectedDateTime.toISOString());

        // Vis success feedback
        const confirmBtn = document.getElementById('confirmPreorderBtn');
        confirmBtn.disabled = true;
        confirmBtn.textContent = '✓ Bekræftet';

        setTimeout(() => {
            this.close();
            // Redirect til meny eller checkout
            window.location.href = '/menu.html';
        }, 800);
    }

    /**
     * Luk modal
     */
    close() {
        if (this.overlayElement) {
            this.overlayElement.style.opacity = '0';
            this.overlayElement.style.transition = 'opacity 0.3s ease';

            setTimeout(() => {
                this.overlayElement.remove();
                this.modal = null;
                this.overlayElement = null;
                this.isOpen = false;
            }, 300);
        }
    }

    /**
     * Hjælpefunktion: Get today's date som YYYY-MM-DD
     */
    getTodayString() {
        const today = new Date();
        return today.toISOString().split('T')[0];
    }

    /**
     * Check om butikken er åben nu
     */
    async isStoreOpen() {
        try {
            const response = await fetch('/opening-hours/next');
            const data = await response.json();
            return data.openNow;
        } catch {
            return true; // Antag åbent hvis fejl
        }
    }
}

// ── Init ved siden loader ────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const preOrderModal = new PreOrderModal();
    preOrderModal.init();

    // Gør tilgængelig globalt så andre scripts kan kalde det
    window.preOrderModal = preOrderModal;
});