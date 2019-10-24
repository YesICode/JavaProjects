
package generator.whowins;

import generator.IGenerator;
import generator.wolfram.Rules;
import generator.wolfram.StartConfig;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import services.IBufferedImageSaveService;

/**Implementation of a Wolfram's elementary cellular automaton combining rules
 * using either OR or AND logical operator.
 * The three colors are used to visualize the contribution of each rule to the positive outcome
 * of the logical OR operation (meaning: rule1 OR rule2 == true):
 *      - if only first rule contributed, the quadrat is painted cyan,
 *      - if only second rule contributed, the quadrant is painted magenta,
 *      - if both rules were evaluated as true, the quadrant is painted blue.
 * This one-dimensional cellular automaton has been modelled as a two dimensional array.
 * The following configuration has been used:
 *      - the edges are fixed,
 *      - the user can choose the width/height of the image as well as
 *        the size of the quadrants which will be drawn.
 *      The following game parameters are choosable:
 *      - startconfiguration: one can start either with one black cell in the middle
 *        or with randomly distributed black cells in the first lane,
 *      - two sets of the rules, which can be combined with each other: there are five ruleset among which the user
 *        can choose (rule 30, rule 90, rule 110, rule 190 and rule 222),
 *      - two logical operators, used for combining the rules with each other:
 *        OR and AND.
 */

public class WhoWins extends Observable implements IGenerator{
    private int height = 600;
    private int width = 600;
    private int size = 10; // size of the quadrat in pixels
    
    private boolean setupDialogIsOpen = false; //Semaphore for displaying the setup dialog only once
    private GeneratorStatusType generatorStatus;
    private final IBufferedImageSaveService saveService;
    private String name = "WhoWinsGenerator";
    
    //Defining default values for GUI
    private StartConfig startConf = StartConfig.MIDDLE;
    private Rules Rule1 = Rules.RULE190;
    private Rules Rule2 = Rules.RULE30;
    private Operators operator = Operators.OR;
    
    // DEFINIED RULES of Wolfram's elementary cellular automaton,
    //used for combinations of rules by WhoWins generator:
    // Fractals -> rule 90
    private final boolean[] rule90 = {false,true,false,true,true,false,true,false};
    // Uniformity -> rule 222
    private final boolean[] rule222 = {false,true,true,true,true,false,true,true};
    // Repetition -> rule 190
    private final boolean[] rule190 = {false,true,true,true,true,true,false,true};
    // Random -> rule 30
    private final boolean[] rule30 = {false,false,false,true,true,true,true,false};
    // Complexity -> rule 110
    private final boolean[] rule110 = {false,true,true,false,true,true,true,false};
    
    //Defining default values for rules to be combined
    private boolean[] currentRule1 = rule190;
    private boolean[] currentRule2 = rule30;
    
    private int rows;
    private int columns;
    private Unit [][] units;
    
    private int generations;
    private int currentGeneration = 0;

    
    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     */
    public WhoWins(IBufferedImageSaveService saveService) {
        this.saveService = saveService;
    }

    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     * @param name Name for this generator
     */
    public WhoWins(IBufferedImageSaveService saveService, String name) {
        this.saveService = saveService;
        this.name = name;
    }

    @Override
    public BufferedImage initialize() {
         updateStatus(GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        
        // STARTCONFIGURATION
        rows = (int)this.height/size;
        columns = (int)this.width/size;
        units = new Unit[rows][columns];
        generations = units.length;
        
        // Fill with false values
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                for(int k = 0; k < 3; k++){
                    units[i][j] = new Unit(false, false, false);
                } 
            }
        }
        // Fill cells of the first column randomly with true-values
        if(startConf == StartConfig.RANDOM){
            for(int j = 0; j < columns; j++){
                boolean trueOrfalse =(Math.random() < 0.5);
                //boolean trueOrfalse =(Math.random() < 0.5)? false:true; 
                units[0][j].setState(trueOrfalse);       
            }          
        }
        else{ //Default start situation:  Fill the middle cell of the first column with the true value
            int middle = (int)columns/2;
            units[0][middle].setState(true);
        }
        currentGeneration = 1;
        setCurrentRule1();
        setCurrentRule2();
        
        
        // DRAW IMAGE
        Graphics2D g2d = image.createGraphics();
           for(int k = 0; k < units.length; k++){
            int x = k*size;
            for(int l = 0; l < units[k].length; l++){
                int y = l*size;
                if(units[k][l].getState() == true){
                    // Paint a blue rectangle
                    g2d.setColor(Color.BLUE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size)); 
                } else { // units[k][l].getState() == false
                    // Paint a white rectangle
                    g2d.setColor(Color.WHITE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
            }
        }
        g2d.dispose();
        return image;   
    }
    
