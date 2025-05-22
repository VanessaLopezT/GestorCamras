package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.repository.CamaraRepository;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.repository.UbicacionRepository;
import com.example.gestorcamras.repository.UsuarioRepository;
import com.example.gestorcamras.service.CamaraService;
import com.example.gestorcamras.dto.CamaraDTO;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class CamaraServiceImpl implements CamaraService {

    @Autowired
    private CamaraRepository camaraRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Solo si también manejas Propietario

    @Autowired
    private ObjectMapper objectMapper;

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
        // Intentar obtener de la caché
        String cacheKey = PREFIX_CACHE + id;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue != null) {
            try {
                return Optional.of(objectMapper.readValue(cachedValue, CamaraDTO.class));
            } catch (JsonProcessingException e) {
                System.err.println("Error al deserializar cámara desde caché: " + e.getMessage());
            }
        }
        
        // Si no está en caché o hay error, obtener de la base de datos
        Optional<Camara> camaraBD = camaraRepository.findById(id);
        if (camaraBD.isPresent()) {
            Camara camara = camaraBD.get();
            // Inicializar relaciones necesarias
            if (camara.getEquipo() != null) {
                camara.getEquipo().getNombre();
            }
            CamaraDTO dto = toDTO(camara);
            
            // Guardar en caché
            try {
                String dtoJson = objectMapper.writeValueAsString(dto);
                redisTemplate.opsForValue().set(
                    cacheKey, 
                    dtoJson,
                    1, TimeUnit.HOURS // Expira después de 1 hora
                );
            } catch (JsonProcessingException e) {
                System.err.println("Error al serializar cámara para caché: " + e.getMessage());
            }
            
            return Optional.of(dto);
        }
        
        return Optional.empty();
    }

    @Override
    public CamaraDTO guardarCamara(CamaraDTO camaraDTO) {
        // Si es una actualización, limpiar la caché existente
        if (camaraDTO.getIdCamara() != null) {
            redisTemplate.delete(PREFIX_CACHE + camaraDTO.getIdCamara());
        }
        
        Camara camara = toEntity(camaraDTO);
        Camara guardada = camaraRepository.save(camara);
        
        // Convertir a DTO para la respuesta
        CamaraDTO dto = toDTO(guardada);
        
        // Guardar en caché
        try {
            String dtoJson = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(
                PREFIX_CACHE + guardada.getIdCamara(), 
                dtoJson,
                1, TimeUnit.HOURS // Expira después de 1 hora
            );
        } catch (JsonProcessingException e) {
            System.err.println("Error al serializar cámara para caché: " + e.getMessage());
        }
        
        return dto;
    }

    @Override
    public void eliminarCamara(Long id) {
        camaraRepository.deleteById(id);
        redisTemplate.delete(PREFIX_CACHE + id);
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
        return camaraRepository.findByTipo(tipo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CamaraDTO> obtenerPorEquipo(Long idEquipo) {
        // Obtener las cámaras
        List<Camara> camaras = camaraRepository.findByEquipoIdEquipo(idEquipo);
        
        // Convertir a DTOs dentro del contexto transaccional
        List<CamaraDTO> dtos = camaras.stream()
                .map(camara -> {
                    // Inicializar las relaciones necesarias
                    if (camara.getEquipo() != null) {
                        camara.getEquipo().getNombre();
                    }
                    return toDTO(camara);
                })
                .collect(Collectors.toList());
        
        // Guardar en caché los DTOs
        for (int i = 0; i < camaras.size(); i++) {
            Camara camara = camaras.get(i);
            CamaraDTO dto = dtos.get(i);
            
            try {
                String dtoJson = objectMapper.writeValueAsString(dto);
                // Guardar el JSON en Redis con tiempo de expiración
                redisTemplate.opsForValue().set(
                    PREFIX_CACHE + camara.getIdCamara(), 
                    dtoJson,
                    1, TimeUnit.HOURS // Expira después de 1 hora
                );
            } catch (Exception e) {
                System.err.println("Error al serializar DTO para caché: " + e.getMessage());
            }
        }
        
        return dtos;
    }
}
