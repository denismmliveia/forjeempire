# Collectible Family System

## Objetivo

Permitir que un juego incremental soporte varias familias visuales del mismo coleccionable sin romper:

- la run actual
- la galeria historica
- la persistencia
- la seleccion de assets

## Conceptos base

- `active family`: familia visual usada en la run actual
- `archived family`: familia con la que se completo una run archivada
- `run id`: identificador unico de una partida
- `gallery entry`: entrada historica de un coleccionable final completado

## Reglas del sistema

- la familia activa no debe cambiar durante la run
- la familia archivada se congela al completar la run
- la galeria no debe derivar la familia desde el estado actual
- la rotacion de familia debe ocurrir al iniciar la siguiente run, no al detectar victoria

## Persistencia recomendada

Separar:

- progreso actual del juego
- galeria historica

La galeria debe vivir fuera del estado principal de la run.

## Datos minimos recomendados

### Estado actual

- `currentRunId`
- `activeCollectibleFamily`

### Entrada de galeria

- `id`
- `runNumber`
- `collectibleFamily`
- `completedAtEpochMillis`
- metadatos de la run que merezcan conservarse

## Archivado idempotente

El archivado debe ser robusto ante recomposicion o relectura de estado.

Niveles recomendados:

1. `run id` unico
2. insercion `if absent`
3. cache de sesion opcional en ViewModel

## Seleccion de assets

Centralizarla en un registry.

Ejemplo de API reusable:

- `collectiblePhaseVisualRes(family, phaseIndex)`
- `collectibleFinalVisualRes(family)`

Nunca dispersar referencias directas a drawables o nombres de archivo por toda la UI.

## Rotacion de familias

La regla mas simple es una rotacion ciclica.

Debe ejecutarse:

- al crear una nueva run tras una run completada

No debe ejecutarse:

- al abrir la pantalla final
- al detectar victoria por primera vez

## Riesgos comunes

- mezclar familia activa con familia archivada
- romper saves antiguos al introducir familias
- dejar referencias hardcoded a assets de una unica familia
- duplicar entradas de galeria
