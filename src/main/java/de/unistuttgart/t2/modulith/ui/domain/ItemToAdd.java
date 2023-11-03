package de.unistuttgart.t2.modulith.ui.domain;

/**
 * Definition of the JSP model attribute 'item'.
 * It is used to add and delete items from the cart.
 *
 * @author maumau
 */
public class ItemToAdd {

    private String productId;
    private int units;

    public ItemToAdd() {
    }

    public ItemToAdd(String productId, int units) {
        this.productId = productId;
        this.units = units;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    @Override
    public String toString() {
        return "ItemToAdd [productId=" + productId + ", units=" + units + "]";
    }
}
