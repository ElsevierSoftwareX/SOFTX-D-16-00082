package edu.purdue.rcac.climatedata.worker;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.Utils;

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
 *
 * A helper class to handle a process to make a zip file. 
 */
public class ZipWorker extends SwingWorker<String, Void>
{

	private MainFrame gui;
	
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	private String cmd;
	private String zipFilePath;
	
	public ZipWorker(MainFrame gui, String cmd, String zipFilePath) 
	{
		this.gui = gui;
		this.cmd = cmd;
		this.zipFilePath = zipFilePath;
	}
	
	@Override
	protected String doInBackground() throws Exception
	{
		String output = runCommand(cmd, zipFilePath);
		return output;
	}

	@Override
	public void done() {
		try 
		{
			String output = get();
//			gui.getScriptoutputTextArea().append(output);
			logger.info(output);
			MainFrame.setDownloadProgress(false);
			gui.getMainProgressBar().setIndeterminate(false);
			gui.getOutputTextArea().append("Done!\n");
			
			if(output.equals("DONE"))
			{
				File f = new File(zipFilePath);
				if(f.exists())
				{
					logger.info("Download data : " + f.toString());
					Utils.downloadFile(f.toString());
				}
				else
				{
					JOptionPane.showMessageDialog(null,
							"No File Exists", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
			
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * @param command linux command to make a zip file.
	 * @param zipfilePath path to the zip file
	 * @return a string about results.
	 * @throws InterruptedException
	 * 
	 * Helper function to run a process to make a zip file.
	 */
	private String runCommand(String command, String zipfilePath) throws InterruptedException
	{
		try
		{
			
			File zip = new File(zipfilePath);
			String workingDirectoryPath = zip.getParent();
			File workingDirectory = new File(workingDirectoryPath);
			
			logger.info("zip command : " + command);
			Process p = Runtime.getRuntime().exec(command, null, workingDirectory);
			p.waitFor();
			
		} 
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		return "DONE";
	}
	
	
	
	
}
