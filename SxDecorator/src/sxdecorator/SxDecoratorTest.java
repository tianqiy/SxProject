/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sxdecorator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author tyang
 */
public class SxDecoratorTest extends Application
{

    @Override
    public void start(Stage primaryStage)
    {
        try {
            Button btn = new Button();
            btn.setText("Say 'Hello World'");
            btn.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    System.out.println("Hello World!");
                }
            });

            Region client = (Region)FXMLLoader.load(getClass().getResource("ClientArea.fxml"));
            SxDecorator root = new SxDecorator(primaryStage, client, SxDecorator.DecoratorStyle.Normal);

            Scene scene = new Scene(root, 800, 600);

            scene.setFill(Color.BLUE);
            primaryStage.setTitle("Hello World!");
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(SxDecoratorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
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
