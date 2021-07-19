import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
/*
public class SaveDFS {

    private ArrayList<Component<T>> getPossibleComponents(Component<T> componentFather, Set<T> verticesOfGuards){

        HashMap<T, Boolean> visited = new HashMap<>();
        HashMap<T, Boolean> explored = new HashMap<>();
        ArrayList<Component<T>> allComponents = new ArrayList<>();
        ArrayList<Component<T>> sol = new ArrayList<>();
        Component<T> partialSolution;
        ArrayList<Component<T>> listOfPartialSolutions = new ArrayList<>();
        boolean isOK;
        boolean alreadyIn;
        boolean canInsert;
        int maxVertices = 0;

        for(T vertex : this.hypergraph.vertexToHyperedge.keySet()){
            if( !explored.containsKey(vertex) && !verticesOfGuards.contains(vertex) ) {
                explored.put(vertex,true);

                partialSolution = new Component<>();
                DFS(vertex, listOfPartialSolutions, partialSolution, verticesOfGuards, visited, explored);

                //Check if the new component is included in the parent one
                for (Component<T> componentInList : listOfPartialSolutions) {

                    isOK = true;

                    for (T vertexInPartial : componentInList.vertices) {
                        if ( !componentFather.vertices.contains(vertexInPartial) ) {
                            isOK = false;
                            break;
                        }
                    }

                    if (isOK) {

                        //check if we don't have this one already
                        canInsert = true;

                        for(Component<T> componentIn : allComponents){

                            alreadyIn = true;

                            for(T vertexInComp : componentInList.vertices){
                                if(!componentIn.vertices.contains(vertexInComp)){
                                    alreadyIn = false;
                                    break;
                                }
                            }
                            for(int edgeInComp : componentInList.indicesOfEdges){
                                if(!componentIn.indicesOfEdges.contains(edgeInComp)){
                                    alreadyIn = false;
                                }
                            }

                            if(alreadyIn){
                                canInsert = false;
                                break;
                            }
                        }

                        if(canInsert) {
                            if (componentInList.vertices.size() > maxVertices){
                                maxVertices = componentInList.vertices.size();
                            }

                            allComponents.add(componentInList);
                        }
                    }
                }
            }
        }

        for(Component<T> comp : allComponents){
            if(comp.vertices.size() == maxVertices){
                sol.add(comp);
            }
        }

        return sol;
    }

    private void DFS(T vertex,
                     ArrayList<Component<T>> listOfPartialSolutions,
                     Component<T> partialSolution,
                     Set<T> verticesOfGuards,
                     HashMap<T, Boolean> visited,
                     HashMap<T, Boolean> explored){

        boolean isLast = true;
        explored.put(vertex, true);


        //We need to explore all the nodes we can get to from this one
        for (int edge : this.hypergraph.vertexToHyperedge.get(vertex).keySet()){
            for (T neighVertex : this.hypergraph.hyperedges.get(edge).keySet()){
                if( !visited.containsKey(neighVertex)  && !verticesOfGuards.contains(neighVertex) && neighVertex != vertex){ //took out neighVertex != vertex
                    visited.put(neighVertex, true);
                    isLast = false;

                    partialSolution.vertices.add(neighVertex);
                    partialSolution.indicesOfEdges.add(edge);

                    DFS(neighVertex,
                            listOfPartialSolutions,
                            partialSolution,
                            verticesOfGuards,
                            visited,
                            explored);

                    //reset this so we can reuse later
                    visited.remove(neighVertex);
                }
            }
        }

        if (isLast){
            Component<T> newComponent = new Component<>();

            newComponent.vertices.addAll(partialSolution.vertices);
            newComponent.indicesOfEdges.addAll(partialSolution.indicesOfEdges);

            listOfPartialSolutions.add(newComponent);
        }
    }

}
*/