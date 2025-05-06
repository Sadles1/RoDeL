package org.example;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import bftsmart.tom.ServiceProxy;

import static org.example.StringSerializer.deserializeStrings;
import static org.example.StringSerializer.serializeStrings;

@RestController
@RequestMapping("/api")
public class LedgerController {
    private final AccountRepository accountRepository;
    private final ServiceProxy bftProxy = new ServiceProxy(0);

    public LedgerController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        System.out.println("Listening on port " + event.getWebServer().getPort());
    }

    //todo: This is the main API method for bft, all other should be deleted after
    @RequestMapping("/**")
    public ResponseEntity<?> handleRequests(HttpServletRequest request,
                                            @RequestHeader("UserId") String accountId,
                                            @RequestHeader("Body") String body,
                                            @RequestHeader("Signature") String signature) throws Exception {
        String path = request.getRequestURI().replaceFirst("^/api/", "");

        byte[] serialized = serializeStrings(path, accountId, body, signature);
        byte[] reply = bftProxy.invokeOrdered(serialized);

        return ResponseEntity.ok().body(deserializeStrings(reply));
    }
}

