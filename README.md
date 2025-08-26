# Random Event Helper
A RuneLite plugin to help solve random events by displaying solutions for each random event.

## Features
- Highlights the solution for each supported random event

## Supported Random Events
### [Surprise Exam](https://oldschool.runescape.wiki/w/Surprise%20Exam)
- Highlights the correct answer for both the matching cards and next item sequence
  - Implemented via relations and keywords to hopefully support any variation of the questions/answers
    - As a result of using keywords, there could be a chance the solutions are incorrect. If this is the case, please open an issue on the [GitHub repository](https://github.com/Infinitay/Random-Event-Solver/issues?q=sort%3Aupdated-desc+is%3Aissue+is%3Aopen) with a screenshot of the random event question.
    - _Disclaimer, the **relation matching system** was generated with AI, with me fixing the bugs and building upon the keywords._
### [Beekeeper](https://oldschool.runescape.wiki/w/Beekeeper_(Random_Event))
- Displays the correct order to place the different hive piece
### [Pinball](https://oldschool.runescape.wiki/w/Pinball)
- Highlights the correct pillar to tag
  - The current event already tags the correct pillar, but this highlight will make it more obvious
### [Freaky Forester](https://oldschool.runescape.wiki/w/Freaky%20Forester)
- Highlights the correct [pheasant](https://oldschool.runescape.wiki/w/Pheasant) to kill
- No support for highlighting the raw pheasant drop itself
### [Drill Demon](https://oldschool.runescape.wiki/w/Drill%20Demon)
- Highlights the correct exercise mat to step on 
### [Gravedigger](https://oldschool.runescape.wiki/w/Gravedigger)
- Displays the correct coffin solution for all the graves via color-coded highlights
