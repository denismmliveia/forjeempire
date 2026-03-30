# Agent.md — Holo Forge

Este archivo contiene la informacion critica que el agente principal necesita para operar sobre este proyecto.

---

## 1. Que es este proyecto

Holo Forge es un juego incremental/clicker para Android con estetica sci-fi holografica.

El jugador acumula **Photons** (recurso) tapeando para **materializar hologramas** capa a capa. Cada holograma progresa a traves de 6 **Layers** (fases) hasta completarse. Los hologramas completados se archivan en la **Holo Gallery**. Cada run reinicia el progreso local pero conserva un bonus permanente.

**Genero**: incremental / idle / clicker con coleccionables visuales 3D.

---

## 2. Stack tecnico

| Componente | Tecnologia |
|-----------|------------|
| Plataforma | Android (minSdk 26, targetSdk 36) |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM (ViewModel + StateFlow) |
| DI | Hilt |
| Persistencia | DataStore Preferences |
| Navegacion | Jetpack Navigation Compose |
| Audio | SoundPool (low-latency game audio) |
| Animaciones | Lottie Compose + spritesheets custom |
| 3D Assets | Blender 5.1 headless via MCP pipeline |
| Build | Gradle 9.3+ / Kotlin / Java 17 |

**Package raiz**: `com.forgelegends`

**Application ID**: `com.forgelegends`

---

## 3. Estructura del proyecto

```
app/src/main/java/com/forgelegends/
├── MainActivity.kt              # NavHost, wiring de pantallas
├── ForgeLegendApp.kt            # Hilt Application
├── audio/
│   └── SoundManager.kt          # SoundPool: tap, purchase, phase, intro, etc.
├── data/repository/
│   ├── GameRepository.kt              # Contrato estado actual
│   ├── DataStoreGameRepository.kt     # Impl DataStore
│   ├── WeaponShowcaseRepository.kt    # Contrato galeria
│   ├── DataStoreWeaponShowcaseRepository.kt
│   ├── PlayerProgressRepository.kt    # Contrato seed/queue
│   └── DataStorePlayerProgressRepository.kt
├── di/                           # Modulos Hilt
├── domain/
│   ├── model/
│   │   ├── GameState.kt          # Estado de la run actual
│   │   ├── Concept.kt            # Modelo de concepto/blueprint
│   │   ├── Upgrade.kt            # Modelo de mejora
│   │   └── WeaponShowcaseEntry.kt # Entrada de galeria
│   └── registry/
│       └── ConceptRegistry.kt    # Descubre conceptos desde assets/
├── presentation/
│   └── GameViewModel.kt          # Logica de negocio, orquestacion
└── ui/
    ├── theme/
    │   ├── Color.kt              # Paleta holografica
    │   ├── Type.kt               # Orbitron + Exo2
    │   └── Theme.kt              # DarkColorScheme
    ├── navigation/
    │   └── NavRoutes.kt          # Rutas: SPLASH, FORGE, HOLO_LAB, etc.
    ├── components/
    │   ├── PhaseImageProvider.kt # Resuelve imagenes de fase desde assets
    │   └── scifi/                # Componentes reutilizables
    │       ├── AnimatedSpriteSheet.kt
    │       ├── GlowText.kt
    │       ├── LottieEffects.kt
    │       ├── PhotonParticleSystem.kt
    │       ├── SciFiBackground.kt
    │       ├── SciFiButton.kt
    │       ├── SciFiCard.kt
    │       ├── SciFiPhaseIndicator.kt
    │       └── SciFiProgressBar.kt
    └── screen/
        ├── SplashScreen.kt       # Logo + energy burst transition
        ├── ForgeScreen.kt        # Pantalla principal (tap)
        ├── HoloLabScreen.kt      # Upgrades
        ├── LayerProgressScreen.kt # Progresion de layers
        ├── CompletionScreen.kt   # Victoria / holograma materializado
        ├── BlueprintSelectScreen.kt # Seleccion de siguiente concepto
        ├── HoloGalleryScreen.kt  # Galeria de hologramas completados
        └── HoloViewerScreen.kt   # Visor 3D de modelo completado

mcp-3d-pipeline/
├── server.py                     # Servidor MCP para Blender
├── scripts/
│   ├── render_animation.py       # Render batch de frames
│   ├── make_spritesheet.py       # Empaqueta frames en atlas PNG + JSON
│   ├── create_plasma_bg.py       # Fondo animado plasma
│   ├── create_energy_burst.py    # Efecto explosion de energia
│   └── create_holo_grid.py       # Grid holografico animado
└── output/                       # Blends y frames temporales

app/src/main/assets/
├── textures/                     # Spritesheets de efectos
│   ├── plasma_bg_sheet.png + .json
│   ├── energy_burst_sheet.png + .json
│   └── holo_grid_sheet.png + .json
├── concepts/<id>/                # Assets por concepto
│   ├── manifest.json             # {id, name, emoji, description, secret?, secretWord?}
│   ├── phase_1.png ... phase_6.png
│   └── angle_0.png ... angle_N.png
└── holo_forge_logo.png           # Logo de splash (opcional, hay fallback)
```

