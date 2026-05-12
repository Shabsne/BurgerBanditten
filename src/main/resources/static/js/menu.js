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
            
            <button onclick="showProduct(${product.id})">
                Se mere
            </button>
        
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
        
        <p>Størelse: ${product.size}</p>
        
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

window.onclick = function (event) {
    const modal = document.getElementById("modal");

    if (event.target === modal) {
        modal.style.display = "none";
    }
}

fetchProducts();