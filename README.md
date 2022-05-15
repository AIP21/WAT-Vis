# TrackerDecoderApp
An app version of my player tracker decoder. It is an interactive and super configurable tool to visualize, analyze, and export the data created by my Player Tracker Mod.
Links to the Minecraft mod:
[The Minecraft mod on Modrinth](https://modrinth.com/mod/wat) or [The Minecraft mod on Github](https://github.com/AIP21/PlayerTracker)

This can ONLY BE USED with my Player Tracker mod for minecraft. The purpose of this app is to be able to view and analyze the data collected from the mod.
The mod is also in the releases page.

I made this (and the minecraft mod) as a fun project to learn more about Java.

# Setup and running the program
The program *should* do the following steps by itself, but its always better doing it yourself just in case. (My code tends to be unpredictable)
1. Put the .jar file in an empty folder
2. Create these folders "inputs", "outputs", and "worldImages"

Now you are good to go, just run the .jar file and import your data using the button on the top left!

# Features
1. Display all the data that you imported.
2. Display a desired date range or single point in time.
3. Animate the time to see players moving around as time passes.
4. See a list of all the players in the data and either change their marker color or disable them entirely.
5. Select individual points (By clicking on points, although it's a bit finicky) and see their information. This included the date, player name, and position.
6. Represent the data in four ways: Pixels, Dots, Lines, or a Heatmap.
7. Option to fade out data based on its age.
8. Set a threshold for line lengths in order to prevent long lines that result from interdimensional travel or deaths.
9. Option to show lines hidden by the threshold (To view nether travel or deaths).
11. Import a background image of the data's world so that you can see where players are.
12. **Extensive** configuration for almost everything
13. Ability to export a view of all the data in one image. (Still kinda buggy, gotta fix it)
14. And some other stuff I forgot to write here.

# Images
Here are some screenshots from the program, using made-up data and data from my Minecraft server.

**Large-scale overview:**
![image](https://user-images.githubusercontent.com/44927160/168449122-76f73826-857d-44f6-9839-eea1f9e24066.png)

**Close-up lines:**
![image](https://user-images.githubusercontent.com/44927160/168449136-b182f879-7db2-4e74-983b-37de249d4fad.png)

**Close-up dots:**
![image](https://user-images.githubusercontent.com/44927160/168450319-e88ecefb-bb34-4392-8ca2-c36ce918d4df.png)

**Heatmap:**
![image](https://user-images.githubusercontent.com/44927160/168449151-4151b751-c39a-48e8-b82d-cc6e78c7d8eb.png)

**Using a background world image:**
![image](https://user-images.githubusercontent.com/44927160/168450297-2415fe3d-f164-484f-827b-37ea82d28cd3.png)

**Exported image:**
![image](https://user-images.githubusercontent.com/44927160/168449213-2c6daaca-a47b-45af-bd96-4a779d8fa759.png)
