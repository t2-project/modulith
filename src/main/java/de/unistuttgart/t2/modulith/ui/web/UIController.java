package de.unistuttgart.t2.modulith.ui.web;

import de.unistuttgart.t2.modulith.inventory.Product;
import de.unistuttgart.t2.modulith.ui.domain.ItemToAdd;
import de.unistuttgart.t2.modulith.ui.domain.PaymentDetails;
import de.unistuttgart.t2.modulith.uibackend.UIBackendService;
import de.unistuttgart.t2.modulith.uibackend.exceptions.OrderNotPlacedException;
import de.unistuttgart.t2.modulith.uibackend.exceptions.ReservationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * Defines the http endpoints of the UI.
 *
 * @author maumau
 */
@Controller
@RequestMapping("/ui")
public class UIController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final UIBackendService uiBackendService;

    public UIController(@Autowired UIBackendService uiBackendService) {
        this.uiBackendService = uiBackendService;
    }

    ////// PAGES TO REALLY LOOK AT ///////////

    @GetMapping({"", "/"})
    public String index(Model model) {
        model.addAttribute("title", "T2-Project");
        return "index";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "Products");
        model.addAttribute("item", new ItemToAdd());

        List<Product> products = uiBackendService.getAllProducts();

        model.addAttribute("productslist", products);

        return "category";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        model.addAttribute("title", "Cart");
        model.addAttribute("item", new ItemToAdd());

        List<Product> products = uiBackendService.getProductsInCart(session.getId());

        model.addAttribute("OrderItems", products);

        return "cart";
    }

    @GetMapping("/confirm")
    public String confirm(Model model, HttpSession session) {

        model.addAttribute("title", "Confirm");
        model.addAttribute("details", new PaymentDetails());

        return "order";
    }

    ////////// ACTIONS /////////////

    @PostMapping("/add")
    public String add(@ModelAttribute("item") ItemToAdd item, Model model, HttpSession session) {

        LOG.debug("Add item to card: {} | SessionID: {}", item.toString(), session.getId());

        try {
            uiBackendService.addItemToCart(session.getId(), item.getProductId(), item.getUnits());
        } catch (ReservationFailedException e) {
            LOG.error(e.getMessage());
            model.addAttribute("messagetitle", "Adding item to cart failed!");
            model.addAttribute("messageparagraph", e.getMessage());
            return "error_page";
        }

        model.addAttribute("message", "Adding items to cart was successful!");

        return "product";
    }

    @PostMapping("/delete")
    public RedirectView delete(@ModelAttribute("item") ItemToAdd item, RedirectAttributes redirectAttributes,
                               HttpSession session) {

        LOG.debug("Delete item from card: {} | SessionID: {}", item.toString(), session.getId());

        uiBackendService.deleteItemFromCart(session.getId(), item.getProductId(), item.getUnits());

        return new RedirectView("/ui/cart", true);
    }

    @PostMapping("/confirm")
    public String confirm(@ModelAttribute("details") PaymentDetails details, Model model, HttpSession session) {

        LOG.debug("Confirm order | SessionID: {}", session.getId());

        try {
            uiBackendService.confirmOrder(session.getId(), details.getCardNumber(), details.getCardOwner(), details.getChecksum());
            model.addAttribute("title", "Confirmed");
        } catch (OrderNotPlacedException e) {
            LOG.error(e.getMessage());
            model.addAttribute("messagetitle", "Confirm order failed!");
            model.addAttribute("messageparagraph", e.getMessage());
            return "error_page";
        }

        model.addAttribute("message", "Order was executed successfully!");

        return "category";
    }


    ////////// UNDEFINED /////////////

    @RequestMapping("/**")
    public String error(Model model, HttpServletRequest request) {

        LOG.warn("Unknown UI path requested: " + request.getRequestURI());

        model.addAttribute("title", "Error");

        return "error_page";
    }
}
