package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Ubicacion;
import com.example.gestorcamras.repository.UbicacionRepository;
import com.example.gestorcamras.service.UbicacionService;
import com.example.gestorcamras.dto.UbicacionDTO;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionServiceImpl implements UbicacionService {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    // Conversión de entidad a DTO
    private UbicacionDTO toDTO(Ubicacion u) {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setId(u.getId());
        dto.setLatitud(u.getLatitud());
        dto.setLongitud(u.getLongitud());
        dto.setDireccion(u.getDireccion());
        if (u.getCamaras() != null) {
            dto.setCamaraIds(u.getCamaras().stream().map(c -> c.getIdCamara()).collect(Collectors.toList()));
        }
        return dto;
    }

    // Conversión de DTO a entidad
    private Ubicacion toEntity(UbicacionDTO dto) {
        Ubicacion u = new Ubicacion();
        u.setId(dto.getId());
        u.setLatitud(dto.getLatitud());
        u.setLongitud(dto.getLongitud());
        u.setDireccion(dto.getDireccion());
        // No asignamos cámaras aquí
        return u;
    }

    @Override
    @Cacheable(value = "ubicaciones")
    public List<UbicacionDTO> obtenerTodas() {
        return ubicacionRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "ubicacion", key = "#id")
    public Optional<UbicacionDTO> obtenerPorId(Long id) {
        return ubicacionRepository.findById(id).map(this::toDTO);
    }

    @Override
    @CachePut(value = "ubicacion", key = "#ubicacionDTO.id")
    public UbicacionDTO guardarUbicacion(UbicacionDTO ubicacionDTO) {
        Ubicacion u = toEntity(ubicacionDTO);
        Ubicacion guardado = ubicacionRepository.save(u);
        return toDTO(guardado);
    }

    @Override
    @CacheEvict(value = "ubicacion", key = "#id")
    public void eliminarUbicacion(Long id) {
        ubicacionRepository.deleteById(id);
    }
}
