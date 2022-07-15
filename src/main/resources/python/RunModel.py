import pickle
from OnlinePlayerCount import create_dataset, load_data
import numpy as np

# Load the model
model = pickle.load(open('model.sav', 'rb'))

# Use some data
df = load_data("data.txt")
# print(df[df["times"] == 100])
# import pdb;pdb.set_trace()
dataset = df.values
print(dataset)
dataset.astype('float32')
train_size = int(len(dataset) * 0.67)  # 67% dataset for training
test_size = len(dataset) - train_size  # 33% dataset for testing
train, test = dataset[0:train_size, :], dataset[train_size:len(dataset), :]
X_test, y_test = create_dataset(test, 25)
X_test = np.reshape(X_test, (X_test.shape[0], 1, X_test.shape[1]))

print(model.predict(np.resize(np.array([93]), X_test.shape)))
print(y_test)
