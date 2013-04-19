/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sxdecorator;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author TianQi
 */
public class SxDecoratorControl
{
    private final SxDecorator sxDecorator;

    public SxDecoratorControl(SxDecorator sxDecorator)
    {
        this.sxDecorator = sxDecorator;
    }
    
    private boolean wndMaximized = false;
    private Rectangle2D prePos;
    public void maximizeOrRestore()
    {
        Rectangle2D bounds;
        Stage stage = sxDecorator.getStage();
        if (!wndMaximized)  // max window
        {
            wndMaximized = true;
            prePos = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

            Screen screen = Screen.getPrimary();
            bounds = screen.getVisualBounds();
        }else
        {
            bounds = prePos;
            wndMaximized = false;
        }

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }
    
    public void addMaximizeOrRestoreNode(final Node node)
    {
        node.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event)
            {
                if (event.getClickCount() == 2)
                {
                    sxDecorator.maximizeProperty().set(!wndMaximized);
                }
            }
        });
    }

    public void minimize()
    {
        Stage stage = sxDecorator.getStage();
        stage.setIconified(true);
    }
    
    public void close()
    {
        Stage stage = sxDecorator.getStage();
        stage.close();
    }

    private double preMouseX;
    private double preMouseY;
    public void addDraggableNode(final Node node)
    {
        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me)
            {
                if (wndMaximized)
                {
                    return;
                }
                preMouseX = me.getSceneX();
                preMouseY = me.getSceneY();
            }
        });

        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me)
            {
                if (wndMaximized)
                {
                    return;
                }
                node.getScene().getWindow().setX(me.getScreenX() - preMouseX);
                node.getScene().getWindow().setY(me.getScreenY() - preMouseY);
            }
        });
    }

    public void addSizeingArea(final Rectangle rect)
    {
        onBorderMouseChange(rect);
        onBorderSizeChange(rect);
    }

    private void onBorderMouseChange(final Rectangle rect)
    {
        final Stage stage = sxDecorator.getStage();
        final double strokeWidth = rect.getStrokeWidth();
        rect.setOnMouseMoved(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (wndMaximized)
                {
                    return;
                }
                double mouseX = event.getSceneX();;
                double mouseY = event.getSceneY();;
                final Bounds bounds = rect.getBoundsInParent();

                if (onTopEdge(bounds, strokeWidth, mouseX, mouseY))
                {
                    if (onLeftEdge(bounds, strokeWidth, mouseX, mouseY))
                    {
                        rect.setCursor(Cursor.NW_RESIZE);
                    }else if (onRightEdge(bounds, strokeWidth, mouseX, mouseY))
                    {
                        rect.setCursor(Cursor.NE_RESIZE);
                    }else
                    {
                        rect.setCursor(Cursor.N_RESIZE);
                    }
                }else if (onBottomEdge(bounds, strokeWidth, mouseX, mouseY))
                {
                    if (onLeftEdge(bounds, strokeWidth, mouseX, mouseY))
                    {
                        rect.setCursor(Cursor.SW_RESIZE);
                    }else if (onRightEdge(bounds, strokeWidth, mouseX, mouseY))
                    {
                        rect.setCursor(Cursor.SE_RESIZE);
                    }else 
                    {
                        rect.setCursor(Cursor.S_RESIZE);
                    }

                }else if (onLeftEdge(bounds, strokeWidth, mouseX, mouseY))
                {
                    rect.setCursor(Cursor.W_RESIZE);
                }else if (onRightEdge(bounds, strokeWidth, mouseX, mouseY))
                {
                    rect.setCursor(Cursor.E_RESIZE);
                }
            }
        });
    }

    private double borderChangeMouseX;
    private double borderChangeMouseY;
    private void onBorderSizeChange(final Rectangle rect)
    {
        final Stage stage = sxDecorator.getStage();
        rect.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event)
            {
                borderChangeMouseX = event.getScreenX();
                borderChangeMouseY = event.getScreenY();
            }
        });

        rect.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event)
            {
                Stage stage = sxDecorator.getStage();
                Cursor cursor = rect.getCursor();
                double deltax = event.getScreenX() - borderChangeMouseX;
                double deltay = event.getScreenY() - borderChangeMouseY;

                if (Cursor.E_RESIZE.equals(cursor))
                {
                    setStageWidth(stage, stage.getWidth() + deltax);
                } else if (Cursor.NE_RESIZE.equals(cursor))
                {
                    if (setStageHeight(stage, stage.getHeight() - deltay))
                    {
                        setStageY(stage, stage.getY() + deltay);
                    }
                    setStageWidth(stage, stage.getWidth() + deltax);
                } else if (Cursor.SE_RESIZE.equals(cursor))
                {
                    setStageWidth(stage, stage.getWidth() + deltax);
                    setStageHeight(stage, stage.getHeight() + deltay);
                } else if (Cursor.S_RESIZE.equals(cursor))
                {
                    setStageHeight(stage, stage.getHeight() + deltay);
                } else if (Cursor.W_RESIZE.equals(cursor))
                {
                    if (setStageWidth(stage, stage.getWidth() - deltax))
                    {
                        stage.setX(stage.getX() + deltax);
                    }
                } else if (Cursor.SW_RESIZE.equals(cursor))
                {
                    if (setStageWidth(stage, stage.getWidth() - deltax))
                    {
                        stage.setX(stage.getX() + deltax);
                    }
                    setStageHeight(stage, stage.getHeight() + deltay);
                } else if (Cursor.NW_RESIZE.equals(cursor))
                {
                    if (setStageWidth(stage, stage.getWidth() - deltax))
                    {
                        stage.setX(stage.getX() + deltax);
                    }
                    if (setStageHeight(stage, stage.getHeight() - deltay))
                    {
                        setStageY(stage, stage.getY() + deltay);
                    }
                } else if (Cursor.N_RESIZE.equals(cursor)) {
                    if (setStageHeight(stage, stage.getHeight() - deltay))
                    {
                        setStageY(stage, stage.getY() + deltay);
                    }
                }

                borderChangeMouseX = event.getScreenX();
                borderChangeMouseY = event.getScreenY();
                event.consume();
            }
        });

    }

    private boolean onTopEdge(Bounds bounds, double edgeWidth, double x, double y)
    {
        if (y >= bounds.getMinY() && y <= bounds.getMinY() + edgeWidth)
        {
            return true;
        }
        return false;
    }

    private boolean onRightEdge(Bounds bounds, double edgeWidth, double x, double y)
    {
        if (x <= bounds.getMaxX() && x >= bounds.getMaxX() - edgeWidth)
        {
            return true;
        }
        return false;
    }
    private boolean onBottomEdge(Bounds bounds, double edgeWidth, double x, double y)
    {
        if (y <= bounds.getMaxY() && y >= bounds.getMaxY() - edgeWidth)
        {
            return true;
        }
        return false;
    }
    private boolean onLeftEdge(Bounds bounds, double edgeWidth, double x, double y)
    {
        if (x >= bounds.getMinX() && x <= bounds.getMinX() + edgeWidth)
        {
            return true;
        }
        return false;
    }

    private void setStageY(Stage stage, double y)
    {
        Screen screen = Screen.getPrimary();
        Rectangle2D visualBounds = screen.getVisualBounds();
        if (y < visualBounds.getHeight() - 30 && y >= visualBounds.getMinY())
        {
            stage.setY(y);
        }
    }

    private boolean setStageWidth(Stage stage, double width)
    {
        if (width >= stage.getMinWidth())
        {
            stage.setWidth(width);
            return true;
        }
        return false;
    }

    private boolean setStageHeight(Stage stage, double height)
    {
        if (height >= stage.getMinHeight())
        {
            stage.setHeight(height);
            return true;
        }
        return false;
    }
}
