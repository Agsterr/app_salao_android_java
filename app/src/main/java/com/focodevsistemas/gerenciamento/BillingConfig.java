package com.focodevsistemas.gerenciamento;

/**
 * Configurações de Faturamento (Billing)
 * Contém chaves e IDs de produtos.
 */
public class BillingConfig {
    
    // Chave pública RSA da Google Play Console
    // IMPORTANTE: Mantenha esta chave segura. Não envie para repositórios públicos.
    public static final String GOOGLE_PLAY_PUBLIC_KEY = 
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuNdEgJC20D8f+HuVc7SRSgDtC4gF0P1jYLRR3nOjhHDmS/9yJM4JwI9MsebGbyPDkN3hDZI6QAGiofdYWs+qiM9qkpRKqv7iOD9BKowS7UEMKZhDeXwKStfUDtpTxfBIX8XNqaRsp9t1pUhmlfGy4J6prZDjKPGP9/O7HYqbSeAxYunUqzbcvu13HuBMX6d1VLSIp+tTo6SXstDoA7WHPMLh/dxUinJHBHgL+pgvt8mTyaMotMEVimm81QH2hakYEQg8Dpjn+JYtiUtkrfPTumfBEb4Qa4Fk+mdTTJFjfMPBofrV6iQctUsCB+hqUKIanDfR61t57GpfoOG7cTZUDwIDAQAB";

    // ID do produto de assinatura mensal (deve corresponder exatamente ao Play Console)
    public static final String PRODUCT_ID_MONTHLY = "focodev_mensal";
}
