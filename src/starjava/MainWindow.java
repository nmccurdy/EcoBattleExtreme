package starjava;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

//import slcodeblocks.WorkspaceController;
import starlogoc.GameManager;
import torusworld.TorusWorld;

public class MainWindow extends JFrame implements Observer {
	private static final long serialVersionUID = 0;

	// **************
	// GUI COMPONENTS

	// the torus world is contained in a floating frame
	// it is possible to have two torus worlds showing differnet views
	// each has its own toolbar
	// //private JInternalFrame outputFrame;
	// private JDialog outputFrame;

	private JPanel renderingPanel;
	public JTabbedPane otherPanel;

	private JSplitPane outputSplitPane = null;

	/** the orientation of the RuntimeWorkspace vertical or horizontal */
	private boolean runtimeVerticalOrientation = false;

	private int controlPanelRightWidth;

	private int controlPanelBottomHeight;

	public MainWindow() {
		super();
		this.setIconImage(this.getToolkit().createImage(
				this.getClass().getResource("starlogotng.png")));

		GameManager.getGameManager().addCameraViewListener(this);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// SJAnimator.stop();
				// ugly, but not sure how else to get everything stopped.
				// stopping the animator doesn't kill all threads.
				System.exit(0);
				super.windowClosing(e);
			}

		});

		// outputFrame = new JFrame();
		// outputFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// outputFrame.addNotify();
		// outputFrame.setBounds(805, 0, 400, 655);

		renderingPanel = new JPanel(new BorderLayout());
		renderingPanel.setPreferredSize(new Dimension(400, 400));

		// TODO: this line is to fix a bug with windows XP where background
		// colors for tabs don't get rendered properly. Remove this line when
		// Java gets updated to fix this issue.
		UIManager.put("TabbedPaneUI",
				"javax.swing.plaf.basic.BasicTabbedPaneUI");

		otherPanel = new JTabbedPane();
		otherPanel.setBackground(Color.DARK_GRAY);
		otherPanel.setForeground(Color.WHITE);

		initOutputSplitPane();
		this.getContentPane().add(outputSplitPane);

	}

	/**
	 * Setup the split pane and setup a menu item to change its orientation JBT
	 * - these functions could be separated or the procedure renamed
	 */
	private void initOutputSplitPane() {
		outputSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				renderingPanel, otherPanel);
		otherPanel.setMinimumSize(new Dimension(100, 100));

		outputSplitPane.setDividerSize(outputSplitPane.getDividerSize() + 1);
		outputSplitPane.setOneTouchExpandable(true);
		outputSplitPane.setResizeWeight(1); // Set top/left component to get
											// space on re-size

		updateRuntimeDivider();

		outputSplitPane.addPropertyChangeListener(
				JSplitPane.DIVIDER_LOCATION_PROPERTY,
				new PropertyChangeListener() {
					boolean updatefirst = true;

					public void propertyChange(PropertyChangeEvent pce) {
						if (updatefirst) {
							// JBT TODO somewhere the Divider Location is
							// getting reset,
							// This ensures that the first time the
							// propertyChange occurs the divider
							// location is set to where we want...
							updateRuntimeDividerLocation();
							updatefirst = false;
						} else {
							// NJM
							// WorkspaceController.getObserver().markChanged();
						}

						int location = outputSplitPane.getDividerLocation();
						if (location >= 0) {
							if (runtimeVerticalOrientation) {
								setControlPanelRightWidth(outputSplitPane
										.getWidth()
										- location);
							} else {
								setControlPanelBottomHeight(outputSplitPane
										.getHeight()
										- location);
							}
						}
					}
				});

	}

	/**
	 * Causes the split pane to be split vertically with spaceland above the
	 * RuntimeWorkspace and causes the RuntimeWorkspace to be placed into a
	 * horizontal orientation
	 */
	private void makeOutputSplitPaneVertical() {
		if (runtimeVerticalOrientation == true) {
//			WorkspaceController.getObserver().markChanged();
			runtimeVerticalOrientation = false;
		}

		if (outputSplitPane != null)
			updateOrientation();
	}

	/**
	 * Causes the split pane to be split horizontally with spaceland to the left
	 * of RuntimeWorkspace and causes the RuntimeWorkspace to be placed into a
	 * vertical orientation
	 */
	private void makeOutputSplitPaneHorizontal() {
		if (runtimeVerticalOrientation == false) {
//			WorkspaceController.getObserver().markChanged();
			runtimeVerticalOrientation = true;
		}

		if (outputSplitPane != null)
			updateOrientation();
	}

	/**
	 * updateOrientation updates outputSplitPane, menuItem, divider, and
	 * RuntimeWorkspace
	 */
	private void updateOrientation() {
		if (runtimeVerticalOrientation) {
			outputSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		} else {
			outputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		}

		updateRuntimeDividerLocation();

		// if (getRuntimeWorkspace() != null) {
		// getRuntimeWorkspace().setVerticalOrientation(runtimeVerticalOrientation);
		// getRuntimeWorkspace().updateRuntimeWorkspace();
		// }
	}

	/**
	 * Updates the location of the divider based on the desired width or height
	 * from the RuntimeWorkspace
	 */
	public void updateRuntimeDividerLocation() {
		if (runtimeVerticalOrientation) {
			outputSplitPane.setDividerLocation(outputSplitPane.getWidth()
					- getControlPanelRightWidth());
		} else {
			outputSplitPane.setDividerLocation(outputSplitPane.getHeight()
					- getControlPanelBottomHeight());
		}
	}

	/**
	 * updates the state of the divider based on current properties of the
	 * split, and preferred sizes
	 */
	public void updateRuntimeDivider() {
		if (runtimeVerticalOrientation) {
			makeOutputSplitPaneHorizontal();
		} else {
			makeOutputSplitPaneVertical();
		}
	}

	/**
	 * Sets the titles of the StarLogoBlocks and SpaceLand windows
	 */
	public void setTitle(String s) {
		if (s == null || s.length() == 0) {
			s = "nada";
		}
		setTitle("StarJava TNG: SpaceLand - " + s);
	}

	/**
	 * Shows and requests focus for the SpaceLand Window
	 */
	public void showOutputFrame() {
		setVisible(true);
		requestFocus();
		renderingPanel.requestFocus();
	}

	/**
	 * Create SpaceLand
	 */
	public void makeTorusWorldWindow(TorusWorld torus) {
		renderingPanel.add(torus, BorderLayout.CENTER);

		addKeyListener(torus);
		renderingPanel.addKeyListener(torus);
	}

	/**
	 * Necessary for Observer interface
	 */
	public void update(Observable observable, Object o) {
	}

	/**
	 * get the desired Width of the control panel when split horizontally
	 * 
	 * @return the controlPanelRightWidth
	 */
	public int getControlPanelRightWidth() {
		return controlPanelRightWidth;
	}

	/**
	 * set the desired Width of the control panel when split horizontally
	 * 
	 * @param controlPanelRightWidth
	 *            the controlPanelRightWidth to set
	 */
	public void setControlPanelRightWidth(int controlPanelRightWidth) {
		this.controlPanelRightWidth = controlPanelRightWidth;
	}

	/**
	 * get the desired Height of the control panel when split vertically
	 * 
	 * @return the controlPanelBottomHeight
	 */
	public int getControlPanelBottomHeight() {
		return controlPanelBottomHeight;
	}

	/**
	 * set the desired Height of the control panel when split vertically
	 * 
	 * @param controlPanelBottomHeight
	 *            the controlPanelBottomHeight to set
	 */
	public void setControlPanelBottomHeight(int controlPanelBottomHeight) {
		this.controlPanelBottomHeight = controlPanelBottomHeight;
	}

	/**
	 * @return the runtime workspace Vertical Orientation
	 */
	public boolean isRuntimeVerticalOrientation() {
		return runtimeVerticalOrientation;
	}

	/**
	 * @param set
	 *            the runtime Vertical Orientation (vertical==true)
	 */
	public void setRuntimeVerticalOrientation(boolean runtimeVerticalOrientation) {
		this.runtimeVerticalOrientation = runtimeVerticalOrientation;
	}
}
