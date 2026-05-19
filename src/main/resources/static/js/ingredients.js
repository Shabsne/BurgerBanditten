async function fetchIngredients() {
    try {
        const response = await fetch("/api/ingredients");

        if (!response.ok) {
            throw new Error("Could not fetch ingredients");
        }

        const ingredients = await response.json();
        renderIngredients(ingredients);
    } catch (error) {
        console.error(error);
        document.getElementById("error-message").style.display = "block";
    }
}

function renderIngredients(ingredients) {
    const tbody = document.getElementById("ingredient-body");
    tbody.innerHTML = "";

    ingredients.forEach(ingredient => {
        tbody.innerHTML += `
            <tr>
                <td>${ingredient.name}</td>
                <td>${ingredient.price}</td>
                <td>${ingredient.inventory}</td>
                <td>${ingredient.addOn ? "Ja" : "Nej"}</td>
                <td>
                <button onclick="openEditModal(${ingredient.id})">Rediger</button>
                <button onclick="deleteIngredient(${ingredient.id})">Slet</button>
                </td>
            </tr>
        `;
    })
}

let currentIngredientId = null

function openEditModal(id) {
    fetch(`/api/ingredients/${id}`)
        .then(r => r.json())
        .then(ingredient => {
            currentIngredientId = ingredient.id;
            document.getElementById("edit-name").value = ingredient.name;
            document.getElementById("edit-price").value = ingredient.price;
            document.getElementById("edit-inventory").value = ingredient.inventory;
            document.getElementById("edit-addOn").checked = ingredient.addOn;
            document.getElementById("edit-modal").style.display = "block";
        });
}

function closeEditModal() {
    document.getElementById("edit-modal").style.display = "none";
    currentIngredientId = null;
}

async function saveIngredient() {
    const updated = {
        name: document.getElementById("edit-name").value,
        price: parseFloat(document.getElementById("edit-price").value),
        inventory: parseInt(document.getElementById("edit-inventory").value),
        addOn: document.getElementById("edit-addOn").checked
    };

    try {
        const response = await fetch(`/api/ingredients/${currentIngredientId}`, {
            method: "PUT" ,
            headers: {"Content-Type": "application/json" },
            body: JSON.stringify(updated)
        });

        if (!response.ok) throw new Error("Kunne ikke opdatere ingrediens");

        closeEditModal();
        fetchIngredients();
    } catch (error) {
        console.error(error);
        alert("Kunne ikke gemme ændringer")
    }
}

async function deleteIngredient(id) {
    if (!confirm("Er du sikker på at du vil slette denne ingrediens?")) {
        return;
    }

    try {
        const response = await fetch(`/api/ingredients/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) throw new Error("Kunne ikke slette ingrediens");

        fetchIngredients();
    } catch (error) {
        console.error(error);
        alert("Kunne ikke slette ingrediens")
    }
}

function openCreateModal() {
    document.getElementById("create-name").value = "";
    document.getElementById("create-price").value = "";
    document.getElementById("create-inventory").value = "";
    document.getElementById("create-addOn").checked = false;
    document.getElementById("create-modal").style.display = "block";
}

function closeCreateModal() {
    document.getElementById("create-modal").style.display = "none";
}

async function createIngredient() {
    const ingredient = {
        name: document.getElementById("create-name").value,
        price: parseFloat(document.getElementById("create-price").value),
        inventory: parseInt(document.getElementById("create-inventory").value),
        addOn: document.getElementById("create-addOn").checked
    };

    try {
        const response = await fetch("/api/ingredients", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ingredient)
        });

        if (!response.ok) throw new Error("Kunne ikke oprette ingrediens");

        closeCreateModal();
        fetchIngredients();

    } catch (error) {
        console.error(error);
        alert("Kunne ikke oprette ingrediens");
    }
}

window.onclick = function (event) {
    if (event.target === document.getElementById("edit-modal")) {
        closeEditModal();
    }
    if (event.target === document.getElementById("create-modal")) {
        closeCreateModal();
    }
}

fetchIngredients();