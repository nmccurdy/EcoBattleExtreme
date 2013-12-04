package torusworld.model;

import javax.media.opengl.GL;

/** @modelguid {C12AD317-FFDC-487E-A7D9-916AD5F48279} */
abstract public class Model {
	public static final int LOW_DETAIL = 1;
	public static final int MEDIUM_DETAIL = 2;
	public static final int HIGH_DETAIL = 3;
	
	public abstract String getModelName();
	public abstract String getSkinName();
	public abstract String[] getAvailableAnimations();
    public abstract boolean init(GL gl);
    public abstract void deinit(GL gl);
}