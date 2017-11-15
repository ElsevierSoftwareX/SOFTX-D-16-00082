package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.Utils;
import edu.purdue.rcac.climatedata.worker.MapWorker;

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
 * A handler class for "create" button on "visualize" tab.
 * Parse strings from a input file name then pass appropriate parameters 
 * to R script to generate map results.
 */
public class CreateButtonAction implements ActionListener
{
	private MainFrame gui;
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());

	
	public CreateButtonAction(MainFrame gui)
	{
		this.gui = gui;
	}
	
	public void actionPerformed(ActionEvent arg0) {

		if (gui.getMapProgress() == true) {
			JOptionPane.showMessageDialog(null,
					"Please wait until the processing completed",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		gui.getVisualizeProgressBar().setIndeterminate(true);
		gui.setMapProgress(true);
		if (gui.getMapSourceFile() == null) {
			JOptionPane.showMessageDialog(null,
					"Select a source file to generate maps!",
					"Warning", JOptionPane.WARNING_MESSAGE);
			gui.getVisualizeProgressBar().setIndeterminate(false);
			gui.setMapProgress(false);
			return;
		}
		gui.getLblMaplabel().setIcon(new ImageIcon(Utils.scaleImage(630,
				420, gui.getMapURL())));
		gui.getLblMaplabel().revalidate();

		// mapsourcefile : path + xxx.csv
		// subfilename = path + xxx
		String subfilename = gui.getMapSourceFile().substring(0,
				gui.getMapSourceFile().length() - 4); // remove
												// extension
												// :
												// .csv

		StringTokenizer st = new StringTokenizer(subfilename, "_");
		int totalTokens = st.countTokens();
		if (!(totalTokens == 10)) 
		{
			JOptionPane
					.showMessageDialog(
							null,
							"Invalid input file. Please check the input file again!",
							"Warning", JOptionPane.WARNING_MESSAGE);
			gui.getVisualizeProgressBar().setIndeterminate(false);
			gui.setMapProgress(false);
			return;
		}
		
		String climate = st.nextToken();
		String bced =  st.nextToken();
		String observeFromYear =  st.nextToken();
		String observeToYear =  st.nextToken();
		String gcm2 = st.nextToken();
		String rcp2 =  st.nextToken();
		String years = st.nextToken();
		
		gui.setGcm(gcm2);
		gui.setRcp(rcp2);
		gui.setClimate(climate);
		
		int start_year, end_year;
		StringTokenizer yt = new StringTokenizer(years,"-");
		if(yt.countTokens() == 2)
		{
			start_year = Integer.parseInt(yt.nextToken());
			end_year = Integer.parseInt(yt.nextToken());
			gui.setSyear(start_year);
			gui.setEyear(end_year);
		}
		else
		{
			start_year = Integer.parseInt(yt.nextToken());
			end_year = start_year;
			gui.setSyear(start_year);
			gui.setEyear(start_year);
		}
		
		
		logger.info("year: " + gui.getSyear() + "-" + gui.getEyear());
		
		String weight = st.nextToken();
		String growing = st.nextToken();
		
		if(totalTokens == 10)
		{
			String region = st.nextToken();
			
			System.out.println("weight : " + weight + " region : " + region);
			
			if(!region.startsWith("WorldId"))
			{
				JOptionPane
				.showMessageDialog(
						null,
						"At this point it is only possible to display country-level aggregations.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				gui.getVisualizeProgressBar().setIndeterminate(false);
				gui.setMapProgress(false);
				
				return;
			}
		}				
		
		String c = gui.getClimate();
		StringTokenizer st2 = new StringTokenizer(c, "/");
		while(st2.hasMoreTokens())
		{
			c = st2.nextToken();
		}
		
		String climateName;
		String abbr;
		if (c.equals("tas")) {
			climateName = "Average_Surface_Air_Temperature";
			abbr = "tas";
		} else if (c.equals("tasmax")) {
			climateName = "Maximum_Surface_Air_Temperature";
			abbr = "tasmax";
		} else if (c.equals("tasmin")) {
			climateName = "Minimum_Surface_Air_Temperature";
			abbr = "tasmin";
		} else if (c.equals("pr")) {
			climateName = "Precipitation";
			abbr = "pr";
		} else {
			climateName = "Others";
			abbr = "other";
		}
		
		String gcmName = gui.getGcm();
		
		if (gcmName.equals("hadgem2-es")) {
			gcmName = "HadGEM2-ES";
		} else if (gcmName.equals("ipsl-cm5a-lr")) {
			gcmName = "IPSL-CM5A-LR";
		} else if (gcmName.equals("miroc-esm-chem")) {
			gcmName = "MIROC-ESM-CHEM";
		} else if (gcmName.equals("gfdl-esm2m")) {
			gcmName = "GFDL-ESM2M";
		} else if (gcmName.equals("noresm1-m")) {
			gcmName = "NorESM1-M";
		} else
			gcmName = "Others";
		
		
		
		String rcpName = gui.getFullNameOfParameter(gui.getRcp());
		if (rcpName.equals("Historical")) {
			rcpName = "Historical";
		} else if (rcpName.equals("RCP8.5")) {
			rcpName = "8.5";
		} else if (rcpName.equals("RCP6.0")) {
			rcpName = "6.0";
		} else if (rcpName.equals("RCP4.5")) {
			rcpName = "4.5";
		} else if (rcpName.equals("RCP2.6")) {
			rcpName = "2.6";
		} else
			rcpName = "Others";
		
		logger.info(climateName);
		logger.info(gcmName);
		logger.info(rcpName);
		
		gui.setMapPath(subfilename + ".png");

		String command = "Rscript " + " " + gui.getMapRunName() + " "
				+ gui.getMapGeneratorName() + " " + gui.getSyear() + " " + gui.getEyear() + " "
				+ subfilename + " " + climateName + " " + gcmName + " " + rcpName + " " + abbr;
		logger.info("command: " + command);
		logger.info("mapsourcefile: " + gui.getMapSourceFile());

		new MapWorker(command, subfilename, gui).execute();

	}
}