/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package window.utilities;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.HashMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;


/**
 *
 * @author tyang
 */
public class IconReader
{
//    private static HashMap<ExtSizePair, Image> bufferedFileIcon = new HashMap<>();
    public static Image getIcon(String filename, boolean largeIcon)
    {
        String parts[] = filename.split("\\.");
//        ExtSizePair tmp;
//        if(parts.length > 1)
//        {
//            tmp = new ExtSizePair(parts[parts.length - 1], largeIcon);
//        }else
//        {
//            tmp = new ExtSizePair("", largeIcon);
//        }
//        if(bufferedFileIcon.containsKey(tmp))
//        {
//            return bufferedFileIcon.get(tmp);
//        }

        IntByReference width = new IntByReference();
        IntByReference height = new IntByReference();
        PointerByReference pt = new PointerByReference();
        NativeLong size = ICONReaderLibrary.INSTANCE.ReadFileIcon(filename, largeIcon, pt, width, height);

        Image image = MemoryImageToFXImage(pt, width.getValue(), height.getValue(), size.longValue());

        ICONReaderLibrary.INSTANCE.FreeImageBuffer(pt.getValue());
        //bufferedFileIcon.put(tmp, image);
        return image;
    }

    private static Image bufferedNormalFolderIcon = null;
    private static Image bufferedDriveIcon = null;
    public static Image getFolderIcon(String filename, boolean largeIcon)
    {
        if (filename == null) 
        {
            filename = new String();
        }
        boolean isDrive = false;
        if (filename.endsWith(":") || filename.endsWith(":\\") )
        {
            isDrive = true;
            if (bufferedDriveIcon != null)
            {
                return bufferedDriveIcon;
            }
        }

        if (!isDrive && bufferedNormalFolderIcon != null)
        {
            return bufferedNormalFolderIcon;
        }
        IntByReference width = new IntByReference();
        IntByReference height = new IntByReference();
        PointerByReference pt = new PointerByReference();
        NativeLong size = ICONReaderLibrary.INSTANCE.ReadFolderIcon(filename, largeIcon, pt, width, height);

        Image image = MemoryImageToFXImage(pt, width.getValue(), height.getValue(), size.longValue());

        ICONReaderLibrary.INSTANCE.FreeImageBuffer(pt.getValue());
        if (isDrive)
        {
            bufferedDriveIcon = image;
        }else
        {
            bufferedNormalFolderIcon = image;
        }
        return image;
    }

    private static Image MemoryImageToFXImage(PointerByReference memory, int width, int height, long size)
    {
        int buf[] = memory.getValue().getIntArray(0, (int)size / 4);
        MemoryImageSource mis = new MemoryImageSource(width, height, buf, 0, width);
        final java.awt.Image _image = java.awt.Toolkit.getDefaultToolkit().createImage(mis);

        BufferedImage b_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        b_img.getGraphics().drawImage(_image, 0, 0, null);

        Image image = SwingFXUtils.toFXImage(b_img, null);

        //TODO: find another way to convert it.
//        byte b[] = pt.getValue().getByteArray(0, (int)size.longValue());
//        ByteArrayInputStream bais = new ByteArrayInputStream(pt.getValue().getByteArray(0, (int)size.longValue()));
//        BufferedImage b_img = ImageIO.read(bais);
//        FileInputStream fis = new FileInputStream("C:/1.ico");
//        byte b2[] = new byte[fis.available()];
//        fis.read(b2, 0, fis.available());
//        ByteArrayInputStream bais2 = new ByteArrayInputStream(b2);
//        Image image = new Image(bais2);

        return image;
    }


    public interface ICONReaderLibrary extends StdCallLibrary
    {
	public static final String JNA_LIBRARY_NAME = "IconReader";
	public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(ICONReaderLibrary.JNA_LIBRARY_NAME);
	public static final ICONReaderLibrary INSTANCE = (ICONReaderLibrary)Native.loadLibrary(JNA_LIBRARY_NAME, ICONReaderLibrary.class, new HashMap<String, String>(){{
                put("FreeImageBuffer","_FreeImageBuffer@4");
                put("ReadFileIcon","_ReadFileIcon@20");
                put("ReadFolderIcon","_ReadFolderIcon@20");
            }});

        //public static final Shell32Library SYNCINSTANCE = (Shell32Library)Native.synchronizedLibrary(INSTANCE);

	NativeLong ReadFileIcon(String filename, boolean largeIcon, PointerByReference imageBuffer, IntByReference bmWidth, IntByReference bmHeight);

	NativeLong ReadFolderIcon(String filename, boolean largeIcon, PointerByReference imageBuffer, IntByReference bmWidth, IntByReference bmHeight);

	void FreeImageBuffer(Pointer imageBuffer);
    }
}
class ExtSizePair
{
    public String extension;
    public boolean isLarge;
    public ExtSizePair(String ext, boolean large)
    {
        extension = ext;
        isLarge = large;
    }

    @Override
    public int hashCode()
    {
        return (extension + Boolean.toString(isLarge)).hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }
        if(getClass() == obj.getClass())
        {
            ExtSizePair tmp = (ExtSizePair)obj;
            if (tmp.extension.equals(this.extension) && tmp.isLarge == this.isLarge)
            {
                return true;
            }
        }
        return false;
    }

}