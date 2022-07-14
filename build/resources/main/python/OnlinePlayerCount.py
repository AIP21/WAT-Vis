from sklearn.model_selection import train_test_split
from sklearn.linear_model import *
import json
import pandas as pd
import sys

data = open(sys.argv[1]).read()
data = json.loads(data)['times']
data = {'times': list(data.keys()), 'players': list(data.values())}
df = pd.DataFrame(data=data)
print(df.head())
X = df[['times']]
y = df['players']
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.20, random_state=1)
lr = LinearRegression()
lr.fit(X_train, y_train)
print(f"Accuracy {lr.score(X_test, y_test) * 100:.2f}%")