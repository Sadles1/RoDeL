package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.example.StringSerializer.deserializeStrings;

public class LedgerClientApplication {
    private static String baseUrl;

    static String userIdBase = "9c2403b0d457c2d7e76b76cabdacab1880881e7e05e3fde22404b8aaf480e4e64d466b77457759484b6f5a497a6a3043415159494b6f5a497a6a3044415163445167414577415a63374a46463678746e4b504f32722b43715046743575355764524c2b7668596943524b494645687a786d346331503873626b6c3162464b3773616b314351613159384465673474613958556c574268373857413d3d";
    static String accountIdBase = "c6267152cef06afb17d549b6bcad6eba025347c3720a87c4b791568eb5b4be1d";
    static String privateKeyBase = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCCjytQhTm/Ik/4EfpqCyb7py7HLVahc/yOCiqKjYYBS7g==";

    static String destinationAccountIdBase = "74e11a21bf1d0837ceba53f9f7fa6db756325a4dd4ced7b03af7d30d51814e05";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        baseUrl = "https://" + host + ":" + port + "/api";

        //This remove client TLS certificate check, only for development.
        //todo: Fix before production
        disableCertificateCheck();

        createUser("test_user_1@gmail.com");

       /* createAccount(userIdBase, privateKeyBase);
        createAccount(userIdBase, privateKeyBase);*/
/*
        loadMoney(accountIdBase, privateKeyBase, 1000);
        getBalance(destinationAccountIdBase, privateKeyBase);

        getGlobalLedgerValue(userIdBase, privateKeyBase);

        sendTransaction(accountIdBase, privateKeyBase, destinationAccountIdBase, 150.0);
        sendTransaction(accountIdBase, privateKeyBase, destinationAccountIdBase, 250.0);
        sendTransaction(destinationAccountIdBase, privateKeyBase, accountIdBase, 100.5);

        getBalance(accountIdBase, privateKeyBase);
        getBalance(destinationAccountIdBase, privateKeyBase);*/

        //getExtract(accountIdBase, privateKeyBase);
        //getLedger(userIdBase, privateKeyBase);
    }

    private static void createUser(String email) throws Exception {
        KeyPair keyPair = KeyPairUtil.generateECDSAKeyPair();
        String publicKey = KeyPairUtil.encodePublicKey(keyPair.getPublic());
        String privateKey = KeyPairUtil.encodePrivateKey(keyPair.getPrivate());

        System.out.println("Public Key: " + publicKey);
        System.out.println("Private Key: " + privateKey);

        ResponseEntity response = MakeRequest("/createUser", email, publicKey, privateKey);
        System.out.println(response.getBody());
    }

    private static void createAccount(String userId, String privateKey) throws Exception {
        String body = "verify";
        ResponseEntity response = MakeRequest("/createAccount", userId, body, privateKey);
        System.out.println(response.getBody());
    }

    private static void getBalance(String accountId, String privateKey) throws Exception {
        String body = "verify";
        ResponseEntity response = MakeRequest("/getBalance", accountId, body, privateKey);
        System.out.println(response.getBody());
    }

    private static void getGlobalLedgerValue(String userId, String privateKey) throws Exception {
        String body = "verify";
        ResponseEntity response = MakeRequest("/getGlobalLedgerValue", userId, body, privateKey);
        System.out.println(response.getBody());
    }

    private static void loadMoney(String accountId, String privateKey, double amount) throws Exception {
        String body = String.valueOf(amount);
        ResponseEntity response = MakeRequest("/loadMoney", accountId, body, privateKey);
        System.out.println(response.getBody());
    }

    private static void sendTransaction(String accountId, String privateKey, String destinationAccountId, double amount) throws Exception {
        Transaction transaction = new Transaction();
        transaction.setSource(accountId);
        transaction.setDestination(destinationAccountId);
        transaction.setAmount(amount);

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(transaction);

        ResponseEntity response = MakeRequest("/sendTransaction", accountId, body, privateKey);
        System.out.println(response.getBody());
    }

    private static void getExtract(String accountId, String privateKey) throws Exception {
        String body = "verify";
        ResponseEntity response = MakeRequest("/getExtract", accountId, body, privateKey);
        System.out.println(response.getBody());
    }

    private static void getLedger(String userId, String privateKey) throws Exception {
        String body = "verify";
        ResponseEntity response = MakeRequest("/getLedger", userId, body, privateKey);
        System.out.println(response.getBody());
    }


    private static ResponseEntity MakeRequest(String Request, String id, String body, String privateKey) throws Exception {
        String signedBase = signBody(body, privateKey);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Id", id);
        headers.add("Body", body);
        headers.add("Signature", signedBase);

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        return restTemplate.exchange(baseUrl + Request, HttpMethod.GET, entity, String.class);
    }


    private static String signBody(String body, String signPrivateKey) throws Exception {
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
        HostnameVerifier validHosts = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
    }
}
