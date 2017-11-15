package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.Utils;
import edu.purdue.rcac.climatedata.MainFrame.Pair;

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
 * A handler class to generate / provide citation strings for raw data.
 */
public class DownloadRawDataCitationHandler  implements ActionListener
{
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	private MainFrame gui;

	public DownloadRawDataCitationHandler(MainFrame gui)
	{
		this.gui = gui;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		
		String path = "";
		if (gui.getGcm().equals("") || gui.getRcp().equals("") || gui.getClimate().equals("")) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please check all the options for crop data to see suggested citation",
							"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		/**
		 * make suggested citation
		 */
		
		// calculate periods
		StringBuffer citationString = new StringBuffer();
		String citationFileName = "";
		
		try {

			
			logger.info("making citaion: climateFileName " + gui.getClimateFileName());
			logger.info("climate " + gui.getClimate());
			String c = gui.getClimate();
			String climateName;
			if (c.equals("tas")) {
				climateName = "Average Surface Air Temperature";
			} else if (c.equals("tasmax")) {
				climateName = "Maximum Surface Air Temperature";
			} else if (c.equals("tasmin")) {
				climateName = "Minimum Surface Air Temperature";
			} else if (c.equals("pr")) {
				climateName = "Precipitation";
			} else {
				climateName = "Others";
			}
			logger.info("climateName " + climateName);
		
			String rcp = gui.getFullNameOfParameter(gui.getRcp());
			String gcmName = gui.getFullNameOfParameter(gui.getGcm());

			citationString.append("Grid-cell level " + climateName + " from " + gcmName + " " 
									+ "under representative concentration pathway " + rcp + " "
									+ "downloaded from the ISI-MIP ESGF Node (details in Elliott et al., 2014).");
			
			citationString.append("\r\n\r\n\r\n");
			citationString.append("References");
			citationString.append("\r\n\r\n\r\n");
			
			citationString.append("Elliott, J., C. Mueller, D. Deryng, J. Chryssanthacopoulos, K. J. Boote, M. Buechner, I. Foster, et al. "
					+ "\"The Global Gridded Crop Model Intercomparison: Data and Modeling Protocols for Phase 1 (v1.0).\" "
					+ "Geosci. Model Dev. Discuss. 7, no. 4 (July 15, 2014): 4383-4427.");
	
			citationFileName = gui.getTempPath() + "Data_Description_" + gui.getGcm() + "_"  + gui.getRcp() + "_" + gui.getClimate() + ".txt";

		} catch (Exception e1) {
			logger.info(e1.toString());
			e1.printStackTrace();
		}
		
		
		logger.info("citation file: " + citationFileName);
		logger.info(citationString.toString());

		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(citationFileName), "UTF-8"));
			
			out.write(citationString.toString());
			out.flush();
			out.close();

		} 
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Download the file
		File f = new File(citationFileName);
		if(f.exists())
		{
			logger.info("Download citation data : " + f.toString());
			Utils.downloadFile(f.toString());
		}
		else
		{
			JOptionPane.showMessageDialog(null,
					"No File Exists", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		/**
		 * end making citations
		 */
		
	}

}
