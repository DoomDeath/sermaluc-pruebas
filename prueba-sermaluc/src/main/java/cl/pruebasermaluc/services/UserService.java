package cl.pruebasermaluc.services;


import cl.pruebasermaluc.model.Rol;
import cl.pruebasermaluc.model.Usuario;
import cl.pruebasermaluc.model.UsuarioActualizadoRequest;
import cl.pruebasermaluc.repository.ContactoRepository;
import cl.pruebasermaluc.repository.RolRepository;
import cl.pruebasermaluc.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class UserService {

    @Autowired
    ContactoRepository contactoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;


    @Autowired
    RolRepository rolRepository;


    public List<Usuario> getAllUsers() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Usuario actualizarUsuario(Integer id, UsuarioActualizadoRequest usuarioActualizado) throws RoleNotFoundException {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();

            // Actualizar los campos del usuario con los valores proporcionados en usuarioActualizado
            usuario.setEmail(usuarioActualizado.getEmail());
            usuario.setNombre(usuarioActualizado.getNombre());
            //usuario.setPassword(usuarioActualizado.getPassword());
            usuario.setPassword(new BCryptPasswordEncoder().encode(usuarioActualizado.getPassword()));

            Rol rol = rolRepository.findByNombre(usuarioActualizado.getRol());

            if (rol != null) {
                // Asignar el rol al usuario
                Set<Rol> roles = new HashSet<>();
                roles.add(rol);
                usuario.setRoles(roles);
            } else {
                throw new RoleNotFoundException("No se encontró un rol con el nombre proporcionado.");
            }
            return usuarioRepository.save(usuario);
        } else {
            throw new RoleNotFoundException("No se encontró un usuario con el nombre proporcionado.");
        }
    }
    public void eliminarUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }




}
