# 🚀 CBDCounter v1.1.0 - Play Store Ready

Segunda versión oficial con correcciones críticas y preparación completa para publicación en Google Play Store.

---

## ✨ Novedades

### Cumplimiento Legal
- **Disclaimer médico** obligatorio en el primer uso de la app
- **Política de privacidad** completa disponible públicamente
- **Documentación oficial** en GitHub Pages
- Cumple con políticas de Google Play y RGPD

### Mejoras de Usuario
- ✅ Widget ahora respeta los emojis personalizados configurados
- ✅ Confirmación obligatoria antes de importar CSV (previene pérdida de datos)

---

## 🐛 Correcciones

- **[Crítico]** Widget usaba emojis hardcodeados en lugar de los personalizados del usuario
- **[Crítico]** Importar CSV borraba todos los datos sin advertencia previa
- Optimización del código con ProGuard/R8

---

## 🔧 Cambios Técnicos

### Optimización
- Migrado de APK a **Android App Bundle (AAB)** (obligatorio para Play Store)
- **ProGuard/R8** configurado y activo
- Tamaño reducido en ~40%: 5.8MB → 3.5MB
- Código ofuscado para mayor seguridad

### Actualización
- **Target SDK 34** (Android 14)
- **versionCode 2** (era 1)
- Configurado Google Play App Signing
- Firmado digitalmente con keystore seguro

---

## 📦 Descargas

### Para Usuarios Finales
**Android App Bundle (Recomendado para sideload vía adb):**
- `app-release.aab` (4.6 MB)

**⚠️ Nota:** Para instalar directamente en tu dispositivo, espera a la publicación en Google Play Store, o simplemente descarga la última actualización en Releases.

---

## 📋 Comparación con v1.0

| Feature | v1.0 | v1.1.0 |
|---------|------|--------|
| Disclaimer médico | ❌ | ✅ |
| Widget con emojis personalizados | ❌ (bug) | ✅ |
| Confirmación import CSV | ❌ | ✅ |
| Política de privacidad | ❌ | ✅ |
| Código ofuscado | ❌ | ✅ |
| Tamaño release | 5.8 MB | 3.5 MB |
| Play Store Ready | ❌ | ✅ |

---

## 🔒 Privacidad

Como siempre:
- ✅ CERO recopilación de datos
- ✅ Almacenamiento 100% local
- ✅ Sin analytics ni tracking
- ✅ Sin servicios de terceros

---

## 📚 Documentación

- **Política de Privacidad:** [https://d4vram369.github.io/CBDcounter/privacy-policy](https://d4vram369.github.io/CBDcounter/privacy.html)
- **CHANGELOG completo:** [CHANGELOG.md](CHANGELOG.md)

---

## ⚠️ Descargo de Responsabilidad

CBDCounter es una herramienta de tracking personal y **NO** constituye dispositivo médico ni consejo médico profesional.

---

## 👨‍💻 Desarrollador

**D4vRAM**
📧 d4vram369@gmail.com
🇮🇨 Gran Canaria, España

---

**Instalación desde Play Store próximamente** 🎉
