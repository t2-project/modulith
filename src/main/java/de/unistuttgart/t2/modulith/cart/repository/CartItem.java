package de.unistuttgart.t2.modulith.cart.repository;

import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The content of someones cart.
 * <p>
 * Cart items have a {@code creationDate} so that they can be killed once they exceed their time to life.
 *
 * @author maumau
 */
public final class CartItem {

    @Id
    private String id;
    private Map<String, Integer> content;

    private Date creationDate;

    public CartItem(String id, Map<String, Integer> content, Date creationDate) {
        this.id = id;
        this.content = content;
        this.creationDate = creationDate;
    }

    public CartItem(String id, Map<String, Integer> content) {
        this(id, content, Date.from(Instant.now()));
    }

    public CartItem(String id) {
        this(id, new HashMap<>());
    }

    public CartItem() {
        this(null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Integer> getContent() {
        return content;
    }

    public void setContent(Map<String, Integer> content) {
        this.content = content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
