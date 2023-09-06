package cl.pruebasermaluc.services;

import cl.pruebasermaluc.exceptions.ValidationError;
import cl.pruebasermaluc.model.TrazaError;
import cl.pruebasermaluc.repository.TrazaErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class ProcesadorArchivoService {

    @Autowired
    private TrazaErrorRepository trazaErrorRepository;
    @Async
    public ValidationError procesarArchivoAsync(MultipartFile file, String nombreEntidadDesdeArchivo) {
        boolean contieneRegistro01 = false;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.length() != 25) {
                    guardarTrazaError(new ValidationError(400, "La línea " + lineNumber + " no tiene un largo de 25 caracteres."));
                    return new ValidationError(400, "La línea " + lineNumber + " no tiene un largo de 25 caracteres.");
                }
                // Procesar cada línea del archivo, puedes implementar la lógica de validación aquí
                ValidationError resultadoValidacion = validarRegistro(line, nombreEntidadDesdeArchivo);
                if (resultadoValidacion != null) {
                    guardarTrazaError(resultadoValidacion);
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

        // Validación entidad

        ValidationError entidadValidation = validarEntidad(registro, nombreEntidadDesdeArchivo);
        if (entidadValidation != null) {
            return entidadValidation;
        }

        // Validación del campo "Fecha"
        String fechaStr = registro.substring(17, 25); // Supongo que el campo "Fecha" ocupa caracteres del 17 al 25 (9(08))
        ValidationError fechaValidation = validarFecha(fechaStr);
        if (fechaValidation != null) {
            return fechaValidation;
        }
        return null; // Retorna null si el registro es válido
    }

    private ValidationError validarEntidad(String registro, String nombreEntidadDesdeArchivo) {

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

        return null;

    }

    private ValidationError validarFecha(String fechaStr) {
        // Verifica que el campo "Fecha" tenga una longitud de 8 caracteres
        if (fechaStr.length() != 8) {
            return new ValidationError(3, "El campo 'Fecha' debe tener una longitud de 8 caracteres.");
        }

        // Verifica si todos los caracteres en el campo "Fecha" son numéricos
        if (!esNumerico(fechaStr)) {
            return new ValidationError(302, "El campo 'Fecha' debe ser numérico.");
        }

        // Convierte la cadena de fecha en un valor numérico para validar el año
        int fechaNumerica = Integer.parseInt(fechaStr);
        int año = fechaNumerica / 10000;

        // Valida que el año sea mayor o igual a 1995
        if (año < 1995) {
            return new ValidationError(11, "El año en el campo 'Fecha' debe ser mayor o igual a 1995.");
        }

        // Valida si la fecha es válida según el calendario
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false); // No permite fechas inválidas como 30 de Febrero
        try {
            Date fechaCampo = dateFormat.parse(fechaStr);

            // Verifica si la fecha del campo "Fecha" es mayor que la fecha del sistema
            Date fechaSistema = new Date(); // Fecha actual del sistema
            if (fechaCampo.after(fechaSistema)) {
                return new ValidationError(102, "La fecha en el campo 'Fecha' debe ser menor o igual a la fecha del sistema.");
            }
        } catch (ParseException e) {
            System.out.println("año + fechaStr = " + año + fechaStr);
            return new ValidationError(100, "Fecha no válida.");
        }

        // Implementa la validación de la fecha según el calendario aquí (puedes utilizar bibliotecas de manejo de fechas)

        return null; // Retorna null si el campo "Fecha" es válido
    }


    private boolean esNumerico(String valor) {
        try {
            Integer.parseInt(valor);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void guardarTrazaError(ValidationError resultadoValidacion) {
        TrazaError trazaError = new TrazaError();
        trazaError.setCodigoError(resultadoValidacion.getCode());
        trazaError.setGravedadError('G');
        trazaError.setValidacionError(resultadoValidacion.getMessage());
        trazaError.setMensajeError(resultadoValidacion.getMessage());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = dateFormat.format(new Date());
        trazaError.setFechaRegistro(fechaFormateada);
        trazaErrorRepository.save(trazaError);
    }



}
