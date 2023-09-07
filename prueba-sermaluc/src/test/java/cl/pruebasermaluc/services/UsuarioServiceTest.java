package cl.pruebasermaluc.services;

import cl.pruebasermaluc.model.Rol;
import cl.pruebasermaluc.model.Usuario;
import cl.pruebasermaluc.model.UsuarioActualizadoRequest;
import cl.pruebasermaluc.repository.RolRepository;
import cl.pruebasermaluc.repository.UsuarioRepository;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;


@RunWith(SpringJUnit4ClassRunner.class)
public class UsuarioServiceTest {


    @InjectMocks
    private UserService usuarioService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testActualizarUsuario() throws RoleNotFoundException {


        // Configurar un usuario existente simulado
        Usuario usuarioExistenteSimulado = new Usuario();
        usuarioExistenteSimulado.setId(1);
        usuarioExistenteSimulado.setEmail("usuarioExistente@example.com");
        usuarioExistenteSimulado.setNombre("Usuario Existente");
        usuarioExistenteSimulado.setPassword("password");

        Mockito.when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistenteSimulado));

        UsuarioActualizadoRequest usuarioActualizadoRequest = new UsuarioActualizadoRequest();
        usuarioActualizadoRequest.setEmail("nuevoEmail@example.com");
        usuarioActualizadoRequest.setNombre("Nuevo Nombre");
        usuarioActualizadoRequest.setPassword("nuevoPassword");
        usuarioActualizadoRequest.setRol("ROL_SIMULADO");

        when(rolRepository.findByNombre("ROL_SIMULADO")).thenReturn(new Rol()); // Simular que se encuentra el rol

        Usuario usuarioActualizado = usuarioService.actualizarUsuario(1, usuarioActualizadoRequest);

        // Verificar que los campos se hayan actualizado correctamente
        assertEquals("nuevoEmail@example.com", usuarioActualizado.getEmail());
        assertEquals("Nuevo Nombre", usuarioActualizado.getNombre());
        assertTrue(new BCryptPasswordEncoder().matches("nuevoPassword", usuarioActualizado.getPassword()));

        // Verificar que se haya encontrado el rol simulado
        verify(rolRepository, times(1)).findByNombre("ROL_SIMULADO");

        // Verificar que se haya guardado el usuario actualizado
        verify(usuarioRepository, times(1)).save(usuarioActualizado);
    }
}

