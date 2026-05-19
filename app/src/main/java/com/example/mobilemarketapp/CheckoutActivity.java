package com.example.mobilemarketapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class CheckoutActivity extends AppCompatActivity {

    // Form input views
    EditText    addressInput, phoneInput;
    RadioGroup  paymentGroup;
    TextView    checkoutTotalText;
    Button      placeOrderBtn;

    String loggedInEmail;
    double cartTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        loggedInEmail = getSharedPreferences("app", MODE_PRIVATE).getString("email", "");

        addressInput      = findViewById(R.id.addressInput);
        phoneInput        = findViewById(R.id.phoneInput);
        paymentGroup      = findViewById(R.id.paymentGroup);
        checkoutTotalText = findViewById(R.id.checkoutTotalText);
        placeOrderBtn     = findViewById(R.id.placeOrderBtn);

        loadTotalFromServer();

        placeOrderBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();
            String phone   = phoneInput.getText().toString().trim();

            if (address.isEmpty()) {
                Toast.makeText(this, "Enter delivery address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter phone number!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPaymentId = paymentGroup.getCheckedRadioButtonId();
            if (selectedPaymentId == -1) {
                Toast.makeText(this, "Select a payment method!", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedPayment = findViewById(selectedPaymentId);
            String paymentMethod = selectedPayment.getText().toString();

            placeOrderBtn.setEnabled(false);
            new Thread(() -> {
                try {
                    String json = "{\"userEmail\":\"" + loggedInEmail + "\"}";
                    String response = ApiClient.post("checkout.php", json);
                    JSONObject obj  = new JSONObject(response);
                    boolean success = obj.optBoolean("success", false);

                    runOnUiThread(() -> {
                        placeOrderBtn.setEnabled(true);
                        if (success) {
                            Toast.makeText(
                                    this,
                                    "Order placed! 🎉\nPayment: " + paymentMethod,
                                    Toast.LENGTH_LONG
                            ).show();
                            // Go back to the main screen and clear the back stack
                            Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            String msg = obj.optString("message", "Checkout failed");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        placeOrderBtn.setEnabled(true);
                        Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }

    private void loadTotalFromServer() {
        checkoutTotalText.setText("Loading total...");

        new Thread(() -> {
            try {
                String response = ApiClient.get("get_cart.php?userEmail=" + loggedInEmail);
                JSONObject obj  = new JSONObject(response);

                if (!obj.has("items")) {
                    runOnUiThread(() -> checkoutTotalText.setText("Total: R 0.00"));
                    return;
                }

                JSONArray arr = obj.getJSONArray("items");
                double total = 0.0;
                for (int i = 0; i < arr.length(); i++) {
                    total += arr.getJSONObject(i).optDouble("price", 0.0);
                }

                final double finalTotal = total;
                runOnUiThread(() ->
                        checkoutTotalText.setText("Total: R " + String.format("%.2f", finalTotal))
                );

            } catch (Exception e) {
                runOnUiThread(() -> checkoutTotalText.setText("Total: R 0.00"));
            }
        }).start();
    }
}
