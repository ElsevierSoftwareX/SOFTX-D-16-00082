package edu.purdue.rcac.climatedata;


import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.json.JSONArray;
import org.json.JSONObject;

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
 * This class is used to download files from Globus Online to Geoshare server. 
 * File transfer is handled by "submit isimiptransfer" program, which is 
 * installed on the Geoshare workspace.
 * A list of target data is listed by "submit isimiptransfer --ls <path>" as a JSON format
 * and the actual fetching is done by "submit isimiptransfer --get <path>".
 */
public class DownloadManager extends Thread {

	private String path;
	private String variable;
	private JTextArea outarea;
	private MainFrame gui;
	
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());

	/**
	 * @param gui		to control over MainFrame
	 * @param path		the path to target files
	 * @param variable	variable name
	 * @param area		to show a log messages
	 * 
	 * A constructor of the DownloadManager
	 */
	public DownloadManager(MainFrame gui, String path, String variable, JTextArea area) {
		this.path = path;
		this.outarea = area;
		this.variable = variable;
		this.gui = gui;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 * 
	 * An inherited thread will execute this method
	 */
	public void run() {
		MainFrame.setDownloadProgress(true);
		StringBuffer sb = new StringBuffer();
		Process p1, p2;
		BufferedReader br;
		MainFrame.setCounter(0);
		String line = null;
		String outputString = "";
		String errorString = "";
		String max = "";
		Vector<String> filenames = new Vector<String>();
		Hashtable<String, Integer> filesizes = new Hashtable<String, Integer>();
		
		try {
			p1 = Runtime.getRuntime().exec("submit isimiptransfer --ls " + path.substring(0, path.length() - variable.length()));
			outarea.setText("running command: "+ "submit isimiptransfer --ls " + path.substring(0, path.length() - variable.length()) + "\n");
			logger.info("running command: "+ "submit isimiptransfer --ls " + path.substring(0, path.length() - variable.length()) + "\n");

			MainFrame.setCounter(10);
			outputString = Utils.readOutput(p1.getInputStream());
			errorString = Utils.readOutput(p1.getErrorStream());
			outarea.append(outputString.toString());
			outarea.append(errorString.toString());
			logger.info(outputString.toString());
			logger.info(errorString.toString());
			
			outputString = outputString.substring(outputString.toString().indexOf("{"));
			
			if(outputString.isEmpty() || outputString.equals("") || outputString == null) {
				outarea.append("error occured during download process");
				logger.info("error occured during download process");
				return;
			}
			
			JSONObject jsonoutput1 = new JSONObject(outputString);
			JSONObject jsoncontent1 = (JSONObject) jsonoutput1.get(path.substring(0, path.length() - variable.length()));
			JSONArray jsondirectories = (JSONArray) jsoncontent1.getJSONArray("directories");
			
			for(int i = 0; i < jsondirectories.length(); i++)
			{
				String t = jsondirectories.getString(i);
				if(t.startsWith(variable + "_")) 
				{
					if(t.compareTo(max) > 0)
						max = t;	
				}
				
			}			
			logger.info("max " + max + " added\n");

			//make real path!!
			path = path.substring(0, path.length() - variable.length()) + max;
			
			p1 = Runtime.getRuntime().exec("submit isimiptransfer --ls " + path);
			outarea.append("running command: "+ "submit isimiptransfer --ls " + path + "\n");
			logger.info("running command: "+ "submit isimiptransfer --ls " + path + "\n");

			MainFrame.setCounter(10);			
			outputString = Utils.readOutput(p1.getInputStream());
			errorString = Utils.readOutput(p1.getErrorStream());
			outarea.append(outputString.toString());
			outarea.append(errorString.toString());
			logger.info(outputString.toString());
			logger.info(errorString.toString());

			outputString = outputString.substring(outputString.toString().indexOf("{"));
			
			if(outputString.isEmpty() || outputString.equals("") || outputString == null) {
				outarea.append("error occured during download process");
				logger.info("error occured during download process");
				return;
			}
			
			JSONObject jsonoutput = new JSONObject(outputString);
			JSONObject jsoncontent = (JSONObject) jsonoutput.get(path);
			JSONObject jsonfiles = (JSONObject) jsoncontent.get("files");
			Iterator<String> iterFilename = jsonfiles.keys();
            while (iterFilename.hasNext()) {
            	String key = iterFilename.next();
            	JSONObject filedescription = (JSONObject)jsonfiles.get(key);
            	
            	//TODO filtering out files not related with "yield"
            	if(key.contains("bced")) {
            		filenames.add(key);
            		filesizes.put(key, (Integer)filedescription.get("size"));
            	}
            }
            
			int temp = 100 - MainFrame.getCounter();
			int add = 0;
			if (filenames.size() != 0) {
				add = temp / filenames.size();
				outarea.append("requesting files: \n");
				for(int i = 0; i < filenames.size(); i++) {
					outarea.append(filenames.get(i) + "\n");
				}
			} else {
				MainFrame.setCounter(100);	
				JOptionPane.showMessageDialog(null, "There is no data matching your selection in the climate data archive. Please select again.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				MainFrame.setDownloadProgress(false);
				return;
			}
			
			for(int i = 0; i < filenames.size(); i++) {
				int currentnum = i+1;
				outarea.append("transfering... : " + currentnum + " / " + filenames.size() + "\n");
				p2 = Runtime.getRuntime().exec("submit isimiptransfer --get " + path + "/" + filenames.get(i));
				outarea.append("running command: "+ "submit isimiptransfer --get " + path + "/" + filenames.get(i) + "\n");
				logger.info("running command: "+ "submit isimiptransfer --get " + path + "/" + filenames.get(i) + "\n");
				outputString = Utils.readOutput(p2.getInputStream());
				errorString = Utils.readOutput(p2.getErrorStream());
				outarea.append(outputString.toString());
				outarea.append(errorString.toString());
				logger.info(outputString.toString());
				logger.info(errorString.toString());
				
				this.sleep(100);
				MainFrame.setCounter(MainFrame.getCounter() + add);
			}

		} catch (Exception e) {
			sb.append(e.toString());
			outarea.append(sb.toString());
			logger.info(e.getMessage());
			e.printStackTrace();
			MainFrame.setDownloadProgress(false);
			return;
		}
		MainFrame.setCounter(100);
		gui.setDownloadPath(gui.getDefaultDownloadPath() + path);
		outarea.append("* Download Complete with " + max + " *\n\n");
		MainFrame.setDownloadProgress(false);
	}
}
