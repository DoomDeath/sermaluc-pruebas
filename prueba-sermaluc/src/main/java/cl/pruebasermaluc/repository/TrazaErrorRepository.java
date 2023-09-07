package cl.pruebasermaluc.repository;

import cl.pruebasermaluc.model.DetalleValidacionResponse;
import cl.pruebasermaluc.model.TrazaError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrazaErrorRepository extends JpaRepository<TrazaError, Long> {
    List<TrazaError> findByArchivoId(Long archivoId);


    // Consulta para obtener detalles de validación filtrados por archivo y código de error
    //List<DetalleValidacionResponse> findByArchivoIdAndCodigoError(Long archivoId, Integer codigoError);

    List<TrazaError> findByArchivoIdAndCodigoError(Long archivoId, Integer codigoError);

}