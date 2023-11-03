package de.unistuttgart.t2.modulith.cart.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Collection;

@RepositoryRestResource(path = "cart", itemResourceRel = "cart", collectionResourceRel = "cart")
public interface CartRepository extends MongoRepository<CartItem, String> {

    void deleteByIdIn(Collection<String> ids);
}
