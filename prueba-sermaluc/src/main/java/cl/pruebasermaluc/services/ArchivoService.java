package cl.pruebasermaluc.services;

import cl.pruebasermaluc.exceptions.ValidationError;
import cl.pruebasermaluc.model.*;
import cl.pruebasermaluc.repository.ArchivoRepository;
import cl.pruebasermaluc.repository.IntentoValidacionRepository;
import cl.pruebasermaluc.repository.RegistroRepository;
import cl.pruebasermaluc.repository.TrazaErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static cl.pruebasermaluc.constants.Constantes.FECHA_NO_VALIDA;
import static cl.pruebasermaluc.constants.Constantes.VALIDACION_ENTIDAD;
import static cl.pruebasermaluc.utils.DateUtils.formatearFecha;


@Service
public class ArchivoService {

    @Autowired
    TrazaErrorRepository trazaErrorRepository;

    @Autowired
    ArchivoRepository archivoRepository;

    @Autowired
    IntentoValidacionRepository intentoValidacionRepository;


    @Autowired
    RegistroRepository registroRepository;


    @Async
    public String procesarArchivoAsync(MultipartFile file, String nombreEntidadDesdeArchivo) {
        Archivo archivo = new Archivo();
        archivo.setNombreArchivo(file.getOriginalFilename());
        archivo.setEstadoArchivo("Recibido"); // Cambiado el estado inicial a "Recibido"

        try {
            archivo = archivoRepository.save(archivo);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                ValidationError resultadoValidacion = validarRegistro(lineNumber, line, nombreEntidadDesdeArchivo, archivo);
                if (line.trim().length() != 25) {

                    archivo.setEstadoArchivo("Procesado con errores");
                    logRegistro(archivo, line, archivo.getEstadoArchivo());
                    guardarTrazaError("G", archivo,
                            new ValidationError(400, "La línea " + lineNumber + " no tiene un largo de 25 caracteres."),
                            "Cada línea del archivo debe tener un largo exacto de 25 caracteres");
                }
                else if (resultadoValidacion != null) {
                    archivo.setEstadoArchivo("Procesado con errores");
                    logRegistro(archivo, line, archivo.getEstadoArchivo());
                    guardarTrazaError("G", archivo, resultadoValidacion, "FALTA");
                }
                else if (!contieneRegistro01(line)) {
                    archivo.setEstadoArchivo("Procesado con errores");
                    archivoRepository.save(archivo);
                    guardarTrazaError("G", archivo, new ValidationError(202, "La línea " + lineNumber + " No existe tipo de registro obligatorio."),
                            "Debe venir informado en el archivo un \n" +
                            "registro tipo 1 (01) ");
                }else {
                    logRegistro(archivo, line, "Validado");
                }
            }
            if (!"Procesado con errores".equals(archivo.getEstadoArchivo())) {
                archivo.setEstadoArchivo("Validado");

            }
            reader.close();
        } catch (IOException e) {
            archivo.setEstadoArchivo("Procesado con errores");
            e.printStackTrace();
        }

        actualizarEstadoArchivo(archivo);
        archivoRepository.save(archivo);
        guardarIntentoValidacion(archivo);

