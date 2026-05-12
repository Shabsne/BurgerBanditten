async function addToCart(productId) {

    const response = await fetch('/api/cart/1/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            productId: productId,
            quantity: 1
        })
    });

    const data = await response.json();

    console.log(data);
}