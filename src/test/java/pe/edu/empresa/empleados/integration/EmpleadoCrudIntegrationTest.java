package pe.edu.empresa.empleados.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import pe.edu.empresa.empleados.model.Empleado;
import pe.edu.empresa.empleados.model.Estado;
import pe.edu.empresa.empleados.model.Rol;
import pe.edu.empresa.empleados.repository.EmpleadoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Casos numerados (ver README, sección "Casos de prueba"):
 * 3.1 crearEmpleado_autenticadoComoAdmin_debeGuardarYRedirigir
 * 3.2 listarEmpleados_debeMostrarTodosLosRegistros
 * 3.3 actualizarEmpleado_debeModificarDatosPersistidos
 * 3.4 eliminarEmpleado_comoAdmin_debeEliminarRegistro
 * 3.5 eliminarEmpleado_comoUsuario_debeSerRechazadoConForbidden
 */
@SpringBootTest
class EmpleadoCrudIntegrationTest {

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
    }


    private Empleado guardarEmpleado(String email, Rol rol) {

        Empleado empleado = new Empleado();
        empleado.setNombre("Empleado Prueba");
        empleado.setEmail(email);
        empleado.setRol(rol);
        empleado.setPassword(passwordEncoder.encode("clave123"));
        empleado.setEstado(Estado.ACTIVO);

        return repository.save(empleado);
    }


    @Test
    @WithMockUser(username = "admin@empresa.com", roles = "ADMIN")
    void crearEmpleado_autenticadoComoAdmin_debeGuardarYRedirigir() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.post("/admin/empleados")
                    .param("nombre", "Pedro Ruiz")
                    .param("email", "pedro@empresa.com")
                    .param("rol", "usuario")
                    .param("estado", "ACTIVO")
                    .param("password", "pedro123")
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/empleados"));

        assertThat(repository.existsByEmail("pedro@empresa.com")).isTrue();
    }


    @Test
    @WithMockUser(username = "admin@empresa.com", roles = "ADMIN")
    void listarEmpleados_debeMostrarTodosLosRegistros() throws Exception {

        guardarEmpleado("uno@empresa.com", Rol.usuario);
        guardarEmpleado("dos@empresa.com", Rol.admin);

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/empleados"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("uno@empresa.com")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("dos@empresa.com")));
    }


    @Test
    @WithMockUser(username = "admin@empresa.com", roles = "ADMIN")
    void actualizarEmpleado_debeModificarDatosPersistidos() throws Exception {

        Empleado existente = guardarEmpleado("pedro@empresa.com", Rol.usuario);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/admin/empleados/{id}", existente.getId())
                    .param("nombre", "Pedro Ruiz Actualizado")
                    .param("email", "pedro@empresa.com")
                    .param("rol", "admin")
                    .param("estado", "ACTIVO")
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/empleados"));

        Empleado actualizado = repository.findById(existente.getId()).orElseThrow();

        assertThat(actualizado.getNombre()).isEqualTo("Pedro Ruiz Actualizado");
        assertThat(actualizado.getRol()).isEqualTo(Rol.admin);
    }


    @Test
    @WithMockUser(username = "admin@empresa.com", roles = "ADMIN")
    void eliminarEmpleado_comoAdmin_debeEliminarRegistro() throws Exception {

        Empleado existente = guardarEmpleado("pedro@empresa.com", Rol.usuario);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/admin/empleados/{id}/eliminar", existente.getId())
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/empleados"));

        assertThat(repository.existsByEmail("pedro@empresa.com")).isFalse();
    }


    @Test
    @WithMockUser(username = "pedro@empresa.com", roles = "USUARIO")
    void eliminarEmpleado_comoUsuario_debeSerRechazadoConForbidden() throws Exception {

        Empleado existente = guardarEmpleado("otro@empresa.com", Rol.usuario);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/admin/empleados/{id}/eliminar", existente.getId())
                    .with(csrf())
            )
            .andExpect(status().isForbidden());

        assertThat(repository.existsByEmail("otro@empresa.com")).isTrue();
    }
}
