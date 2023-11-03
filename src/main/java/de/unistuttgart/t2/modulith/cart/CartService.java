package de.unistuttgart.t2.modulith.cart;

import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Manages the products in the users carts.
 * <p>
 * Users are distinguished by their session ids.
 *
 * @author maumau
 * @author davidkopp
 */
@Service
@EnableMongoRepositories(basePackageClasses = CartRepository.class)
public class CartService {

    private final CartRepository cartRepository;

    public CartService(@Autowired CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    /**
     * Get the entire get for the given sessionId.
     *
     * @param sessionId identifies the cart content
     * @return the content of the cart, if exists
     */
    public Optional<CartContent> getCart(String sessionId) {
        Optional<CartContent> result = Optional.empty();

        Optional<CartItem> optionalCartItem = cartRepository.findById(sessionId);
        if (optionalCartItem.isPresent()) {
            Map<String, Integer> content = optionalCartItem.get().getContent();
            result = Optional.of(new CartContent(content));
        }
        return result;
    }

    /**
     * Add the given number units of product to a users cart.
     * <p>
     * If the product is already in the cart, the units of that product will be updated.
     *
     * @param sessionId identifies the cart to add to
     * @param productId id of product to be added
     * @param units     number of units to be added (must not be negative)
     */
    public void addItemToCart(String sessionId, String productId, int units) {
        if (units < 0) {
            throw new IllegalArgumentException("Value of units must not be negative.");
        }

        Optional<CartContent> optCartContent = getCart(sessionId);
        if (optCartContent.isPresent()) {
            CartContent cartContent = optCartContent.get();
            cartContent.getContent().put(productId, units + cartContent.getUnits(productId));
            saveCart(sessionId, cartContent);
        } else {
            saveCart(sessionId, new CartContent(Map.of(productId, units)));
        }
    }

    /**
     * Delete the given number units of product from a users cart.
     * <p>
     * If the number of units in the cart decrease to zero or less, the product is remove from the cart. If the no such
     * product is in cart, do nothing.
     *
     * @param sessionId identifies the cart to delete from
     * @param productId id of the product to be deleted
     * @param units     number of units to be deleted (must not be negative)
     */
    public void deleteItemFromCart(String sessionId, String productId, int units) {
        if (units < 0) {
            throw new IllegalArgumentException("Value of units must not be negative.");
        }

        Optional<CartContent> optCartContent = getCart(sessionId);
        if (optCartContent.isPresent()) {
            CartContent cartContent = optCartContent.get();
            int remainingUnitsInCart = cartContent.getUnits(productId) - units;
            if (remainingUnitsInCart > 0) {
                cartContent.getContent().put(productId, remainingUnitsInCart);
            } else {
                cartContent.getContent().remove(productId);
            }
            saveCart(sessionId, cartContent);
        }
    }

    /**
     * Delete the entire cart for the given sessionId.
     *
     * @param sessionId identifies the cart content to delete
     */
    public void deleteCart(String sessionId) {
        cartRepository.deleteById(sessionId);
    }

    private void saveCart(String id, CartContent cartContent) {
        Optional<CartItem> optionalCartItem = cartRepository.findById(id);
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cartItem.setContent(cartContent.getContent());
            cartRepository.save(cartItem);
        } else {
            cartRepository.save(new CartItem(id, cartContent.getContent()));
        }
    }
}
