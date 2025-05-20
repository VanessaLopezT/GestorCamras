package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.repository.CamaraRepository;
import com.example.gestorcamras.redis.IRedisCache;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.repository.UbicacionRepository;
import com.example.gestorcamras.repository.UsuarioRepository;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.dto.CamaraDTO;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CamaraServiceImpl implements CamaraService {

    @Autowired
    private CamaraRepository camaraRepository;

    @Autowired
    private IRedisCache<Camara> redisCache;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Solo si también manejas Propietario

    private static final String PREFIX_CACHE = "camara_";

    // Métodos de conversión entre entidad y DTO
    private CamaraDTO toDTO(Camara camara) {
        if (camara == null) return null;
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
    public Optional<Camara> obtenerPorNombreYEquipo(String nombre, Equipo equipo) {
        return camaraRepository.findByNombreAndEquipo(nombre, equipo);
    }


    public Camara toEntity(CamaraDTO dto) {
        if (dto == null) return null;
        Camara camara = new Camara();
        camara.setIdCamara(dto.getIdCamara());
        camara.setNombre(dto.getNombre());
        camara.setIp(dto.getIp());
        camara.setActiva(dto.isActiva());
        camara.setTipo(dto.getTipo());
        camara.setFechaRegistro(dto.getFechaRegistro());

        // Asociar Ubicacion
        if (dto.getUbicacionId() != null) {
            ubicacionRepository.findById(dto.getUbicacionId())
                    .ifPresent(camara::setUbicacion);
        }

// Asociar Equipo
        if (dto.getEquipoId() != null) {
            equipoRepository.findById(dto.getEquipoId())
                    .ifPresent(camara::setEquipo);
        }

// Asociar Propietario (si aplica)
        if (dto.getPropietarioId() != null) {
            usuarioRepository.findById(dto.getPropietarioId())
                    .ifPresent(camara::setPropietario);
        }

        return camara;
    }

    @Override
    public List<CamaraDTO> obtenerTodas() {
        return camaraRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CamaraDTO> obtenerPorId(Long id) {
        Optional<Camara> cacheada = redisCache.obtener(PREFIX_CACHE + id);
        if (cacheada.isPresent()) {
            return cacheada.map(this::toDTO);
        }
        Optional<Camara> camaraBD = camaraRepository.findById(id);
        camaraBD.ifPresent(cam -> redisCache.guardar(PREFIX_CACHE + id, cam));
        return camaraBD.map(this::toDTO);
    }

    @Override
    public CamaraDTO guardarCamara(CamaraDTO camaraDTO) {
        Camara camara = toEntity(camaraDTO);
        Camara guardada = camaraRepository.save(camara);
        redisCache.guardar(PREFIX_CACHE + guardada.getIdCamara(), guardada);
        return toDTO(guardada);
    }

    @Override
    public void eliminarCamara(Long id) {
        camaraRepository.deleteById(id);
        redisCache.eliminar(PREFIX_CACHE + id);
    }

    @Override
    public List<CamaraDTO> obtenerPorPropietario(Long idUsuario) {
        return camaraRepository.findByPropietarioIdUsuario(idUsuario)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CamaraDTO> obtenerPorUbicacion(Long idUbicacion) {
        return camaraRepository.findByUbicacionId(idUbicacion)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CamaraDTO> obtenerPorActiva(boolean activa) {
        return camaraRepository.findByActiva(activa)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CamaraDTO> obtenerPorTipo(String tipo) {
        return camaraRepository.findByTipo(tipo)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
}

