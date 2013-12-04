package torusworld.gui;


public class GUITextButton extends GUIButton
{
    public int vertMargin, horizMargin;
    static public float alphaUnpressed = 0.5f, alphaHover = 0.72f, alphaPressed = 0.95f;
    
    private GUITextManager textManager;
    private String text;
    
    public GUITextButton(int _x, int _y, String _text, int actionType)
    {
        super(_x, _y, 0, 0, actionType);
        text = _text;
        textManager = new GUITextManager();
    }
    
    public GUITextButton(String _text, int actionType)
    {
        this(0, 0, _text, actionType);
    }
        
    public void setText(String _text)
    {
        text = _text; 
        width = textManager.getTextWidth(text) + 2 * horizMargin;
    }
        
    public void setTextSize(GUITextManager.TextSize size)
    {
        textManager.setSize(size);
        int text_width = textManager.getTextWidth(text);
        int text_height = textManager.getTextHeight();
        vertMargin = text_height / 3;
        horizMargin = text_height;
        width = text_width + 2 * horizMargin;
        height = text_height + 2 * vertMargin;
    }
    
    public void draw(boolean hover, boolean pressed)
    {
        float alpha;
        if (hover) 
            alpha = alphaHover;
        else
            alpha = pressed ? alphaPressed : alphaUnpressed;
        
        GUI.gl.glColor4f(0, 0, 0, 0.5f * alpha);
        GUI.gl.glRecti(x, y+1, x+width, y+height-1);
        GUI.gl.glRecti(x+1, y, x+width-1, y+height);
        
        // width can be arbitrary (to allow resizing buttons), and the text goes in the middle
        int xpos = x + (width - textManager.getTextWidth(text)) / 2;
        textManager.displayText(xpos, y + vertMargin - 1, text);
        if (pressed)
        {
            textManager.setColor(1, 1, 1, 0.5f);
            textManager.displayText(xpos+1, y + vertMargin - 1, text);
            textManager.setColor(1, 1, 1, 1.f);
        }
    }
}
