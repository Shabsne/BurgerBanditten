async function loadCategories() {

    try {
        const response =
            await fetch("/categories");

        if (!response.ok) {
            throw new Error("Could not fetch categories");
        }

        const categories = await response.json();

        const categorySelect = document.getElementById("category");

        categorySelect.innerHTML = "";

        categories.forEach(category => {
            categorySelect.innerHTML += `
            
                <option value="${category}"> ${category} </option>
                
            `;
        });
    } catch (error) {

        console.error(error);
    }
}

async function loadIngredients() {

    try {

        const response = await fetch("/ingredients");

        if (!response.ok) {
            throw new Error("Could not fetch ingredients")
        }

        const ingredients = await response.json();

        const container = document.getElementById("ingredients-container");

        ingredients.forEach(ingredient => {
            container.innerHTML += `
                <label>
                    <input type="checkbox" name="ingredients" value="${ingredient.id}">
                    ${ingredient.name}
                </label>
                `;
        });
    } catch (error) {
        console.error(error);
    }
}

async function createProduct(event) {

    event.preventDefault();

    const fileInput = document.getElementById("image");
    const file = fileInput.files[0];
    const image = file ? await new Promise(resolve => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result);
        reader.readAsDataURL(file);
    }) : null;

    const product = {

        name:
        document.getElementById("name").value,

        description:
        document.getElementById("description").value,

        price:
            parseFloat(
                document.getElementById("price").value
            ),

        category:
        document.getElementById("category").value,

        ingredients: [...document.querySelectorAll("input[name='ingredients']:checked")]
            .map(checkbox => checkbox.value),

        lunchOffer: false,

        image: image


    };

    try {

        const response = await fetch("/admin/product/create", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(product)
        });

        if (!response.ok) {
            throw new Error("Could not create product")
        }

        document.getElementById("product-form").reset();

    } catch (error) {
        console.error(error);
    }
}


document.getElementById("product-form")
    .addEventListener("submit", createProduct);

loadCategories();
loadIngredients()


