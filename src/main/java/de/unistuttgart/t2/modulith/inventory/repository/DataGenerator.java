package de.unistuttgart.t2.modulith.inventory.repository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Generates new products into the inventory repository or restocks existing ones. Generation is always triggered after
 * initialisation.
 *
 * @author maumau
 */
@Component
public class DataGenerator {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final InventoryRepository repository;
    private int inventorySize;
    private final boolean setUnitsToMax;
    private final Random random = new Random(5);

    public DataGenerator(@Autowired InventoryRepository repository,
                         @Value("${t2.inventory.size:0}") int inventorySize,
                         @Value("${t2.inventory.setUnitsToMax:false}") boolean setUnitsToMax) {
        assert (repository != null);
        this.repository = repository;
        this.inventorySize = inventorySize;
        this.setUnitsToMax = setUnitsToMax;
    }

    /**
     * Generates products into the inventory repository.
     */
    @PostConstruct
    public void generateProducts() {
        if (inventorySize > PRODUCT_NAMES.length) {
            LOG.info("Inventory size is configured to be {}, but only {} products are available.", inventorySize, PRODUCT_NAMES.length);
            inventorySize = PRODUCT_NAMES.length;
        }

        if (repository.count() >= inventorySize) {
            LOG.info("Repository already contains {} entries. Not adding new entries.", repository.count());
            return;
        }

        LOG.info("Repository too small. Generate {} new entries.", inventorySize);
        if (setUnitsToMax) {
            LOG.info("Option 'setUnitsToMax' is enabled. All items will be available {} times.", Integer.MAX_VALUE);
        }

        for (int i = (int) repository.count(); i < inventorySize; i++) {
            String name = PRODUCT_NAMES[i];
            int units;
            if (!setUnitsToMax) {
                units = random.nextInt(500) + 42;
            } else {
                units = Integer.MAX_VALUE;
            }
            double price = random.nextInt(10) + random.nextDouble();
            String description = "very nice " + PRODUCT_NAMES[i] + " tea";

            InventoryItem product = new InventoryItem(null, name, description, units, price);

            repository.save(product);
        }
    }

    /**
     * Restock products in the repository. at some point all products will be sold out. thus there must be an option to
     * restock them.
     */
    @Transactional
    public void restockProducts() {
        List<InventoryItem> items = repository.findAll();

        for (InventoryItem item : items) {
            int units;
            if (!setUnitsToMax) {
                units = random.nextInt(500) + 42;
            } else {
                units = Integer.MAX_VALUE;
            }
            item.setUnits(units);
        }

        repository.saveAll(items);
        LOG.info("Restocked all products.");
    }

