package starjava;

//import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import utility.Utility;

/**
 * The Agent Monitor is a floating dialog that shows an agent's state updating
 * in real time. The user can read, set, and graph agent properties and
 * variables.
 * 
 * Note: Once an agent monitor is created, it must be initialized with a call to
 * init(). init() will fail and return false if there is already a monitor open
 * for the given who number.
 */
public class AgentMonitor extends JDialog {

	private static final long serialVersionUID = 1L;
	// Only allow one monitor for each who number.
	// In other words, having multiple agent 0 monitors is NOT fine even if only
	// one of them
	// represents the currently running agent 0, and the others represent
	// previously deceased instances.
	private static Map<Integer, AgentMonitor> openMonitors = new HashMap<Integer, AgentMonitor>();
	// Keep track of which agents have active but hidden monitors following a
	// hatch.
	private static Map<Integer, AgentMonitor> hatchMonitors = new HashMap<Integer, AgentMonitor>();

	// Agent Monitor Support
	private AgentMonitorObserver observer;
	private Object lock;
	private Timer timer;
	private NumberFormat format = new DecimalFormat();

	// Agent Monitor Values
	private int whoNumber;

	// GUI Constants
	private static final int FONTSIZE = 2;
	private final int FIXEDFONTSIZE;
	// red

	// GUI elements
	private JPanel panel;
	private JTextPane text;

	private JButton hatchButton;

	/**
	 * Constructs a new AgentMonitor for a particular agent. AgentMonitor is a
	 * JDialog and should be created on the Swing thread.
	 * 
	 * @param owner
	 *            the parent frame of the AgentMonitor
	 * @param who
	 *            the ID number of the agent to monitor
	 * @param slObserver
	 * @param lock
	 */
	public AgentMonitor(Frame owner, int who, AgentMonitorObserver observer,
			Object lock) {
		super(owner);
		this.whoNumber = who;
		this.observer = observer;
		this.lock = lock;
		format.setMaximumFractionDigits(3);
		FIXEDFONTSIZE = FONTSIZE + getFont().getSize();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		text = new JTextPane();
	}

	/**
	 * 
	 * @param c
	 *            show monitor next to parent monitor or at mouse position if
	 *            null
	 * @return true if new monitor is shown
	 */
	private boolean showAgentMonitor(Component c) {
		// Remove from list of hatch monitors in case it was one
		// hatchMonitors.remove(whoNumber);
		AgentMonitor existingMonitor = openMonitors.get(whoNumber);
		if (existingMonitor != null && existingMonitor.isVisible()) {
			existingMonitor.requestFocus();
			return false;
		}
		openMonitors.put(whoNumber, this);

		addWindowListener(new WindowAdapter() {
			// Stop monitoring a particular agent when the monitor is closed,
			// unless the agent is dead, in which case the agent may have been
			// reincarnated
			// and monitored by a different monitor.
			public void windowClosed(WindowEvent e) {
				disposeMonitor(whoNumber);
			}
		});

//		synchronized (lock) {
			observer.setAgentMonitored(whoNumber, true);
//		}

		// If opened from a hatch indicator, put it next to that monitor
		if (c != null) {
			setLocationRelativeTo(c);
			// If overlapping component, move it to right of component (or left
			// to keep on screen).
			if (getLocation().x >= c.getLocation().x
					&& getLocation().x < c.getLocation().x + c.getWidth()) {
				if (getLocation().x + c.getWidth() + getWidth() > Toolkit
						.getDefaultToolkit().getScreenSize().width)
					setLocation(c.getLocation().x - getWidth(), getLocation().y);
				else
					setLocation(c.getLocation().x + c.getWidth(),
							getLocation().y);
			}
		}
		// otherwise put it at the mouse pointer where the user clicked to
		// select an agent
		else {
			setLocation(MouseInfo.getPointerInfo().getLocation());
			// If off the screen, move it to a different quadrant.
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (getLocation().x + getWidth() > screenSize.width)
				setLocation(getLocation().x - getWidth(), getLocation().y);
			if (getLocation().y + getHeight() > screenSize.height)
				setLocation(getLocation().x, getLocation().y - getHeight());
		}

		setVisible(true);
		toFront();
		return true;
	}

