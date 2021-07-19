import java.util.List;
import java.util.ArrayList;

public class GetSets{

    private List<Integer> currentStep = new ArrayList<>();
    private final int maximumSize;
    private final int numberOfEdges;
    private boolean firstRun = true;

    /**
     *
     * @param maximumSize
     * @param numberOfEdges
     */
    public GetSets(int maximumSize, int numberOfEdges){
        this.maximumSize = maximumSize;
        this.numberOfEdges = numberOfEdges;
    }

    /**
        Returns the next permutation of size maximum K in increasing order.
     */
    public int[] getNextPerm(){

        //We need an empty set of hyperedges with width 1 for FHD
        if(this.firstRun){
            this.firstRun = false;
            return new int[]{};
        }

        if( this.currentStep.size() == 0){
            this.currentStep.add(0);
            return new int[]{0};
        }

        int level = this.currentStep.size() - 1;
        int[] returnArray = new int[this.currentStep.size()];

        while (level >= 0){
            for(int nextEdge = this.currentStep.get(level) + 1; nextEdge < this.numberOfEdges - (this.currentStep.size() - 1 - level); ++ nextEdge){
                this.currentStep.set(level, nextEdge);

                // we need to reset the numbers at higher levels

                for(int higherLevel = level + 1; higherLevel < this.currentStep.size(); ++ higherLevel) {
                    this.currentStep.set(higherLevel, this.currentStep.get(higherLevel - 1) + 1);
                }

                for (int index = 0; index < this.currentStep.size(); ++index) {
                    returnArray[index] = this.currentStep.get(index);
                }

                return returnArray;
            }

            level --;
        }

        // If we get here, it means we exhausted the values for this number of levels and we need to increase it by one

        if (this.currentStep.size() >= maximumSize || this.currentStep.size() >= this.numberOfEdges){
            return null;
        }

        for(int index = 0; index < this.currentStep.size(); ++index){
            this.currentStep.set(index, index);
        }

        this.currentStep.add(this.currentStep.size());
        returnArray = new int[this.currentStep.size()];

        for (int index = 0; index < this.currentStep.size(); ++index) {
            returnArray[index] = this.currentStep.get(index);
        }

        return returnArray;
    }

    /**
        Returns the next permutation of size maximum K in increasing order.
     */
    public int[] getNextPerm(ArrayList<Integer> domain){


        if( this.firstRun){
            this.firstRun = false;
            return new int[]{};
        }

        if(this.maximumSize == 0){
            return null;
        }

        if(this.currentStep.size() == 0){
            this.currentStep.add(0);
            return new int[]{domain.get(0)};
        }

        int level = this.currentStep.size() - 1;
        int[] returnArray = new int[this.currentStep.size()];

        while (level >= 0){
            for(int nextEdge = this.currentStep.get(level) + 1; nextEdge < domain.size() - (this.currentStep.size() - 1 - level); ++ nextEdge){
                this.currentStep.set(level, nextEdge);

                // we need to reset the numbers at higher levels

                for(int higherLevel = level + 1; higherLevel < this.currentStep.size(); ++ higherLevel) {
                    this.currentStep.set(higherLevel, this.currentStep.get(higherLevel - 1) + 1);
                }

                for (int index = 0; index < this.currentStep.size(); ++index) {
                    returnArray[index] = domain.get(this.currentStep.get(index));
                }

                return returnArray;
            }

            level --;
        }

        // If we get here, it means we exhausted the values for this number of levels and we need to increase it by one

        if (this.currentStep.size() >= maximumSize || this.currentStep.size() >= domain.size() || this.currentStep.size() >= this.numberOfEdges){
            return null;
        }

        for(int index = 0; index < this.currentStep.size(); ++index){
            this.currentStep.set(index, index);
        }

        this.currentStep.add(this.currentStep.size());
        returnArray = new int[this.currentStep.size()];

        for (int index = 0; index < this.currentStep.size(); ++index) {
            returnArray[index] = domain.get(this.currentStep.get(index));
        }

        return returnArray;
    }

    /*public static void main(String[] args){

        GetSets test = new GetSets(3, 10);
        ArrayList<Integer> auxTest = new ArrayList<>();
        auxTest.add(1); auxTest.add(9); auxTest.add(3); auxTest.add(4);auxTest.add(5);

        int[] aux;
        for(int repeat = 0; repeat <= 100; ++repeat) {
            aux = test.getNextPerm(auxTest);

            if (aux == null){
                System.out.println("There are no more possible permutations");
                break;
            }

            for (int i : aux) {
                System.out.print(Integer.toString(i) + ' ');
            }

            System.out.println();
        }
    } */

}
