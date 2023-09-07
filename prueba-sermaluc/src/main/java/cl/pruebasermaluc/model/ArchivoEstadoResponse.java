package cl.pruebasermaluc.model;

import lombok.Data;

import java.util.Map;


@Data
public class ArchivoEstadoResponse {


    private Long idArchivo;
    private String nombreArchivo;
    private String estadoArchivo;
    private int registrosOk;
    private int registrosNok;
    private Map<Integer, Integer> contadorErrores;


}
