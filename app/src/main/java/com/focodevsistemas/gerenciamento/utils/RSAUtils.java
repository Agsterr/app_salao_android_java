package com.focodevsistemas.gerenciamento.utils;

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAUtils {

    private static final String TAG = "RSAUtils";
    private static final String CRYPTO_METHOD = "RSA";
    private static final int KEY_SIZE = 2048;

    // Gera um par de chaves RSA
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(CRYPTO_METHOD);
            kpg.initialize(KEY_SIZE);
            return kpg.genKeyPair();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao gerar par de chaves RSA", e);
            return null;
        }
    }

    // Converte chave para String Base64 sem quebras de linha (NO_WRAP)
    public static String keyToBase64(Key key) {
        if (key == null) return null;
        return Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);
    }

    // Converte String Base64 para Chave Pública
    public static PublicKey base64ToPublicKey(String base64PublicKey) {
        try {
            byte[] decoded = Base64.decode(base64PublicKey, Base64.NO_WRAP);
            KeyFactory keyFactory = KeyFactory.getInstance(CRYPTO_METHOD);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter Base64 para PublicKey", e);
            return null;
        }
    }

    // Converte String Base64 para Chave Privada
    public static PrivateKey base64ToPrivateKey(String base64PrivateKey) {
        try {
            byte[] decoded = Base64.decode(base64PrivateKey, Base64.NO_WRAP);
            KeyFactory keyFactory = KeyFactory.getInstance(CRYPTO_METHOD);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter Base64 para PrivateKey", e);
            return null;
        }
    }

    // Encripta texto plano usando chave pública
    public static String encrypt(String plainText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao encriptar dados", e);
            return null;
        }
    }

    // Decripta texto encriptado (Base64) usando chave privada
    public static String decrypt(String encryptedBase64, PrivateKey privateKey) {
        try {
            byte[] encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao decriptar dados", e);
            return null;
        }
    }
    
    // Método utilitário para gerar e logar chaves para o desenvolvedor copiar
    public static void logNewKeyPair() {
        KeyPair kp = generateKeyPair();
        if (kp != null) {
            String pub = keyToBase64(kp.getPublic());
            String priv = keyToBase64(kp.getPrivate());
            
            Log.d(TAG, "=== NOVAS CHAVES RSA GERADAS ===");
            Log.d(TAG, "PublicKey (Base64 NO_WRAP): " + pub);
            Log.d(TAG, "PrivateKey (Base64 NO_WRAP): " + priv);
            Log.d(TAG, "================================");
        }
    }

    /*
     * EXEMPLO DE USO (após obter as chaves do Logcat):
     * 
     * String publicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."; // Cole sua chave aqui
     * PublicKey key = RSAUtils.base64ToPublicKey(publicKeyBase64);
     * String encryptedData = RSAUtils.encrypt("Dados sensíveis", key);
     */
}
