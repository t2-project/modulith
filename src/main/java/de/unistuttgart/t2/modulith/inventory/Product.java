package de.unistuttgart.t2.modulith.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unistuttgart.t2.modulith.inventory.repository.InventoryItem;

/**
 * A single product in the store.
 * <p>
 * Depending on the context the product is used in, the {@code units} represent different things:
 * <ul>
 * <li>reservations : reserved number of units of that product
 * <li>inventory : available number of units in the inventory
 * <li>cart : number of units of the product in the cart
 * </ul>
 * <p>
 * Used to communicate from UI to UIBackend to inventory.
 *
 * @author maumau
 */
public final class Product {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("units")
    private int units;
    @JsonProperty("price")
    private double price;

    public Product() {}

    @JsonCreator
    public Product(@JsonProperty("id") String id, @JsonProperty("name") String name,
        @JsonProperty("description") String description, @JsonProperty("units") int units,
        @JsonProperty("price") double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.units = units;
        this.price = price;
    }

    public Product(String name, String description, int units, double price) {
        this.name = name;
        this.description = description;
        this.units = units;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
