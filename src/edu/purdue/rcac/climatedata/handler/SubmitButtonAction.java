package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.MainFrame.Pair;
import edu.purdue.rcac.climatedata.worker.ProgressWorker;

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
 * A handler class to make command list to aggregate input files.
 * ProgressWorker will execute the generated commands in its threadd.
 */
public class SubmitButtonAction extends AbstractAction 
{
	private MainFrame gui;
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	public SubmitButtonAction(MainFrame gui) 
	{
		putValue(NAME, "Run");
		this.gui = gui;
	}

	// Run Aggregation
	public void actionPerformed(ActionEvent e) 
	{
		gui.setOutputFileList(new Vector<String>());
		
		
		if (gui.getRunProgress() == true) {
			JOptionPane.showMessageDialog(null,
					"Please wait until the processing completed",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		gui.getScriptoutputTextArea().setText("Starting Processing...\n");
		logger.info("Starting Processing...\n");
		if (gui.getClimateFileList() == null) {
			JOptionPane.showMessageDialog(null,
					"Please select an Climate data file", "Warning",
					JOptionPane.WARNING_MESSAGE);
			gui.getScriptoutputTextArea().append("no climate data file.");
			logger.info("no climate data file.");
			gui.getCommandProgressbar().setIndeterminate(false);
			gui.setRunProgress(false);
			return;
		}
		int agmipfilenum = gui.getClimateFileList().length;
		logger.info("agmipfilenum :" + agmipfilenum);
		Vector<String> commandlist = new Vector<String>();

		// agmipfile = new File(txtAgmip.getText());
		gui.setClimateFile( new File(gui.getTxtRegionFile().getText()) );
		gui.setRunProgress(true);

		gui.getCommandProgressbar().setIndeterminate(true);
		int inputstatus = validateInput();
		if (inputstatus == 1) {
			JOptionPane.showMessageDialog(null,
					"Please select an climate data file", "Warning",
					JOptionPane.WARNING_MESSAGE);
			gui.getScriptoutputTextArea().append("no AgMIP input file");
			logger.info("no climate data file.");
			gui.getCommandProgressbar().setIndeterminate(false);
			gui.setRunProgress( false );
			return;
		} else if (inputstatus == 2) {
			JOptionPane.showMessageDialog(null,
					"Please select a Region Map", "Warning",
					JOptionPane.WARNING_MESSAGE);
			gui.getScriptoutputTextArea().append("no Region map file");
			logger.info("no Region map file");
			gui.getCommandProgressbar().setIndeterminate(false);
			gui.setRunProgress( false );
			return;
		} else if (inputstatus == 3) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please either provide a weight file for weighted averages or select a summary statistic.",
							"Warning", JOptionPane.WARNING_MESSAGE);
			gui.getScriptoutputTextArea().append("No weight map file");
			logger.info("No weight map file");
			gui.getCommandProgressbar().setIndeterminate(false);
			gui.setRunProgress( false );
			return;
		}
		
		if(gui.getChckbxEnableGrowingSeasons().isSelected())
			gui.setGrowingSeasonFile(new File(gui.getTextGrwoingFile().getText()));
		else
			gui.setGrowingSeasonFile(null);

		String command;
		gui.setPeriods( new ArrayList<Pair>() );
		if (gui.getRdbtnMax().isEnabled() && gui.getRdbtnMax().isSelected()) {
			gui.setFunctioName("max");
		} else if (gui.getRdbtnMean().isEnabled() && gui.getRdbtnMean().isSelected()) {
			gui.setFunctioName("mean");
		} else if (gui.getRdbtnMin().isEnabled() && gui.getRdbtnMin().isSelected()) {
			gui.setFunctioName("min");
		} else if (gui.getRdbtnSd().isEnabled() && gui.getRdbtnSd().isSelected()) {
			gui.setFunctioName("sd");
		}
		
//		ArrayList<Integer> startYearList = new ArrayList<Integer>();
//		ArrayList<Integer> endYearList = new ArrayList<Integer>();
//		ArrayList<String> climateFileList = new ArrayList<String>();
		String lon = "lon";
		String lat = "lat";
		
		try {
			for (int i = 0; i < gui.getClimateFileList().length; i++) {
				gui.setClimateFile(gui.getClimateFileList()[i]);
				if (gui.getClimateFile().getAbsolutePath() == gui.getClimateFileName()) 
				{
					JOptionPane.showMessageDialog(null,
							"Please select an climate data file", "Warning",
							JOptionPane.WARNING_MESSAGE);
					gui.getScriptoutputTextArea().append("no climate data file.");
					logger.info("no climate data file");
					gui.getCommandProgressbar().setIndeterminate(false);
					gui.setRunProgress( false );
					return;
				}
				String climateFileName = gui.getClimateFile().getName();
				int start_year = 0;
				int end_year = 0;
				
				
				String subfilename = climateFileName.substring(0,
						climateFileName.lastIndexOf(".mm.nc"));

				System.out.println(climateFileName);
				System.out.println(subfilename);

				StringTokenizer st = new StringTokenizer(subfilename,
						"_");
				if (st.countTokens() != 7) {
					gui.getScriptoutputTextArea()
							.append("Invalid input files. Please check input files again! \n");
					logger.info("invalid input file");
					gui.getCommandProgressbar().setIndeterminate(false);
					gui.setRunProgress(false);
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

				
				gui.getPeriods().add(gui.new Pair(start_year, end_year));


				int index = i + 1;
				gui.getScriptoutputTextArea().append("Processing Input...[" + index
						+ "/" + agmipfilenum + "] \n");
				gui.getScriptoutputTextArea()
						.append("===========================\n");
				gui.getScriptoutputTextArea().append("Climate File: " + gui.getClimateFile()
						+ "\n");
				gui.getScriptoutputTextArea().append("Region File: " + gui.getRegionFile()
						+ "\n");
				gui.getScriptoutputTextArea().append("Function: " + gui.getFunctioName()
						+ "\n");
				gui.getScriptoutputTextArea().append("Weight File: " + gui.getWeightFile()
						+ "\n");
				gui.getScriptoutputTextArea().append("Year: " + start_year + "-"
						+ end_year + "\n");
				gui.getScriptoutputTextArea().append("Variable ID: " + gui.getClimate() + "\n");
				
				gui.getScriptoutputTextArea().append("Growing File: " + gui.getGrowingSeasonFile() + "\n");
				gui.getScriptoutputTextArea()
						.append("===========================\n");

				logger.info("Processing Input...[" + index + "/"
						+ agmipfilenum + "] \n");
				logger.info("===========================\n");
				logger.info("Climate File: " + gui.getClimateFile() + "\n");
				logger.info("Region File: " + gui.getRegionFile() + "\n");
				logger.info("Function: " + gui.getFunctioName() + "\n");
				logger.info("Weight File: " + gui.getWeightFile() + "\n");
				logger.info("Year: " + start_year + "-" + end_year + "\n");
				logger.info("Variable ID: " + gui.getClimate() + "\n");
				logger.info("Growing File: " + gui.getGrowingSeasonFile() + "\n");
				logger.info("===========================\n");


				if (gui.getWeightFile() == null) {
					gui.setOutputFile( gui.getOutputPath() + subfilename + "_" + gui.getFunctioName() );
				} else {
					gui.setOutputFile( gui.getOutputPath() + subfilename + "_weight" );
				}
				
				if(gui.getGrowingSeasonFile() == null) {
					gui.setOutputFile( gui.getOutputFile() + "_noGR");
				}
				else {
					gui.setOutputFile( gui.getOutputFile() + "_GR");
				}
				
				
				System.out.println(gui.getRegionFile().getName());
				
				gui.setOutputFile(gui.getOutputFile() + "_" + gui.getRegionFile().getName()
									.substring(0, gui.getRegionFile().getName().length() - 4));
				
				gui.setOutputFile(gui.getOutputFile() + ".csv");
				
				
				///////////////////////////////////////////////////////////////////////
				command = "Rscript " + gui.getClimateRunName() + " " + gui.getClimateFnsName()
						+ " " + gui.getClimateFile() + " " + gui.getRegionFile() + " " + gui.getGrowingSeasonFile() 
						+ " " + gui.getFunctioName() + " " + gui.getWeightFile() + " " + start_year
						+ " " + end_year + " " + gui.getClimate() + " " + gui.getOutputFile()
						+ " " + lon + " " + lat; 
//						 i = 0; i < gui.getClimateFileList().length;
						
				if (i == 0)
					command += " null";
				else
					command += " " + gui.getTempPath() + "temp_" + (start_year-1) + ".csv";
				
				if( i == gui.getClimateFileList().length - 1 )
					command += " null";
				else
					command += " " + gui.getTempPath() + "temp_" + end_year + ".csv";
						
				commandlist.add(command);
				gui.getOutputFileList().add(gui.getOutputFile());
				
//				climateFileList.add(gui.getClimateFile().toString());
//				startYearList.add(start_year);
//				endYearList.add(end_year);
				
			}
		} catch (Exception e2) {
			gui.getScriptoutputTextArea().append("unexpected errors! \n");
			logger.severe("unexpected errors!");
			e2.printStackTrace();
			gui.getCommandProgressbar().setIndeterminate(false);
			gui.setRunProgress(false);
			return;
		}

		gui.getScriptoutputTextArea().append("Running R...\n");
		logger.info("Running R...");
		
		
		gui.getBtnSubmit().setEnabled(false);
		gui.getBtnReset().setEnabled(false);
		
		new ProgressWorker(commandlist, gui).execute();
	}

	/**
	 * @return error number from 1 to 4.
	 * Returns an error number. 
	 */
	private int validateInput() {
		if (gui.getTxtClimate().getText().equals("") || gui.getTxtClimate().getText() == null
				|| gui.getTxtClimate().getText().endsWith("Select climate files")) {
			return 1;
		}
		if (gui.getClimateFileList() == null || gui.getClimateFileList().length == 0) {
			logger.info("climatefilelist is null");
			return 1;
		}
		if (gui.getRegionFile() == null || !gui.getRegionFile().exists()) {
			logger.info("regionfile is null");
			return 2;
		}
		if (gui.getWeightFile() == null
				&& !(gui.getRdbtnMax().isSelected() || gui.getRdbtnMean().isSelected()
						|| gui.getRdbtnMin().isSelected() || gui.getRdbtnSd().isSelected())) {
			logger.info("weightfile is null && function not selected");
			return 3;
		}
		return 4;
	}
}