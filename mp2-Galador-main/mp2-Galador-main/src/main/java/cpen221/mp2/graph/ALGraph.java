package cpen221.mp2.graph;

import java.util.*;

public class ALGraph<V extends Vertex, E extends Edge<V>> implements MGraph<V, E> {

    private final Map<V, Map<V, E>> AL;
    private final Set<E> edges;

    /**
     * Representation Invariant:
     * AL <- first keys are individually unique in their ID and name
     * AL <- values are a map
     * AL <- second keys in the map, are unique in their ID and name and not the same as the first key
     * AL <- second values in the map, v1 and v2 of the edge match the first and second key
     * Edges - edges are unique in the set
     *       - edges appear in AL
     *
     * Abstraction Function:
     * Create a graph following the AL format with a set of edges
     */

    public ALGraph() {
        this.AL = new HashMap<>();
        this.edges = new HashSet<>();
    }

    public ALGraph(MGraph<V, E> g) {
        this.AL = new HashMap<>();
        this.edges = new HashSet<>();
        for (V v: g.allVertices()) {
            this.AL.put(v, new HashMap<>());
        }
        for (E e: g.allEdges()) {
            this.AL.get(e.v1()).put(e.v2(), e);
            this.AL.get(e.v2()).put(e.v1(), e);
            this.edges.add(e);
        }
    }

    public boolean addVertex(V v) {
        if (this.AL.containsKey(v)) {
            return false;
        } else  {
            this.AL.put(v, new HashMap<>());
            return true;
        }

    }

    public boolean vertex(V v) {
        return this.AL.containsKey(v);
    }

    public boolean addEdge(E e) {
        if (this.edges.contains(e)) {
            return false;
        } else {
            this.AL.get(e.v1()).put(e.v2(), e);
            this.AL.get(e.v2()).put(e.v1(), e);
            edges.add(e);
            return true;
        }
    }

    public boolean edge(E e) {
        return this.edges.contains(e);
    }

    public boolean edge(V v1, V v2) {
        if (this.AL.containsKey(v1)) {
            return this.AL.get(v1).containsKey(v2);
        }
        return false;
    }

    public int edgeLength(V v1, V v2) throws IllegalArgumentException {
        if (AL.containsKey(v1)) {
            if (AL.get(v1).containsKey(v2)) {
                return AL.get(v1).get(v2).length();
            }
        }
        throw new IllegalArgumentException("Edge does not exist between v1:" + v1.name() + "and in v2:" + v2.name());
    }


    public int edgeLengthSum() {
        int sum = 0;
        for (E i : edges) {
            sum += i.length();
        }
        return sum;
    }

    public boolean remove(E e) {
        if (this.edges.contains(e)) {
            this.AL.get(e.v1()).remove(e.v2());
            this.AL.get(e.v2()).remove(e.v1());
            edges.remove(e);
            return true;
        }
        return false;
    }

    public boolean remove(V v) {
        if (this.AL.containsKey(v)) {
            this.AL.remove(v);
            for (Map<V, E> j: this.AL.values()) {
                j.remove(v);
            }
            ArrayList<E> temp = new ArrayList<>();

            for (E e : this.edges) {
                if (e.v1().equals(v) || e.v2().equals(v)) {
                    temp.add(e);
                }
            }
            for (E k: temp) {
                this.edges.remove(k);
            }
            return true;
        }
        return false;
    }

    public Set<V> allVertices() {
        Set<V> returnSet = new HashSet<>();
        for (V vertex: this.AL.keySet()) {
            returnSet.add((V) vertex.clone());
        }
        return returnSet;
    }

    public Set<E> allEdges(V v) {
        Set<E> returnSet = new HashSet<>();
        for (E edge: edges) {
            if (edge.v1().equals(v) || edge.v2().equals(v)) {
                returnSet.add((E) edge.clone());
            }
        }
        return returnSet;
    }

    public Set<E> allEdges() {
        Set<E> returnSet = new HashSet<>();
        for (E edge: edges) {
            returnSet.add((E) edge.clone());
        }
        return returnSet;
    }

    public Map<V, E> getNeighbours(V v) {
        if (this.vertex(v)) {
            Map<V, E> returnMap = new HashMap<>();
            for (Map.Entry<V, E> entry: AL.get(v).entrySet()) {
                returnMap.put((V) entry.getKey().clone(), (E) entry.getValue().clone());
            }
            return returnMap;
        } else {
            return new HashMap<>();
        }
    }

    public void clear() {
        this.AL.clear();
        this.edges.clear();
    }
}
