# IndustryCalculatorForEVE
Android app that calculates profit from industry in EVE Online.

[Google Play Store Page](https://play.google.com/store/apps/details?id=com.exter.eveindcalc)

To update the EVE data:

- Download/clone the [EVEIndustryData repository](https://github.com/EXTER7/EVEIndustryData)

- Get the needed SDE files (Follow the instructions on 'datadump/sde/README.md').

- Open a terminal/command promt in the 'datadump' directory of the EVEIndustryData repository.

- Excecute the command 'python3 datadump.py' or './datadump.py'

- After the script finishes, copy the 'eid.zip' file it created to the root directory of this project replacing the existing 'eid.zip'.

When making any change to the EVE data the value of the 'DATABASE_VERSION' field in the 'src/main/java/com/exter/eveindcalc/EICDatabaseHelper.java' class must be incremented to make the app refresh the databases after updating.

