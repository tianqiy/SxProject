/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package window.utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import javafx.collections.FXCollections;

/**
 *
 * @author tyang
 */
public class SxUtilities
{

    public static String humanReadableByteCount(long bytes, boolean si)
    {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
        {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static class PrimitiveHolder<T>
    {
        public T value;

        public PrimitiveHolder()
        {
        }

        public PrimitiveHolder(T value)
        {
            this.value = value;
        }
    }
    
    public static <T> ArrayList castArray(Object[] array)
    {
        ArrayList<T> list = new ArrayList();
        for( Object o : array)
        {
            list.add((T)o);
        }
        return list;
    }
}
