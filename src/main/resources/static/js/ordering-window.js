document.querySelectorAll(".save-window-button").forEach(button => {

    button.addEventListener("click", async (event) => {

        event.preventDefault();

        const row = button.closest("tr");

        const dayOfWeek = row.querySelector(".dayOfWeek").value;
        const openTime = row.querySelector(".openTime").value;
        const closeTime = row.querySelector(".closeTime").value;
        const active = row.querySelector(".active").checked;

        const formData = new URLSearchParams();

        formData.append("dayOfWeek", dayOfWeek);
        formData.append("openTime", openTime);
        formData.append("closeTime", closeTime);
        formData.append("active", active);

        const response = await fetch("/admin/order-window/api", {
            method: "POST",
            body: formData
        });

        if (response.ok) {
            alert("Bestillingsvindue gemt");
        }
    });
});