# Call Of MineCraft: Zombies
COMZ, is a Bukkit plugin that adds the Zombies minigame from the Call Of Duty© Franchise

BukkitDev page: https://www.curseforge.com/minecraft/bukkit-plugins/call-duty-zombies


### Contributing:
For those interested in contributing simply pull down the master branch, add your changes and make a PR! I'm not overly
stringent on formatting, just try and keep the same style that you see in the code, or when in doubt just ask!

This is a
gradle based project, so to make your life easier I recommend importing the project as such in your IDE. To build the
plugin, simple run `gradlew build` on the root folder and then the plugin jar should be found in `Core/build/libs` when
the build completes.

All core code that you will ikely need to edit will be located inside the `Core` module, but version compatibility code
can be found in the NMS Support folder and then the corresponding version module.