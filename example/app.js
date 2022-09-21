var gpay = require("ti.googlepay");
var isReady = false;
const win = Ti.UI.createWindow();
const btn = Ti.UI.createButton({title: "test payment"});
win.add(btn);
win.addEventListener("open", function(e) {
	// prepare payment
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

	gpay.isAvailable();

})
win.open();

gpay.addEventListener("available", function(e) {
	console.log("available", e.success);
	console.log(e.message);
});

gpay.addEventListener("success", function(e) {
	console.log("success:");
	data = JSON.parse(e.info);
	console.log(data.paymentMethodData.tokenizationData.token);
});

gpay.addEventListener("canceled", function(e) {
	console.log("cancel");
});

gpay.addEventListener("error", function(e) {
	console.log("error");
	console.log(e.message);
});

gpay.addEventListener("ready", function(e) {
	console.log("ready");
	isReady = true;
});

btn.addEventListener("click", e => {
	if (isReady) {
		gpay.doPayment();
	} else {
		alert("Payment is not ready")
	}
});
