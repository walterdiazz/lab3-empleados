package pe.edu.empresa.empleados.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pe.edu.empresa.empleados.model.Empleado;
import pe.edu.empresa.empleados.repository.EmpleadoRepository;


@Service
public class EmpleadoServiceImpl
        implements EmpleadoService {

    private final EmpleadoRepository repository;

    private final PasswordEncoder passwordEncoder;


    public EmpleadoServiceImpl(
            EmpleadoRepository repository,
            PasswordEncoder passwordEncoder
    ) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public List<Empleado> listarTodos() {

        return repository.findAll();
    }


    @Override
    public Empleado obtenerPorId(Long id) {

        return repository
                .findById(id)
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "Empleado no encontrado"
                    )
                );
    }


    @Override
    public Empleado crear(
            Empleado empleado
    ) {

        validarDatosBasicos(empleado);

        if (
            empleado.getPassword() == null ||
            empleado.getPassword().isBlank()
        ) {

            throw new IllegalArgumentException(
                "La contraseña es obligatoria"
            );
        }

        if (repository.existsByEmail(empleado.getEmail())) {

            throw new IllegalArgumentException(
                "Ya existe un empleado con ese email"
            );
        }

        empleado.setPassword(
            passwordEncoder.encode(empleado.getPassword())
        );

        return repository.save(empleado);
    }


    @Override
    public Empleado actualizar(
            Long id,
            Empleado datosEmpleado,
            String nuevaPassword
    ) {

        Empleado empleado =
            obtenerPorId(id);

        validarDatosBasicos(datosEmpleado);

        if (
            !empleado.getEmail().equals(datosEmpleado.getEmail()) &&
            repository.existsByEmail(datosEmpleado.getEmail())
        ) {

            throw new IllegalArgumentException(
                "Ya existe un empleado con ese email"
            );
        }

        empleado.setNombre(
            datosEmpleado.getNombre()
        );

        empleado.setEmail(
            datosEmpleado.getEmail()
        );

        empleado.setRol(
            datosEmpleado.getRol()
        );

        empleado.setEstado(
            datosEmpleado.getEstado()
        );

        if (
            nuevaPassword != null &&
            !nuevaPassword.isBlank()
        ) {

            empleado.setPassword(
                passwordEncoder.encode(nuevaPassword)
            );
        }

        return repository.save(empleado);
    }


    @Override
    public void eliminar(Long id) {

        Empleado empleado =
            obtenerPorId(id);

        repository.delete(empleado);
    }


    private void validarDatosBasicos(
            Empleado empleado
    ) {

        if (
            empleado.getNombre() == null ||
            empleado.getNombre().isBlank()
        ) {

            throw new IllegalArgumentException(
                "El nombre es obligatorio"
            );
        }


        if (
            empleado.getEmail() == null ||
            empleado.getEmail().isBlank()
        ) {

            throw new IllegalArgumentException(
                "El email es obligatorio"
            );
        }


        if (empleado.getRol() == null) {

            throw new IllegalArgumentException(
                "El rol es obligatorio"
            );
        }
    }
}