        return archivo.getEstadoArchivo(); // Devuelve el estado del archivo una vez procesado
    }
    private boolean contieneRegistro01(String line) {
        boolean contieneRegistro01 = false;
        if (line.startsWith("01")) {
            contieneRegistro01 = true;
        }
        return contieneRegistro01;
    }


    private Registro logRegistro(Archivo archivo, String contenidoRegistro, String estadoValidacion) {
        Registro registro = new Registro();
        registro.setArchivo(archivo);
        registro.setContenidoRegistro(contenidoRegistro);
        registro.setEstadoValidacion(estadoValidacion);

        return registroRepository.save(registro);
    }


    private void actualizarEstadoArchivo(Archivo archivo) {
        archivo.setEstadoArchivo(archivo.getEstadoArchivo());
        archivoRepository.save(archivo);
    }

    @Transactional
    private void guardarIntentoValidacion(Archivo archivo) {
        IntentoValidacion intentoValidacion = new IntentoValidacion();
        intentoValidacion.setArchivo(archivo);
        intentoValidacion.setFechaIntento(formatearFecha());
        intentoValidacion.setResultadoValidacion(archivo.getEstadoArchivo());
        intentoValidacionRepository.save(intentoValidacion);

    }


    private ValidationError validarRegistro(int lineNumber, String linea, String nombreEntidadDesdeArchivo, Archivo archivo) {
        System.out.println("registro = " + linea);
        if (linea.length() < 2) {
            guardarTrazaError("G", archivo, new ValidationError(300, "La línea " + lineNumber + " El campo 'Tipo de Registro' debe tener al menos 2 caracteres."), "El campo 'Tipo de Registro' debe tener al menos 2 caracteres.");
        }
        if (!esNumerico(linea.substring(0, 2))) {
            guardarTrazaError("G", archivo, new ValidationError(300, "La línea " + lineNumber + " El campo 'Tipo de Registro' debe ser numérico."), "El campo debe ser numérico. ");
         }

        // Validación entidad
        validarEntidad(lineNumber, linea, nombreEntidadDesdeArchivo, archivo);

        // Validación del campo "Fecha"
        String fechaStr = linea.substring(17, 25); // "Fecha" ocupa caracteres del 17 al 25 (9(08))
        ValidationError fechaValidation = validarFecha(lineNumber, fechaStr, archivo);
        if (fechaValidation != null) {
            return fechaValidation;
        }
        return null; // Retorna null si el registro es válido
    }

    private ValidationError validarEntidad(int lineNumber, String line, String nombreEntidadDesdeArchivo, Archivo archivo) {

        // Verifica si la longitud del campo "Entidad" es menor que 15 caracteres
        if (line.length() < 15) {
            // Completa los caracteres faltantes con espacios en blanco
            line = String.format("%-15s", line);
        }
        // Extrae el campo "Entidad" de la línea
        String entidad = line.substring(2, 17); // "Entidad" ocupa caracteres del 2 al 17 (basado en X(15))
        // Comparación con el nombre de la entidad obtenido del nombre del archivo
        if (!entidad.trim().equals(nombreEntidadDesdeArchivo.trim())) {
            guardarTrazaError("G", archivo, new ValidationError(2, "La línea " + lineNumber + " El campo 'Entidad' no coincide con el nombre de la entidad del archivo."), VALIDACION_ENTIDAD);
        }

        // Verifica si el campo "Entidad" es igual a "SERMALUC" o "NombreEntidad"
        if (!entidad.trim().equals("SERMALUC") && !entidad.trim().equals("NombreEntidad")) {
            guardarTrazaError("G", archivo, new ValidationError(2, "La línea " + lineNumber + " El campo 'Entidad' debe ser igual a 'SERMALUC' o 'NombreEntidad'."), VALIDACION_ENTIDAD);
        }

        return null;

    }

    private ValidationError validarFecha(int lineNumber, String fechaStr, Archivo archivo) {
        // Verifica que el campo "Fecha" tenga una longitud de 8 caracteres
        if (fechaStr.length() != 8) {
            //return new ValidationError(3, "El campo 'Fecha' debe tener una longitud de 8 caracteres.");
            guardarTrazaError("G", archivo, new ValidationError(3, "La línea " + lineNumber + " El campo 'Entidad' debe ser igual a 'SERMALUC' o 'NombreEntidad'."), VALIDACION_ENTIDAD);

        }

        // Verifica si todos los caracteres en el campo "Fecha" son numéricos
        if (!esNumerico(fechaStr)) {
            guardarTrazaError("G", archivo, new ValidationError(302, "La línea " + lineNumber + " El campo 'Fecha' debe ser numérico."),"El campo debe ser numérico." );
        }

        // Convierte la cadena de fecha en un valor numérico para validar el año
        int fechaNumerica = Integer.parseInt(fechaStr);
        int año = fechaNumerica / 10000;

        // Valida que el año sea mayor o igual a 1995
        if (año < 1995) {
            guardarTrazaError("M", archivo, new ValidationError(11, "La línea " + lineNumber + " El año en el campo 'Fecha' debe ser mayor o igual a 1995."), "El año debe ser mayor o igual a 1995. ");
        }

        // Valida si la fecha es válida según el calendario
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setLenient(false); // No permite fechas inválidas como 30 de Febrero
        try {
            Date fechaCampo = dateFormat.parse(fechaStr);

            // Verifica si la fecha del campo "Fecha" es mayor que la fecha del sistema
            Date fechaSistema = new Date(); // Fecha actual del sistema
            if (fechaCampo.after(fechaSistema)) {
                guardarTrazaError("L", archivo, new ValidationError(102, "La línea " + lineNumber + " La fecha en el campo 'Fecha' debe ser menor o igual a la fecha del sistema."), "Fecha <= a Fecha del Sistema");

            }
        } catch (ParseException e) {
            System.out.println("año + fechaStr = " + año + fechaStr);
            guardarTrazaError("G", archivo, new ValidationError(100, "La línea " + lineNumber + " La fecha en el campo 'Fecha' debe ser menor o igual a la fecha del sistema."), FECHA_NO_VALIDA);
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

    public void guardarTrazaError(String gravedad, Archivo archivo, ValidationError resultadoValidacion, String validacionError) {

        TrazaError trazaError = new TrazaError();
        trazaError.setArchivo(archivo);
        trazaError.setCodigoError(resultadoValidacion.getCode());
        trazaError.setGravedadError(gravedad);
        trazaError.setValidacionError(validacionError);
        trazaError.setMensajeError(resultadoValidacion.getMessage());
        trazaError.setFechaRegistro(formatearFecha());
        trazaErrorRepository.save(trazaError);
    }

    public Archivo obtenerArchivoPorId(Long archivoId) {
        Optional<Archivo> archivoOptional = archivoRepository.findById(archivoId);
        return archivoOptional.orElse(null);
    }

    public List<Registro> obtenerRegistrosPorArchivo(Archivo archivo) {
        return registroRepository.findByArchivo(archivo);
    }
    public List<Registro> obtenerRegistrosValidadosPorArchivo(Long archivoId, String estado) {
        return registroRepository.findByArchivoIdAndEstadoValidacion(archivoId ,estado);
    }

    public List<TrazaError> obtenerTrazasErroresPorArchivo(Long archivoId) {
        return trazaErrorRepository.findByArchivoId(archivoId);
    }

    public List<TrazaError> obtenerDetalleValidacionPorArchivoYCodigoError(Long archivoId, Integer codigoError) {

        return trazaErrorRepository.findByArchivoIdAndCodigoError(archivoId, codigoError);
    }
}
