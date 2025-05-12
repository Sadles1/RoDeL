import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static org.example.KeyPairUtil.signBody;
import static org.example.KeyPairUtil.verifySignature;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import org.example.KeyPairUtil;
import org.example.Transaction;

import java.security.KeyPair;
import java.util.*;

public class ApiBenchmarkSimulation extends Simulation {
    static String serverPublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJP+PNMN30iH0wcEBSWPiBIupJEuC1lZLJV80WnpHan9UTVZEDRgYV/VaH4QUHnUA+H335IDiqa4LGCsx3v9hLg==";
    // Читаем параметры из командной строки (значения по умолчанию указаны)
    private final String host = System.getProperty("host", "127.0.0.1");
    private final int port = Integer.parseInt(System.getProperty("port", "8443"));

    private final long testTime = 20;

    // Формируем базовый URL
    private final String baseUrl = "https://" + host + ":" + port;

    HttpProtocolBuilder httpProtocol = http.baseUrl(baseUrl)
            .acceptHeader("application/json");

    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, String> accounts = new HashMap<>();

    private static String getRandomAccountId() {
        List<String> userIds = new ArrayList<>(accounts.keySet());

        String accountId = userIds.get(new Random().nextInt(accounts.size()));
        return accountId;
    }

    private static String getRandomUserId() {
        List<String> userIds = new ArrayList<>(users.keySet());
        String userId = userIds.get(new Random().nextInt(userIds.size()));
        return userId;
    }

