package starjava;

import starlogoc.PatchManager;
import starlogoc.StarLogo;

public class PatchManagerJava extends PatchManager {

	private StarLogo sl;
	private int width;
	private int height;

	/**
	 * Creates a new PatchManager with numPatches patches
	 **/
	public PatchManagerJava(int width, int height, StarLogo sl) {
		super(width, height, sl);
		this.width = width;
		this.height = height;
		this.sl = sl;
	}

	public void setModified(int x, int y) {
		if (getNumChangedPatches() < getTotalPatches() / 2) {
			changedPatches.add(new PatchCoordinates(x, y));
		}
	}

	public int getPatchCoordX(double x) {
		int patchX = (int) x + (width / 2);
		if (patchX < 0) {
			patchX = 0;
		}
		if (patchX > width) {
			patchX = width;
		}

		return patchX;
	}

	public int getPatchCoordY(double y) {
		int patchY = (height - 1) - ((int) y + (height / 2));

		if (patchY < 0) {
			patchY = 0;
		}
		if (patchY > height) {
			patchY = height;
		}

		return patchY;

	}
}
