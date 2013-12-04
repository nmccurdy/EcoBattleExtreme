package torusworld.gui;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.GLUT;
import java.util.*;
import java.awt.event.MouseEvent;

public class GUI
{
    public static GL gl = null;
    public static GLU glu = null;
    public static GLUT glut = null;
    
    private List<GUIElement> elements = new ArrayList<GUIElement>();
    
    int lastMouseX = -1, lastMouseY = -1;
    
    public GUI(GL _gl, GLU _glu, GLUT _glut)
    {
        gl = _gl;
        glu = _glu;
        glut = _glut;
    }
    
    public void addElements(GUIElement ... elems)
    {
        for (GUIElement elem : elems)
            elements.add(elem);
    }
    
    public void addElements(Collection<GUIElement> elems)
    {
        for (GUIElement elem : elems)
            elements.add(elem);
    }
    
    public boolean handleMouseEvent(MouseEvent e)
    {
        boolean overControl = false;
        for (int i = 0; i < elements.size(); i++)
        {
            GUIElement o = elements.get(i);
            if (!o.isVisible()) continue;
            if (o.isMouseOver(e.getX(), e.getY()))
            {
                overControl = true;
                e.translatePoint(-o.getX(), -o.getY());
                switch (e.getID())
                {
                    case MouseEvent.MOUSE_CLICKED: o.mouseClicked(e); break;
                    case MouseEvent.MOUSE_PRESSED: o.mousePressed(e); break;
                    case MouseEvent.MOUSE_RELEASED: o.mouseReleased(e); break;
                }
                if (!o.isMouseOver(lastMouseX, lastMouseY))
                    o.mouseEntered(e);
                e.translatePoint(+o.getX(), +o.getY());
            }
            else
            {
                if (o.isMouseOver(lastMouseX, lastMouseY))
                {
                    e.translatePoint(-o.getX(), -o.getY());
                    o.mouseExited(e);
                    e.translatePoint(+o.getX(), +o.getY());
                }
            }
        }
        lastMouseX = e.getX(); 
        lastMouseY = e.getY();
        return overControl;
    }
    
    public String getCurrentDescription()
    {
        for (GUIElement elem: elements)
            if (elem instanceof GUIDescription &&
                elem.isMouseOver(lastMouseX, lastMouseY))
                return ((GUIDescription) elem).getDescription();

        return null;
    }
    
    public void draw()
    {
        gl.glDisable(GL.GL_FOG);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisable(GL.GL_TEXTURE_2D);
        
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        for (GUIElement elem : elements)
            if (elem.isVisible())
                elem.draw();

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_CULL_FACE);
    }
}
