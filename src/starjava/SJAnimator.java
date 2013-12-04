package starjava;

import javax.media.opengl.GLAutoDrawable;

import starlogoc.StarLogo;
//import torusworld.SLAnimator;
import torusworld.SLTerrain;
import torusworld.TorusWorld;

/**
 * WARNING!
 * 
 * The inspiration for the code within this class comes from JOGL's
 * Animator.java. We use a custom animator because the current FPSAnimator does
 * not provide fine-grained control over the framerate to permit automatic
 * backoff to keep the VM running full speed.
 * 
 * Keep an eye on the Animator class from release to release and check for
 * changes that should be incorporated into our custom animator to maintain
 * interoperability with the rest of JOGL.
 * 
 * END WARNING
 * 
 * 
 * <P>
 * An Animator can be attached to a GLDrawable to drive its display() method in
 * a loop. For efficiency, it sets up the rendering thread for the drawable to
 * be its own internal thread, so it can not be combined with manual repaints of
 * the surface.
 * </P>
 * 
 * <P>
 * The Animator currently contains a workaround for a bug in NVidia's drivers
 * (80174). The current semantics are that once an Animator is created with a
 * given GLDrawable as a target, repaints will likely be suspended for that
 * GLDrawable until the Animator is started. This prevents multithreaded access
 * to the context (which can be problematic) when the application's intent is
 * for single-threaded access within the Animator. It is not guaranteed that
 * repaints will be prevented during this time and applications should not rely
 * on this behavior for correctness.
 * </P>
 */

public class SJAnimator {
	private static volatile GLAutoDrawable drawable;
	private static Thread threadVM, threadRender;
	private static volatile boolean shouldStop;

	private static long nextVMTime = 0;
	private static double VMRPS = 5;

	private static TorusWorld tw;
	private static Object lock;

	private static boolean run = false;
	private static boolean paused = false;
	private static boolean hasFocus = true;
	/**
	 * remembers if at the last mobile objects update any object was moving;
	 * used to add Sleeps
	 */
	private static boolean anythingMoving = true;

	private static Object lockVMStateChanging = new Object();
	private static Executable executable;

	/** Initializes the Animator */
	public static void init(GLAutoDrawable _drawable, StarLogo _sl,
			TorusWorld _tw) {
		drawable = _drawable;
		tw = _tw;
		lock = _sl.getLock();

		// // Workaround for NVidia driver bug 80174
		// if (drawable instanceof GLCanvas)
		// {
		// ((GLCanvas) drawable).willSetRenderingThread();
		// }
	}

	/** Starts this animator. */
	public static synchronized void start() {
		if (threadVM != null || threadRender != null)
			throw new RuntimeException("Already started");

		threadVM = new Thread(new Runnable() {
			public void run() {
				SJAnimator.runVMLoop();
			}
		});

		threadRender = new Thread(new Runnable() {
			public void run() {
				SJAnimator.runRenderLoop();
			}
		});

		threadVM.setPriority(Thread.MIN_PRIORITY);
		threadRender.setPriority(Thread.MIN_PRIORITY);
		threadVM.start();
		// Hack to handle race condition
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threadRender.start();
	}

