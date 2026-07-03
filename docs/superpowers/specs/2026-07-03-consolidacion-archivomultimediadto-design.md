# Consolidación de ArchivoMultimediaDTO — Diseño

**Fecha:** 2026-07-03
**Estado:** Implementado y verificado (2026-07-03: `mvnw clean test` BUILD SUCCESS, 5/5 tests)

## Problema

`ArchivoMultimediaDTO` existe por triplicado y ya divergió:

| Versión | Ubicación | Fechas | Nombres de campo | Usos |
|---|---|---|---|---|
| V1 | `dto/` | `LocalDateTime` | `idArchivo, nombreArchivo, ...` | 1 (controller) |
| V2 | `Escritorio/model/` | `String` | iguales a V1 | 4 (cliente y servidor) |
| V3 | `Escritorio/dto/` | `LocalDateTime` | `id, nombre, ruta, idCamara, idEquipo` | 1 (VisualizadorMultimediaUI) |

Además existen tres conversiones paralelas: `convertirADTO()` en `ArchivoMultimediaController`, `convertToDTO()` en `ArchivoMultimediaService`, y un mapeo manual campo a campo con org.json en `VisualizadorMultimediaUI`. **Un cambio de campo hoy se escribe en 6 sitios.** Agravante: `service/ArchivoMultimediaService` (servidor) importa el DTO del paquete del cliente Swing (`Escritorio.model`) — dependencia invertida.

## Decisiones

1. **DTO único compartido** entre servidor y cliente Swing (viven en el mismo artefacto y classpath).
2. **Alcance completo**: se eliminan también los convertidores duplicados y el mapeo org.json.
3. **Mapper manual único** (sin MapStruct ni records): explícito, sin dependencias nuevas.

## Estado final

```
dto/
├── ArchivoMultimediaDTO.java    ← única definición
└── ArchivoMultimediaMapper.java ← única conversión entity→DTO
```

**DTO** (Lombok `@Data`; claves JSON idénticas al contrato actual → wire-compatible):

```java
private Long idArchivo;
private String nombreArchivo;
private String rutaArchivo;
private String tipo;              // enum TipoArchivo → String
private LocalDateTime fechaCaptura;
private LocalDateTime fechaSubida;
private Long camaraId;
private Long equipoId;
```

**Mapper** (métodos estáticos):

- `toDTO(ArchivoMultimedia)`: mapea campos directos, `tipo` enum→`name()`, `camara.getIdCamara()`→`camaraId`, `equipo.getIdEquipo()`→`equipoId`, con null-safety en relaciones.
- `toDTOList(List<ArchivoMultimedia>)`.

## Cambios por archivo

| Archivo | Cambio |
|---|---|
| `dto/ArchivoMultimediaDTO.java` | Queda como única versión (ya tiene la forma correcta) |
| `dto/ArchivoMultimediaMapper.java` | **Nuevo** |
| `controller/ArchivoMultimediaController.java` | Borra `convertirADTO()`, usa mapper |
| `service/IArchivoMultimediaService.java` | Import `dto.` (elimina dependencia hacia Escritorio) |
| `service/ArchivoMultimediaService.java` | Borra `convertToDTO()`, usa mapper, import `dto.` |
| `Escritorio/service/ClienteArchivoMultimediaService.java` | Import `dto.` (su ObjectMapper ya registra JavaTimeModule) |
| `Escritorio/AplicarFiltros.java` | Import `dto.`; línea ~357: fecha `String`→`LocalDateTime` (adaptar formateo) |
| `Escritorio/VisualizadorMultimediaUI.java` | Bucle org.json → `objectMapper.readValue(...)`; getters `getId→getIdArchivo`, `getNombre→getNombreArchivo`, `getRuta→getRutaArchivo`; fechas ya eran `LocalDateTime` |
| `Escritorio/model/ArchivoMultimediaDTO.java` | **Eliminar** |
| `Escritorio/dto/ArchivoMultimediaDTO.java` | **Eliminar** |

## Riesgos

- `VisualizadorMultimediaUI` (Swing, ~800+ líneas): si inserta el DTO directo en componentes que rendericen vía `toString()` (V3 lo sobreescribía), el `toString()` de Lombok mostraría todos los campos. Mitigación: renderizado explícito en la UI.
- Formato de fecha: el servidor serializa ISO-8601 (jsr310 + `WRITE_DATES_AS_TIMESTAMPS` off en el cliente). El mapper usa el `LocalDateTime` de la entidad directamente; se elimina el `toString()` manual de fechas del service.
- Sin cambio de contrato JSON: mismas claves, mismos formatos. **Verificado:** `templates/archivos.html` consume `archivo.idArchivo` / `archivo.nombreArchivo` vía fetch — tercer consumidor del contrato; con las claves intactas no requiere cambios.

## Verificación

1. Test unitario de `ArchivoMultimediaMapper` (entidad completa, relaciones null, lista).
2. Chequeo sintáctico completo en sandbox (java-parser).
3. En máquina del usuario: `mvnw.cmd clean compile`, arrancar servidor + cliente, abrir Visualizador Multimedia y Aplicar Filtros con datos reales.
4. `grep` final: cero referencias a los paquetes eliminados.

## Criterio de éxito

Añadir un campo nuevo al archivo multimedia debe requerir tocar exactamente 2 archivos: la entidad y el par DTO+mapper.
