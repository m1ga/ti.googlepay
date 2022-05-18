const win = Ti.UI.createWindow();
const btn = Ti.UI.createButton({
	title: "test payment"
});
win.add(btn);
win.open();

var gpay = require("ti.googlepay");
gpay.addEventListener("available", function(e) {
	console.log(e.success);
})
gpay.addEventListener("success", function(e) {
	console.log("success:");
	data = JSON.parse(e.info);
	console.log(data.paymentMethodData.tokenizationData.token);
})
gpay.addEventListener("canceled", function(e) {
	console.log("cancel");
})
gpay.addEventListener("error", function(e) {
	console.log("status");
})

btn.addEventListener("click", e => {
	gpay.setupPaymentGateway({
		name: gpay.PAYMENT_GATEWAY_STRIPE,
		apiKey: 'API_KEY'
	});

	gpay.createPaymentRequest({
		environment: gpay.ENVIRONMENT_TEST,
		price: 1000,
		countryCode: "DE",
		currencyCode: "EUR",
		merchantName: "Test User",
		supportedNetworks: [gpay.PAYMENT_NETWORK_VISA, gpay.PAYMENT_NETWORK_MASTERCARD]
	});
});
