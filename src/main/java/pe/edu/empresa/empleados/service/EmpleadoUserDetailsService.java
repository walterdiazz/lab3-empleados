package pe.edu.empresa.empleados.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pe.edu.empresa.empleados.model.Empleado;
import pe.edu.empresa.empleados.model.Estado;
import pe.edu.empresa.empleados.repository.EmpleadoRepository;


@Service
public class EmpleadoUserDetailsService
        implements UserDetailsService {

    private final EmpleadoRepository repository;


    public EmpleadoUserDetailsService(
            EmpleadoRepository repository
    ) {
        this.repository = repository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        Empleado empleado =
            repository
                .findByEmail(email)
                .orElseThrow(
                    () -> new UsernameNotFoundException(
                        "Credenciales inválidas"
                    )
                );

        return User.builder()
                .username(empleado.getEmail())
                .password(empleado.getPassword())
                .authorities(
                    "ROLE_" + empleado.getRol().name().toUpperCase()
                )
                .disabled(empleado.getEstado() == Estado.INACTIVO)
                .build();
    }
}
