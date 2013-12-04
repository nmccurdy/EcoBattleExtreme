package torusworld;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;

import javax.media.opengl.GLAutoDrawable;

import starlogoc.StarLogo;

/** Class for managing keyboard input and passing the data to the VM */
public class KeyboardInput implements KeyListener {
    /**
     * Array of keys StarLogo will listen for.  This array must be synchronized with
     * the list of constants in turtle.h<br>
     *
     * 0-25: A through Z<br>
     * 26-35: 0 through 9<br>
     * 36: Left arrow<br>
     * 37: Right arrow<br>
     * 38: Up arrow<br>
     * 39: Down arrow<br>
     * 40: Space bar<br>
     */
    private static final int[] keysUsed =
    {KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E,
     KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J,
     KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O,
     KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S, KeyEvent.VK_T,
     KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y,
     KeyEvent.VK_Z,
     
     KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
     KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9,
     
     KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
     KeyEvent.VK_SPACE};
    
    /**
     * Mapping of virtual key codes to our order.
     * Inverse table of <code>keysUsed</code>.
     * */
    private int[] keyIndices;
    
    /** Buffer storing state of all keys; passed to the VM */
//    private ByteBuffer keyboardData;
    
    /**
     *This takes the value of the sliderspeed and gives the multiple of speed to use as the delay
     *
     *The lowest values are odd because the vm's sensitivity is really low
     */
    private double[] sliderSpeedToMultiplierMap={
        0, 300, .1, .26, .48, 0.66, 0.83, 1.0, 1.3333, 1.6667, 2.0, 5.0, 10.0, 300
    };
    
    /** Find keyboard and downloads key information to the VM */
    public KeyboardInput(GLAutoDrawable drawable) {
        //drawable.addKeyListener(this);
        
        keyIndices = new int[256];
        
        for(int i = 0; i < 256; i++)
            keyIndices[i] = -1;
        
        for(int i = 0; i < keysUsed.length; i++)
            keyIndices[keysUsed[i]] = i;
        
//        keyboardData = ByteBuffer.allocateDirect(keysUsed.length);
        
//        StarLogo.setKeyboardData(keyboardData);
    }
    
    public void keyPressed(KeyEvent e) {
//        int index = -1;
//        
//        if(e.getKeyCode() >= 0 && e.getKeyCode() < 256)
//            index = keyIndices[e.getKeyCode()];
//        
//        if(index >= 0)
//            keyboardData.put(keyIndices[e.getKeyCode()], (byte)1);
    }
    
    public void keyReleased(final KeyEvent e) {
//        Runnable invokeAfterDelay=new Runnable(){
//            public void run(){
//                try {
//                    Thread.sleep( (long)
//                            (150 / sliderSpeedToMultiplierMap[StarLogo.getSpeedSliderPosition()])
//                            ); //this should fix the unregistered key taps
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//                
//                int index = -1;
//                
//                if(e.getKeyCode() >= 0 && e.getKeyCode() < 256)
//                    index = keyIndices[e.getKeyCode()];
//                
//                if(index >= 0)
//                    keyboardData.put(keyIndices[e.getKeyCode()], (byte)0);
//            }
//        };
//        new Thread(invokeAfterDelay).start();
    }
    
    public void keyTyped(KeyEvent e) {
    }
}