/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import window.MainWnd;
import sxdecorator.SxDecorator;

/**
 *
 * @author tyang
 */
public class SxFileCaseMain extends Application
{

    @Override
    public void start(Stage primaryStage)
    {
        
        MainWnd mainWindow = new MainWnd();
        SxDecorator root = new SxDecorator(primaryStage, mainWindow, SxDecorator.DecoratorStyle.Normal);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setTitle("SxFileCase");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
