
## CBDcounter

**Contador minimalista de CBD para Android**

Empezó como un proyecto para uso personal que no tenía intención de subir o publicar, pero luego pensé *"¿Por qué no?"*.

Interfaz simple y minimalista. Además del funcionamiento dentro de la propia app, también permite registrar rápidamente las tomas mediante un widget agregado a pantalla de inicio, y visualizar la evolución de ese día. 

La posibilidad de poder añadir notas fue implementada un tiempo después de empezar el proyecto a partir de un Artifact de Claude Sonnet 4, que fue la base sobre la que seguí iterando mientras creaba y aprendía de forma práctica.

Implementado recientemente para mayor comodidad por preferencia personal, el método *appendTimestampToTodayNote()* para que cada vez que se pulse +1, crear notas en ese día con un 🔸 seguido de la hora en formato 24H.

También fueron implementados recientemente las opciones de Exportar e Importar CSV, que primero estaban implementados como MaterialButton en la parte inferior de la app, pero esto quitaba espacio al historial (RecyclerView).
Para optimizar la interfaz, los sustituí por ImageButton/IconButton, más compactos e intuitivos, y los coloqué en la parte superior.

Como todos los proyectos que he desarrollado hasta ahora (publicos y aún sin publicar), ha sido desarrollado siguiendo un enfoque de aprendizaje basado en proyectos, o Project-Based Learning, mediante el uso de IA para implementar nuevas funciones, aprender nuevos conceptos, tomar notas de como funciona cada cosa, y posteriormente al final de cada fix o feature, exportando el chat para estudiarlo a medida que sigo modificando y probando la app, especialmente las que uso diariamente.


Hecho para uso personal, y compartido por el amor al open source y su comunidad ❤️

---

## ✨ Características actuales
- 📲 Widget para sumar +1 rápidamente desde la pantalla de inicio.
- 📝 Posibilidad de tomar notas en cada día.
- 🔒 Persistencia de datos aunque se cierre la app mediante SharedPreferences.
- 🎨 Interfaz sencilla y minimalista.
- 🔄 Posibilidad de Exportar CSV e Importar CSV

---

## Licencia

GPLv3.0


## 🚀 Cómo compilar
1. Clonar este repositorio:
   ```bash
   git clone https://github.com/D4vRAM369/CBDcounter2.git
   
   ./gradlew clean assembleRelease