	private static void runRenderLoop() {
		try {
			while (!shouldStop) {
				// System.out.println("Focus: " + hasFocus);
				Thread.yield();
				if (!hasFocus)
					Thread.sleep(20);

				if (paused) {
					synchronized (lockVMStateChanging) {
						tw.updateMobileObjects(1);
						anythingMoving = tw.isAnythingMoving();
					}
				} else {
					synchronized (lockVMStateChanging) {
						long time = System.nanoTime();
						float delta = 1.f - (float) (VMRPS * 1e-9 * (nextVMTime - time));
						if (delta > 1.f)
							delta = 1.f;
						if (delta < 0.f)
							delta = 0.f;
						tw.updateMobileObjects(delta);
						anythingMoving = tw.isAnythingMoving();

					}
				}

				if (!hasFocus) { // && !anythingMoving)
					Thread.sleep(200);
				}
				drawable.display();

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			drawable.setAutoSwapBufferMode(false);
		}
	}

	private static void runVMLoop() {
		try {
			while (!shouldStop) {
				Thread.yield();

				long time = System.nanoTime();

				// if more than 1s away from running VM, sleep 1s.
				if (!run && (nextVMTime - time > 1000000000)) {
					Thread.sleep(1000);
					continue;
				}
				// otherwise, if more than 5ms away from VM run, sleep for that
				// time.
				// 5ms is the smallest amount of time Windows XP will sleep a
				// thread for.
				else if (!run && (nextVMTime - time > 1048576 * 5)) {
					// >>20 is like dividing by 1048576.
					Thread.sleep((nextVMTime - time) >> 20);
					continue;
				}
				// otherwise, we're getting close. Just yield until it's time to
				// run.
				else if (!run && (nextVMTime - time > 0)) {
					Thread.yield();
					continue;
				}

				// run the VM
				if ((tw != null && tw.getTurtles() != null)
						&& (run || (!paused && VMRPS > 0 && time >= nextVMTime)))

				// &&
				// NJM nothing is running as far as starlogo is concerned, but
				// we may be running, so we still
				// want to go through the steps. Eventually we may want to have
				// java code running concurrently with
				// star logo code in which case we'll want to have a

				// StarLogo.isAnythingRunning())
				{

					synchronized (lock) {
						run = false;
						executable.execute();
// NJM this deadlocks with 1.5 for some reason
//						tw.checkTurtlesCollision(0);
//						tw.getStarLogo().runVM(0);
						tw.setRanVM();

						synchronized (lockVMStateChanging) {
							
							tw.updateVMRelatedState();
						}
					}
				}
				// otherwise, we're either not started, paused, or nothing
				// is running, so get this thread to get out of the way for .1s
				else {
					Thread.sleep(100);
				}

				if (paused) {
					// update state in case monitors have changed something
					synchronized (lock) {
						synchronized (lockVMStateChanging) {
							tw.updateVMRelatedState();
						}
					}
				}

				// set the next time for this loop to run
				nextVMTime = time + (int) Math.floor(1e9 / VMRPS);
			}
		} catch (Throwable e) {
			if (e instanceof ThreadDeath)
				throw (ThreadDeath) e;
			e.printStackTrace();
		} finally {
			System.out.println("VM thread finished.");

		}
	}

	/**
	 * Stops this animator. In most situations this method blocks until
	 * completion, except when called from the animation thread itself or in
	 * some cases from an implementation-internal thread like the AWT event
	 * queue thread.
	 */
	public static void stop() {
		shouldStop = true;
	}

	/*
	 * private long curTime = System.currentTimeMillis(); private boolean ranVM
	 * = false; private class VMRunner extends TimerTask { public void run() {
	 * try { synchronized(lock) { if (tw != null && tw.turtles != null) {
	 * tw.sl.runVM(0);//framesPerVMRun); tw.checkTurtlesCollision(0);//changed
	 * tw.ranVM = true; ranVM = true; lastVMTime = System.nanoTime(); } } }
	 * catch (Exception e) { e.printStackTrace(); } } }
	 */

	/**
	 * Sets the "paused" property of the SLAnimator. If set to true, all VM
	 * calls and animation updates are suspended, although agents continue to
	 * talk to monitors.
	 * 
	 * @param _paused
	 *            - true for paused, false for normal operation.
	 */
	public static void setPaused(boolean _paused) {
		paused = _paused;
	}

	/**
	 * Runs the VM, regardless of whether we're paused or not.
	 */
	public static synchronized void stepVM() {
		if (tw != null)
			synchronized (lock) {
				executable.execute();

//				tw.checkTurtlesCollision(0);
//				tw.getStarLogo().runVM(0);
				tw.setRanVM();
//				tw.updateVMRelatedState();
				synchronized (lockVMStateChanging) {
					tw.updateMobileObjects(0);
				}
			}
	}

	/**
	 * Sets the # of VM Runs Per Second
	 * 
	 * @param vmrps
	 *            - the VM Runs Per Second
	 */
	public synchronized static void setVMrps(double vmrps) {
		VMRPS = vmrps;
	}

	public static void setRun() {
		// note: access is unsynchronized, but it's only a boolean.. and not so
		// time-critical
		// that we have to read it straight away
		run = true;
	}

	/**
	 * Informs SLAnimator of whether SpaceLand has focus to provide better
	 * thread control
	 * 
	 * @param _hasFocus
	 *            true when SpaceLand has focus, false otherwise
	 */
	public static void setFocus(boolean _hasFocus) {
		hasFocus = _hasFocus;
		// System.out.println("SpaceLand focus = " + this.hasFocus);
	}

	public static void addExecutable(Executable exec) {
		executable = exec;

	}

}
