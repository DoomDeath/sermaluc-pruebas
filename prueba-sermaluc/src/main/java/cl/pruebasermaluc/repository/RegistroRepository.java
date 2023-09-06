package cl.pruebasermaluc.repository;

import cl.pruebasermaluc.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository extends JpaRepository<Registro, Long> {

}