package pe.edu.empresa.empleados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {


    @GetMapping("/login")
    public String mostrarLogin() {

        return "login";
    }


    @GetMapping("/")
    public String raiz() {

        return "redirect:/admin/empleados";
    }
}
