/******************************************************************
 * Copyright 2003 by the Massachusetts Institute of Technology.  
 * All rights reserved.
 *
 * Developed by Mitchel Resnick, Andrew Begel, Eric Klopfer, 
 * Michael Bolin, Molly Jones, Matthew Notowidigdo, Sebastian Ortiz,
 * Michael Mandel, Tim Garnett, Max Goldman, Julie Kane, 
 * Russell Zahniser, Weifang Sun, and Robert Tau. 
 *
 * Previous versions also developed by Bill Thies, Vanessa Colella, 
 * Brian Silverman, Monica Linden, Alice Yang, and Ankur Mehta.
 *
 * Developed at the Media Laboratory, MIT, Cambridge, Massachusetts,
 * with support from the National Science Foundation and the LEGO Group.
 *
 * Permission to use, copy, or modify this software and its documentation
 * for educational and research purposes only and without fee is hereby
 * granted, provided that this copyright notice and the original authors'
 * names appear on all copies and supporting documentation.  If
 * individual files are separated from this distribution directory
 * structure, this copyright notice must be included.  For any other uses
 * of this software, in original or modified form, including but not
 * limited to distribution in whole or in part, specific prior permission
 * must be obtained from MIT.  These programs shall not be used,
 * rewritten, or adapted as the basis of a commercial software or
 * hardware product without first obtaining appropriate licenses from
 * MIT.  MIT makes no representations about the suitability of this
 * software for any purpose.  It is provided "as is" without express or
 * implied warranty.
 *
 *******************************************************************/

package starlogotng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import utility.Utility;

public class SLURL {

    static final int HTTP = 1;
    static final int FTP = 2;
    public static final int FILE = 3;
    static final int HTTPS = 4;

    int protocol;

    String hostname = "";
    String path = "";
    String file = "";
    
    boolean pathp;

    public static final String fileseparator = System.getProperty("file.separator");
    public static final String urlseparator = "/";

    // if it's a URL, name is a hostname
    // if it's a file, name is a path
    // if it's an ftp link, name is a hostname
    public SLURL(int type, String name) {
	protocol = type;
	switch (type) {
	case HTTP: 
	case HTTPS:
	case FTP: hostname = name; break;
	case FILE: 
	    path = name;
	    if (path.endsWith(fileseparator)) 
		path = path.substring(0, path.length() - fileseparator.length());
	    pathp = true;
	    break;
	}
    }

    // if it's a URL, name1 is hostname, name2 is path
    // if it's a file, name1 is path and name2 is the file.
    public SLURL(int type, String name1, String name2) {
	protocol = type;
	switch (type) {
	case HTTP:
	case HTTPS:
	case FTP:
	    hostname = name1;
	    setNetworkPath(name2);
	    break;
	case FILE:
	    if (name1.endsWith(fileseparator)) {
		path = name1.substring(0, name1.length() - fileseparator.length());
	    } else path = name1;
	    file = name2;
	    break;
	}
    }
    
    public SLURL(int type, String hostname, String path, String file) {
	this.hostname = hostname;
	if (path.endsWith(urlseparator)) 
	    path = path.substring(0, path.length() - urlseparator.length());
	this.path = path;
	this.file = file;
	this.pathp = false;
    }