    ScenarioBuilder createUser = scenario("Create User")
            .exec(session -> {
                try {
                    KeyPair keyPair = KeyPairUtil.generateECDSAKeyPair();

                    String publicKey = KeyPairUtil.encodePublicKey(keyPair.getPublic());
                    String privateKey = KeyPairUtil.encodePrivateKey(keyPair.getPrivate());

                    System.out.println("Public Key: " + publicKey);
                    System.out.println("Private Key: " + privateKey);

                    String id = "test_user@gmail.com";
                    String body = publicKey;
                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", id)
                            .set("body", body)
                            .set("signature", signature)
                            .set("privateKey", privateKey);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/createUser")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(jsonPath("$.id").saveAs("userId"))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            })
            .exec(session -> {
                users.put(session.get("userId"), session.get("privateKey"));

                int numAccounts = new Random().nextInt(3) + 1;
                return session.set("numAccounts", numAccounts);
            })
            .repeat(session -> session.getInt("numAccounts"), "accountIndex").on(
                    exec(session -> {
                        try {
                            String body = "verify";
                            String privateKey = session.getString("privateKey");

                            String signature = signBody(body, privateKey);
                            return session
                                    .set("body", body)
                                    .set("signature", signature);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                            .exec(
                                    http("Signed Request")
                                            .post("/api/createAccount")
                                            .header("Id", "#{userId}")
                                            .header("Body", "#{body}")
                                            .header("Signature", "#{signature}")
                                            .check(status().is(200))
                                            .check(jsonPath("$.id").saveAs("accountId"))
                                            .check(bodyString().saveAs("responseBody"))
                                            .check(header("Signature").saveAs("responseSignature"))
                            )
                            .exec(session -> {
                                String body = session.getString("responseBody");
                                String signature = session.getString("responseSignature");

                                if (!verifySignature(serverPublicKey, signature, body)) {
                                    System.err.println("Invalid signature");
                                    return session.markAsFailed();
                                }

                                return session;
                            })
                            .exec(session -> {
                                accounts.put(session.get("accountId"), session.get("privateKey"));
                                return session;
                            })
            );

    ScenarioBuilder getBalance = scenario("Get Balance")
            .exec(session -> {
                try {
                    String accountId = getRandomAccountId();
                    String privateKey = accounts.get(accountId);

                    System.out.println("Account Id: " + accountId);

                    String body = "verify";

                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", accountId)
                            .set("body", body)
                            .set("signature", signature);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/getBalance")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            });

    ScenarioBuilder loadMoney = scenario("Load Money")
            .exec(session -> {
                try {
                    String accountId = getRandomAccountId();
                    String privateKey = accounts.get(accountId);

                    System.out.println("Account Id: " + accountId);

                    String body = String.valueOf(new Random().nextDouble(500));

                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", accountId)
                            .set("body", body)
                            .set("signature", signature);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/loadMoney")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            });

    ScenarioBuilder getGlobalLedgerValue = scenario("Get Global Ledger Value")
            .exec(session -> {
                try {
                    String userId = getRandomUserId();
                    String privateKey = users.get(userId);

                    System.out.println("User Id: " + userId);

                    String body = "verify";

                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", userId)
                            .set("body", body)
                            .set("signature", signature);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/getGlobalLedgerValue")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            });

    ScenarioBuilder sendTransaction = scenario("Send Transaction")
            .exec(session -> {
                try {
                    String sourceAccountId = getRandomAccountId();
                    String privateKey = accounts.get(sourceAccountId);

                    System.out.println("Source Account Id: " + sourceAccountId);

                    String destinationAccountId = getRandomAccountId();
                    System.out.println("Destination Account Id: " + sourceAccountId);

                    Transaction transaction = new Transaction();
                    transaction.setSource(sourceAccountId);
                    transaction.setDestination(destinationAccountId);
                    transaction.setAmount(new Random().nextDouble(500));

                    ObjectMapper objectMapper = new ObjectMapper();
                    String body = objectMapper.writeValueAsString(transaction);

                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", sourceAccountId)
                            .set("body", body)
                            .set("signature", signature);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/sendTransaction")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            });

    ScenarioBuilder getExtract = scenario("Get Extract")
            .exec(session -> {
                try {
                    String accountId = getRandomAccountId();
                    String privateKey = accounts.get(accountId);

                    System.out.println("Account Id: " + accountId);

                    String body = "verify";
                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", accountId)
                            .set("body", body)
                            .set("signature", signature);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/getExtract")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            });

    ScenarioBuilder getLedger = scenario("Get Ledger")
            .exec(session -> {
                try {
                    String userId = getRandomUserId();
                    String privateKey = users.get(userId);

                    System.out.println("User Id: " + userId);

                    String body = "verify";
                    String signature = signBody(body, privateKey);

                    return session
                            .set("id", userId)
                            .set("body", body)
                            .set("signature", signature);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .exec(
                    http("Signed Request")
                            .post("/api/getLedger")
                            .header("Id", "#{id}")
                            .header("Body", "#{body}")
                            .header("Signature", "#{signature}")
                            .check(status().is(200))
                            .check(bodyString().saveAs("responseBody"))
                            .check(header("Signature").saveAs("responseSignature"))
            )
            .exec(session -> {
                String body = session.getString("responseBody");
                String signature = session.getString("responseSignature");

                if (!verifySignature(serverPublicKey, signature, body)) {
                    System.err.println("Invalid signature");
                    return session.markAsFailed();
                }

                return session;
            });


    {
        setUp(
                createUser.injectOpen(rampUsers(new Random().nextInt(3) + 2).during(10))
                        .andThen(
                                loadMoney.injectOpen(rampUsers(new Random().nextInt(400) + 100).during(testTime)),
                                getBalance.injectOpen(nothingFor(5), rampUsers(new Random().nextInt(200) + 100).during(testTime)),
                                getGlobalLedgerValue.injectOpen(nothingFor(3), rampUsers(new Random().nextInt(100) + 50).during(testTime)),
                                sendTransaction.injectOpen(nothingFor(3), rampUsers(new Random().nextInt(30) + 20).during(testTime)),
                                getExtract.injectOpen(nothingFor(6), rampUsers(new Random().nextInt(30) + 20).during(testTime)),
                                getLedger.injectOpen(nothingFor(10), rampUsers(new Random().nextInt(30) + 20).during(testTime))
                        )
        ).protocols(httpProtocol);
    }
}


