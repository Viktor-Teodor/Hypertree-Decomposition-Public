import java.util.HashSet;

public class Component <K extends Comparable<? super K>> {
    public HashSet<K> vertices;
    public HashSet<Integer> indicesOfEdges;

    public Component(){
        this.vertices = new HashSet<>();
        this.indicesOfEdges = new HashSet<>();
    }
}
