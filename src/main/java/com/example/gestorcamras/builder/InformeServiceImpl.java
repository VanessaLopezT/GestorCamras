package com.example.gestorcamras.builder;

import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.model.Usuario;
import com.example.gestorcamras.model.Informe;
import com.example.gestorcamras.repository.InformeRepository;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la gestión de informes.
 * Utiliza el patrón Builder para la creación de informes.
 */
@Service
@Transactional
public class InformeServiceImpl implements InformeService {

    private final InformeRepository informeRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final InformeBuilder informeBuilder;

    @Autowired
    public InformeServiceImpl(InformeRepository informeRepository,
                            UsuarioRepository usuarioRepository,
                            EquipoRepository equipoRepository) {
        this.informeRepository = informeRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
        this.informeBuilder = new InformeBuilder();
    }

    @Override
    public List<Informe> obtenerTodos() {
        return (List<Informe>) informeRepository.findAll();
    }

    @Override
    public Optional<Informe> obtenerPorId(Long id) {
        return informeRepository.findById(id)
            .map(informe -> (Informe) informe);
    }

    @Override
    public Informe guardarInforme(Informe informe) {
        return (Informe) informeRepository.save(informe);
    }

    @Override
    public void eliminarInforme(Long id) {
        informeRepository.deleteById(id);
    }

    @Override
    public Informe crearInformeBasico(String titulo, String contenido, double tamaño, Usuario usuario) {
        Informe informe = new Informe();
        informe.setTitulo(titulo);
        informe.setContenido(contenido);
        informe.setTamaño(tamaño);
        informe.setFechaGeneracion(LocalDateTime.now());
        if (usuario != null) {
            informe.setUsuario(usuario);
        }
        return guardarInforme(informe);
    }

    @Override
    public Informe generarInformeEquipo(String titulo, String descripcion, Long equipoId, Long usuarioId) {
        // Obtener el equipo por su ID
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("No se encontró el equipo con ID: " + equipoId));
        
        // Obtener el usuario por su ID
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario con ID: " + usuarioId));
        
        // Usar el builder para crear el contenido HTML del informe
        String contenidoHtml = informeBuilder
                .conTitulo(titulo)
                .conEquipo(equipo)
                .conCamaras(equipo.getCamaras() != null ? 
                    new java.util.ArrayList<>(equipo.getCamaras()) : 
                    new java.util.ArrayList<>())
                .agregarSeccion("Descripción", descripcion)
                .construir();
        
        // Crear el objeto Informe
        Informe informe = new Informe();
        informe.setTitulo(titulo);
        informe.setContenido(contenidoHtml);
        informe.setFechaGeneracion(LocalDateTime.now());
        informe.setUsuario(usuario);
        
        return informeRepository.save(informe);
    }

    @Override
    public Informe generarInformeActividad(LocalDateTime fechaInicio, LocalDateTime fechaFin, Long usuarioId) {
        // Obtener el usuario que genera el informe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + usuarioId));
        
        // Obtener la lista de equipos activos en el período
        // Obtener todos los equipos y filtrar manualmente
        List<Equipo> equiposActivos = equipoRepository.findAll().stream()
            .filter(Objects::nonNull)
            .filter(Equipo::getActivo)
            .filter(e -> e.getUltimaConexion() != null && 
                       !e.getUltimaConexion().isBefore(fechaInicio) && 
                       !e.getUltimaConexion().isAfter(fechaFin))
            .collect(Collectors.toList());
        
        List<Equipo> equiposEnPeriodo = equiposActivos;
        
        // Construir el contenido del informe
        StringBuilder contenido = new StringBuilder();
        contenido.append("# Informe de Actividad del Sistema\n\n");
        contenido.append(String.format("Período: %s - %s\n\n", 
            fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        
        contenido.append(String.format("## Total de equipos activos: %d\n\n", equiposEnPeriodo.size()));
        
        // Resumen por equipo
        contenido.append("## Resumen por Equipo\n");
        for (Equipo equipo : equiposEnPeriodo) {
            contenido.append(String.format("### %s\n", equipo.getNombre()));
            contenido.append(String.format("- **IP:** %s\n", equipo.getIp()));
            contenido.append(String.format("- **Puerto:** %d\n", equipo.getPuerto()));
            if (equipo.getUltimaConexion() != null) {
                contenido.append(String.format("- **Última Conexión:** %s\n", 
                    equipo.getUltimaConexion().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            }
            contenido.append("\n");
        }
        
        // Crear y devolver el informe
        String titulo = String.format("Informe de Actividad %s - %s", 
            fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
            fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE));
            
        return crearInformeBasico(
            titulo,
            contenido.toString(),
            contenido.length() * 2L, // Estimación del tamaño
            usuario
        );
    }
}
