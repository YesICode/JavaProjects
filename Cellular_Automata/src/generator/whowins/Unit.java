
package generator.whowins;

/**This Class holds the information about the boolean status of the cell and how it came about. 
 * The information is used by GUI to draw quadrant in different colors,
 * depending on which rule contributed to the state of the unit.
 * It is used by the Class WhoWins.
 */
public class Unit {
    
    private boolean state; // the result of evaluating the rule combination
    private boolean rule1;// the result of evaluating the first rule
    private boolean rule2;// the result of evaluating the second rule
    
    public Unit(boolean state, boolean rule1, boolean rule2){
        this.state = state;
        this.rule1 = rule1;
        this.rule2 = rule2;
    }
    
    public boolean getState(){
        return state;
    }
    
    public void setState(boolean s){
        state = s;
    }
    
    public boolean getRule1(){
        return rule1;
    }
    
    public void setRule1(boolean r1){
        rule1 = r1;
    }
    
    public boolean getRule2(){
        return rule2;
    }
    
    public void setRule2(boolean r2){
        rule2 = r2;
    }
    
}
