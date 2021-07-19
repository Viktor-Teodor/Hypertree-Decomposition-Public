import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class HDSolver<T extends Comparable<? super T>> {

    private Hypergraph<T> hypergraph;
    private final int limitK;

    /**
    Constructor takes a hypergraph class as parameter and the maximum bound K for the hypergraph decomposition
     */
    public HDSolver(Hypergraph<T> hypergraph, int limitK){
        this.hypergraph = hypergraph;
        this.limitK = limitK;
    }

    public boolean checkMutualVerticesConstraint(Component<T> componentFather, Set<Integer> bagOfEdgesFather, HashSet<T> verticesOfGuards){
        HashSet<T> commonVertices;
        boolean notCommon = true;

        if (bagOfEdgesFather.size() != 0) {
            for (int edgeIndex : componentFather.indicesOfEdges) {

                //We get the common vertices of the edge covering some part of the component and the bag of edges of the father
                commonVertices = this.hypergraph.intersectionOfEdgeWithSetOfEdges(edgeIndex, bagOfEdgesFather);

                if (commonVertices.size() > 0) {
                    notCommon = false;
                }

                //and we check if they are included in the current set of guards
                for (T vertex : commonVertices) {
                    if (!verticesOfGuards.contains(vertex)) {
                        return false;
                    }
                }
            }
        }

        if(notCommon && bagOfEdgesFather.size() > 0){
            return false;
        }

        // The intersection of the vertices of the component with the vertices of the guards must be not null
        for(T vertex : verticesOfGuards){
            if(componentFather.vertices.contains(vertex)){
                return true;
            }
        }

        return false;
    }

    public ArrayList<Component<T>> getPossibleComponents(Component<T> componentFather, Set<T> verticesOfGuards){

        HashMap<T, Boolean> explored = new HashMap<>();
        ArrayList<Component<T>> allComponents = new ArrayList<>();
        Component<T> partialSolution;
        boolean isOK;


        for(T vertex : this.hypergraph.vertexToHyperedge.keySet()){
            if( !explored.containsKey(vertex) && !verticesOfGuards.contains(vertex) ) {

                partialSolution = new Component<>();
                DFS(vertex, partialSolution, verticesOfGuards, explored);

                //Check if the new component is included in the parent one

                isOK = true;

                for (T vertexInPartial : partialSolution.vertices) {
                    if ( !componentFather.vertices.contains(vertexInPartial) ) {
                        isOK = false;
                        break;
                    }
                }

                if (isOK) {
                    allComponents.add(partialSolution);
                }
            }
        }


        return allComponents;
    }

    public void DFS(T vertex,
                     Component<T> partialSolution,
                     Set<T> verticesOfGuards,
                     HashMap<T, Boolean> explored){

        if( explored.containsKey(vertex) ) {
            return;
        }

        explored.put(vertex, true);

        //We need to explore all the nodes we can get to from this one
        for (int edge : this.hypergraph.vertexToHyperedge.get(vertex).keySet()){
            for (T neighVertex : this.hypergraph.hyperedges.get(edge).keySet()){

                partialSolution.indicesOfEdges.add(edge);

                if(!verticesOfGuards.contains(neighVertex)){

                    partialSolution.vertices.add(neighVertex);

                    DFS(neighVertex,
                            partialSolution,
                            verticesOfGuards,
                            explored);

                }
            }
        }
    }

    public boolean Kdecomposable(Component<T> componentFather,
                                 Set<Integer> guardsFather,
                                 Set<T> bagsVerticesFather,
                                 Set<T> coveredVertices,
                                 ArrayList<Set<T>> bags,
                                 ArrayList<Set<Integer>> guards){

        int[] currentPermutation;

        //get the current permutation as indices of the edges in the current component
        GetSets permutationGenerator = new GetSets(this.limitK, componentFather.indicesOfEdges.size());
        currentPermutation = permutationGenerator.getNextPerm();

        //I need to get them as an array so I can iterate through them
        Integer[] indicesOfedges = componentFather.indicesOfEdges.toArray(Integer[]::new);

        // It goes through all the combinations of guards
        while(currentPermutation != null){

            //iterate through the indices of the indices and get the direct indices of hyperedges
            for(int index = 0; index < currentPermutation.length; ++index){
                currentPermutation[index] = indicesOfedges[currentPermutation[index]];
            }

            //now currentPermutation has the indices of the hyperedges, but just those from the current component

             if(Kdecomposable(componentFather,
                                guardsFather,
                                bagsVerticesFather,
                                currentPermutation,
                                 coveredVertices,
                                 bags,
                                 guards)){

                 return true;
             }

            currentPermutation = permutationGenerator.getNextPerm();
        }

        return false;

    }

    public boolean Kdecomposable(Component<T> componentFather,
                                 Set<Integer> guardsFather,
                                 Set<T> bagsVerticesFather,
                                 int[] currentPermutation,
                                 Set<T> coveredVertices,
                                 ArrayList<Set<T>> bags,
                                 ArrayList<Set<Integer>> guards){

        // 1 --> guess a set S for the guards of this node, we'll go through all possible combinations of at max K edges as guards, starting from k = 1
        Set<Integer> permAsSet;
        ArrayList<Component<T>> possibleComponentsForThisNode;

        permAsSet = new HashSet<>();
        HashSet<T> verticesOfGuards = new HashSet<>();

        //We need to get the vertices of the guards
        for(int edge : currentPermutation){
            verticesOfGuards.addAll(this.hypergraph.hyperedges.get(edge).keySet());
        }

        //2--> We need to test that the intersection of the edges in the Component with the bag of atoms of the father are included in the guards of S
        if (!checkMutualVerticesConstraint(componentFather, guardsFather, verticesOfGuards)){
            return false;
        }

        //3 --> We need to generate all the possible components using DFS
        possibleComponentsForThisNode = getPossibleComponents(componentFather, verticesOfGuards);

        for(int edge : currentPermutation){
            permAsSet.add(edge);
        }

        HashSet<Integer> aux = new HashSet<>(permAsSet);
        guards.add(aux);
        HashSet<T> newBag = new HashSet<>();

        if(guardsFather.size() == 0){
            Set<T> verticesOfGuardsThis = new HashSet<>();

            for(int edge : currentPermutation){
                verticesOfGuardsThis.addAll(this.hypergraph.hyperedges.get(edge).keySet());
            }
            newBag.addAll(verticesOfGuardsThis);
        }
        else{
            Set<T> verticesOfGuardsThis = new HashSet<>();

            for(int edge : currentPermutation){
                verticesOfGuardsThis.addAll(this.hypergraph.hyperedges.get(edge).keySet());
            }

            Set<T> reunionComponentBagFather = new HashSet<>();
            reunionComponentBagFather.addAll(componentFather.vertices);
            reunionComponentBagFather.addAll(bagsVerticesFather);

            for(T vertex : reunionComponentBagFather){
                if(verticesOfGuardsThis.contains(vertex)){
                    newBag.add(vertex);
                }
            }
        }

        bags.add(newBag);

        coveredVertices.addAll(newBag);

        for(Component<T> thisComponent : possibleComponentsForThisNode) {

            if ( !Kdecomposable(thisComponent, permAsSet, newBag, coveredVertices, bags, guards)) {
                bags.remove(bags.size() - 1);
                guards.remove(guards.size() - 1);
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args){

        /*
        Instant start = Instant.now();
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        //0
        ArrayList<Integer> newEgde = new ArrayList<>();

        newEgde.add(0);
        newEgde.add(1);
        newEgde.add(2);
        myHypergraph.addHyperEdge(newEgde);

        //1
        newEgde = new ArrayList<>();

        newEgde.add(3);
        newEgde.add(2);
        myHypergraph.addHyperEdge(newEgde);

        //2
        newEgde = new ArrayList<>();

        newEgde.add(3);
        myHypergraph.addHyperEdge(newEgde);

        //3
        newEgde = new ArrayList<>();

        newEgde.add(1);
        myHypergraph.addHyperEdge(newEgde);

        //4
        newEgde = new ArrayList<>();

        newEgde.add(0);
        newEgde.add(3);
        myHypergraph.addHyperEdge(newEgde);

        //5
        newEgde = new ArrayList<>();

        newEgde.add(4);
        newEgde.add(3);
        myHypergraph.addHyperEdge(newEgde);

        //6
        newEgde = new ArrayList<>();

        newEgde.add(4);
        myHypergraph.addHyperEdge(newEgde);
        */

        /*
        Instant start = Instant.now();
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        //1
        ArrayList<Integer> newEgde = new ArrayList<>();

        newEgde.add(1);
        newEgde.add(2);

        myHypergraph.addHyperEdge(newEgde);

        //2
        newEgde = new ArrayList<>();

        newEgde.add(2);
        newEgde.add(3);
        newEgde.add(9);

        myHypergraph.addHyperEdge(newEgde);

        //3
        newEgde = new ArrayList<>();

        newEgde.add(3);
        newEgde.add(4);
        newEgde.add(10);

        myHypergraph.addHyperEdge(newEgde);

        //4
        newEgde = new ArrayList<>();

        newEgde.add(4);
        newEgde.add(5);

        myHypergraph.addHyperEdge(newEgde);

        //5
        newEgde = new ArrayList<>();

        newEgde.add(5);
        newEgde.add(6);
        newEgde.add(9);

        myHypergraph.addHyperEdge(newEgde);

        //6
        newEgde = new ArrayList<>();

        newEgde.add(6);
        newEgde.add(7);
        newEgde.add(10);

        myHypergraph.addHyperEdge(newEgde);

        //7
        newEgde = new ArrayList<>();

        newEgde.add(7);
        newEgde.add(8);
        newEgde.add(9);

        myHypergraph.addHyperEdge(newEgde);

        // 8
        newEgde = new ArrayList<>();

        newEgde.add(8);
        newEgde.add(1);
        newEgde.add(10);

        myHypergraph.addHyperEdge(newEgde);
        */

        /*
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();
        //1
        ArrayList<Integer> newEdge = new ArrayList<>();

        newEdge.add(1);
        newEdge.add(2);
        newEdge.add(3);
        myHypergraph.addHyperEdge(newEdge);

        //2
        newEdge = new ArrayList<>();
        newEdge.add(2);
        newEdge.add(3);
        newEdge.add(4);

        myHypergraph.addHyperEdge(newEdge);

        //3
        newEdge = new ArrayList<>();
        newEdge.add(3);
        newEdge.add(4);
        newEdge.add(5);

        myHypergraph.addHyperEdge(newEdge);

        //4
        newEdge = new ArrayList<>();
        newEdge.add(4);
        newEdge.add(5);
        newEdge.add(1);

        myHypergraph.addHyperEdge(newEdge);

        //5
        newEdge = new ArrayList<>();
        newEdge.add(5);
        newEdge.add(1);
        newEdge.add(2);

        myHypergraph.addHyperEdge(newEdge);
        */


        Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        Map<Integer, Integer> edge = new HashMap<>();

        Instant start = Instant.now();


        for(int indexEdge = 0; indexEdge < 100; ++indexEdge) {
            edge = new HashMap<>();

            for (int vertexIndex = 0; vertexIndex < 100; ++vertexIndex) {
                if (vertexIndex != indexEdge) {
                    edge.put(vertexIndex, 0);
                }
            }

            myHypergraph.addHyperEdge(edge);
        }


        /*
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        //1
        ArrayList<Integer> newEdge = new ArrayList<>();
        newEdge.add(1);
        newEdge.add(2);

        myHypergraph.addHyperEdge(newEdge);
        //2
        newEdge = new ArrayList<>();
        newEdge.add(2);
        newEdge.add(3);

        myHypergraph.addHyperEdge(newEdge);

        //3
        newEdge = new ArrayList<>();
        newEdge.add(3);
        newEdge.add(1);

        myHypergraph.addHyperEdge(newEdge);
        */

        /*
        long startTime = System.nanoTime();
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        //0
        ArrayList<Integer> newEgde = new ArrayList<>();
        newEgde.add(0);
        newEgde.add(1);
        newEgde.add(2);

        myHypergraph.addHyperEdge(newEgde);

        //1
        newEgde = new ArrayList<>();
        newEgde.add(2);
        newEgde.add(3);
        myHypergraph.addHyperEdge(newEgde);

        //2
        newEgde = new ArrayList<>();
        newEgde.add(3);
        myHypergraph.addHyperEdge(newEgde);

        //3
        newEgde = new ArrayList<>();
        newEgde.add(1);
        myHypergraph.addHyperEdge(newEgde);

        //4
        newEgde = new ArrayList<>();
        newEgde.add(0);
        newEgde.add(3);
        myHypergraph.addHyperEdge(newEgde);

        //5
        newEgde = new ArrayList<>();
        newEgde.add(3);
        newEgde.add(4);
        myHypergraph.addHyperEdge(newEgde);

        //6
        newEgde = new ArrayList<>();
        newEgde.add(4);
        myHypergraph.addHyperEdge(newEgde);
        */

        /*Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        ArrayList<Integer> myEdge = new ArrayList<>();

        myEdge.add(1);
        myEdge.add(2);
        myHypergraph.addHyperEdge(myEdge);

        myEdge = new ArrayList<>();

        myEdge.add(2);
        myEdge.add(3);
        myHypergraph.addHyperEdge(myEdge);

        myEdge = new ArrayList<>();

        myEdge.add(1);
        myEdge.add(3);
        myHypergraph.addHyperEdge(myEdge);

        myEdge = new ArrayList<>();

        myEdge.add(3);
        myEdge.add(4);
        myHypergraph.addHyperEdge(myEdge);
         */

        /*
        long startTime = System.nanoTime();
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();
        //1
        ArrayList<Integer> newEdge = new ArrayList<>();

        newEdge.add(1);
        newEdge.add(2);
        newEdge.add(4);
        myHypergraph.addHyperEdge(newEdge);

        //2
        newEdge = new ArrayList<>();
        newEdge.add(1);
        newEdge.add(3);
        newEdge.add(4);

        myHypergraph.addHyperEdge(newEdge);

        //3
        newEdge = new ArrayList<>();
        newEdge.add(1);
        newEdge.add(6);
        newEdge.add(7);

        myHypergraph.addHyperEdge(newEdge);

        //4
        newEdge = new ArrayList<>();
        newEdge.add(1);
        newEdge.add(5);
        newEdge.add(7);

        myHypergraph.addHyperEdge(newEdge);

        //5
        newEdge = new ArrayList<>();
        newEdge.add(4);
        newEdge.add(8);
        newEdge.add(7);

        myHypergraph.addHyperEdge(newEdge);

        //6
        newEdge = new ArrayList<>();
        newEdge.add(4);
        newEdge.add(9);
        newEdge.add(7);

        myHypergraph.addHyperEdge(newEdge);
        */

        HDSolver<Integer> mySolver = new HDSolver<>(myHypergraph, 1);

        Component<Integer> firstComponent = new Component<>();
        firstComponent.vertices.addAll(myHypergraph.vertexToHyperedge.keySet());

        for(int index = 0; index < myHypergraph.hyperedges.size(); ++index){
            firstComponent.indicesOfEdges.add(index);
        }

        Set<Integer> bafOfWordsFather = new HashSet<>();
        ArrayList<Set<Integer>> bags = new ArrayList<>();
        ArrayList<Set<Integer>> guards = new ArrayList<>();
        Set<Integer> bagOfVerticesFather = new HashSet<>();

        System.out.println(mySolver.Kdecomposable(firstComponent, bafOfWordsFather, bagOfVerticesFather, new HashSet<>(), bags, guards));

        System.out.println("Bags");

        for(Set<Integer> bag : bags){
            for(int vertex : bag){
                System.out.print(vertex + " ");
            }
            System.out.println();
        }


        System.out.println("Guards");
        for(Set<Integer> guard : guards){
            for(int vertex : guard){
                System.out.print(vertex + " ");
            }
            System.out.println();
        }

        Instant stop = Instant.now();

        System.out.print("The execution took " + Duration.between(start, stop) + " seconds");
    }
}
