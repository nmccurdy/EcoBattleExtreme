package torusworld;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.io.File;
import java.io.FilenameFilter;
import javax.media.opengl.GL;
import javax.imageio.ImageIO;
import java.util.prefs.Preferences;
import java.awt.FileDialog;
import java.awt.Frame;
import utility.Utility;


public class SLScreenShot
{
    private static FilenameFilter filter = new FilenameFilter()
    {
        public boolean accept(File file, String name)
        {
            return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp");
        }
    };
    
    private static String suffix = "";

    private static File getFile(Frame parent)
    {
        FileDialog d = new FileDialog(parent,
                "Save Image (use extension jpg, png, or bmp)...", FileDialog.SAVE);

        d.setFilenameFilter(filter);

        Preferences prefs = Preferences.userNodeForPackage(SLScreenShot.class);

        // we can get the current directory from the preferences
        String lastdir = prefs.get("LAST_OPENED_DIRECTORY", null);
        if (lastdir != null)
            d.setDirectory(lastdir);

        if (!Utility.macosxp)
            d.setFile("*.jpg");
        d.setVisible(true);
        // save current working directory in preferences
        String name = d.getFile();
        if (name == null)
            return null;
        prefs.put("LAST_OPENED_DIRECTORY", d.getDirectory());
        if (name.indexOf('.') < 0) name += ".jpg"; // add jpg extension by default
            
        suffix = name.substring(name.lastIndexOf('.') + 1);
        suffix.toLowerCase();
        return new File(d.getDirectory(), name);
    }

    /**
     * Save the current gl screen to a file
     * @param gl
     * @param width
     * @param height
     * @param parent
     */
    public static void SaveScreen(BufferedImage image, Frame parent) {
        File file = getFile(parent);
        if (file == null) return;
        
        try
        {
            ImageIO.write(image, suffix, file);
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an image of the gl context of width and height dimensions
     * @param gl
     * @param width
     * @param height
     * @return
     */
	public static BufferedImage makeImage(GL gl, int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
        gl.glViewport(0, 0, width, height);
        buffer.rewind();
        gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);

        buffer.rewind();
        int[] pixels = new int[width * height];
        for (int i = 0; i < width * height; i++)
        {
            int r = ((int) buffer.get()) & 0xFF;
            int g = ((int) buffer.get()) & 0xFF;
            int b = ((int) buffer.get()) & 0xFF;
            buffer.get();
            pixels[i] = (r << 16) | (g << 8) | b;
        }
        // set the pixels to the image, flipped (using big offset and negative spansize)
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, pixels, width*(height-1), -width);

        return image;
	}
}
