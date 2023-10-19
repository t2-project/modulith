package de.unistuttgart.t2.modulith.inventory.repository;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Date;

/**
 * A Reservation of a certain number of units.
 * <p>
 * Reservations have a {@code creationDate} such that they might be killed after they exceeded their time to life.
 * 
 * @author maumau
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date creationDate;
    @Column(name = "units")
    private int units;

    @Column(name = "userId")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    public InventoryItem item;

    public Reservation() {
        this(0, Date.from(Instant.now()), "", null);
    }

    public Reservation(int units, String userId, InventoryItem item) {
        this(units, Date.from(Instant.now()), userId, item);
    }

    protected Reservation(int units, Date date, String userId, InventoryItem item) {
        super();
        this.units = units;
        this.creationDate = date;
        this.userId = userId;
        this.item = item;
    }

    public int getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public int getUnits() {
        return units;
    }

    /**
     * increase number of units by 'update' and also renew the creation date.
     * 
     * @param update additionally reserved units
     */
    public void updateUnits(int update) {
        this.units = units + update;
        renewCreationdate();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * updates creation date to current time. this extends the life span of the reservation. it exists because the time
     * to life of a reservation should always be measured according to the most recent time of modification.
     */
    private void renewCreationdate() {
        creationDate = Date.from(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reservation)) {
            return false;
        }
        return this.userId.equals(((Reservation) o).userId);
    }

    @Override
    public String toString() {
        return "Reservation [id=" + id + ", creationDate=" + creationDate + ", units=" + units + ", userId=" + userId
            + ", item=" + item + "]";
    }
}
