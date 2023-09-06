package cl.pruebasermaluc.repository;

import cl.pruebasermaluc.model.TrazaError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrazaErrorRepository extends JpaRepository<TrazaError, Long> {
}