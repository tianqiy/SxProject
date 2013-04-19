/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package window;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 *
 * @author tyang
 */
public class DirectoryTreeViewController
{
    private final TreeView<String> treeview;
    private FilePathTreeItem treeRoot;
    private DirectoryTreeViewController(TreeView<String> treeview)
    {
        this.treeview = treeview;
        treeRoot = new FilePathTreeItem();
        treeview.setRoot(treeRoot);
        treeview.setShowRoot(false);

//        treeview.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
//
//            @Override
//            public TreeCell<File> call(TreeView<File> param)
//            {
//                TreeCell<File> cell = new TreeCell<File>()
//                {
//                    @Override
//                    protected void updateItem(File item, boolean empty)
//                    {
//                        super.updateItem(item, empty);
//                        if (item == null)
//                        {
//                            return;
//                        }
//                        setText(item.getPath());
//                        if (getItem().isDirectory())
//                        {
//                            getTreeItem().getChildren().add(new TreeItem<File>());
//                        }
//                    }
//                };
//                return cell;
//            }
//        });
    }
    public static DirectoryTreeViewController control(TreeView<String> treeview)
    {
        return new DirectoryTreeViewController(treeview);
    }

    public ObservableList<TreeItem<String>> getItems()
    {
        return treeRoot.getChildren();
    }
}
