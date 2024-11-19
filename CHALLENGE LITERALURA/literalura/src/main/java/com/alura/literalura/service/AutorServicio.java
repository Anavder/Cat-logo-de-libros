package com.alura.literalura.service;

import com.alura.literalura.dto.AutorDTO;
import com.alura.literalura.model.Autor;
import com.alura.literalura.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutorServicio {

    @Autowired
    private AutorRepository repository;

    public List<AutorDTO> obtenerTodosLosAutores() {
        List<Autor> autores = repository.findAll();
        return convierteDatos(autores);
    }

    public List<AutorDTO> convierteDatos(List<Autor> autores) {

        return autores.stream()
                .map(a -> new AutorDTO(
                        a.getId(),
                        a.getNombre(),
                        a.getFechaNacimiento(),
                        a.getFechaFallecimiento()
                ))
                .collect(Collectors.toList());
    }
}
