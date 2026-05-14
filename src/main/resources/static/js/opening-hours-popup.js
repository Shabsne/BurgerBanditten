document.addEventListener("DOMContentLoaded", async () => {
    const overlay = document.getElementById("openingHoursOverlay");
    const closeButton = document.getElementById("closeOpeningHoursPopup");
    const message = document.getElementById("nextOpeningMessage");
    const preOrderButton = document.getElementById("preOrderButton");

    async function updateOpeningHoursPopup() {
        const response = await fetch("/opening-hours/next");
        const data = await response.json();

        message.textContent = data.message;

        if (!data.openNow) {
            overlay.classList.remove("hidden");

            // Senere når forudbestilling er klar:
            preOrderButton.classList.remove("hidden");
        } else {
            overlay.classList.add("hidden");
        }
    }

    closeButton.addEventListener("click", () => {
        overlay.classList.add("hidden");
    });

    await updateOpeningHoursPopup();

    setInterval(updateOpeningHoursPopup, 60000);
});