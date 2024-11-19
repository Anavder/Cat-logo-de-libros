package com.alura.literalura.principal;

import com.alura.literalura.model.Autor;
import com.alura.literalura.model.DatosAutor;
import com.alura.literalura.model.DatosLibro;
import com.alura.literalura.model.Libro;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import com.alura.literalura.service.ConvierteDatosAutor;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private ConvierteDatosAutor conversorAutor = new ConvierteDatosAutor();
    private final String URL_BASE = "https://gutendex.com/books/";
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository libRepositorio;
    private AutorRepository autorRepository;
    private List<Libro> libros;
    private List<Autor> autores;
    private Optional<Autor> autorBuscado;

    // Constructor
    public Principal(LibroRepository libRepository, AutorRepository autRepository) {
        this.libRepositorio = libRepository;
        this.autorRepository = autRepository;
    }

    // Método para mostrar el menú en consola
    public void muestraElMenu() {
        int opcion = -1;
        while (opcion != 0) {
            try {
                mostrarMenu();
                opcion = obtenerOpcionDelUsuario();
                procesarOpcion(opcion);
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número del 0 al 10.");
                teclado.nextLine(); // Limpiar el buffer del scanner
            }
        }
        System.out.println("Cerrando la aplicación...");
        System.exit(0);
    }

    // Método para mostrar el menú en consola
    private void mostrarMenu() {
        var menu = """
                Seleccione una opción del menú ingresando el número correspondiente:
                
                1- Consultar y guardar libros desde la API 
                2- Listar libros registrados en la BD
                3- Listar autores registrados en la BD
                4- Buscar autores vivos en un determinado año de la BD
                5- Buscar libros registrados en la BD por idioma
                6- Buscar autores por nombre en la BD
                7- Buscar los 10 libros más descargados de la API
                8- Buscar los 10 libros más descargados en la BD
                0- Salir
                """;
        System.out.println(menu);
    }
    private int obtenerOpcionDelUsuario() {
        System.out.print("Ingrese su opción: ");
        return teclado.nextInt();
    }
    private void procesarOpcion(int opcion) {
        teclado.nextLine(); // Limpiar el buffer del scanner
        System.out.println(); // Salto de línea después de seleccionar la opción
        switch (opcion) {
            case 1:
                buscarYGuardarLibroAPI();
                break;
            case 2:
                mostrarLibrosBaseDatos();
                break;
            case 3:
                mostrarAutoresBaseDatos();
                break;
            case 4:
                mostrarAutoresVivosEnUnDeterminadoAno();
                break;
            case 5:
                mostrarLibrosPorIdioma();
                break;
            case 6:
                buscarAutorPorNombreEnBD();
                break;
            case 7:
                buscarLibrosTop10EnAPI();
                break;

            case 0:
                break;
            default:
                System.out.println("Opción inválida. Por favor, intente nuevamente.");
        }
        System.out.println(); // Salto de línea después de procesar la opción
    }
    private DatosLibro obtenerDatosLibroAPI(String nombreLibro) {
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        return conversor.obtenerDatos(json, DatosLibro.class);
    }


    private DatosAutor obtenerDatosAutorAPI(String nombreLibro) {
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        return conversorAutor.obtenerDatos(json, DatosAutor.class);
    }


    public void buscarYGuardarLibroAPI() {
        System.out.println("¿Cuál es el título del libro que desea buscar en la API Gutendex?");
        String libroBuscado = teclado.nextLine();
        libros = libros != null ? libros : new ArrayList<>();
        Optional<Libro> optionalLibro = libros.stream()
                .filter(l -> l.getTitulo().toLowerCase().contains(libroBuscado.toLowerCase()))
                .findFirst();
        if (optionalLibro.isPresent()) {
            var libroEncontrado = optionalLibro.get();
            System.out.println("Este libro ya ha sido cargado previamente:");
            System.out.println(libroEncontrado);
            System.out.println("Por favor, pruebe con otro título.");
        } else {
            try {
                DatosLibro datosLibro = obtenerDatosLibroAPI(libroBuscado);
                if (datosLibro != null) {
                    DatosAutor datosAutor = obtenerDatosAutorAPI(libroBuscado);
                    if (datosAutor != null) {
                        List<Autor> autores = autorRepository.findAll();
                        autores = autores != null ? autores : new ArrayList<>();
                        Optional<Autor> autorOptional = autores.stream()
                                .filter(a -> datosAutor.nombre() != null &&
                                        a.getNombre().toLowerCase().contains(datosAutor.nombre().toLowerCase()))
                                .findFirst();
                        Autor autor;
                        if (autorOptional.isPresent()) {
                            autor = autorOptional.get();
                        } else {
                            autor = new Autor(
                                    datosAutor.nombre(),
                                    datosAutor.fechaNacimiento(),
                                    datosAutor.fechaFallecimiento()
                            );
                            autorRepository.save(autor);
                        }
                        Libro libro = new Libro(
                                datosLibro.titulo(),
                                autor,
                                datosLibro.idioma() != null ? datosLibro.idioma() : Collections.emptyList(),
                                datosLibro.descargas()
                        );
                        libros.add(libro);
                        autor.setLibros(libros);
                        System.out.println("Se ha encontrado el siguiente libro:");
                        System.out.println(libro);
                        libRepositorio.save(libro);
                        System.out.println("El libro ha sido guardado exitosamente.");
                    } else {
                        System.out.println("No se encontró información sobre el autor para este libro.");
                    }
                } else {
                    System.out.println("No se encontró el libro con el título proporcionado.");
                }
            } catch (Exception e) {
                System.out.println("Se produjo una excepción: " + e.getMessage());
            }
        }
    }


    private void mostrarLibrosBaseDatos() {
        System.out.println("**********************************************************************************");
        System.out.println("------------------- Libros registrados en la base de datos -----------------------");

        try {
            List<Libro> libros = libRepositorio.findAll();
            libros.stream()
                    .sorted(Comparator.comparing(Libro::getDescargas))
                    .forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            libros = new ArrayList<>();
        }
    }


    public void mostrarAutoresBaseDatos() {
        System.out.println("**********************************************************************************");
        System.out.println("--------------------- Autores registrados en la Base de Datos --------------------");
        autores = autorRepository.findAll();
        autores.stream()
                .forEach(System.out::println);
    }


    public void mostrarAutoresVivosEnUnDeterminadoAno() {
        System.out.println("**********************************************************************************");
        System.out.println("------------- Búsqueda de autores vivos en un año especifico ---------------------");

        System.out.print("Ingrese un año: ");
        int anio = teclado.nextInt();
        List<Autor> autores = autorRepository.findAll();
        List<String> autoresNombre = autores.stream()
                .filter(a -> a.getFechaFallecimiento() >= anio && a.getFechaNacimiento() <= anio)
                .map(Autor::getNombre)
                .collect(Collectors.toList());

        if (autoresNombre.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anio);
        } else {

            System.out.println("Autores vivos en el año " + anio + ":");
            autoresNombre.forEach(System.out::println);

        }
    }


    public void mostrarLibrosPorIdioma() {
        libros = libRepositorio.findAll();
        System.out.println("**********************************************************************************");
        System.out.println("-------------- Búsqueda de libros registrados en la BD por idioma ----------------");

        System.out.println("Ingrese el idioma del que desea buscar los libros: en (english) o es (español)");
        String idiomaBuscado = teclado.nextLine();
        List<Libro> librosBuscados = libros.stream()
                .filter(l -> l.getIdioma().contains(idiomaBuscado))
                .collect(Collectors.toList());
        librosBuscados.forEach(System.out::println);
    }

    // Método para buscar autor por nombre en la base de datos
    public void buscarAutorPorNombreEnBD() {
        System.out.println("**********************************************************************************");
        System.out.println("------------------ Búsqueda de un autor registrado en la BD ----------------------");

        System.out.println("Ingrese el nombre del autor que desea buscar");
        var nombreAutor = teclado.nextLine();
        autorBuscado = autorRepository.findByNombreContainingIgnoreCase(nombreAutor);
        if (autorBuscado.isPresent()) {
            System.out.println(autorBuscado.get());
        } else {
            System.out.println("Autor no encontrado");
        }
    }

    // Método para buscar los 10 libros más descargados de la API
    public void buscarLibrosTop10EnAPI() {
        System.out.println("**********************************************************************************");
        System.out.println("---------------- Top 10 de libros más descargados de la API ----------------------");
        try {
            String json = consumoAPI.obtenerDatos(URL_BASE + "?sort");

            List<DatosLibro> datosLibros = conversor.obtenerDatosArray(json, DatosLibro.class);
            List<DatosAutor> datosAutor = conversorAutor.obtenerDatosArray(json, DatosAutor.class);

            List<Libro> libros = new ArrayList<>();
            for (int i = 0; i < datosLibros.size(); i++) {
                Autor autor = new Autor(
                        datosAutor.get(i).nombre(),
                        datosAutor.get(i).fechaNacimiento(),
                        datosAutor.get(i).fechaFallecimiento());

                Libro libro = new Libro(
                        datosLibros.get(i).titulo(),
                        autor,
                        datosLibros.get(i).idioma(),
                        datosLibros.get(i).descargas());
                libros.add(libro);
            }

            libros.sort(Comparator.comparingDouble(Libro::getDescargas).reversed());

            List<Libro> top10Libros = libros.subList(0, Math.min(10, libros.size()));

            for (int i = 0; i < top10Libros.size(); i++) {
                System.out.println((i + 1) + ". " + top10Libros.get(i));
            }

        } catch (NullPointerException e) {
            System.out.println("Error al buscar los libros: " + e.getMessage());
        }
    }


    public void buscarLibrosTop10EnLaDB() {
        System.out.println("*******************************************************");
        System.out.println("Top 10 de libros más descargados registrados en  la BD");

        try {
            List<Libro> libros = libRepositorio.findAll();
            List<Libro> librosOrdenados = libros.stream()
                    .sorted(Comparator.comparingDouble(Libro::getDescargas).reversed())
                    .collect(Collectors.toList());
            List<Libro> topLibros = librosOrdenados.subList(0, Math.min(10, librosOrdenados.size()));
            for (int i = 0; i < topLibros.size(); i++) {
                System.out.println((i + 1) + ". " + topLibros.get(i));
            }
        } catch (NullPointerException e) {
            System.out.println("Error al buscar los libros en la base de datos: " + e.getMessage());
            libros = new ArrayList<>();
        }
    }


    }


