package org.example.repository.interfaces;

import java.util.List;
import java.util.Optional;

public interface Repository<ID,T> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    T update(T entity);
    void delete(ID id);
}
