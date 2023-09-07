package cl.pruebasermaluc.services;


import cl.pruebasermaluc.model.Archivo;
import cl.pruebasermaluc.model.IntentoValidacion;
import cl.pruebasermaluc.model.Registro;
import cl.pruebasermaluc.model.TrazaError;
import cl.pruebasermaluc.repository.ArchivoRepository;
import cl.pruebasermaluc.repository.IntentoValidacionRepository;
import cl.pruebasermaluc.repository.RegistroRepository;
import cl.pruebasermaluc.repository.TrazaErrorRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static cl.pruebasermaluc.constants.Mocks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class ArchivoServiceTest {


    @InjectMocks
    ArchivoService archivoService;

    @Mock
    private MultipartFile mockFile;

    @Mock
    private ArchivoRepository archivoRepository;
    @Mock
    RegistroRepository registroRepositoryMock;

    @Mock
    IntentoValidacionRepository intentoValidacionRepository;

    @Mock
    TrazaErrorRepository trazaErrorRepository;



    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void procesarArchivoAsyncTestValido() throws IOException {
        Registro nuevoRegistro = new Registro();
        Archivo archivoSimulado = new Archivo();
        IntentoValidacion intentoValidacion = new IntentoValidacion();
        archivoSimulado.setNombreArchivo("20230329_SERMALUC_001.DAT");
        archivoSimulado.setEstadoArchivo("Recibido");

        String simulatedContent = "Este es un contenido simulado.\nOtra línea simulada.";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(DATA_ARCHIVO.getBytes());

        String entidad = "SERMALUC";

        Mockito.when(mockFile.getInputStream()).thenReturn(inputStream);
        Mockito.when(registroRepositoryMock.save(nuevoRegistro)).thenReturn(nuevoRegistro);
        Mockito.when(intentoValidacionRepository.save(intentoValidacion)).thenReturn(intentoValidacion);
        Mockito.when(mockFile.getOriginalFilename()).thenReturn("20230329_SERMALUC_001.DAT");
        Mockito.when(archivoRepository.save(archivoSimulado)).thenReturn(archivoSimulado);

        String resultado = archivoService.procesarArchivoAsync(mockFile, entidad);

        System.out.println("resultado = " + resultado);

        assertEquals("Validado", resultado);



    }

    @Test
    public void TestProcesadoConErroresFalta01() throws IOException { //TEST Debe venir informado en el archivo un registro tipo 1 (01)
        Registro nuevoRegistro = new Registro();
        Archivo archivoSimulado = new Archivo();
        IntentoValidacion intentoValidacion = new IntentoValidacion();
        TrazaError trazaError = new TrazaError();
        archivoSimulado.setNombreArchivo("20230329_SERMALUC_001.DAT");
        archivoSimulado.setEstadoArchivo("Recibido");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(DATA_ARCHIVO_ERROR_01.getBytes());

        String entidad = "SERMALUC";

        Mockito.when(mockFile.getInputStream()).thenReturn(inputStream);
        Mockito.when(registroRepositoryMock.save(nuevoRegistro)).thenReturn(nuevoRegistro);
        Mockito.when(intentoValidacionRepository.save(intentoValidacion)).thenReturn(intentoValidacion);
        Mockito.when(mockFile.getOriginalFilename()).thenReturn("20230329_SERMALUC_001.DAT");
        Mockito.when(archivoRepository.save(archivoSimulado)).thenReturn(archivoSimulado);
        Mockito.when(trazaErrorRepository.save(trazaError)).thenReturn(trazaError);

        String resultado = archivoService.procesarArchivoAsync(mockFile, entidad);

        System.out.println("resultado = " + resultado);

        assertEquals("Procesado con errores", resultado);

    }

    @Test
    public void TestProcesadoConErroresCaracteres() throws IOException { //TEST Cada línea del archivo debe tener un largo exacto de 25 caracteres
        Registro nuevoRegistro = new Registro();
        Archivo archivoSimulado = new Archivo();
        IntentoValidacion intentoValidacion = new IntentoValidacion();
        TrazaError trazaError = new TrazaError();
        archivoSimulado.setNombreArchivo("20230329_SERMALUC_001.DAT");
        archivoSimulado.setEstadoArchivo("Recibido");

        String simulatedContent = "Este es un contenido simulado.\nOtra línea simulada.";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(DATA_ARCHIVO_ERROR_CARACTERES.getBytes());

        String entidad = "SERMALUC";

        Mockito.when(mockFile.getInputStream()).thenReturn(inputStream);
        Mockito.when(registroRepositoryMock.save(nuevoRegistro)).thenReturn(nuevoRegistro);
        Mockito.when(intentoValidacionRepository.save(intentoValidacion)).thenReturn(intentoValidacion);
        Mockito.when(mockFile.getOriginalFilename()).thenReturn("20230329_SERMALUC_001.DAT");
        Mockito.when(archivoRepository.save(archivoSimulado)).thenReturn(archivoSimulado);
        Mockito.when(trazaErrorRepository.save(trazaError)).thenReturn(trazaError);

        String resultado = archivoService.procesarArchivoAsync(mockFile, entidad);

        System.out.println("resultado = " + resultado);

        assertEquals("Procesado con errores", resultado);

    }



}
