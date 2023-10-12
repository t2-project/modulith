package de.unistuttgart.t2.modulith.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CartRepository extends MongoRepository<CartItem, String> {

    void deleteByIdIn(Collection<String> ids);
}
