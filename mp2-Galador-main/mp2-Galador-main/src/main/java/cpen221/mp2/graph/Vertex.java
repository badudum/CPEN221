package cpen221.mp2.graph;

/**
 * Represents a graph vertex. Each vertex has an associated id and name.
 * No two vertices in the same graph should have the same id.
 */
public class Vertex implements Cloneable {
    private final int id;
    private final String name;

    /**
     * Representation Invariant:
     * name - name is not null or empty.
     * Abstraction Function:
     * Represents an unique graph vertex.
     */

    /**
     * Create a new vertex
     *
     * @param id   is a numeric identifier for the vertex
     * @param name is a name for the vertex
     */
    public Vertex(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Vertex clone() {
        return new Vertex(this.id, this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vertex other) {
            return other.id == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + id;
    }

    /**
     * Obtain the vertex id
     *
     * @return the vertex id
     */
    public int id() {
        return id;
    }

    /**
     * Obtain the vertex name
     *
     * @return the vertex name
     */
    public String name() {
        return name;
    }

    //// --- any new methods, if necessary, go below this link --- ////

}
