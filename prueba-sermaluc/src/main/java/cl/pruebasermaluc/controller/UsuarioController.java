package cl.pruebasermaluc.controller;



import cl.pruebasermaluc.model.Usuario;
import cl.pruebasermaluc.model.UsuarioActualizadoRequest;
import cl.pruebasermaluc.security.WebSecurityService;
import cl.pruebasermaluc.services.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UsuarioController {




    @Autowired
    UserService userService;




    //@Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        List<Usuario> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Usuario obtenerUsuarioPorId(@PathVariable Integer id) throws RoleNotFoundException {
        Optional<Usuario> usuario = userService.obtenerUsuarioPorId(id);
        if (usuario.isPresent()) {
            return usuario.get();
        } else {
            throw new RoleNotFoundException("No se encontró un usuario con el nombre proporcionado.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarUsuario(@PathVariable Integer id, @RequestBody UsuarioActualizadoRequest usuarioActualizado) throws RoleNotFoundException {
        Usuario usuario = userService.actualizarUsuario(id, usuarioActualizado);
        if (usuario != null) {
            return new ResponseEntity<>("USUARIO ACTUALIZADO CORRECTAMENTE", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Integer id) {
        userService.eliminarUsuario(id);
        return new ResponseEntity<>("Usuario eliminado correctamente", HttpStatus.OK);
    }

}
