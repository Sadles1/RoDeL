package org.example;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.util.*;

import static org.example.KeyPairUtil.verifySignature;
import static org.example.StringSerializer.deserializeStrings;
import static org.example.StringSerializer.serializeStrings;

@Service
public final class BFTSmartNode extends DefaultSingleRecoverable {
    ServiceReplica replica;
    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    public BFTSmartNode(ApplicationArguments args, AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;

        int serverId = Integer.parseInt(args.getNonOptionArgs().get(0));
        this.replica = new ServiceReplica(serverId, this, this);
    }

    @Override
    public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
        return super.executeOrdered(command, msgCtx);
    }

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        try {
            String[] deserialized = deserializeStrings(bytes);
            System.out.println("Received order: " + Arrays.toString(deserialized));

            String command = deserialized[0];
            String id = deserialized[1];
            String body = deserialized[2];
            String signature = deserialized[3];
            String timestamp = deserialized[4];

            String response;
            switch (command) {
                case "createUser": {
                    if (!verifySignature(body, signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    User user = new User();

                    String userId = generateUserID(id, body);

                    user.setId(userId);
                    user.setPublicKey(body);

                    userRepository.save(user);


                    ObjectMapper objectMapper = new ObjectMapper();
                    response = objectMapper.writeValueAsString(user);
                    break;
                }
                case "createAccount": {
                    User user = getUser(id);
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    String accountId = generateUserAccountID(id, timestamp);
                    Account account = new Account();
                    account.setId(accountId);
                    account.setUserId(id);

                    accountRepository.save(account);

                    ObjectMapper objectMapper = new ObjectMapper();
                    response = objectMapper.writeValueAsString(account);
                    break;
                }
                case "getBalance": {
                    Account account = getAccount(id);
                    if (account == null) {
                        response = "Account not found";
                        break;
                    }

                    User user = getUser(account.getUserId());
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    response = String.valueOf(account.getBalance());
                    break;
                }
                case "loadMoney": {
                    Account account = getAccount(id);
                    if (account == null) {
                        response = "Account not found";
                        break;
                    }

                    User user = getUser(account.getUserId());
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    account.AddBalance(Double.parseDouble(body));

                    accountRepository.save(account);

                    response = String.valueOf(account.getBalance());
                    break;
                }
                case "getGlobalLedgerValue": {
                    User user = getUser(id);
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    double TotalBalance = 0;
                    List<Account> accounts = accountRepository.findAccountsByUserId(user.getId());
                    for (Account account : accounts) {
                        TotalBalance += account.getBalance();
                    }

                    response = String.valueOf(TotalBalance);
                    break;
                }
                case "sendTransaction": {
                    Account sourceAccount = getAccount(id);
                    if (sourceAccount == null) {
                        response = "Source account not found";
                        break;
                    }

                    User user = getUser(sourceAccount.getUserId());
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    Transaction transaction = objectMapper.readValue(body, Transaction.class);

                    if(sourceAccount.getBalance() < transaction.getAmount()) {
                        response = "Insufficient balance";
                        break;
                    }

                    Account destinationAccount = getAccount(transaction.getDestination());
                    if(destinationAccount == null) {
                        response = "Destination account not found";
                        break;
                    }

                    sourceAccount.RemoveBalance(transaction.getAmount());
                    destinationAccount.AddBalance(transaction.getAmount());

                    UUID uuid = UUID.nameUUIDFromBytes(timestamp.getBytes());
                    transaction.setId(uuid);

                    accountRepository.saveAll(List.of(sourceAccount, destinationAccount));
                    transactionRepository.save(transaction);

                    response = objectMapper.writeValueAsString(transaction);
                    break;
                }
                case "getExtract": {
                    Account account = getAccount(id);
                    if (account == null) {
                        response = "Account not found";
                        break;
                    }

                    User user = getUser(account.getUserId());
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    List<Transaction> transactions = transactionRepository.findTransactionsBySource(account.getId());
                    transactions.addAll(transactionRepository.findTransactionsByDestination(account.getId()));
                    transactions.sort(Comparator.comparing(Transaction::getId));

                    ObjectMapper objectMapper = new ObjectMapper();
                    response = objectMapper.writeValueAsString(transactions);
                    break;
                }
                case "getLedger": {
                    User user = getUser(id);
                    if (user == null) {
                        response = "User not found";
                        break;
                    }

                    if (!verifySignature(user.getPublicKey(), signature, body)) {
                        response = "Invalid signature";
                        break;
                    }

                    Iterable<Transaction> transactionsIterable = transactionRepository.findAll();
                    List<Transaction> transactions = new ArrayList<>();
                    transactionsIterable.forEach(transactions::add);
                    transactions.sort(Comparator.comparing(Transaction::getId));


                    Iterable<User> usersIterable = userRepository.findAll();
                    List<User> users = new ArrayList<>();
                    usersIterable.forEach(users::add);
                    users.sort(Comparator.comparing(User::getId));

                    Iterable<Account> accountsIterable = accountRepository.findAll();
                    List<Account> accounts = new ArrayList<>();
                    accountsIterable.forEach(accounts::add);
                    accounts.sort(Comparator.comparing(Account::getId));

                    Ledger ledger = new Ledger();
                    ledger.setTransactions(transactions);
                    ledger.setUsers(users);
                    ledger.setAccounts(accounts);

                    ObjectMapper objectMapper = new ObjectMapper();
                    response = objectMapper.writeValueAsString(ledger);
                    break;
                }
                default: {
                    response = String.valueOf(0);
                }
            }

            System.out.println("Response: " + response);
            return serializeStrings(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return serializeStrings(e.getMessage());
        }
    }



    // UserID = SHA256(email) || PublicKey
    private static String generateUserID(String email, String publicKey)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] emailHash = digest.digest(email.getBytes());

        byte[] publicKeyBytes = publicKey.getBytes();

        byte[] userID = new byte[emailHash.length + publicKeyBytes.length];
        System.arraycopy(emailHash, 0, userID, 0, emailHash.length);
        System.arraycopy(publicKeyBytes, 0, userID, emailHash.length, publicKeyBytes.length);

        return HexFormat.of().formatHex(userID);
    }

    // UserAccountID = HMAC-SHA256(UserID || Timestamp)
    private static String generateUserAccountID(String userIDHex, String timestamp)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] timestampBytes = timestamp.getBytes();

        byte[] userIDBytes = HexFormat.of().parseHex(userIDHex);
        byte[] userIDWithTimestamp = new byte[userIDBytes.length + timestampBytes.length];

        System.arraycopy(userIDBytes, 0, userIDWithTimestamp, 0, userIDBytes.length);
        System.arraycopy(timestampBytes, 0, userIDWithTimestamp, userIDBytes.length, timestampBytes.length);

        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec hmacKey = new SecretKeySpec(timestampBytes, "HmacSHA256");
        hmac.init(hmacKey);

        byte[] hmacResult = hmac.doFinal(userIDWithTimestamp);
        return HexFormat.of().formatHex(hmacResult);
    }


    Account getAccount(String accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    User getUser(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public static UUID generateUUIDFromSeed(long seed) {
        Random random = new Random(seed);
        long mostSigBits = random.nextLong();
        long leastSigBits = random.nextLong();

        mostSigBits &= 0xffffffffffff0fffL;
        mostSigBits |= 0x0000000000004000L;

        leastSigBits &= 0x3fffffffffffffffL;
        leastSigBits |= 0x8000000000000000L;

        return new UUID(mostSigBits, leastSigBits);
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