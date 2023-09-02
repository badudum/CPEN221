package cpen221.mp2;

import cpen221.mp2.graph.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EdgeVertexTest {
    @Test
    public void edgeTest() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "B");
        Vertex v3 = new Vertex(3, "C");
        Vertex v4 = new Vertex(4, "D");

        assertThrows(IllegalArgumentException.class, () -> new Edge(null, null, 3));
        assertThrows(IllegalArgumentException.class, () -> new Edge(null, v1, 3));
        assertThrows(IllegalArgumentException.class, () -> new Edge(v2, null, 3));
        assertThrows(IllegalArgumentException.class, () -> new Edge(v1, v1, 3));
        assertThrows(IllegalArgumentException.class, () -> new Edge(v1, v2, -1));

        Edge e1 = new Edge(v1, v2);
        Edge e2 = new Edge(v1, v3, 2);
        Edge e3 = new Edge(v2, v1);
        Edge e4 = new Edge(v3, v4);

        assertFalse(e1.equals(e2));
        assertFalse(e1.equals(null));
        assertTrue(e1.equals((e3)));
        assertTrue(e1.incident(v1));
        assertTrue(e1.incident(v2));
        assertFalse(e1.incident(null));
        assertTrue(e1.intersects(e2));
        assertTrue(e2.intersects(e1));
        assertFalse(e1.intersects(null));
        assertThrows(NoSuchElementException.class, () -> e1.intersection(null));
        assertThrows(NoSuchElementException.class, () -> e1.intersection(e4));
        assertEquals(v1, e1.intersection(e2));
        assertEquals(v1, e3.intersection(e2));
        assertEquals(v1, e3.distinctVertex(v2));
        assertEquals(v2, e3.distinctVertex(v1));
        assertThrows(NoSuchElementException.class, () -> e1.distinctVertex(e3));
        assertEquals(v1, e1.distinctVertex(e4));
        assertEquals(v2, e1.distinctVertex(e2));
        assertEquals(v2, e3.distinctVertex(e2));
    }
    @Test
    public void vertexTest() {
        Vertex v1 = new Vertex(1, "A");
        Vertex v2 = new Vertex(2, "A");
        Vertex v3 = new Vertex(1, "B");
        Vertex v4 = new Vertex(2, "B");
        assertFalse(v1.equals(v2));
        assertFalse(v1.equals(v4));
        assertFalse(v1.equals(null));
        assertEquals(1, v1.id());
    }
}
