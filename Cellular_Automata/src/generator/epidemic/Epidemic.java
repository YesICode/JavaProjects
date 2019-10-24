
package generator.epidemic;

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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import services.IBufferedImageSaveService;

/***Implementation of a cellular automaton modelling an epidemic as a generator.
 * The Epidemic starts with variable (choosable) percentage of empty, healthy and sick cells.
 * One can also set for how many generations a sick cell should stay sick.
 * Importantly, there are no new cells getting spontaneusly sick during the run of the algorithm.
 * In this way a healthy cell can only get sick if it has a direct contact with a sick cell.
 * Additionally, the sick and healthy cells can move, if there are empty cells next to them.
 * This contributes to the spread of infection and modells the living cells, which can move.
 *  
 * This cellular automaton has been modelled as a two dimensional array.
 * The following configuration has been used:
 *      - the edges are fixed,
 *      - the game starts with randomly distributed
 *        empty(BLUE), healthy(GREEN) and sick(RED) cells,
 *      - von Neumann neighborhood has been used.
 *      - the user can choose the width/height of the image as well as
 *        the size of the quadrants which will be drawn.
 *      The following game parameters are choosable:
 *      - the percentage of the empty and living cells at the beginning
 *       (hence the % of sick cells == 100% - % of healthy cells),
 *      - the number of generations for how long a cell stays sick,
 *      - the number of generations for the generator to run.
 */
public class Epidemic extends Observable implements IGenerator{
    private int height = 600;
    private int width = 600;
    private int size = 10; // The size of the quadrat in pixels
    
    private boolean setupDialogIsOpen = false; //Semaphore for displaying the setup dialog only once
    private IGenerator.GeneratorStatusType generatorStatus;
    private final IBufferedImageSaveService saveService;
    private String name = "EpidemicsGenerator";
    
    private int generations = 10; 
     private int currentGeneration = 0;
    
    private int c = 2;// For how many generations a cell stays sick
    private double empty = 0.3; // Empty cells ratio
    private double healthy = 0.99; // Healthy cells ratio
    
    private int rows;
    private int columns;
    private Cell [][] cells;
     
    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     */
    public Epidemic(IBufferedImageSaveService saveService) {
        this.saveService = saveService;
    }

    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     * @param name Name for this generator
     */
    public Epidemic(IBufferedImageSaveService saveService, String name) {
        this.saveService = saveService;
        this.name = name;
    }

