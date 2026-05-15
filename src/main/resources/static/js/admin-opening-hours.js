document.addEventListener("DOMContentLoaded", () => {
    loadWeeklySchedule();
    loadHolidayOpeningHours();

    document.getElementById("addHolidayButton")
        .addEventListener("click", addHolidayOpeningHours);
});

async function loadWeeklySchedule() {
    const response = await fetch("/admin/order-window/api/weekly");
    const weeklySchedule = await response.json();

    const tableBody = document.getElementById("weeklyScheduleTableBody");
    tableBody.innerHTML = "";

    weeklySchedule.forEach(window => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${window.dayOfWeek}</td>
            <td><input class="openTime" type="time" value="${window.openTime}"></td>
            <td><input class="closeTime" type="time" value="${window.closeTime}"></td>
            <td><input class="active" type="checkbox" ${window.active ? "checked" : ""}></td>
            <td>
                <button type="button" class="save-window-button">
                    Gem
                </button>
            </td>
        `;

        row.querySelector(".save-window-button").addEventListener("click", async () => {
            await updateOpeningHours(
                window.dayOfWeek,
                row.querySelector(".openTime").value,
                row.querySelector(".closeTime").value,
                row.querySelector(".active").checked
            );
        });

        tableBody.appendChild(row);
    });
}

async function updateOpeningHours(dayOfWeek, openTime, closeTime, active) {
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
        alert(dayOfWeek + " blev gemt");
        await loadWeeklySchedule();
    }
}

async function loadHolidayOpeningHours() {
    const response = await fetch("/admin/order-window/api/holidays");
    const holidays = await response.json();

    const tableBody = document.getElementById("holidayTableBody");
    tableBody.innerHTML = "";

    holidays.forEach(holiday => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${holiday.description}</td>
            <td>${holiday.date}</td>
            <td>${holiday.openTime}</td>
            <td>${holiday.closeTime}</td>
            <td>${holiday.active}</td>
            <td>
                <button type="button" class="delete-holiday-button">
                    Slet
                </button>
            </td>
        `;

        row.querySelector(".delete-holiday-button").addEventListener("click", async () => {
            await deleteHolidayOpeningHours(holiday.id);
        });

        tableBody.appendChild(row);
    });
}

async function addHolidayOpeningHours() {
    const formData = new URLSearchParams();

    formData.append("description", document.getElementById("holidayName").value);
    formData.append("date", document.getElementById("holidayDate").value);
    formData.append("openTime", document.getElementById("holidayOpenTime").value);
    formData.append("closeTime", document.getElementById("holidayCloseTime").value);
    formData.append("active", document.getElementById("holidayActive").checked);

    const response = await fetch("/admin/order-window/api/holiday", {
        method: "POST",
        body: formData
    });

    if (response.ok) {
        await loadHolidayOpeningHours();
    }
}

async function deleteHolidayOpeningHours(id) {
    const formData = new URLSearchParams();
    formData.append("id", id);

    const response = await fetch("/admin/order-window/api/holiday/delete", {
        method: "POST",
        body: formData
    });

    if (response.ok) {
        await loadHolidayOpeningHours();
    }
}