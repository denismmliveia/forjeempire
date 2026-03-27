# Agent Onboarding Guide

Este documento explica como debe interpretar otro agente el transfer pack.

## 1. Que es este paquete

Este paquete no es un proyecto listo para ejecutar.

Es una destilacion de conocimiento reusable para construir proyectos del tipo:

- incremental / cookie clicker
- coleccionables visuales por familias
- progresion por fases
- galeria persistente entre runs
- pipeline Blender MCP

## 2. Como debe leerse

El paquete esta dividido en dos capas:

- `fundamentos`
  - principios y patrones reutilizables
- `referencia`
  - como se implemento realmente en Tap Empire

No confundas una capa con la otra.

## 3. Orden de lectura obligatorio

### Si eres un agente generalista

1. `TRANSFER_PACK_README.md`
2. `INCREMENTAL_COLLECTIBLE_GAME_FOUNDATIONS.md`
3. `COLLECTIBLE_FAMILY_SYSTEM.md`
4. `GLOSSARY_AND_NAMING.md`

### Si vas a tocar pipeline visual o Blender

1. `TRANSFER_PACK_README.md`
2. `BLENDER_MCP_WORKFLOW.md`
3. `PHASED_COLLECTIBLE_DESIGN_DOCTRINE.md`
4. `BLENDER_QUALITY_ELEVATION_GUIDE.md`
5. `GLOSSARY_AND_NAMING.md`

### Si vas a tocar arquitectura o persistencia

1. `TRANSFER_PACK_README.md`
2. `COLLECTIBLE_FAMILY_SYSTEM.md`
3. `REFERENCE_IMPLEMENTATION_MAP.md`
4. `GLOSSARY_AND_NAMING.md`

## 4. Como interpretar los documentos

### Documentos fundacionales

Estos documentos describen patrones abstractos.

No deben leerse como instrucciones atadas a naves, astilleros o al lore de Tap Empire.

Su funcion es explicar:

- que sistema conviene construir
- por que funciona
- que errores evitar

### Documento de referencia

`REFERENCE_IMPLEMENTATION_MAP.md` no es teoria.

Su funcion es responder:

- donde vivia cada patron en Tap Empire
- que archivos tocar en una implementacion real

### Doctrina de diseño

Los documentos de diseño no describen una unica familia visual.

Describen como crear cualquier familia visual fuerte para un coleccionable progresivo.

## 5. Regla de traduccion al nuevo proyecto

Antes de implementar nada, sustituye mentalmente:

- `nave` por `coleccionable`
- `astillero` por `vista de coleccion / progreso`
- `victoria` por `culminacion de run`

Si el nuevo proyecto usa coches, peceras, maquinas o cualquier otro objeto, el patron sigue siendo el mismo.

## 6. Que no debes hacer

- no copiar nombres tematicos de Tap Empire sin cuestionarlos
- no asumir que todas las pantallas del nuevo juego deben parecerse a las de Tap Empire
- no tomar los ejemplos como reglas absolutas
- no mezclar el caso de estudio con la doctrina reusable

## 7. Que si debes hacer

- conservar la separacion entre familia activa y familia archivada
- conservar la idea de progresion visual por fases
- conservar la persistencia separada de la galeria historica
- adaptar el pipeline Blender MCP al nuevo coleccionable

## 8. Regla final

Si dudas entre seguir el tema visual de Tap Empire o seguir el patron reusable del paquete, prioriza siempre el patron reusable.
