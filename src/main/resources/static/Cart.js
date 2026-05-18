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

async function checkPreorderBeforeCheckout() {
    try {
        const response = await fetch("/opening-hours/next");
        const data = await response.json();

        const isOpen = data.openNow;
        const hasPreorderedTime = sessionStorage.getItem("preorderedPickupTime");

        if (!isOpen && !hasPreorderedTime) {
            window.preOrderModal.showPreOrderPopup(data);
            return false;
        }

        return true;
    } catch (error) {
        console.error("Kunne ikke tjekke åbningstid:", error);
        return true;
    }
}

async function handleCheckoutClick() {
    const canCheckout = await checkPreorderBeforeCheckout();

    if (!canCheckout) {
        return;
    }

    window.location.href = "/checkout.html";
}