package cl.pruebasermaluc.model;


import lombok.Data;

@Data
public class UsuarioActualizadoRequest {


    private String nombre;
    private String email;
    private String password;
    private String rol;
}
