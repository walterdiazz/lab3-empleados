package pe.edu.empresa.empleados.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import pe.edu.empresa.empleados.model.Empleado;
import pe.edu.empresa.empleados.model.Estado;
import pe.edu.empresa.empleados.model.Rol;
import pe.edu.empresa.empleados.repository.EmpleadoRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Casos numerados (ver README, sección "Casos de prueba"):
 * 1.1 login_credencialesCorrectas_debeAutenticarYRedirigirAlPanel
 * 1.2 login_passwordIncorrecta_debeQuedarNoAutenticadoYRedirigirConError
 * 1.3 login_usuarioInexistente_debeQuedarNoAutenticadoYRedirigirConError
 * 2.1 accederPanelSinSesion_debeRedirigirALogin
 */
@SpringBootTest
class LoginIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmpleadoRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        repository.deleteAll();

        Empleado admin = new Empleado();
        admin.setNombre("Administrador");
        admin.setEmail("admin@empresa.com");
        admin.setRol(Rol.admin);
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEstado(Estado.ACTIVO);

        repository.save(admin);
    }


    @Test
    void login_credencialesCorrectas_debeAutenticarYRedirigirAlPanel() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                    .param("email", "admin@empresa.com")
                    .param("password", "admin123")
                    .with(csrf())
            )
            .andExpect(authenticated())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/empleados"));
    }


    @Test
    void login_passwordIncorrecta_debeQuedarNoAutenticadoYRedirigirConError() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                    .param("email", "admin@empresa.com")
                    .param("password", "incorrecta")
                    .with(csrf())
            )
            .andExpect(unauthenticated())
            .andExpect(redirectedUrl("/login?error"));
    }


    @Test
    void login_usuarioInexistente_debeQuedarNoAutenticadoYRedirigirConError() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                    .param("email", "nadie@empresa.com")
                    .param("password", "cualquiera")
                    .with(csrf())
            )
            .andExpect(unauthenticated())
            .andExpect(redirectedUrl("/login?error"));
    }


    @Test
    void accederPanelSinSesion_debeRedirigirALogin() throws Exception {

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/empleados")
            )
            .andExpect(status().is3xxRedirection());
    }
}
