package starjava;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import utility.Utility;

/**
 * The App Monitor is a floating dialog that shows an app's state updating in
 * real time. The user can read, set, and graph agent properties and variables.
 * 
 */
public class AppMonitor extends JDialog {

	private static final long serialVersionUID = 1L;

	// Agent Monitor Support
	private AppMonitorObserver observer;
	private Object lock;
	private Timer timer;
	private NumberFormat format = new DecimalFormat();

	// GUI Constants
	private static final int FONTSIZE = 2;
	private final int FIXEDFONTSIZE;
	// red

	// GUI elements
	private JPanel panel;
	private JTextPane text;

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
	public AppMonitor(Frame owner, AppMonitorObserver observer, Object lock) {
		super(owner);
		this.observer = observer;
		this.lock = lock;
		format.setMaximumFractionDigits(3);
		FIXEDFONTSIZE = FONTSIZE + getFont().getSize();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		text = new JTextPane();
		text.setPreferredSize(new Dimension(200,200));
	}

	/**
	 * 
	 * @param c
	 *            show monitor next to parent monitor or at mouse position if
	 *            null
	 * @return true if new monitor is shown
	 */
	private boolean showAppMonitor() {
		addWindowListener(new WindowAdapter() {
			// Stop monitoring a particular agent when the monitor is closed,
			// unless the agent is dead, in which case the agent may have been
			// reincarnated
			// and monitored by a different monitor.
			public void windowClosed(WindowEvent e) {
				AppMonitor.this.timer.stop();
				dispose();
			}
		});

		setVisible(true);
		toFront();
		return true;
	}

	/**
	 * Initializes the GUI elements of the AgentMonitor. (Invoke on the Swing
	 * thread.)
	 * 
	 * @return false if a live monitor for the who number already exists
	 */
	public boolean init() {

		panel = new JPanel();
		panel.add(text);

		
		// Set a timer delay for retrieving updates
		timer = new Timer(250, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				synchronized (lock) {
					AppMonitor.this.updateFields();
//				}
			}
		});

//		synchronized (lock) {
			updateFields();
			setTitle("Application Monitor");
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
		showAppMonitor();
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

	private void updateFields() {
		try {
			if (observer != null) {

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(os);
				observer.outputStatusInfo(ps);

				byte[] charData = os.toByteArray();
				String str;

				str = new String(charData, "UTF-8");

				text.setText(str);

			} else {
				text.setText("A problem occured");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
