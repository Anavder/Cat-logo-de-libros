package com.alura.literalura.dto;

public record AutorDTO(
        //dto transferencia de datos
        Long Id,
        String nombre,
        int fechaNacimiento,
        int fechaFallecimiento) {
}
