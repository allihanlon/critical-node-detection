import java.util.*;
import java.io.*;

/*******************************************************************/
/********************************************************************
 * Critical Node Detection Problem
 *******************************************************************/
/*******************************************************************/

public class Graph {
    private int vertexCount;        // total number of vertices in the graph
    private Vertex[] vertexArr;     // array of all vertices in the graph
    private boolean[] VISITED;      // denotes whether a vertex has been visited by Evaluate


    /********************************************************************
     * Graph Constructor
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

        // Use BufferedReader and String Tokenizer for input
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
     *******************************************************************/
    public int f(int N) {
        return (N * (N - 1)) / 2;
    }


    /********************************************************************
     * Algorithm 2: Fast vertex removal for CNDP, from Page 7
     *******************************************************************/
    public Graph FastRemoval(int k, Graph G) {
        Vertex deleted;
        Comparator<Vertex> comparator = new Comparator<Vertex>() {
            @Override
            public int compare(Vertex v1, Vertex v2) {
                return v2.compareTo(v1);
            }
        };
        PriorityQueue<Vertex> PQ = new PriorityQueue(vertexCount, comparator);

        // get the vertex with the greatest IMPACT first! Using the first vertex as the root!
        Vertex v = Evaluate(G, G.vertexArr[0]);
        PQ.add(v);

        // repeat until k vertices have been deleted
        for (int i = 0; i < k; i++) {
            // delete the node with the greatest IMPACT on the objective function
            deleted = PQ.remove();
            deleted.markDeleted();

            // now that node has been removed, reset the VISITED array for next pass with Evaluate
            Arrays.fill(VISITED, false);

            // now need to re-Evaluate all neighbors of the deleted node to ensure IMPACT values are updated
            ArrayList<Vertex> adj = deleted.getEdges();
            int size = adj.size();
            Vertex next;

            // for all unvisited neighbors of v, re-run Evaluate
            for (int j = 0; j < size; j++) {
                next = adj.get(j);
                // only run evaluate again if the neighbor is not deleted or visited
                if (!next.checkDeleted() && !VISITED[next.getNodeNum()]) {
                    v = Evaluate(G, next);
                    // v only equals null if there are no cut points in that component
                    PQ.add(v);
                }
            }
        }

        return G;
    }


