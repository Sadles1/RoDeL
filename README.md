Starting a REST API Server with BFT Replica

To launch a REST API server with a BFT replica, run LedgerServerApplication.java with an integer command-line argument specifying the replica ID (must be between 0 and 3). Optionally, you can set the API port using --server.port=8443 as a command-line argument.

Note: For PBFT to function correctly, you need 4 replicas running simultaneously.
Persistence Configuration (Redis)

For persistence, a Redis database is required. You can configure your Redis settings in application.properties.
Running the Client

Once all 4 replicas are up and have reached consensus, you can run LedgerClientApplication.java with two command-line arguments:

    String – the API URL

    int – the API port

Currently, there is no dedicated UI for the client. To make requests, you need to manually call the following methods in main():
Available Methods:

    createAccount()

        Parameters: String userId (any custom identifier)

        Generates and returns a private key (save this key for future use).

    getBalance()

        Parameters:

            String userId (should exist)

            String private key (generated during createAccount())
