async function fetchProducts() {

    try {

        const response = await fetch("/menu")

        if (!response.ok) {
            throw new Error("Could not fetch products");
        }

        const products = await response.json();

        renderMenu(products);

    } catch (error) {
        console.error(error);

        document.body.innerHTML += `<p>Kunne ikke hente menuen</p>`;

    }
}

function renderMenu(products) {

    const burgers = products.filter(product => product.category === "BURGER");

    const drinks = products.filter(product => product.category === "DRINK");

    const sides = products.filter(product => product.category === "SIDES");

    renderProducts(burgers, "burger-container");

    renderProducts(drinks, "drink-container");

    renderProducts(sides, "side-container")
}


function renderProducts(products, containerId) {
    const container = document.getElementById(containerId);

    console.log(containerId);
    console.log(container);

    container.innerHTML = "";

    products.forEach(product => {

        container.innerHTML += `
            <div class="product-card">

            <h3>${product.name}</h3>
            
            <p>${product.description}</p>
            
            <p>${product.price} kr.</p>
            
            <button onclick="showProduct(${product.id})">Se mere</button>
            <button onclick="showUpdateModal(${product.id})">Rediger</button>
        </div>`
    })
}

async function showProduct(id) {

    try {

        const response = await fetch(`/product/${id}`);

        if (!response.ok) {
            throw new Error("Could not fetch product");
        }

        const product = await response.json();

        showProductModal(product);
    } catch (error) {
        console.error(error);

        alert("Kunne ikke hente produkt")
    }
}

function showProductModal(product) {

    const ingredientList = product.ingredients
        .map(ingredient =>
            `<li>${ingredient}</li>`)
        .join("");

    const modal = document.getElementById("modal");

    modal.innerHTML =`
    <div class="modal-content">
        <span class="close" onclick="closeModal()"> &times;</span>
        
        <h2>${product.name}</h2>
        
        <p>${product.description}</p>
        
        <p>Pris: ${product.price} kr.</p>
        
        <p>Kategori: ${product.category}</p>
        
        <h3>Ingredienser</h3>
        
        <ul>
            ${ingredientList}
        </ul>
        
        </div>
    
    `;

    modal.style.display ="block";
}

function closeModal() {

    const modal = document.getElementById("modal");

    modal.style.display = "none";
}

async function showUpdateModal(id) {

    try {

        const response = await fetch(`/product/${id}`);

        if (!response.ok) {
            throw new Error("Could not fetch product")
        }

        const product = await response.json();
        const categories = await fetch("/categories").then(r => r.json());
        const ingredients = await fetch("/ingredients").then(r => r.json());

        const categoryOptions = categories.map(category => `
            <option value="${category}" ${product.category == category ? "selected" : ""}>
                ${category}
            </option>
        `).join("")


        const ingredientCheckboxes = ingredients.map(ingredient => `
        <label>
            <input type="checkbox" name="ingredients" value="${ingredient.id}"
                ${product.ingredients.includes(ingredient.name) ? "checked" : ""}>
            ${ingredient.name}
        </label>
        `).join("");

        const modal = document.getElementById("update-modal");

        modal.innerHTML = `
        <div class="modal-content">
                <span class="close" onclick="closeUpdateModal()">&times;</span>
                <h2>Rediger produkt</h2>
                <input type="text" id="update-name" value="${product.name}">
                <input type="text" id="update-description" value="${product.description}">
                <input type="number" id="update-price" value="${product.price}">
                <select id="update-category">${categoryOptions}</select>
                <label>
                    <input type="checkbox" id="update-lunchOffer" ${product.lunchOffer ? "checked" : ""}>
                    Frokosttilbud
                </label>
                <fieldset id="update-ingredients">
                    <legend>Ingredienser</legend>
                    ${ingredientCheckboxes}
                </fieldset>
                <button onclick="updateProduct(${product.id})">Gem ændringer</button>
            </div>
        `;

        modal.style.display = "block";

    } catch (error) {

        console.error(error);

        alert("Kunne ikke hente produkt")
    }
}


async function updateProduct(id) {

    const updatedProduct = {
        name: document.getElementById("update-name").value,
        description: document.getElementById("update-description").value,
        price: parseFloat(document.getElementById("update-price").value),
        category: document.getElementById("update-category").value,
        ingredients: [...document.querySelectorAll("input[name='ingredients']:checked")]
            .map(checkbox => parseInt(checkbox.value)),
        lunchOffer: document.getElementById("update-lunchOffer").checked
    };

    try {
        const response = await fetch(`/admin/product/update/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json"},
            body: JSON.stringify(updatedProduct)
        });

        if (!response.ok) {
            throw new Error("Could not update product");
        }

        closeUpdateModal();
        fetchProducts();

    } catch (error) {
        console.error(error);

        alert("Kunne ikke opdatere produkt")
    }
}

function closeUpdateModal() {
    const modal = document.getElementById("update-modal");
    modal.style.display = "none";
}

window.onclick = function (event) {
    const modal = document.getElementById("modal");
    const updateModal = document.getElementById("update-modal")

    if (event.target === modal) {
        modal.style.display = "none";
    }

    if (event.target === updateModal) {
        updateModal.style.display = "none";
    }
}

fetchProducts();