
## CBDcounter
[English version](README_english-version.md)

**Contador minimalista de CBD para Android.**

<img width="512" height="512" alt="icono_cbdcounter2_cleaned" src="https://github.com/user-attachments/assets/0d6eff86-af54-4a67-a132-9e63a87eef40" />


[![release](https://img.shields.io/github/v/release/D4vRAM369/CBDcounter?label=release)](https://github.com/D4vRAM369/CBDcounter/releases)
[![license](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
![minSdk](https://img.shields.io/badge/minSdk-24+-informational)
![targetSdk](https://img.shields.io/badge/targetSdk-35-informational)
<img src="https://img.shields.io/badge/ClaudeCode_&_PBL-powered-4B0082?style=flat-square&logo=anthropic&logoColor=white"/>
![privacy](https://img.shields.io/badge/privacy-100%25%20local-success)

---

> 📢 **Novedad — v1.1 ya disponible (10/11/2025)**  
> Primera versión pública estable en GitHub y Play Store *(pendiente de verificación)*.
>
> 🟢 **Novedades principales:**
> - Widget de contador diario mejorado  
> - Calendario con estadísticas  
> - Notas con marcas de tiempo  
> - 151 emojis personalizables  
> - Import/Export **CSV**  
> - Privacidad total: **cero datos enviados**, **sin anuncios**, **sin analytics** (_esto siempre ha sido así, pero la política de Google Play me obliga a aclararlo, por ello hay una nueva página de Política de Privacidad)_.
> - La aplicación **será publicada en la PlayStore** desde que acabe _el trámite burocrático con Google y el papeleo_, que sigue todo en marcha.

🔗 **Descarga v1.1:**  
👉 [Ver Release oficial (v1.1)](https://github.com/D4vRAM369/CBDcounter/releases/tag/v1.1)

🛡️ **Política de Privacidad:**  
[https://d4vram369.github.io/CBDcounter/privacy.html](https://d4vram369.github.io/CBDcounter/privacy.html)

---

CBDcounter empezó como otro proyecto más para uso personal, que no tenía intención de subir o publicar, pero ayer por la tarde pensé *"¿Por qué no?"*.

Interfaz simple y minimalista. Además del funcionamiento dentro de la propia app, también permite registrar rápidamente las tomas mediante un widget agregado a pantalla de inicio, y visualizar la evolución de ese día. 

La posibilidad de poder añadir notas fue implementada un tiempo después de empezar el proyecto a partir de un Artifact de Claude Sonnet 4, que fue la base sobre la que seguí iterando mientras creaba y aprendía de forma práctica.

Implementado en su momento para mayor comodidad por preferencia personal, el método *appendTimestampToTodayNote()* para que cada vez que se pulse +1, crear notas en ese día con un 🔸 seguido de la hora en formato 24H. Al igual que ha sido incluido en el código también el mismo método pero a la inversa, para que al pulsar -1 se borre el string que se ha generado _(por si +1 se pulsa por error, no tener que eliminarlo de la nota de forma manual)_.

Desde poco después del inicio fueron implementadas las opciones de Exportar e Importar CSV, que primero estaban implementados como MaterialButton en la parte inferior de la app, pero esto quitaba espacio al historial (RecyclerView).
Para optimizar la interfaz, los sustituí por ImageButton/IconButton, más compactos e intuitivos, y los coloqué en la parte superior.

Como todos los proyectos que he desarrollado hasta ahora (publicos y aún sin publicar), ha sido desarrollado siguiendo un enfoque de aprendizaje basado en proyectos, o Project-Based Learning, mediante el uso de IA para implementar nuevas funciones, aprender nuevos conceptos, tomar notas de como funciona cada cosa, y posteriormente al final de cada fix o feature, exportando el chat para estudiarlo a medida que sigo modificando y probando la app, especialmente las que uso diariamente.


Hecho para uso personal, y compartido por el amor al open source y su comunidad ❤️


## 📸 Capturas de pantalla (versión 1.1)
Aquí algunas capturas en tema claro y oscuro, mostrando el contador, el historial, las notas y el widget en acción 👇
<img width="400" height="699" alt="image" src="https://github.com/user-attachments/assets/e6c8e1f9-6555-4633-a44f-47a3833c1259" />
<p align="center">
  <img src="https://github.com/user-attachments/assets/b2f65ea2-a472-4460-9da7-2e7274a72605" width="260"/>
  <img src="https://github.com/user-attachments/assets/94f10a04-3c53-44e3-8681-7000b39ba78b" width="260"/>
  <img src="https://github.com/user-attachments/assets/25616dbf-3ba4-499c-9a26-c0287dea2d9a" width="260"/>
</p>

<details>
  <summary>Ver más capturas</summary>

  <p align="center">
    <img src="https://github.com/user-attachments/assets/2ff86285-f835-4ec8-854d-4128b581afeb" width="260"/>
    <img src="https://github.com/user-attachments/assets/e44e2091-8693-4ae2-8b69-119773a83120" width="260"/>
    <img src="https://github.com/user-attachments/assets/551b7675-42d8-4fcb-8200-c724135af18c" width="260" />
    <img src="https://github.com/user-attachments/assets/5a6cabbb-ef51-4cfb-a5b2-ab2d19f2181f" width="260" />
  </p>
  <p align="center">

 


---

## ✨ Características actuales
- 📲 Widget para sumar +1 rápidamente desde la pantalla de inicio, que también usa un timestamp con un emoji 🔸 previamente  de la hora en formato 24H.
- ➖ Al clickar en -1 también elimina la cadena de texto _(o string)_ creada al clickar en +1 con el emoji y la hora en notas adicionales de ese día, haciéndolo más cómodo si se pulsa +1 por error.
- 🙂 Los emojis cambian dependiendo de la cantidad que lleves fumados en cada día, además de la posibilidad de editar los iconos a tu conveniencia entre los 151 disponibles.
- 📝 Posibilidad de tomar notas cada día.
- 🔒 Persistencia de datos aunque se cierre la app.
- 🎨 Interfaz sencilla y minimalista.
- 🔄 Posibilidad de Exportar CSV e Importar CSV
- 💨 Nuevo botón +1 añadido encima del +1 principal de CBD. Éste vale para si es un aliñado, seleccionar con que lo quieres aliñar. Luego se mostrará en notas con un 🟢 si el valor elegido es weed, o un 🟤 si el valor elegido es polen. Aquí en ésta captura muestro un ejemplo:

  
  <img width="640" height="317" alt="image" src="https://github.com/user-attachments/assets/da9f0188-0604-40ae-aee4-06caebefc9e7" />


---

## 📜 Licencia
Este proyecto está bajo la licencia [GPL-3.0](LICENSE).


## 🚀 Cómo compilar
1. Clonar este repositorio: 
   ```bash
   
   git clone https://github.com/D4vRAM369/CBDcounter.git
   cd CBDcounter
   ./gradlew clean assembleDebug
   ./gradlew clean assembleRelease
