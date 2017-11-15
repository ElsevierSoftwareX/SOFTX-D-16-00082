package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.Utils;
import edu.purdue.rcac.climatedata.worker.ZipWorker;

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
 * A handler class to make a zip file for raw data to be downloaded.
 */
public class DownloadRawDataHandler implements ActionListener
{
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	private static final String ZIP_EXE = "/usr/bin/zip";
	private MainFrame gui;
	
	public DownloadRawDataHandler(MainFrame gui)
	{
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (MainFrame.getDownloadProgress() == true) {
			JOptionPane.showMessageDialog(null,
					"Please wait until fetching completed",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String path = "";
		if (gui.getGcm().equals("") || gui.getRcp().equals("")
				|| gui.getClimate().equals("")) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please check all the options for crop data before clicking on \"Download Raw Data\"",
							"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}


		path = gui.getTxtPathfields().getText();

		if (path.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					"Please build a path by selecting Crop Data",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String downloadpath = gui.getDefaultDownloadPath() + path;
		
		logger.info("path : " + downloadpath + "\n");
		
		String variable = gui.getClimate();
		downloadpath = downloadpath.substring(0, downloadpath.length() - variable.length());
		
		File directories = new File(downloadpath);
		String[] children = directories.list();
		
		String max = "";
		if(children != null)
		{	
			for(int i = 0; i < children.length; i++)
			{
				String t = children[i];
				
				if(t.startsWith(variable + "_")) 
				{
					if(t.compareTo(max) > 0)
						max = t;
					
				}
				
			}
		}
		logger.info("max " + max + " added\n");

		//make real path!!		
		downloadpath = downloadpath + max;
		
		logger.info("new path : " + downloadpath + "\n");		
		
		// check if files exist
		File target = new File(downloadpath);
		
		if (target.exists() && target.isDirectory()) 
		{
			File[] list = target.listFiles();
			logger.info("number of files in the list " + list.length);
			
			if(list.length == 0)
			{
				JOptionPane
						.showMessageDialog(
								null,
								"The selected dataset has not been fetched.\nPlease fetch the data and try again.",
								"Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			else
			{
				logger.info("Create zip file into temp directory");
				String command = "zip  -j -r " ;
				
				
				String zipfilePath = gui.getTempPath() + "Climate_raw_data_" + 
								gui.getGcm() + "_" + gui.getRcp() + "_" +
								gui.getClimate() + ".zip";
				
				command += zipfilePath + " ";
				
				for(File file : list)
				{
					command += file.toString() + " ";
					logger.info(file.toString());
				}
						
				File previousZipFile = new File(zipfilePath);
				if(previousZipFile.exists())
				{
					logger.info("Download data : " + previousZipFile.toString());
					Utils.downloadFile(previousZipFile.toString());
					return;
				}
				
				MainFrame.setDownloadProgress(true);
				gui.getMainProgressBar().setStringPainted(false);
				gui.getMainProgressBar().setIndeterminate(true);
				gui.getOutputTextArea().append("Compressing files..\n");
				
				ZipWorker zw = new ZipWorker(gui, command, zipfilePath);
				zw.execute();

				return;
			}
		}
		else
		{
			JOptionPane
				.showMessageDialog(
						null,
						"The selected dataset has not been fetched.\nPlease fetch the data and try again.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}		
	}
	
	
}