    @Override
    public BufferedImage initialize() {
        updateStatus(IGenerator.GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // STARTCONFIGURATION
        rows = (int)(this.height/size);
        columns = (int)(this.width/size);
        cells = new Cell[rows][columns];
        // 1. Initiate an array of empty cells
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                    cells[i][j] = new Cell(); 
            } 
        }
        // 2. Fill x % of the array with healthy cells
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                   int number =(Math.random()< empty)?0:1;  
                   if(number == 1){
                       cells[i][j].setState(State.HEALTHY);
                   } 
            }
        }
       
        // 3. Make  y % of the healthy cells sick
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                   int number =(Math.random() < healthy)?0:1;  
                   if((cells[i][j].getState() == State.HEALTHY)){
                       if(number == 1){
                            cells[i][j] = new Cell(State.SICK, c);
                       }
                   } 
            }
        }
        currentGeneration = 1;
        
        // DRAW IMAGE
        for(int k = 0; k < cells.length; k++){
            int x = k*size;
            for(int l = 0; l < cells[k].length; l++){
                int y = l*size;
                if(cells[k][l].getState() == State.HEALTHY){
                    // Paint a green rectangle to visualize a healthy cell
                    g2d.setColor(Color.GREEN);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
                // Paint a red rectangle to visualize a sick cell
                else if (cells[k][l].getState() == State.SICK){
                    g2d.setColor(Color.RED);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                } 
                // Paint a blue rectangle to visualize an empty cell
                else {
                    g2d.setColor(Color.BLUE);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }      
            }
        }
        g2d.dispose();
        return image;    
    }
    
    @Override
    public BufferedImage generate(){
        updateStatus(IGenerator.GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
    // AlGORITHM -> Epidemic
        // PHASE 1: Interactions -> Infections
        // a)Deep copy the existing array into a new one
        Cell [][] next = new Cell[rows][columns];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                 next[i][j] = new Cell(cells[i][j].getState(), cells[i][j].getCounter());  
            }
        }   // There are two arrays existing now, 
            //which are filled with the cells having the same states.
            
        //  b)Using the information from the first array
        //  calculate the states in the next array
        for (int i = 1; i < rows - 1; i++){
            for(int j = 1; j < columns - 1; j++){
                if(cells[i][j].getState() == State.SICK){
                    Cell left   = cells[i][j-1];
                    if(left.getState() == State.HEALTHY){
                        //int number =(Math.random()<0.5)?0:1; 
                        //if(number == 1){
                            next[i][j-1] = new Cell(State.SICK, c);
                        //}
                    }
                    Cell right  = cells[i][j+1];
                    if(right.getState() == State.HEALTHY){
                        //int number =(Math.random()<0.5)?0:1; 
                        //if(number == 1){
                            next[i][j+1] = new Cell(State.SICK, c);
                        //}
                    }
                    Cell upper   = cells[i-1][j];
                    if(upper.getState() == State.HEALTHY){
                        //int number =(Math.random()<0.5)?0:1; 
                        //if(number == 1){
                            next[i-1][j] = new Cell(State.SICK, c);
                        //}
                    }
                    Cell lower  = cells[i+1][j];
                    if(lower.getState() == State.HEALTHY){
                        //int number =(Math.random()<0.5)?0:1; 
                        //if(number == 1){
                            next[i+1][j] = new Cell(State.SICK, c);
                        //}
                    }
                } 
               
            } 
        }
        // Now that the new health status of the cells has been calculated,
        // set the newly calculated array as a main one
        cells = next;
        
        // PHASE 2: Movement
        for (int i = 1; i < rows - 1; i++){
            for(int j = 1; j < columns - 1; j++){
                // Healthy or sick cells can move ...
                if((cells[i][j].getState() == State.HEALTHY)
                    ||(cells[i][j].getState() == State.SICK)){
                    // (Choose randomly a number: 1, 2, 3 or 4)
                    int number = 1 + (int)(Math.random()*4);
                    // ... to the left
                    if((cells[i][j-1].getState() == State.EMPTY)&& (number == 1)){
                       Cell change = cells[i][j];
                       cells[i][j] = cells[i][j-1];
                       cells[i][j-1] = change;
                       break;
                    }
                    // ...or up
                    if((cells[i-1][j].getState() == State.EMPTY)&& (number == 2)){
                        Cell change = cells[i][j];
                        cells[i][j] = cells[i-1][j];
                        cells[i-1][j] = change; 
                        break;
                    }
                    // ...or to the right
                    if((cells[i][j+1].getState() == State.EMPTY)&& (number == 3)){
                        Cell change = cells[i][j];
                        cells[i][j] = cells[i][j+1];
                        cells[i][j+1] = change;
                        break;
                    }
                    // ... or down
                    if((cells[i+1][j].getState() == State.EMPTY)&& (number == 4)){
                        Cell change = cells[i][j];
                        cells[i][j] = cells[i+1][j];
                        cells[i+1][j] = change;
                        break;
                    }
                } 
               
            } 
        }

        // PHASE 3: Update of the sickness counter and health status. 
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){ 
                // First decrement counter of disease of the sick cells...
                if(cells[i][j].getState() == State.SICK){
                    cells[i][j].decrementCounter();
                    // ... and then if it's zero set the sick cell healthy.
                    if(cells[i][j].getCounter() == 0){
                        cells[i][j].setState(State.HEALTHY);
                    }
                }      
            }
        }
        if(currentGeneration < generations - 1){
              currentGeneration++;
        }

        // DRAW IMAGE
        for(int k = 0; k < cells.length; k++){
            int x = k*size;
            for(int l = 0; l < cells[k].length; l++){
                int y = l*size;
                if(cells[k][l].getState() == State.HEALTHY){
                    // Paint a green rectangle to visualize a healthy cell
                    g2d.setColor(Color.GREEN);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                }
                // Paint a red rectangle to visualize a sick cell
                else if (cells[k][l].getState() == State.SICK){
                    g2d.setColor(Color.RED);
                    g2d.fill(new Rectangle2D.Double(x, y, size, size));
                } 
                // Paint a blue rectangle to visualize an empty cell
                else {
                    g2d.setColor(Color.BLUE);
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

    
     @Override
    public void save() {
        updateStatus(IGenerator.GeneratorStatusType.SAVE);
        this.saveService.saveBufferedImage("new", this.generate());
        updateStatus(IGenerator.GeneratorStatusType.FINISHED);
    }

    @Override
    public String getStatus() {
        return this.generatorStatus.toString();
    }
    
       private void showDialog() {
        //Load Setup Dialog with Parmameters for Width/Height and Size of the quadrats
        SpinnerModel widthHeightModel = new SpinnerNumberModel(this.width, 0, 9000, 1);
        SpinnerModel sizeModel = new SpinnerNumberModel(this.size, 1, 50, 1);
        
        //Load Setup Dialog with Parmameters for generation number, length of the sickness (counter),
        // the percentage of empty and healthy cells.
        SpinnerModel generationModel = new SpinnerNumberModel(this.generations, 0, 10000, 1);
        SpinnerModel counterModel = new SpinnerNumberModel(this.c, 0, 1000, 1);
        SpinnerModel emptyModel = new SpinnerNumberModel(this.empty*100.0, 0.0, 100.0, 1.0);
        SpinnerModel healthyModel = new SpinnerNumberModel(this.healthy*100.0, 0.0, 100.0, 1.0);

        JSpinner widthHeightSpinner = new JSpinner(widthHeightModel);
        JSpinner sizeSpinner = new JSpinner(sizeModel);
        
        JSpinner generationSpinner = new JSpinner(generationModel);
        JSpinner counterSpinner = new JSpinner(counterModel);
        JSpinner emptySpinner = new JSpinner(emptyModel);
        JSpinner healthySpinner = new JSpinner(healthyModel);
        
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
                    new JLabel("Generations:"),
                    generationSpinner,
                    new JLabel("Sick (how long?):"),
                    counterSpinner,
                    new JLabel("Empty:"),
                    emptySpinner,
                    new JLabel("Healthy:"),
                    healthySpinner,
                    // here some more parameter Spinners, JComboBoxes
                    generateButton,
                    cancelButton},
                null);
        JDialog setupDialog = optionPane.createDialog(optionPane, "Setup Epidemics Generator");

        //Add WindowsListener for CloseEvent
        setupDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setupDialog.dispose();
                setupDialogIsOpen = false;
                updateStatus(IGenerator.GeneratorStatusType.UNCONFIGURED);
            }
        });

        //Add ActionListeners
        cancelButton.addActionListener((ActionEvent e) -> {
            setupDialogIsOpen = false;
            setupDialog.dispose();
            updateStatus(IGenerator.GeneratorStatusType.UNCONFIGURED);
        });

        generateButton.addActionListener((ActionEvent e) -> {
            this.width = (int) widthHeightSpinner.getValue();
            this.height = (int) widthHeightSpinner.getValue();
            this.size = (int)sizeSpinner.getValue();
            
            this.generations = (int) generationSpinner.getValue();
            this.c = (int)counterSpinner.getValue();
            this.empty = ((double)emptySpinner.getValue())/100.0;
            this.healthy = ((double)healthySpinner.getValue())/100.0;

            setupDialogIsOpen = false;
            setupDialog.dispose();
            updateStatus(IGenerator.GeneratorStatusType.READY);
        });

        setupDialog.setModal(false);
        setupDialog.setVisible(true);
        setupDialogIsOpen = true;
    }
    
    @Override
    public void setup() {
        if (!setupDialogIsOpen) {
            updateStatus(IGenerator.GeneratorStatusType.SETUP);
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
    private void updateStatus(IGenerator.GeneratorStatusType newGeneratorStatusValue) {
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
