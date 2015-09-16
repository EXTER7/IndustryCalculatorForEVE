# IndustryCalculatorForEVE
Android app that calculates profit from industry in EVE Online.

Before building, data files must be added to the 'src/main/assets' directory.

To add the data files:

- Download/clone the [EVEIndustryData repository](https://github.com/EXTER7/EVEIndustryData)

- Get the needed SDE files (Follow the instructions on 'datadump/sde/README.md').

- Open a terminal/command promt in the 'datadump' directory of the EVEIndustryData repository.

- Enter the command 'python3 datadump.py --no-zip' or './datadump.py --no-zip'

- After the script finishes, copy the contents of the 'eid' directory (not the directory itself) to the 'src/main/assets' directory in this repository.


The directory structure in the end should look like this:

    'src/'
    +-- 'main/'
    |   +-- 'java/'
    |   +-- 'res/'
    |   +-- 'assets/'
    |        +-- blueprint/
    |        +-- icons/
    |        +-- planet/
    |        +-- reaction/
    |        +-- refine/
    |        +-- inventory.tsl
    |        +-- starbases.tsl
    |        +-- starmap.tsl
    |        +-- README.md
    |
    +-- 'build.gradle'
    +-- 'settings.gradle'
    +-- ...

