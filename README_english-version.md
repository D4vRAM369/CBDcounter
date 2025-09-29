
## CBDcounter

<img width="512" height="512" alt="icono_cbdcounter2_cleaned" src="https://github.com/user-attachments/assets/0d6eff86-af54-4a67-a132-9e63a87eef40" />


**Minimalist CBD counter for Android**

It started as a project for personal use that I had no intention of uploading or publishing, but then I thought, “Why not?”

Simple and minimalist interface. In addition to working within the app itself, it also allows you to quickly record your doses using a widget added to the home screen and view your progress for the day. 

The ability to add notes was implemented some time after starting the project based on an Artifact by Claude Sonnet 4, which was the basis on which I continued to iterate while creating and learning in a practical way.

Recently implemented for convenience based on personal preference, the *appendTimestampToTodayNote()* method creates notes for that day with a 🔸 followed by the time in 24-hour format each time +1 is pressed.

The CSV Export and Import options were also recently implemented. They were first implemented as MaterialButton at the bottom of the app, but this took away space from the history (RecyclerView).
To optimize the interface, I replaced them with ImageButton/IconButton, which are more compact and intuitive, and placed them at the top.




Like all the projects I have developed so far (both published and unpublished), it has been developed using a project-based learning approach, using AI to implement new features, learn new concepts, take notes on how everything works, and then at the end of each fix or feature, exporting the chat to study it as I continue to modify and test the app, especially the ones I use daily.


Made for personal use, and shared for the love of open source and its community ❤️

## 📸 Screenshots
Here are some screenshots in light and dark themes, showing the counter, history, notes, and widget in action 👇

<p align="center">
  <img src="https://github.com/user-attachments/assets/0cb21950-e715-40e3-827d-6a77ddcdc0fd" width="260"/>
  <img src="https://github.com/user-attachments/assets/1585a809-8803-44fa-8ea1-20053e5263b4" width="260"/>
  <img src="https://github.com/user-attachments/assets/401145c9-49df-4b6a-80d7-7466122e7f05" width="260"/>
</p>

<details>
  <summary>See more screenshots</summary>

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

## ✨ Current features
- 📲 Widget to quickly add +1 from the home screen.
- 📝 Ability to take notes each day.
- 🔒 Data persistence even if the app is closed using SharedPreferences.
- 🎨 Simple and minimalist interface.
- 🔄 Ability to export CSV and import CSV.

---

## License

GPLv3.0


## 🚀 How to compile
1. Clone this repository:
```bash
  git clone https://github.com/D4vRAM369/CBDcounter2.git
  
  ./gradlew clean assembleRelease

