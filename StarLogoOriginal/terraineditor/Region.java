package terraineditor;

public class Region 
{
	public final int minX, minY, maxX, maxY;

	public Region(int x1, int y1, int x2, int y2) {
		if (x1 > x2) 
		{
			maxX = x1;
			minX = x2;
		} 
		else {
			minX = x1;
			maxX = x2;
		}

		if (y1 > y2) 
		{
			maxY = y1;
			minY = y2;
		} 
		else {
			minY = y1;
			maxY = y2;
		}
	}

	public boolean isEquivalentTo(Region r) {
		if (r.maxX == maxX && r.minX == minX && r.maxY == maxY && r.minY == minY) {
			return true;
		}
		return false;
	}

	public boolean isWiderThan(Region r) {
		if (maxX-minX > r.maxX-r.minX) {
			return true;
		}
		return false;
	}

	public boolean isTallerThan(Region r) {
		if (maxY-minY > r.maxY-r.minY) {
			return true;
		}
		return false;
	}

	public Region subtractX(Region r) {
		if (r.minX == minX) { // left edge is fixed
			return new Region(r.maxX, minY, maxX, maxY);
		}
		// otherwise right edge is fixed
		return new Region(minX, minY, r.minX, maxY);
	}

	public Region subtractY(Region r) {
		if (r.minY == minY) { // top edge is fixed
			return new Region(minX, r.maxY, maxX, maxY);
		}
		// otherwise bottom edge is fixed
		return new Region(minX, minY, maxX, r.minY);
	}

	public boolean sharesCornerWith(Region r) {
		return ((r.minX == minX) && (r.minY == minY || r.maxY == maxY))
			|| ((r.minY == minY) && (r.minX == minX || r.maxX == maxX))
			|| ((r.maxX == maxX) && (r.minY == minY || r.maxY == maxY))
			|| ((r.maxY == maxY) && (r.minX == minX || r.maxX == maxX));
	}
}