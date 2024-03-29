/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2018 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.googlepay;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiLifecycle;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@RequiresApi(api = Build.VERSION_CODES.N)
@Kroll.module(name = "TiGooglepay", id = "ti.googlepay")
public class TiGooglepayModule extends KrollModule implements TiLifecycle.OnActivityResultEvent {

    public static final BigDecimal CENTS_IN_A_UNIT = new BigDecimal(100d);
    // You can define constants with @Kroll.constant, for example:
    @Kroll.constant
    public static final String PAYMENT_GATEWAY_STRIPE = "stripe";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_VISA = "VISA";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_MASTERCARD = "MASTERCARD";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_DISCOVER = "DISCOVER";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_AMEX = "AMEX";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_INTERAC = "INTERAC";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_MIR = "MIR";
    @Kroll.constant
    public static final String PAYMENT_NETWORK_JCB = "JCB";
    @Kroll.constant
    public static final int ENVIRONMENT_PRODUCTION = WalletConstants.ENVIRONMENT_PRODUCTION;
    @Kroll.constant
    public static final int ENVIRONMENT_TEST = WalletConstants.ENVIRONMENT_TEST;


    private static final String LCAT = "TiGooglepayModule";
    private static final boolean DBG = TiConfig.LOGD;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    static String countryCode = "DE";
    static String currencyCode = "EUR";
    static String merchantName = "Example Merchant";
    static String gatewayName = "stripe";
    static String gatewayApikey = "";
    static JSONArray supportedNetworks = new JSONArray();
    static JSONObject shippingAddressParameters = null;
    static int envToken = WalletConstants.ENVIRONMENT_TEST;
    PaymentsClient paymentsClient;
    Activity payActivity = null;
    PaymentDataRequest paymentDataRequest;

    public TiGooglepayModule() {
        super();
    }

