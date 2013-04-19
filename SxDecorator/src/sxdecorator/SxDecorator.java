/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sxdecorator;

import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

/**
 *
 * @author tyang
 */
public class SxDecorator extends StackPane
{
    private final Region clientLayer;
    private Rectangle sizeingLayer;
    private Rectangle backgroundLayer;
    private Pane decoratorLayer;

    private final Stage stage;
    private final SxDecoratorControl control;

    public static enum DecoratorStyle{Normal, ToolBox};

    public SxDecorator(Stage stage, Region clientLayer, DecoratorStyle style)
    {
        this.stage = stage;
        this.clientLayer = clientLayer;
        this.getStylesheets().add(this.getClass().getResource("undecorator.css").toExternalForm());

        this.control = new SxDecoratorControl(this);
        String decoratorFXML = "StageDecorationLayer.fxml";
        if (style == DecoratorStyle.ToolBox)
        {
                decoratorFXML = "StageToolBoxDecorationLayer.fxml";
        }
        initBackgroundLayer();
        initdecoratorLayer(decoratorFXML);
        initSizeingLayer();

        stage.setMinWidth(clientLayer.getMinWidth());
        stage.setMinHeight(clientLayer.getMinHeight());

        this.control.addDraggableNode(clientLayer);
        this.control.addMaximizeOrRestoreNode(clientLayer);
        this.getChildren().addAll(backgroundLayer, clientLayer, decoratorLayer, sizeingLayer);
    }

    private DropShadow dsFocused;
    private DropShadow dsNotFocused;

    private int SHADOW_WIDTH = 10;
    private int SAVED_SHADOW_WIDTH = SHADOW_WIDTH;
    private void initBackgroundLayer()
    {
        backgroundLayer = new Rectangle();
        backgroundLayer.getStyleClass().add("undecorator-background");

        //set shadow
        dsFocused = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, SHADOW_WIDTH, 0.1, 0, 0);
        dsNotFocused = new DropShadow(BlurType.THREE_PASS_BOX, Color.DARKGREY, SHADOW_WIDTH, 0, 0, 0);

        stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    backgroundLayer.setEffect(dsFocused);
                }else
                {
                    backgroundLayer.setEffect(dsNotFocused);
                }
            }
        });

        maximizeProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    backgroundLayer.setEffect(null);
                }else
                {
                    backgroundLayer.setEffect(dsFocused);
                }
            }
        });
    }

    @FXML private Button btnClose;
    @FXML private Button btnMaximize;
    @FXML private Button btnMinimize;
    @FXML private Button btnResize;
    private void initdecoratorLayer(String decoratorFXML)
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(decoratorFXML));
        loader.setController(this);
        try
        {
            this.decoratorLayer = (AnchorPane)loader.load();
        } catch (IOException e)
        {
            // TODO: handle exception
            throw new RuntimeException(e);
        }

        if (btnMaximize != null)
        {
            btnMaximize.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent t)
                {
                    maximizeProperty().set(!maximizeProperty().get());
                }
            });
        }

        // Minimize button
        if (btnMinimize != null)
        {
            btnMinimize.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent t)
                {
                    minimizeProperty().set(!minimizeProperty().get());
                    control.minimize();
                }
            });
        }

        btnClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t)
            {
                closeProperty().set(!closeProperty().get());
                control.close();
            }
        });

        maximizeProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
            {
                if (t1)
                {
                    btnMaximize.getStyleClass().add("decoration-button-restore");
                }else 
                {
                    btnMaximize.getStyleClass().remove("decoration-button-restore");
                }
                btnResize.setVisible(!t1);
                setShowShadow(!t1);
                setShowSizeingArea(!t1);
                control.maximizeOrRestore();
            }
        });
    }

    private int RESIZE_WIDTH = 5;
    private int SAVED_RESIZE_WIDTH = RESIZE_WIDTH;
    private void initSizeingLayer()
    {
        sizeingLayer = new Rectangle();
        sizeingLayer.setFill(null);
        sizeingLayer.setStrokeWidth(RESIZE_WIDTH);
        sizeingLayer.setStrokeType(StrokeType.INSIDE);
        sizeingLayer.setStroke(Color.TRANSPARENT);
        control.addSizeingArea(sizeingLayer);
    }

    private BooleanProperty maximizeProperty;
    public BooleanProperty maximizeProperty()
    {
        if (maximizeProperty == null)
        {
            maximizeProperty = new SimpleBooleanProperty(false);
        }
        return maximizeProperty;
    }
    
    private BooleanProperty minimizeProperty;
    public BooleanProperty minimizeProperty()
    {
        if (minimizeProperty == null)
        {
            minimizeProperty = new SimpleBooleanProperty(false);
        }
        return minimizeProperty;
    }

    private BooleanProperty closeProperty;
    public BooleanProperty closeProperty()
    {
        if (closeProperty == null)
        {
            closeProperty = new SimpleBooleanProperty(false);
        }
        return closeProperty;
    }

    public Stage getStage()
    {
        return this.stage;
    }
    
    private void setShowShadow(boolean show) 
    {
        if (show && maximizeProperty.get()) {
            return;
        }
        if (!show)
        {
            SAVED_SHADOW_WIDTH = SHADOW_WIDTH;
            SHADOW_WIDTH = 0;
        } else 
        {
            SHADOW_WIDTH = SAVED_SHADOW_WIDTH;
        }
    }
    
    private void setShowSizeingArea(boolean show) 
    {
        // From fullscreen to maximize situation
        if (show && maximizeProperty.get()) {
            return;
        }
        if (!show)
        {
            SAVED_RESIZE_WIDTH = RESIZE_WIDTH;
            RESIZE_WIDTH = 0;
        } else 
        {
            RESIZE_WIDTH = SAVED_RESIZE_WIDTH;
        }
    }

    @Override
    protected double computePrefWidth(double d) {
        return clientLayer.getPrefWidth() + SHADOW_WIDTH * 2 + RESIZE_WIDTH * 2;
    }

    @Override
    protected double computePrefHeight(double d) {
        return clientLayer.getPrefHeight() + SHADOW_WIDTH * 2 + RESIZE_WIDTH * 2;
    }

    @Override
    protected double computeMaxWidth(double d) {
        return clientLayer.getMaxWidth() + SHADOW_WIDTH * 2 + RESIZE_WIDTH * 2;
    }

    @Override
    protected double computeMinWidth(double d) {
        double d2 = super.computeMinWidth(d);
        d2 += SHADOW_WIDTH * 2 + RESIZE_WIDTH * 2;
        return d2;
    }

    @Override
    public void layoutChildren()
    {
        Bounds b = super.getLayoutBounds();
        double w = b.getWidth();
        double h = b.getHeight();
        ObservableList<Node> list = super.getChildren();
        for (Node node : list)
        {
            if (node == backgroundLayer)
            {
                backgroundLayer.setWidth(w - SHADOW_WIDTH * 2);
                backgroundLayer.setHeight(h - SHADOW_WIDTH * 2);
                backgroundLayer.setX(SHADOW_WIDTH);
                backgroundLayer.setY(SHADOW_WIDTH);
            } else if (node == decoratorLayer)
            {
                decoratorLayer.resize(w - SHADOW_WIDTH * 2 - RESIZE_WIDTH * 2, h - SHADOW_WIDTH * 2);
                decoratorLayer.setLayoutX(SHADOW_WIDTH + RESIZE_WIDTH);
                decoratorLayer.setLayoutY(SHADOW_WIDTH );
            } else if (node == sizeingLayer)
            {
                sizeingLayer.setWidth(w - SHADOW_WIDTH * 2);
                sizeingLayer.setHeight(h - SHADOW_WIDTH * 2);
                sizeingLayer.setLayoutX(SHADOW_WIDTH);
                sizeingLayer.setLayoutY(SHADOW_WIDTH);
            } else
            {
                node.resize(w - SHADOW_WIDTH * 2 - RESIZE_WIDTH * 2, h - SHADOW_WIDTH * 2 - RESIZE_WIDTH * 2);
                node.setLayoutX(SHADOW_WIDTH + RESIZE_WIDTH);
                node.setLayoutY(SHADOW_WIDTH + RESIZE_WIDTH);
            }
        }
    }
}
