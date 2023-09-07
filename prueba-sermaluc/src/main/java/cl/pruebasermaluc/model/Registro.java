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
    @JoinColumn(name = "archivo_id")
    private Archivo archivo;

    @Column(name = "contenido_registro", length = 25)
    private String contenidoRegistro;

    @Column(name = "estado_validacion")
    private String estadoValidacion;


}