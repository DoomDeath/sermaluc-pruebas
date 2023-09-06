package cl.pruebasermaluc.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "registros")
@Data
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "archivo_id", nullable = false)
    private Archivo archivo;

    @Column(name = "contenido_registro", nullable = false)
    private String contenidoRegistro;

    @Column(name = "estado_validacion", nullable = false)
    private String estadoValidacion;


}