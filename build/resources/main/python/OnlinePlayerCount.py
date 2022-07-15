from sklearn.model_selection import train_test_split
from sklearn.linear_model import *
import json
import ast
import pandas as pd
import sys
import numpy as np

data = open(sys.argv[1]).read()
data = ast.literal_eval(data)["periods"]
data = {'times': list(map(int, ["".join(i.split("-")) for i in list(data.keys())])), 'players': list(map(int, data.values()))}
df = pd.DataFrame(data=data)
print(df.head())
X = df[['times']]
y = df['players']
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1, random_state=42)
lr = BayesianRidge()
lr.fit(X_train, y_train)
# import pdb;pdb.set_trace()
predict = lr.predict(X_test)
y_test_arr = y_test.to_numpy()
error = []
for i in range(len(X_test)):
    print(abs(predict[i] - y_test_arr[i]) / y_test_arr[i])
    error.append(abs(predict[i] - y_test_arr[i]) / y_test_arr[i])
print(predict, y_test_arr)
print(f"Average % Error: {(sum(error) / len(error)) * 100:.2f}%")
print(f"Accuracy: {lr.score(X_test, y_test) * 100:.2f}%")