    public SLURL(URL u) {
	//System.out.println("Make SLURL from: " + u.toString());
	if (u.getProtocol().equals("file")) {
//use newish code to work for macs
//but, this newish code doesn't work! for mac loading locally. setFilePath not complete?
	  if (Utility.macintoshp)
	     {this.protocol = FILE;
	      setFilePath(u.getHost() + u.getFile());}
	  else {
//use older code (just for the if protocol = file if) from 2/5/01 build for pcs
//windows remote and locally now works fine for ie, netscape4.7
		this.protocol = FILE;
		this.path = u.getFile();
		if (u.getFile().indexOf(':') < 0) {
	  	  if (u.getHost() != null && u.getHost().length() == 1) 
		   {this.path = u.getHost() + ":" + u.getFile();}
		}
		if (this.path.startsWith("/"))
	  	  {this.path = this.path.substring(1, this.path.length());}
		this.path = swapSlashes(this.path, urlseparator.charAt(0), fileseparator.charAt(0));
		if (path.endsWith(fileseparator)) {
	  	  path = path.substring(0, path.length() - fileseparator.length());
	  	  this.pathp = true;
		} else if (path.endsWith(urlseparator)) {
	  	  path = path.substring(0, path.length() - urlseparator.length());
	  	  this.pathp = true;
		} else {
	  	  int index = path.lastIndexOf(fileseparator.charAt(0));
	  	  if (index >= 0) {
		    String newpath = path.substring(0, index);
		    this.file = path.substring(index + 1, path.length());
		    this.path = newpath;
		    this.pathp = false;
	  	  } else {
		    index = path.lastIndexOf(urlseparator.charAt(0));
		    if (index >= 0) {
		      String newpath = path.substring(0, index);
		      this.file = path.substring(index + 1, path.length());
		      this.path = newpath;
		      this.pathp = false;}
	          }
	        }
	}

	} else if (u.getProtocol().equals("http")) {
	    this.protocol = HTTP;
	    this.hostname = u.getHost();
	    setNetworkPath(u.getFile());
	} else if (u.getProtocol().equals("https")) {
	    this.protocol = HTTPS;
	    this.hostname = u.getHost();
	    setNetworkPath(u.getFile());
	} else if (u.getProtocol().equals("ftp")) {
	    this.protocol = FTP;
	    this.hostname = u.getHost();
	    setNetworkPath(u.getFile());
	} else {
	    throw new RuntimeException("unknown protocol type: " + u.toString());
	}

	//System.out.println("slurl becomes: " + toString());
    }

    // interpret file in relation to URL u. file may be absolute.
    public SLURL(URL u, String file) {
	this(u);
	switch (protocol) {
	case FTP:
	case HTTP:
	case HTTPS:
	    if (absolutePathp(file)) {
		setNetworkPath(file);
	    } else setFile(file);
	    break;
	case FILE:
	    if (absolutePathp(file)) {
		setFilePath(file);
	    } else setFile(file);
	    break;
	}
    }

    public SLURL(SLURL u) {
	protocol = u.protocol;
	hostname = u.hostname;
	path = u.path;
	file = u.file;
	pathp = u.pathp;
    }

    public boolean absolutePathp(String path) {
	if (path.startsWith(urlseparator) || path.startsWith(fileseparator)) {
	    return true;
	} 
	if (path.indexOf(':') == 1) return true;
	return false;
    }

    public String getFile() {
	if (pathp) {
	    return "";
	} else {
	    return file;
	}
    }

    public String getFileExtension() {
	if (pathp) {
	    return "";
	} else {
	    int index = file.lastIndexOf(".");
	    if (index < 0) return "";
	    return file.substring(index + 1, file.length());
	}
    }

    public String getFileNoExtension() {
	if (pathp) {
	    return "";
	} else {
	    int index = file.lastIndexOf(".");
	    if (index < 0) return file;
	    return file.substring(0, index);
	}
    }

    public void setFile(String newfile) {
	file = newfile;
	pathp = false;
    }

    public void setFileExtension(String ext) {
	if (!pathp) {
	    setFile(getFileNoExtension() + "." + ext);
	}
    }

    public void addFileExtension(String ext) {
	if (!pathp) {
	    setFile(file + "." + ext);
	}
    }

    public void setPath(String newpath) {
	switch(protocol) {
	case HTTP:
	case HTTPS:
	case FTP:
	    setNetworkPath(newpath);
	    break;
	case FILE:
	    setFilePath(newpath);
	    break;
	}
    }

    protected void setNetworkPath(String newpath) {
	while (newpath.startsWith(urlseparator)) 
	    newpath = newpath.substring(urlseparator.length(), newpath.length());
	if (newpath.endsWith(urlseparator)) {
	    path = newpath.substring(0, newpath.length() - urlseparator.length());
	    pathp = true;
	} else {
	    // get the file at the end
	    int index = newpath.lastIndexOf(urlseparator.charAt(0));
	    if (index >= 0) {
		path = newpath.substring(0, index);
		file = newpath.substring(index + 1, newpath.length());
		pathp = false;
	    } else {
		path = newpath;
		pathp = true;
	    }
	}
    }
    
