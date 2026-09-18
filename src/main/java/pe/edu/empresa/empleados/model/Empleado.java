package pe.edu.empresa.empleados.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        nullable = false,
        length = 100
    )
    private String nombre;

    @Column(
        nullable = false,
        unique = true,
        length = 150
    )
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private Rol rol;

    @Column(
        nullable = false,
        length = 100
    )
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private Estado estado = Estado.ACTIVO;

    @Column(
        name = "fecha_registro",
        nullable = false
    )
    private LocalDateTime fechaRegistro;


    @PrePersist
    public void prePersist() {

        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }

        if (estado == null) {
            estado = Estado.ACTIVO;
        }
    }


    public Empleado() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getNombre() {
        return nombre;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public Rol getRol() {
        return rol;
    }


    public void setRol(Rol rol) {
        this.rol = rol;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public Estado getEstado() {
        return estado;
    }


    public void setEstado(Estado estado) {
        this.estado = estado;
    }


    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }


    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }
}
