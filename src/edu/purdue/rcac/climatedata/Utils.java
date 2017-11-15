package edu.purdue.rcac.climatedata;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.ImageIcon;

/**
 * Climate Scenario Agregator
 * Copyright (C) 2016  N. Villoria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/**
 * @author nujwoo
 * This class provides several system utils that already installed on the Hubzero server.
 */
public class Utils {

	private static final String EXPORT_EXE = "/usr/bin/exportfile";
	private static final String UPLOAD_FILE = "/usr/bin/importfile";
	private static final String URL_EXE = "/usr/bin/clientaction";
	
	private static final long DEFAULT_IMPORT_TIMEOUT = 3 * 60 * 1000; // 3-min
    
	
	/** The path to the ZIP archiving program within the HubZero system. */
    private static final String ZIP_EXE = "/usr/bin/zip";
	
	private static Boolean IS_HUB = null;

	public static String readFile(String filename) throws IOException {
		BufferedReader br;
		br = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}
		br.close();
		return sb.toString();
	}

	public static String readOutput(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		StringBuffer sb = new StringBuffer();
		String output = null;

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		output = sb.toString();
		return output;
	}

	public static void downloadFile(String file) {

		// check the file value given
		if (file == null || file.trim().length() < 1) {
			// no file was given to download
			return;
		}
		File fileFile = new File(file);
		if (!fileFile.exists() || !fileFile.isFile()) {
			// the file to download doesn't actually exist
			return;
		}

		// download the file to the user through the hub download system
		try {
			String[] downloadArray = { EXPORT_EXE, file };

			Process p = Runtime.getRuntime().exec(downloadArray);
			p.waitFor();
		} catch (Throwable t) {
			// a problem occurred, so we will cancel
			return;
		}
	}
	   /**
     * Attempts to create an archive (ZIP-format) file from the list of files
     * given to the ZIP location specified.  Allows the specification of
     * whether the program should append to existing ZIP targets at the start
     * of this operation or wipe out any existing archive file at the location.
     * 
     * NOTE:  The tasks of this method may be non-trivial and should not be
     * run directly from any main program threads to prevent blocking the
     * entire application while the numerous subprocesses are executing.
     * 
     * @param files the list of files to place within the ZIP archive.
     * @param archive the location of where to write the ZIP archive.
     * @param includeEmpty whether orn ot to include zero-byte files.
     * @param append whether or not to start the archive fresh or append it.
     * @throws IOException if a problem occurs writing the archive file
     */
    public static void createZipArchive(Vector<String> files, 
                                        String archive,
                                        boolean includeEmpty,
                                        boolean append) throws IOException {
        // first, check our inputs
        if (files == null || files.isEmpty()) {
            throw new IOException("No files to zip.");
        }
        if (archive == null || archive.trim().length() < 1) {
            throw new IOException("No archive file name.");
        }
        
        // get a file object of the archive filename
        File archiveFile = new File(archive);
        if (archiveFile.exists() && !append) {
            // the file already exists, but we do not want to append
            // to it so we will delete the existing archive file we
            try {
                boolean deleted = archiveFile.delete();
                if (!deleted) {
                    throw new IOException("Could not delete existing archive.");
                }
            }
            catch (SecurityException se) {
                throw new IOException("Denied existing archive deletion.");
            }
        }
        
        // get the directory of the archive path given to use as working dir
        String workingDir = archiveFile.getParent();
        System.out.println("workingDir:" + workingDir);
        if (workingDir == null || workingDir.trim().length() < 1) {
            throw new IOException("No parent directory for archive.");
        }
        File workingDirFile = new File(workingDir);
        if (!workingDirFile.exists() || !workingDirFile.isDirectory()) {
            throw new IOException("Archive parent directory does not exist.");
        }
        
        // get the name of the archive file
        String archiveName = archiveFile.getName();
        System.out.println("archiveName:" + archiveName);
        if (archiveName == null || archiveName.trim().length() < 1) {
            throw new IOException("Could not determine archive file name.");
        }
        
        // loop through the vector, adding each file to the archive
        for (String file : files) {
            // check current file exists
            if (file == null || file.trim().length() < 1) {
                continue;
            }
//            String filePath = SystemUtils.appendPath(workingDir, file);
//            System.out.println("filePath: " + filePath);
            File fileFile = new File(file);
            if (!fileFile.exists() || !fileFile.isFile()) {
            	System.out.println("File not exists or not file:" + file);
                continue;
            }
            
            // check the file has contents (if we care about that)
            if (!includeEmpty) {
                if (fileFile.length() < 1) {
                    continue;
                }
            }
            
            // build the archiving command line arguments
            String[] archiveCmdArgs = { ZIP_EXE, 
                                        "-r",
                                        archiveName,
                                        fileFile.getName() };
            
            // run the process to add the current file to the archive
            try {
                // add the file
                Process archiveProcess =  
                        Runtime.getRuntime().exec(archiveCmdArgs, 
                                                  null, 
                                                  workingDirFile);
                archiveProcess.waitFor();
                
                // check it worked
                int exitValue = archiveProcess.exitValue();
                if (exitValue != 0) {
                    throw new IOException("Non-zero exit value.");
                }
            }
            catch (Throwable t) {
                throw new IOException("Error writing to archive.", t);
            }
            System.out.println("zipped");
        }
    }

	public static String uploadFile() {

		String outputString;
		try {
			Process p = Runtime.getRuntime().exec(UPLOAD_FILE);
			outputString = Utils.readOutput(p.getInputStream());
			return outputString;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
	
    /**
     * Attempts to import a file into the system using the HubZero "importfile"
     * script, allowing for the giving of a purpose string and a timeout 
     * period after which the import will cancel.  A path to the imported file
     * is returned or null if the import did not complete.
     * 
     * NOTE: This method will take an extended and non-trivial time to 
     * return.  It should not be run from any sort of "main" threads and 
     * should be spawned into it's own thread or some sort of lower-priority 
     * thread which will not block the entire program.  For example, if this 
     * is run directly from a user click on a button, then it will be running
     * in the main Swing event and will block the entire UI from updating. 
     * 
     * @param workingDir the directory to which the file is to be imported.
     * @param purpose a brief description of the purpose of the imported file.
     * @param timeout the amount of time (ms) to allow before aborting.
     * @return the path to the imported file or null if no import occurred.
     */
    public static String importFile(String workingDir, 
                                    String purpose, 
                                    long timeout) {
        
        // make sure the working directory exists and build a File for it
        if (workingDir == null || workingDir.trim().length() < 1) {
            // there is no place to dump the imported file
            return null;
        }
        File workingDirFile = new File(workingDir);
        if (!workingDirFile.exists() || !workingDirFile.isDirectory()) {
            // the given location to dump the imported file does not exist
            // or it is not a path to a directory so we will give up
            return null;
        }
        
        // create a vector to hold the import command arguments.  this 
        // is more complicated than just a flat array, but we have a
        // variable number of arguments we will not know until runtime.
        Vector<String> importCmdArgVector = new Vector<String>();
        importCmdArgVector.add(UPLOAD_FILE);
        
        // convert the arg vector to an array
        String[] importCmdArgs = importCmdArgVector.toArray(new String[0]);
        
        // start the import process
        Process importProcess = null;
        try {
            importProcess = Runtime.getRuntime().exec(importCmdArgs, 
                                                      null, 
                                                      workingDirFile);
        }
        catch (Throwable t) {
            // an error importing the file occurred, so we will error-out
            return null;
        }
        
        // loop until either the process finishes or the timeout is reached
        if (timeout < 1) {
            timeout = Utils.DEFAULT_IMPORT_TIMEOUT;
        }
        long startTime = System.currentTimeMillis();
        long finishTime = startTime + timeout;
        
        // try to find temp upload file in /tmp/
        File tmpdir = new File("/tmp/");
        File tmpfile = null;
        
        int exitValue = -1;
        boolean finished = false;
        while (!finished) {
            try {
                // wait a bit...
                Thread.sleep(1000);
                
                // get incoming file size
                if(tmpfile == null){
                	for(File tmp: tmpdir.listFiles()){
                		if(tmp.getName().contains("filexfer")){
                			tmpfile = tmp;
                			break;
                		}
                	}
                }
                // check we are not past our deadline
                long currentTime = System.currentTimeMillis();
                if (currentTime > finishTime) {
                	finished = true;
                }

                // try to get an exit value from the process
                exitValue = importProcess.exitValue();
                finished = true;
            }
            catch (InterruptedException ie) {
                // the thread was interrupted even though it is still running
                finished = true;
            }
            catch (IllegalThreadStateException itse) {
                // the process is not yet finished, so we will do nothing
                // and let the loop wrap around to the next iteration.
            }
        }
        
        // catch whether we were interrupted or hit our deadline with no file
        if (exitValue == -1) {
        	return null;
        }
        
        // retrieve the filename of the import from the process and return it
        try {
            // open a buffer to the stdout of the process
            InputStream is = importProcess.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            // read the output of the import command
            if (!br.ready()) {
                // we can't read the filename
                return null;
            }
            String importedFile = br.readLine();
            
            // check this value is a file that exists
            File importedFileFile = new File(importedFile);
            if (!importedFileFile.exists() || !importedFileFile.isFile()) {
                // this is not a file or it is not a "normal" file
                return null;
            }
            
            // it would seem everything worked, so return the file path
            return importedFile;
        }
        catch (Throwable t) {
            // we could not retrieve the imported file name
            return null;
        }
        
    }
    
    /**
     * Returns a string displaying the size of the file with the proper units.
     * If the given file is a directory, the value returned will be the size
     * of all of its contents calculated recursively.
     * 
     * @param file the file for which a size is requested.
     * @return a string displaying the size of the file with the proper units.
     */
    public static String getFilesizeString(File file) {
        // check we actually have something to size up
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            return null;
        }
        
        // get the size of the file (or the size of directory contents)
        double filesize = (double)Utils.getFilesize(file);
        if (filesize < 0.0d) {
            return null;
        }
        
        // get the proper units to use for the filesize string
        int divisor = 1024;
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int level = 0;
        while (filesize >= divisor && level < units.length) {
            level++;
            filesize /= (double)divisor;
        }
        
        // put together and return the filesize string
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedFilesize = df.format(filesize);
        return (formattedFilesize + " " + units[level]);
        
    }
    
    /**
     * Returns the size (in bytes) of the given file.  This method differs
     * from the standard Java API method File.length() in that it will
     * recursively return the size of all the contents of directories.
     * 
     * @param file the file for which a size is requested.
     * @return the size (in bytes) of the given file.
     */
    public static long getFilesize(File file) {
        if (file == null) {
            return -1;
        }
        if (!file.exists()) {
            return -1;
        }
        
        if (file.isDirectory()) {
            // for directories, we need to add up the size of all contents
            long total = 0L;
            File[] contents = file.listFiles();
            for (File f : contents) {
                long size = Utils.getFilesize(f);
                if (size > 0) {
                    total += size;
                }
            }
            return total;
        }
        else {
            // return the length of the file
            return file.length();
        }
    }//end getFilesize(File)
    

	public static void deleteOutputFile(String outputPath) {
		try {
			Process p = Runtime.getRuntime().exec("rm " + outputPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void viewExternally(URL url) throws MalformedURLException,
			IllegalStateException {
		
		System.out.println("called viewExternally()");
        // first, branch to the HubZero URL processing system if necessary
        if (isHubZeroSystem()) {
        	String urlString = url.toExternalForm();
        	if (urlString == null || urlString.trim().length() < 1) {
        		throw new MalformedURLException("URL has no address.");
        	}
        	viewScript(urlString);
        	return;
        }
        
		// check the url exists
		if (url == null) {
			throw new MalformedURLException("No URL to open given.");
		}

		// make sure we can get a handle to the "desktop"
		if (!Desktop.isDesktopSupported()) {
			throw new IllegalStateException("System services not supported.");
		}

		// get the "desktop" and check that browsing is possible
		Desktop desktop = Desktop.getDesktop();
		if (desktop == null) {
			throw new IllegalStateException("Cannot access system services.");
		}
		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			throw new IllegalStateException("Browsing access not available.");
		}

		// finally, tell the system to display the url for us
		try {
			URI uri = new URI(url.toString());
			System.out.println("calling desktop.browse(uri):" + uri.toASCIIString());
			desktop.browse(uri);
		} catch (URISyntaxException use) {
			throw new IllegalStateException("Could not convert to URI.", use);
		} catch (IOException ioe) {
			throw new IllegalStateException("Could not browse URL.", ioe);
		}
	}
	
	public static void viewScript(String url) {
        // check we have been given a url and build a URL object
        if (url == null || url.trim().length() < 1) {
            return;
        }
        URL urlURL = null;
        try {
          urlURL = new URL(url);
        }
        catch (MalformedURLException mue) {
            return;
        }
        
        // create the argument list for the URL exporting command
        String[] urlArgs = { URL_EXE, "url", urlURL.toString() };
        try {
            Runtime.getRuntime().exec(urlArgs);
        }
        catch (Throwable t) {
            return;
        }
    }//end openExternalURL(String)
	
	public static boolean isHubZeroSystem() {
        // NOTE: this is not a very robust method for checking whether or not
        // we are in the Hub system, but this is how the HZ team says their
        // own programs do the checking and how we should do it, so for now
        // it will have to do until we explore our own better solution.
        
        // first, if we've already done the check we can short-circuit with
        // that answer without having to do all this checking a second time
        if (IS_HUB != null) {
            return IS_HUB;
        }
        
        // check to see if the env var "SESSIONDIR" exists and get it
        String sessionDirVar = "SESSIONDIR";
        String sessionDirVal = SystemUtils.getEnvVar(sessionDirVar);
        if (sessionDirVal == null || sessionDirVal.trim().length() < 1) {
            // the env var was not set, so we are not in a Hub
            IS_HUB = false;
            return false;
        }
        
        // just because we have this value set, it does not mean we are in
        // a Hub, necessarily.  we must also check to see if this is a valid
        // path and if the Hub-specific 'resources' file is within it.
        File sessionsDirFile = new File(sessionDirVal);
        if (!sessionsDirFile.exists() || !sessionsDirFile.isDirectory()) {
            // session dir var was set, but it was not a valid
            // directory, so we are not in a Hub environment
            IS_HUB = false;
            return false;
        }
        
        // okay, the sessions dir env var was indeed a valid directory, but
        // does it contain the resources file that (according to the Hub team)
        // is our way of knowing we are in a Hub?
        String resourcesPath = 
                SystemUtils.appendPath(sessionDirVal, "resources");
        File resourcesFile = new File(resourcesPath);
        if (!resourcesFile.exists() || !resourcesFile.isFile()) {
            // the resources file does not exist or is not a regular file
            IS_HUB = false;
            return false;
        }
        
        // well, we passed all of our (horrible, non-robust) checks, so we
        // must be in a HubZero environment (or so we've been told...)
        IS_HUB = true;
        return true;
    }
	
	public static BufferedImage scaleImage(int WIDTH, int HEIGHT, URL file) {
	    BufferedImage bi = null;
	    try {
	        ImageIcon ii = new ImageIcon(file);//path to image
	        bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2d = (Graphics2D) bi.createGraphics();
	        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY));
	        g2d.drawImage(ii.getImage(), 0, 0, WIDTH, HEIGHT, null);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    return bi;
	}
	public static BufferedImage scaleImage(int WIDTH, int HEIGHT, File file) {
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return scaleImage(WIDTH, HEIGHT, url);
	}
	
}
