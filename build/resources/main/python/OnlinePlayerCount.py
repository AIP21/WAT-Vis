import ast
import pandas as pd
import sys
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from sklearn.preprocessing import MinMaxScaler
from keras.models import Sequential
from keras.layers import Dense, LSTM
from sklearn.metrics import mean_squared_error
import pickle
import logging

logging.basicConfig(level=logging.DEBUG)

def main():
    logging.info("Reading data...")
    df = load_data(sys.argv[1])
    print(df.head())
    plt.style.use('ggplot')
    plt.plot(df)
    plt.show()
    logging.info("Finished reading data!")

    logging.info("Setting seed to 54546 (I'm not quite sure why this one is the best one I've found so far)")
    tf.random.set_seed(54546)
    dataset = df.values
    dataset.astype('float32')
    logging.info("Seed is 54546!")

    logging.info("Normalizing dataset...")
    # Normalize dataset
    scaler = MinMaxScaler(feature_range=(0, 1))
    dataset = scaler.fit_transform(dataset)
    logging.info("Finished normalizing dataset!")

    logging.info("Splitting into testing and training sets...")
    # Split into testing and training sets
    train_size = int(len(dataset) * 0.67)  # 67% dataset for training
    test_size = len(dataset) - train_size  # 33% dataset for testing
    train, test = dataset[0:train_size, :], dataset[train_size:len(dataset), :]
    print(len(train), len(test))
    logging.info("Split into testing and training sets!")


    look_back = 25
    X_train, y_train = create_dataset(train, look_back)
    X_test, y_test = create_dataset(test, look_back)

    # reshape input to be [samples, time steps, features]
    X_train = np.reshape(X_train, (X_train.shape[0], 1, X_train.shape[1]))
    X_test = np.reshape(X_test, (X_test.shape[0], 1, X_test.shape[1]))

    logging.info("Training model (300 epochs)...")
    # Make LSTM model
    model = Sequential()
    model.add(LSTM(4, input_shape=(1, look_back)))
    model.add(Dense(1))
    model.compile(loss='mean_squared_error', optimizer='adam')
    model.fit(X_train, y_train, epochs=300, batch_size=1, verbose=2, shuffle=False)
    logging.info("Finished training model, making predictions...")

    # make predictions
    train_predict = scaler.fit_transform(model.predict(X_train))
    test_predict = scaler.transform(model.predict(X_test))

    # invert predictions
    train_predict = scaler.inverse_transform(train_predict)
    y_train = scaler.inverse_transform([y_train])
    test_predict = scaler.inverse_transform(test_predict)
    y_test = scaler.inverse_transform([y_test])

    # calculate root mean squared error
    train_score = np.sqrt(mean_squared_error(y_train[0], train_predict[:, 0]))
    print('Train Score: %.2f RMSE' % (train_score))
    test_score = np.sqrt(mean_squared_error(y_test[0], test_predict[:, 0]))
    print('Test Score: %.2f RMSE' % (test_score))
    print()

    # Prep train for plotting
    train_predict_plot = np.empty_like(dataset)
    train_predict_plot[:, :] = np.nan
    train_predict_plot[look_back:len(train_predict) + look_back, :] = train_predict

    # Prep test for plotting
    test_predict_plot = np.empty_like(dataset)
    test_predict_plot[:, :] = np.nan
    test_predict_plot[len(train_predict) + (look_back * 2) + 1:len(dataset) - 1, :] = test_predict

    # plot predictions
    plt.plot(scaler.inverse_transform(dataset))
    plt.plot(train_predict_plot)
    plt.plot(test_predict_plot)
    plt.show()
    logging.info("Done!")
    print(model.predict(np.resize(np.array([X_test[1]]), X_test.shape)))

    # Save the model to a .sav file
    f = 'model.sav'
    pickle.dump(model, open(f, 'wb'))
    logging.info("Model saved to file \"model.sav\".")

def load_data(file):
    data = open(file).read()
    data = ast.literal_eval(data)["periods"]
    data = {'times': list(map(int, ["".join(i.split("-")) for i in list(data.keys())])),
            'players': list(map(int, data.values()))}
    df = pd.DataFrame(data=data)
    return df

# make a dataset matrix
def create_dataset(dataset, look_back=1):
    X, y = [], []
    for i in range(len(dataset) - look_back - 1):
        a = dataset[i:(i + look_back), 0]
        X.append(a)
        y.append(dataset[i + look_back, 0])
    return np.array(X), np.array(y)

if __name__ == "__main__":
    main()