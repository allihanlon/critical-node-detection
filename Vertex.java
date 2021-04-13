import java.util.*;

public class Vertex implements Comparable<Vertex> {
    private int nodeNum;                                // Node number, and index of Graph's Vertex array
    private int IMPACT;                                 // IMPACT if removed from its component/graph
    private boolean deleted;                            // Removed from graph marker
    private ArrayList<Vertex> adj = new ArrayList<>();  // List of adjacent vertices

    /********************************************************************
     * Constructor & Getters
     * ******************************************************************/
    public Vertex (int nodeNum) {
        this.nodeNum = nodeNum;
        deleted = false;
    }

    public void setIMPACT(int IMPACT) { this.IMPACT = IMPACT; }

    public int getNodeNum() { return nodeNum; }

    public int getIMPACT() { return IMPACT; }

    public ArrayList getEdges(){
        return adj;
    }

    public boolean checkDeleted () { return deleted; }


    /********************************************************************
     * Add edges; Mark node as deleted
     * *******************************************************************/
    public void addEdge(Vertex v) {
        adj.add(v);
    }

    public void markDeleted () {
        deleted = true;
    }


    /********************************************************************
     * Implement Comparable Interface
     * *******************************************************************/
    @Override
    public int compareTo(Vertex v) {
        if (this.getIMPACT() < v.getIMPACT()) {
            return -1;
        } else if (this.getIMPACT() == v.getIMPACT()) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        Vertex v = (Vertex) o;
        if (v.getNodeNum() == this.getNodeNum()) {
            return true;
        }
        return false;
    }
}
