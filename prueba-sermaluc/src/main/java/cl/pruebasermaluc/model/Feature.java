package cl.pruebasermaluc.model;


import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "Feature")
public class Feature {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
}
