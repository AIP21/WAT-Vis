import pandas as pd
from warnings import filterwarnings

# Ignore all warnings
filterwarnings("ignore")

dfs = []
players = dict()
files = open("data.txt").read().replace("]", "").replace("[", "").replace("\n", "").split(", ")
print("Reading through input files")
# Add new files to list of dataframes
for f in files:
    df = pd.read_csv(f, sep='; ', header=0, names=["Time", "User", "Pos"])
    date = "-".join(f.split("/")[-1].split("-")[3:]).replace(".txt", "")
    df['Time'] = date + " " + df['Time']
    dfs.append(df)
print("Finished reading data!\nAdding data to list... (this might take a moment)")



class PosTime:
    """Represents a player's position at a certain time"""

    def __init__(self, pos, time):
        self.pos = pos
        self.time = time

    def __str__(self):
        return f"{self.pos} at {self.time}"


for df in dfs:
    df = df.reset_index()
    for user in df['User'].unique():
        players[user] = []
    for index, row in df.iterrows():
        players[row['User']].append(PosTime(row['Pos'], row['Time']))
for key in list(players.keys()):
    for postime in players[key]:
        print(postime.__str__())

total_players = len(list(players.keys()))

# TODO: Get total number of players per day

days = {}
for key in list(players.keys()):
    counted = []
    for postime in players[key]:
        if postime.time.split(" ")[0] not in counted:
            try:
                days[postime.time.split(" ")[0]] += 1
            except:
                days[postime.time.split(" ")[0]] = 1
            counted.append(postime.time.split(" ")[0])
print(days)
print(total_players)

# Please don't ask why I'm writing to a json file like this, it's 12:28 AM
# output = open("output.txt", "w")

