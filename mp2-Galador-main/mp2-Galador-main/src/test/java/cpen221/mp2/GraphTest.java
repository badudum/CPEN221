package cpen221.mp2;

import cpen221.mp2.graph.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest<V extends Vertex, E extends Edge<V>> {

    private static final Vertex v1 = new Vertex(1, "A");
    private static final Vertex v2 = new Vertex(2, "B");
    private static final Vertex v3 = new Vertex(3, "C");
    private static final Vertex v4 = new Vertex(4, "D");
    private static final Vertex v5 = new Vertex(5, "E");

    private static final Edge<Vertex> e1 = new Edge<>(v1, v2, 1);
    private static final Edge<Vertex> e2 = new Edge<>(v1, v3, 7);
    private static final Edge<Vertex> e3 = new Edge<>(v2, v3, 5);
    private static final Edge<Vertex> e4 = new Edge<>(v2, v4, 4);
    private static final Edge<Vertex> e5 = new Edge<>(v2, v5, 3);
    private static final Edge<Vertex> e6 = new Edge<>(v3, v5, 6);
    private static final Edge<Vertex> e7 = new Edge<>(v4, v5, 2);

    private static final Graph<Vertex, Edge<Vertex>> g = new Graph<>();

    @BeforeAll
    public static void setup() {
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        g.addEdge(e4);
        g.addEdge(e5);
        g.addEdge(e6);
        g.addEdge(e7);
    }

    @Test
    public void testCenterAndDiameter() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(5, "E");
        Vertex v6 = new Vertex(6, "F");
        Vertex v7 = new Vertex(7, "G");

        Edge<Vertex> e1 = new Edge<>(v1, v2, 25);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v2, v4, 9);
        Edge<Vertex> e4 = new Edge<>(v5, v6, 1);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        assertThrows(IllegalArgumentException.class, g::getCenter);
        assertEquals(0,g.diameter());
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addVertex(v6);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        g.addEdge(e4);

        assertEquals(e1, g.getEdge(v1, v2));
        assertEquals(e1, g.getEdge(v2, v1));
        assertThrows(IllegalArgumentException.class, () -> g.getEdge(v1, v4));
        assertThrows(IllegalArgumentException.class, () -> g.getEdge(v1, v7));
        assertEquals(v2, g.getCenter());
        assertEquals(34, g.diameter());
    }

    @Test
    public void testNeighbors() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(5, "E");

        Edge<Vertex> e1 = new Edge<>(v1, v2, 1);
        Edge<Vertex> e2 = new Edge<>(v1, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v2, v4, 9);

        Map<V, E> nMap = new HashMap<>();
        Map<V, E> fullMap = new HashMap<>();
        Map<V, E> emptyMap = new HashMap<>();
        nMap.put((V) v1, (E) e1);
        nMap.put((V) v3, (E) e2);
        fullMap.put((V) v1, (E) e1);
        fullMap.put((V) v3, (E) e2);
        fullMap.put((V) v4, (E) e3);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        assertThrows(IllegalArgumentException.class, () -> g.getNeighbours(v5, 5));
        assertEquals(emptyMap.entrySet(), g.getNeighbours(v2, 0).entrySet());
        assertEquals(nMap.entrySet(), g.getNeighbours(v2, 8).entrySet());
        assertEquals(fullMap.entrySet(), g.getNeighbours(v2, 100).entrySet());
    }

    @Test
    public void testMinSpanCompK1() {
        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        g.addEdge(e4);
        g.addEdge(e5);
        g.addEdge(e6);
        g.addEdge(e7);

        Graph<Vertex, Edge<Vertex>> correct = new Graph<>();
        correct.addVertex(v1);
        correct.addVertex(v2);
        correct.addVertex(v3);
        correct.addVertex(v4);
        correct.addVertex(v5);
        correct.addEdge(e1);
        correct.addEdge(e3);
        correct.addEdge(e5);
        correct.addEdge(e7);
        Set<ImGraph<Vertex, Edge<Vertex>>> test = new HashSet<>();
        test.add(correct);
        List<ImGraph<Vertex, Edge<Vertex>>> test2 = test.stream().toList();
        int[] arrTest = test2.get(0).getIntegers();

        Set<ImGraph<Vertex, Edge<Vertex>>> real = g.minimumSpanningComponents(1);
        List<ImGraph<Vertex, Edge<Vertex>>> real2 = real.stream().toList();
        int[] arrReal = real2.get(0).getIntegers();

        assertEquals(arrTest[0], arrReal[0]);
        assertEquals(arrTest[1], arrReal[1]);
    }

    @Test
    public void testMinSpanCompK5() {
        Graph<Vertex, Edge<Vertex>> correct = new Graph<>();
        correct.addVertex(v1);
        correct.addVertex(v2);
        correct.addVertex(v3);
        correct.addVertex(v4);
        correct.addVertex(v5);
        Set<ImGraph<Vertex, Edge<Vertex>>> test = new HashSet<>();
        test.add(correct);
        List<ImGraph<Vertex, Edge<Vertex>>> test2 = test.stream().toList();
        int[] arrTest = test2.get(0).getIntegers();

        Set<ImGraph<Vertex, Edge<Vertex>>> real = g.minimumSpanningComponents(5);
        List<ImGraph<Vertex, Edge<Vertex>>> real2 = real.stream().toList();
        int edge = 0;
        int vertex = 0;
        for (ImGraph<Vertex, Edge<Vertex>> x : real2) {
            int[] arrReal = x.getIntegers();
            vertex += arrReal[0];
            edge += arrReal[1];
        }

        assertEquals(arrTest[0], vertex);
        assertEquals(arrTest[1], edge);
    }

    @Test
    public void testMinSpanCompK2() {
        Graph<Vertex, Edge<Vertex>> correct = new Graph<>();
        correct.addVertex(v1);
        correct.addVertex(v2);
        correct.addVertex(v3);
        correct.addVertex(v4);
        correct.addVertex(v5);
        correct.addEdge(e1);
        correct.addEdge(e7);
        correct.addEdge(e5);
        Set<ImGraph<Vertex, Edge<Vertex>>> test = new HashSet<>();
        test.add(correct);
        List<ImGraph<Vertex, Edge<Vertex>>> test2 = test.stream().toList();
        int[] arrTest = test2.get(0).getIntegers();

        Set<ImGraph<Vertex, Edge<Vertex>>> real = g.minimumSpanningComponents(2);
        List<ImGraph<Vertex, Edge<Vertex>>> real2 = real.stream().toList();
        int edge = 0;
        int vertex = 0;
        for (ImGraph<Vertex, Edge<Vertex>> x : real2) {
            int[] arrReal = x.getIntegers();
            vertex += arrReal[0];
            edge += arrReal[1];
        }

        assertEquals(arrTest[0], vertex);
        assertEquals(arrTest[1], edge);
    }


    @Test
    public void testMinSpanCompK3() {
        Graph<Vertex, Edge<Vertex>> correct = new Graph<>();
        correct.addVertex(v1);
        correct.addVertex(v2);
        correct.addVertex(v3);
        correct.addVertex(v4);
        correct.addVertex(v5);
        correct.addEdge(e1);
        correct.addEdge(e2);
        Set<ImGraph<Vertex, Edge<Vertex>>> test = new HashSet<>();
        test.add(correct);
        List<ImGraph<Vertex, Edge<Vertex>>> test2 = test.stream().toList();
        int[] arrTest = test2.get(0).getIntegers();

        Set<ImGraph<Vertex, Edge<Vertex>>> real = g.minimumSpanningComponents(3);
        List<ImGraph<Vertex, Edge<Vertex>>> real2 = real.stream().toList();
        int edge = 0;
        int vertex = 0;
        for (ImGraph<Vertex, Edge<Vertex>> x : real2) {
            int[] arrReal = x.getIntegers();
            vertex += arrReal[0];
            edge += arrReal[1];
        }

        assertEquals(arrTest[0], vertex);
        assertEquals(arrTest[1], edge);
    }

    @Test
    public void testShortestPath() {
        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        g.addEdge(e4);
        g.addEdge(e5);
        g.addEdge(e6);
        g.addEdge(e7);

        List<Vertex> test1 = new ArrayList<>();
        test1.add(v1);
        test1.add(v2);
        test1.add(v3);
        assertEquals(test1, g.shortestPath(v1, v3));


        List<Vertex> test2 = new ArrayList<>();
        test2.add(v4);
        test2.add(v2);
        test2.add(v1);
        assertEquals(test2, g.shortestPath(v4, v1));

        List<Vertex> test3 = new ArrayList<>();
        test3.add(v1);
        test3.add(v5);
        assertThrows(IllegalArgumentException.class, () -> g.pathLength(test3));

        List<Vertex> test4 = new ArrayList<>();
        test4.add(v1);
        assertEquals(test4, g.shortestPath(v1, v1));
    }
    @Test
    public void testShortestPathException() {
        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        g.addEdge(e4);
        g.addEdge(e5);
        g.addEdge(e6);
        g.addEdge(e7);

        Vertex tester = new Vertex(99, "Z");
        List<Vertex> test = new ArrayList<>();
        assertEquals(test, g.shortestPath(tester, v1));
        g.addVertex(tester);
        assertEquals(test, g.shortestPath(tester, v1));
    }

    @Test
    public void testLengths() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(5, "E");

        Edge<Vertex> e1 = new Edge<>(v1, v2, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        assertEquals(g.edgeLength(v2, v3),7);
        assertEquals(g.edgeLength(v3, v2),7);
        assertThrows(IllegalArgumentException.class, () -> g.edgeLength(v4, v2));
        assertThrows(IllegalArgumentException.class, () -> g.edgeLength(v5, v2));
        assertThrows(IllegalArgumentException.class, () -> g.edgeLength(v2, v2));
        assertEquals(g.edgeLengthSum(),21);
    }

    @Test
    public void testNeighbors2() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");

        Edge<Vertex> e1 = new Edge<>(v2, v1, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);
        Edge<Vertex> e4 = new Edge<>(v1, v3, 9);
        Edge<Vertex> e5 = new Edge<>(v2, v4, 9);
        Edge<Vertex> e6 = new Edge<>(v3, v4, 9);

        Map<Vertex, Edge> neighborMap = new HashMap<>();
        neighborMap.put(v2, e1);
        neighborMap.put(v4, e3);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        g.addEdge(e4);
        g.addEdge(e5);
        g.addEdge(e6);

        assertEquals(g.getNeighbours(v1).get(v2), neighborMap.get(v2));
        assertEquals(g.getNeighbours(v1).get(v4), neighborMap.get(v4));
        g.pruneRandomEdges(new Random());
    }

    @Test
    public void testRemovals() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(5, "E");
        Set<Vertex> vertSet2 = new HashSet<>();

        vertSet2.add(v1);
        vertSet2.add(v3);
        vertSet2.add(v4);

        Edge<Vertex> e1 = new Edge<>(v1, v2, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);
        Edge<Vertex> e4 = new Edge<>(v1, v5, 9);
        Set<Edge> edgeSetV1 = new HashSet<>();
        edgeSetV1.add(e1);
        edgeSetV1.add(e3);
        Set<Edge> edgeSetE3 = new HashSet<>();
        edgeSetE3.add(e3);

        Map<Vertex, Edge> neighborMap = new HashMap<>();
        neighborMap.put(v2, e1);
        neighborMap.put(v4, e3);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        assertFalse(g.remove(e4));
        g.remove(e2.clone());
        assertEquals(g.allEdges(), edgeSetV1);
        g.addEdge(e2);
        g.remove(v2);
        assertEquals(g.allVertices(), vertSet2);
        assertEquals(g.allEdges(), edgeSetE3);
    }

    @Test
    public void testGraph() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(4, "E");

        Edge<Vertex> e1 = new Edge<>(v1, v2, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);
        Edge<Vertex> e4 = new Edge<>(v3, v4, 9);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        Assertions.assertFalse(g.remove(v5));
        Assertions.assertFalse(g.remove(e4));
        Assertions.assertTrue(g.vertex(v1));
        Assertions.assertFalse(g.vertex(v5));
        Assertions.assertTrue(g.edge(e1));
        Assertions.assertFalse(g.edge(e4));
        Assertions.assertTrue(g.edge(v1, v2));
        Assertions.assertFalse(g.edge(v2, v4));
        Assertions.assertFalse(g.edge(v5, v2));
    }

    @Test
    public void testReturnSets() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Set<Vertex> vertSet = new HashSet<>();
        vertSet.add(v1);
        vertSet.add(v2);
        vertSet.add(v3);
        vertSet.add(v4);

        Edge<Vertex> e1 = new Edge<>(v1, v2, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);
        Set<Edge> edgeSet = new HashSet<>();
        edgeSet.add(e1);
        edgeSet.add(e2);
        edgeSet.add(e3);
        Set<Edge> edgeSetV1 = new HashSet<>();
        edgeSetV1.add(e1);
        edgeSetV1.add(e3);

        Graph<Vertex, Edge<Vertex>> g = new Graph<>();
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        Assertions.assertFalse(g.addVertex(v4));
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);
        Assertions.assertFalse(g.addEdge(e3));

        assertEquals(g.allEdges(), edgeSet);
        assertEquals(g.allVertices(), vertSet);
        assertEquals(g.allEdges(v1), edgeSetV1);
        g.clear();
    }

}
