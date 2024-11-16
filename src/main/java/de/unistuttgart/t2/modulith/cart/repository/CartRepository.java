package de.unistuttgart.t2.modulith.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface CartRepository extends MongoRepository<CartItem, String> {

    void deleteByIdIn(Collection<String> ids);
}
