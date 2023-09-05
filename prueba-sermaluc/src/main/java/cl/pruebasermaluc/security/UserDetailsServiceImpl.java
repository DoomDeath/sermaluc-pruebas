package cl.pruebasermaluc.security;

import cl.pruebasermaluc.model.Rol;
import cl.pruebasermaluc.model.Usuario;
import cl.pruebasermaluc.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.util.HashSet;
import java.util.Set;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("El Usuario con email " + email + " no existe."));

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Rol role : usuario.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getNombre()));
        }
        System.out.println("authorities = " + authorities);
        //return new UserDetailsImpl(usuario);
        return new org.springframework.security.core.userdetails.User(usuario.getEmail(), usuario.getPassword(), authorities);
    }


}
