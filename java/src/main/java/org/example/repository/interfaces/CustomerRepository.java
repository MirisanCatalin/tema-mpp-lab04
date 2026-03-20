package org.example.repository.interfaces;

import org.example.domain.Customer;

import java.util.List;

public interface CustomerRepository extends Repository<Long, Customer> {
    List<Customer> findByName(String name);
    List<Customer> findByAddress(String address);
    List<Customer> findByNameOrAddress(String name, String address);
}
