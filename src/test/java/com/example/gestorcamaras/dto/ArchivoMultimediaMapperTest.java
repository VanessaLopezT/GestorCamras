package com.example.gestorcamaras.dto;

import com.example.gestorcamaras.model.ArchivoMultimedia;
import com.example.gestorcamaras.model.Camara;
import com.example.gestorcamaras.model.Equipo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test puro (sin contexto Spring ni BD): valida el único punto de
 * conversión entidad → DTO del sistema.
 */
class ArchivoMultimediaMapperTest {

    private ArchivoMultimedia archivoCompleto() {
        Camara camara = new Camara();
        camara.setIdCamara(7L);

        Equipo equipo = new Equipo();
        equipo.setIdEquipo(3L);

        ArchivoMultimedia archivo = new ArchivoMultimedia();
        archivo.setIdArchivo(42L);
        archivo.setNombreArchivo("captura_001.png");
        archivo.setRutaArchivo("equipo3/camara7/captura_001.png");
        archivo.setTipo(ArchivoMultimedia.TipoArchivo.FOTO);
        archivo.setFechaCaptura(LocalDateTime.of(2026, 7, 3, 10, 30));
        archivo.setFechaSubida(LocalDateTime.of(2026, 7, 3, 10, 31));
        archivo.setCamara(camara);
        archivo.setEquipo(equipo);
        return archivo;
    }

    @Test
    void toDTO_mapeaTodosLosCampos() {
        ArchivoMultimediaDTO dto = ArchivoMultimediaMapper.toDTO(archivoCompleto());

        assertEquals(42L, dto.getIdArchivo());
        assertEquals("captura_001.png", dto.getNombreArchivo());
        assertEquals("equipo3/camara7/captura_001.png", dto.getRutaArchivo());
        assertEquals("FOTO", dto.getTipo());
        assertEquals(LocalDateTime.of(2026, 7, 3, 10, 30), dto.getFechaCaptura());
        assertEquals(LocalDateTime.of(2026, 7, 3, 10, 31), dto.getFechaSubida());
        assertEquals(7L, dto.getCamaraId());
        assertEquals(3L, dto.getEquipoId());
    }

    @Test
    void toDTO_toleraRelacionesYTipoNulos() {
        ArchivoMultimedia archivo = archivoCompleto();
        archivo.setCamara(null);
        archivo.setEquipo(null);
        archivo.setTipo(null);

        ArchivoMultimediaDTO dto = ArchivoMultimediaMapper.toDTO(archivo);

        assertNull(dto.getCamaraId());
        assertNull(dto.getEquipoId());
        assertNull(dto.getTipo());
        assertEquals(42L, dto.getIdArchivo());
    }

    @Test
    void toDTO_deNullEsNull() {
        assertNull(ArchivoMultimediaMapper.toDTO(null));
    }

    @Test
    void toDTOList_mapeaListaYToleraNull() {
        List<ArchivoMultimediaDTO> dtos = ArchivoMultimediaMapper.toDTOList(List.of(archivoCompleto()));
        assertEquals(1, dtos.size());
        assertEquals(42L, dtos.get(0).getIdArchivo());

        assertTrue(ArchivoMultimediaMapper.toDTOList(null).isEmpty());
    }
}
