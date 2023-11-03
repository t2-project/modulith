package de.unistuttgart.t2.modulith.ui;

import de.unistuttgart.t2.modulith.ui.web.UIController;
import de.unistuttgart.t2.modulith.uibackend.UIBackendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WebUIControllerTests {

    private MockMvc mockMvc;

    @Mock
    private UIBackendService uiBackendService;

    @BeforeEach
    public void setup() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(new UIController(uiBackendService))
            .setViewResolvers(viewResolver)
            .build();
    }

    @Test
    public void index() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ui/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }

    @Test
    public void cart() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ui/cart/"))
            .andExpect(status().isOk())
            .andExpect(view().name("cart"));
    }

    @Test
    public void confirm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ui/confirm/"))
            .andExpect(status().isOk())
            .andExpect(view().name("order"));
    }
}
