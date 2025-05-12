package org.example;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyPairUtil {

    public static KeyPair generateECDSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"));
        return keyGen.generateKeyPair();
    }

    public static String encodePublicKey(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String encodePrivateKey(PrivateKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String signBody(String body, String signPrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(signPrivateKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(body.getBytes());
        byte[] signedData = signature.sign();

        return Base64.getEncoder().encodeToString(signedData);
    }

    public static boolean verifySignature(String publicKeyBase, String signature, String body) {
        try {
            byte[] decodedSig = Base64.getDecoder().decode(signature);
            byte[] decodedPub = Base64.getDecoder().decode(publicKeyBase);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPub));

            Signature ecdsaVerify = java.security.Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(body.getBytes());

            return ecdsaVerify.verify(decodedSig);
        }
        catch (Exception e) {
            return false;
        }
    }
}