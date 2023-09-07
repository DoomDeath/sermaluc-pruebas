package cl.pruebasermaluc.model;


import lombok.Data;

@Data
public class DetalleValidacionResponse {

    private Long registroId;
    private String nombreRegistro;
    private Integer codigoError;
    private String mensajeError;

    // Constructor
    public DetalleValidacionResponse(Long registroId, String nombreRegistro, Integer codigoError, String mensajeError) {
        this.registroId = registroId;
        this.nombreRegistro = nombreRegistro;
        this.codigoError = codigoError;
        this.mensajeError = mensajeError;
    }

}
