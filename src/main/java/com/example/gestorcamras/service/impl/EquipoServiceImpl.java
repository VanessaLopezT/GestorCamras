package com.example.gestorcamras.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gestorcamras.dto.EquipoDTO;
import com.example.gestorcamras.dto.EquipoConCamarasDTO;
import com.example.gestorcamras.dto.EquipoDetalleDTO;
import com.example.gestorcamras.dto.RegistroEquipoRequest;
import com.example.gestorcamras.dto.CamaraDTO;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.repository.CamaraRepository;
import com.example.gestorcamras.repository.EquipoRepository;
import com.example.gestorcamras.service.EquipoService;

@Service
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private CamaraRepository camaraRepository;

    @Override
    public List<EquipoDTO> obtenerTodos() {
        return equipoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EquipoDTO> obtenerPorId(Long id) {
        return equipoRepository.findById(id)
                .map(this::convertirADTO);
    }

    @Override
    @Transactional
    public EquipoDTO registrarEquipo(EquipoDTO equipoDTO) {
        Equipo equipo = new Equipo();
        equipo.setNombre(equipoDTO.getNombre());
        equipo.setIdentificador(equipoDTO.getIdentificador());
        equipo.setIp(equipoDTO.getIp());
        equipo.setPuerto(equipoDTO.getPuerto());
        equipo.setUltimaConexion(LocalDateTime.now());
        equipo.setActivo(true);

        Equipo guardado = equipoRepository.save(equipo);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public void actualizarPing(Long id) {
        equipoRepository.findById(id).ifPresent(equipo -> {
            equipo.setUltimaConexion(LocalDateTime.now());
            equipo.setActivo(true);
            equipoRepository.save(equipo);
        });
    }

    @Override
    @Transactional
    public void asignarCamara(Long idEquipo, Long idCamara) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        
        Camara camara = camaraRepository.findById(idCamara)
                .orElseThrow(() -> new RuntimeException("Cámara no encontrada"));

        equipo.getCamaras().add(camara);
        equipoRepository.save(equipo);
    }

    @Override
    @Transactional
    public EquipoDTO guardarEquipo(EquipoDTO equipoDTO) {
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(equipoDTO.getIdEquipo());
        equipo.setNombre(equipoDTO.getNombre());
        equipo.setIdentificador(equipoDTO.getIdentificador());
        equipo.setIp(equipoDTO.getIp());
        equipo.setPuerto(equipoDTO.getPuerto());
        equipo.setUltimaConexion(equipoDTO.getUltimaConexion());
        equipo.setActivo(equipoDTO.getActivo());

        Equipo guardado = equipoRepository.save(equipo);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public void eliminarEquipo(Long id) {
        equipoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EquipoDTO registrarEquipoConCamaras(RegistroEquipoRequest request) {
        Equipo equipo = new Equipo();
        equipo.setNombre(request.getNombreEquipo());
        equipo.setIp(request.getIp());
        equipo.setUltimaConexion(LocalDateTime.now());
        equipo.setActivo(true);

        Equipo guardado = equipoRepository.save(equipo);
        return convertirADTO(guardado);
    }

    @Override
    public List<EquipoDTO> buscarPorNombre(String nombreEquipo) {
        return equipoRepository.findByNombreContainingIgnoreCase(nombreEquipo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EquipoDetalleDTO> obtenerDetallePorId(Long idEquipo) {
        return equipoRepository.findById(idEquipo)
                .map(equipo -> {
            EquipoDetalleDTO dto = new EquipoDetalleDTO();
            dto.setIdEquipo(equipo.getIdEquipo());
                    dto.setNombreEquipo(equipo.getNombre());
                    dto.setIp(equipo.getIp());
                    dto.setFechaRegistro(equipo.getUltimaConexion());
                    dto.setCamaras(equipo.getCamaras().stream()
                            .map(camara -> {
                                CamaraDTO camaraDTO = new CamaraDTO();
                                camaraDTO.setIdCamara(camara.getIdCamara());
                                camaraDTO.setNombre(camara.getNombre());
                                camaraDTO.setTipo(camara.getTipo());
                                return camaraDTO;
                            })
                            .collect(Collectors.toList()));
            return dto;
        });
    }

    @Override
    @Transactional
    public EquipoDTO actualizarEquipoConCamaras(Long id, EquipoConCamarasDTO dto) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        equipo.setNombre(dto.getNombreEquipo());
        equipo.setIp(dto.getIp());

        // Actualizar cámaras
        equipo.getCamaras().clear();
        if (dto.getCamaras() != null) {
            dto.getCamaras().forEach(camaraDTO -> {
                Camara camara = camaraRepository.findById(camaraDTO.getIdCamara())
                        .orElseThrow(() -> new RuntimeException("Cámara no encontrada: " + camaraDTO.getIdCamara()));
                equipo.getCamaras().add(camara);
            });
        }

        Equipo guardado = equipoRepository.save(equipo);
        return convertirADTO(guardado);
    }

    @Override
    public Optional<Equipo> obtenerEntidadPorId(Long id) {
        return equipoRepository.findById(id);
    }

    @Override
    @Transactional
    public Equipo guardarEntidad(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    private EquipoDTO convertirADTO(Equipo equipo) {
        EquipoDTO dto = new EquipoDTO();
        dto.setIdEquipo(equipo.getIdEquipo());
        dto.setNombre(equipo.getNombre());
        dto.setIdentificador(equipo.getIdentificador());
        dto.setIp(equipo.getIp());
        dto.setPuerto(equipo.getPuerto());
        dto.setUltimaConexion(equipo.getUltimaConexion());
        dto.setActivo(equipo.getActivo());
        return dto;
    }
    }

