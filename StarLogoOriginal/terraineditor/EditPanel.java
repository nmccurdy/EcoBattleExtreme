package terraineditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import starlogoc.PatchManager;

public class EditPanel extends JPanel implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 0;
	
	// patch constants
	private final int NUM_PATCHES = 101;
	public static final int PPP = 4;

	// tools
	public final int SELECT = 0;
	public final int WALL = 1;
	public final int MOUND = 2;
	public final int LEVEL = 3;
	public final int BREED = 4;
	public final int TRENCH = 5;
	public final int PIT = 6;
	public final int PAINT = 7;
	public final int TEXTURE = 8;

	// views
	public final int COLORS_ONLY = 0;
	public final int HEIGHTS_ONLY = 1;
	public final int COLORS_AND_HEIGHTS = 2;


	// state variables
	private int currentTool = SELECT;
	private int currentView = COLORS_AND_HEIGHTS;
	private int currentTexture = 0;
	private String currentBreed = "Turtle";
	private int currentColor = 55;

	// child classes
	private EditGraphicsCanvas editCanvas;
	private TerrainManager tm;
	private HeightSelector heightSel;

	// Constructor
	public EditPanel(PatchManager pm, HeightSelector hs) {
		super();
		tm = new TerrainManager(pm);
		heightSel = hs;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		editCanvas = new EditGraphicsCanvas();
		add(editCanvas);
		setBorder(BorderFactory.createEmptyBorder(5,0,5,5)); //top,left,bottom,right widths
		setBackground(Color.black);
		setMaximumSize(new Dimension((int) (editCanvas.getSize().getWidth()+5), (int) (editCanvas.getSize().getHeight()+10)));
		setMinimumSize(new Dimension((int) (editCanvas.getSize().getWidth()+5), (int) (editCanvas.getSize().getHeight()+10)));
		setPreferredSize(new Dimension((int) (editCanvas.getSize().getWidth()+5), (int) (editCanvas.getSize().getHeight()+10)));
		editCanvas.addMouseMotionListener(this);
		editCanvas.addMouseListener(this);
		updateDisplayRegion(new Region(0, 0, NUM_PATCHES-1, NUM_PATCHES-1));
		setVisible(true);
	}

	public void setTool(int toolNum) {
		currentTool = toolNum;
	}

	public void setView(int viewNum) {
		currentView = viewNum;
		updateDisplayRegion(new Region(0, 0, NUM_PATCHES-1, NUM_PATCHES-1));
	}

	public void setBreed(String breed) {
		currentBreed = breed;
	}

	public void setColor(int color) {
		currentColor = color;
		tm.setCurColor(color);
	}

	public void setTexture(int texNum) {
		currentTexture = texNum;
	}
	
	// MouseMotionListener methods
	private int lastMouseX = 0, lastMouseY = 0;
	public void mouseMoved(MouseEvent e) {
		editCanvas.unhighlightSquare(lastMouseX, lastMouseY);
		lastMouseX = e.getX()/PPP;
		lastMouseY = e.getY()/PPP;
		if (lastMouseX < 0) {lastMouseX = 0;}
		if (lastMouseY < 0) {lastMouseY = 0;}
		if (lastMouseX >= NUM_PATCHES-1) {lastMouseX = NUM_PATCHES-1;}
		if (lastMouseY >= NUM_PATCHES-1) {lastMouseY = NUM_PATCHES-1;}
		editCanvas.highlightSquare(lastMouseX, lastMouseY);
	}
	
	private int lastDragMouseX = 0, lastDragMouseY = 0;
	private Region selectedRegion = new Region(0,0,0,0);
	private boolean dragging = false;
	public void mouseDragged(MouseEvent e) {
		// only respond to the first mouse button
		if (dragging) {
			lastDragMouseX = e.getX()/PPP;
			lastDragMouseY = e.getY()/PPP;
			if (lastDragMouseX < 0) {lastDragMouseX = 0;}
			if (lastDragMouseY < 0) {lastDragMouseY = 0;}
			if (lastDragMouseX >= NUM_PATCHES-1) {lastDragMouseX = NUM_PATCHES-1;}
			if (lastDragMouseY >= NUM_PATCHES-1) {lastDragMouseY = NUM_PATCHES-1;}
			Region currentRegion = new Region(mouseDownX, mouseDownY, lastDragMouseX, lastDragMouseY);

			// if region has changed, update canvas and selectedRegion variable
			if (!currentRegion.isEquivalentTo(selectedRegion)) {

				// if current region overlaps selected region with a common corner
				if (selectedRegion.sharesCornerWith(currentRegion)) {

					// if the current region is smaller than the selected region in one dimension, 
					// we need to unhighlight the newly unselected part
					if (selectedRegion.isWiderThan(currentRegion)) {
						editCanvas.unhighlightRegion(selectedRegion.subtractX(currentRegion));
					}
					if (selectedRegion.isTallerThan(currentRegion)) {
						editCanvas.unhighlightRegion(selectedRegion.subtractY(currentRegion));
					}

				
					// if the current region is bigger than the selected region in one dimension, 
					// we need to highlight the newly selected part
					if (currentRegion.isWiderThan(selectedRegion)) {
						editCanvas.highlightRegion(currentRegion.subtractX(selectedRegion));
					}
					if (currentRegion.isTallerThan(selectedRegion)) {
						editCanvas.highlightRegion(currentRegion.subtractY(selectedRegion));
					}
					editCanvas.highlightRegion(currentRegion);
				}

					// if no common corner, all the old stuff needs to be unhighlighted,
					// and all new stuff needs to be highlighted
				else {
					editCanvas.unhighlightRegion(selectedRegion);
					editCanvas.highlightRegion(currentRegion);
				}
				selectedRegion = currentRegion;
			}
		}
		else {
			// they must be doing a right-click drag if we get here.
			// just treat it as a mouse move.
			mouseMoved(e);
		}
	}
	
	// MouseListener methods
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
		if (mouseDownX != lastMouseX || mouseDownY != lastMouseY) {
			editCanvas.unhighlightSquare(lastMouseX, lastMouseY);
			lastMouseX = 0;
			lastMouseY = 0;
		}
	}
	
	private int mouseDownX, mouseDownY;
	public void mousePressed(MouseEvent e) {
		// only respond to the first mouse button
		if (e.getButton() == MouseEvent.BUTTON1) {
			editCanvas.unhighlightSquare(lastMouseX, lastMouseY);
			mouseDownX = e.getX()/PPP;
			mouseDownY = e.getY()/PPP;
			lastDragMouseX = e.getX()/PPP;
			lastDragMouseY = e.getY()/PPP;
			dragging = true;
			selectedRegion = new Region(mouseDownX, mouseDownY, lastDragMouseX, lastDragMouseY);
		}
	}
	
	private int mouseUpX, mouseUpY;
	public void mouseReleased(MouseEvent e) {
		// only respond to the first mouse button
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseUpX = e.getX()/PPP;
			mouseUpY = e.getY()/PPP;
			dragging = false;

			//Color pColor;
			switch (currentTool) {
				case SELECT:
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case WALL:
					tm.addRegionHeight(selectedRegion, (heightSel.getHeightValue()+50)/2, true);
					updateDisplayRegion(selectedRegion);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case TRENCH:
					tm.addRegionHeight(selectedRegion, -(heightSel.getHeightValue()+50)/2, true);
					updateDisplayRegion(selectedRegion);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case LEVEL:
					tm.setRegionHeight(selectedRegion, heightSel.getHeightValue(), true);
					updateDisplayRegion(selectedRegion);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case MOUND:
					tm.mound(new Region(mouseDownX, mouseDownY, mouseUpX, mouseUpY), (heightSel.getHeightValue()+50)/2, true);
					updateDisplayRegion(selectedRegion);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case PIT:
					tm.pit(new Region(mouseDownX, mouseDownY, mouseUpX, mouseUpY), (heightSel.getHeightValue()+50)/2, true);
					updateDisplayRegion(selectedRegion);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case BREED:
					editCanvas.unhighlightRegion(selectedRegion);
					tm.addTurtle(mouseUpX, mouseUpY, currentBreed);
					editCanvas.drawTurtles(tm);
					break;
				case PAINT:
					tm.setRegionColor(selectedRegion, currentColor);
					updateDisplayRegion(selectedRegion);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
				case TEXTURE:
					tm.setRegionTexture(selectedRegion, currentTexture);
					editCanvas.unhighlightRegion(selectedRegion);
					break;
			}
		}
	}

	public void updateDisplayRegion(Region r) {
		if (currentView == HEIGHTS_ONLY) {
			editCanvas.fillHeights(r, tm);
		} 
		else if (currentView == COLORS_ONLY) {
			editCanvas.fillColors(r, tm);
		} 
		else if (currentView == COLORS_AND_HEIGHTS) {
			editCanvas.fillColorsAndHeights(r, tm);
		}
	}

	// swaps the current terrain into TorusWorld, and edits the one that was running
	public void setPatchTerrain(int index) {
		tm.setPatchTerrain(index);
		updateDisplayRegion(new Region(0, 0, NUM_PATCHES-1, NUM_PATCHES-1));
	}

	// saves the current terrain to a file
	public void save() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			// check for overwriting existing files
			if (file.exists()) {
				int option = JOptionPane.showConfirmDialog(null,
					"Warning: File already exists.  Overwrite?", "File exists", JOptionPane.YES_NO_OPTION);

				// if they choose not to overwrite, simply return without
				// doing anything to the file
				if (option == JOptionPane.NO_OPTION) 
					return;
					// otherwise delete the file and start over
				else 
					file.delete();
			}

			// save to the file
			tm.save(file);
		}
	}

	// loads a terrain from a terrain file
	public void load() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			tm.load(file);
		}
		
		// update display with new terrain
		updateDisplayRegion(new Region(0, 0, NUM_PATCHES-1, NUM_PATCHES-1));
	}

	public String saveTerrain() {
		return tm.save();
	}

	public boolean loadTerrain(String data) {
		boolean load = tm.load(data);
		updateDisplayRegion(new Region(0,0, NUM_PATCHES-1, NUM_PATCHES-1));
		return load;
	}
}
