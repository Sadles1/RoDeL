package org.example;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, String> {
    public List<Transaction> findTransactionsBySource(String source);
    public List<Transaction> findTransactionsByDestination(String destination);
}
