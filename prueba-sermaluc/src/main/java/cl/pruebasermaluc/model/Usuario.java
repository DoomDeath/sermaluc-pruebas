package cl.pruebasermaluc.model;


import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "Usuario")
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String email;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )

    private Set<Rol> roles = new HashSet<>();

    // Método para actualizar roles del usuario
    public void actualizarRoles(Set<Rol> nuevosRoles) {
        this.roles.clear();
        this.roles.addAll(nuevosRoles);
    }

}
