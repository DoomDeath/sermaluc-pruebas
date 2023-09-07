package cl.pruebasermaluc.services;

import cl.pruebasermaluc.model.Feature;
import cl.pruebasermaluc.model.Rol;
import cl.pruebasermaluc.model.Usuario;
import cl.pruebasermaluc.model.UsuarioActualizadoRequest;
import cl.pruebasermaluc.repository.RolRepository;
import cl.pruebasermaluc.repository.UsuarioRepository;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.management.relation.RoleNotFoundException;
import java.util.*;


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



        UsuarioActualizadoRequest usuarioActualizadoRequest = new UsuarioActualizadoRequest();
        usuarioActualizadoRequest.setEmail("nuevoEmail@example.com");
        usuarioActualizadoRequest.setNombre("Nuevo Nombre");
        usuarioActualizadoRequest.setPassword("nuevoPassword");
        usuarioActualizadoRequest.setRol("ROL_SIMULADO");


        Feature privileges = new Feature();
        privileges.setId(1L);
        privileges.setNombre("VER_USUARIOS");

        Rol nuevoRol = new Rol();
        nuevoRol.setNombre("ROLE_ADMIN");
        nuevoRol.setId(1L);
        Set<Feature> featuresSet = new HashSet<>();
        featuresSet.add(privileges);
        nuevoRol.setFeatures(featuresSet);

        Mockito.when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistenteSimulado));
        Mockito.when(rolRepository.findByNombre("ROL_SIMULADO")).thenReturn(nuevoRol); // Simular que se encuentra el rol
        Mockito.when(usuarioRepository.save(usuarioExistenteSimulado)).thenReturn(usuarioExistenteSimulado);

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

    @Test
    public void testActualizarUsuarioFalla() {


        int idNoExistente = 100; //no existe en la base de datos

        // Configurar un usuario existente simulado
        Usuario usuarioExistenteSimulado = new Usuario();
        usuarioExistenteSimulado.setId(1);
        usuarioExistenteSimulado.setEmail("usuarioExistente@example.com");
        usuarioExistenteSimulado.setNombre("Usuario Existente");
        usuarioExistenteSimulado.setPassword("password");

        UsuarioActualizadoRequest usuarioActualizadoRequest = new UsuarioActualizadoRequest();
        usuarioActualizadoRequest.setEmail("nuevoEmail@example.com");
        usuarioActualizadoRequest.setNombre("Nuevo Nombre");
        usuarioActualizadoRequest.setPassword("nuevoPassword");
        usuarioActualizadoRequest.setRol("ROL_SIMULADO");

        Feature privileges = new Feature();
        privileges.setId(1L);
        privileges.setNombre("VER_USUARIOS");

        Rol nuevoRol = new Rol();
        nuevoRol.setNombre("ROLE_ADMIN");
        nuevoRol.setId(1L);
        Set<Feature> featuresSet = new HashSet<>();
        featuresSet.add(privileges);
        nuevoRol.setFeatures(featuresSet);

        // Configurar el comportamiento simulado para usuarioRepository.findById
        when(usuarioRepository.findById(idNoExistente)).thenReturn(Optional.empty()); // Simular que el ID no se encuentra

        // Llamar al método
        assertThrows(RoleNotFoundException.class, () -> usuarioService.actualizarUsuario(idNoExistente, usuarioActualizadoRequest));

        // Verificar que el método findById se haya llamado una vez en usuarioRepository con el ID especificado
        verify(usuarioRepository, times(1)).findById(idNoExistente);
    }

    @Test
    public void testAsignarRolAUsuarioRolNoEncontrado() {


        // Nombre de usuario y nombre de rol simulados
        Integer id = 1;
        Usuario usuarioExistenteSimulado = new Usuario();
        usuarioExistenteSimulado.setId(1);
        usuarioExistenteSimulado.setEmail("usuarioExistente@example.com");
        usuarioExistenteSimulado.setNombre("Usuario Existente");
        usuarioExistenteSimulado.setPassword("password");

        UsuarioActualizadoRequest usuarioActualizadoRequest = new UsuarioActualizadoRequest();
        usuarioActualizadoRequest.setEmail("NUEVOEMAIL@NUEVO:COM");
        usuarioActualizadoRequest.setRol("ROLE_NOEXISTENTE");
        usuarioActualizadoRequest.setPassword("NUEVO_PASSWORD");
        usuarioActualizadoRequest.setNombre("NUEVONOMBRE");

        Mockito.when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioExistenteSimulado));

        // Configura el comportamiento simulado para rolRepository.findByNombre
        when(rolRepository.findByNombre(usuarioActualizadoRequest.getRol())).thenReturn(null);

        // Realiza el llamado al método y verifica que se lance la excepción RoleNotFoundException
        try {
            usuarioService.actualizarUsuario(id, usuarioActualizadoRequest);
            fail("Se esperaba que se lanzara RoleNotFoundException");
        } catch (RoleNotFoundException e) {
            // Verifica que el mensaje de la excepción sea el esperado
            assertEquals("No se encontró un rol con el nombre proporcionado.", e.getMessage());
        }

        // Verifica que rolRepository.findByNombre se haya llamado una vez con el nombre de rol proporcionado
        verify(rolRepository, times(1)).findByNombre(usuarioActualizadoRequest.getRol());
    }




    @Test
    public void getAllUsers() {
        // Crea una lista simulada de usuarios
        List<Usuario> usuariosSimulados = new ArrayList<>();
        Usuario usuario1 = new Usuario(1, "usuario1@example.com", "Usuario 1", "password1");
        Usuario usuario2 = new Usuario(2, "usuario2@example.com", "Usuario 2", "password2");

        usuariosSimulados.add(usuario1);
        usuariosSimulados.add(usuario2);

        Mockito.when(usuarioRepository.findAll()).thenReturn(usuariosSimulados);

        List<Usuario> usuariosObtenidos = usuarioService.getAllUsers();

        // Verifica que el método findAll() se haya llamado una vez en usuarioRepository
        verify(usuarioRepository, times(1)).findAll();

        // Verifica que la lista de usuarios obtenidos coincida con la lista simulada
        assertEquals(usuariosSimulados, usuariosObtenidos);



    }

    @Test
    public void obtenerUsuarioPorIdTest(){

        // Crea un usuario simulado
        Usuario usuarioSimulado = new Usuario(1, "usuario@example.com", "Usuario Simulado", "password");

        // Configura el comportamiento simulado para usuarioRepository.findById
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioSimulado));


        Optional<Usuario> usuarioObtenido = usuarioService.obtenerUsuarioPorId(1);

        // Verifica que el método findById se haya llamado una vez en usuarioRepository
        verify(usuarioRepository, times(1)).findById(1);

        assertTrue(usuarioObtenido.isPresent()); // Verifica que el Optional no esté vacío
        assertEquals(usuarioSimulado, usuarioObtenido.get());
    }
    @Test
    public void eliminarUsuarioTest() {

        // ID del usuario a eliminar
        int idUsuarioAEliminar = 1;


        doNothing().when(usuarioRepository).deleteById(idUsuarioAEliminar);
        usuarioService.eliminarUsuario(idUsuarioAEliminar);

        // Verifica que el método deleteById se haya llamado una vez en usuarioRepository con el ID especificado
        verify(usuarioRepository, times(1)).deleteById(idUsuarioAEliminar);
    }



}

