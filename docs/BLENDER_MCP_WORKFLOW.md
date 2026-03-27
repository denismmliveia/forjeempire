# Blender MCP Workflow

## Objetivo

Definir un workflow portable para producir lineas de coleccionables visuales por fases con Blender controlado por MCP.

## Arquitectura base

- Blender local con addon MCP activo
- cliente/agent conectado a servidor MCP local
- scripts versionados para:
  - reconstruccion
  - progresion por fases
  - variante final/victory
  - render batch

## Checklist de arranque

1. abrir Blender
2. verificar addon MCP
3. comprobar puerto del addon
4. comprobar que el cliente ve el servidor conectado
5. hacer smoke test antes de modelar o renderizar

## Estructura de scripts recomendada

- `rebuild_<family>_variant.py`
- `rebuild_<family>_progression.py`
- `rebuild_<family>_victory.py`
- `render_<family>_sequence.py`

## Regla de reproducibilidad

No depender de edicion manual no documentada.

Todo asset final debe poder regenerarse desde scripts.

## Flujo de trabajo recomendado

1. blockout de familia
2. progresion por fases
3. variante final heroica
4. render batch
5. integracion runtime

## Validaciones minimas

- misma camara para toda la secuencia
- misma luz base para toda la secuencia
- naming estable
- preview de fase individual
- preview hero/final

## Estabilidad operativa

- si Blender interactivo cae, rehacer desde script
- preferir render batch reproducible para validacion final
- si Eevee headless es inestable, usar Cycles CPU

## Que documentar siempre

- scripts existentes
- naming de outputs
- reglas de actualizacion por familia
- ubicacion de assets integrados
