/*
javafxfilebrowsedemo - Demo application for browsing files in a JavaFX TreeView
Copyright (C) 2012 Hugues Johnson

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package window;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.eclipse.jgit.lib.Constants;
import window.utilities.IconReader;

public class FilePathTreeItem extends TreeItem<String>
{
    //this stores the full path to the file or directory

    public FilePathTreeItem()
    {
        super();
    }

    private static FilePathTreeItem FAKE_ITEM = new FilePathTreeItem();
    public boolean isOpened = false;
    public FilePathTreeItem(final Path file, final boolean showFile)
    {
        super(file.toString());
        this.fullPath = file.toString();
        if (file.getFileName().equals(Constants.DOT_GIT))
        {
            return;
        }
        
        //test if this is a directory and set the icon
        if(Files.isDirectory(file))
        {
            this.isDirectory = true;
            this.setGraphic(new ImageView(IconReader.getFolderIcon(this.fullPath,false)));
        }else if (showFile)
        {
            this.isDirectory = false;
            this.setGraphic(new ImageView(IconReader.getIcon(this.fullPath,false)));
        }
        
        if (this.isDirectory)
        {
            try {
                DirectoryStream<Path> dir = Files.newDirectoryStream(file);

                for(Path subfile : dir)
                {
                    if(showFile || Files.isDirectory(subfile) && !Files.isHidden(subfile))
                    {
                        this.getChildren().add(FAKE_ITEM);
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(FilePathTreeItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //set the value
        if(!fullPath.endsWith(File.separator))
        {
            //set the value (which is what is displayed in the tree)
            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if(indexOf > 0){
                this.setValue(value.substring(indexOf + 1));
            }else{
                this.setValue(value);
            }
        }
            
        this.addEventHandler(TreeItem.branchExpandedEvent(),new EventHandler(){
            @Override
            public void handle(Event e){
                FilePathTreeItem source = (FilePathTreeItem)e.getSource();
//                if(source.isDirectory()&&source.isExpanded()){
//                    ImageView iv=(ImageView)source.getGraphic();
//                    iv.setImage(folderExpandImage);
//                }
                try{
                    if (isOpened)
                    {
                        return;
                    }
                    isOpened = true;
                    if (source.getChildren().get(0) == FAKE_ITEM)
                    {
                        source.getChildren().clear();
                    }
                    Path path = Paths.get(source.getFullPath());
                    BasicFileAttributes attribs = Files.readAttributes(path,BasicFileAttributes.class);
                    if(attribs.isDirectory())
                    {
                        DirectoryStream<Path> dir = Files.newDirectoryStream(path);
                        for(Path file: dir)
                        {
                            if(showFile || Files.isDirectory(file) && !file.toFile().isHidden())
                            {
                                FilePathTreeItem treeNode = new FilePathTreeItem(file, showFile);
                                source.getChildren().add(treeNode);
                            }
                        }
                    }

                }catch(IOException x){
                    x.printStackTrace();
                }
            }   
        });

        this.addEventHandler(TreeItem.branchCollapsedEvent(),new EventHandler(){
            @Override
            public void handle(Event e){
//                FilePathTreeItem source=(FilePathTreeItem)e.getSource();
//                if(source.isDirectory()&&!source.isExpanded()){
//                    ImageView iv=(ImageView)source.getGraphic();
//                    iv.setImage(folderCollapseImage);
//                }
            }
        });
    }
    
    private String fullPath;
    public String getFullPath()
    {
        return(this.fullPath);
    }
    
    private boolean isDirectory;
    public boolean isDirectory()
    {
        return(this.isDirectory);
    }
}
