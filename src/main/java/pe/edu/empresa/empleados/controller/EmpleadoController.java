package pe.edu.empresa.empleados.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.empresa.empleados.model.Empleado;
import pe.edu.empresa.empleados.model.Estado;
import pe.edu.empresa.empleados.model.Rol;
import pe.edu.empresa.empleados.service.EmpleadoService;


@Controller
@RequestMapping("/admin/empleados")
public class EmpleadoController {

    private final EmpleadoService service;


    public EmpleadoController(
            EmpleadoService service
    ) {

        this.service = service;
    }


    @GetMapping
    public String listar(
            Model model,
            Authentication authentication
    ) {

        model.addAttribute(
            "empleados",
            service.listarTodos()
        );

        boolean esAdmin =
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("usuarioActual", authentication.getName());

        return "empleados/lista";
    }


    @GetMapping("/nuevo")
    public String nuevo(
            Model model
    ) {

        model.addAttribute(
            "empleado",
            new Empleado()
        );

        agregarCatalogos(model);

        return "empleados/formulario";
    }


    @PostMapping
    public String crear(
            @ModelAttribute Empleado empleado,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            service.crear(empleado);

            redirectAttributes.addFlashAttribute(
                "mensaje",
                "Empleado creado correctamente"
            );

            return "redirect:/admin/empleados";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("error", ex.getMessage());
            model.addAttribute("empleado", empleado);
            agregarCatalogos(model);

            return "empleados/formulario";
        }
    }


    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
            "empleado",
            service.obtenerPorId(id)
        );

        agregarCatalogos(model);

        return "empleados/formulario";
    }


    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable Long id,
            @ModelAttribute Empleado empleado,
            @RequestParam(required = false) String nuevaPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            service.actualizar(
                id,
                empleado,
                nuevaPassword
            );

            redirectAttributes.addFlashAttribute(
                "mensaje",
                "Empleado actualizado correctamente"
            );

            return "redirect:/admin/empleados";

        } catch (IllegalArgumentException ex) {

            empleado.setId(id);

            model.addAttribute("error", ex.getMessage());
            model.addAttribute("empleado", empleado);
            agregarCatalogos(model);

            return "empleados/formulario";
        }
    }


    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        service.eliminar(id);

        redirectAttributes.addFlashAttribute(
            "mensaje",
            "Empleado eliminado correctamente"
        );

        return "redirect:/admin/empleados";
    }


    private void agregarCatalogos(Model model) {

        model.addAttribute("roles", Rol.values());
        model.addAttribute("estados", Estado.values());
    }
}
