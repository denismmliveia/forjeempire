# Agent Roles

Este documento define los roles de subagentes que Claude Code puede invocar durante el desarrollo de Holo Forge.

Cada rol tiene un objetivo acotado, herramientas permitidas y criterios de exito claros.

---

## Roles disponibles

### 1. Explore Agent

**Objetivo**: Mapear el estado actual del codebase antes de cualquier modificacion.

**Cuando usarlo**:
- Al arrancar una sesion nueva
- Antes de tocar un area del codigo que no se ha leido en esta sesion
- Para responder preguntas sobre arquitectura o estructura de archivos

**Herramientas permitidas**: Read, Glob, Grep

**Restricciones**:
- No edita archivos
- No ejecuta comandos
- No hace inferencias sobre codigo que no ha leido

**Criterio de exito**: Devuelve una lista de archivos relevantes con una descripcion de su proposito y las lineas clave que el agente principal necesita.

---

### 2. Plan Agent

**Objetivo**: Diseñar la estrategia de implementacion antes de escribir codigo.

**Cuando usarlo**:
- Cuando la tarea afecta mas de 3 archivos
- Cuando hay dependencias entre cambios que deben hacerse en orden
- Cuando el usuario pide un plan explicitamente

**Herramientas permitidas**: Read, Glob, Grep, WebFetch, WebSearch

**Restricciones**:
- No escribe codigo de produccion
- No modifica archivos del proyecto
- Devuelve un plan estructurado: orden de pasos, archivos criticos, riesgos

**Criterio de exito**: Plan que el agente principal puede ejecutar paso a paso sin ambiguedades.

---

### 3. 3D Design Agent

**Objetivo**: Generar assets visuales 3D de calidad para el proyecto usando Blender headless.

**Cuando usarlo**:
- Para crear nuevas familias de coleccionables (armas, artefactos, estructuras)
- Para generar spritesheets de efectos animados (explosiones, bursts, overlays)
- Para renderizar progresiones por fases de un coleccionable

**Herramientas disponibles**: Bash (blender headless), Read, Write, Glob

---

#### Configuracion tecnica obligatoria

**Ejecutar Blender:**
```
blender.exe --background --python <script.py>
```

**Motor de render:**
```python
scene.render.engine = 'BLENDER_EEVEE'
```

**Color management (obligatorio para colores neon correctos):**
```python
scene.view_settings.view_transform = 'Standard'
scene.view_settings.look = 'None'
```

**Keyframes en Blender 5.1 (API rota en versiones anteriores):**
```python
for action in bpy.data.actions:
    for layer in action.layers:
        for strip in layer.strips:
            for cbag in strip.channelbags:
                for fc in cbag.fcurves:
                    for kp in fc.keyframe_points:
                        kp.interpolation = 'LINEAR'
```

**Transparencia para overlays:**
```python
scene.render.film_transparent = True
scene.render.image_settings.color_mode = 'RGBA'
```

**Fondos opacos (backgrounds, no overlays):**
```python
scene.world = bpy.data.worlds.new("W")
scene.world.use_nodes = True
bg = scene.world.node_tree.nodes['Background']
bg.inputs['Color'].default_value = (0.0, 0.0, 0.02, 1.0)
```

---

#### Paleta de colores del proyecto

Usar siempre valores lineales (Blender trabaja en linear, no en sRGB):

| Color | Uso | Valor Blender (linear) |
|-------|-----|------------------------|
| NeonCyan | Primary, emitters activos | (0.0, 0.94, 1.0, 1.0) |
| ElectricBlue | Titulos, secondary | (0.12, 0.56, 1.0, 1.0) |
| VoidBlack | Background base | (0.0, 0.0, 0.02, 1.0) |
| DeepNavy | Background secundario | (0.0, 0.01, 0.04, 1.0) |
| NeonGreen | Completado, exito | (0.05, 1.0, 0.08, 1.0) |

**Regla critica**: No usar colores calidos (rojo, naranja, purple dominante, magenta) en fondos o efectos principales. Los tonos warm solo en acentos muy pequenos o bordes de contraste.

---

#### Checklist de diseño para coleccionables

Antes de generar cualquier asset de coleccionable, verificar:

**Silueta:**
- [ ] La silueta es legible a 64x64px
- [ ] La silueta es distinta de todas las familias anteriores
- [ ] Tiene un eje estructural claro (vertical, horizontal, diagonal)

**Progresion por fases (phase_1 a phase_6 + victory):**
- [ ] phase_1: masa base reconocible
- [ ] phase_2: identidad temprana de la familia
- [ ] phase_3: funcion principal visible (canon, lente, estructura)
- [ ] phase_4: blindaje o estructura secundaria
- [ ] phase_5: sofisticacion / detalles mecanicos
- [ ] phase_6: cierre estructural total
- [ ] victory: culminacion heroica, 30-40% mas compleja que phase_6
- [ ] Cada fase cambia masa, funcion O altura percibida (no solo color)
- [ ] El salto entre fases es legible en miniatura

