package de.unistuttgart.t2.modulith.cart;

import de.unistuttgart.t2.modulith.cart.repository.CartItem;
import de.unistuttgart.t2.modulith.cart.repository.CartRepository;
import de.unistuttgart.t2.modulith.common.CartContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Manages the products in the users carts.
 * <p>
 * Users are distinguished by their session ids.
 *
 * @author maumau
 */
@Service
@EnableMongoRepositories(basePackageClasses = CartRepository.class)
public class CartModule {

    @Autowired
    CartRepository cartRepository;

    public CartModule() {
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    public Optional<CartContent> getCart(String id) {
        Optional<CartContent> result = Optional.empty();

        Optional<CartItem> optionalCartItem = cartRepository.findById(id);
        if (optionalCartItem.isPresent()) {
            Map<String, Integer> content = optionalCartItem.get().getContent();
            result = Optional.of(new CartContent(content));
        }
        return result;
    }

    public void saveCart(String id, CartContent cartContent) {
        Optional<CartItem> optionalCartItem = cartRepository.findById(id);
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cartItem.setContent(cartContent.getContent());
            cartRepository.save(cartItem);
        } else {
            cartRepository.save(new CartItem(id, cartContent.getContent()));
        }
    }

    public void deleteCart(String id) {
        cartRepository.deleteById(id);
    }
}
