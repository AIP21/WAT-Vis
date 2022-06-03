# TrackerDecoderApp
An app version of my player tracker decoder. It is an interactive and super configurable tool to visualize, analyze, and export the data created by my Player Tracker Mod.

Links to the Minecraft mod:
[Modrinth](https://modrinth.com/mod/wat) or [Github](https://github.com/AIP21/WAT-mod)

This can ONLY BE USED with the format of data produced by my Player Tracker mod for minecraft. The purpose of this app is to be able to view and analyze the data collected from the mod.
If you'd like to, you can create your own tool to generate the data, see the Data Format section below for more details on the structure of the the logs.

I made this (and the minecraft mod) as a fun project to learn more about Java.

TLDR: It's like nocom but serverside.

# Setup and running the program
The program *should* do the following steps by itself, but its always better doing it yourself just in case. (My code tends to be unpredictable)
1. Put the .jar file in an empty folder
2. Create these folders "inputs", "outputs", and "worldImages"

Now you are good to go, just run the .jar file and import your data using the button on the top left!

# Features
1. Display all the data that you imported.
2. Display a desired date range or a single point in time.
3. Animate the time to see players moving around as time passes.
4. See a list of all the players in the data and either change their marker color or disable them entirely.
5. Select individual points (By clicking on points, although it's a bit finicky) and see their information. This includes the date, player name, and position.
6. Represent the data in four ways: Pixels, Dots, Lines, or a Heatmap.
7. Option to fade out data based on its age.
8. Set a threshold for line lengths in order to prevent long lines that result from interdimensional travel or deaths.
9. Option to show lines hidden by the threshold (To view nether travel or deaths).
11. Import a background image of the data's world so that you can see where players are.
12. **Extensive** configuration for almost everything
13. Ability to export a view of all the data in one image. (Still kinda buggy, gotta fix it)
14. And some other stuff I forgot to write here.

# Data Format
The log data is formatted in a specific way, and can only be read in that way (I might change that in the future). Here is an example line of data:
> 21:25:29; Anip24; (-98, 65, -1460);

Each "chunk" of data is separated by a semicolon and a space ("; ").
The first chunk of data, 21:25:29, is the time of the log, formatted in "HH:mm:ss" (hours, minutes, seconds).
The second chunk of data, Anip24, is the name of the player whom this log is of, it is the player name.
The third chunk of data, (-98, 65, -1460), is the block position of the player, it is an integer 3-dimensional vector, formatted in (x, y, z). It must have parenthesis and must be in the order (x, y, z).
The entry ends in a semicolon and a line break (new line).


A log file is created every day, for every dimension in the world (if on a client, a new set of files is created for every server or world you join). Each log file contains every entry for every player from that day and dimension (and if on a client, the server/world), in chronological order from oldest to newest.
The naming of a log file is also important, it is also formatted in a specific way. They are formatted as a .txt file. Here is an example file name:
> MyServer-overworld-log-2022-04-23.txt

Each part is separated by a hyphen ("-").
The first part is the source name, such as the world/server name.
The second part is the data dimension, it can be either "overworld", "nether", or "the-end".
The third part is just indicating that the file is a log file, it is always "log" (You can change it if you want to though).
The fourth part is the date, separated by hyphens. It is formatted in "YYYY-MM-dd" (year, month, day of month).
The file type should be a .txt file.

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
![image](https://user-images.githubusercontent.com/44927160/171914953-4ca2684b-328a-4a4e-928e-016d6bf0b025.png)
