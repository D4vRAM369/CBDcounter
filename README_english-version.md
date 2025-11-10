
## CBDcounter

**Minimalist CBD counter for Android**

<img width="512" height="512" alt="icono_cbdcounter2_cleaned" src="https://github.com/user-attachments/assets/0d6eff86-af54-4a67-a132-9e63a87eef40" />


[![release](https://img.shields.io/github/v/release/D4vRAM369/CBDcounter?label=release)](https://github.com/D4vRAM369/CBDcounter/releases)
[![license](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
![minSdk](https://img.shields.io/badge/minSdk-24+-informational)
![targetSdk](https://img.shields.io/badge/targetSdk-35-informational)
<img src="https://img.shields.io/badge/ClaudeCode_&_PBL-powered-4B0082?style=flat-square&logo=anthropic&logoColor=white"/>
![privacy](https://img.shields.io/badge/privacy-100%25%20local-success)

---

> 📢 **New — v1.1 now available (11/10/2025)**  
> First stable public release on GitHub and the Play Store *(pending verification)*.
>
> 🟢 **Main new features:**
> - Improved daily counter widget  
> - Calendar with statistics  
> - Notes with timestamps  
> - 151 customizable emojis  
> - Import/Export **CSV**  
> - Total privacy: **zero data sent**, **no ads**, **no analytics** (_this has always been the case, but Google Play policy requires me to clarify this, which is why there is a new Privacy Policy page)_.
> - The app **will be published on the PlayStore** as soon as _the bureaucratic process with Google and the paperwork_ are completed, which is still in progress.

🔗 **Download v1.1:**  
👉 [See Official Release (v1.1)](https://github.com/D4vRAM369/CBDcounter/releases/tag/v1.1)

🛡️ **Privacy Policy:**  
[https://d4vram369.github.io/CBDcounter/privacy.html](https://d4vram369.github.io/CBDcounter/privacy.html)

---

CBDcounter started out as just another personal project that I had no intention of uploading or publishing, but yesterday afternoon I thought, “Why not?”

Simple, minimalist interface. In addition to working within the app itself, it also allows you to quickly record your doses using a widget added to the home screen and view your progress for the day. 

The ability to add notes was implemented some time after starting the project, based on an Artifact by Claude Sonnet 4, which was the basis on which I continued to iterate while creating and learning in a practical way.

Implemented at the time for convenience and personal preference, the *appendTimestampToTodayNote()* method creates notes for that day with a 🔸 followed by the time in 24-hour format each time +1 is pressed. The same method has also been included in the code but in reverse, so that when -1 is pressed, the string that has been generated is deleted (in case +1 is pressed by mistake, so you don't have to delete it from the note manually).

Shortly after launch, the Export and Import CSV options were implemented. They were first implemented as MaterialButton at the bottom of the app, but this took up space in the history (RecyclerView).
To optimize the interface, I replaced them with ImageButton/IconButton, which are more compact and intuitive, and placed them at the top.

A Stats menu has also been added with a calendar and a dynamic component with legends indicating the value corresponding to each emoji, and the emoji linked to each day according to the selected amount. As before, users can now conveniently edit the emojis they want to appear in the different values up to 12.

Like all the projects I have developed so far (both published and unpublished), it has been developed using a project-based learning approach, using AI to implement new features, learn new concepts, take notes on how everything works, and then at the end of each fix or feature, exporting the chat to study it as I continue to modify and test the app, especially the ones I use daily.


Made for personal use, and shared for the love of open source and its community ❤️



## 📸 Screenshots
Here are some screenshots in light and dark themes, showing the counter, history, notes, and widget in action 👇

<img width="400" height="699" alt="image" src="https://github.com/user-attachments/assets/e6c8e1f9-6555-4633-a44f-47a3833c1259" />
<p align="center">
  <img src="https://github.com/user-attachments/assets/b2f65ea2-a472-4460-9da7-2e7274a72605" width="260"/>
  <img src="https://github.com/user-attachments/assets/94f10a04-3c53-44e3-8681-7000b39ba78b" width="260"/>
  <img src="https://github.com/user-attachments/assets/25616dbf-3ba4-499c-9a26-c0287dea2d9a" width="260"/>
</p>

<details>
  <summary>See more screenshots</summary>

  <p align="center">
    <img src="https://github.com/user-attachments/assets/2ff86285-f835-4ec8-854d-4128b581afeb" width="260"/>
    <img src="https://github.com/user-attachments/assets/e44e2091-8693-4ae2-8b69-119773a83120" width="260"/>
    <img src="https://github.com/user-attachments/assets/551b7675-42d8-4fcb-8200-c724135af18c" width="260" />
    <img src="https://github.com/user-attachments/assets/5a6cabbb-ef51-4cfb-a5b2-ab2d19f2181f" width="260" />
  </p>
  <p align="center">


</details>

---

## ✨ Current features
- 📲 Widget to quickly add +1 from the home screen, which also uses a timestamp with an emoji 🔸  preceding the time in 24-hour format.
- ➖ Clicking -1 also deletes the text string created when clicking +1 with the emoji and time in additional notes for that day, making it more convenient if you press +1 by mistake.
- 🙂 The emojis change depending on how much you smoke each day, and you can also edit the icons to your liking from among the 151 available.
- 📝 Ability to take notes every day.
- 🔒 Data persistence even when the app is closed.
- 🎨 Simple and minimalist interface.
- 🔄 Ability to export CSV and import CSV.
- 📊📆 Statistics added with a calendar that indicates the legend with the value assigned to each emoji, with a dynamic legend that changes according to the selected emojis.
- 💨 New +1 button added above the main CBD +1. This is useful if you are seasoning something, to select what you want to season it with. It will then be displayed in notes with a 🟢 if the chosen value is weed, or a 🟤 if the chosen value is pollen. Here in this screenshot I show an example:

  
  <img width="640" height="317" alt="image" src="https://github.com/user-attachments/assets/da9f0188-0604-40ae-aee4-06caebefc9e7" />


---

## 📜 License
This project is licensed under the [GPL-3.0](LICENSE).


## 🚀 How to compile
1. Clone this repository:
   ```bash
   git clone https://github.com/D4vRAM369/CBDcounter.git
   cd CBDcounter
   ./gradlew clean assembleDebug
   ./gradlew clean assembleRelease
