package gui;

import generator.IGenerator;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.border.*;
import services.IBufferedImageSaveService;


public class MainWindow extends JFrame implements Observer {

    JMenuBar menubar;
    JPanel statusbarPanel;
    BufferedImageDisplayPanel centerImagePanel;
    JLabel statusLabel;
    IGenerator observableGenerator;

    private final IBufferedImageSaveService imageSaveService;
    private final ArrayList<IGenerator> generators;

    public MainWindow(
            IBufferedImageSaveService imageSaveService,
            ArrayList<IGenerator> generators) {
        this.imageSaveService = imageSaveService;
        this.generators = generators;
        initUI();
        RegisterGenerators();
    }

    private void RegisterGenerators() {
        //Register as Observer
        generators.forEach((generator) -> {
            generator.addObserver(this);
        });
    }

    private void initUI() {
        setTitle("Cellular Automata");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());

        //Add Statusbar as JPanel
        statusbarPanel = new JPanel();
        statusbarPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.add(statusbarPanel, BorderLayout.SOUTH);
        statusbarPanel.setPreferredSize(new Dimension(this.getWidth(), 32));
        statusbarPanel.setLayout(new BoxLayout(statusbarPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel("");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusbarPanel.add(statusLabel);

        //Add Menu Bar
        JMenu menu;
        JMenuItem menuItem;
        menubar = new JMenuBar();

        //MenuBar File Entry
        menu = new JMenu("File");
        menubar.add(menu);

        menuItem = new JMenuItem("Save");
        menuItem.addActionListener((ActionEvent ae) -> {
            saveMenuItemClicked(ae);
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        menu.add(menuItem);

        //MenuBar Generators
        menu = new JMenu("Generators");
        menubar.add(menu);

        //Add Generator Entries from ArrayList
        for (IGenerator generator : generators) {
            menuItem = new JMenuItem(generator.getName());
            menuItem.addActionListener((ActionEvent ae) -> {
                generator.setup();
            });
            menu.add(menuItem);
        }

        this.setJMenuBar(menubar);

        //Add Center Panel
        centerImagePanel = new BufferedImageDisplayPanel();
        centerImagePanel.setBackground(Color.DARK_GRAY);
        JScrollPane scrollPane = new JScrollPane(centerImagePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void saveMenuItemClicked(ActionEvent ae) {
        JFileChooser saveFileChooser = new JFileChooser();
        if (saveFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = saveFileChooser.getSelectedFile().getAbsolutePath();
            BufferedImage image = GetBufferedImageFromCenterPanel();
            imageSaveService.saveBufferedImage(path, image);
        }
    }

    private BufferedImage GetBufferedImageFromCenterPanel() {
        return centerImagePanel.getImage();
    }

    @Override
    public void update(Observable o, Object o1) {
        observableGenerator = (IGenerator) o;
        statusLabel.setText(observableGenerator.getName() + " Status: " + observableGenerator.getStatus()
        );
        statusbarPanel.repaint();
        if (observableGenerator.getStatus().equals("READY")) {
            //Update Panel with Image
            centerImagePanel.setImage(observableGenerator.initialize());
            
            int gen = observableGenerator.getGenerations();
            
            if (!observableGenerator.getStatus().equals("FINISHED")){
                Thread t = new Thread(new Runnable(){ 
  
                    @Override
                    public void run() {
                        for(int i = 1; i < gen - 1; i++){
                            centerImagePanel.setImage(observableGenerator.generate());
                            try{
                                Thread.sleep(100);
                            } catch (InterruptedException ie){
                                ie.printStackTrace();
                            }
                        }
                    }
               }); 
               t.start();
            }
        }
    }
    
}
