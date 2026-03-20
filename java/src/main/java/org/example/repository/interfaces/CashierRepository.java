package org.example.repository.interfaces;

import org.example.domain.Cashier;

import java.util.Optional;

public interface CashierRepository extends Repository<Long,Cashier> {
    Optional<Cashier> findByUsernameAndPassword(String username, String password);
    Optional<Cashier> findByUsername(String username);
}
