Starting a REST API Server with BFT Replica

To launch a BFT replicas with a REST API servers:
1. Build maven app
1. Build docker image
2. Run docker compose (you can change ip's and port's here)

Running the API Client:

Once all 4 replicas are up, you can run LedgerClientApplication.java with two command-line arguments:

    String – the API URL

    int – the API port

Currently, there is no dedicated UI for the client. 
To make requests, you can manually call the following methods in main():

createUser()

        Parameters: String userId (any custom identifier)

        Generates a private key (save this key for future use).

        Returns: User user
createAccount()

        Parameters:

            String userId (should exist)

            String private key (generated during createUser())

        Returns: Account account
        
loadMoney()

        Parameters:

            String accountId (should exist)

            double amount

        Returns: double newBalance
        
getBalance()

        Parameters:

            String accountId (should exist)

            String private key (generated during createUser())

        Returns: double balance

getGlobalLedgerValue()

        Parameters:

            String accountId (should exist)

            String private key (generated during createUser())

        Returns: double totalBalance

sendTransaction()

        Parameters:

            String sourceAccountId (should exist)

            String private key (generated during createUser())

            String destinationAccountId (should exist)

            double amount

        Returns: Transaction transaction

getExtract()

        Parameters:

            String accountId (should exist)

            String private key (generated during createUser())

        Returns: List<Transaction> transactions


getExtract()

        Parameters:

            String userId (should exist)

            String private key (generated during createUser())

        Returns: Ledger ledger

