package cl.pruebasermaluc.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "archivos")
@Data
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "estado_archivo", nullable = false)
    private String estadoArchivo;

    @OneToMany(mappedBy = "archivo")
    private List<Registro> registros;

    @OneToMany(mappedBy = "archivo")
    private List<IntentoValidacion> intentosValidacion;


}