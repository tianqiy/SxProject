/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package window.structure;

import java.text.SimpleDateFormat;
import java.util.Date;
import window.utilities.SxUtilities;

/**
 *
 * @author tyang
 */
public class ExplorerItem
{
    public static String STATE_ADDED = "Added";
    public static String STATE_CHANGED = "Changed";
    public static String STATE_REMOVED = "Removed";
    public static String STATE_MODIFIED = "Modified";
    public static String STATE_MISSED = "Missed";
    public static String STATE_UNTRACKED = "Untracked";
    public static String STATE_UNMERGED = "Unmerged";
    public static String STATE_UNCHANGED = "Unchanged";

    private String filename;
    private String state;
    private String dir;
    private long date;
    private long size;

    public ExplorerItem()
    {
    }

    public ExplorerItem(String filename, String state, String dir, long date, long size)
    {
        this.filename = filename;
        this.state = state;
        this.dir = dir;
        this.date = date;
        this.size = size;
    }

    public final String getFilename()
    {
        return filename;
    }

    public final String getState()
    {
        return state;
    }

    public final String getDir()
    {
        return dir;
    }

    public final String getDate()
    {
        final Date d = new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(d);
    }

    public final String getSize()
    {
        return SxUtilities.humanReadableByteCount(size, false);
    }

    
}
