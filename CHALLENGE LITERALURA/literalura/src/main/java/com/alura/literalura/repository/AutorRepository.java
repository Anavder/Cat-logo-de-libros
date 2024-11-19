package com.alura.literalura.repository;

import com.alura.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor,Long> {
    Optional<Autor> findByNombreContainingIgnoreCase(String nombreAutor);

    @Query("SELECT a FROM Autor a WHERE a.fechaNacimiento > :fecha")
    List<Autor> findByFechaNacimientoAfter(@Param("fecha") int fecha);

    @Query("SELECT a FROM Autor a WHERE a.fechaFallecimiento < :fecha")
    List<Autor> findByFechaFallecimientoBefore(@Param("fecha") int fecha);
}
