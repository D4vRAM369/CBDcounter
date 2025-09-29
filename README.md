
## CBDcounter
[English version](README_english-version.md)

**Contador minimalista de CBD para Android.**

<img width="512" height="512" alt="icono_cbdcounter2_cleaned" src="https://github.com/user-attachments/assets/0d6eff86-af54-4a67-a132-9e63a87eef40" />

CBDcounter empezó como un proyecto para uso personal que no tenía intención de subir o publicar, pero luego pensé *"¿Por qué no?"*.

Interfaz simple y minimalista. Además del funcionamiento dentro de la propia app, también permite registrar rápidamente las tomas mediante un widget agregado a pantalla de inicio, y visualizar la evolución de ese día. 

La posibilidad de poder añadir notas fue implementada un tiempo después de empezar el proyecto a partir de un Artifact de Claude Sonnet 4, que fue la base sobre la que seguí iterando mientras creaba y aprendía de forma práctica.

Implementado recientemente para mayor comodidad por preferencia personal, el método *appendTimestampToTodayNote()* para que cada vez que se pulse +1, crear notas en ese día con un 🔸 seguido de la hora en formato 24H.

También fueron implementados recientemente las opciones de Exportar e Importar CSV, que primero estaban implementados como MaterialButton en la parte inferior de la app, pero esto quitaba espacio al historial (RecyclerView).
Para optimizar la interfaz, los sustituí por ImageButton/IconButton, más compactos e intuitivos, y los coloqué en la parte superior.

Como todos los proyectos que he desarrollado hasta ahora (publicos y aún sin publicar), ha sido desarrollado siguiendo un enfoque de aprendizaje basado en proyectos, o Project-Based Learning, mediante el uso de IA para implementar nuevas funciones, aprender nuevos conceptos, tomar notas de como funciona cada cosa, y posteriormente al final de cada fix o feature, exportando el chat para estudiarlo a medida que sigo modificando y probando la app, especialmente las que uso diariamente.


Hecho para uso personal, y compartido por el amor al open source y su comunidad ❤️


## 📸 Capturas de pantalla
Aquí algunas capturas en tema claro y oscuro, mostrando el contador, el historial, las notas y el widget en acción 👇

<p align="center">
  <img src="https://github.com/user-attachments/assets/0cb21950-e715-40e3-827d-6a77ddcdc0fd" width="260"/>
  <img src="https://github.com/user-attachments/assets/1585a809-8803-44fa-8ea1-20053e5263b4" width="260"/>
  <img src="https://github.com/user-attachments/assets/401145c9-49df-4b6a-80d7-7466122e7f05" width="260"/>
</p>

<details>
  <summary>Ver más capturas</summary>

  <p align="center">
    <img src="https://github.com/user-attachments/assets/615ee511-6f20-4221-bfe9-0f8eeff9c396" width="260"/>
    <img src="https://github.com/user-attachments/assets/c276f201-bc78-4e82-bfa6-1c7cbc06bef0" width="260"/>
    <img src="https://github.com/user-attachments/assets/0800c256-43b1-4203-9009-7bf2887d6f34" width="260"/>
  </p>
  <p align="center">
    <img src="https://github.com/user-attachments/assets/60026c54-8bdf-47e2-9bce-6afda6e882c3" width="260"/>
    <img src="https://github.com/user-attachments/assets/0245b3d9-4014-4eff-9ef8-161ca4e311ce" width="260" />

  </p>

</details>

---

## ✨ Características actuales
- 📲 Widget para sumar +1 rápidamente desde la pantalla de inicio.
- 🙂 Los emojis cambian dependiendo de la cantidad que lleves fumados en cada día (puedes editar los iconos que quieres a tu gusto en MainActivity.kt:línes 272-286_ en la función _getEmoji_, y desde 597-609 en la función _onBindViewHolder_).
  
El primero corresponde a los emojis que se actualizan a medida que suma el contador en el recuadro con cada día, y el segundo corresponde a que dicho icono salga en la parte de arriba, junto al contador.
También deberías editarlos acorde a los cambios que quieras en _CBDWidgetProvider:137-152_, pues son los iconos que aparecerán en el widget de pantalla si lo usas.

- 📝 Posibilidad de tomar notas cada día.
- 🔒 Persistencia de datos aunque se cierre la app.
- 🎨 Interfaz sencilla y minimalista.
- 🔄 Posibilidad de Exportar CSV e Importar CSV

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
