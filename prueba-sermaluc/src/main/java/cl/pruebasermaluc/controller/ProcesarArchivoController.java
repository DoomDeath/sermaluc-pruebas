package cl.pruebasermaluc.controller;


import java.io.IOException;

import cl.pruebasermaluc.exceptions.ValidationError;
import cl.pruebasermaluc.services.ProcesadorArchivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/procesar-archivo")
public class ProcesarArchivoController {


    @Autowired
    ProcesadorArchivoService procesadorArchivoService;


    private static final String ARCHIVO_PATTERN = "^(\\d{8})_([a-zA-Z]{5,15})_(\\d{3})\\.DAT$";
    private static final Pattern archivoPattern = Pattern.compile(ARCHIVO_PATTERN);

    @PostMapping("/cargar")
    public ResponseEntity<Object> procesarArchivo(@RequestParam("file") MultipartFile file) {
        // Verifica si el archivo está vacío
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }

        // Verifica si el nombre del archivo cumple con el formato especificado
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

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
        ValidationError resultadoValidacion = procesadorArchivoService.procesarArchivoAsync(file, nombreEntidadDesdeArchivo);

        if (resultadoValidacion != null) {
            // Si se encuentra un problema de validación, devolver una respuesta de error
            return ResponseEntity.badRequest().body(resultadoValidacion);
        }


        return ResponseEntity.status(HttpStatus.CREATED).body("Archivo cargado y validado exitosamente.");
    }



}
