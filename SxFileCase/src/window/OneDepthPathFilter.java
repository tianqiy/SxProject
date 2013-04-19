/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package window;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 *
 * @author tyang
 */
public class OneDepthPathFilter extends TreeFilter
{
    public static OneDepthPathFilter create(String path)
    {
        while (path.endsWith("/")) //$NON-NLS-1$
        {
            path = path.substring(0, path.length() - 1);
        }
        return new OneDepthPathFilter(path);
    }

    final String pathStr;
    final byte[] pathRaw;
    final int depth;

    private OneDepthPathFilter(final String s)
    {
        pathStr = s;
        pathRaw = Constants.encode(pathStr);
        depth = pathStr.split("/").length;
    }

    /** @return the path this filter matches. */
    public String getPath()
    {
        return pathStr;
    }

    private int getRawMode(final TreeWalk walker)
    {
        int i = 0;
        for (; i < walker.getTreeCount(); i++)
        {
            if (walker.getRawMode(i) != 0)
            {
                break;
            }
        }
        return walker.getRawMode(i);
    }
    @Override
    public boolean include(final TreeWalk walker)
    {
        if (pathStr.isEmpty())
        {
            walker.setRecursive(false);
            int mode = getRawMode(walker);
            if ((FileMode.TYPE_FILE & mode) != FileMode.TYPE_FILE)
            {
                return false;
            }
            return true;
        }
        boolean ret = walker.isPathPrefix(pathRaw, pathRaw.length) == 0;
        if (ret)
        {
            walker.getPathString();
            walker.getNameString();
            walker.isSubtree();
            if (walker.getDepth() == depth)
            {
                int mode = getRawMode(walker);
                if ((FileMode.TYPE_FILE & mode) != FileMode.TYPE_FILE)
                {
                    walker.setRecursive(false);
                    return false;
                }
            }
            if (walker.getDepth() > depth)
            {
                    walker.setRecursive(false);
                    return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean shouldBeRecursive()
    {
        for (final byte b : pathRaw)
        {
            if (b == '/')
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public OneDepthPathFilter clone()
    {
        return this;
    }

    @SuppressWarnings("nls")
    public String toString()
    {
        return "PATH(\"" + pathStr + "\")";
    }
}
