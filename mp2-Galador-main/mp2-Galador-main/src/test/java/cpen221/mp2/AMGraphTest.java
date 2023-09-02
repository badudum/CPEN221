package cpen221.mp2;

import cpen221.mp2.graph.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AMGraphTest {

    @Test
    public void testGraph() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(5, "E");

        Edge<Vertex> e1 = new Edge<>(v1, v2, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);
        Edge<Vertex> e4 = new Edge<>(v3, v4, 9);

        MGraph<Vertex, Edge<Vertex>> g = new AMGraph<>(4);
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
        Assertions.assertFalse(g.edge(v2, v5));
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

        MGraph<Vertex, Edge<Vertex>> g = new AMGraph<>(5);
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
        Edge<Vertex> e4 = new Edge<>(v1, v3, 9);
        Edge<Vertex> e5 = new Edge<>(v5, v3, 9);
        Set<Edge> edgeSetV1 = new HashSet<>();
        edgeSetV1.add(e1);
        edgeSetV1.add(e3);
        Set<Edge> edgeSetE3 = new HashSet<>();
        edgeSetE3.add(e3);

        Map<Vertex, Edge> neighborMap = new HashMap<>();
        neighborMap.put(v2, e1);
        neighborMap.put(v4, e3);

        MGraph<Vertex, Edge<Vertex>> g = new AMGraph<>(4);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        assertFalse(g.remove(e4));
        assertFalse(g.remove(e5));
        assertTrue(g.remove(e2));
        assertEquals(g.allEdges(), edgeSetV1);
        assertTrue(g.remove(v2));
        assertEquals(g.allVertices(), vertSet2);
        assertEquals(g.allEdges(), edgeSetE3);
        assertTrue(g.remove(v1));
        g.clear();
    }
    @Test
    public void testNeighbors() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");

        Edge<Vertex> e1 = new Edge<>(v2, v1, 5);
        Edge<Vertex> e2 = new Edge<>(v2, v3, 7);
        Edge<Vertex> e3 = new Edge<>(v1, v4, 9);

        Map<Vertex, Edge> neighborMap = new HashMap<>();
        neighborMap.put(v2, e1);
        neighborMap.put(v4, e3);

        MGraph<Vertex, Edge<Vertex>> g = new AMGraph<>(4);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        assertEquals(g.getNeighbours(v1).get(v2), neighborMap.get(v2));
        assertEquals(g.getNeighbours(v1).get(v4), neighborMap.get(v4));
    }
    @Test
    public void testOverMaxVert() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");
        Vertex v5 = new Vertex(4, "E");

        MGraph<Vertex, Edge<Vertex>> g = new AMGraph<>(4);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        Assertions.assertFalse(g.addVertex(v5));
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

        MGraph<Vertex, Edge<Vertex>> g = new AMGraph<>(4);
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addEdge(e1);
        g.addEdge(e2);
        g.addEdge(e3);

        assertEquals(g.edgeLength(v2, v3), 7);
        assertEquals(g.edgeLength(v3, v2), 7);
        assertThrows(IllegalArgumentException.class, () -> g.edgeLength(v4, v2));
        assertThrows(IllegalArgumentException.class, () -> g.edgeLength(v5, v2));
        assertThrows(IllegalArgumentException.class, () -> g.edgeLength(v2, v2));
        assertEquals(g.edgeLengthSum(), 21);
    }

}