    private static JSONObject getBaseRequest() throws JSONException {
        return new JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0);
    }

    private static JSONObject getGatewayTokenizationSpecification() throws JSONException {
        return new JSONObject() {{
            put("type", "PAYMENT_GATEWAY");
            put("parameters", new JSONObject() {{
                put("gateway", gatewayName);
                if (gatewayName.equals("stripe")) {
                    put("stripe:version", "2018-10-31");
                    put("stripe:publishableKey", gatewayApikey);
                } else {
                    put("gatewayMerchantId", gatewayApikey);
                }
            }});
        }};
    }

    private static JSONArray getAllowedCardAuthMethods() {
        return new JSONArray()
                .put("PAN_ONLY")
                .put("CRYPTOGRAM_3DS");
    }

    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder().setEnvironment(envToken).build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }

    private static JSONObject getCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = getBaseCardPaymentMethod();
        cardPaymentMethod.put("tokenizationSpecification", getGatewayTokenizationSpecification());
        return cardPaymentMethod;
    }

    public static Optional<JSONObject> getIsReadyToPayRequest() {
        try {
            JSONObject isReadyToPayRequest = getBaseRequest();
            isReadyToPayRequest.put("allowedPaymentMethods", new JSONArray().put(getBaseCardPaymentMethod()));
            return Optional.of(isReadyToPayRequest);

        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    private static JSONObject getTransactionInfo(String price) throws JSONException {
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", price);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("countryCode", countryCode);
        transactionInfo.put("currencyCode", currencyCode);
        transactionInfo.put("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE");
        return transactionInfo;
    }

    private static JSONObject getMerchantInfo() throws JSONException {
        return new JSONObject().put("merchantName", merchantName);
    }

    private static JSONObject getBaseCardPaymentMethod() throws JSONException {
        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedAuthMethods", getAllowedCardAuthMethods());
        parameters.put("allowedCardNetworks", supportedNetworks);
        // Optionally, you can add billing address/phone number associated with a CARD payment method.
        //parameters.put("billingAddressRequired", true);

        JSONObject billingAddressParameters = new JSONObject();
        billingAddressParameters.put("format", "FULL");

        parameters.put("billingAddressParameters", billingAddressParameters);

        cardPaymentMethod.put("parameters", parameters);
        return cardPaymentMethod;
    }

    public static String centsToString(long cents) {
        return new BigDecimal(cents)
                .divide(CENTS_IN_A_UNIT, RoundingMode.HALF_EVEN)
                .setScale(2, RoundingMode.HALF_EVEN)
                .toString();
    }

    public static Optional<JSONObject> getPaymentDataRequest(long priceCents) {

        final String price = centsToString(priceCents);

        try {
            JSONObject paymentDataRequest = getBaseRequest();
            paymentDataRequest.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
            paymentDataRequest.put("transactionInfo", getTransactionInfo(price));
            paymentDataRequest.put("merchantInfo", getMerchantInfo());

            paymentDataRequest.put("shippingAddressRequired", false);
            if (shippingAddressParameters != null) {
                paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters);
            }
            return Optional.of(paymentDataRequest);

        } catch (JSONException e) {
            Log.e(LCAT, "Error: " + e);
            return Optional.empty();
        }
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
    }

    private void possiblyShowGooglePayButton() {
        KrollDict kd = new KrollDict();

        if (payActivity == null) {
            init();
        }
        final Optional<JSONObject> isReadyToPayJson = getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            kd.put("success", false);
            kd.put("message", "no payment request found");
            fireEvent("available", kd);
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(TiApplication.getAppCurrentActivity(),
                task1 -> {
                    if (task1.isSuccessful()) {
                        kd.put("message", task1.getResult());
                        kd.put("success", true);
                    } else {
                        kd.put("success", false);
                        kd.put("message", task1.getException().getMessage());
                        Log.e(LCAT, "" + task1.getException());
                    }
                    fireEvent("available", kd);
                });
    }

    private void init() {
        payActivity = TiApplication.getInstance().getCurrentActivity();
        paymentsClient = createPaymentsClient(payActivity);
        TiBaseActivity baseActivity = (TiBaseActivity) TiApplication.getInstance().getCurrentActivity();
        baseActivity.addOnActivityResultListener(this);
    }


    @Kroll.method
    public void isAvailable() {
        possiblyShowGooglePayButton();
    }

    @Kroll.method
    public void setupPaymentGateway(KrollDict kd) {
        //if (payActivity == null) {
        init();
        //}
        gatewayName = kd.getString("name");
        gatewayApikey = kd.getString("apiKey");
    }

    @Kroll.method
    public void createPaymentRequest(KrollDict kd) throws JSONException {

        envToken = TiConvert.toInt(kd.get("environment"), WalletConstants.ENVIRONMENT_PRODUCTION);
        countryCode = kd.getString("countryCode");
        currencyCode = kd.getString("currencyCode");
        merchantName = kd.getString("merchantName");
        int price = kd.getInt("price");
        String[] cards = TiConvert.toStringArray((Object[]) kd.get("supportedNetworks"));
        //KrollDict shippingContact = (KrollDict) kd.get("shippingContact");
        for (String card : cards) {
            supportedNetworks.put(card);
        }

        if (envToken == ENVIRONMENT_TEST) {
            Log.i(LCAT, "# Using test environment #");
        }

        /*
        shippingAddressParameters= new JSONObject();
        shippingAddressParameters.put("phoneNumberRequired", false);
        JSONArray allowedCountryCodes = new JSONArray("DE");
        shippingAddressParameters.put("allowedCountryCodes", allowedCountryCodes);
        */

        Optional<JSONObject> paymentDataRequestJson = getPaymentDataRequest(price);
        if (!paymentDataRequestJson.isPresent()) {
            KrollDict kdEvent = new KrollDict();
            kdEvent.put("message", "no payment data request");
            fireEvent("error", kdEvent);
            return;
        }

        paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
        if (paymentDataRequest == null) {
            KrollDict kdEvent = new KrollDict();
            kdEvent.put("message", "no payment request");
            fireEvent("error", kdEvent);
            return;
        }

        KrollDict kdEvent = new KrollDict();
        kdEvent.put("message", "payment ready");
        kdEvent.put("code", 1);
        fireEvent("ready", kdEvent);
    }

    @Kroll.method
    public void doPayment() {
        Activity activity = TiApplication.getAppCurrentActivity();
        if (paymentDataRequest != null && payActivity != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(paymentDataRequest),
                    activity, LOAD_PAYMENT_DATA_REQUEST_CODE);
        } else {
            Log.e(LCAT, "Initialize payment first");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                KrollDict kd = new KrollDict();
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        kd.put("info", paymentData.toJson());
                        fireEvent("success", kd);
                        break;

                    case Activity.RESULT_CANCELED:
                        fireEvent("canceled", kd);
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        kd.put("status", status.getStatusCode());
                        fireEvent("error", kd);
                        break;
                }
        }
    }
}

