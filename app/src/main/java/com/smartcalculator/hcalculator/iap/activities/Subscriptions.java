package com.smartcalculator.hcalculator.iap.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.smartcalculator.hcalculator.R;
import com.smartcalculator.hcalculator.iap.adapters.ProductDetailsAdapter;
import com.smartcalculator.hcalculator.iap.interfaces.RecycleViewInterface;
import com.smartcalculator.hcalculator.iap.utilies.Prefs;

import java.util.ArrayList;
import java.util.List;

public class Subscriptions extends AppCompatActivity implements RecycleViewInterface {
    Prefs prefs;
    private BillingClient billingClient;
    List<ProductDetails> productDetailsList;
    ProgressBar loadProducts;
    RecyclerView recyclerView;
    Handler handler;
    ExtendedFloatingActionButton btn_restore_fab;
    ProductDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);
        initViews();
        handler = new Handler();
        prefs = new Prefs(this);
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase : list) {
                                    verifySubPurchase(purchase);
                                }
                            }
                        }
                ).build();
        establishConnection();
        btn_restore_fab.setOnClickListener(v -> {
            restorePurchases();
        });

    }

    void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    showProducts();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                establishConnection();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    void showProducts() {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("one_year")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, prodDetailsList) -> {
                    productDetailsList.clear();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadProducts.setVisibility(View.INVISIBLE);
                            productDetailsList.addAll(prodDetailsList);
                            adapter = new ProductDetailsAdapter(getApplicationContext(), productDetailsList, (RecycleViewInterface) Subscriptions.this);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(Subscriptions.this, LinearLayoutManager.VERTICAL, false));
                            recyclerView.setAdapter(adapter);
                        }
                    }, 2000);
                }
        );
    }

    void launchPurchaseFlow(ProductDetails productDetails) {
        assert productDetails.getSubscriptionOfferDetails() != null;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(this, billingFlowParams);
    }

    protected void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            verifySubPurchase(purchase);
                        }
                    }
                }
        );
    }

    void verifySubPurchase(Purchase purchases) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Toast.makeText(Subscriptions.this, "Subscription activated, Enjoy!", Toast.LENGTH_SHORT).show();
                prefs.setPremium(1);
                startActivity(new Intent(this, IAPActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        productDetailsList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        btn_restore_fab = findViewById(R.id.fab);
        loadProducts = findViewById(R.id.loadProducts);
    }

    @Override
    public void onItemClick(int pos) {
        launchPurchaseFlow(productDetailsList.get(pos));
    }

    void restorePurchases() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener((billingResult, list) -> {
        }).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                                    if (list.size() > 0) {
                                        prefs.setPremium(1);
                                        showSnackbar(btn_restore_fab, "Successfully restored");
                                        Toast.makeText(Subscriptions.this, "Successfully restored", Toast.LENGTH_SHORT).show();
                                    } else {
                                        showSnackbar(btn_restore_fab, "Oops, No purchase found.");
                                        Toast.makeText(Subscriptions.this, "Oops, No purchase found.", Toast.LENGTH_SHORT).show();
                                        prefs.setPremium(0);
                                    }
                                }
                            });
                }
            }
        });
    }

    public void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

}