**Materiales:**
- [ ] Roughness variado entre piezas (no todo igual)
- [ ] Las piezas principales tienen un material distinto al cuerpo base
- [ ] Edge wear o quemado termico en zonas de tension
- [ ] Glow neon muy contenido (solo en emisores o cristales, no en todo)

**Estructura:**
- [ ] Toda pieza importante parece sostenida por otra (no floating)
- [ ] Uniones entre piezas con collar, clamp o soporte
- [ ] Bevel en aristas principales (no aristas perfectamente afiladas)

---

#### Checklist de diseño para efectos animados (spritesheets)

Para bursts, explosiones, overlays, backgrounds animados:

- [ ] Numero de frames: 16 (burst rapido), 24 (holo grid), 32 (background loop)
- [ ] Loop seamless verificado: frame 1 y frame N+1 deben coincidir en W si se usa Noise4D
- [ ] Camera ortho con ortho_scale ajustado al contenido (no dejar espacio vacio)
- [ ] Transparencia correcta si es overlay (film_transparent = True)
- [ ] Opaco correcto si es background (color world seteado)
- [ ] Emision Strength <= 2.0 con Standard color mgmt (no sobreexponer)
- [ ] Animacion via keyframes W de ShaderNodeTexNoise/Voronoi para loops

---

#### Reglas de reproducibilidad

Todo asset debe poder regenerarse desde script sin intervencion manual:

- Scripts en `mcp-3d-pipeline/scripts/`
- Naming: `create_<asset_name>.py` para scenes, `render_<asset_name>.py` para render
- Output en `mcp-3d-pipeline/output/`
- Assets finales en `app/src/main/assets/textures/`
- JSON de metadata junto al PNG: `{cols, rows, frameCount, frameWidth, frameHeight}`

No documentar steps manuales. Si no se puede scripting, no se puede reproducir.

---

#### Ejemplos de prompts bien formados para el 3D Design Agent

**Correcto:**
```
Crea un spritesheet de 16 frames (4x4 grid, 256x256px por frame) de una explosion
de energia neon cyan que se expande desde el centro y desaparece. Fondo transparente.
Paleta: blanco al inicio, NeonCyan en expansion, desvanece a ElectricBlue.
Output: mcp-3d-pipeline/output/nova_burst_sheet.png + .json
```

**Incorrecto (demasiado vago):**
```
Haz una explosion bonita para la pantalla de fases.
```

**Correcto:**
```
Crea la progresion de fases 1 a 6 + victory de un artefacto tipo "plasma cannon"
para Holo Forge. Silueta vertical. Materiales: metal oxidado base, cristal cyan
en camara central. Cada fase añade una seccion del canon. Victory: añade 3 emitters
laterales con glow. Camera ortho frontal. Output: weapon_plasma_cannon_phase_N.png
```

**Incorrecto (mezcla lore con instrucciones tecnicas):**
```
Haz que se vea como una pistola espacial muy sci-fi con efectos cool.
```

---

#### Validaciones minimas antes de entregar un asset

1. Renderizar preview de fase individual y verificar silueta
2. Renderizar todas las fases en fila y verificar que la progresion es obvia
3. Verificar que los colores son frios (cyan/blue/teal), no calidos
4. Verificar que el asset se ve bien a 200x200dp (resolucion de display en app)
5. Verificar que el JSON de metadata es correcto (cols * rows >= frameCount)

---

### 4. Build & Deploy Agent

**Objetivo**: Compilar el APK y desplegarlo en el dispositivo conectado.

**Cuando usarlo**:
- Despues de completar un bloque de cambios en el codigo Android
- Cuando el usuario pide probar los cambios en el dispositivo

**Herramientas permitidas**: Bash

**Comandos estandar:**
```bash
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.forgelegends/.MainActivity
```

**Restricciones**:
- No modifica archivos de produccion
- Si el build falla, reporta el error completo sin intentar arreglarlo solo
- Usar `assembleDebug`, nunca `assembleRelease` sin instruccion explicita

**Criterio de exito**: `BUILD SUCCESSFUL` + `Success` en adb install.

---

### 5. Commit Agent

**Objetivo**: Crear commits atomicos con mensajes descriptivos.

**Cuando usarlo**:
- Cuando el usuario pide explicitamente un commit
- Nunca de forma proactiva

**Herramientas permitidas**: Bash (solo git commands)

**Restricciones**:
- Nunca `git push` sin instruccion explicita
- Nunca `--no-verify`
- Nunca `git add .` — stagear archivos especificos
- Mensajes en ingles, imperativo presente: "Add X", "Fix Y", "Refactor Z"

**Criterio de exito**: Commit creado sin errores, `git status` limpio.

---

## Reglas generales para todos los agentes

- Un agente no puede asumir que el estado actual del repo coincide con lo que otro agente vio en una sesion anterior.
- Antes de modificar un archivo, leerlo. Siempre.
- Si una validacion falla, reportar el error exacto. No reintentar el mismo comando.
- Los cambios visuales (assets, efectos, colores) siempre deben pasar por la paleta fria del proyecto.
- Los nombres de archivos y variables siguen el vocabulario holografico del proyecto (ver GLOSSARY_AND_NAMING.md).
