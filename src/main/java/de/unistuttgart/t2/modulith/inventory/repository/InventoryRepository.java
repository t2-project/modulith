package de.unistuttgart.t2.modulith.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
}
