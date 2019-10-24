
package generator.wolfram;

import generator.IGenerator;
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

/**Implementation of a Wolfram's elementary cellular automaton as a generator.
 * This one-dimensional cellular automaton has been modelled as a two dimensional array.
 * The following configuration has been used:
 *      - the edges are fixed,
 *      - the user can choose the width/height of the image as well as
 *        the size of the quadrants which will be drawn.
 *      The following game parameters are choosable:
 *      - startconfiguration: one can start either with one black cell in the middle
 *        or with randomly distributed black cells in the first lane,
 *      - the rules, which can get applied: there are five ruleset among which the user
 *        can choose (rule 30, rule 90, rule 110, rule 190 and rule 222).
 */

public class WolframCA extends Observable implements IGenerator{
    
    private int height = 600;
    private int width = 600;
    private int size = 10; // size of the quadrat in pixels
    
    private boolean setupDialogIsOpen = false; //Semaphore for displaying the setup dialog only once
    private GeneratorStatusType generatorStatus;
    private final IBufferedImageSaveService saveService;
    private String name = "WolframCAGenerator";
    
    //Defining default values for GUI
    private StartConfig startConf = StartConfig.MIDDLE;
    private Rules Rule = Rules.RULE30;
    
    // DEFINIED RULES of Wolfram's elementary cellular automaton
    // Random => rule 30
    private final int[] rule30 = {0,0,0,1,1,1,1,0};
    // Fractals => rule 90
    private final int[] rule90 = {0,1,0,1,1,0,1,0};
    // Complexity => rule 110
    private final int[] rule110 = {0,1,1,0,1,1,1,0};
    // Repetition => rule 190
    private final int[] rule190 = {0,1,1,1,1,1,0,1};
    // Uniformity => rule 222
    private final int[] rule222 = {0,1,1,1,1,0,1,1};
    
    private int[] currentRule = rule30;
    
    private int rows;
    private int columns;
    private int [][] cells;
    
    private int generations;
    private int currentGeneration = 0;
   
    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     */
    public WolframCA(IBufferedImageSaveService saveService) {
        this.saveService = saveService;
    }

    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     * @param name Name for this generator
     */
    public WolframCA(IBufferedImageSaveService saveService, String name) {
        this.saveService = saveService;
        this.name = name;
    }

    @Override
    public BufferedImage initialize(){
        updateStatus(GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        
        rows = (int)this.height/size;
        columns = (int)this.width/size;
        cells = new int[rows][columns];
        
        generations = cells.length;
         // STARTCONFIGURATION
        // Fill with zeros
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                cells[i][j] = 0;
            } 
        }
        // Fill cells of the first column randomly with ones
        if(startConf == StartConfig.RANDOM){
          for(int j = 0; j < columns; j++){
               int number =(Math.random() < 0.5)?0:1; 
                cells[0][j] = number;
            }      
        }
        else{ //Default start situation:  Fill the middle cell of the first column with 1
            int middle = (int)columns/2;
            cells[0][middle] = 1;
        }
        currentGeneration = 1;
        setCurrentRule();
        
        // DRAW IMAGE
        Graphics2D g2d = image.createGraphics();
        for(int k = 0; k < cells.length; k++){
            int x = k*size;
            for(int l = 0; l < cells[k].length; l++){
                int y = l*size;
                if(cells[k][l] == 1){
                    // Paint a black rectangle
                    g2d.setColor(Color.BLACK);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
                else {// Paint a white rectangle
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
        
        // ALGORITHM: Wolfram's elementary cellular automaton.
        int i = currentGeneration;
          for(int j = 1; j < columns - 1; j++){
                int left   = cells[i-1][j-1];
                int me = cells[i-1][j];
                int right  = cells[i-1][j+1];
                
                int newstate = rules(left,me,right);
                cells[i][j] = newstate;
          }
          if(currentGeneration < generations - 1){
              currentGeneration++;
          }
        
        // DRAW IMAGE
        Graphics2D g2d = image.createGraphics();
        for(int k = 0; k < cells.length; k++){
            int x = k*size;
            for(int l = 0; l < cells[k].length; l++){
                int y = l*size;
                if(cells[k][l] == 1){
                    // Paint a black rectangle
                    g2d.setColor(Color.BLACK);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
                else {// Paint a white rectangle
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
    
   private int rules(int a, int b, int c){
        if      (a == 1 && b == 1 && c == 1) return currentRule[0];
        else if (a == 1 && b == 1 && c == 0) return currentRule[1];
        else if (a == 1 && b == 0 && c == 1) return currentRule[2];
        else if (a == 1 && b == 0 && c == 0) return currentRule[3];
        else if (a == 0 && b == 1 && c == 1) return currentRule[4];
        else if (a == 0 && b == 1 && c == 0) return currentRule[5];
        else if (a == 0 && b == 0 && c == 1) return currentRule[6];
        else if (a == 0 && b == 0 && c == 0) return currentRule[7];
        return 0;
   }
   
   // Setting the currentRule is dependent on the input from the GUI
   private void setCurrentRule(){
        
       switch(Rule){
            case RULE30:
                currentRule = rule30;
                break;
            case RULE90:
                currentRule = rule90;
                break;
            case RULE110:
                currentRule = rule110;
                break;   
            case RULE190:
                currentRule = rule190;
                break;
            default:
                currentRule = rule222;      
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
        JComboBox rulesCombo = new JComboBox(Rules.values());
 
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
                    new JLabel("Rule No.:"),
                    rulesCombo,
                    generateButton,
                    cancelButton},
                null);
        JDialog setupDialog = optionPane.createDialog(optionPane, "Setup Wolfram Elementary CA Generator");

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
            this.Rule = (Rules)rulesCombo.getSelectedItem();
    
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
    
    public void setCurrentGeneration(int cg) {
        currentGeneration = cg;
    }
}
