package starlogotng;


public class Project
{
	public static final String VERSION_STRING = "\"v1.0pr4\"";

	public static final String CB_VERSION_STRING = "\"b2.0\"";
	
	public static final String DEFAULT_ENCODING = "UTF-8";

	// Save file version numbers (not program version numbers)
	/**
	 * Save files should only be loaded if they have a lower version number or
	 * have a version number that differs only in the last two decimal digits
	 * (thousandths and ten-thousandths).
	 * 
	 * MUST BE KEPT SYNCHRONIZED WITH StarLogoBlocks.LOAD_VERSION.
	 * (and StarLogoBlocks.NEW_CB_VERSION)
	 */
	public static final double CURRENT_VERSION = 10400;
	public static final double NEW_CB_VERSION = 10700;

	public static final String HEADER = "StarLogoTNG";
	public static String language = "en"; // English

	public static final String EXTENSION = "sltng";
//	// filters file dialog file lists of files without EXTENSION
//	private static FilenameFilter filter = new FilenameFilter()
//	{
//		public boolean accept(File file, String name)
//		{
//			return name.endsWith("." + EXTENSION);
//		}
//	};
//
//	private static SLURL curDir;
//	// used for preferences
//	private final static String LAST_OPENED_DIRECTORY = "LAST_OPENED_DIRECTORY";
//
//	private double version;
//
//	public SLURL filename;
//
//	private Application app;
//
//	public Project(Application app)
//	{
//		synchronized (app.getLock())
//		{
//			app.resetTorusWorld();
//			app.clearAll();
//			if(!app.doesBreedExist("Turtles"))
//				app.addBreed("Turtles", "animals/turtle-default");
//			app.createTurtles(2, "Turtles", -1);
//			app.setAgentYcor(0,1);
//			app.setAgentYcor(1,-1);            
//			app.hideClock();
//			app.hideScore();
//			app.showMiniView();
//			app.resetTime();
//			app.setWhoNumberForCamera(0);
//			app.resetRuntimeWorkspace();
//			this.app = app;
//
//			if (curDir == null)
//			{
//				curDir = new SLURL(SLURL.FILE, System.getProperty("user.home"));
//				if (Utility.pcp)
//				{
//					curDir.setPath(curDir.getFullPath() + "\\My Documents\\");
//				}
//			}
//			filename = null;
//		}
//    }
//
//	public static void newProject(Application app, boolean loadingOldProject)
//	{
//		synchronized (app.getLock())
//		{
//			app.setTitle("");
//			app.deleteAllBreeds();
//			System.out.println("resetting slb");
//			WorkspaceController.resetWorkspace();
//			//reset compiler
//			System.gc(); /* we left a lot of garbage */
//			if(!loadingOldProject)
//				WorkspaceController.getInstance().reloadWorkspace();
//			Project p = new Project(app);
//			app.project = p;
//			app.setCommunityModel(null);
//			app.clearChanged();
//			//app.resetTorusWorld();
//		}
//	}
//
//	public static void openProject(Application app)
//	{
//		SLURL newname = null;
//
//		FileDialog d = new FileDialog(app.getActiveFrame(), "Open Project",
//				FileDialog.LOAD);
//		d.setFilenameFilter(filter);
//
//		Preferences prefs = Preferences.userNodeForPackage(Project.class);
//
//		// we can get the current directory from the preferences
//		String lastdir = prefs.get(LAST_OPENED_DIRECTORY, null);
//		if (lastdir != null)
//		{
//			curDir = new SLURL(SLURL.FILE, lastdir);
//			d.setDirectory(curDir.getFullPath());
//		}
//
//		if (!Utility.macosxp)
//			d.setFile("*." + EXTENSION);
//		d.setVisible(true);
//		curDir = new SLURL(SLURL.FILE, d.getDirectory());
//		prefs.put(LAST_OPENED_DIRECTORY, curDir.getFullPath());
//		// save the most recent directory in preferences
//		// to be used for future open and save dialog boxes
//		if (d.getFile() != null)
//		{
//			newname = new SLURL(SLURL.FILE, curDir.getPath(), d.getFile());
//		} else
//			newname = null;
//		d.dispose();
//		if (newname != null)
//		{
//			newProject(app, true);
//			app.project = Project.loadFile(newname, app);
//			// for maintaining the file history in the file menu
//			app.insertFileInHistory(newname);
//		}
//	}
//
//	/**
//	 * Makes new project from String representation of project.
//	 * If the string is a base64-encoded zip file, unzips it first.
//	 * @param app
//	 * @param newprojectString
//	 */
//	static void newProjectFromString(Application app, String newprojectString, String projectTitle) {		
//		newProject(app, true);
//		try {
//			// if the string is an encoded zip file, load the bundle before continuing
//			if (!newprojectString.startsWith(Project.HEADER)) {
//				newprojectString = loadBundle(newprojectString, app);
//			}
//			
//			// now that we're sure we have an unzipped String, create a new project out of it
//			app.project = Project.loadFromString(app, newprojectString, projectTitle, null, app.project);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * Opens a project at the specified <code>SLURL</code> note: this is
//	 * mainly used for when the user clicks on a file history item in the File
//	 * menu
//	 * 
//	 * @param url
//	 *            the desired project location to open
//	 * @param app
//	 *            the managing <code>Application</code>
//	 */
//	public static void openProject(SLURL url, Application app)
//	{
//		File file = new File(url.getFullPath());
//		// first check to see if the file path is valid and still exists
//		// if it doesn't ask the user if they'd like to locate it themselves
//		if (!file.exists())
//		{
//			int ret = JOptionPane
//			.showConfirmDialog(
//					null,
//					"Cannot locate file: "
//					+ file.getAbsolutePath()
//					+ " \nThis file may have been moved, renamed, or deleted. \n\nWould you like to locate the file yourself?",
//					"StarLogo TNG - Cannot Locate File",
//					JOptionPane.YES_NO_OPTION,
//					JOptionPane.ERROR_MESSAGE);
//			switch (ret)
//			{
//			case JOptionPane.YES_OPTION:
//				Project.openProject(app);
//			case JOptionPane.NO_OPTION:
//				// remove from File Item History
//				app.removeFileInHistory(url);
//				return;
//			}
//		}
//
//		Project.newProject(app, true);
//		app.project = Project.loadFile(url, app);
//		app.insertFileInHistory(url);
//
//		// saved this opened directory in preferences
//		curDir = new SLURL(SLURL.FILE, url.getPath());
//		Preferences prefs = Preferences.userNodeForPackage(Project.class);
//		prefs.put(LAST_OPENED_DIRECTORY, curDir.getFullPath());
//	}
//
//	public static Project loadFile(SLURL file, Application app)
//	{
//		Project result = app.project;
//		try
//		{
//			// If file is null, cancel the load.
//			if (file == null)
//			{
//				return result;
//			}
//
//			app.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//
//			String newtitle = file.getFileNoExtension();
//			String newcontents = null;
//			try {
//				newcontents = file.load(Project.DEFAULT_ENCODING);
//				
//				// if this doesn't look like a project file, it's probably
//				// zipped, so send it to the unzipper
//				if (!newcontents.startsWith(HEADER)) {
//					newcontents = loadBundle(file, app);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				// if an exception occurred when decoding as a string,
//				// it may be a zipped file, so send it to the unzipper
//				newcontents = loadBundle(file, app);
//			}
//			result = loadFromString(app, newcontents, newtitle, file,
//					app.project);
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			// System.out.println("Error loading project file: " +
//			// e.toString());
//			JOptionPane.showMessageDialog(app.getActiveFrame(), "Error loading project file: "
//					+ e.toString(), "Load error", JOptionPane.ERROR_MESSAGE);
//		} finally
//		{
//			app.restoreCursor();
//		}
//		return result;
//	}
//
//	/**
//	 * Given a zipped bundle file, returns the (unzipped) String representation of  
//	 * the Project.
//	 * 
//	 * Also (TODO!) loads any bundled shapes and sounds
//	 * @param file the bundle file to load
//	 * @param app the Application
//	 * @return a String containing the unzipped Project file contents
//	 * @throws IOException
//	 */
//	private static String loadBundle(SLURL file, Application app) throws IOException {
//		// try to load the file from a zip file
//		try {
//			return file.loadZipped(Project.DEFAULT_ENCODING);
//		} catch (IOException e) {
//			// it wasn't a zip file!
//		}
//		
//		// if not a zip, maybe a zip saved as a base64-encoded string?
//		try {
//			return loadBundle(file.load(), app);
//		} catch (IOException e) {
//			// it wasn't base64, either!
//		}
//		
//		return null; // something went wrong - this is not a file we know how to load
//	}
//	
//	/**
//	 * Given the base64-encoded String representation of a zipped bundle,
//	 * loads the bundle and returns the (unzipped) String representation of  
//	 * the Project.
//	 * Note that this method may add to available shapes and sounds as the
//	 * bundle loaded my contain additional assets (TODO!).
//	 * @param encodedData - a String containing the base64-encoded contents of a zip file
//	 * @param app - the Application
//	 * @return a String containing the unzipped Project file contents, or null if project contents can't be found
//	 * @throws IOException
//	 */
//	private static String loadBundle(String encodedData, Application app) 
//												throws IOException {		
//		// decode the Base64-encoded data and create a ZipInputStream from it
//		InputStream is = new ByteArrayInputStream(Base64.decode(encodedData));
//		ZipInputStream zis = new ZipInputStream(is);
//		
//		// read the first entry from the zipped contents
//		ZipEntry entry = zis.getNextEntry();
//		
//		// loop through the entries looking for the one representing the project file
//		while (entry != null && !entry.getName().endsWith(Project.EXTENSION)) {
//			entry = zis.getNextEntry();
//		}
//		
//		// if we found the entry, extract it and return it
//		if (entry.getName().endsWith(Project.EXTENSION)) {
//			return SLURL.readZipEntryToString(zis, Project.DEFAULT_ENCODING);
//		}
//		
//		return null;
//	}
//
//	private static Project loadFromString(Application app, String newcontents,
//			String newtitle, SLURL filename, Project result) throws Exception
//			{
//		newcontents = newcontents.trim();
//		if (newcontents.startsWith(HEADER))
//		{
//			// stop the world once we've committed to loading
//			result = new Project(app);
//			app.project = result;
//			result.setVersion(newcontents);
//
//			app.deleteAllBreeds();
//			System.out.println("resetting slb");
//			WorkspaceController.resetWorkspace();
//			//reset compiler
//			System.gc(); /* we left a lot of garbage */
//			app.deleteAllBreeds();
//			result.app.resetRuntimeWorkspace();
//			if (!(result.version > NEW_CB_VERSION))
//			{
//				System.out.println("loading from string");
//				
//				// check for features of old projects that need to be converted before loading
//				newcontents = ProjectConverter.convertIfNecessary(newcontents);
//				
//				WorkspaceController.getInstance().loadSaveString(newcontents, false, result.version, NEW_CB_VERSION);
//				result.app.loadTime(newcontents);
//				result.app.loadRuntimeWorkspace(newcontents);
//				result.app.loadTerrain(newcontents);
//				SLBlockCompiler.getCompiler().compile(); // compile variables before loading turtles
//				result.app.loadTurtles(newcontents);
//				result.app.loadGlobals(newcontents);
//
//				result.filename = filename;
//				app.setTitle(newtitle);
//				result.clearChanged();
//			} else
//			{
//				JOptionPane
//				.showMessageDialog(app.getActiveFrame(), filename.toString()
//						+ " is not compatible with this version of StarLogo.  You have to update StarLogo.",
//						"Load error", JOptionPane.ERROR_MESSAGE);
//			}
//		} else
//		{
//			System.out.println(filename.toString() + " is not a valid project.\n"+newcontents);
//			JOptionPane.showMessageDialog(app.getActiveFrame(), filename.toString()
//					+ " is not a valid project.", "Load error",
//					JOptionPane.ERROR_MESSAGE);
//		}
//		return result;
//			}
//
//	public void closeProject(Application app)
//	{
//		newProject(app, false);
//	}
//
//	/**
//	 * Quits the application. Returns true if application is closed. False if
//	 * close cancelled.
//	 */
//	public static boolean quittingp = false;
//
//	public boolean quit(Application app)
//	{
//		// if necessary, ask user to save changes, and return if answer is
//		// "cancel"
//		// guard against quit being called twice.
//		if (quittingp)
//			return false;
//		quittingp = true;
//
//		// save the file history in the file menu for future uses of starlogo
//		// we have to put this here instead of in application because for macs
//		// there is a
//		// seperate handler that handles quit. thus, the actionperformed in
//		// Applcation.java
//		// will not handle a quit. in all cases, this function is always called.
//		app.saveFileHistory();
//
//		System.exit(0);
//		return true;
//	}
//
//	static final int YES = 0;
//	static final int NO = 1;
//	static final int CANCEL = 2;
//
//	/**
//	 * returns YES/NO if caller should continue (the user answered yes/no to
//	 * save changes, and completed save process successfully if answer was yes,
//	 * OR there is no need to save changes) and CANCEL if user canceled
//	 */
//	int saveChanges(Application app)
//	{
//		// System.out.println("need to save changes? " + contentsChanged);
//		if (app.contentsChanged())
//		{
//			// System.out.println("Ask to save changes?");
//			String choice = "Yes";
//			if (choice.equals("Yes"))
//			{
//				saveProject(app);
//				// while(savingProject) { Thread.currentThread().yield(); }
//				return (projectSaved) ? YES : CANCEL;
//			} else if (choice.equals("No"))
//			{
//				return NO;
//			} else
//			{
//				return CANCEL;
//			}
//		}
//		return YES;
//	}
//
//	volatile boolean projectSaved;
//	volatile boolean savingProject;
//
//	public boolean saveProject(Application app)
//	{
//		projectSaved = false;
//		savingProject = true;
//		if (filename == null)
//		{
//			projectSaved = saveProjectAs(app);
//		} else
//		{
//			projectSaved = saveFile(filename, app);
//		}
//		savingProject = false;
//		return projectSaved;
//	}
//
//	public boolean saveProjectAs(Application app)
//	{
//
//		FileDialog d = new FileDialog(app.getActiveFrame(),
//				"Save Project...", FileDialog.SAVE);
//
//		d.setFilenameFilter(filter);
//
//		Preferences prefs = Preferences.userNodeForPackage(Project.class);
//
//		// we can get the current directory from the preferences
//		String lastdir = prefs.get(LAST_OPENED_DIRECTORY, null);
//		if (lastdir != null)
//		{
//			curDir = new SLURL(SLURL.FILE, lastdir);
//			d.setDirectory(curDir.getFullPath());
//		}
//
//		if (!Utility.macosxp)
//			d.setFile("*." + EXTENSION);
//		d.setVisible(true);
//		curDir = new SLURL(SLURL.FILE, d.getDirectory());
//		// save current working directory in preferences
//		prefs.put(LAST_OPENED_DIRECTORY, curDir.getFullPath());
//		if (d.getFile() != null)
//		{
//			filename = new SLURL(SLURL.FILE, curDir.getPath(), d.getFile());
//			saveFile(filename, app);
//			return true;
//		} else
//		{
//			return false;
//		}
//	}
//
//	public void saveNextVersion(Application app)
//	{
//		projectSaved = false;
//		savingProject = true;
//
//		if (filename == null)
//		{
//			projectSaved = saveProjectAs(app);
//		} else
//		{
//			String newName = filename.getFile();
//			if (!(Utility.macintoshp && !Utility.macosxp))
//			{
//				newName = getNewVersionFilenamePC(newName);
//			} else
//			{
//				newName = getNewVersionFilenameMac(newName);
//			}
//			// System.out.println("NEW NAME: " + newName);
//			filename.setFile(newName);
//			projectSaved = saveFile(filename, app);
//		}
//
//		savingProject = false;
//	}
//
//	// this code was taken from starlogo 2.1
//	public String getNewVersionFilenamePC(String fname)
//	{
//		int dotindex = fname.lastIndexOf(".");
//		// if the file name has no numbers at the end
//		if (!(Character.isDigit(fname.charAt(dotindex - 1))))
//		{
//			fname = fname.substring(0, dotindex) + "01." + EXTENSION;
//		}
//		// else if the file name has one number at the end (= the user
//		// implementing this by hand then switching)
//		else if (!(Character.isDigit(fname.charAt(dotindex - 2)))
//				&& (Character.isDigit(fname.charAt(dotindex - 1))))
//		{
//			int tmp = Character.digit(fname.charAt(dotindex - 1), 10);
//			tmp++;
//			if (tmp < 10)
//			{
//				fname = fname.substring(0, dotindex - 1) + "0"
//				+ Integer.toString(tmp) + "." + EXTENSION;
//			} else
//			{
//				fname = fname.substring(0, dotindex - 1)
//				+ Integer.toString(tmp) + "." + EXTENSION;
//			}
//		}
//		// else the file has previously been saved using the save next version
//		// feature
//		else
//		{
//			int tmp = Character.digit(fname.charAt(dotindex - 2), 10);
//			tmp *= 10;
//			tmp += Character.digit(fname.charAt(dotindex - 1), 10);
//			tmp++;
//			if (tmp < 10)
//			{
//				fname = fname.substring(0, dotindex - 1)
//				+ Integer.toString(tmp) + "." + EXTENSION;
//			} else
//			{
//				fname = fname.substring(0, dotindex - 2)
//				+ Integer.toString(tmp) + "." + EXTENSION;
//			}
//		}
//		return fname;
//	}
//
//	public String getNewVersionFilenameMac(String fname)
//	{
//		// if we're on a mac, do the mac version of adding version numbers.
//		// this version has to check to see if there's an extension or not
//		if (fname.toLowerCase().endsWith("." + EXTENSION))
//		{
//			return getNewVersionFilenamePC(fname);
//		} else
//		{
//			int dotindex = fname.length() - 1;
//			if (!(Character.isDigit(fname.charAt(dotindex))))
//			{
//				fname = fname + "01";
//			} else if (!(Character.isDigit(fname.charAt(dotindex - 1)))
//					&& (Character.isDigit(fname.charAt(dotindex))))
//			{
//				int tmp = Character.digit(fname.charAt(dotindex), 10);
//				tmp++;
//				if (tmp < 10)
//				{
//					fname = fname.substring(0, dotindex) + "0"
//					+ Integer.toString(tmp);
//				} else
//				{
//					fname = fname.substring(0, dotindex - 1)
//					+ Integer.toString(tmp);
//				}
//			} else
//			{
//				int tmp = Character.digit(fname.charAt(dotindex - 1), 10);
//				tmp *= 10;
//				tmp += Character.digit(fname.charAt(dotindex), 10);
//				tmp++;
//				if (tmp < 10)
//				{
//					fname = fname.substring(0, dotindex)
//					+ Integer.toString(tmp);
//				} else
//				{
//					fname = fname.substring(0, dotindex - 1)
//					+ Integer.toString(tmp);
//				}
//			}
//		}
//		return fname;
//
//	}
//
//	public boolean saveFile(SLURL fname, Application app)
//	{
//		// djwendel - now saving always bundles the file in a zipped file 
//		return saveBundle(fname, app);
//		
//		/*
//		try
//		{
//			app.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//			if (!fname.getFileExtension().equalsIgnoreCase(EXTENSION))
//			{
//				fname.addFileExtension(EXTENSION);
//			}
//			String fileContents = "";
//			fileContents = getContents();
//
//			fname.save(fileContents, Project.DEFAULT_ENCODING);
//
//			// save this file also in the file history in the file menu
//			app.insertFileInHistory(fname);
//
//			clearChanged();
//			// filename = fname;
//			app.setTitle(filename.getFileNoExtension());
//			return true;
//		} catch (Exception ioe)
//		{
//			ioe.printStackTrace();
//			System.out.println("Error saving project.");
//			return false;
//		} finally
//		{
//			app.restoreCursor();
//		}*/
//	}
//
//	/**
//	 * returns the uncompressed String representation of this project
//	 * @return the uncompressed String representation of this project
//	 * @throws IOException
//	 */
//	private String getContents() throws IOException
//	{
//		return HEADER + " " + CB_VERSION_STRING + "\r\n"
//		+ new DecimalFormat("#").format(NEW_CB_VERSION) + "\r\n"
//		+ Utility.getLanguageAsString() + "\r\n" + WorkspaceController.getInstance().getSaveString()
//		+ "\r\n" + app.saveTime() + "\r\n" + app.saveRuntimeWorkspace()
//		+ "\r\n" + app.saveTerrain() + "\r\n" + app.saveTurtles() + "\r\n" + app.saveGlobals() + "\r\n";
//	}
//
//	/**
//	 * Much like saveBundle, except that the zipped bundle contents are returned
//	 * as a base64-encoded String rather than saved immediately to a file.  This
//	 * should be used for posting projects to the Community Site.
//	 * @param app the Application
//	 * @return a String containing the base64-encoded contents of a zip file
//	 * @throws IOException
//	 */
//	public String getBase64EncodedBundle(Application app) throws IOException {
//		
//		// create a byte stream and a ZipOutputStream that will write to the byte stream
//		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//    	ZipOutputStream zout = new ZipOutputStream(byteStream);
//    	
//    	// create an entry for the project, using a generic filename if necessary
//    	ZipEntry entry;
//    	if (filename.getFile().endsWith(Project.EXTENSION)) {
//    		entry = new ZipEntry(filename.getFile());
//    	} else {
//    		System.out.println("No project name known - using project.sltng");
//    		entry = new ZipEntry("project."+Project.EXTENSION);
//    	}
//    	
//    	// add the entry to the zip stream
//    	zout.putNextEntry(entry);
//    	
//    	// write the contents of the project to the entry in the zip stream   	
//    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(zout, Charset.forName(Project.DEFAULT_ENCODING)));
//    	out.write(getContents());
//    	out.close();    	
//    	
//    	// return the byte stream as a Base64-encoded String
//		return Base64.encode(byteStream.toByteArray()); 
//	}
//	
//	public void markChanged()
//	{
//		app.markChanged();
//	}
//
//	public void clearChanged()
//	{
//		app.clearChanged();
//	}
//
//	private void setVersion(String str)
//	{
//		Tokenizer tokenizer = new Tokenizer(str.substring(HEADER.length()));
//		tokenizer.getNextToken(); // ignore version string
//
//		// I have no idea why this was here - it gets the terrain version!!
//		// while (!tokenizer.getNextToken().equalsIgnoreCase("version"));
//
//		String token = tokenizer.getNextToken();
//
//		// version 1.00 files had the word "version" in them
//		if (token.equalsIgnoreCase("version"))
//		{
//			token = tokenizer.getNextToken(); // so get the number
//		}
//		version = Double.parseDouble(token);
//	}
//
//
//	/**
//	 * Save the project bundled in a zip file with the models and sounds it uses
//	 * @param app the Application
//	 * @return true iff the save was successful
//	 */
//	public boolean saveBundle(Application app)
//	{
//		projectSaved = false;
//		savingProject = true;
//		if (filename == null)
//		{
//			projectSaved = saveProjectAs(app);
//		} else
//		{
//			projectSaved = saveBundle(filename, app);
//		}
//		savingProject = false;
//		return projectSaved;
//	}
//
//
//	/**
//	 * Save the project bundled in a zip file with the models and sounds it uses
//	 * @param fname the filename to save to
//	 * @param application the Application
//	 * @return iff the save was successful
//	 */
//	public boolean saveBundle(SLURL fname, Application application) {
//		try
//		{
//			app.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//			if (!fname.getFileExtension().equalsIgnoreCase(EXTENSION))
//			{
//				fname.addFileExtension(EXTENSION);
//			}
//			String fileContents = "";
//			fileContents = getContents();
//
//			fname.saveZipped(fileContents, Project.DEFAULT_ENCODING);
//
//			// save this file also in the file history in the file menu
//			app.insertFileInHistory(fname);
//
//			clearChanged();
//			// filename = fname;
//			app.setTitle(filename.getFileNoExtension());
//			return true;
//		} catch (Exception ioe)
//		{
//			ioe.printStackTrace();
//			System.out.println("Error saving project.");
//			return false;
//		} finally
//		{
//			app.restoreCursor();
//		}
//	}

}
