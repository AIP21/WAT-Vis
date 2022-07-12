import pandas as pd
from warnings import filterwarnings
import json

# Ignore all warnings
filterwarnings("ignore")

dfs = []
players = []
files = open("data.txt").read().replace("]", "").replace("[", "").replace("\n", "").split(", ")
print("Reading through input files")
ppd = {}
player_locs = {}
# Add new files to list of dataframes
for f in files:
    df = pd.read_csv(f, sep='; ', header=0, names=["Time", "User", "Pos"])
    date = "-".join(f.split("/")[-1].split("-")[3:]).replace(".txt", "")
    df['Pos'].replace(";", "")
    ppd[date] = len(df['User'].unique())
    for user in df['User'].unique():
        if user not in players:
            players.append(user)
        player_locs[user] = {}
    for i in range(len(df['User'])):
        try:
            player_locs[df['User'][i]][df['Pos'][i]] += 1
        except:
            player_locs[df['User'][i]][df['Pos'][i]] = 1
    df['Time'] = date + " " + df['Time']
    dfs.append(df)
print("Finished reading data!")



# class PosTime:
#     """Represents a player's position at a certain time"""
#
#     def __init__(self, pos, time):
#         self.pos = pos
#         self.time = time
#
#     def __str__(self):
#         return f"{self.pos} at {self.time}"
#
#
# for df in dfs:
#     df = df.reset_index()
#     for user in df['User'].unique():
#         players[user] = []
#     for index, row in df.iterrows():
#         players[row['User']].append(PosTime(row['Pos'], row['Time']))
# for key in list(players.keys()):
#     for postime in players[key]:
#         print(postime.__str__())

total_players = len(players)
avgppd = round(sum(ppd.values()) / len(ppd.keys()), 2)
print(avgppd)
output = open("playerdata.json", "w", encoding='utf-8')
data = {
    "total": total_players,
    "avg-ppd": avgppd,
    "ppd": ppd,
    "player-locs": player_locs,
}
json.dump(data, output, ensure_ascii=False, indent=4)
output.close()