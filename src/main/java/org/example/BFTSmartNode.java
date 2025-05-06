package org.example;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static org.example.StringSerializer.deserializeStrings;
import static org.example.StringSerializer.serializeStrings;

@Service
public final class BFTSmartNode extends DefaultSingleRecoverable {
    ServiceReplica replica;
    private final AccountRepository accountRepository;

    public BFTSmartNode(ApplicationArguments args, AccountRepository accountRepository) {
        int serverId = Integer.parseInt(args.getNonOptionArgs().get(0));
        this.replica = new ServiceReplica(serverId, this, this);

        this.accountRepository = accountRepository;
    }

    @Override
    public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
        return super.executeOrdered(command, msgCtx);
    }

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        try {
            String[] deserialized = deserializeStrings(bytes);
            System.out.println("Received oreder: " + Arrays.toString(deserialized));

            String command = deserialized[0];
            String accountId = deserialized[1];
            String body = deserialized[2];
            String signature = deserialized[3];

            String response;
            switch (command) {
                case "createAccount": {
                    if (accountRepository.existsById(accountId)) {
                        response = "Account already exists!";
                        break;
                    }

                    Account account = new Account();

                    if(!verifySignature(body, signature, body))
                    {
                        response = "Invalid signature";
                        break;
                    }

                    account.setId(accountId);
                    account.setPublicKey(body);

                    accountRepository.save(account);

                    response = "Success";
                    break;
                }
                case "getBalance" : {
                    Account account = getAccount(accountId);
                    if(account == null)
                    {
                        response = "Account not found";
                        break;
                    }

                    if(!verifySignature(account.getPublicKey(), signature, body))
                    {
                        response = "Invalid signature";
                        break;
                    }

                    response = String.valueOf(accountRepository.findById(accountId).orElse(null).getBalance());
                    break;
                }
                default: {
                    response = String.valueOf(0);
                }
            }

            return serializeStrings(response);
        } catch (Exception e) {
            return ("ERROR: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    Account getAccount(String accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    boolean verifySignature(String publicKeyBase, String signature, String body) throws Exception {


        byte[] decodedSig = Base64.getDecoder().decode(signature);
        byte[] decodedPub = Base64.getDecoder().decode(publicKeyBase);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPub));

        Signature ecdsaVerify = java.security.Signature.getInstance("SHA256withECDSA");
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(body.getBytes());

        return ecdsaVerify.verify(decodedSig);
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        return new byte[0];
    }

    public void installSnapshot(byte[] state) {
        try {
            System.out.println("setState called");
            ByteArrayInputStream bis = new ByteArrayInputStream(state);
            ObjectInput in = new ObjectInputStream(bis);
            in.close();
            bis.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Error deserializing state: " + e.getMessage());
        }

    }

    public byte[] getSnapshot() {
        try {
            System.out.println("getState called");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.flush();
            bos.flush();
            out.close();
            bos.close();
            return bos.toByteArray();
        } catch (IOException ioe) {
            System.err.println("[ERROR] Error serializing state: " + ioe.getMessage());
            return "ERROR".getBytes();
        }
    }
}