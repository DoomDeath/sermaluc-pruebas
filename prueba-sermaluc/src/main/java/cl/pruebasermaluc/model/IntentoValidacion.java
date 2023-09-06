package cl.pruebasermaluc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "intentos_validacion")
@Data
public class IntentoValidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "archivo_id", nullable = false)
    private Archivo archivo;

    @Column(name = "fecha_intento", nullable = false)
    private Date fechaIntento;

    @Column(name = "resultado_validacion", nullable = false)
    private String resultadoValidacion;


}