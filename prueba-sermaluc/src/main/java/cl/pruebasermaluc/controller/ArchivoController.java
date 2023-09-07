package cl.pruebasermaluc.controller;


import cl.pruebasermaluc.model.*;
import cl.pruebasermaluc.repository.ArchivoRepository;
import cl.pruebasermaluc.services.ArchivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/procesar-archivo")
public class ArchivoController {


    @Autowired
    ArchivoService archivoService;

    @Autowired
    ArchivoRepository archivoRepository;




    private static final String ARCHIVO_PATTERN = "^(\\d{8})_([a-zA-Z]{5,15})_(\\d{3})\\.DAT$";
    private static final Pattern archivoPattern = Pattern.compile(ARCHIVO_PATTERN);

    @PostMapping("/cargar")
    public ResponseEntity<Object> procesarArchivo(@RequestParam("file") MultipartFile file) throws InterruptedException {
        // Verifica si el archivo está vacío

        if (file.isEmpty()) {
            ArchivoResponse response = new ArchivoResponse(HttpStatus.NOT_FOUND.value(), "Archivo no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        }

        // Verifica si el nombre del archivo cumple con el formato especificado
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Verifica si el archivo ya ha sido validado y procesado anteriormente   //VALIDAR CON BASTIAN
        /*Archivo archivoExistente = archivoRepository.findByNombreArchivo(fileName);
        if (archivoExistente != null) {
            return ResponseEntity.badRequest().body("El archivo ya ha sido procesado y validado anteriormente.");
        }*/

        // Analiza el nombre del archivo para extraer el nombre de la entidad
        String[] parts = fileName.split("_"); // Suponiendo que el nombre de la entidad está entre guiones bajos
        String nombreEntidadDesdeArchivo = "";
        if (parts.length >= 2) {
            nombreEntidadDesdeArchivo = parts[1]; // La segunda parte debe ser el nombre de la entidad
        }

        Matcher matcher = archivoPattern.matcher(fileName);

        if (!matcher.matches()) {
            return ResponseEntity.badRequest().body("El nombre del archivo no cumple con el formato requerido.");
        }

        // Verifica el tamaño del archivo
        long fileSize = file.getSize();
        if (fileSize < 1024 || fileSize > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("El tamaño del archivo no cumple con los requisitos (1 KB - 2 MB).");
        }
        // Genera una ID única para el archivo utilizando UUID
        String archivoId = UUID.randomUUID().toString();

        System.out.println("archivoId = " + archivoId);

        // Inicia el procesamiento asíncrono del archivo
        String resultadoValidacion = archivoService.procesarArchivoAsync(file, nombreEntidadDesdeArchivo);

        if (resultadoValidacion != "Validado") {
            // Si se encuentra un problema de validación, devolver una respuesta de error
            ArchivoResponse response = new ArchivoResponse(HttpStatus.NOT_FOUND.value(), resultadoValidacion);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        }

        ArchivoResponse response = new ArchivoResponse(HttpStatus.OK.value(), "Archivo cargado y validado exitosamente.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{archivoId}/estado")
    public ResponseEntity<Object> consultarEstadoArchivo(@PathVariable Long archivoId) {

        Archivo archivo = archivoService.obtenerArchivoPorId(archivoId);

        if (archivo == null) {
            ArchivoResponse response = new ArchivoResponse(HttpStatus.NOT_FOUND.value(), "Archivo no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        //List<Registro> registros = archivoService.obtenerRegistrosPorArchivo(archivo);

        List<Registro> validos = archivoService.obtenerRegistrosValidadosPorArchivo(archivoId, "Validado");
        List<TrazaError> trazasErrores = archivoService.obtenerTrazasErroresPorArchivo(archivoId);

        Map<Integer, Integer> contadorErrores = new HashMap<>();
        int registrosOk = 0;
        int registrosNok = 0;

        for (TrazaError trazaError : trazasErrores) {
            int codigoError = trazaError.getCodigoError();
            contadorErrores.put(codigoError, contadorErrores.getOrDefault(codigoError, 0) + 1);
            if (!(codigoError == 0)) {
                registrosNok++;
            }
        }

        registrosOk = validos.size();

        // Crear ArchivoEstadoResponse con los datos recopilados
        ArchivoEstadoResponse estadoArchivoResponse = new ArchivoEstadoResponse();
        estadoArchivoResponse.setIdArchivo(archivo.getId());
        estadoArchivoResponse.setNombreArchivo(archivo.getNombreArchivo());
        estadoArchivoResponse.setEstadoArchivo(archivo.getEstadoArchivo());
        estadoArchivoResponse.setRegistrosOk(registrosOk);
        estadoArchivoResponse.setRegistrosNok(registrosNok);
        estadoArchivoResponse.setContadorErrores(contadorErrores);

        return ResponseEntity.ok(estadoArchivoResponse);
    }

    @GetMapping("/{archivoId}/detalle-validacion")
    public ResponseEntity<Object> obtenerDetalleValidacionArchivo(
            @PathVariable Long archivoId,
            @RequestParam(required = false) Integer codigoError) {
        // Obtener el archivo por su ID
        Archivo archivo = archivoService.obtenerArchivoPorId(archivoId);

        if (archivo == null) {
            ArchivoResponse response = new ArchivoResponse(HttpStatus.NOT_FOUND.value(), "Archivo no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Obtener el detalle de validación filtrado por código de error (si se proporciona)
        List<TrazaError> detalleValidacion = archivoService.obtenerDetalleValidacionPorArchivoYCodigoError(archivoId, codigoError);

        if (detalleValidacion.isEmpty()) {
            ArchivoResponse response = new ArchivoResponse(HttpStatus.NOT_FOUND.value(), "No existen detalles de validación con el código buscado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(response);
        }


        return ResponseEntity.ok(detalleValidacion);
    }




}
