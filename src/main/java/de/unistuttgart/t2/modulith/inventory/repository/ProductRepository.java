package de.unistuttgart.t2.modulith.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "inventory", itemResourceRel = "inventory", collectionResourceRel = "inventory")
public interface ProductRepository extends JpaRepository<InventoryItem, String> {

}
