# Reference Implementation Map

Este documento no es teoria. Resume donde vive en Tap Empire cada patron tecnico reusable.

## Estado actual de la run

- `GameState`
  - `app/src/main/java/com/tapempire/domain/model/GameState.kt`

## Familias de coleccionables

- enum de familias:
  - `app/src/main/java/com/tapempire/domain/model/ShipModelFamily.kt`

## Persistencia del estado actual

- repositorio principal:
  - `app/src/main/java/com/tapempire/data/repository/DataStoreGameRepository.kt`

## Galeria historica persistente

- modelo:
  - `app/src/main/java/com/tapempire/domain/model/ShipShowcaseEntry.kt`
- contrato:
  - `app/src/main/java/com/tapempire/data/repository/ShipShowcaseRepository.kt`
- persistencia:
  - `app/src/main/java/com/tapempire/data/repository/DataStoreShipShowcaseRepository.kt`

## Orquestacion de estado y archivado

- `app/src/main/java/com/tapempire/presentation/GameViewModel.kt`

## Registry de assets por familia

- `app/src/main/java/com/tapempire/ui/components/ShipVisualRegistry.kt`

## Pantallas que consumen la familia activa

- `app/src/main/java/com/tapempire/ui/screen/ForgeScreen.kt`
- `app/src/main/java/com/tapempire/ui/screen/ShipyardScreen.kt`
- `app/src/main/java/com/tapempire/ui/screen/VictoryScreen.kt`
- `app/src/main/java/com/tapempire/ui/components/ShipyardViewportCard.kt`

## Galeria que consume la familia archivada

- `app/src/main/java/com/tapempire/ui/components/ShipShowcaseGallery.kt`

## Pipeline Blender de referencia

- familia original:
  - `tools/rebuild_ship_blender_*.py`
- familia zeppelin:
  - `tools/rebuild_ship_blender_balloon_*.py`
- familia cathedral:
  - `tools/rebuild_ship_blender_forgecathedral_*.py`

## Tests de referencia

- rotacion de familias:
  - `app/src/test/java/com/tapempire/domain/model/ShipModelFamilyTest.kt`
- registry de assets:
  - `app/src/test/java/com/tapempire/ui/components/ShipVisualRegistryTest.kt`
