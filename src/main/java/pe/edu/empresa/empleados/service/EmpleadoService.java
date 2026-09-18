package pe.edu.empresa.empleados.service;

import java.util.List;

import pe.edu.empresa.empleados.model.Empleado;


public interface EmpleadoService {

    List<Empleado> listarTodos();

    Empleado obtenerPorId(Long id);

    Empleado crear(Empleado empleado);

    Empleado actualizar(
        Long id,
        Empleado empleado,
        String nuevaPassword
    );

    void eliminar(Long id);
}
