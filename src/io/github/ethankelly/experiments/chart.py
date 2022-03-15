import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import seaborn as sns

data_filepath = "/Users/ethankelly/IdeaProjects/Equations/data.csv"
font = {'family': 'serif', 'color': 'black', 'weight': 'normal', 'size': 24}
num_v = 12

dataframe = pd.read_csv(data_filepath, encoding="ISO-8859-1")

print(dataframe)

plt.figure(figsize=(12, 8))
plt.rcParams['figure.dpi'] = 300
plt.rcParams['savefig.dpi'] = 300

boxplot = sns.boxplot(x="probability", y="result", data=dataframe, showfliers=False)
sns.stripplot(x="probability", y="result", data=dataframe, marker="o", alpha=0.3, color="black", linewidth=1)
boxplot.axes.set_title("Numbers of equations required to describe ER\ngraphs with increasing probabilities", fontsize=22)
boxplot.set_xlabel("Probability", fontsize=14)
boxplot.set_ylabel("Number of Equations", fontsize=14)

plt.show()
