package cl.pruebasermaluc.services;

import cl.pruebasermaluc.exceptions.ValidationError;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Service
public class ProcesadorArchivoService {


    @Async
    public ValidationError procesarArchivoAsync(MultipartFile file, String nombreEntidadDesdeArchivo) {
        boolean contieneRegistro01 = false;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Procesar cada línea del archivo, puedes implementar la lógica de validación aquí
                ValidationError resultadoValidacion = validarRegistro(line, nombreEntidadDesdeArchivo);
                if (resultadoValidacion != null) {
                    // Si hay un problema de validación, devolver el mensaje de error
                    return resultadoValidacion;
                }
                // Verificar si la línea contiene un registro "01"
                if (line.startsWith("01")) {
                    contieneRegistro01 = true;
                }
                // Continuar procesando el archivo
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!contieneRegistro01) {
            return new ValidationError(202, "No existe tipo de registro obligatorio.");
        }

        // Si no se encontraron problemas de validación, devuelve null para indicar éxito
        return null;
    }


    private ValidationError validarRegistro(String registro, String nombreEntidadDesdeArchivo) {
        System.out.println("registro = " + registro);
        if (registro.length() < 2) {
            return new ValidationError(300, "El campo 'Tipo de Registro' debe tener al menos 2 caracteres.");
        }
        if (!esNumerico(registro.substring(0, 2))) {
            return new ValidationError(300, "El campo 'Tipo de Registro' debe ser numérico.");
        }
        // Verifica si la longitud del campo "Entidad" es menor que 15 caracteres
        if (registro.length() < 15) {
            // Completa los caracteres faltantes con espacios en blanco
            registro = String.format("%-15s", registro);
        }
        // Extrae el campo "Entidad" de la línea
        String entidad = registro.substring(2, 17); // Supongo que el campo "Entidad" ocupa caracteres del 2 al 17 (basado en X(15))
        // Comparación con el nombre de la entidad obtenido del nombre del archivo
        if (!entidad.trim().equals(nombreEntidadDesdeArchivo.trim())) {
            System.out.println("entidad = " + entidad.trim());
            System.out.println("registro = " + nombreEntidadDesdeArchivo.trim());
            return new ValidationError(2, "El campo 'Entidad' no coincide con el nombre de la entidad del archivo.");
        }

        // Verifica si el campo "Entidad" es igual a "SERMALUC" o "NombreEntidad"
        if (!entidad.trim().equals("SERMALUC") && !entidad.trim().equals("NombreEntidad")) {
            return new ValidationError(2, "El campo 'Entidad' debe ser igual a 'SERMALUC' o 'NombreEntidad'.");
        }
        return null; // Retorna null si el registro es válido
    }

    private boolean esNumerico(String valor) {
        try {
            Integer.parseInt(valor);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }



}
