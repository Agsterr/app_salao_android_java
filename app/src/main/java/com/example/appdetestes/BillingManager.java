package com.example.appdetestes;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.QueryProductDetailsResult;

import java.util.Collections;
import java.util.List;

public class BillingManager {

    private static final String TAG = "BillingManager";
    private final BillingClient billingClient;
    private final Context context;
    private final BillingReadyListener readyListener;

    // Interface para comunicar o resultado da verificação da assinatura
    public interface SubscriptionVerificationListener {
        void onVerificationResult(boolean isSubscribed);
    }

    // Interface para notificar quando o BillingClient está pronto
    public interface BillingReadyListener {
        void onBillingClientReady();
    }

    public BillingManager(Context context, BillingReadyListener readyListener) {
        this.context = context;
        this.readyListener = readyListener;

        // Listener para atualizações de compras
        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    // TODO: Processar a compra: validar no backend e conceder o acesso
                    Log.d(TAG, "Compra bem-sucedida: " + purchase.getOrderId());
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.d(TAG, "Usuário cancelou a compra.");
            } else {
                Log.e(TAG, "Erro na compra. Código: " + billingResult.getResponseCode());
            }
        };

        // Parâmetros para compras pendentes
        PendingPurchasesParams pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .build();

        // Criação do cliente de faturamento
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(pendingPurchasesParams)
                .build();

        startConnection();
    }

    private void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Conexão com Google Play Billing estabelecida.");
                    // Notifica o listener que o cliente está pronto
                    if (readyListener != null) {
                        readyListener.onBillingClientReady();
                    }
                } else {
                    Log.e(TAG, "Falha ao conectar com Google Play Billing. Código: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Tente reconectar se a conexão for perdida
                Log.w(TAG, "Conexão com Google Play Billing perdida. Tentando reconectar...");
                startConnection();
            }
        });
    }

    public void verificarAssinaturaAtiva(SubscriptionVerificationListener listener) {
        if (!billingClient.isReady()) {
            Log.e(TAG, "BillingClient não está pronto para verificar assinaturas.");
            listener.onVerificationResult(false);
            return;
        }

        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                // TODO: Adicionar validação de backend aqui para segurança máxima
                                listener.onVerificationResult(true);
                                return;
                            }
                        }
                    }
                    // Nenhuma assinatura ativa encontrada
                    listener.onVerificationResult(false);
                }
        );
    }

    public void queryAndLaunchBillingFlow(Activity activity, String productId) {
        if (billingClient.isReady()) {
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(
                            Collections.singletonList(
                                    QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId(productId)
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build()))
                    .build();

            billingClient.queryProductDetailsAsync(params, (billingResult, queryProductDetailsResult) -> {
                List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.isEmpty()) {
                    ProductDetails productDetails = productDetailsList.get(0);
                    launchBillingFlow(activity, productDetails);
                } else {
                    Log.e(TAG, "Falha ao consultar detalhes do produto. Código: " + billingResult.getResponseCode());
                }
            });
        } else {
            Log.e(TAG, "BillingClient não está pronto.");
        }
    }

    private void launchBillingFlow(Activity activity, ProductDetails productDetails) {
        // Certifique-se de que há ofertas de assinatura disponíveis
        if (productDetails.getSubscriptionOfferDetails() == null || productDetails.getSubscriptionOfferDetails().isEmpty()) {
            Log.e(TAG, "Nenhuma oferta de assinatura encontrada para o produto.");
            return;
        }

        // Pegando o token da primeira oferta (geralmente a oferta base)
        String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }
}