    @Override
    public BufferedImage generate(){
        updateStatus(GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        // ALGORITHM: Wolfram's elementary cellular automaton
        // Combining two rule sets with each other using either OR or AND operator.
        int i = currentGeneration;
        for(int j = 1; j < columns - 1; j++){
            boolean left   = units[i-1][i-1].getState();
            boolean me = units[i-1][j].getState();
            boolean right  = units[i-1][j+1].getState();
            units[i][j].setState(applyBothRules(left, me, right));
            units[i][j].setRule1(applyRule1(left, me, right));
            units[i][j].setRule2(applyRule2(left, me, right));
        } 
        if(currentGeneration < generations - 1){
              currentGeneration++;
        }
          
        // DRAW IMAGE: different colors of quadrants are used here dependent on the fact,
        // in as much each of both rules contributed to the result of the logical OR operation.
        // (hence the name of the generator: WhoWins).
        Graphics2D g2d = image.createGraphics();
        for(int k = 0; k < units.length; k++){
            int x = k*size;
            for(int l = 0; l < units[k].length; l++){
                int y = l*size;
                if(units[k][l].getState() == true){
                    if(operator == Operators.OR){
                        //First rule is true and second rule is false
                        if((units[k][l].getRule1() == true) && (units[k][l].getRule2() == false)){
                            // Paint a cyan rectangle
                            g2d.setColor(Color.CYAN);
                            g2d.fill(new Rectangle2D.Double(x, y, size, size));
                        //First rule is false and second rule is true
                        } else if((units[k][l].getRule1() == false) && (units[k][l].getRule2() == true)){
                           // Paint a magenta rectangle
                            g2d.setColor(Color.MAGENTA);
                            g2d.fill(new Rectangle2D.Double(x, y, size, size)); 
                        } else { // 
                            // Both rules are true
                            // Paint a blue rectangle
                            g2d.setColor(Color.BLUE);
                            g2d.fill(new Rectangle2D.Double(x, y, size, size)); 
                        }
                    } else { // operator == Operators.AND and cells[i][j].getState() == true
                    // Paint a blue rectangle
                    g2d.setColor(Color.BLUE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                    }
                }else { //(cells[k][l].getState() == false)
                    // Paint a white rectangle
                    g2d.setColor(Color.WHITE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }  
                
            }
        }
        g2d.dispose();
        if(currentGeneration == generations - 1){
            updateStatus(GeneratorStatusType.FINISHED);
        }
        return image;
    }
    
    // Combining both rules is dependent on the setting of the operator-variable.
    private boolean applyBothRules(boolean a, boolean b, boolean c){
        boolean result;
        if(operator == Operators.AND){
            result = ((applyRule1(a, b, c))&&(applyRule2(a, b, c)));
        } else { // operator == Operators.OR
            result = ((applyRule1(a, b, c))||(applyRule2(a, b, c)));
        }
        return result;
    }
    
   // Applying first rule
   private boolean applyRule1(boolean a, boolean b, boolean c){ 
        if      (a == true && b == true && c == true) return currentRule1[0];
        else if (a == true && b == true && c == false) return currentRule1[1];
        else if (a == true && b == false && c == true) return currentRule1[2];
        else if (a == true && b == false && c == false) return currentRule1[3];
        else if (a == false && b == true && c == true) return currentRule1[4];
        else if (a == false && b == true && c == false) return currentRule1[5];
        else if (a == false && b == false && c == true) return currentRule1[6];
        else if (a == false && b == false && c == false) return currentRule1[7];
        return false;
   }
   
    // Applying second rule 
    private boolean applyRule2(boolean a, boolean b, boolean c){
        if      (a == true && b == true && c == true) return currentRule2[0];
        else if (a == true && b == true && c == false) return currentRule2[1];
        else if (a == true && b == false && c == true) return currentRule2[2];
        else if (a == true && b == false && c == false) return currentRule2[3];
        else if (a == false && b == true && c == true) return currentRule2[4];
        else if (a == false && b == true && c == false) return currentRule2[5];
        else if (a == false && b == false && c == true) return currentRule2[6];
        else if (a == false && b == false && c == false) return currentRule2[7];
        return false;
    }
    
    // Setting the currentRule1 is dependent on the input from the GUI
    private void setCurrentRule1(){
        
        switch(Rule1){
            case RULE30:
                currentRule1 = rule30;
                break;
            case RULE90:
                currentRule1 = rule90;
                break;
            case RULE110:
                currentRule1 = rule110;
                break;   
            case RULE190:
                currentRule1 = rule190;
                break;
            default:
                currentRule1 = rule222;      
        }
        
    }
    
    // Setting the currentRule2 is dependent on the input from the GUI
    private void setCurrentRule2(){
        
        switch(Rule2){
            case RULE30:
                currentRule2 = rule30;
                break;
            case RULE90:
                currentRule2 = rule90;
                break;
            case RULE110:
                currentRule2 = rule110;
                break;   
            case RULE190:
                currentRule2 = rule190;
                break;
            default:
                currentRule2 = rule222;
        }
        
    }
    
     @Override
    public void save() {
        updateStatus(GeneratorStatusType.SAVE);
        this.saveService.saveBufferedImage("new", this.generate());
        updateStatus(GeneratorStatusType.FINISHED);
    }

    @Override
    public String getStatus() {
        return this.generatorStatus.toString();
    }
    
       private void showDialog() {
        //Load Setup Dialog with Parmameters for Width/Height and Size of the quadrats
        SpinnerModel widthHeightModel = new SpinnerNumberModel(this.width, 0, 9000, 1);
        SpinnerModel sizeModel = new SpinnerNumberModel(this.size, 1, 50, 1);
        
        JSpinner widthHeightSpinner = new JSpinner(widthHeightModel);
        JSpinner sizeSpinner = new JSpinner(sizeModel);
        
        JComboBox startCombo = new JComboBox(StartConfig.values());
        JComboBox rule1Combo = new JComboBox(Rules.values());
        JComboBox rule2Combo = new JComboBox(Rules.values());
        
        JComboBox operatorsCombo = new JComboBox(Operators.values());
        
        JButton generateButton = new JButton("Generate");
        JButton cancelButton = new JButton("Cancel");

        JOptionPane optionPane = new JOptionPane(
                "Parameters",
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                new Object[]{
                    new JLabel("Width/Height:"),
                    widthHeightSpinner,
                    new JLabel("Quadrat Size:"),
                    sizeSpinner,
                    new JLabel("Start Conditions:"),
                    startCombo,
                    new JLabel("1.Rule No.:"),
                    rule1Combo,
                    operatorsCombo,
                    new JLabel("2.Rule No.:"),
                    rule2Combo,
                    generateButton,
                    cancelButton},
                null);
        JDialog setupDialog = optionPane.createDialog(optionPane, "Setup Who Wins Generator");

        //Add WindowsListener for CloseEvent
        setupDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setupDialog.dispose();
                setupDialogIsOpen = false;
                updateStatus(GeneratorStatusType.UNCONFIGURED);
            }
        });

        //Add ActionListeners
        cancelButton.addActionListener((ActionEvent e) -> {
            setupDialogIsOpen = false;
            setupDialog.dispose();
            updateStatus(GeneratorStatusType.UNCONFIGURED);
        });

        generateButton.addActionListener((ActionEvent e) -> {
            this.width = (int) widthHeightSpinner.getValue();
            this.height = (int) widthHeightSpinner.getValue();
            this.size = (int)sizeSpinner.getValue();
       
            this.startConf = (StartConfig)startCombo.getSelectedItem();
            
            this.Rule1 = (Rules)rule1Combo.getSelectedItem();
            this.Rule2 = (Rules)rule2Combo.getSelectedItem();
            this.operator = (Operators)operatorsCombo.getSelectedItem();
            
            setupDialogIsOpen = false;
            setupDialog.dispose();
            updateStatus(GeneratorStatusType.READY);
        });

        setupDialog.setModal(false);
        setupDialog.setVisible(true);
        setupDialogIsOpen = true;
    }
    
    @Override
    public void setup() {
        if (!setupDialogIsOpen) {
            updateStatus(GeneratorStatusType.SETUP);
            showDialog();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Updates the generator status and notifies all registered observer that
     * the status has changed.
     *
     * @param newGeneratorStatusValue The new status for this generator
     */
    private void updateStatus(GeneratorStatusType newGeneratorStatusValue) {
        this.generatorStatus = newGeneratorStatusValue;
        //Notify Observers
        setChanged();
        notifyObservers();
    }

    @Override
    public int getGenerations() {
        return generations;
    }
}