---

## 4. Flujo de navegacion

```
SPLASH → BLUEPRINT_SELECT (si no hay concepto activo)
       → FORGE (si hay concepto activo)

FORGE → HOLO_LAB (upgrades)
      → LAYER_PROGRESS (ver fases)
      → HOLO_GALLERY (ver coleccion)
      → COMPLETION (auto, cuando phase > maxPhase)

COMPLETION → BLUEPRINT_SELECT (archiva run, selecciona nuevo concepto)
           → HOLO_GALLERY

HOLO_GALLERY → HOLO_VIEWER/{entryId}
```

---

## 5. Vocabulario del proyecto

Este es el vocabulario holografico vigente. No usar el vocabulario anterior (medieval/forja).

| Termino | Significado | Donde se usa |
|---------|-------------|-------------|
| Photons | Recurso/moneda del juego | ForgeScreen, HoloLabScreen |
| Layer | Fase de construccion (1-6) | LayerProgressScreen, ForgeScreen |
| Blueprint | Concepto/modelo a materializar | BlueprintSelectScreen |
| Holo Lab | Pantalla de upgrades | HoloLabScreen |
| Holo Gallery | Galeria de hologramas completados | HoloGalleryScreen |
| Holo Viewer | Visor 3D de modelo | HoloViewerScreen |
| Projection #N | Numero de run | HoloGalleryScreen |
| Access Code | Codigo secreto para conceptos ocultos | BlueprintSelectScreen |
| Emitter Power | Upgrade: tap strength tier 1 | GameState.kt |
| Lens Density | Upgrade: tap strength tier 2 | GameState.kt |
| Photon Purity | Upgrade: tap strength tier 3 | GameState.kt |
| Holo Precision | Upgrade: tap strength tier 4 | GameState.kt |
| Auto-Emitter | Upgrade: passive income tier 1 | GameState.kt |
| Holo Drone | Upgrade: passive income tier 2 | GameState.kt |
| Quantum Loop | Upgrade: passive income tier 3 | GameState.kt |

---

## 6. Sistema de conceptos

Los conceptos (modelos 3D) se descubren automaticamente desde `assets/concepts/<id>/manifest.json`.

Cada concepto requiere:
- `manifest.json` con `{id, name, emoji, description}`
- `phase_1.png` a `phase_N.png` (renders de cada layer)
- Opcionalmente: `angle_0.png` a `angle_N.png` (vistas para rotacion)

Los conceptos secretos requieren ademas `secret: true` y `secretWord: "palabra"` en el manifest.

El orden de presentacion al jugador es determinista por seed (shuffled queue per-player).

---

## 7. Paleta de colores

