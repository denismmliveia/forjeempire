# Incremental Collectible Game Foundations

## Patron base

Este patron sirve para juegos incrementales donde cada run gira alrededor de construir, desbloquear o completar un objeto coleccionable.

Bucle base:

1. accion principal repetible
2. obtencion de recurso principal
3. compra de mejoras
4. progreso visible del coleccionable
5. culminacion de la run
6. archivado del coleccionable final
7. nueva run con valor heredado

## Principios clave

- la accion principal debe ser inmediata y satisfactoria
- las mejoras deben ser faciles de comparar
- el coleccionable debe ser la meta visible de la run
- el reset debe sentirse como avance, no como castigo
- la galeria historica debe reforzar la memoria del progreso

## Estructura minima recomendada

- vista principal de accion
- vista de optimizacion/mejoras
- vista de coleccionable/progreso
- vista de culminacion/final
- sistema de persistencia

## Reglas UX reutilizables

- una sola accion principal por pantalla dominante
- textos cortos y jerarquizados
- progresion visible sin leer demasiado
- mobile-first y usable con una mano
- las pantallas secundarias no deben competir con la principal

## Coleccionable como meta

El objeto final de la run debe:

- cambiar de forma visible durante la partida
- tener fases claras
- culminar en una version final memorable
- poder archivarse historicamente

## Reinicio con valor

El reset correcto incluye:

- progreso local reiniciado
- valor permanente conservado
- coleccionable final archivado
- posibilidad de arrancar una nueva familia o variante

## Errores comunes

- reset sin recompensa clara
- coleccionable solo cosmetico y no estructural
- demasiada homogeneidad visual entre pantallas
- progreso por fases demasiado sutil
- galeria historica mezclada con progreso actual