	/**
	 * Disposes an open monitor for a particular who number
	 * 
	 * @param who
	 *            the who number of the agent whose monitor should be disposed
	 * @return true if monitor is disposed successfully
	 */
	public static boolean disposeMonitor(int who) {
		return disposeMonitor(who, false);
	}

	private static boolean disposeMonitor(int who, boolean hatchMonitor) {
		AgentMonitor monitor = openMonitors.get(who);
		if (monitor == null)
			return false;

		if (!hatchMonitor) {
			openMonitors.remove(monitor.whoNumber);
//			synchronized (monitor.lock) {
				monitor.observer.setAgentMonitored(monitor.whoNumber, false);
//			}
			if (hatchMonitors.get(who) == null) {
				monitor.timer.stop();
			}
		}

		monitor.dispose();
		return true;
	}

	/**
	 * Initialize a monitor post construction. This method must be called
	 * following the construction of a new AgentMonitor.
	 * 
	 * @return true if the monitor could be initialized successfully.
	 */
	public boolean init() {
		return init(true);
	}

	/**
	 * Initializes the GUI elements of the AgentMonitor. (Invoke on the Swing
	 * thread.)
	 * 
	 * @return false if a live monitor for the who number already exists
	 */
	private boolean init(boolean showMonitor) {
		AgentMonitor existingMonitor = openMonitors.get(whoNumber);
		if (existingMonitor != null)
			return false;

		// Add shortcut keys
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		// KeyStroke.getKeyStroke(KeyEvent.VK_W,
				// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				KeyStroke.getKeyStroke("ESCAPE"), "Close Monitor");
		getRootPane().getActionMap().put("Close Monitor", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				AgentMonitor.disposeMonitor(whoNumber);
			}
		});

		panel = new JPanel();
		panel.add(text);

		// Set a timer delay for retrieving updates
		timer = new Timer(250, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				synchronized (lock) {
					AgentMonitor.this.updateFields();
//				}
			}
		});

//		synchronized (lock) {
			updateFields();
			setTitle("Monitor " + whoNumber);
//		}

		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// MacOS - Scrollbar always present
		if (Utility.macosxp) {
			scrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			// scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}

		add(scrollPane, BorderLayout.CENTER);

		setFontSize(panel, FIXEDFONTSIZE);

		// setBounds(100, 100, 300, 540);
		pack();
		timer.start();
		if (showMonitor)
			showAgentMonitor(null);
		return true;
	} // init

	private void setFontSize(Component c, int points) {
		Font f = c.getFont();
		// Prevent div by zero exception
		if (!(c instanceof JComboBox) && f.getSize() + points > 0)
			c.setFont(new Font(f.getName(), f.getStyle(), points));

		if (c instanceof Container) {
			Container cont = (Container) c;
			for (int i = 0, n = cont.getComponentCount(); i < n; ++i) {
				setFontSize(cont.getComponent(i), points);
			}
		}
	}

	private void disableMonitor(Component c) {
		timer.stop();
		// liveMonitors.remove(whoNumber);
		text.setText(text.getText() + "\n\nAgent is deceased.  :(");
		// // Make sure all values get reverted when the monitor is disabled.
		// System.out.println(shapeLabel.requestFocusInWindow());
		// KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
		disableMonitorHelper(c);
	}

	private void disableMonitorHelper(Component c) {
		if (!(c instanceof JDialog) && c != hatchButton)
			((Container) c).setEnabled(false);
		if (c instanceof Container) {
			Container cont = (Container) c;
			for (int i = 0, n = cont.getComponentCount(); i < n; ++i) {
				disableMonitorHelper(cont.getComponent(i));
			}
		}
	}

	private void updateFields() {
		try {
			if (observer != null) {

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(os);
				if (observer.outputStatusInfo(whoNumber, ps)) {

					byte[] charData = os.toByteArray();
					String str;

					str = new String(charData, "UTF-8");

					text.setText(str);
				} else {
					// NJM
					// WARNING: This does not really work properly.  Really need to have
					// agentMonitor be a listener on agent death.  It is possible that a
					// new agent is created before we poll the old one.  we'll get the values
					// of the new one without realizing it.
					disableMonitor(null);					
				}
			} else {
				text.setText("A problem occured");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
