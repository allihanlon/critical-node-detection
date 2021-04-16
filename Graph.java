import java.util.*;
import java.io.*;
import com.opencsv.CSVWriter;

/*******************************************************************/
/********************************************************************
 * Critical Node Detection Problem
 *******************************************************************/
/*******************************************************************/

public class Graph {
    private int vertexCount;        // total number of vertices in the graph
    private Vertex[] vertexArr;     // array of all vertices in the graph
    private boolean[] VISITED;      // denotes whether a vertex has been visited by Evaluate
    private int[] DELETED;          // tracks the deleted nodes, in order of deletion

    /********************************************************************
     * Graph Constructor
     * ** Converts .txt input to a Graph
     *******************************************************************/
    public Graph(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringTokenizer st;
        String currLine;

        // Read in first line (should always be the number of vertices)
        st = new StringTokenizer(br.readLine(), " " );
        vertexCount = Integer.parseInt(st.nextToken());

        // Initialize array of all nodes and VISITED array (for use in Evaluate)
        vertexArr = new Vertex[vertexCount];
        VISITED = new boolean[vertexCount];
        // Create vertices in the Vertex array
        for (int i = 0; i < vertexCount; i++) {
            vertexArr[i] = new Vertex(i);
        }

        // Use BufferedReader and String Tokenizer to process  input
        while ((currLine = br.readLine()) != null) {
            st = new StringTokenizer(currLine, ": " );
            int currNode = Integer.parseInt(st.nextToken());
            // Add all neighbors using String Tokenizer
            while (st.hasMoreTokens()) {
                int neighbor = Integer.parseInt(st.nextToken());
                vertexArr[currNode].addEdge(vertexArr[neighbor]);
            }
        }
        br.close();
    }


    /********************************************************************
     * Objective Function
     * ** Calculates the objective value based on inputted integer
     *******************************************************************/
    public int f(int N) {
        return (N * (N - 1)) / 2;
    }


    /********************************************************************
     * Algorithm 2: Fast vertex removal for CNDP, from Page 7
     * ** Removes k vertices from the Graph
     *******************************************************************/
    public Graph FastRemoval(int k, Graph G) {
        // Declarations
        Vertex v;                   // the vertex to be added to the priority queue
        Vertex deleted;             // the vertex that was most recently deleted from the graph
        Vertex next;                // the next neighbor of the most recently deleted node that needs to be re-evaluated
        ArrayList<Vertex> adj;      // list of neighbors of the most recently deleted node
        int numAdj;                 // size of adj (number of neighbors of deleted node)
        DELETED = new int[k];       // tracks deleted nodes in order of deletion

        // Create a Comparator to ensure PQ is a Max-PQ
        Comparator<Vertex> comparator = (v1, v2) -> v2.compareTo(v1);
        PriorityQueue<Vertex> PQ = new PriorityQueue(vertexCount, comparator);

        // Selection first node with minimum IMPACT, using first value as root
        v = Evaluate(G, G.vertexArr[0]);
        PQ.add(v);

        // Repeat until k vertices have been deleted
        for (int i = 0; i < k; i++) {
            // Delete the node with the greatest IMPACT on the objective function in the PQ
            deleted = PQ.remove();
            deleted.markDeleted();
            DELETED[i] = deleted.getNodeNum();

            // Reset the VISITED array for next pass with Evaluate
            Arrays.fill(VISITED, false);

            adj = deleted.getEdges();
            numAdj = adj.size();

            // For all unvisited neighbors of v, re-evaluate to ensure IMPACT values are updated
            for (int j = 0; j < numAdj; j++) {
                next = adj.get(j);
                // Only run Evaluate again if the neighbor is not deleted or already visited
                if (!next.checkDeleted() && !VISITED[next.getNodeNum()]) {
                    v = Evaluate(G, next);
                    PQ.add(v);
                }
            }
        }
        return G;
    }


