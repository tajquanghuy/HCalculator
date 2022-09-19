package com.smartcalculator.hcalculator.iap.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;
import com.smartcalculator.hcalculator.R;
import com.smartcalculator.hcalculator.iap.adapters.BuyCoinsAdapter;
import com.smartcalculator.hcalculator.iap.interfaces.RecycleViewInterface;
import com.smartcalculator.hcalculator.iap.utilies.Prefs;

import java.util.ArrayList;
import java.util.List;

public class BuyCoinActivity extends AppCompatActivity implements RecycleViewInterface {

    BillingClient billingClient;
    RecyclerView recyclerView;
    List<ProductDetails> productDetailsList;
    Activity activity;
    Prefs prefs;
    Handler handler;
    ProgressBar loadProducts;
    BuyCoinsAdapter adapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_coin);
        initViews();
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase : list) {
                                    verifyPurchase(purchase);
                                }
                            }
                        }
                ).build();
        connectGooglePlayBilling();
    }

    private void initViews() {
        handler = new Handler();
        activity = this;
        prefs = new Prefs(this);
        recyclerView = findViewById(R.id.recyclerview);
        loadProducts = findViewById(R.id.loadProducts);
        productDetailsList = new ArrayList<>();
    }

    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    showProducts();
                }
            }
        });
    }

    void showProducts() {
        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("10_coins_id")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),

                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("20_coins_id")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),

                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("50_coins_id")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),

                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("100_coins_id")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),

                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("200_coins_id")
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            productDetailsList.clear();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadProducts.setVisibility(View.INVISIBLE);
                    productDetailsList.addAll(list);
                    adapter = new BuyCoinsAdapter(getApplicationContext(), productDetailsList, BuyCoinActivity.this);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(BuyCoinActivity.this, LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(adapter);
                }
            }, 2000);
        });
    }


    void launchPurchaseFlow(ProductDetails productDetails) {
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }


    void verifyPurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

    protected void onResume() {
        super.onResume();
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            verifyPurchase(purchase);
                        }
                    }
                }
        );
    }

    @Override
    public void onItemClick(int pos) {
        launchPurchaseFlow(productDetailsList.get(pos));
    }
}