package cpen221.mp2.graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a graph with vertices of type V.
 *
 * @param <V> represents a vertex type
 */
public class Graph<V extends Vertex, E extends Edge<V>> implements ImGraph<V, E>, MGraph<V, E> {

    /**
     * Representation Invariant:
     * mGraph - the representation invariant holds
     * cycle_check - the value is not null;
     *
     * Abstraction Function:
     * Represents an undirected graph using an mGraph object.
     */
    private final MGraph<V, E> mGraph;
    private Boolean cycleCheck = false;
    public Graph() {
        this.mGraph = new ALGraph<>();
    }

    /**
     * Find the edge that connects two vertices if such an edge exists.
     * This method should not permit graph mutations.
     *
     * Requires edge() must return true before this method can be called
     * @param v1 one end of the edge
     * @param v2 the other end of the edge
     * @return the edge connecting v1 and v2
     */
    @Override
    public E getEdge(V v1, V v2) throws IllegalArgumentException {
        if (mGraph.vertex(v1) && mGraph.vertex(v2)) {
            Set<E> set1 = mGraph.allEdges(v1);
            Set<E> set2 = mGraph.allEdges(v2);
            for (E edge1: set1) {
                for (E edge2: set2) {
                    if (edge1.equals(edge2)) {
                        return (E) edge1.clone();
                    }
                }
            }
        }
        throw new IllegalArgumentException("Edge does not exist between " + v1.name() + " and " + v2.name());
    }

    /**
     * Compute the shortest path from source to sink
     *
     * @param source the start vertex
     * @param sink   the end vertex
     * @return the vertices, in order, on the shortest path from source to sink (both end points are part of the list)
     *         if there is no path between the given vertices returns an empty list implying infinite length
     */
    @Override
    public List<V> shortestPath(V source, V sink) throws IllegalArgumentException {
        if (!mGraph.vertex(source) || !mGraph.vertex(sink)) {
            return new ArrayList<>();
        }
        if (source.equals(sink)) {
            List<V> returnList = new ArrayList<>();
            returnList.add(source);
            return returnList;
        }
        Map<Integer, List<V>> minDist = new HashMap<>();
        Set<E> temp = allEdges(source);
        V lookAt, from;
        for (E e : temp) {
            if (e.v2().equals(source)) {
                lookAt = e.v1();
                from = e.v2();
            } else {
                lookAt = e.v2();
                from = e.v1();
            }
            List<V> toVertex = new ArrayList<>();
            toVertex.add(from);
            toVertex.add(lookAt);
            path(minDist, lookAt, toVertex, sink);
        }
        if (minDist.size() == 0) {
            return new ArrayList<>();
        }
        List<V> returnList = new ArrayList<>();
        for (V vertex: minDist.get(Collections.min(minDist.keySet()))) {
            returnList.add((V) vertex.clone());
        }
        return returnList;
    }

    /**
     * A recursive support method to help mutate a map of distance and list of vertices
     * given from the main method shortestPath.
     *
     * @param distList the map of all valid paths toward the target vertex
     * @param v        the current vertex we want to examine
     * @param toVertex the vertices in which we have already traversed
     * @param target   the destination vertex which we want to end up on
     */
    private void path(Map<Integer, List<V>> distList, V v, List<V> toVertex, V target) {
        if (v.equals(target)) {
            distList.put(pathLength(toVertex), toVertex);
        } else {
            Set<E> temp = allEdges(v);
            V lookAt;
            for (E e : temp) {
                if (e.v2().equals(v)) {
                    lookAt = e.v1();
                } else {
                    lookAt = e.v2();
                }
                if (!toVertex.contains(lookAt)) {
                    List<V> tempToVertex = new ArrayList<>(toVertex);
                    tempToVertex.add(lookAt);
                    path(distList, lookAt, tempToVertex, target);
                }

            }
        }
    }