| Color | Hex | Uso |
|-------|-----|-----|
| NeonCyan | #00F0FF | Primary, acciones activas, glow |
| ElectricBlue | #1E90FF | Secondary, titulos |
| HoloPurple | #8B5CF6 | Tertiary, acentos en bordes |
| NeonGreen | #39FF14 | Success, layers completados |
| NeonMagenta | #FF00E5 | Error, alertas |
| VoidBlack | #0A0A14 | Background raiz |
| DeepSpace | #0D1117 | Surface |
| NebulaSurface | #151B2B | Cards/paneles |
| CoolWhite | #E8ECF1 | Texto primario |
| GhostText | #6B7B8D | Texto muted |

**Regla critica**: fondos y efectos principales siempre en tonos frios (cyan, blue, teal). Nunca warm/reddish en areas extensas.

---

## 8. Pipeline 3D (Blender)

**Ejecutar Blender headless:**
```bash
blender.exe --background --python script.py
```

**Configuracion obligatoria en scripts:**
- Motor: `BLENDER_EEVEE` (no `BLENDER_EEVEE_NEXT`)
- Color management: `Standard` (no `Filmic` — desatura neon)
- API keyframes Blender 5.1: `action.layers[].strips[].channelbags[].fcurves`

**Output de spritesheets:**
- PNG grid + JSON metadata `{cols, rows, frameCount, frameWidth, frameHeight}`
- Ubicacion final: `app/src/main/assets/textures/`

---

## 9. Build y deploy

```bash
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.forgelegends/.MainActivity
```

---

## 10. Comportamiento del agente principal

### Antes de cualquier tarea

1. Leer los archivos que vas a modificar. Nunca editar a ciegas.
2. Si la tarea toca mas de 3 archivos, hacer un plan antes de escribir codigo.
3. Si hay un plan activo (.claude/plans/), verificar si sigue vigente.

### Al escribir codigo

- Respetar el vocabulario holografico vigente (seccion 5).
- Respetar la paleta de colores fria (seccion 7).
- Usar los componentes sci-fi existentes (`ui/components/scifi/`) en vez de reinventarlos.
- No añadir dependencias sin consultar al usuario.
- No refactorizar codigo que no esta relacionado con la tarea actual.

### Al generar assets 3D

- Seguir las instrucciones del 3D Design Agent en `docs/agent_roles.md`.
- Scripts reproducibles en `mcp-3d-pipeline/scripts/`.
- Verificar colores frios en el output.

### Al hacer commits

- Solo cuando el usuario lo pide explicitamente.
- Stagear archivos especificos, no `git add .`.
- Mensajes en ingles, imperativo presente.
- Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>

### Al desplegar

- Solo `assembleDebug` salvo instruccion explicita de release.
- Verificar `BUILD SUCCESSFUL` antes de instalar.
- Verificar `Success` en adb install antes de lanzar.

---

## 11. Documentacion relacionada

| Documento | Funcion |
|-----------|---------|
| `docs/agent_roles.md` | Roles de subagentes (Explore, Plan, 3D Design, Build, Commit) |
| `docs/AGENT_ONBOARDING_GUIDE.md` | Como interpretar el transfer pack |
| `docs/INCREMENTAL_COLLECTIBLE_GAME_FOUNDATIONS.md` | Patron base incremental |
| `docs/COLLECTIBLE_FAMILY_SYSTEM.md` | Sistema de familias de coleccionables |
| `docs/PHASED_COLLECTIBLE_DESIGN_DOCTRINE.md` | Doctrina de diseño por fases |
| `docs/BLENDER_MCP_WORKFLOW.md` | Pipeline Blender headless |
| `docs/BLENDER_QUALITY_ELEVATION_GUIDE.md` | Guia de calidad visual |
| `docs/GLOSSARY_AND_NAMING.md` | Glosario y convenciones de naming |
| `docs/REFERENCE_IMPLEMENTATION_MAP.md` | Mapa de referencia (Tap Empire) |