    /********************************************************************
     * Algorithm 3: Evaluate Function, from Appendix A
     *******************************************************************/
    public Vertex Evaluate(Graph G, Vertex root) {
        // Declarations
        Stack<Vertex> S = new Stack();
        int num = 1;                                    // tracks the discovery time of the nodes
        int minNode = root.getNodeNum();                // vertex number with min IMPACT to be returned at end of algorithm
        int minVal = f(vertexCount);                    // starting minimum impact value (largest it could be)
        int rootChildren = 0;                           // tracks the number of children that the root has (used to determine if root is AP)
        int DFN[] = new int[vertexCount];               // stores the discovery time of every vertex
        int LOW[] = new int[vertexCount];               // earliest connected vertex (in other words, earliest back edge)
        int PARENT[] = new int[vertexCount];            // stores the parent of each vertex
        boolean CUTPOINT[] = new boolean[vertexCount];  // denotes whether a vertex was found to be a cut point
        boolean COUNTED[] = new boolean[vertexCount];   // denotes whether a vertex's subtrees have been counted towards the objective value
        int CUT_SIZE[] = new int[vertexCount];          // stores the number of new connected components
        int ST_SIZE[] = new int[vertexCount];           // stores the subtree size of each vertex
        int IMPACT[] = new int[vertexCount];            // stores the impact on the objective value of removing each vertex, from Eq. 1

        // Begin EVALUATE algorithm
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
            // Could I make this more efficient so its not cycling through more neighbors each time? Probably.
            int adjCounter = 0;
            Vertex y = adj.get(0);
            while (adjCounter < (numAdj - 1) && (VISITED[y.getNodeNum()] || y.checkDeleted())) {
                adjCounter += 1;
                y = adj.get(adjCounter);
            }

            // If there is an unvisited neighbor, then use it! If not, go into the else statement for back-tracing
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
                // If the parent of the node is the root, then we increment the number of children the root has
                // When the root has more than one child in the DFS tree, it is an AP
                if (PARENT[y.getNodeNum()] == root.getNodeNum() && !y.equals(root)) {
                    rootChildren++;
                }
            } else {
                S.pop();
                // For all neighbors w of v, do the following:
                for (int j = 0; j < numAdj; j++) {
                    // get w and verify that it has not be deleted!
                    Vertex w = adj.get(j);
                    if (w.checkDeleted()) {
                        continue;
                    }
                    // Start back tracing :-)
                    int vertex = v.getNodeNum();
                    int neighbor = w.getNodeNum();

                    if (DFN[neighbor] < DFN[vertex] && PARENT[vertex] != neighbor) {
                        LOW[vertex] = Math.min(LOW[vertex], DFN[neighbor]);
                        // This constraint makes sure that ONLY true back edges are included
                    } else if (PARENT[neighbor] == vertex) {
                        LOW[vertex] = Math.min(LOW[vertex], LOW[neighbor]);
                        // Update subtree size
                        if (!COUNTED[neighbor] && (PARENT[vertex] != neighbor || v.equals(root))) {
                            COUNTED[neighbor] = true;
                            ST_SIZE[vertex] = ST_SIZE[vertex] + ST_SIZE[neighbor];
                        }
                        // Mark as articulation point and update cut size
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

        // finish calculating IMPACT -- Should incorporate this back into above loops after verifying correctness
        COUNTED[root.getNodeNum()] = true;
        for (int i = 0; i < vertexCount; i++) {
            // if deleted node, do not update IMPACT or let it go towards min
            if (vertexArr[i].checkDeleted() || !COUNTED[i]) {
                continue;
            }
            // otherwise go ahead and update impact!
            if (CUTPOINT[i]) {
                IMPACT[i] = IMPACT[i] + f(num - 2 - CUT_SIZE[i]);
            } else {
                IMPACT[i] = IMPACT[i] + f(num - 2);
            }
            // maintain if the minimum thus far
            if (IMPACT[i] < minVal && COUNTED[i]) {
                minNode = i;
                minVal = IMPACT[i];
            }
        }

        //////////////////////////
        // DEBUGGING PURPOSES ONLY
        //////////////////////////
        // Print cut points
        System.out.print("Cut Points: ");
        for (int i = 0; i < vertexCount; i++) {
            if (CUTPOINT[i] == true) {
                System.out.print(i + " ");
            }
        }
        System.out.println();

        /*
        // Print low array
        System.out.print("Low Array: ");
        for (int i = 0; i < LOW.length; i++) {
            System.out.print(LOW[i] + " ");
        }

        System.out.println();
         */

        // Print DFN array
        System.out.print("DFN Array: ");
        for (int i = 0; i < DFN.length; i++) {
            System.out.print(DFN[i] + " ");
        }

        System.out.println();

        // Print IMPACT array
        System.out.print("IMPACT Array: ");
        for (int i = 0; i < IMPACT.length; i++) {
            System.out.print(IMPACT[i] + " ");
        }

        System.out.println();
        System.out.println("Chosen IMPACT: " + minNode);

        System.out.println();

        //////////////////////////
        // END DEBUGGING PURPOSES ONLY
        //////////////////////////

        vertexArr[minNode].setIMPACT(IMPACT[minNode]);
        return vertexArr[minNode];
    }


    /********************************************************************
     * Driver
     *******************************************************************/
    public static void main(String args[]) throws IOException {
        Graph G = new Graph("adjList(1).txt");
        G.FastRemoval(3, G);

        // print which nodes were deleted
        for (int i = 0; i < G.vertexCount; i++) {
            if (G.vertexArr[i].checkDeleted()){
                System.out.print(i + " ");
            }
        }
    }
}
