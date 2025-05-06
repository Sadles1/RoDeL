package org.example;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class LedgerClientApplication {
    private static String baseUrl;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        baseUrl = "https://" + host + ":" + port + "/api";

        //This remove TLS certificate check, only for development.
        //todo: Fix before production
        disableCertificateCheck();

        createAccount("test_user_1");
    }

    private static void createAccount(String newUserId) throws Exception {
        KeyPair keyPair = KeyPairUtil.generateECDSAKeyPair();
        String publicKey = KeyPairUtil.encodePublicKey(keyPair.getPublic());
        String privateKey = KeyPairUtil.encodePrivateKey(keyPair.getPrivate());

        System.out.println("Public Key: " + publicKey);
        System.out.println("Private Key: " + privateKey);

        String signedBase = signBody(publicKey, privateKey);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("UserId", newUserId);
        headers.add("Body", publicKey);
        headers.add("Signature", signedBase);

        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity response = restTemplate.exchange(baseUrl + "/createAccount", HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
    }

    private static void getBalance(String userId, String privateKeyBase) throws Exception {
        String body = "verify";

        String signedBase = signBody(body, privateKeyBase);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("UserId", userId);
        headers.add("Body", body);
        headers.add("Signature", signedBase);

        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity response = restTemplate.exchange(baseUrl + "/getBalance", HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
    }

    private static String signBody(String body, String signPrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(signPrivateKey);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(body.getBytes()); // Данные, которые мы подписываем (например, строка с запросом)
        byte[] signedData = signature.sign(); // Подписанные данные

        return Base64.getEncoder().encodeToString(signedData);
    }

    private static void disableCertificateCheck() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }
                }
        };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier validHosts = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };
        // All hosts will be valid
        HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
    }
}
