package generator.simplegenerator;

import generator.IGenerator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
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

/**
 * Implementation of a simple generator that creates a blue circle for a given
 * height and width.
 */
public class SimpleGenerator extends Observable implements IGenerator {

    private int height = 400;
    private int width = 400;
    private boolean setupDialogIsOpen = false; //Semaphore for displaying the setup dialog only once
    private GeneratorStatusType generatorStatus;
    private final IBufferedImageSaveService saveService;
    private String name = "SimpleGenerator";
    
    private int generations = 1;

    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     */
    public SimpleGenerator(IBufferedImageSaveService saveService) {
        this.saveService = saveService;
    }

    /**
     * Constructor
     *
     * @param saveService Inject IBufferedImageService
     * @param name Name for this generator
     */
    public SimpleGenerator(IBufferedImageSaveService saveService, String name) {
        this.saveService = saveService;
        this.name = name;
    }
    
    @Override
    public BufferedImage initialize(){
      updateStatus(GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fill(new Ellipse2D.Float(0, 0, image.getWidth(), image.getHeight()));
        g2d.dispose();
        updateStatus(GeneratorStatusType.FINISHED);
        return image;   
    }
    
    @Override
    public BufferedImage generate() {
        updateStatus(GeneratorStatusType.CALCULATE);
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fill(new Ellipse2D.Float(0, 0, image.getWidth(), image.getHeight()));
        g2d.dispose();
        updateStatus(GeneratorStatusType.FINISHED);
        return image;
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
        //Load Setup Dialog with Parmameters for Width and Height
        SpinnerModel widthModel = new SpinnerNumberModel(this.width, 0, 9000, 1);
        SpinnerModel heightModel = new SpinnerNumberModel(this.height, 0, 9000, 1);
        JSpinner widthSpinner = new JSpinner(widthModel);
        JSpinner heightSpinner = new JSpinner(heightModel);
        JButton generateButton = new JButton("Generate");
        JButton cancelButton = new JButton("Cancel");

        JOptionPane optionPane = new JOptionPane(
                "Parameters",
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                new Object[]{
                    new JLabel("Width:"),
                    widthSpinner,
                    new JLabel("Height:"),
                    heightSpinner,
                    generateButton,
                    cancelButton},
                null);
        JDialog setupDialog = optionPane.createDialog(optionPane, "Setup Generator");

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
            this.width = (int) widthSpinner.getValue();
            this.height = (int) heightSpinner.getValue();
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
