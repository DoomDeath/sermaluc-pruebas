package cl.pruebasermaluc.repository;

import cl.pruebasermaluc.model.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Rol findByNombre(String nombre);
}
