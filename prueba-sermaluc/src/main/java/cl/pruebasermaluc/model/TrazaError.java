package cl.pruebasermaluc.model;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "traza_error")
@Data
public class TrazaError {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_error")
    private Integer codigoError;

    @Column(name = "gravedad_error")
    private Character gravedadError;

    @Column(name = "validacion_error")
    private String validacionError;

    @Column(name = "mensaje_error")
    private String mensajeError;

    @Column(name = "fecha_registro")
    private String fechaRegistro;

}
