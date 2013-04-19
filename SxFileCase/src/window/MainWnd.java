/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package window;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import window.structure.ExplorerItem;
import window.utilities.IconReader;
import window.utilities.StringUtils;
import window.utilities.SxUtilities;

/**
 *
 * @author tyang
 */
public class MainWnd extends AnchorPane
{
    public MainWnd()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWnd.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try
        {
            loader.load();
        } catch (IOException e)
        {
            // TODO: handle exception
            throw new RuntimeException(e);
        }

        this.getStylesheets().add(getClass().getResource("MainWnd.css").toExternalForm());

        initFileExplorer();
        initDirectoryTree();
        setOnMenuHandler();
    }

    @FXML BorderPane mainBorderPane;
   
    @FXML TreeView repositoryTreeView;
    @FXML TreeView directoryTreeView;

    //menu items
    @FXML MenuItem dirAddMenu;
    @FXML MenuItem dirRemoveMenu;
    @FXML MenuItem dirFileFilterMenu;

    @FXML MenuItem bkupNowMenu;
    @FXML MenuItem bkupStopMenu;

    @FXML MenuItem rstToOrgnlMenu;
    @FXML MenuItem rstToAtntMenu;

    @FXML ToolBar mainToolBar;
    @FXML Button btnAdd;
    @FXML Button btnCommit;
    @FXML Button btnDiscard;
    @FXML Button btnRemove;
    @FXML Button btnDelete;
    @FXML Button btnShowRevision;

    @FXML TableView fileExplorerTableView;
    @FXML TableColumn columnName;
    @FXML TableColumn columnState;
    @FXML TableColumn columnDate;
    @FXML TableColumn columDir;
    @FXML TableColumn columnSize;

    private void initFileExplorer()
    {
        columnName.setCellValueFactory(new PropertyValueFactory<ExplorerItem, String>("filename"));
        columnName.setCellFactory(new Callback<TableColumn, TableCell>()
        {
            @Override
            public TableCell call(TableColumn param)
            {
                TableCell cell = new TableCell<TableColumn, String>()
                {
                    @Override
                    protected void updateItem(String item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (!empty)
                        {
                            Image im;
                            im = IconReader.getIcon(item, false);
                            setText(item);
                            setGraphic(new ImageView(im));
                        }
                    }

                };

                return cell;
            }
            
        });
        columnState.setCellValueFactory(new PropertyValueFactory<ExplorerItem, String>("state"));
        columnDate.setCellValueFactory(new PropertyValueFactory<ExplorerItem, String>("date"));
        columDir.setCellValueFactory(new PropertyValueFactory<ExplorerItem, String>("dir"));
        columnSize.setCellValueFactory(new PropertyValueFactory<ExplorerItem, String>("size"));
        fileExplorerTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private DirectoryTreeViewController directoryTreeViewController;
    private void initDirectoryTree()
    {
        directoryTreeViewController = DirectoryTreeViewController.control(directoryTreeView);

        directoryTreeView.getSelectionModel().getSelectedIndices().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable)
            {
                try {
                    fileExplorerTableView.getItems().clear();
                    directoryTreeView.getSelectionModel().getSelectedIndex();
                    FilePathTreeItem item = (FilePathTreeItem)directoryTreeView.getSelectionModel().getSelectedItem();
                    String relvatePath = StringUtils.getRelativePath(explorerController.gitFolder.getAbsolutePath(), item.getFullPath());

                    ArrayList<ExplorerItem> explorerItems = explorerController.getAllFileStatus(relvatePath);
                    fileExplorerTableView.getItems().addAll(explorerItems);
                } catch (GitAPIException ex) {
                    Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
    }

    class ExplorerController
    {

        Git gitDB = null;
        Collection<String> added;
        Collection<String> changed;
        Collection<String> removed;
        Collection<String> modified;
        Collection<String> missing;
        Collection<String> untracked;
        Collection<String> unmerged;
        Collection<String> unchanged;
        Collection<String> toBeCommitted;

        private final File gitFolder;

        public ExplorerController(final File folder)
        {
            this.gitFolder = folder;
            try
            {
                gitDB = Git.open(gitFolder);
            } catch (IOException ex)
            {
                if (ex instanceof RepositoryNotFoundException)
                {
                    Logger.getLogger(MainWnd.class.getName()).log(Level.INFO, null, ex);
                    try {
                        gitDB = Git.init().setDirectory(gitFolder).call();
                    } catch (GitAPIException ex1) {
                        Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        }

        public ArrayList<ExplorerItem> getAllFileStatus(String path) throws GitAPIException, IOException
        {      
            IndexDiff indexDiff = new IndexDiff(gitDB.getRepository(), Constants.HEAD, new FileTreeIterator(gitDB.getRepository()));
            indexDiff.setFilter(OneDepthPathFilter.create(path));
            indexDiff.diff();
            Status status = new Status(indexDiff);
            added = status.getAdded();
            changed = status.getChanged();
            removed = status.getRemoved();
            modified = status.getModified();
            missing = status.getMissing();
            untracked = status.getUntracked();
            unmerged = status.getConflicting();
            ArrayList<ExplorerItem> items = new ArrayList<>();
            fillExplorerItem(added, ExplorerItem.STATE_ADDED, items);
            fillExplorerItem(changed, ExplorerItem.STATE_CHANGED, items);
            fillExplorerItem(removed, ExplorerItem.STATE_REMOVED, items);
            fillExplorerItem(modified, ExplorerItem.STATE_MODIFIED, items);
            fillExplorerItem(missing, ExplorerItem.STATE_MISSED, items);
            fillExplorerItem(untracked, ExplorerItem.STATE_UNTRACKED, items);
            fillExplorerItem(unmerged, ExplorerItem.STATE_UNMERGED, items);


            ObjectId head = gitDB.getRepository().resolve(Constants.HEAD);

            RevWalk rw = new RevWalk(gitDB.getRepository());
            RevCommit commit = rw.parseCommit(head);
            rw.dispose();
            
            final TreeWalk tw = new TreeWalk(gitDB.getRepository());
            tw.addTree(commit.getTree());
            tw.setFilter(OneDepthPathFilter.create(path));
            tw.setRecursive(true);

            if (unchanged == null)
            {
                unchanged = new ArrayList<>();
            }else
            {
                unchanged.clear();
            }
            while (tw.next())
            {
                unchanged.add(tw.getPathString());
            }

            fillExplorerItem(unchanged, ExplorerItem.STATE_UNCHANGED, items);
            return items;
        }

        private void fillExplorerItem(final Collection<String> stateArray, final String state, ArrayList<ExplorerItem> items)
        {
            for (String path: stateArray)
            {
                File f = new File(gitFolder.getAbsolutePath(), path);
                items.add(new ExplorerItem(f.getName(), state, path, f.lastModified(), f.length()));
            }
        }

        public void commitFiles(final List<ExplorerItem> files)
        {
            for (ExplorerItem item: files)
            {
                gitDB.add().addFilepattern(item.getDir());
            }
            try {
                gitDB.commit().setMessage(Calendar.getInstance().getTime().toString()).call();
            } catch (GitAPIException ex) {
                Logger.getLogger(MainWnd.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    private ExplorerController explorerController;
    private void handleAddAction()
    {
        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle("Choose a folder for backup");
        File folder = chooser.showDialog(null);
        if (folder == null)
        {
            return;
        }
        explorerController = new ExplorerController(folder);
        directoryTreeViewController.getItems().add(new FilePathTreeItem(folder.toPath(), false));
    }

    private void handleCommitAction()
    {
        Object obj[] = fileExplorerTableView.getSelectionModel().getSelectedItems().toArray();
        List<ExplorerItem> items = SxUtilities.<ExplorerItem>castArray(obj);
        explorerController.commitFiles(items);
    }

    private void setOnMenuHandler()
    {
        dirAddMenu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event)
            {
                handleAddAction();
            }
        });

        btnAdd.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event)
            {
                handleAddAction();
            }
        });

        btnCommit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event)
            {
                handleCommitAction();
            }
        });
        
    }
}
