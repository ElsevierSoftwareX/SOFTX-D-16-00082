package edu.purdue.rcac.climatedata;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.net.URISyntaxException;
import java.net.URL;

import java.security.CodeSource;
import java.security.ProtectionDomain;

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
 * This class provides a Hubzero system utils. 
 */
public class SystemUtils {
    
    /** The system-dependent character used to separate path elements. */
    public static final String SEPARATOR = System.getProperty("file.separator");
    
    /** The system-dependent character used to separate text lines. */
    public static final String NEWLINE = System.getProperty("line.separator");
    
    /** A carriage-return plus line-feed for writing files for Windows. */
    public static final String CRLF = "\r\n";
    
    /** The name of the operating system upon which the program is running. */
    public static final String OS = System.getProperty("os.name");
    
    /** The type of architectures upon which the system is running. */
    public static final String ARCH = System.getProperty("os.arch");
    
    /** The version of the system upon which the program is running. */
    public static final String VERSION = System.getProperty("os.version");
    
    /** A reference to the original System.out of the program. */
    public static final PrintStream REAL_SYSTEM_OUT = System.out;
    
    /** A reference to the original System.err of the program. */
    public static final PrintStream REAL_SYSTEM_ERR = System.err;
    
    /** A reference to the original System.in of the program. */
    public static final InputStream REAL_SYSTEM_IN = System.in; 
    
    
    /** The current working directory of the user's last file access. */
    private static String CURRENT_WORKING_DIR = null;
    
    /** The name of the lock file for determining if the program is running. */
    private static String RUNNING_LOCK_FILE = "RUNNING_LOCK";
    
    /** The period of time (milliseconds) the lock file is still valid. */
    private static long RUNNING_LOCK_FILE_TIMEOUT = 30 * 1000;
    
    /** How often (milliseconds) the running lock file timestamp is updated. */
    private static long RUNNING_LOCK_FILE_UPDATE = 15 * 1000;
    
