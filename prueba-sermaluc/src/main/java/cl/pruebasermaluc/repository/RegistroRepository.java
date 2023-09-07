package cl.pruebasermaluc.repository;

import cl.pruebasermaluc.model.Archivo;
import cl.pruebasermaluc.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

    List<Registro> findByArchivo(Archivo archivo);

    List<Registro> findByArchivoIdAndEstadoValidacion(Long archivoId, String validado);
}