    /**
     * Compute the length of a given path.
     *
     * @param path indicates the vertices on the given path
     * @return the length of path, an empty path implies an infinite length of Integer.MAX_VALUE
     */
    @Override
    public int pathLength(List<V> path) throws IllegalArgumentException {
        if (path.size() == 0) {
            return Integer.MAX_VALUE;
        }
        int length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            try {
                length += mGraph.edgeLength(path.get(i), path.get(i + 1));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Path does not exist: no edge between "
                    + path.get(i).name() + " and " + path.get(i + 1).name());
            }
        }
        return length;
    }

    /**
     * Obtain all vertices w that are no more than a <em>path distance</em> of range from v.
     *
     * @param v     the vertex to start the search from.
     * @param range the radius of the search.
     * @return a map where the keys are the vertices in the neighbourhood of v,
     * and the value for key w is the last edge on the shortest path
     * from v to w.
     */
    @Override
    public Map<V, E> getNeighbours(V v, int range) throws IllegalArgumentException {
        if (!mGraph.vertex(v)) {
            throw new IllegalArgumentException("Vertex" + v.name() + "does not exist in this graph");
        }
        Map<V, E> neighborMap = new HashMap<>();
        Set<V> seenSet = new HashSet<>();
        seenSet.add(v);
        int end;
        Set<Map.Entry<V, E>> currSet = new HashSet<>(mGraph.getNeighbours(v).entrySet());
        Set<Map.Entry<V, E>> nextSet = new HashSet<>();
        do  {
            end = 0;
            for (Map.Entry<V, E> entry: currSet) {
                if (this.pathLength(this.shortestPath(v, entry.getKey()))
                        <= range && !seenSet.contains(entry.getKey())) {
                    neighborMap.put((V) entry.getKey().clone(), (E) entry.getValue().clone());
                    nextSet.addAll(mGraph.getNeighbours(entry.getKey()).entrySet());
                    seenSet.add(entry.getKey());
                    end++;
                }
            }
            currSet.clear();
            for (Map.Entry<V, E> entry: nextSet) {
                if (!seenSet.contains(entry.getKey())) {
                    currSet.add(entry);
                }
            }
            nextSet.clear();
        } while (end > 0);
        return neighborMap;
    }

    /**
     * Return a set with k connected components of the graph.
     *
     * <ul>
     * <li>When k = 1, the method returns one graph in the set, and that graph
     * represents the minimum spanning tree of the graph.
     * See: <a href="https://en.wikipedia.org/wiki/Minimum_spanning_tree">...</a></li>
     *
     * <li>When k = n, where n is the number of vertices in the graph, then
     * the method returns a set of n graphs, and each graph contains a
     * unique vertex and no edge.</li>
     *
     * <li>When k is in [2, n-1], the method partitions the graph into sub-graphs
     * such that for any two vertices V_i and V_j, if vertex V_i is in subgraph
     * G_a and vertex V_j is in subgraph G_b (a != b), and there is an edge
     * between V_i and V_j then there must exist some vertex V_k in G_a such
     * that the length of the edge between V_i and V_k is at most the length
     * of the edge between V_i and V_j.</li>
     * </ul>
     *
     * @param k number of connected components to be returned
     * @return a set of graph partitions such that a vertex in one partition
     * is no closer to a vertex in a different partition than it is to a vertex
     * in its own partition.
     */
    @Override
    public Set<ImGraph<V, E>> minimumSpanningComponents(int k) {
        Set<ImGraph<V, E>> minList = new HashSet<>();
        Graph<V, E> min = new Graph<>();
        this.mGraph.allVertices().forEach(min.mGraph::addVertex);
        List<E> edge = this.mGraph.allEdges().stream().sorted(E.byWeightOrder()).collect(Collectors.toList());
        int numEdge = this.mGraph.allVertices().size() - k;
        if (k == 1) {
            for (E x : edge) {
                min.mGraph.addEdge(x);
                isCycle(x.v1(), x.v2(), x.v1(), min);
                if (!cycleCheck) {
                    min.mGraph.addEdge(x);
                } else {
                    min.mGraph.remove(x);
                    cycleCheck = false;
                }
            }
            minList.add(min);
        } else if (k == this.mGraph.allVertices().size()) {
            for (V x : this.mGraph.allVertices()) {
                Graph<V, E> temp = new Graph<>();
                temp.addVertex(x);
                minList.add(temp);
            }
        } else {
            Set<V> vertexAdded = new HashSet<>();
            Set<E> edgeAdded = new HashSet<>();
            for (E x : edge) {
                if (edgeAdded.size() == numEdge) {
                    break;
                }
                min.mGraph.addEdge(x);
                isCycle(x.v1(), x.v2(), x.v1(), min);
                if (!cycleCheck) {
                    min.mGraph.remove(x);
                    if (min.mGraph.allEdges().isEmpty()) {
                        min.mGraph.addEdge(x);
                        edgeAdded.add(x);
                        vertexAdded.add(x.v1());
                        vertexAdded.add(x.v2());
                        continue;
                    }
                    boolean test = potential(numEdge, x, edgeAdded, edge);
                    if (test) {
                        min.mGraph.addEdge(x);
                        edgeAdded.add(x);
                        vertexAdded.add(x.v1());
                        vertexAdded.add(x.v2());
                    } else {
                        Graph<V, E> temp = new Graph<>();
                        temp.addVertex(x.v1());
                        temp.addVertex(x.v2());
                        vertexAdded.add(x.v1());
                        vertexAdded.add(x.v2());
                        temp.addEdge(x);
                        edgeAdded.add(x);
                        minList.add(temp);
                        min.remove(x.v1());
                        min.remove(x.v2());
                        break;
                    }
                } else {
                    min.mGraph.remove(x);
                    cycleCheck = false;
                }
            }
            for (V x : this.mGraph.allVertices()) {
                if (!vertexAdded.contains(x)) {
                    Graph<V, E> temp = new Graph<>();
                    temp.addVertex(x);
                    minList.add(temp);
                    min.mGraph.remove(x);
                }
            }
            minList.add(min);
        }
        return minList;
    }

    /**
     * Check whether the graph forms a cycle updating the cycle_check global variable true if
     * a cycle is identified within the graph @min
     *
     * @param start the vertex to start the search from
     * @param end the vertex to check each if it equals the start
     * @param last the last vertex that is checked
     * @param min the graph to check
     *
     */
    private void isCycle(V start, V end, V last, Graph<V, E> min) {
        if (end.equals(start)) {
            cycleCheck = true;
        } else {
            Set<E> temp = min.allEdges(end);
            for (E e : temp) {
                if (!(e.v2().equals(last))) {
                    if (!(e.v1().equals(last))) {
                        isCycle(start, e.v2(), end, min);
                        isCycle(start, e.v1(), end, min);
                    }
                }
            }
        }
    }

    /**
     * Check whether the current edge (@edge) is connected to any other vertices or the next
     * # (numEdge) of edges.
     *
     * @param numEdge number of edges to be added
     * @param edge the edge that will be added to the graph
     * @param edgeAdded edges already added to the graph
     * @param sortedEdges list of sorted edges added to the original graph
     * @return a boolean indicating true if it connects to other edges or false if it is its own subgroup
     */
    private boolean potential(int numEdge, E edge, Set<E> edgeAdded, List<E> sortedEdges) {
        int remainder = numEdge - edgeAdded.size() - 1;
        int startIndex = edgeAdded.size() + 1;
        List<E> temp = sortedEdges.stream().skip(startIndex).limit(remainder).toList();
        for (E x : edgeAdded) {
            if (edge.v1().equals(x.v1()) || edge.v2().equals(x.v2()) || edge.v2().equals(x.v1()) || edge.v2().equals(x.v2())) {
                return true;
            }
        }
        for (E x : temp) {
            if (edge.v1().equals(x.v1()) || edge.v2().equals(x.v2()) || edge.v2().equals(x.v1()) || edge.v2().equals(x.v2())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtain an array of all vertexes and edges in a graph.
     *
     * @return an array with the number of vertexes and edges
     */
    public int[] getIntegers() {
        int[] arr = new int[2];
        arr[0] = this.mGraph.allVertices().size();
        arr[1] = this.mGraph.allEdges().size();
        return arr;
    }


    /**
     * Splits a graph into and returns them in a list in descending order by size.
     *
     * @param graph the graph to be split into components
     * @return a list of a graph's components in descending order by size
     */
    private List<MGraph<V, E>> getComponents(MGraph<V, E> graph) {
        List<MGraph<V, E>> compList = new ArrayList<>();
        List<V> verts = graph.allVertices().stream().toList();
        List<V> usedVerts = new ArrayList<>();
        for (V vert: verts) {
            if (!usedVerts.contains(vert)) {
                ALGraph<V, E> currComp = new ALGraph<>();
                currComp.addVertex(vert);
                usedVerts.add(vert);
                int fin;
                List<V> currLayer = new ArrayList<>();
                List<V> nextLayer = new ArrayList<>();
                currLayer.add(vert);
                do {
                    fin = 0;
                    for (V vertL: currLayer) {
                        Map<V, E> currMap = graph.getNeighbours(vertL);
                        for (Map.Entry<V, E> entry: currMap.entrySet()) {
                            currComp.addVertex(entry.getKey());
                            currComp.addEdge(entry.getValue());
                            if (!usedVerts.contains(entry.getKey())) {
                                usedVerts.add(entry.getKey());
                                nextLayer.add(entry.getKey());
                                fin++;
                            }
                        }
                    }
                    currLayer = new ArrayList<>(nextLayer);
                    nextLayer.clear();
                } while (fin > 0);
                compList.add(new ALGraph<>(currComp));
                currComp.clear();
            }
        }
        compList.sort(((o1, o2) -> o2.allVertices().size() - o1.allVertices().size()));
        return compList;
    }

    /**
     * Compute the diameter of the graph.
     * <ul>
     * <li>The diameter of a graph is the length of the longest shortest path in the graph.</li>
     * <li>If a graph has multiple components then we will define the diameter
     * as the diameter of the largest component.</li>
     * </ul>
     *
     * @return the diameter of the graph.
     */
    @Override
    public int diameter() {
        if (this.getComponents(mGraph).size() == 0) {
            return 0;
        }
        MGraph<V, E> largestComp = this.getComponents(mGraph).get(0);
        List<V> verts = largestComp.allVertices().stream().toList();
        Set<E> edges = largestComp.allEdges();
        double[][] dist = new double[verts.size()][verts.size()];
        for (double[] row: dist) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
        for (E edge: edges) {
            dist[verts.indexOf(edge.v1())][verts.indexOf(edge.v2())] = edge.length();
            dist[verts.indexOf(edge.v2())][verts.indexOf(edge.v1())] = edge.length();
        }
        for (V vert: verts) {
            dist[(verts.indexOf(vert))][(verts.indexOf(vert))] = 0.0;
        }
        for (int k = 0; k < verts.size(); k++) {
            for (int i = 0; i < verts.size(); i++) {
                for (int j = 0; j < verts.size(); j++) {
                    dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                }
            }
        }
        double diameter = 0;
        for (double[] row : dist) {
            for (int j = 0; j < dist.length; j++) {
                if (row[j] > diameter) {
                    diameter = row[j];
                }
            }
        }
        return (int) diameter;
    }

    /**
     * Compute the center of the graph.
     *
     * <ul>
     * <li>For a vertex s, the eccentricity of s is defined as the maximum distance
     * between s and any other vertex t in the graph.</li>
     *
     * <li>The center of a graph is the vertex with minimum eccentricity.</li>
     *
     * <li>If a graph is not connected, we will define the graph's center to be the
     * center of the largest connected component.</li>
     * </ul>
     *
     * @return the center of the graph.
     */
    @Override
    public V getCenter() throws IllegalArgumentException {
        if (this.getComponents(mGraph).size() == 0) {
            throw new IllegalArgumentException("Center Does Not Exist: Graph is Empty");
        }
        MGraph<V, E> largestComp = this.getComponents(mGraph).get(0);
        List<V> verts = largestComp.allVertices().stream().toList();
        Set<E> edges = largestComp.allEdges();
        double[][] dist = new double[verts.size()][verts.size()];
        for (double[] row: dist) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
        for (E edge: edges) {
            dist[verts.indexOf(edge.v1())][verts.indexOf(edge.v2())] = edge.length();
            dist[verts.indexOf(edge.v2())][verts.indexOf(edge.v1())] = edge.length();
        }
        for (V vert: verts) {
            dist[(verts.indexOf(vert))][(verts.indexOf(vert))] = 0.0;
        }
        for (int k = 0; k < verts.size(); k++) {
            for (int i = 0; i < verts.size(); i++) {
                for (int j = 0; j < verts.size(); j++) {
                    dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                }
            }
        }
        double[] eccList = new double[dist.length];
        double maxLength;
        for (int i = 0; i < dist.length; i++) {
            maxLength = 0;
            for (int j = 0; j < dist.length; j++) {
                if (dist[i][j] > maxLength) {
                    maxLength = dist[i][j];
                }
            }
            eccList[i] = maxLength;
        }
        int index = 0;
        double min = eccList[0];
        for (int i = 0; i < eccList.length; i++) {
            if (eccList[i] < min) {
                index = i;
                min = eccList[i];
            }
        }
        return (V) verts.get(index).clone();
    }

    public boolean addVertex(V v) {
        return mGraph.addVertex(v);
    }

    public boolean vertex(V v) {
        return mGraph.vertex(v);
    }

    public boolean addEdge(E e) {
        return mGraph.addEdge(e);
    }

    public boolean edge(E e) {
        return mGraph.edge(e);
    }

    public boolean edge(V v1, V v2) {
        return mGraph.edge(v1, v2);
    }

    public int edgeLength(V v1, V v2) {
        return mGraph.edgeLength(v1, v2);
    }

    public int edgeLengthSum() {
        return mGraph.edgeLengthSum();
    }

    public boolean remove(E e) {
        return mGraph.remove(e);
    }

    public boolean remove(V v) {
        return mGraph.remove(v);
    }

    public Set<V> allVertices() {
        return mGraph.allVertices();
    }

    public Set<E> allEdges(V v) {
        return mGraph.allEdges(v);
    }

    public Set<E> allEdges() {
        return mGraph.allEdges();
    }

    public Map<V, E> getNeighbours(V v) {
        return mGraph.getNeighbours(v);
    }

    public void clear() {
        mGraph.clear();
    }

    //// add all new code above this line ////


    /**
     * This method removes some edges at random while preserving connectivity
     * <p>
     * DO NOT CHANGE THIS METHOD
     * </p>
     * <p>
     * You will need to implement allVertices() and allEdges(V v) for this
     * method to run correctly
     *</p>
     * <p><strong>requires:</strong> this graph is connected</p>
     *
     * @param rng random number generator to select edges at random
     */

    public void pruneRandomEdges(Random rng) {
        class VEPair {
            final V v;
            final E e;

            public VEPair(V v, E e) {
                this.v = v;
                this.e = e;
            }
        }
        /* Visited Nodes */
        Set<V> visited = new HashSet<>();
        /* Nodes to visit and the cpen221.mp2.graph.Edge used to reach them */
        Deque<VEPair> stack = new LinkedList<>();
        /* Edges that could be removed */
        ArrayList<E> candidates = new ArrayList<>();
        /* Edges that must be kept to maintain connectivity */
        Set<E> keep = new HashSet<>();

        V start = null;
        for (V v : this.allVertices()) {
            start = v;
            break;
        }
        if (start == null) {
            // nothing to do
            return;
        }
        stack.push(new VEPair(start, null));
        while (!stack.isEmpty()) {
            VEPair pair = stack.pop();
            if (visited.add(pair.v)) {
                keep.add(pair.e);
                for (E e : this.allEdges(pair.v)) {
                    stack.push(new VEPair(e.distinctVertex(pair.v), e));
                }
            } else if (!keep.contains(pair.e)) {
                candidates.add(pair.e);
            }
        }
        // randomly trim some candidate edges
        int iterations = rng.nextInt(candidates.size());
        for (int count = 0; count < iterations; ++count) {
            int end = candidates.size() - 1;
            int index = rng.nextInt(candidates.size());
            E trim = candidates.get(index);
            candidates.set(index, candidates.get(end));
            candidates.remove(end);
            remove(trim);
        }
    }

}
