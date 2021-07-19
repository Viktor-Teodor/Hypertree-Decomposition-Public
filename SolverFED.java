import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SolverFED {

    public static Hypergraph<Integer> getPartialHypergraph(Hypergraph<Integer> fullHypergraph, ArrayList<Integer> verticesForThisNode){

        Hypergraph<Integer> partialHypergraph = new Hypergraph<>();
        HashMap<Integer, Integer> newEdge;
        boolean isContained = false;
        boolean isContainedOriginal = false;

        for(int vertex : verticesForThisNode){
            for(int coveringEdge : fullHypergraph.vertexToHyperedge.get(vertex).keySet()){
                newEdge = new HashMap<>();

                for(int vertexCovered : fullHypergraph.hyperedges.get(coveringEdge).keySet()){
                    if( verticesForThisNode.contains(vertexCovered)){
                        newEdge.put(vertexCovered, coveringEdge);
                    }
                }

                if(partialHypergraph.hyperedges.size() == 0){
                    partialHypergraph.addHyperEdge(newEdge);
                }
                else {
                    for (Map<Integer, Integer> usedEdge : partialHypergraph.hyperedges) {
                        isContained = true;

                        for (int usedVertex : usedEdge.keySet()) {
                            if (!newEdge.containsKey(usedVertex)) {
                                isContained = false;
                                break;
                            }
                        }
                        if (isContained) {
                            break;
                        }
                    }

                    for (Map<Integer, Integer> usedEdge : fullHypergraph.hyperedges) {
                        isContainedOriginal = true;

                        for (int usedVertex : usedEdge.keySet()) {
                            if (!newEdge.containsKey(usedVertex)) {
                                isContainedOriginal = false;
                                break;
                            }
                        }
                        if (isContainedOriginal) {
                            break;
                        }
                    }

                    if(!isContained || !isContainedOriginal){
                        partialHypergraph.addHyperEdge(newEdge);
                    }
                }

                /*
                if(partialHypergraph.hyperedges.size() == 0){
                    partialHypergraph.addHyperEdge(newEdge);
                }
                else {
                    for (Map<Integer, Integer> usedEdge : partialHypergraph.hyperedges) {
                        isContained = true;

                        for (int usedVertex : usedEdge.keySet()) {
                            if (!newEdge.containsKey(usedVertex)) {
                                isContained = false;
                                break;
                            }
                        }

                        if (isContained) {
                            break;
                        }
                    }

                    if(! isContained){
                        partialHypergraph.addHyperEdge(newEdge);
                    }
                }
                */
            }
        }

        return partialHypergraph;
    }

    public static boolean solveForHypergraph(Hypergraph<Integer> myHypergraph,
                                             float limitK,
                                             float epsilon,
                                             HashMap<Integer, Double> guards,
                                             ArrayList<Integer> wForThisNode){

        //Need to create a new hypergraph that has only the vertices specified by this domain
        if(wForThisNode.size() == 0){
            return true;
        }

        int sizeOfGuards = guards.size();
        Hypergraph<Integer> partialHypergraph = getPartialHypergraph(myHypergraph, wForThisNode);


        // Create the linear solver with the GLOP backend.
        double[] parametersForEdges = new double[partialHypergraph.hyperedges.size()];
        double[] constraintOnEdge = new double[partialHypergraph.hyperedges.size()];
        double[] constraintOnVertex = new double[partialHypergraph.hyperedges.size()];

        for(int index = 0; index < partialHypergraph.hyperedges.size(); ++index){ //all hyperedges have equal weights
            parametersForEdges[index] = 1.0;
            constraintOnEdge[index] = 0.0;
            constraintOnVertex[index] = 0.0;
        }

        LinearProgram lp = new LinearProgram(parametersForEdges); //instantiate the solver

        for(int index = 0; index < partialHypergraph.hyperedges.size(); ++index){ //all weights on edges need to be positive
            constraintOnEdge[index] = 1.0;

            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraintOnEdge, 0.0, "ce" + Integer.toString(index)));
            constraintOnEdge[index] = 0.0;
        }

        for(int indexVertex : partialHypergraph.vertexToHyperedge.keySet()){ //all vertices need to be covered to at least 1.0
            for(int indexEdge : partialHypergraph.vertexToHyperedge.get(indexVertex).keySet()){
                constraintOnVertex[indexEdge] ++;
            }

            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraintOnVertex, 1.0, "cv" + Integer.toString(indexVertex)));

            for(int indexEdge : partialHypergraph.vertexToHyperedge.get(indexVertex).keySet()){
                constraintOnVertex[indexEdge] --;
            }
        }


        lp.setMinProblem(true);

        LinearProgramSolver solver  = SolverFactory.newDefault();
        double[] sol = solver.solve(lp);
        double sum = 0;

        for (int index = 0; index < sol.length; ++index) {

            if(sol[index] == 1){ return false;}

            if(sol[index] > 0){
                sum += sol[index];

                for(int originalEdge : partialHypergraph.hyperedges.get(index).values()) {
                    guards.put(originalEdge, sol[index]);
                    break;
                }
            }
        }


        return sum <= limitK + epsilon - sizeOfGuards;

    }

    public static void main(String[] args) {
        //Hypergraph<Integer> hypergraph = new Hypergraph<>();

       //Map<Integer, Integer> edge = new HashMap<>();

       // Instant start = Instant.now();
        /*
        for(int indexEdge = 0; indexEdge < 100; ++indexEdge){
            edge = new HashMap<>();

            for (int vertexIndex = 0; vertexIndex < 100; ++vertexIndex) {
                if(vertexIndex != indexEdge) {
                    edge.put(vertexIndex, 0);
                }
            }

            hypergraph.addHyperEdge(edge);
        }

        */

        Instant start = Instant.now();
        Hypergraph<Integer> hypergraph = new Hypergraph<>();
        //1
        ArrayList<Integer> newEgde = new ArrayList<>();

        newEgde.add(1);
        newEgde.add(2);

        hypergraph.addHyperEdge(newEgde);

        //2
        newEgde = new ArrayList<>();

        newEgde.add(2);
        newEgde.add(3);
        newEgde.add(9);

        hypergraph.addHyperEdge(newEgde);

        //3
        newEgde = new ArrayList<>();

        newEgde.add(3);
        newEgde.add(4);
        newEgde.add(10);

        hypergraph.addHyperEdge(newEgde);

        //4
        newEgde = new ArrayList<>();

        newEgde.add(4);
        newEgde.add(5);

        hypergraph.addHyperEdge(newEgde);

        //5
        newEgde = new ArrayList<>();

        newEgde.add(5);
        newEgde.add(6);
        newEgde.add(9);

        hypergraph.addHyperEdge(newEgde);

        //6
        newEgde = new ArrayList<>();

        newEgde.add(6);
        newEgde.add(7);
        newEgde.add(10);

        hypergraph.addHyperEdge(newEgde);

        //7
        newEgde = new ArrayList<>();

        newEgde.add(7);
        newEgde.add(8);
        newEgde.add(9);

        hypergraph.addHyperEdge(newEgde);

        // 8
        newEgde = new ArrayList<>();

        newEgde.add(8);
        newEgde.add(1);
        newEgde.add(10);

        hypergraph.addHyperEdge(newEgde);

        // Create the linear solver with the GLOP backend.
        double[] parametersForEdges = new double[hypergraph.hyperedges.size()];
        double[] constraintOnEdge = new double[hypergraph.hyperedges.size()];
        double[] constraintOnVertex = new double[hypergraph.hyperedges.size()];

        for(int index = 0; index < hypergraph.hyperedges.size(); ++index){ //all hyperedges have equal weights
            parametersForEdges[index] = 1.0;
            constraintOnEdge[index] = 0.0;
            constraintOnVertex[index] = 0.0;
        }

        LinearProgram lp = new LinearProgram(parametersForEdges); //instantiate the solver

        for(int index = 0; index < hypergraph.hyperedges.size(); ++index){ //all weights on edges need to be positive
            constraintOnEdge[index] = 1.0;

            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraintOnEdge, 0.0, "ce" + Integer.toString(index)));
            constraintOnEdge[index] = 0.0;
        }

        for(int indexVertex : hypergraph.vertexToHyperedge.keySet()){ //all vertices need to be covered to at least 1.0
            for(int indexEdge : hypergraph.vertexToHyperedge.get(indexVertex).keySet()){
                constraintOnVertex[indexEdge] ++;
            }

            lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraintOnVertex, 1.0, "cv" + Integer.toString(indexVertex)));

            for(int indexEdge : hypergraph.vertexToHyperedge.get(indexVertex).keySet()){
                constraintOnVertex[indexEdge] --;
            }
        }


        lp.setMinProblem(true);
        System.out.println(lp.getConstraints());

        LinearProgramSolver solver  = SolverFactory.newDefault();
        double[] sol = solver.solve(lp);


        System.out.println(sol);

        Instant end = Instant.now();
        System.out.println(Duration.between(start, end));

    }


}
