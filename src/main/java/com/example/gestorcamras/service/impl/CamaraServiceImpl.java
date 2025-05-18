package com.example.gestorcamras.service.impl;

import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.repository.CamaraRepository;
import com.example.gestorcamras.redis.IRedisCache;
import com.example.gestorcamras.service.CamaraService;
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

    private static final String PREFIX_CACHE = "camara_";

    @Override
    public List<Camara> obtenerTodas() {
        return camaraRepository.findAll();
    }

    @Override
    public Optional<Camara> obtenerPorId(Long id) {
        Optional<Camara> cacheada = redisCache.obtener(PREFIX_CACHE + id);
        if (cacheada.isPresent()) {
            return cacheada;
        }
        Optional<Camara> camaraBD = camaraRepository.findById(id);
        camaraBD.ifPresent(cam -> redisCache.guardar(PREFIX_CACHE + id, cam));
        return camaraBD;
    }

    @Override
    public Camara guardarCamara(Camara camara) {
        Camara guardada = camaraRepository.save(camara);
        redisCache.guardar(PREFIX_CACHE + guardada.getIdCamara(), guardada);
        return guardada;
    }

    @Override
    public void eliminarCamara(Long id) {
        camaraRepository.deleteById(id);
        redisCache.eliminar(PREFIX_CACHE + id);
    }

    @Override
    public List<Camara> obtenerPorPropietario(Long idUsuario) {
        return camaraRepository.findByPropietarioIdUsuario(idUsuario);
    }

    @Override
    public List<Camara> obtenerPorUbicacion(Long idUbicacion) {
        return camaraRepository.findByUbicacionId(idUbicacion);
    }

    @Override
    public List<Camara> obtenerPorActiva(boolean activa) {
        return camaraRepository.findByActiva(activa);
    }

    @Override
    public List<Camara> obtenerPorTipo(String tipo) {
        return camaraRepository.findByTipo(tipo);
    }
}
