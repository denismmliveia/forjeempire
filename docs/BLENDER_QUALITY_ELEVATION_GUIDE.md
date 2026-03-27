# Blender Quality Elevation Guide

## Problema tipico

Los primeros resultados en Blender suelen ser funcionales pero primitivos:

- cubos colocados
- cilindros visibles como primitives
- poca riqueza superficial
- uniones poco creibles
- materiales demasiado planos

## Objetivo

Subir de blockout funcional a objeto industrial rico y bien diseñado.

## Palancas de mejora reales

### 1. Libreria modular de piezas

Crear un kit reutilizable de:

- clamps
- collarines
- soportes
- conduits
- rejillas
- boquillas
- sensores
- paneles de mantenimiento

### 2. Hard-surface basico bien hecho

Usar mejor:

- bevel
- weighted normal
- booleans limpios
- solidify
- array
- curves para tuberias y cables

### 3. Materiales procedurales simples pero ricos

- roughness variado
- edge wear
- quemado termico localizado
- cobre oxidado
- cristal tintado
- glow tecnico muy contenido

### 4. Soportes y uniones creibles

Toda pieza importante debe parecer sostenida por otra.

Si algo parece pegado o flotante, la calidad percibida cae mucho.

### 5. Validacion por camaras

No diseñar solo desde la camara final.

Usar al menos:

- camara de silueta
- camara de detalle
- camara hero
- camara de validacion movil

### 6. Separar pases

- pase de silueta
- pase estructural
- pase de detalle mecanico
- pase de materiales
- pase de render hero

## Señales de que el diseño aun es flojo

- parece una primitive escalada
- el detalle no cambia la lectura
- la tecnologia no se ve en camara general
- la familia no tiene vocabulario formal claro

## Regla practica

Antes de añadir mas detalle, comprobar siempre:

- ¿la silueta ya funciona?
- ¿las piezas parecen fabricadas?
- ¿las uniones parecen creibles?
- ¿el material cuenta algo?

Si la respuesta es no, el problema no es “falta de detalle”, sino falta de diseño estructural.
