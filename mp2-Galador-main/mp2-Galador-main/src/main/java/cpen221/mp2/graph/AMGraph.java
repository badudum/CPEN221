package cpen221.mp2.graph;

import java.util.*;

public class AMGraph<V extends Vertex, E extends Edge<V>> implements MGraph<V, E> {

    private final List<V> vertexList = new ArrayList<>();
    private final List<E> edgeList = new ArrayList<>();
    private final int[][] matrix;
    private final int maxVertices;

    /**
     * Representation Invariant:
     * maxVertices - maximum number of vertices is greater than 1
     * vertexList - vertexList.size() <= maxVertices
     * vertexList - All vertices are not equal to any other vertex
     * edgeList - edgeList.size() <= (maxVertices(maxVertices - 1)) / 2
     * edgeList - All edges are not equal to any other edge
     * matrix - matrix length = maxVertices
     * matrix - matrix[] length = maxVertices
     * Abstraction Function:
     * Represent a mutable undirected graph without edges going to and from the same vertex.
     */

    /**
     * Create an empty graph with an upper-bound on the number of vertices
     * @param maxVertices is greater than 1
     */
    public AMGraph(int maxVertices) {
        this.matrix = new int[maxVertices][maxVertices];
        this.maxVertices = maxVertices;
    }

    /**
     * Add a vertex to the graph
     *
     * @param v vertex to add
     * @return true if the vertex was added successfully and false otherwise
     */
    public boolean addVertex(V v) {
        if (vertexList.size() < maxVertices && !vertexList.contains(v)) {
            vertexList.add(v);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if a vertex is part of the graph
     *
     * @param v vertex to check in the graph
     * @return true of v is part of the graph and false otherwise
     */
    public boolean vertex(V v) {
        return vertexList.contains(v);
    }

    /**
     * Add an edge of the graph
     *
     * @param e the edge to add to the graph
     * @return true if the edge was successfully added and false otherwise
     */
    public boolean addEdge(E e) {
        int pos1, pos2;
        if (vertexList.contains(e.v1()) && vertexList.contains(e.v2())) {
            pos1 = vertexList.indexOf(e.v1());
            pos2 = vertexList.indexOf(e.v2());
            if (matrix[pos1][pos2] == 0) {
                if (e.length() == 0) {
                    matrix[pos1][pos2] = -1;
                    matrix[pos2][pos1] = -1;
                } else {
                    matrix[pos1][pos2] = e.length();
                    matrix[pos2][pos1] = e.length();
                }
                edgeList.add(e);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an edge is part of the graph
     *
     * @param e the edge to check in the graph
     * @return true if e is an edge in the graph and false otherwise
     */
    public boolean edge(E e) {
        return edgeList.contains(e);
    }

    /**
     * Check if v1-v2 is an edge in the graph
     *
     * @param v1 the first vertex of the edge
     * @param v2 the second vertex of the edge
     * @return true of the v1-v2 edge is part of the graph and false otherwise
     */
    public boolean edge(V v1, V v2) {
        int pos1, pos2;
        if (vertexList.contains(v1) && vertexList.contains(v2)) {
            pos1 = vertexList.indexOf(v1);
            pos2 = vertexList.indexOf(v2);
            return matrix[pos1][pos2] != 0;
        }
        return false;
    }

    /**
     * Determine the length on an edge in the graph
     *
     * Requires edge() must return true before this method can be called
     * @param v1 the first vertex of the edge
     * @param v2 the second vertex of the edge
     * @return the length of the v1-v2 edge if this edge is part of the graph
     */
    public int edgeLength(V v1, V v2) throws IllegalArgumentException {
        int pos1, pos2;
        if (vertexList.contains(v1) && vertexList.contains(v2) && !v1.equals(v2)) {
            pos1 = vertexList.indexOf(v1);
            pos2 = vertexList.indexOf(v2);
            if (matrix[pos1][pos2] != 0) {
                return matrix[pos1][pos2];
            }
        }
        throw new IllegalArgumentException("Edge does not exist between " + v1.name() + " and " + v2.name());
    }

    /**
     * Obtain the sum of the lengths of all edges in the graph
     *
     * @return the sum of the lengths of all edges in the graph
     */
    public int edgeLengthSum() {
        int sum = 0;
        for (E edge: edgeList) {
            sum += edge.length();
        }
        return sum;
    }

    /**
     * Remove an edge from the graph
     *
     * @param e the edge to remove
     * @return true if e was successfully removed and false otherwise
     */
    public boolean remove(E e) {
        int pos1, pos2;
        if (edgeList.contains(e)) {
            pos1 = vertexList.indexOf(e.v1());
            pos2 = vertexList.indexOf(e.v2());
            if (matrix[pos1][pos2] != 0) {
                matrix[pos1][pos2] = 0;
                matrix[pos2][pos1] = 0;
                edgeList.remove(e);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a vertex from the graph
     *
     * @param v the vertex to remove
     * @return true if v was successfully removed and false otherwise
     */
    public boolean remove(V v) {
        int index;
        if (vertexList.contains(v)) {
            index = vertexList.indexOf(v);
            vertexList.remove(index);
            edgeList.removeIf(x -> x.v1().equals(v) || x.v2().equals(v));
            for (int i = index; i < matrix.length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    if (i == matrix.length - 1) {
                        matrix[i][j] = 0;
                    } else {
                        matrix[i][j] = matrix[i + 1][j];
                    }
                }
            }
            for (int i = index; i < matrix.length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    if (i == matrix.length - 1) {
                        matrix[j][i] = 0;
                    } else {
                        matrix[j][i] = matrix[j][i + 1];
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Obtain a set of all vertices in the graph.
     * Access to this set **should not** permit graph mutations.
     *
     * @return a set of all vertices in the graph
     */
    public Set<V> allVertices() {
        Set<V> returnSet = new HashSet<>();
        for (V vertex: vertexList) {
            returnSet.add((V) vertex.clone());
        }
        return returnSet;
    }

    /**
     * Obtain a set of all vertices incident on v.
     * Access to this set **should not** permit graph mutations.
     *
     * @param v the vertex of interest
     * @return all edges incident on v
     */
    public Set<E> allEdges(V v) {
        Set<E> returnSet = new HashSet<>();
        for (E edge: edgeList) {
            if (edge.v1().equals(v) || edge.v2().equals(v)) {
                returnSet.add((E) edge.clone());
            }
        }
        return returnSet;
    }

    /**
     * Obtain a set of all edges in the graph.
     * Access to this set **should not** permit graph mutations.
     *
     * @return all edges in the graph
     */
    public Set<E> allEdges() {
        Set<E> returnSet = new HashSet<>();
        for (E edge: edgeList) {
            returnSet.add((E) edge.clone());
        }
        return returnSet;
    }

    /**
     * Obtain all the neighbours of vertex v.
     * Access to this map **should not** permit graph mutations.
     *
     * @param v is the vertex whose neighbourhood we want.
     * @return a map containing each vertex w that neighbors v and the edge between v and w.
     */
    public Map<V, E> getNeighbours(V v) {
        Map<V, E> returnMap = new HashMap<>();
        for (E x: edgeList) {
            if (x.v1().equals(v) || x.v2().equals(v)) {
                if (x.v1().equals(v)) {
                    returnMap.put((V) x.v2().clone(), (E) x.clone());
                } else {
                    returnMap.put((V) x.v1().clone(), (E) x.clone());
                }
            }
        }
        return returnMap;
    }

    @Override
    public void clear() {
        vertexList.clear();
        edgeList.clear();
        for (int[] row: matrix) {
            Arrays.fill(row, 0);
        }
    }

}
