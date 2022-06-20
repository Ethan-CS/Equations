import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import seaborn as sns

data_filepath = "/Users/ethankelly/IdeaProjects/Equations/data-reg-diff.csv"
font = {'family': 'serif', 'color': 'black', 'weight': 'normal', 'size': 24}
num_v = 12

dataframe = pd.read_csv(data_filepath, encoding="ISO-8859-1")

print(dataframe)

plt.figure(figsize=(12, 8))
plt.rcParams['figure.dpi'] = 300
plt.rcParams['savefig.dpi'] = 300

boxplot = sns.barplot(x="probability", y="difference", data=dataframe[dataframe["probability"] >= 00])
# sns.stripplot(x="probability", y="result-reg", data=dataframe[dataframe["probability"] >= 0], marker="o", alpha=0.3,
#               color="blue", linewidth=1)
# sns.stripplot(x="probability", y="result-red", data=dataframe[dataframe["probability"] >= 0], marker="x", alpha=0.3,
#               color="red", linewidth=1)
boxplot.axes.set_title("Reduction in numbers of equations needed for ER\ngraphs after using closures",
                       fontsize=22, weight='bold')
boxplot.set_xlabel("Probability", fontsize=14)
boxplot.set_ylabel("Difference in number of equations", fontsize=14)

plt.show()
