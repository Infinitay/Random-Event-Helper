# [Random Event Helper](https://runelite.net/plugin-hub/show/random-event-helper)
A RuneLite plugin to help solve random events by displaying solutions for each random event.

![image](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/random-event-helper)
![image](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/random-event-helper)

## Features
- Highlights the solution for each supported random event

## Supported Random Events
### [Surprise Exam](https://oldschool.runescape.wiki/w/Surprise%20Exam)
<details>
  <summary>Screenshot</summary>
  <img width="1932" height="698" alt="image" src="https://github.com/user-attachments/assets/94df8805-414d-48fb-a7fb-7d4535080d6a" />
</details>
<details>
  <summary>Video</summary>
  
  https://github.com/user-attachments/assets/793d3d0c-b31d-42fc-ae44-2891fc67f37a
</details>

- Highlights the correct answer for both the matching cards and next item sequence
  - Implemented via relations and keywords to hopefully support any variation of the questions/answers
    - As a result of using keywords, there could be a chance the solutions are incorrect. If this is the case, please open an issue on the [GitHub repository](https://github.com/Infinitay/Random-Event-Solver/issues?q=sort%3Aupdated-desc+is%3Aissue+is%3Aopen) with a screenshot of the random event question and/or the logs via the chat command `::exportexampuzzle` or `::exportexampuzzles` to copy the data to your clipboard and output it to the log file.
    - _Disclaimer, the **relation matching system** was generated with AI, with me fixing the bugs and building upon the keywords._

### [Beekeeper](https://oldschool.runescape.wiki/w/Beekeeper_(Random_Event))
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/061a19ec-73bc-4841-8312-f362e99cc3a9" />
</details>
<details>
  <summary>Video</summary>
  
  https://github.com/user-attachments/assets/23a1c491-03dd-44f5-886b-7875647be379
</details>
- Displays the correct order to place the different hive piece

### [Pinball](https://oldschool.runescape.wiki/w/Pinball)
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/291a38f9-9c3d-49f8-8be6-218925076cc9" />
</details>
<details>
  <summary>Video</summary>
  
  https://github.com/user-attachments/assets/221ee9a1-2557-4ef8-8ee1-7c6dc9361878
</details>

- Highlights the correct pillar to tag
  - The current event already tags the correct pillar, but this highlight will make it more obvious

### [Freaky Forester](https://oldschool.runescape.wiki/w/Freaky%20Forester)
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/051f5721-363b-47e1-a417-6346b8e987c7" />
</details>
<details>
  <summary>Video</summary>
  
  https://github.com/user-attachments/assets/38dfbc15-d007-44e0-8661-8e501f6e7797
</details>

- Highlights the correct [pheasant](https://oldschool.runescape.wiki/w/Pheasant) to kill
  - Three various modes depending on user preference [since you're no longer required to kill a specific pheasant](https://oldschool.runescape.wiki/w/Pheasant#:~:text=Despite%20the%20Freaky%20Forester%20asking%20for%20the%20raw%20pheasant%20from%20a%20pheasant%20with%20a%20specific%20number%20of%20tails%2C%20all%20pheasants%20will%20drop%20the%20same%20raw%20pheasant%20as%20if%20it%20was%20correct%2C%20which%20the%20Freaky%20Forester%20will%20accept%20for%20a%20reward)

      | Mode     | Description                                                       |
      |----------|-------------------------------------------------------------------|
      | Specific | Highlights the pheasant with the required tail-feather count      |
      | Nearest  | Highlights any nearest pheasant (disregarding tail-feather count) |
      | All      | Highlights any and all pheasants                                  |
- No support for highlighting the raw pheasant drop itself

### [Drill Demon](https://oldschool.runescape.wiki/w/Drill%20Demon)
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/c35c23cc-5ae5-4d93-b185-a2cbf9bf7577" />
</details>
<details>
  <summary>Video</summary>

  https://github.com/user-attachments/assets/c1cdc8f9-1aec-40b2-ae08-8397c138139f
</details>

- Highlights the correct exercise mat to step on

### [Gravedigger](https://oldschool.runescape.wiki/w/Gravedigger)
<details>
  <summary>Screenshot</summary>

  _Prior to highlight modes being added_
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/14b231e7-2a89-462b-97ce-ec2192d7b889" />
</details>
<details>
  <summary>Video</summary>

  https://github.com/user-attachments/assets/a911d686-5b0c-43c8-b6a6-9ba45bea7b63
</details>

- Displays the correct coffin solution for all the graves

    | Mode             | Description                                                 |
    |------------------|-------------------------------------------------------------|
    | Gravestone Icon  | Shows the respective skill/item icon related to the grave   |
    | Coffin Icon      | Shows the respective skill/item icon related to the coffin  |
    | Highlight Grave  | Highlights the grave and gravestone with a respective color |
    | Highlight Coffin | Highlights the coffin with a respective color               |
    | Grave Text       | Displays a text associated with the grave                   |
    | Coffin Text      | Displays a text associated with the coffin                  |
  - Keep in mind you are able to select none or multiple modes at once (CTRL + Click to select multiple or to deselect)
- Swaps "Use" to be the first option on a coffin
- View either skill icons or item icons related to the grave

### [Mime](https://oldschool.runescape.wiki/w/Mime_(Random_Event))
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/f7c8422c-8aa6-4123-9171-aaeb7afe42a7" />
</details>
<details>
  <Summary>Video</Summary>

https://github.com/user-attachments/assets/0260b0c0-3024-403b-8fcb-c24da748b03b
</details>

- Highlights the correct emote to perform
- Displays the emote being performed above the mime's head

### [Maze](https://oldschool.runescape.wiki/w/Maze)
_REQUIRES [SHORTEST PATH](https://runelite.net/plugin-hub/show/shortest-path) PLUGIN TO BE INSTALLED_
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/6e3881fc-da5c-47fc-9080-718d84862e60" />
</details>
<details>
  <Summary>Video</Summary>

https://github.com/user-attachments/assets/b9d810ba-be0b-4b09-a009-9aa5c2be8fb4
</details>

- Automatically sets the shortest path to the Strange Shrine where the maze exits

### [Sandwich Lady](https://oldschool.runescape.wiki/w/Sandwich_lady)
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/62ab89fc-96ec-471b-901f-d1cde0a5c4cf" />
</details>
<details>
  <summary>Video</summary>
  
  https://github.com/user-attachments/assets/79ddfb8c-0022-4c3c-8258-4f1179eb3c91
</details>

- Highlights the correct food offered by the Sandwich Lady and available to take

### [Quiz Master](https://oldschool.runescape.wiki/w/Quiz_Master)
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/e3790169-2d15-4910-9dd7-79d239af3396" />
</details>
<details>
  <summary>Video</summary>

  https://github.com/user-attachments/assets/d24eaf7f-4140-49c0-9291-1aac24a0a43d
</details>

- Highlights the correct answer indicating the odd item out of the given options

### [Capt' Arnav's Chest](https://oldschool.runescape.wiki/w/Capt%27_Arnav%27s_Chest)
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/137f3580-5db4-48bb-a5ba-7569bd3c289e" />
</details>
<details>
  <summary>Video</summary>

https://github.com/user-attachments/assets/d6430888-785e-4d43-90bb-ab98b8b08415
</details>

- Helps unlock the chest by highlighting which rotating dial align to the correct item

### [Kiss the Frog](https://oldschool.runescape.wiki/w/Frog_(Kiss_the_frog))
<details>
  <summary>Screenshot</summary>
  <img width="966" height="700" alt="image" src="https://github.com/user-attachments/assets/6ca312e9-a5ff-4340-a5e0-fc5a36771b67" />
</details>
<details>
  <summary>Video</summary>

_No video at this time_
</details>

- Highlights the correct frog to interact with
