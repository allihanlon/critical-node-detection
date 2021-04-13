## Download and install the package
# install.packages("igraph")

## Load package
library(igraph)

# Read adjacency list from file and remove row with number of nodes
adjList = read.delim('ErdosRenyi_n250.txt', header = FALSE, sep = " ")
adjList = adjList[-1,]

# Create an edgeList dataframe for the converted edge list
edgeList = data.frame(Node = numeric(), Neighbor = numeric())

# Convert the adjList dataframe to an edgeList
edgeRow = 1
for (i in 1:nrow(adjList)) {
  # Set up inner loop 
  j = 2
  while (j <= length(adjList) && !is.na(adjList[i, j])) {
    edgeList[edgeRow, 1] = adjList[i,1]
    edgeList[edgeRow, 2] = adjList[i,j]
    edgeRow = edgeRow + 1
    j = j + 1
  }
}
  

# Graph using igraph to visalize the graph
my_graph = graph.data.frame(d = edgeList, directed=FALSE)
plot(my_graph)
