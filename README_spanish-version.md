# 🌿 Contador de CBD y THC

> Tracker personal de reducción de daños para el consumo de CBD y THC — construido con Kotlin y Material Expressive 3.

[![Version](https://img.shields.io/badge/versión-1.5-6750A4?style=flat-square)](https://github.com/d4vram/CBDcounter2/releases/tag/v1.5)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green?style=flat-square&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7B68EE?style=flat-square&logo=kotlin)](https://kotlinlang.org)

**[🇬🇧 English version](README.md)**

---

## ¿Qué es esto?

Contador de CBD y THC es una app de tracking **privada y sin conexión**. Sin cuenta, sin datos que salgan del dispositivo. Te ayuda a monitorizar tu consumo diario de CBD y THC con una interfaz limpia y expresiva — para que puedas tomar decisiones conscientes e informadas sobre tu consumo.

> ⚠️ Esta app **no es un dispositivo médico**. No proporciona consejo médico y no promueve el consumo de sustancias. Úsala de forma responsable y consulta siempre con un profesional de la salud para decisiones relacionadas con tu bienestar.

---

## Características

### 🔢 Contador dual
- Contadores separados de **CBD** y **THC** para el mismo día
- Botón **+1** rápido para la sustancia activa
- **Añadir aliñado** — registra una sesión etiquetada como *weed* 🌿 o *polen* 🍫 (siempre suma a THC)
- Corrección **−1** con diálogo de confirmación; feedback si el contador ya está en 0
- **Reset** con confirmación — solo resetea la sustancia activa de hoy

### 📅 Calendario
- Cuadrícula mensual con un **emoji en cada día** que refleja el total de ese día
- Toca cualquier día para abrir un **Modal de día** con desglose completo (CBD, THC, nota de voz, registro horario)
- Navegación mes anterior/siguiente
- **Leyenda de emojis** — toda la escala de consumo explicada de un vistazo

### 📊 Panel de Estadísticas  *(→ chip 📊 Estadísticas)*
- Tarjetas de métricas **Hoy / Semana / Promedio / Racha** — colores ME3 Expresivos:
  - Hoy → azul hielo · Semana → naranja cálido · Promedio → verde lima · Racha → lavanda
- Sección **Patrones** — día de la semana con mayor promedio de consumo
- **Mini gráfico de líneas** integrado (7D / 14D / 30D)

### 📈 Gráfico de Evolución
- Gráfico de líneas suavizado a pantalla completa: rangos **7D · 14D · 30D · 60D**
- Navega hacia atrás en el tiempo con los botones ← →
- Etiquetas de valor con **salto inteligente** para vistas 30D/60D sin superposición

### 🎙️ Notas de Voz
- Graba una nota de audio breve para cualquier día desde el historial o el Modal de día
- Reproducción y borrado — almacenadas de forma privada en el dispositivo (formato M4A)
- Gestión del permiso `RECORD_AUDIO` en el primer uso

### 📤 Importar / Exportar CSV
- Exporta todo el historial como archivo `.csv` (compatible con cualquier hoja de cálculo)
- Importa desde un `.csv` — restaura o fusiona datos por día
- Acceso desde los iconos superiores derecha (↑ exportar · ↓ importar)

### ☀️ Toggle Tema Claro / Oscuro
- Tema completo **Material Expressive 3** día/noche
- Botón ☀️/🌙 siempre visible en la columna de iconos superior derecha
- Los iconos de la barra de estado se adaptan automáticamente
- Preferencia guardada entre sesiones

### 🏠 Widget en pantalla de inicio (2×2)
- Diseño de tarjeta ME3: púrpura sólido `#6750A4` (claro) / índigo profundo `#1E1640` (oscuro)
- Muestra: **fecha** · **badge modo CBD/THC** · **emoji estado** · **contador total**
- Cuatro botones de acción: 🌿 Weed · ↺ Reset · 🍫 Polen · **+1**
- Actualización automática a medianoche vía AlarmManager

### ⚙️ Ajustes
- Cambia el modo de tracking por defecto: **CBD ↔ THC**
- **Personaliza los emojis** para cada nivel de consumo (se reflejan en contador, calendario y widget)
- Exportar / Importar CSV para copia de seguridad y restauración

---

## Stack técnico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.0 |
| UI | Material Design 3 / Material Expressive 3 |
| Arquitectura | Single-Activity + Fragments + BottomSheetDialogFragment |
| Almacenamiento | SharedPreferences (sin dependencias, offline-first) |
| Audio | MediaRecorder API — M4A/AAC |
| Gráfico | `LineChartView` personalizado — Canvas API, spline Catmull-Rom |
| Widget | AppWidgetProvider + RemoteViews |
| Sistema de temas | AppCompatDelegate DayNight + tokens en `values-night/` |
| SDK mínimo | API 26 (Android 8.0 Oreo) |
| SDK objetivo | API 35 (Android 15) |

---

## Versiones

| Versión | Novedades principales |
|---------|----------------------|
| **v1.5** | Material Expressive 3 · Toggle claro/oscuro ☀️🌙 · Tarjetas ME3 en Dashboard · Widget ME3 rediseñado · Evolución 14D/60D · Fix labels gráfico |
| **v1.4.1** | Calendario con mapa de emojis · Panel Estadísticas · Fix crash notas de voz · Importar/Exportar CSV · Botones rotos reemplazados |
| **v1.4** | Contador dual CBD/THC · Pestañas de historial (Semana/Mes/Todo) · Modal de día |
| **v1.3** | Widget en pantalla de inicio · Pantalla de ajustes y emojis personalizables |
| **v1.0–1.2** | Lanzamiento inicial · Contador básico · Historial simple |

---

## Instalación

### Compilar desde fuente

```bash
git clone https://github.com/d4vram/CBDcounter2.git
cd CBDcounter2
git checkout main          # versión estable
```

Abre en **Android Studio Ladybug 2024.2+** y ejecuta en dispositivo/emulador con API 26+.

### Releases

APKs precompilados disponibles en la [página de Releases](https://github.com/d4vram/CBDcounter2/releases).

---

## Privacidad

- ✅ **100% sin conexión** — sin permiso de internet declarado
- ✅ Sin analíticas, sin crash reporting, sin tracking de ningún tipo
- ✅ Todos los datos en `SharedPreferences` y almacenamiento privado del dispositivo
- ✅ Las notas de voz se guardan en almacenamiento interno privado — inaccesible para otras apps

---

## Limitaciones conocidas / Roadmap

- [ ] i18n: ~30 strings en español hardcodeados en layouts — pendiente migrar a `strings.xml` + `values-en/`
- [ ] Layout responsive del widget para 2×3 (mostrar desglose CBD·THC al ampliar el widget)
- [ ] Modo oscuro automático siguiendo el sistema (actualmente solo toggle manual)
- [ ] Migración a Room DB para conjuntos de datos grandes (actualmente SharedPreferences)

---

## Licencia

Licencia MIT — ver [LICENSE](LICENSE).

---

## Aviso médico

Esta aplicación es únicamente una herramienta de tracking personal. **No es un dispositivo médico**, no proporciona consejo médico y no promueve ni facilita la compra ni venta de ninguna sustancia. Consulta siempre con un profesional de la salud cualificado para decisiones relacionadas con tu bienestar.
