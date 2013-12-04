package starjava;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.io.PrintStream;
import java.net.URL;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import layout.SLLoadLevelComponent;
import slcodeblocks.RuntimeSpeedPanel;
import starlogoc.GameManager;
import starlogoc.StarLogo;
import starlogoc.StarLogoCObserver;
import starlogotng.SLURL;
import terraineditor.TerrainManager;
import torusworld.TorusWorld;
import torusworld.Turtles;

public class Application implements SpeedObserver, Executable,
		StarLogoCObserver, AgentMonitorObserver, AppMonitorObserver {

	/** Camera perspectives */
	public static final int AERIAL = 0;
	public static final int AGENT_EYE = 1;
	public static final int AGENT_SHOULDER = 2;

	public static final int MAX_NUM_AGENTS = 4096;

	public final int MAX_XY = 50;
	public final int MIN_XY = -50;

	protected MainWindow mainWindow;
	private TorusWorld tw;
	protected StarLogo sl;
	// protected TurtleManagerJava tManager;
	private PatchManagerJava pManager;
	private TerrainManager terrainManager;

//	private RuntimeWorkspace slrw;

	private final Object lock = new Object();

	protected LinkedList<Executable> executables;
	protected LinkedList<Agent> agents;
	protected HashMap<Integer, Agent> whoAgentMap;

	protected Collisions collisions;

	private boolean firstExecute = true;

	private HashMap<String, Long> shapes;

	private Queue<Integer> availableWhos;

	private Turtles turtles;
	
	private int lastWhoNumber = 0;

	/**
	 * @param args
	 */
	public Application() {

		agents = new LinkedList<Agent>();
		whoAgentMap = new HashMap<Integer, Agent>();
		collisions = new Collisions(this);
		executables = new LinkedList<Executable>();
		shapes = new HashMap<String, Long>();
		availableWhos = new LinkedList<Integer>();

//		for (int i = 0; i < MAX_NUM_AGENTS; i++) {
//			availableWhos.add(i);
//		}

		mainWindow = new MainWindow();
		mainWindow.setSize(800, 600);
		mainWindow.addNotify();

		sl = new StarLogo(null);
		// tManager = new TurtleManagerJava(sl.MAX_TURTLES, sl.MAX_BOUNCES,
		// sl.MAX_SOUNDS, sl);
		// sl.setTurtleManager(tManager);

		sl.addObserver(this);

		pManager = new PatchManagerJava(101, 101, sl);
		sl.setPatchManager(pManager);

		terrainManager = new TerrainManager(pManager);

		sl.setLock(lock);

		// NJM TorusWorld has to be created after setPatchManager is
		// called otherwise it uses the non-java patch manager.

		turtles = new Turtles(sl);
		tw = new TorusWorld(sl, mainWindow, turtles);

		mainWindow.makeTorusWorldWindow(tw);

		mainWindow.setVisible(true);

		mainWindow.setRuntimeVerticalOrientation(false);
		// getMainWindow().setControlPanelRightWidth(vals.controlPanelRightWidth);
		// getMainWindow().setControlPanelBottomHeight(vals.controlPanelBottomHeight);
		mainWindow.updateRuntimeDivider();
		// mainWindow.updateRuntimeWorkspace();

		initAllTabs();

		// app.hideClock();
		// app.hideScore();
		// app.showMiniView();
		// app.resetTime();
		// app.setWhoNumberForCamera(0);
		// app.resetRuntimeWorkspace();

	}

	public void initAllTabs() {
		/*
		 * mainWindow.otherPanel.addChangeListener(new ChangeListener() { //
		 * This method is called whenever the selected tab changes public void
		 * stateChanged(ChangeEvent evt) { JTabbedPane pane =
		 * (JTabbedPane)evt.getSource(); int sel = pane.getSelectedIndex();
		 * if(sel != 0) stopNow(); // stop execution if we're not viewing the
		 * runtime tab } });
		 */
		mainWindow.otherPanel.addTab("Runtime", createRuntimeTab());
		mainWindow.otherPanel.addTab("Levels", new SLLoadLevelComponent(tw));

		// create rest of tabs here....
	}

	public void init() {
		// SLAnimator still gets created, but just isn't started. tw is the one
		// that normally
		// calls init() for the animator, but there's no way to override this
		// behavior in a clean way.
		// Having two animators should be fine.
		SJAnimator.init(tw, sl, tw);
		SJAnimator.addExecutable(this);

		SJAnimator.start();
	}

	private JComponent createRuntimeTab() {
		JComponent runtimePanel = new JPanel(new BorderLayout());

//		slrw = new RuntimeWorkspace();
//		CTracklessScrollPane rwScrollPanel = new CTracklessScrollPane(slrw,
//				ScrollPolicy.VERTICAL_BAR_AS_NEEDED,
//				ScrollPolicy.HORIZONTAL_BAR_AS_NEEDED, 10, CGraphite.blue,
//				RuntimeWorkspace.RUNTIMEBACKGROUNDCOLOR);
//		runtimePanel.add(rwScrollPanel, BorderLayout.CENTER);

		// NJM not needed

		// slrw.addJMenuItem(mainWindow.getMenuItem());
		// BreedManager.setRuntimeWorkspace(slrw);

		// The RuntimeSpeedPanel is the graphic interface for
		// changing the speed values for the VM.
		RuntimeSpeedPanel runtimeSpeedPanel = new RuntimeSpeedPanel(this);
		runtimePanel.add(runtimeSpeedPanel, BorderLayout.NORTH);

		return runtimePanel;
	}

	public int getNextWhoNumber() {
//		return availableWhos.remove();
		
		return lastWhoNumber++;
	}

	public void putWhoNumberInQueue(int who) {
//		availableWhos.add(who);
	}

	@Override
	public void speedChanged(int newSpeedSliderPosition) {
		// 0 1 2 3 4 5 6 7 8 9 10 11
		double[] newSpeed = { 0, 0, 0.5, 1, 2.5, 3.75, 5, 7.5, 10, 25, 50, 500 };

		// check sync with SpeedSlider
		assert newSpeed.length == RuntimeSpeedPanel.MAX_VALUE + 1;
		assert Math.abs(newSpeed[RuntimeSpeedPanel.REALTIME_VALUE] - 5) < 1e-5;

		// pause
		if (newSpeedSliderPosition <= 1) {
			SJAnimator.setPaused(true);
			return;
		} else {
			SJAnimator.setPaused(false);
		}
		if (newSpeedSliderPosition > RuntimeSpeedPanel.MAX_VALUE)
			newSpeedSliderPosition = RuntimeSpeedPanel.MAX_VALUE;

		// speedSliderPosition = newSpeedSliderPosition;

		SJAnimator.setVMrps(newSpeed[newSpeedSliderPosition]);
		// System.out.println(tw.sl.getStarLogoTime());

	}

	public StarLogo getStarLogo() {
		return sl;
	}

	// public TurtleManagerJava getTurtleManager() {
	// return tManager;
	// }

	public AbstractList<Agent> getEntities() {
		return agents;
	}

	public void addCollidableAgent(Agent agent) {
		addDrawableAgent(agent);  // hack to make ecobattle backwards compatible
		if (agent.isAlive()) {
			agents.add(agent);
			whoAgentMap.put(agent.getWho(), agent);
		}
	}

	public void removeCollidableAgent(Agent agent) {
		agents.remove(agent);
		whoAgentMap.remove(agent.getWho());
	}

	public Agent getAgentFromWho(int who) {
		return whoAgentMap.get(who);
	}

	public void addCollision(Agent collider, CollisionHandler handler) {
		collisions.addCollision(collider, handler);
	}

	public void addExecutable(Executable exec) {
		if (exec.isAlive()) {
			executables.add(exec);
		}
	}

	public void removeExecutable(Executable exec) {
		executables.remove(exec);
	}

	public AbstractList<Agent> smell(double radius, Agent smeller,
			SmellHandler smellHandler) {

		LinkedList<Agent> results = new LinkedList<Agent>();

		if (smeller.isAlive()) {
			Iterator<Agent> itr = agents.iterator();

			double x = smeller.getX();
			double y = smeller.getY();

			double r2 = radius * radius;

			while (itr.hasNext()) {
				Agent smellee = itr.next();

				if (smellee.isAlive()) {

					// don't smell ourselves
					if (smellee != smeller) {
						// check to see if smellee is within radius

						if (Math.pow(smellee.getX() - x, 2)
								+ Math.pow(smellee.getY() - y, 2) <= r2) {
							if (smellHandler.smellCondition(smellee)) {
								results.add(smellee);
							}
						}
					}
				}
			}
		}
		return results;
	}

	public void sortByClosestTo(final double x, final double y,
			AbstractList<Agent> list) {
		Collections.sort(list, new Comparator<Agent>() {
			public int compare(Agent a1, Agent a2) {
				double diffX = a1.getX() - x;
				double diffY = a1.getY() - y;

				double diffX2 = a2.getX() - x;
				double diffY2 = a2.getY() - y;

				return Double.compare(diffX * diffX + diffY * diffY, diffX2
						* diffX2 + diffY2 * diffY2);
			}
		});
	}

	public void killAgent(Agent agent) {
		agent.setAlive(false);

		putWhoNumberInQueue(agent.getWho());
		removeCollidableAgent(agent);
		removeExecutable(agent);
		collisions.removeCollisionHandlers(agent);
		// sl.killTurtle(agent.getWho());
		removeDrawableAgent(agent);
	}

	public Collisions getCollisions() {
		return collisions;
	}

	public AbstractList<Agent> getEntitiesCopy() {
		AbstractList<Agent> copy = (AbstractList<Agent>) agents.clone();
		return copy;
	}

	@Override
	public void stepVM() {
		SJAnimator.stepVM();

	}

	@Override
	public void stopNow() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() {

		if (firstExecute) {
			firstExecute = false;
			setup();
		}
		LinkedList<Executable> copy = (LinkedList<Executable>) executables
				.clone();

		for (Executable executable : copy) {
			if (executable.isAlive()) {
				executable.execute();
			}
		}

		collisions.callCollisionCallbacks(0);
	}

	public void setup() {

	}

	/**
	 * This only works if you enable assertions. -ea for VM
	 * 
	 * @param url
	 */
	public void loadTerrain(URL url) {
		SLURL file = new SLURL(url);

		String contents = file.load("UTF-8");

		terrainManager.load(contents);
	}

	public void resetTerrain() {
		pManager.reset();
	}

	public void clearAll() {
		// remove everything we know about entities

		agents = new LinkedList<Agent>();
		collisions = new Collisions(this);
		executables = new LinkedList<Executable>();

		resetTerrain();
	}

	public Color getPatchColor(double x, double y) {
		if (x > MAX_XY) {
			x = MAX_XY;
		}
		if (x < MIN_XY) {
			x = MIN_XY;
		}
		if (y > MAX_XY) {
			y = MAX_XY;
		}
		if (y < MIN_XY) {
			y = MIN_XY;
		}

		int patchX = pManager.getPatchCoordX(x);
		int patchY = pManager.getPatchCoordY(y);

		return pManager.getCurrentTerrain().getColor(patchX, patchY);
	}

	public void setPatchColor(double x, double y, Color color) {
		if (x > MAX_XY) {
			x = MAX_XY;
		}
		if (x < MIN_XY) {
			x = MIN_XY;
		}
		if (y > MAX_XY) {
			y = MAX_XY;
		}
		if (y < MIN_XY) {
			y = MIN_XY;
		}

		int patchX = pManager.getPatchCoordX(x);
		int patchY = pManager.getPatchCoordY(y);
		pManager.getCurrentTerrain().setColor(patchX, patchY, color);
		pManager.setModified(patchX, patchY);
	}

	public void setPatchHeight(double x, double y, float height) {
		int patchX = pManager.getPatchCoordX(x);
		int patchY = pManager.getPatchCoordY(y);
		pManager.getCurrentTerrain().setHeight(patchX, patchY,
				height);
		pManager.setModified(patchX, patchY);
	}
	// protected long getShapePointer(String shape) {
	// Long pointer = shapes.get(shape);
	// if (pointer == null) {
	// pointer = StarLogo.addToHeap(shape);
	// shapes.put(shape, pointer);
	// }
	//
	// return pointer;
	// }

	public String getShape(Agent agent) {
		return agent.getShape();
	}

	public void setAgentCamera(Agent agent) {
		GameManager.getGameManager()
				.setTurtleWhoNumberForCamera(agent.getWho());
	}

	public void setCameraView(int cameraView) {
		GameManager.getGameManager().setPressedCameraViewButton(cameraView);
	}

	public void setOverhead(boolean value) {
		GameManager.getGameManager().setOverhead(value);
	}

	public boolean isAlive() {
		return true;
	}

	@Override
	public boolean selectedAgent(final int who) {
		final JFrame frame = mainWindow;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AgentMonitor agentMonitor = new AgentMonitor(frame, who,
						Application.this, Application.this.getLock());
				agentMonitor.init();
			}
		});
		return true;
	}

	public void setAgentMonitored(int who, boolean monitored) {
		Agent agent = whoAgentMap.get(who);

		if (agent != null) {
			if (agent.isAlive()) {
				agent.setMonitored(monitored);
				tw.getTurtles().getTurtleWho(who).monitored = monitored; // update
																			// state
																			// for
																			// when
																			// VM
																			// isn't
																			// running
			}
		}
	}

	@Override
	public boolean outputStatusInfo(int who, PrintStream os) {
		Agent agent = whoAgentMap.get(who);

		if (agent != null) {
			agent.outputStatusInfo(os);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void outputStatusInfo(PrintStream os) {
	}

	public Object getLock() {
		return sl.getLock();
	}

	public void addKeyListener(KeyListener listener) {
		tw.addKeyListener(listener);
	}

	public void addMouseAdapter(MouseAdapter adapter) {
		tw.addMouseListener(adapter);
		tw.addMouseMotionListener(adapter);
		tw.addMouseWheelListener(adapter);
	}

	public void addDrawableAgent(Agent agent) {
		if (agent.isAlive()) {
			turtles.addTurtle(agent);
		}
	}

	public void removeDrawableAgent(Agent agent) {
		turtles.removeTurtle(agent);
	}

	/*****
	 * BEGIN unimplemented functions that are part of StarLogoCObserver
	 * interface
	 ****/

	@Override
	public void addExportBreed(long arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean deselectedAgent(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getSpeedSliderPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void globalVariableChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMenuEvent(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAnythingRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void keysToExport() {
		// TODO Auto-generated method stub

	}

	@Override
	public void noTurtlesAlive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void runForSomeTimeBlockDone(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSpaceLandFocus(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void turtlesToExport() {
		// TODO Auto-generated method stub

	}

	@Override
	public void vmTicked(double arg0) {
		// TODO Auto-generated method stub

	}

	/***** END unimplemented functions that are part of StarLogoCObserver interface ****/
}
