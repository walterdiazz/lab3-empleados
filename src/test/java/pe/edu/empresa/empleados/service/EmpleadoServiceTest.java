package pe.edu.empresa.empleados.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pe.edu.empresa.empleados.model.Empleado;
import pe.edu.empresa.empleados.model.Estado;
import pe.edu.empresa.empleados.model.Rol;
import pe.edu.empresa.empleados.repository.EmpleadoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


/**
 * Casos numerados (ver README, sección "Casos de prueba"):
 * 4.1 crear_conDatosValidos_debeEncriptarPasswordYGuardar
 * 4.2 crear_conEmailDuplicado_debeLanzarExcepcion
 * 4.3 crear_conNombreVacio_debeLanzarExcepcion
 * 4.4 actualizar_sinNuevaPassword_debeConservarHashOriginal
 * 4.5 actualizar_conNuevaPassword_debeReencriptar
 * 4.6 eliminar_empleadoExistente_debeInvocarDelete
 * 4.7 obtenerPorId_inexistente_debeLanzarExcepcion
 */
@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmpleadoServiceImpl service;


    @BeforeEach
    void setUp() {

        service = new EmpleadoServiceImpl(repository, passwordEncoder);
    }


    private Empleado empleadoValido() {

        Empleado empleado = new Empleado();
        empleado.setNombre("Maria Lopez");
        empleado.setEmail("maria@empresa.com");
        empleado.setRol(Rol.usuario);
        empleado.setPassword("maria123");
        empleado.setEstado(Estado.ACTIVO);

        return empleado;
    }


    @Test
    void crear_conDatosValidos_debeEncriptarPasswordYGuardar() {

        Empleado empleado = empleadoValido();

        when(repository.existsByEmail("maria@empresa.com")).thenReturn(false);
        when(passwordEncoder.encode("maria123")).thenReturn("HASH_BCRYPT");
        when(repository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado guardado = service.crear(empleado);

        assertThat(guardado.getPassword()).isEqualTo("HASH_BCRYPT");
        verify(repository).save(empleado);
    }


    @Test
    void crear_conEmailDuplicado_debeLanzarExcepcion() {

        Empleado empleado = empleadoValido();

        when(repository.existsByEmail("maria@empresa.com")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(empleado))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe");

        verify(repository, never()).save(any());
    }


    @Test
    void crear_conNombreVacio_debeLanzarExcepcion() {

        Empleado empleado = empleadoValido();
        empleado.setNombre("  ");

        assertThatThrownBy(() -> service.crear(empleado))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nombre");

        verifyNoInteractions(passwordEncoder);
    }


    @Test
    void actualizar_sinNuevaPassword_debeConservarHashOriginal() {

        Empleado existente = empleadoValido();
        existente.setId(1L);
        existente.setPassword("HASH_ORIGINAL");

        Empleado datosNuevos = empleadoValido();
        datosNuevos.setNombre("Maria Lopez Actualizada");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado actualizado = service.actualizar(1L, datosNuevos, null);

        assertThat(actualizado.getNombre()).isEqualTo("Maria Lopez Actualizada");
        assertThat(actualizado.getPassword()).isEqualTo("HASH_ORIGINAL");
        verify(passwordEncoder, never()).encode(anyString());
    }


    @Test
    void actualizar_conNuevaPassword_debeReencriptar() {

        Empleado existente = empleadoValido();
        existente.setId(1L);
        existente.setPassword("HASH_ORIGINAL");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("HASH_NUEVO");
        when(repository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado actualizado = service.actualizar(1L, empleadoValido(), "nuevaClave123");

        assertThat(actualizado.getPassword()).isEqualTo("HASH_NUEVO");
    }


    @Test
    void eliminar_empleadoExistente_debeInvocarDelete() {

        Empleado existente = empleadoValido();
        existente.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repository).delete(existente);
    }


    @Test
    void obtenerPorId_inexistente_debeLanzarExcepcion() {

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no encontrado");
    }
}
