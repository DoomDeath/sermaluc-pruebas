package cl.pruebasermaluc.repository;

import cl.pruebasermaluc.model.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivoRepository extends JpaRepository<Archivo, Long> {
    
}