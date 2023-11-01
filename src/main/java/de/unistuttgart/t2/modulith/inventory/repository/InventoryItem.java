package de.unistuttgart.t2.modulith.inventory.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A Product in the inventory. Each product has some describing attributes such as a name, a description and a price, as
 * well as the number of units in stock. If a user placed units of product in their cart, that product has some
 * reservations attached. The actual number of unit in stock shall only ever be changed by committing reservations (c.f.
 * {@link InventoryItem#commitReservation(String)}})
 *
 * @author maumau
 */
@Entity
@Table(name = "inventory_item")
public class InventoryItem {

    @Id
    @Column(name = "id")
    @JsonProperty("id")
    @UuidGenerator
    private final String id;

    @Column(name = "name")
    @JsonProperty("name")
    private final String name;

    @Column(name = "description")
    @JsonProperty("description")
    private final String description;

    /**
     * number units of this product. never less than the sum of all reservations.
     */
    @Column(name = "units")
    @JsonProperty("units")
    private int units;

    @Column(name = "price")
    @JsonProperty("price")
    private final double price;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonProperty("reservations")
    private final List<Reservation> reservations;

    /**
     * because spring framework wants this.
     */
    public InventoryItem() {
        this("", "", "", 0, 0);
    }

    public InventoryItem(String id, String name, String description, int units, double price) {
        this(id, name, description, units, price, new ArrayList<>());
    }

    @JsonCreator
    public InventoryItem(String id, String name, String description, int units, double price,
                         List<Reservation> reservations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.units = units;
        this.price = price;
        this.reservations = new ArrayList<>(reservations);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getUnits() {
        return units;
    }

    /**
     * set the units. cannot be used to decrease the number of units.
     *
     * @param units new number of unit in stock
     */
    public void setUnits(int units) {
        if (units > this.units) {
            this.units = units;
        }
    }

    public double getPrice() {
        return price;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    @Override
    public String toString() {
        return id + ", " + name + ", " + description + ", " + units + ", " + price;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InventoryItem)) {
            return false;
        }

        return id.equals(((InventoryItem) o).id) && name.equals(((InventoryItem) o).name)
            && description.equals(((InventoryItem) o).description) && units == ((InventoryItem) o).units
            && price == ((InventoryItem) o).price;
    }

    /**
     * Calculate the number of available units. The number of available units is
     * {@code units in stock - sum of reserved units}
     *
     * @return number of not yet reserved units of this product
     * @throws IllegalStateException if the reservations are too much.
     */
    @JsonIgnore
    public int getAvailableUnits() {
        int availableUnits = units - reservations.stream().mapToInt(Reservation::getUnits).sum();
        if (availableUnits < 0) {
            throw new IllegalStateException(
                String.format("%d units reserved, even though only %d are in stock", units - availableUnits, units));
        }
        return availableUnits;
    }

    /**
     * Add to or updated the products reservations. If a reservation for the given {@code sessionId} already exists, the
     * existing reservation is updated, otherwise a new reservation is added. However, a reservation is only added or
     * updated reservations if enough products are available.
     *
     * @param sessionId      to identify user
     * @param unitsToReserve number of units to reserve
     * @throws IllegalArgumentException if not enough units available or otherwise illegal arguments
     */
    public void addReservation(String sessionId, int unitsToReserve) {
        if (unitsToReserve > getAvailableUnits() || unitsToReserve < 0) {
            throw new IllegalArgumentException(String.format(
                "illegal amount of units to reserve: tried ro reserve %d units of product %s, but only %d are available",
                unitsToReserve, id, getAvailableUnits()));
        }
        if (unitsToReserve == 0) {
            return;
        }
        for (Reservation reservation : reservations) {
            if (reservation.getUserId().equals(sessionId)) {
                reservation.updateUnits(unitsToReserve);
                return;
            }
        }
        reservations.add(new Reservation(unitsToReserve, sessionId, this));
    }

    /**
     * remove a reservation and decrease units in stock. always use this operation to decrease the number of unit in
     * stock.
     *
     * @param sessionId to identify the reservation to be committed
     */
    public void commitReservation(String sessionId) {
        for (Reservation reservation : reservations) {
            if (reservation.getUserId().equals(sessionId)) {
                units -= reservation.getUnits();
                reservations.remove(reservation);
                return;
            }
        }
    }

    public void deleteReservation(String sessionId) {
        reservations.removeIf(reservation -> reservation.getUserId().equals(sessionId));
    }
}
