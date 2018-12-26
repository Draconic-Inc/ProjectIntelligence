# ProjectIntelligence
Project Intelligence DOCUMENT ALL THE THINGS!!!!

Welcome to Project Intelligence! aka "PI"

The goal of this project is to create a truly universal documentation mod by leveraging the power of the minecraft community.

So how will this work? Well for starters any mod developer can add documentation for their mod to PI. The problem is mod devs are busy people so most of them wont have time to write documentation for some random documentation mod such as PI and that in my opinion is a major issue with most of the current documentation mods. They need to be directly supported by modders. PI aims to solve this problem by making it possible for anyone in the community to contribute by writing documentation for various mods using the relatively simple built in editor. They can then submit their documentation via a pull request and if it passes review it will be added to the online repository.

Everything PI does is open source and completely transparent. All documentation is stored in its raw form on github. That is then built by a jenkins server and uploaded to a web server controlled by covers1624 where it can be accessed by PI. Everything PI uses comes from this server including all images used in documentation. No requests are made to any servers outside of pi.brandon3055.com. The only possible exception to this is pack documentation which you can read more about below.

 

####Features: (Expect this list to grow as PI develops)

**Highly customizable User Interface**
The entire PI user interface is highly user customizable. PI currently only has 2 default style presets but more will be added in the future. And users have the ability to save their own custom presets.Side note: If you have created a preset that you think would make a nice addition to the default presets feel free to post it on github.

**JEI Integration**
PI has full JEI recipe support meaning any recipe that is supported by JEI can be displayed in PI documentation.

**Content Linking & "Explain This"**
This allows you to link items, blocks or entities to documentation pages. This works with a feature called "Explain This" which allows you to open linked documentation pages by simply pressing a keybind while looking at an object in world or while hovering your mouse over an item in an inventory. 

**Link documentation to Gui's**
This feature currently requires mods to support it (though that may change in the future) This feature allows mod devs to link specific documentation pages to gui's in their mod. Pi will then add an "info" button to supported gui's that will open a mini pi window inside the gui containing the pages linked to the gui.

**Language Localisation Support**

**Built in editor**
The pi doc editor still needs some work but as it is now the editor provides all of the basic functionality required to write and maintain documentation. 

**Support for multiple mod versions**
This is an essential feature when writing documentation for ever changing mods. This feature allows you to update your documentation when a mod changes something without breaking compatibility with previous versions of the mod.

**Pack Doc**
This is a feature added for mod pack developers. It allows you to write documentation identical to mod documentation except it is not bound to a specific mod and it is stored locally in the pack config meaning it can be shipped with the mod pack. Its worth noting that unlike mod documentation i have no control over the hosting of images used in pack documentation. It is up to the pack developer to host these on a site like imgur.com or there own private image hosting server.