    /** The thread for periodically touching the lock file timestamp. */
    private static Thread runningLockThread;
    
    
    /**
     * Returns the username of the current system account running the program.
     * 
     * @return the username of the current system account running the program.
     */
    public static String getUsername() {
        return System.getProperty("user.name");
    }
    
    
    /**
     * Returns the location of the user's home directory.
     * 
     * @return the location of the user's home directory.
     */
    public static String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }
    
    
    /**
     * Returns the location of the user's documents directory.
     *  
     * @return the location of the user's documents directory.
     */
    public static String getUserDocumentsDirectory() {
        return System.getProperty("user.dir");
    }
    
    
    
    /**
     * Returns the value of the requested environment variable or null if
     * the environment variable is not set.  This method wraps around the
     * Java environment variable retrieval system and catches any security
     * exceptions that may occur from being unable to access the environment.
     * While this may seem unnecessary now, it is designed with future needs
     * in mind.  This gives the program an easy mechanism for rolling other
     * sources of external configuration values or internal values which may
     * override the actual system environment into this same existing framework
     * which will already be in use throughout the program.
     * 
     * @param var the requested environment variable.
     * @return the value of the requested environment variable.
     */
    public static String getEnvVar(String var) {
        
        // make sure a variable is specified
        if (var == null || var.length() < 1) {
            return null;
        }
        
        // attempt to retrieve it, catching any security permission problems
        try {
            return System.getenv(var);
        }
        catch (Throwable t) {
            return null;
        }
    }
    
    
    /**
     * Returns the user's current working directory from which they have
     * most recently accessed files within the program or the default
     * document directory if one is not already set.  This is intended to
     * be used for setting the initial location of any file choosers to
     * give the user a more consistent experience and relies on the any
     * file choosers to subsequently set this value after any file accesses
     * to keep it consistent for future invocations.
     * 
     * @return the user's current working directory.
     */
    public static String getCurrentWorkingDirectory() {
        // set the current dir to the default if necessary
        if (CURRENT_WORKING_DIR == null) {
            CURRENT_WORKING_DIR = SystemUtils.getUserHomeDirectory();
        }
        
        // return the current working directory
        return CURRENT_WORKING_DIR;
    }
    
    
    /**
     * Sets the current working directory of the user's latest file activity
     * if the given value equates to the valid path of a directory in the
     * system.  This is intended to be used in a manner that allows the
     * program to maintain a consistent view of the file system across file
     * access attempts by remembering the user's browsing location.  If an
     * invalid value is given, no change is made to the currently stored
     * working directory.
     * 
     * @param dir the user's current working directory to set.
     */
    public static void setCurrentWorkingDirectory(String dir) {
        
        // test the given value is a valid directory
        if (dir == null || dir.length() < 1) {
            return;
        }
        File file = new File(dir);
        if (!file.exists()) {
            return;
        }
        else if (!file.isDirectory()) {
            return;
        }
        
        // all tests passed, so set the current working directory
        CURRENT_WORKING_DIR = dir;
    }
    
    
    /**
     * Returns a path string which appends the given file to the given parent
     * base path, making sure there is a system-dependent file separator
     * inserted between the two.   
     * 
     * @param parent the base path to which an append is being made.
     * @param file the file to append to the given base path.
     * @return the appended path string.
     */
    public static String appendPath(String parent, String file) {
        // check the validity of the inputs
        if (parent == null || parent.trim().length() < 1) {
            return null;
        }
        if (file == null || file.trim().length() < 1) {
            return null;
        }
        
        // make sure a file separator is already on the end of the base path 
        if (!parent.endsWith(SystemUtils.SEPARATOR)) {
            parent += SystemUtils.SEPARATOR;
        }
        
        // append the new file to the base and return it
        return (parent + file);
    }
    
    
    /**
     * Returns whether or not the program is running in a HubZero environment.
     * The program is designed primarily to be used in one of two ways.  It 
     * is meant to be used either by the user directly as a desktop application
     * on their own workstation or as an installed tool hosted upon a HubZero
     * web-site within an embedded tool session window.  The program needs a
     * good way to determine whether it is running in this environment or not.
     * 
     * @return whether or not the program is running in a HubZero environment.
     */
    public static boolean isHubZeroSystem() {
        return Utils.isHubZeroSystem();
    }
    
    
    /**
     * Reassigns the System.out stream to the given stream.  If the stream
     * provided is null and the System.out has previously been reassigned,
     * the original System.out will be restored.
     * 
     * @param newSystemOutStream the new stream to use for System.out.
     */
    public static void setSystemOutputStream(PrintStream newSystemOutStream) {
        if (newSystemOutStream == null) {
            // no stream has been given so restore original if reassigned
            if (REAL_SYSTEM_OUT != null && 
                !REAL_SYSTEM_OUT.equals(System.out)) {
                System.setOut(REAL_SYSTEM_OUT);
            }
        }
        else {
            // replace existing system out with given stream
            System.setOut(newSystemOutStream);
        }
    }
    
    
    /**
     * Reassigns the System.err stream to the given stream.  If the stream
     * provided is null and the System.err has previously been reassigned,
     * the original System.err will be restored.
     * 
     * @param newSystemErrorStream the new stream to use for System.err.
     */
    public static void setSystemErrorStream(PrintStream newSystemErrorStream) {
        if (newSystemErrorStream == null) {
            // no stream has been given so restore original if reassigned
            if (REAL_SYSTEM_ERR != null &&
                !REAL_SYSTEM_ERR.equals(System.err)) {
                System.setErr(REAL_SYSTEM_ERR);
            }
        }
        else {
            // replace existing system err with given stream
            System.setErr(newSystemErrorStream);
        }
    }
    
    
    /**
     * Reassigns the System.in stream to the given stream.  If the stream
     * provided is null and the System.in has previously been reassigned,
     * the original System.in will be restored.
     * 
     * @param newSystemInputStream the new stream to use for System.in.
     */
    public static void setSystemInputStream(InputStream newSystemInputStream) {
        if (newSystemInputStream == null) {
            // no stream has been given so restore original if reassigned
            if (REAL_SYSTEM_IN != null && !REAL_SYSTEM_IN.equals(System.in)) {
                System.setIn(REAL_SYSTEM_IN);
            }
        }
        else {
            // replace existing system in with given stream
            System.setIn(newSystemInputStream);
        }
    }
    
        
}//end class edu.purdue.rcac.blastgui.util.SystemUtils