    protected void setFilePath(String newpath) {
	if (Utility.pcp) {
	    // if it's missing the drive letter, and it's not a network file
	    if (newpath.indexOf(':') < 0 && !newpath.startsWith("\\")) {
		// get the old path's drive letter and stick it to the front
		if (path.indexOf(':') == 1) {
		    StringBuffer sb = new StringBuffer();
		    sb.append(path.charAt(0));
		    sb.append(':');
		    sb.append(newpath);
		    newpath = sb.toString();
		}
	    } else {
		// check for windows networking (oy)
		if (!newpath.startsWith("\\")) {
		    while (newpath.startsWith(fileseparator)) 
			newpath = newpath.substring(fileseparator.length(), newpath.length());
		}
	    }
	} else {
	    while (newpath.startsWith(fileseparator)) 
		newpath = newpath.substring(fileseparator.length(), newpath.length());
	}
	//   System.out.println("2. path is " + newpath);
	newpath = swapSlashes(newpath, urlseparator.charAt(0), 
			      fileseparator.charAt(0));
	//   System.out.println("3. path is " + newpath);
	if (newpath.endsWith(fileseparator)) {
	    path = newpath.substring(0, newpath.length() - fileseparator.length());
	    pathp = true;
	} else if (newpath.endsWith(urlseparator)) {
	    path = newpath.substring(0, newpath.length() - urlseparator.length());
	    pathp = true;
	} else {
	    int index = newpath.lastIndexOf(fileseparator.charAt(0));
	    if (index < 0) index = newpath.lastIndexOf(urlseparator.charAt(0));
	    if (index >= 0) {
		path = newpath.substring(0, index);
		file = newpath.substring(index + 1, newpath.length());
		pathp = false;
	    } else {
		path = newpath;
		pathp = true;
	    }
	}
    }


    public int getProtocol() {
	return protocol;
    }
    
    public String getPath() {
	return path;
    }
    
    public String getFullPath() {
	if (pathp) {
	    return path;
	} else {
	    switch(protocol) {
	    case HTTP:
	    case HTTPS:
	    case FTP:
		return urlseparator + path + urlseparator + file;
	    case FILE:
		if (!Utility.pcp) return fileseparator + path + fileseparator + file;
		else return path + fileseparator + file;
	    }
	    return "unknown protocol";
	}
    }

    public String encode(String url) {
	StringBuffer buf = new StringBuffer(url.length());
	StringBuffer src = new StringBuffer(url);
	for(int i = 0; i < src.length(); i++) {
	    char c = src.charAt(i);
	    if ((c >= 0 && c <= 0x20) || (c == 0x7f)) {
		buf.append('%');
		buf.append(Integer.toHexString((int)(c & 0xFF)));
		continue;
	    }
	    switch(c) {
	    case '<': 
	    case '>':
	    case '\"':
	    case '#':
	    case '%':
	    case '{':
	    case '}':
	    case '|':
	    case '\\':
	    case '^':
	    case '~':
	    case '[':
	    case ']':
	    case '`':
	    case '?':
	    case ':':
	    case '@':
	    case '=':
	    case '&':
		buf.append('%');
		buf.append(Integer.toHexString((int)(c & 0xFF)));
		break;
	    default:
		buf.append(c);
	    }
	}
	return buf.toString();
    }

    public URL getURL() {
	try {
	    switch (protocol) {
	    case HTTP:
		return new URL("http", hostname, urlseparator + encode(path) + urlseparator + encode(file));
	    case HTTPS:
		return new URL("https", hostname, urlseparator + encode(path) + urlseparator + encode(file));
	    case FTP:
		return new URL("ftp", hostname, urlseparator + encode(path) + urlseparator + encode(file));
	    case FILE:
		return new URL("file", "", ((Utility.pcp) ? "" : fileseparator) + path + fileseparator + file);
	    }
	} catch (MalformedURLException mue) { mue.printStackTrace(); }
	return null;
    }

    public URL getWebURL() {
	try {
	    switch (protocol) {
	    case HTTP:
		return new URL("http", hostname, urlseparator + encode(path) + urlseparator + encode(file));
	    case HTTPS:
		return new URL("https", hostname, urlseparator + encode(path) + urlseparator + encode(file));
	    case FTP:
		return new URL("ftp", hostname, urlseparator + encode(path) + urlseparator + encode(file));
	    case FILE:
		return new URL("file", "", urlseparator + 
			       swapSlashes(((Utility.pcp || Utility.macintoshp) ? "" : fileseparator) + path + fileseparator + file, 
					   fileseparator.charAt(0),
					   urlseparator.charAt(0)));
	    }
	} catch (MalformedURLException mue) { mue.printStackTrace(); }
	return null;
    }
	
