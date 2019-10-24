package program;

import generator.IGenerator;
import generator.simplegenerator.SimpleGenerator;
import generator.wolfram.WolframCA;
import generator.whowins.WhoWins;
import generator.epidemic.Epidemic;
import generator.gol.GameOfLife;
import gui.MainWindow;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import services.BufferedImageSaveService;
import services.IBufferedImageSaveService;

/**
 *
 * @author Jan-Christopher Icken
 */
public class Program {

    public static void main(String[] args) {
        //Create ImageSaveService Instance
        IBufferedImageSaveService imageSaveService = new BufferedImageSaveService();
        //Register Generators
        ArrayList<IGenerator> generators = new ArrayList<>();
        generators.add(new SimpleGenerator(imageSaveService, "Simple Generator"));
        generators.add(new WolframCA(imageSaveService, "Wolfram CA Generator"));
        generators.add(new GameOfLife(imageSaveService, "Game of Life Generator"));
        generators.add(new WhoWins(imageSaveService, "Who Wins Generator"));
        generators.add(new Epidemic(imageSaveService, "Epidemics Generator"));
        //Show GUI
        SwingUtilities.invokeLater(() -> {
            final MainWindow wnd = new MainWindow(imageSaveService, generators);
            wnd.setVisible(true);
        });
    }
}
