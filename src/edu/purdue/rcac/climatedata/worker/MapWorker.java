package edu.purdue.rcac.climatedata.worker;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.Utils;
import edu.purdue.rcac.climatedata.Vars;

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
 * This class is a helper class to generate results maps.
 * A cmd string contains a command line to call Rscript.
 *  
 */
public class MapWorker extends SwingWorker<String, Void> {

	private String command;
	private String subfilename;
	private MainFrame gui;
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());


	public MapWorker(String cmd, String filename, MainFrame gui) {
		command = cmd;
		subfilename = filename;
		this.gui = gui;
	}

	@Override
	public String doInBackground() throws IOException {
		String output = runCommand(command);
		return output;
	}

	@Override
	public void done() {
		try {
			String results = get();
			gui.getScriptoutputTextArea().append(results);
			logger.info(results);
			
			if (results.equals("Map Generated!")) 
			{
				JOptionPane.showMessageDialog(null, results, "Info",
						JOptionPane.INFORMATION_MESSAGE);
			} 
			else 
			{
				JOptionPane.showMessageDialog(null, results, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			gui.getScriptoutputTextArea().append(ex.toString());
			logger.info(ex.toString());
		}
	}

	/**
	 * @param rcommand linux command line to be executed in a process.
	 * @return a comment for result.
	 * @throws IOException
	 * 
	 * A helper function to execute a Rscript command in a separated process
	 * to generate result maps. After the scripts are done, update GUI components
	 * to connect with the result files.
	 */
	private String runCommand(String rcommand) throws IOException {

		gui.getScriptoutputTextArea().append("Generating Maps\n");
		gui.getScriptoutputTextArea().append("Command:" + command + "\n");
		logger.info("Generating Maps\n");
		logger.info("Command:" + command + "\n");
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			String outputString = Utils.readOutput(p.getInputStream());
			String errorString = Utils.readOutput(p.getErrorStream());
			// TODO to be removed
			if (!outputString.isEmpty()) {
				gui.getScriptoutputTextArea().append(outputString);
				logger.info(outputString);
			}
			if (!errorString.isEmpty()) {
				gui.getScriptoutputTextArea().append(errorString);
				logger.info(errorString);
			}
		} catch (IOException e) {
			gui.getVisualizeProgressBar().setIndeterminate(false);
			gui.setMapProgress(false);
			logger.info(e.toString());
			e.printStackTrace();
		}
		
		logger.info("mappath: " + gui.getMapPath());
		File mapfile = new File(gui.getMapPath().substring(0,gui.getMapPath().length()-4) + "_" + gui.getSyear() + ".png");
		
		logger.info("mappath: " + mapfile.toString());
		
		gui.getLblMaplabel().setIcon(new ImageIcon(Utils.scaleImage(Vars.imgWidth, Vars.imgHeight,
				mapfile)));
		
		gui.getSlider().setMinimum(gui.getSyear());
		gui.getSlider().setMaximum(gui.getEyear());
		int term = gui.getEyear() - gui.getSyear();
		int index = 1;
		if (term > 20) {
			index = 5;
		}
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for (int i = gui.getSyear(); i <= gui.getEyear(); i++) {
			if (i % index == 0)
				labelTable.put(new Integer(i),
						new JLabel(Integer.toString(i)));
		}
				
		gui.getSlider().setLabelTable(labelTable);
		gui.getSlider().revalidate();

		gui.setImgZipFile(subfilename + "_maps" + ".zip");
		
		Vector<String> imagefilelist = new Vector<String>();
		for (int i = gui.getSyear(); i <= gui.getEyear(); i++) {
			imagefilelist.add(subfilename + "_" + i + ".png");
		}

		try {
			logger.info("imgzipfile: " + gui.getImgZipFile());
			logger.info("imagefilelist: " + imagefilelist.toString());
			Utils.createZipArchive(imagefilelist, gui.getImgZipFile(), false,
					false);
		} catch (IOException e) {
			logger.info(e.toString());
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		gui.getVisualizeProgressBar().setIndeterminate(false);
		gui.setMapProgress(false);
		File file = new File(gui.getImgZipFile());
		String output;
		if (file.isFile() && file.exists()) {
			gui.getSlider().setValue(gui.getSlider().getMinimum());
			output = "Map Generated!";
		} else {
			output = "Process completed abnomally";
		}
		return output;

	}
}