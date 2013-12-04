package torusworld.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class GUIImageFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		
		if (file.isDirectory()) {
			return true;
		}
		
		// TODO Auto-generated method stub
		String filepath = file.getPath();
		if (filepath.endsWith(".jpg") ||
				filepath.endsWith(".JPG") ||
				filepath.endsWith(".jpeg") ||
				filepath.endsWith(".JPEG") ||
				filepath.endsWith(".png") ||
				filepath.endsWith(".PNG") || 
				filepath.endsWith(".tiff") ||
				filepath.endsWith(".TIFF") ||
				filepath.endsWith(".gif") ||
				filepath.endsWith(".GIF") ||
				filepath.endsWith(".tif") ||
				filepath.endsWith(".TIF")) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Images:" +
				"'.jpg' , '.jpeg', '.png', '.gif', '.tiff', '.tif'";
	}
	
}
