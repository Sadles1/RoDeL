# REST API Server with BFT Replica

## Launching BFT Replicas with REST API Servers

To launch BFT replicas with REST API servers:
1. Build the Maven application
2. Build Docker image
3. Run Docker compose (you can change IPs and ports in the configuration)

## Testing Options

You have two ways to test the system:

### 1. Manual Testing with API Client

Run the client using `LedgerClientApplication.java` with two command-line arguments:
- `String` - the API URL
- `int` - the API port

**Available API Methods:**

#### `createUser()`
- **Parameters**: 
  - `String userId` (any custom identifier)
- **Description**:
  - Generates a private key (save this key for future use)
- **Returns**: `User user`

#### `createAccount()`
- **Parameters**:
  - `String userId` (should exist)
  - `String privateKey` (generated during `createUser()`)
- **Returns**: `Account account`

#### `loadMoney()`
- **Parameters**:
  - `String accountId` (should exist)
  - `double amount`
- **Returns**: `double newBalance`

#### `getBalance()`
- **Parameters**:
  - `String accountId` (should exist)
  - `String privateKey` (generated during `createUser()`)
- **Returns**: `double balance`

#### `getGlobalLedgerValue()`
- **Parameters**:
  - `String accountId` (should exist)
  - `String privateKey` (generated during `createUser()`)
- **Returns**: `double totalBalance`

#### `getExtract()` 
- **Parameters**:
  - `String accountId` (should exist)
  - `String privateKey` (generated during `createUser()`)
- **Returns**: `List<Transaction> transactions`

#### `getLedger()` 
- **Parameters**:
  - `String userId` (should exist)
  - `String privateKey` (generated during `createUser()`)
- **Returns**: `Ledger ledger`


#### `sendTransaction()`
- **Parameters**:
  - `String sourceAccountId` (should exist)
  - `String privateKey` (generated during `createUser()`)
  - `String destinationAccountId` (should exist)
  - `double amount`
- **Returns**: `Transaction transaction`

### 2. Automated Performance Testing with Gatling

For load testing and performance measurements, use the Gatling test suite:

```bash
# You can specify custom connection parameters
mvn gatling:test -Dhost=<API_HOST> -Dport=<API_PORT>
