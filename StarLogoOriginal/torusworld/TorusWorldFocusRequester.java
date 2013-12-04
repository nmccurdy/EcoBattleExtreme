package torusworld;

/**
 * A tiny access class to let SLCodeBlocks request that TorusWorld get focus
 * without having to make SLCodeBlocks' compilation depend on TorusWorld's 
 * dependencies.
 * 
 * @author Daniel
 *
 */
public class TorusWorldFocusRequester {

	private static TorusWorld tw;
	
	/**
	 * Register a TorusWorld instance with the the TWFR, so the
	 * TWFR can call the TW to tell it to request focus.
	 * @param tw
	 */
	public static void registerTW(TorusWorld tw) {
		TorusWorldFocusRequester.tw = tw;
	}
	
	/**
	 * Tell the TorusWorld instance to request keyboard focus.
	 */
	public static void requestTWFocus() {
		if (tw != null) 
			tw.requestFocus();
	}
}