    private String swapSlashes(String path, char fromslash, char toslash) {
	char[] pathchars = path.toCharArray();
	char[] newpathchars = new char[pathchars.length];
	for(int i = 0; i < pathchars.length; i++) {
	    if (pathchars[i] == fromslash) newpathchars[i] = toslash;
	    else newpathchars[i] = pathchars[i];
	}
	return new String(newpathchars);
    } 

    public String load() {
	switch (protocol) {
	case FTP:
	case HTTP:
	case HTTPS:
	    return (String)Utility.prim_urltostring(getWebURL());
	case FILE:
	    if (Utility.applet == null) {
		return (String)Utility.prim_filetostring(toString());
	    } else return (String)Utility.prim_urltostring(getWebURL());
	}
	return "";
    }

    public String load(String encoding) {
	switch (protocol) {
	case FTP:
	case HTTP:
	case HTTPS:
	    return (String)Utility.prim_urlwithencodingtostring(getWebURL(), encoding);
	case FILE:
	    if (Utility.applet == null) {
		return (String)Utility.prim_filewithencodingtostring(toString(), encoding);
	    } else return (String)Utility.prim_urlwithencodingtostring(getWebURL(), encoding);
	}
	return "";
    }

    public byte[] loadBinary() {
//System.out.println("top of SLURL.load. getWebURL() = " + getWebURL());
	switch (protocol) {
	case FTP:
	case HTTP:
	case HTTPS:
	    return (byte[])Utility.prim_urltobytes(getWebURL());
	case FILE:
	    if (Utility.applet == null) {
		return (byte[])Utility.prim_filetobytes(toString());
	    } else return (byte[])Utility.prim_urltobytes(getWebURL());
	}
	return new byte[0];
    }

    public void save(String contents) throws IOException {
	assert protocol == FILE;
	File file = new File(getFullPath());
	BufferedWriter out = new BufferedWriter(new FileWriter(file));
	out.write(contents);
	out.close();
    }


    public void save(String contents, String encoding) throws IOException {
	assert protocol == FILE;
	File file = new File(getFullPath());
	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
								       Charset.forName(encoding)));
	out.write(contents);
	out.close();
    }
    
    /**
     * Saves this file as a zip file with this file inside
     * @param contents the String contents of the TNG project file
     * @param encoding the encoding to use when saving the file
     * @throws IOException
     */
    public void saveZipped(String contents, String encoding) throws IOException {
    	assert protocol == FILE;
    	File file = new File(getFullPath());
    	ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
    	ZipEntry entry = new ZipEntry(getFile());
    	zout.putNextEntry(entry);
    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(zout, Charset.forName(encoding)));
    	out.write(contents);
    	out.close();    	
    }
    
    /**
     * Loads this file assuming that it is a zipped file containing a TNG
     * project file inside.
     * @param encoding the String encoding to use when reading the file
     * @return a String containing the contents of the file
     * @throws IOException
     */
    public String loadZipped(String encoding) throws IOException {
    	assert protocol == FILE;
    	File file = new File(getFullPath());
    	
    	// open the zipped file
    	ZipFile zipfile = new ZipFile(file);
    	Enumeration entries = zipfile.entries();
    	ZipEntry entry = null;
    	
    	// look for the entry corresponding to the TNG project file
    	while(entries.hasMoreElements()) {
    		entry = (ZipEntry) entries.nextElement();
    		if (entry.getName().endsWith(Project.EXTENSION)) {
    			break;
    		}
    	}
    	
    	return readZipEntryToString(zipfile.getInputStream(entry), encoding);
    }
    
    public static String readZipEntryToString(InputStream is, String encoding) throws IOException {
    	// read the contents into a StringBuffer and return it as a String.
    	StringBuffer newcontents = new StringBuffer();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
    	char[] target = new char[4096];
    	while (reader.read(target) > 0) {
    		newcontents.append(target);
    	}
    	reader.close();    
     	return newcontents.toString();

    }

    public String toString() {
	switch (protocol) {
	case HTTP:
	    return "http://" + hostname + urlseparator + path + urlseparator + file;
	case HTTPS:
	    return "https://" + hostname + urlseparator + path + urlseparator + file;
	case FTP:
	    return "ftp://" + hostname + urlseparator + path + urlseparator + file;
	case FILE:
	    return ((Utility.pcp) ? "" : fileseparator) + path + fileseparator + file;
	}
	return "unknown protocol: ";
    }

    public boolean exists() {
	assert protocol == FILE;
	File file = new File(getFullPath());
	return file.exists();
    }

}
