import java.util.*;


public class Hypergraph<K extends Comparable<? super K>>{

    public ArrayList<Map<K, Integer>> hyperedges; //maps hyperedges to vertices. ArrayList< edgeNumber, Map< vertex, weightOnVertex>>
    public Map<K, Map<Integer, Boolean>> vertexToHyperedge = new HashMap<>(); //maps vertices to hyperedges to keep track more easily of intersections of hyperedges
    private int numberOfHyperEdges;

    public Hypergraph(){
        this.numberOfHyperEdges = 0;
        hyperedges = new ArrayList<>();
    }

    public void addHyperEdge(List<K> hyperEdge){
        Map<K, Integer> newHyperEdge = new HashMap<>();
        Map<Integer, Boolean> aux;

        for(K vertex : hyperEdge){
            newHyperEdge.put(vertex, 0);
            aux = this.vertexToHyperedge.getOrDefault(vertex, new HashMap<>());
            aux.put(this.numberOfHyperEdges, true);
            this.vertexToHyperedge.put(vertex, aux);
        }

        this.hyperedges.add(newHyperEdge);
        this.numberOfHyperEdges ++;
    }

    public void addHyperEdge(Map<K, Integer> hyperEdge){
        Map<Integer, Boolean> aux;

        for(K vertex : hyperEdge.keySet()){
            aux = this.vertexToHyperedge.getOrDefault(vertex, new HashMap<>());
            aux.put(this.numberOfHyperEdges, true);
            this.vertexToHyperedge.put(vertex, aux);
        }

        this.hyperedges.add(hyperEdge);
        this.numberOfHyperEdges ++;
    }

    public HashSet<K> intersectionOfEdgeWithSetOfEdges(int edge, Set<Integer> bagOfEdgesFather){

        HashSet<K> solution = new HashSet<>();

        //we go through all the vertices belonging to all the edges in the bag of atoms
        for(K vertex : this.hyperedges.get(edge).keySet()){
            for(int coveringHyperEdge : this.vertexToHyperedge.get(vertex).keySet()){
                if(bagOfEdgesFather.contains(coveringHyperEdge)){
                    solution.add(vertex);
                }
            }
        }

        return solution;
    }
}