    /********************************************************************
     * Algorithm 3: Evaluate Function, from Appendix A
     * ** Evaluates IMPACT of nodes in given component, return minimum IMPACT node
     *******************************************************************/
    public Vertex Evaluate(Graph G, Vertex root) {
        // Declarations
        Stack<Vertex> S = new Stack();
        int num = 1;                                    // tracks the discovery time of the nodes
        int minNode = root.getNodeNum();                // vertex number with min IMPACT to be returned at end of algorithm
        int minVal = f(vertexCount);                    // starting minimum impact value (largest it could be)
        int rootChildren = 0;                           // tracks the number of children that the root has (used to determine if root is AP)
        int adjCounter;                                 // tracks which adjacent node we have reached
        int DFN[] = new int[vertexCount];               // stores the discovery time of every vertex
        int LOW[] = new int[vertexCount];               // earliest connected vertex (in other words, earliest back edge)
        int PARENT[] = new int[vertexCount];            // stores the parent of each vertex
        boolean CUTPOINT[] = new boolean[vertexCount];  // denotes whether a vertex was found to be a cut point
        boolean COUNTED[] = new boolean[vertexCount];   // denotes whether a vertex's subtrees have been counted towards the objective value
        int CUT_SIZE[] = new int[vertexCount];          // stores the number of new connected components
        int ST_SIZE[] = new int[vertexCount];           // stores the subtree size of each vertex
        int IMPACT[] = new int[vertexCount];            // stores the impact on the objective value of removing each vertex, from Eq. 1

        // Initialize values for root after pushing to Stack
        S.push(root);
        int rootNum = root.getNodeNum();
        VISITED[rootNum] = true;
        DFN[rootNum] = num;
        LOW[rootNum] = DFN[rootNum];
        PARENT[rootNum] = rootNum;
        ST_SIZE[rootNum] = 1;
        IMPACT[rootNum] = 0;
        num += 1;

        // Begin DFS on every vertex
        while (!S.empty()) {
            // DECLARATIONS
            Vertex v = S.peek();                       // Get the next vertex in the Stack
            ArrayList<Vertex> adj = v.getEdges();      // The ArrayList of adjacent vertices (neighbors) of v
            int numAdj = adj.size();                   // The number of adjacent vertices (neighbors) of v

            // Find next unvisited neighbor, y, of v
            // Could I make this more efficient, so its not cycling through more neighbors each time?
            adjCounter = 0;
            Vertex y = adj.get(0);
            while (adjCounter < (numAdj - 1) && (VISITED[y.getNodeNum()] || y.checkDeleted())) {
                adjCounter += 1;
                y = adj.get(adjCounter);
            }

            // If there is an unvisited neighbor then push to Stack, Else backtrace
            if (!VISITED[y.getNodeNum()] && !y.checkDeleted()) {
                // Push and initialize all values
                S.push(y);
                VISITED[y.getNodeNum()] = true;
                DFN[y.getNodeNum()] = num;
                LOW[y.getNodeNum()] = DFN[y.getNodeNum()];
                PARENT[y.getNodeNum()] = v.getNodeNum();
                ST_SIZE[y.getNodeNum()] = 1;
                IMPACT[y.getNodeNum()] = 0;
                num += 1;
                // Track children of the root; When the root has more than one child in DFS tree, it is an AP
                if (PARENT[y.getNodeNum()] == root.getNodeNum() && !y.equals(root)) {
                    rootChildren++;
                }
            } else {
                S.pop();
                // For all neighbors w of v, do the following:
                for (int j = 0; j < numAdj; j++) {
                    Vertex w = adj.get(j);
                    // Verify that the node has not been deleted
                    if (w.checkDeleted()) {
                        continue;
                    }
                    // Begin back tracing
                    int vertex = v.getNodeNum();
                    int neighbor = w.getNodeNum();

                    if (DFN[neighbor] < DFN[vertex] && PARENT[vertex] != neighbor) {
                        LOW[vertex] = Math.min(LOW[vertex], DFN[neighbor]);
                    } else if (PARENT[neighbor] == vertex) {
                        LOW[vertex] = Math.min(LOW[vertex], LOW[neighbor]);
                        if (!COUNTED[neighbor] && (PARENT[vertex] != neighbor || v.equals(root))) {
                            COUNTED[neighbor] = true;
                            ST_SIZE[vertex] = ST_SIZE[vertex] + ST_SIZE[neighbor];
                        }
                        // Mark as AP, update cut size and IMPACT
                        if (LOW[neighbor] >= DFN[vertex] && !v.equals(root)) {
                            CUTPOINT[vertex] = true;
                            CUT_SIZE[vertex] = CUT_SIZE[vertex] + ST_SIZE[neighbor];
                            IMPACT[vertex] = IMPACT[vertex] + f(ST_SIZE[neighbor]);
                        }
                    }

                    // If the root has more than one child, then it is an articulation point
                    if (v.equals(root) && rootChildren > 1) {
                        CUTPOINT[vertex] = true;
                    }
                }
            }
        }

        // Finish calculating IMPACT with ancestors
        COUNTED[root.getNodeNum()] = true;
        for (int i = 0; i < vertexCount; i++) {
            // if deleted node, do not consider or update IMPACT
            if (vertexArr[i].checkDeleted() || !COUNTED[i]) {
                continue;
            }
            // The equation below is (num - 2) rather than (num - 1) because "num" is pre-incremented
            // Essentially, num will always be one greater than the actual number of nodes in the component
            if (CUTPOINT[i]) {
                IMPACT[i] = IMPACT[i] + f(num - 2 - CUT_SIZE[i]);
            } else {
                IMPACT[i] = IMPACT[i] + f(num - 2);
            }
            // Maintain if the minimum thus far
            if (IMPACT[i] < minVal && COUNTED[i]) {
                minNode = i;
                minVal = IMPACT[i];
            }
        }

        vertexArr[minNode].setIMPACT(IMPACT[minNode]);
        return vertexArr[minNode];
    }


    /********************************************************************
     * Output
     * ** Function to output to a CSV file after algorithm completion
     *******************************************************************/
    public void SaveOutput(String path) throws IOException {
        // DECLARATIONS
        FileWriter writer = new FileWriter(path);       // FileWriter to write to CSV file
        int len = DELETED.length;                       // Number of deleted nodes
        int currNode;                                   // Current node to write

        writer.append("Node Number");
        writer.append(",");
        writer.append("IMPACT");
        writer.append("\n");

        // Write all deleted nodes and their IMPACT to a CSV, in order of deletion
        for (int i = 0; i < len; i++) {
            currNode = DELETED[i];
            writer.write(currNode + "");
            writer.write(",");
            writer.write(vertexArr[currNode].getIMPACT() + "");
            writer.write("\n");
        }

        writer.flush();
        writer.close();
    }


    /********************************************************************
     * Debugger
     * ** Can be called from Evaluate, to print IMPACT, DFN, or full Vertex array
     *******************************************************************/
    public void Debugger(int[] arr, String name) {
        // Print array sent to function:
        System.out.print(name);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }


    /********************************************************************
     * Driver
     *******************************************************************/
    public static void main(String args[]) throws IOException {
        Graph G = new Graph("adjList(1).txt");
        G.FastRemoval(3, G);
        G.SaveOutput("output.csv");
    }
}
