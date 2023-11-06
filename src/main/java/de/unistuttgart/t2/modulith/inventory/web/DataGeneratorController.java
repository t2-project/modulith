package de.unistuttgart.t2.modulith.inventory.web;

import de.unistuttgart.t2.modulith.inventory.repository.DataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Defines endpoints for the DataGenerator.
 *
 * @author maumau
 */
@Controller
public class DataGeneratorController {

    private final DataGenerator dataGenerator;

    public DataGeneratorController(@Autowired DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    /**
     * trigger generation of new products TODO post x generation request seems more reasonable
     */
    @Operation(summary = "Populate the store with new products")
    @GetMapping("/generate")
    public @ResponseBody void generateData() {
        dataGenerator.generateProducts();
    }

    /**
     * trigger restock of all products TODO post x restock request seems more reasonable
     */
    @Operation(summary = "Restock units of the store's products")
    @GetMapping("/restock")
    public @ResponseBody void restock() {
        dataGenerator.restockProducts();
    }
}
