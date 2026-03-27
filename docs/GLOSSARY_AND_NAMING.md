# Glossary and Naming

## 1. Glosario minimo

### Collectible

Objeto principal que el jugador construye, completa, desbloquea o archiva entre runs.

Ejemplos:

- nave
- coche
- pecera
- locomotora
- artefacto

### Family

Linea visual distinta del mismo tipo de coleccionable.

Una familia no es una variacion menor: debe tener una silueta y lenguaje formal propios.

### Active Family

Familia visual usada en la run actual.

### Archived Family

Familia con la que se completo una run archivada en la galeria.

### Gallery Entry

Registro historico de un coleccionable final completado.

### Run

Partida actual entre reinicios valiosos.

### Run ID

Identificador unico de una run. Sirve para evitar duplicados al archivar.

### Phased Progression

Secuencia visual de construccion o evolucion del coleccionable, normalmente de `phase_1` a `phase_N`.

### Final / Victory Variant

Version culminante del coleccionable ya completado.

## 2. Convenciones de naming reutilizables

### Nombres de familias

Usar enums o constantes estables y cortas.

Ejemplo:

- `HEAVY_FREIGHTER`
- `ZEPPELIN`
- `FORGE_CATHEDRAL`

En otro proyecto:

- `SPORT_COUPE`
- `ARMORED_TRUCK`
- `RETRO_BUGGY`

### Asset registry

Centralizar siempre la resolucion visual.

Funciones recomendadas:

- `collectiblePhaseVisualRes(family, phaseIndex)`
- `collectibleFinalVisualRes(family)`

No dispersar referencias directas a assets por toda la UI.

### Naming de assets

Formato recomendado:

- `<collectible>_<family>_phase_1`
- `<collectible>_<family>_phase_2`
- ...
- `<collectible>_<family>_victory_final`

Ejemplo:

- `ship_zeppelin_phase_1`
- `ship_cathedral_victory_final`

### Scripts Blender

Formato recomendado:

- `rebuild_<collectible>_<family>_variant.py`
- `rebuild_<collectible>_<family>_progression.py`
- `rebuild_<collectible>_<family>_victory.py`
- `render_<collectible>_<family>_sequence.py`

### Persistencia

Nombres conceptuales recomendados:

- `activeCollectibleFamily`
- `currentRunId`
- `galleryEntry`
- `archivedFamily`

## 3. Convenciones de separacion conceptual

### Lo activo

Todo lo que depende de la run actual debe colgar de:

- estado actual
- familia activa
- progreso local

### Lo historico

Todo lo que pertenece a la galeria debe colgar de:

- entradas archivadas
- familia archivada
- run historica

Nunca deducir la galeria desde el estado activo.

## 4. Regla editorial

Si un termino depende demasiado del lore de un proyecto, renombrarlo a un termino estructural y reusable.

Ejemplo:

- `shipyard` -> `collectible progression view`
- `victory screen` -> `run completion view`
- `ship family` -> `collectible family`
