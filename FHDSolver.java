import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class FHDSolver {

    private Hypergraph<Integer> hypergraph;
    private final float limitK;
    private final int cBound;
    private final float epsilon;

    /**
     Constructor takes a hypergraph class as parameter and the maximum bound K for the hypergraph decomposition
     */
    public FHDSolver(Hypergraph<Integer> hypergraph, float limitK, int cBound, float epsilon){
        this.hypergraph = hypergraph;
        this.limitK = limitK;
        this.cBound = cBound;
        this.epsilon = epsilon;
    }

    public ArrayList<Integer> getCommonVertices(int edgeIndex, Set<Integer> verticesOfParent){
        ArrayList<Integer> sol = new ArrayList<>();

        for(int vertex : this.hypergraph.hyperedges.get(edgeIndex).keySet()){
            if( verticesOfParent.contains(vertex)){
                sol.add(vertex);
            }
        }

        return sol;
    }

    public boolean checkMutualVerticesConstraint(Component<Integer> componentFather, Set<Integer> verticesOfParent, HashSet<Integer> verticesOfGuards){
        ArrayList<Integer> commonVertices;

        boolean notCommon = true;

        if(verticesOfParent.size() > 0) {
            for (int edgeIndex : componentFather.indicesOfEdges) {

                //We get the common vertices of the edge covering some part of the component and the bag of edges of the father
                commonVertices = getCommonVertices(edgeIndex, verticesOfParent);

                if (commonVertices.size() > 0){ notCommon = false; }

                //and we check if they are included in the current set of guards
                for (Integer vertex : commonVertices) {
                    if (!verticesOfGuards.contains(vertex)) {
                        return false;
                    }
                }
            }
        }

        if(notCommon && verticesOfParent.size() > 0){
            return false;
        }

        // The intersection of the vertices of the component with the vertices of the guards must be not null
        for(Integer vertex : verticesOfGuards){
            if(componentFather.vertices.contains(vertex)){
                return true;
            }
        }

        return false;
    }

    public ArrayList<Component<Integer>> getPossibleComponents(Component<Integer> componentFather, Set<Integer> verticesOfGuards){

        HashMap<Integer, Boolean> explored = new HashMap<>();
        ArrayList<Component<Integer>> allComponents = new ArrayList<>();
        Component<Integer> partialSolution;
        boolean isOK;


        for(Integer vertex : this.hypergraph.vertexToHyperedge.keySet()){
            if( !explored.containsKey(vertex) && !verticesOfGuards.contains(vertex) ) {

                partialSolution = new Component<>();
                DFS(vertex, partialSolution, verticesOfGuards, explored);

                //Check if the new component is included in the parent one

                isOK = true;

                for (Integer vertexInPartial : partialSolution.vertices) {
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

    public void DFS(Integer vertex,
                     Component<Integer> partialSolution,
                     Set<Integer> verticesOfGuards,
                     HashMap<Integer, Boolean> explored){

        if( explored.containsKey(vertex) ) {
            return;
        }

        explored.put(vertex, true);

        //We need to explore all the nodes we can get to from this one
        for (int edge : this.hypergraph.vertexToHyperedge.get(vertex).keySet()){
            for (Integer neighVertex : this.hypergraph.hyperedges.get(edge).keySet()){

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


    public ArrayList<Integer> getPermForW(GetSets permutationGeneratorW, ArrayList<Integer> domainForW){

        ArrayList<Integer> solution = new ArrayList<>();
        int[] indices = permutationGeneratorW.getNextPerm(domainForW);

        if (indices == null){ return null; }

        for(int index : indices){
            solution.add(index);
        }

        return solution;
    }

    public boolean fracDecomp(Component<Integer> componentFather, // comp
                              Set<Integer> treeDecompFather, //set vertices
                              Set<Integer> guardsFather, //set of edges
                              Set<Integer> bagsVerticesFather, // set of vertices
                              ArrayList<Set<Integer>> bags, // arrayList of sets of vertices
                              ArrayList<HashMap<Integer, Double>> guards, // arrayList of sets of edges
                              Set<Integer> coveredVertices){ //set of vertices

        int[] currentPermutation;
        int limit = 0;

        if ((int) this.limitK < this.limitK){
            limit = (int) (this.limitK + this.epsilon) + 1;
        }
        else{
            limit = (int)this.limitK + (int) this.epsilon;
        }

        GetSets permutationGeneratorBag = new GetSets(limit, componentFather.indicesOfEdges.size());

        currentPermutation = permutationGeneratorBag.getNextPerm();

        Set<Integer> reunionForW = new HashSet<>();

        for(int edge : guardsFather){
            reunionForW.addAll(this.hypergraph.hyperedges.get(edge).keySet());
        }

        reunionForW.addAll(componentFather.vertices);
        reunionForW.addAll(treeDecompFather);

        GetSets permutationGeneratorW;

        ArrayList<Integer> currentPermW;

        //I need to get them as an array so I can iterate through them
        Integer[] indicesOfedges = componentFather.indicesOfEdges.toArray(Integer[]::new);

        // It goes through all the combinations of guards and W sets
        while(currentPermutation != null) {

            //iterate through the indices of the indices and get the direct indices of hyperedges
            for(int index = 0; index < currentPermutation.length; ++index){
                currentPermutation[index] = indicesOfedges[currentPermutation[index]];
            }

            permutationGeneratorW = new GetSets(this.cBound, reunionForW.size());
            currentPermW = getPermForW(permutationGeneratorW, new ArrayList<>(reunionForW));


            while (currentPermW != null) {

                if( fracDecomp(componentFather,
                            treeDecompFather,
                            guardsFather,
                            bagsVerticesFather,
                            currentPermutation,
                            currentPermW,
                            bags,
                            guards,
                            coveredVertices)){
                    return true;
                }
                currentPermW = getPermForW(permutationGeneratorW, new ArrayList<>(reunionForW));
            }
            currentPermutation = permutationGeneratorBag.getNextPerm();
        }

        return false;
    }

    public boolean fracDecomp(Component<Integer> componentFather, // comp
                              Set<Integer> treeDecompFather, //set vertices
                              Set<Integer> guardsFather, //set of edges
                              Set<Integer> bagsVerticesFather, // set of vertices
                              int[] currentPermutation, //array of  edges
                              ArrayList<Integer> currentPermW, //arraylist of vertices
                              ArrayList<Set<Integer>> bags, // arrayList of sets of vertices
                              ArrayList<HashMap<Integer, Double>> guards, // arrayList of sets of edges
                              Set<Integer> coveredVertices){ //set of vertices

        // 1 --> guess a set S for the guards of this node, we'll go through all possible combinations of at max K edges as guards, starting from k = 1
        //I also need to go through all the sets for W, and need to compute V(R) U Wr U Cr

        if(currentPermutation.length > this.limitK + this.epsilon){
            return false;
        }

        ArrayList<Component<Integer>> possibleComponentsForThisNode;
        Set<Integer> permAsSet;
        Set<Integer> treeDecompSet;
        permAsSet = new HashSet<>();
        HashSet<Integer> verticesOfGuards = new HashSet<>();

        //We need to get the vertices of the guards
        for (int edge : currentPermutation) {
            verticesOfGuards.addAll(this.hypergraph.hyperedges.get(edge).keySet());
        }

        verticesOfGuards.addAll(currentPermW);

        HashSet<Integer> verticesOfParent = new HashSet<>();

        for(int edge : guardsFather){
            verticesOfParent.addAll(this.hypergraph.hyperedges.get(edge).keySet());
        }
        verticesOfParent.addAll(treeDecompFather);

        HashMap<Integer, Double> newGuard = new HashMap<>();

        for (int edge : currentPermutation) {
            newGuard.put(edge, 1.0);
        }

        //2.a --> we need to check if there is such a decomposition
        if( !SolverFED.solveForHypergraph(this.hypergraph, this.limitK, this.epsilon, newGuard, currentPermW) ) {
            return false;
        }

        //2.b and 2.c
        if ( !checkMutualVerticesConstraint(componentFather, verticesOfParent, verticesOfGuards) ){
            return false;
        }


        guards.add(newGuard);
        HashSet<Integer> newBag = new HashSet<>();

        if(guardsFather.size() == 0){
            newBag.addAll(verticesOfGuards);
        }
        else{
            Set<Integer> reunionComponentBagFather = new HashSet<>();
            reunionComponentBagFather.addAll(componentFather.vertices);
            reunionComponentBagFather.addAll(bagsVerticesFather);

            for(Integer vertex : reunionComponentBagFather){
                if(verticesOfGuards.contains(vertex)){
                    newBag.add(vertex);
                }
            }
        }

        bags.add(newBag);
        coveredVertices.addAll(newBag);

        //if( coveredVertices.size() == this.hypergraph.vertexToHyperedge.size() ) {
        //        return true;
        //}

        //3 --> We need to generate all the possible components using DFS
        possibleComponentsForThisNode = getPossibleComponents(componentFather, verticesOfGuards);

        for (int edge : currentPermutation) {
            permAsSet.add(edge);
        }

        treeDecompSet = new HashSet<>(currentPermW);

        for (Component<Integer> thisComponent : possibleComponentsForThisNode) {

            if ( !fracDecomp(thisComponent, treeDecompSet , permAsSet, newBag, bags, guards, coveredVertices) ){

                bags.remove(bags.size() - 1);
                guards.remove(guards.size() - 1);
                coveredVertices.removeAll(newBag);

                return false;
            }
        }

        return true;
    }


    public static void main(String[] args){

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

        myHypergraph.addHyperEdge(newEgde);

        //3
        newEgde = new ArrayList<>();

        newEgde.add(3);
        newEgde.add(1);

        myHypergraph.addHyperEdge(newEgde);

        //4
        newEgde = new ArrayList<>();

        newEgde.add(3);
        newEgde.add(4);

        myHypergraph.addHyperEdge(newEgde);
        */

        /*
        Hypergraph<Integer> myHypergraph = new Hypergraph<>();

        Map<Integer, Integer> edge = new HashMap<>();

        Instant start = Instant.now();


        for(int indexEdge = 0; indexEdge < 10; ++indexEdge){
            edge = new HashMap<>();

            for (int vertexIndex = 0; vertexIndex < 10; ++vertexIndex) {
                if(vertexIndex != indexEdge) {
                    edge.put(vertexIndex, 0);
                }
            }

            myHypergraph.addHyperEdge(edge);
        }   */

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

        FHDSolver mySolver = new FHDSolver(myHypergraph, (float) 2.5, 3, (float) 0.1);

        Component<Integer> firstComponent = new Component<>();
        firstComponent.vertices.addAll(myHypergraph.vertexToHyperedge.keySet());

        for(int index = 0; index < myHypergraph.hyperedges.size(); ++index){
            firstComponent.indicesOfEdges.add(index);
        }

        Set<Integer> treedecompFather = new HashSet<>();
        ArrayList<Set<Integer>> bags = new ArrayList<>();
        ArrayList<HashMap<Integer, Double>> guards = new ArrayList<>();
        Set<Integer> coveredVertices = new HashSet<>();
        Set<Integer> bagOfVerticesFather = new HashSet<>();

        boolean aux = mySolver.fracDecomp(firstComponent, treedecompFather, bagOfVerticesFather, new HashSet<>(), bags, guards, coveredVertices);
        System.out.println(aux);

        for(Set<Integer> bag : bags){

            System.out.print("The vertices in the bags are ");

            for(int edgeaux : bag){
                System.out.print(edgeaux + " ");
            }

            System.out.println();
        }

        for(HashMap<Integer, Double> guard : guards){

            System.out.print("The guards are ");

            for(int edgeaux : guard.keySet()){
                System.out.print(edgeaux + " has weight " + guard.get(edgeaux) + ", ");
            }

            System.out.println();
        }

        Instant stop = Instant.now();

        System.out.println("The execution took " + Duration.between(start, stop) + " seconds");
    }

}