    // Predefined products from original tea store and some more generated with ChatGPT
    private static final String[] PRODUCT_NAMES = {
        "Earl Grey (loose)",
        "Assam (loose)",
        "Darjeeling (loose)",
        "Frisian Black Tee (loose)",
        "Anatolian Assam (loose)",
        "Earl Grey (20 bags)",
        "Assam (20 bags)",
        "Darjeeling (20 bags)",
        "Ceylon (loose)",
        "Ceylon (20 bags)",
        "House blend (20 bags)",
        "Assam with Ginger (20 bags)",
        "Sencha (loose)",
        "Sencha (15 bags)",
        "Sencha (25 bags)",
        "Earl Grey Green (loose)",
        "Earl Grey Green (15 bags)",
        "Earl Grey Green (25 bags)",
        "Matcha 30 g",
        "Matcha 50 g",
        "Matcha 100 g",
        "Gunpowder Tea (loose)",
        "Gunpowder Tea (15 bags)",
        "Gunpowder Tea (25 bags)",
        "Camomile (loose)",
        "Camomile (15 bags)",
        "Peepermint (loose)",
        "Peppermint (15 bags)",
        "Peppermint (15 bags)",
        "Sweet Mint (loose)",
        "Sweet Mint (15 bags)",
        "Sweet Mint (25 bags)",
        "Lemongrass (loose)",
        "Lemongrass (20 bags)",
        "Chai Mate (15 bags)",
        "Chai Mate (25 bags)",
        "Stomach Soothing Tea (15 bags)",
        "Headache Soothing Tea (15 bags)",
        "Rooibos Pure (loose)",
        "Rooibos Pure (20 bags)",
        "Rooibos Orange (loose)",
        "Rooibos Orange (20 bags)",
        "Rooibos Coconut (loose)",
        "Rooibos Coconut (20 bags)",
        "Rooibos Vanilla (loose)",
        "Rooibos Pure (20 bags)",
        "Rooibos Ginger (loose)",
        "Rooibos Pure (20 bags)",
        "Rooibos Grapefruit (loose)",
        "Rooibos Pure (20 bags)",
        "White Tea (loose)",
        "White Tea (15 bags)",
        "White Tea (25 bags)",
        "White Chai (loose)",
        "White Chai (15 bags)",
        "White Chai (25 bags)",
        "Pai Mu Tan White (loose)",
        "Pai Mu Tan White (15 bags)",
        "Pai Mu Tan White (25 bags)",
        "White Apricot (loose)",
        "White Apricot (15 bags)",
        "White Apricot (25 bags)",
        "Moroccan Mint (loose)",
        "Moroccan Mint (20 bags)",
        "Moroccan Mint (25 bags)",
        "Jasmine Dragon Pearl (loose)",
        "Jasmine Dragon Pearl (15 bags)",
        "Jasmine Dragon Pearl (25 bags)",
        "Pu-erh (loose)",
        "Pu-erh (20 bags)",
        "Pu-erh (25 bags)",
        "Oolong (loose)",
        "Oolong (20 bags)",
        "Oolong (25 bags)",
        "Green Rooibos (loose)",
        "Green Rooibos (20 bags)",
        "Green Rooibos (25 bags)",
        "Hibiscus (loose)",
        "Hibiscus (20 bags)",
        "Hibiscus (25 bags)",
        "Blueberry Herbal (loose)",
        "Blueberry Herbal (15 bags)",
        "Blueberry Herbal (25 bags)",
        "Cranberry Apple (loose)",
        "Cranberry Apple (15 bags)",
        "Cranberry Apple (25 bags)",
        "Ginger Lemon (loose)",
        "Ginger Lemon (20 bags)",
        "Ginger Lemon (25 bags)",
        "Vanilla Chai (loose)",
        "Vanilla Chai (15 bags)",
        "Vanilla Chai (25 bags)",
        "Lavender Earl Grey (loose)",
        "Lavender Earl Grey (15 bags)",
        "Lavender Earl Grey (25 bags)",
        "Chocolate Mint (loose)",
        "Chocolate Mint (20 bags)",
        "Chocolate Mint (25 bags)",
        "Peach Oolong (loose)",
        "Peach Oolong (15 bags)",
        "Peach Oolong (25 bags)",
        "Strawberry Green (loose)",
        "Strawberry Green (20 bags)",
        "Strawberry Green (25 bags)",
        "Blueberry White (loose)",
        "Blueberry White (15 bags)",
        "Blueberry White (25 bags)",
        "Coconut Chai (loose)",
        "Coconut Chai (20 bags)",
        "Coconut Chai (25 bags)",
        "Mint Chamomile (loose)",
        "Mint Chamomile (15 bags)",
        "Mint Chamomile (25 bags)",
        "Lemon Ginger (loose)",
        "Lemon Ginger (20 bags)",
        "Lemon Ginger (25 bags)",
        "Orange Spice (loose)",
        "Orange Spice (15 bags)",
        "Orange Spice (25 bags)",
        "Apple Cinnamon (loose)",
        "Apple Cinnamon (20 bags)",
        "Apple Cinnamon (25 bags)",
        "Berry Hibiscus (loose)",
        "Berry Hibiscus (15 bags)",
        "Berry Hibiscus (25 bags)",
        "Mango Black (loose)",
        "Mango Black (20 bags)",
        "Mango Black (25 bags)",
        "Pineapple Green (loose)",
        "Pineapple Green (15 bags)",
        "Pineapple Green (25 bags)",
        "Lemon Rooibos (loose)",
        "Lemon Rooibos (20 bags)",
        "Lemon Rooibos (25 bags)",
        "Vanilla Rooibos (loose)",
        "Vanilla Rooibos (15 bags)",
        "Vanilla Rooibos (25 bags)",
        "Strawberry Rooibos (loose)",
        "Strawberry Rooibos (20 bags)",
        "Strawberry Rooibos (25 bags)",
        "Honeybush (loose)",
        "Honeybush (20 bags)",
        "Honeybush (25 bags)",
        "Cranberry Rooibos (loose)",
        "Cranberry Rooibos (15 bags)",
        "Cranberry Rooibos (25 bags)",
        "Cherry Sencha (loose)",
        "Cherry Sencha (20 bags)",
        "Cherry Sencha (25 bags)",
        "Raspberry Black (loose)",
        "Raspberry Black (15 bags)",
        "Raspberry Black (25 bags)",
        "Apricot Oolong (loose)",
        "Apricot Oolong (20 bags)",
        "Apricot Oolong (25 bags)",
        "Peach Rooibos (loose)",
        "Peach Rooibos (15 bags)",
        "Peach Rooibos (25 bags)",
        "Almond Green (loose)",
        "Almond Green (20 bags)",
        "Almond Green (25 bags)",
        "Coconut Rooibos (loose)",
        "Coconut Rooibos (15 bags)",
        "Coconut Rooibos (25 bags)",
        "Minty Jasmine (loose)",
        "Minty Jasmine (20 bags)",
        "Minty Jasmine (25 bags)",
        "Lemon Verbena (loose)",
        "Lemon Verbena (15 bags)",
        "Lemon Verbena (25 bags)",
        "Cherry Blossom (loose)",
        "Cherry Blossom (20 bags)",
        "Cherry Blossom (25 bags)",
        "Ginseng Oolong (loose)",
        "Ginseng Oolong (20 bags)",
        "Ginseng Oolong (25 bags)",
        "Blackberry Sage (loose)",
        "Blackberry Sage (20 bags)",
        "Blackberry Sage (25 bags)",
        "Peach Blossom (loose)",
        "Peach Blossom (15 bags)",
        "Peach Blossom (25 bags)",
        "Mango Tango (loose)",
        "Mango Tango (15 bags)",
        "Mango Tango (25 bags)",
        "Cinnamon Apple (loose)",
        "Cinnamon Apple (20 bags)",
        "Cinnamon Apple (25 bags)",
        "Lavender Mint (loose)",
        "Lavender Mint (15 bags)",
        "Lavender Mint (25 bags)",
        "Rose Petal (loose)",
        "Rose Petal (20 bags)",
        "Rose Petal (25 bags)",
        "Blueberry Lemon (loose)",
        "Blueberry Lemon (15 bags)",
        "Blueberry Lemon (25 bags)",
        "Ginger Peach (loose)",
        "Ginger Peach (20 bags)",
        "Ginger Peach (25 bags)",
        "Pomegranate Green (loose)",
        "Pomegranate Green (15 bags)",
        "Pomegranate Green (25 bags)",
        "Coconut Lime (loose)",
        "Coconut Lime (20 bags)",
        "Coconut Lime (25 bags)",
        "Raspberry Rose (loose)",
        "Raspberry Rose (15 bags)",
        "Raspberry Rose (25 bags)",
        "Orange Blossom (loose)",
        "Orange Blossom (20 bags)",
        "Orange Blossom (25 bags)",
        "Minty Citrus (loose)",
        "Minty Citrus (15 bags)",
        "Minty Citrus (25 bags)",
        "Honey Lavender (loose)",
        "Honey Lavender (20 bags)",
        "Honey Lavender (25 bags)",
        "Chamomile Lavender (loose)",
        "Chamomile Lavender (15 bags)",
        "Chamomile Lavender (25 bags)",
        "Cranberry Orange (loose)",
        "Cranberry Orange (20 bags)",
        "Cranberry Orange (25 bags)",
        "Lemon Berry (loose)",
        "Lemon Berry (15 bags)",
        "Lemon Berry (25 bags)",
        "Blackcurrant (loose)",
        "Blackcurrant (20 bags)",
        "Blackcurrant (25 bags)",
        "Honey Lemon (loose)",
        "Honey Lemon (15 bags)",
        "Honey Lemon (25 bags)",
        "Peachy Keen (loose)",
        "Peachy Keen (20 bags)",
        "Peachy Keen (25 bags)",
        "Spiced Apple (loose)",
        "Spiced Apple (15 bags)",
        "Spiced Apple (25 bags)",
        "Citrus Burst (loose)",
        "Citrus Burst (20 bags)",
        "Citrus Burst (25 bags)",
        "Gingerbread (loose)",
        "Gingerbread (15 bags)",
        "Gingerbread (25 bags)",
        "Maple Walnut (loose)",
        "Maple Walnut (20 bags)",
        "Maple Walnut (25 bags)",
        "Caramel Rooibos (loose)",
        "Caramel Rooibos (15 bags)",
        "Caramel Rooibos (25 bags)",
        "Pumpkin Spice (loose)",
        "Pumpkin Spice (20 bags)",
        "Pumpkin Spice (25 bags)",
        "Butter Pecan (loose)",
        "Butter Pecan (15 bags)",
        "Butter Pecan (25 bags)"
    };
}
