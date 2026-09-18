package pe.edu.empresa.empleados.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.empresa.empleados.model.Empleado;


public interface EmpleadoRepository
        extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByEmail(String email);

    boolean existsByEmail(String email);
}
