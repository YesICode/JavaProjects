package generator.epidemic;

/**This Class holds the information about the health status of the cell 
 * and for how many generations it will stay sick.
 * It is used by the Class Epidemic.
 */
public class Cell {
    
    private int counter;
    private State state;
    
    public Cell(){
        counter = 0;
        state = State.EMPTY;
    }
    
    public Cell(State state){
        counter = 0;
        this.state = state;
    }
    
    public Cell(State state, int counter){ 
        this.state = state;
        this.counter = counter;
    }
    
    public int getCounter(){
        return counter;
    }
    
    
    public void setCounter(int c){
        counter = c;
    }
    
    public void decrementCounter(){
        if (counter > 0)
            counter = counter - 1;
    }
    
    public State getState(){
        return state;
    }
    
    public void setState(State s){
        state = s;
    }
    
}
