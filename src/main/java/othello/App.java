package othello;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import othello.gui.GameController;


/**
 * Controller used to manipulate the GUI of the game.
 */
public class App extends javafx.application.Application {

    /**
     * A list of accepted command-line arguments for player types.
     * These correspond to different player implementations (e.g., human or AI).
     */
    private final List<String> acceptedArgs = List.of("human", "minimax", "expectimax", "mcts", "custom");

    /**
     * The main entry point for JavaFX applications. Loads the game view and initializes the controller
     * with user-specified player types.
     *
     * @param stage the primary stage for this application, onto which the scene is set
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        Parameters params = getParameters();
        List<String> argList = params.getRaw();
        FXMLLoader fxmlLoader;
        fxmlLoader = new FXMLLoader(App.class.getResource("game-view.fxml"));
        Parent root = fxmlLoader.load();
        GameController controller = fxmlLoader.getController();
        if (argList.size() != 2) {
            System.err.println("Error: Did not provide 2 program arguments");
            System.exit(1);
        }
        if (!acceptedArgs.contains(argList.get(0)) || !acceptedArgs.contains(argList.get(1))) {
            System.err.println("Error: Arguments don't match either 'human' or a computer strategy.");
            System.exit(1);
        }
        controller.initGame(argList.get(0), argList.get(1));
        Scene scene = new Scene(root, 960, 600);
        stage.setTitle("Othello Demo");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the JavaFX Othello application.
     *
     * @param args the command-line arguments specifying the two player types (e.g., "human" "mcts")
     */
    public static void main(String[] args) {
        launch(args);
    }
}