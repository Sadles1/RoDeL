package org.example;

import bftsmart.tom.ServiceReplica;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import bftsmart.tom.ServiceProxy;

import java.util.Arrays;

import static org.example.KeyPairUtil.signBody;
import static org.example.StringSerializer.deserializeStrings;
import static org.example.StringSerializer.serializeStrings;

@RestController
@RequestMapping("/api")
public class LedgerController {

    private static String serverPrivateKey = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCCAq1BshQWE+u3FXwjnGz/nmgvqgp+eBeb7qDMTxAwpjg==";

    private final ServiceProxy bftProxy;
    public LedgerController(ApplicationArguments args) {
        int serverId = Integer.parseInt(args.getNonOptionArgs().get(0));
        this.bftProxy = new ServiceProxy(serverId);
    }

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        System.out.println("Listening on port " + event.getWebServer().getPort());
    }

    //todo: This is the main API method for bft, all other should be deleted after
    @RequestMapping("/**")
    public ResponseEntity<?> handleRequests(HttpServletRequest request,
                                            @RequestHeader("Id") String id,
                                            @RequestHeader("Body") String body,
                                            @RequestHeader("Signature") String signature) throws Exception {
        String path = request.getRequestURI().replaceFirst("^/api/", "");

        System.out.println("Path: " + path);
        System.out.println("Id: " + id);
        System.out.println("Body: " + body);
        System.out.println("Signature: " + signature);

        byte[] serialized = serializeStrings(path, id, body, signature, String.valueOf(System.currentTimeMillis()));
        byte[] reply = bftProxy.invokeOrdered(serialized);

        String[] replyStrings = deserializeStrings(reply);

        String replyBody = replyStrings[0];
        String replySignature = signBody(replyBody, serverPrivateKey);

        ResponseEntity response = ResponseEntity.ok().header("Signature", replySignature).body(replyBody);

        System.out.println("Response: " + response);
        return response;
    }
}

