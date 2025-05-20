package com.example.gestorcamras.service.impl;


import com.example.gestorcamras.dto.*;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Ubicacion;
import com.example.gestorcamras.repository.UbicacionRepository;
import com.example.gestorcamras.service.CamaraService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.redis.IRedisCache;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.service.EquipoService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private CamaraService camaraService;


    @Autowired
    private IRedisCache<Equipo> redisCache;

    private static final String PREFIX_CACHE = "equipo_";

    // Métodos de conversión entre entidad y DTO
    private EquipoDTO toDTO(Equipo equipo) {
        if (equipo == null) return null;
        return new EquipoDTO(
            equipo.getIdEquipo(),
            equipo.getNombreEquipo(),
            equipo.getIpAsignada(),
            equipo.getFechaRegistro()
        );
    }

    private Equipo toEntity(EquipoDTO dto) {
        if (dto == null) return null;
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(dto.getIdEquipo());
        equipo.setNombreEquipo(dto.getNombreEquipo());
        equipo.setIpAsignada(dto.getIpAsignada());
        equipo.setFechaRegistro(dto.getFechaRegistro());
        return equipo;
    }

    @Override
    public List<EquipoDTO> obtenerTodos() {
        return equipoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Equipo> obtenerEntidadPorId(Long id) {
        Optional<Equipo> cacheado = redisCache.obtener(PREFIX_CACHE + id);
        if (cacheado.isPresent()) {
            return cacheado;
        }
        Optional<Equipo> equipoBD = equipoRepository.findById(id);
        equipoBD.ifPresent(equipo -> redisCache.guardar(PREFIX_CACHE + id, equipo));
        return equipoBD;
    }

    @Override
    public Equipo guardarEntidad(Equipo equipo) {
        Equipo guardado = equipoRepository.save(equipo);
        redisCache.guardar(PREFIX_CACHE + guardado.getIdEquipo(), guardado);
        return guardado;
    }


    @Override
    public Optional<EquipoDTO> obtenerPorId(Long id) {
        Optional<Equipo> cacheado = redisCache.obtener(PREFIX_CACHE + id);
        if (cacheado.isPresent()) {
            return cacheado.map(this::toDTO);
        }
        Optional<Equipo> equipoBD = equipoRepository.findById(id);
        equipoBD.ifPresent(equipo -> redisCache.guardar(PREFIX_CACHE + id, equipo));
        return equipoBD.map(this::toDTO);
    }

    @Override
    public EquipoDTO guardarEquipo(EquipoDTO equipoDTO) {
        Equipo equipo = toEntity(equipoDTO);
        Equipo guardado = equipoRepository.save(equipo);
        redisCache.guardar(PREFIX_CACHE + guardado.getIdEquipo(), guardado);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        equipoRepository.delete(equipo); // se eliminan también las cámaras por cascade
        redisCache.eliminar(PREFIX_CACHE + id);
    }


    @Override
    public List<EquipoDTO> buscarPorNombre(String nombreEquipo) {
        return equipoRepository.findByNombreEquipoContainingIgnoreCase(nombreEquipo)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }


    @Override
    public EquipoDTO registrarEquipoConCamaras(RegistroEquipoRequest request) {
        // Crear ubicación
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(request.getLatitud());
        ubicacion.setLongitud(request.getLongitud());
        ubicacion.setDireccion(request.getDireccion());
        ubicacion = ubicacionRepository.save(ubicacion);

        // Crear equipo
        Equipo equipo = new Equipo();
        equipo.setNombreEquipo(request.getNombreEquipo());
        equipo.setIpAsignada(request.getIpAsignada());
        equipo.setFechaRegistro(LocalDateTime.now());

        List<Camara> camaras = new ArrayList<>();
        for (CamaraDTO cDto : request.getCamaras()) {
            Camara camara = new Camara();
            camara.setNombre(cDto.getNombre());
            camara.setTipo(cDto.getTipo());
            camara.setIp(cDto.getIp());
            camara.setActiva(true);
            camara.setFechaRegistro(LocalDateTime.now());
            camara.setUbicacion(ubicacion);
            camara.setEquipo(equipo);
            camaras.add(camara);
        }

        equipo.setCamaras(camaras);

        Equipo guardado = equipoRepository.save(equipo);

        redisCache.guardar("equipo_" + guardado.getIdEquipo(), guardado);

        return toDTO(guardado);
    }


    @Override
    public Optional<EquipoDetalleDTO> obtenerDetallePorId(Long idEquipo) {
        return equipoRepository.findById(idEquipo).map(equipo -> {
            EquipoDetalleDTO dto = new EquipoDetalleDTO();
            dto.setIdEquipo(equipo.getIdEquipo());
            dto.setNombreEquipo(equipo.getNombreEquipo());
            dto.setIpAsignada(equipo.getIpAsignada());
            dto.setFechaRegistro(equipo.getFechaRegistro());

            // Cámaras
            List<CamaraDTO> camaras = equipo.getCamaras().stream()
                    .map(this::mapCamaraToDTO)
                    .collect(Collectors.toList());
            dto.setCamaras(camaras);

            // Ubicación (de la primera cámara, asumiendo que todas comparten)
            if (!equipo.getCamaras().isEmpty() && equipo.getCamaras().get(0).getUbicacion() != null) {
                Ubicacion ubicacion = equipo.getCamaras().get(0).getUbicacion();
                UbicacionDTO uDTO = new UbicacionDTO();
                uDTO.setId(ubicacion.getId());
                uDTO.setLatitud(ubicacion.getLatitud());
                uDTO.setLongitud(ubicacion.getLongitud());
                uDTO.setDireccion(ubicacion.getDireccion());
                dto.setUbicacion(uDTO);
            }

            return dto;
        });
    }

    private CamaraDTO mapCamaraToDTO(Camara camara) {
        CamaraDTO dto = new CamaraDTO();
        dto.setIdCamara(camara.getIdCamara());
        dto.setNombre(camara.getNombre());
        dto.setIp(camara.getIp());
        dto.setActiva(camara.isActiva());
        dto.setTipo(camara.getTipo());
        dto.setFechaRegistro(camara.getFechaRegistro());
        dto.setUbicacionId(camara.getUbicacion() != null ? camara.getUbicacion().getId() : null);
        dto.setPropietarioId(camara.getPropietario() != null ? camara.getPropietario().getIdUsuario() : null);
        dto.setEquipoId(camara.getEquipo() != null ? camara.getEquipo().getIdEquipo() : null);
        return dto;
    }

    @Override
    @Transactional
    public EquipoDTO actualizarEquipoConCamaras(Long id, EquipoConCamarasDTO dto) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        // Actualizar datos básicos
        equipo.setNombreEquipo(dto.getNombreEquipo());
        equipo.setIpAsignada(dto.getIpAsignada());
        equipo.setFechaRegistro(dto.getFechaRegistro());

// Mapear cámaras existentes por ID
        Map<Long, Camara> camarasExistentesMap = equipo.getCamaras().stream()
                .filter(c -> c.getIdCamara() != null)
                .collect(Collectors.toMap(Camara::getIdCamara, Function.identity()));

        List<Camara> nuevasCamaras = new ArrayList<>();

        Set<Long> idsCamarasNuevas = new HashSet<>();

        for (CamaraDTO camDto : dto.getCamaras()) {
            Camara camara;
            if (camDto.getIdCamara() != null && camarasExistentesMap.containsKey(camDto.getIdCamara())) {
                // Actualizar cámara existente
                camara = camarasExistentesMap.get(camDto.getIdCamara());
                camara.setNombre(camDto.getNombre());
                camara.setIp(camDto.getIp());
                camara.setTipo(camDto.getTipo());
                camara.setActiva(camDto.isActiva());
                camara.setFechaRegistro(camDto.getFechaRegistro());
            } else {
                // Nueva cámara
                camara = camaraService.toEntity(camDto);
                camara.setFechaRegistro(camDto.getFechaRegistro() != null ? camDto.getFechaRegistro() : LocalDateTime.now());
                camara.setEquipo(equipo); // Relación bidireccional
            }

            // Asociar ubicación si existe
            if (camDto.getUbicacionId() != null) {
                ubicacionRepository.findById(camDto.getUbicacionId()).ifPresent(camara::setUbicacion);
            }

            nuevasCamaras.add(camara);
            if (camDto.getIdCamara() != null) {
                idsCamarasNuevas.add(camDto.getIdCamara());
            }
        }

// Eliminar cámaras que ya no están
        List<Camara> camarasAEliminar = equipo.getCamaras().stream()
                .filter(c -> c.getIdCamara() != null && !idsCamarasNuevas.contains(c.getIdCamara()))
                .collect(Collectors.toList());

        camarasAEliminar.forEach(equipo.getCamaras()::remove);

// Asignar nuevas cámaras (sustituye completamente)
        equipo.setCamaras(nuevasCamaras);

        Equipo guardado = equipoRepository.save(equipo);

        redisCache.guardar(PREFIX_CACHE + guardado.getIdEquipo(), guardado);

        return toDTO(guardado);
    }

    }

