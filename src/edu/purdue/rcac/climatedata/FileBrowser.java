package edu.purdue.rcac.climatedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

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
 * This class is to control a file system using external filebrowser.py program.
 * Each statement sets appropriate parameters for the filebrowser, then execute it.
 */
public class FileBrowser extends SwingWorker<String, Void>
{
	private MainFrame gui;
	private String desc;
	private String outputString="";
	private String errorString="";
	
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	
	/**
	 * @param gui	to control the MainFrame
	 * @param desc	description of the execution
	 * 
	 * desc will set an appropriate parameter for the execution of the file browser 
	 */
	public FileBrowser(MainFrame gui, String desc)
	{
		this.gui = gui;
		this.desc = desc;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 * 
	 * This method will be executed with the execute() call
	 */
	@Override
	protected String doInBackground() throws Exception
	{
		// make execution command
		String cmd = "python " + gui.getBrowserPath();
		
		if(desc.equals("saveToIdata"))
		{
			cmd += " " + "--save --suggest "; // current output file name
			String zip = gui.getZipFile().substring(gui.getZipFile().lastIndexOf("/") + 1, 
													gui.getZipFile().length());
			cmd += zip;
			cmd += " " + "--hide-session-dir";
			cmd += " " + "--hide-sdata-dir";
			cmd += " " + "--hide-home-dir";
		}
		
		else if(desc.equals("climateFileSelection"))
		{
			cmd += " " + "--open";
			cmd += " " + "--shortcut " + gui.getDefaultDownloadPath() + "/" + gui.getPrefix();
			cmd += " " + "--shortcutdesc " + "Climate_Data"; 
			cmd += " " + "--multiple";
//			cmd += " " + "--filter .nc4";
			cmd += " " + "--hide-session-dir";
			cmd += " " + "--hide-sdata-dir";
			cmd += " " + "--hide-home-dir";
			cmd += " " + "--hide-idata-projects-browser";
		}
		else if(desc.equals("regionFileSelection"))
		{
			cmd += " " + "--open";
			cmd += " " + "--shortcut " + gui.getRegionMapDir();
			cmd += " " + "--shortcutdesc " + "Region_Map"; 
			cmd += " " + "--filter .csv";
			cmd += " " + "--hide-session-dir";
			cmd += " " + "--hide-sdata-dir";
			cmd += " " + "--hide-home-dir";
//			cmd += " " + "--hide-idata-projects-browser";
		}
		else if(desc.equals("growingSeasonFileSelection"))
		{
			cmd += " " + "--open";
			cmd += " " + "--shortcut " + gui.getGrowingSeasonDir();
			cmd += " " + "--shortcutdesc " + "GS_File"; 
			cmd += " " + "--filter .RData";
			cmd += " " + "--hide-session-dir";
			cmd += " " + "--hide-sdata-dir";
			cmd += " " + "--hide-home-dir";
//			cmd += " " + "--hide-idata-projects-browser";
		}
		else if(desc.equals("weightFileSelection"))
		{
			cmd += " " + "--open";
			cmd += " " + "--shortcut " + gui.getWeightMapDir();
			cmd += " " + "--shortcutdesc " + "Weight_File"; 
			cmd += " " + "--filter .csv";
			cmd += " " + "--hide-session-dir";
			cmd += " " + "--hide-sdata-dir";
			cmd += " " + "--hide-home-dir";
//			cmd += " " + "--hide-idata-projects-browser";			
		}
		else if(desc.equals("visualizeFileSelection"))
		{
			cmd += " " + "--open";
			cmd += " " + "--shortcut " + gui.getOutputPath();
			cmd += " " + "--shortcutdesc " + "Output_File"; 
			cmd += " " + "--filter .csv";
			cmd += " " + "--hide-session-dir";
			cmd += " " + "--hide-sdata-dir";
			cmd += " " + "--hide-home-dir";
//			cmd += " " + "--hide-idata-projects-browser";	
		}
		else
		{
			cmd = "T";
		}
		
		System.out.println(cmd);
		
		String output = runCommand(cmd);
		return output;
	}
	
	/**
	 * A helper method called in done().
	 * This method will save files to iData repository.
	 */
	private void saveToIdata(){
		try
		{
			Process p1;
			p1 = Runtime.getRuntime().exec("cp " + gui.getZipFile() + " " + outputString);
			System.out.println(outputString + "<---");
			appendMetadata(outputString);
		} 
		catch (IOException e1)
		{
			gui.getScriptoutputTextArea().append("Export Failed \n");
			e1.printStackTrace();
		}
		
		gui.getScriptoutputTextArea().append("Export Completed! \n");
	}
	
	/**
	 * A helper method called in done().
	 * For the control of the selection of climate files,
	 * This method will examine selected files then check whether
	 * they are consecutive years of climate files or not.  
	 */
	private void climateFileSelection(){
		// get all file lies from the file browser
		String[] fileNameList = outputString.split(System.getProperty("line.separator"));
		File[] fileList = new File[fileNameList.length];
		int i = 0;
		
		for(String s : fileNameList)
		{
			fileList[i] = new File(s);
			i++;
		}
		gui.setClimateFileList(fileList);

		StringBuffer strings = new StringBuffer();
		for (File file : fileList)
		{
			strings.append(file.getPath()
					+ System.getProperty("line.separator"));
		}
		gui.getTxtClimate().setText(strings.toString());
		
		
		ArrayList<Integer> ar = new ArrayList<Integer>();
		int minStartYear = 9999;
		int maxEndYear = 0;
		
		// Iterate all of the selected files, then check whether they have consecutive years or not 
		// Name of the climate files needs to be parsed to extract "years" from the file name.
		for (i = 0; i < gui.getClimateFileList().length; i++) 
		{
//			logger.info(gui.getClimateFileList()[i].toString());
			
			String climateFileName = gui.getClimateFileList()[i].getName();
			String subfilename = climateFileName.substring(0,
					climateFileName.lastIndexOf(".mm.nc"));
			
			StringTokenizer st = new StringTokenizer(subfilename,"_");
			
			String climate = st.nextToken();
			String bced =  st.nextToken();
			String observeFromYear =  st.nextToken();
			String observeToYear =  st.nextToken();
			String gcm = st.nextToken();
			String rcp =  st.nextToken();
			String years = st.nextToken();
			
			int start_year = 0;
			int end_year = 0;
			
			StringTokenizer yt = new StringTokenizer(years,"-");
			if(yt.countTokens() == 2)
			{
				start_year = Integer.parseInt(yt.nextToken());
				end_year = Integer.parseInt(yt.nextToken());
			}
			else
			{
				start_year = Integer.parseInt(yt.nextToken());
				end_year = start_year;
			}
			
			for(int y = start_year ; y <= end_year ; y++)
			{
				ar.add(y);
			}
			
			if(start_year <= minStartYear)
				minStartYear = start_year;
			
			if(end_year >= maxEndYear)
				maxEndYear = end_year;
			
		}
		
		int numYears = maxEndYear - minStartYear + 1;
		
		Collections.sort(ar);
		
		// Check consecutive years
		boolean cons = true;
		for(int y = 0, t = minStartYear; y < ar.size(); y++, t++)
		{
			if( ar.get(y) != t )
				cons = false;
		}
		
		if(!cons)
		{
			JOptionPane.showMessageDialog(gui,
				"Data files need to have continuous years", "Warning",
				JOptionPane.WARNING_MESSAGE);
			
			gui.setClimateFileList(null);
			gui.getTxtClimate().setText(gui.getClimateFileName());
			
			return;
		}
		
		gui.setMinStartYear(minStartYear);
		gui.setMaxEndYear(maxEndYear);
	}
	
	@Override
	public void done() {
		try 
		{
			String output = get();
			if (output.equals("OK")) 
			{
				System.out.println("OK");
				System.out.println("Browser output is " + outputString);
				
				if(desc.equals("saveToIdata"))
				{
					this.saveToIdata();
				}
				else if(desc.equals("climateFileSelection"))
				{
					this.climateFileSelection();
				}
				else if(desc.equals("regionFileSelection"))
				{
					// set path to the region file into appropriate variable.
					// this will be used during the execution of aggregate.  
					String fileName = outputString.replace(System.getProperty("line.separator"), "");
					File file = new File(fileName);
					
					gui.getTxtRegionFile().setText(file.getAbsolutePath());
					gui.setRegionFile(new File(gui.getTxtRegionFile().getText()));
					
				}
				else if(desc.equals("growingSeasonFileSelection"))
				{
					// set path to the growing season file into appropriate variable.
					// this will be used during the execution of aggregate.
					String fileName = outputString.replace(System.getProperty("line.separator"), "");
					File file = new File(fileName);
					
					gui.getTextGrwoingFile().setText(fileName);
					gui.setGrowingSeasonFile(file);
					
				}
				else if(desc.equals("weightFileSelection"))
				{
					// set set path to the weight file into appropriate variable.
					// this will be used during the execution of aggregate.
					String fileName = outputString.replace(System.getProperty("line.separator"), "");
					File file = new File(fileName);
					
					gui.setWeightFile(file);
					gui.getTxtWeightFile().setText(file.toString());
					
					if(gui.getChckbxFunctionEnable().isSelected())
						gui.getChckbxFunctionEnable().doClick();

				}
				else if(desc.equals("visualizeFileSelection"))
				{
					// set selected input file to appropriate variable.
					// this will be used during the execution of visualization.
					String fileName = outputString.replace(System.getProperty("line.separator"), "");
					File file = new File(fileName);
					
					gui.getTextMapField().setText(file.getName());
					gui.setMapSourceFile(fileName);
				}
			} 
			else 
			{

			}
			
			
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param cmd string to be executed
	 * @return "OK" string or IOException
	 * Execute input cmd command in the system's process. 
	 */
	private String runCommand(String cmd) 
	{
		if( cmd.equals("T") )
		{
			appendMetadata("/srv/irods/ianstestproject/test3/dd4.zip");
		}
		else
		{
			try
			{
				Process p = Runtime.getRuntime().exec(cmd);
				outputString = Utils.readOutput(p.getInputStream());
				errorString = Utils.readOutput(p.getErrorStream());
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if (!outputString.isEmpty()) {
				logger.info(outputString);
			} else if (!errorString.isEmpty()) {
				logger.info(outputString);
				return "unexpected error occured while file browser handling.";
			} else {
				return "unexpected error occured while file browser handling.";
			}
		}
		return "OK"; 
	}
	
	/**
	 * @param target path to the file
	 * 
	 * Helper method to set metadata in the Geoshare's IData.
	 * Saving into iData requires a set of metadata information.
	 * e.g. title, subject, contributor, publisher, etc.
	 * This method will be called by saveToIdata().
	 */
	private void appendMetadata(String target)
	{
		// split to get session token
		String cmd = "";
		String output1 = "";
		String err1 = "";
		
		String sessionDirVar = "SESSIONDIR";
        String sessionDirVal = SystemUtils.getEnvVar(sessionDirVar);
        File sessionsDirFile = new File(sessionDirVal);
        String resourcesPath = SystemUtils.appendPath(sessionDirVal, "resources");
        
        String sessionId = "";
        String sessionToken = "";
		try
		{
			BufferedReader br;
			br = new BufferedReader(new FileReader(resourcesPath));
			String line = br.readLine();

			while (line != null) {
				
				if(line.startsWith("sessionid"))
				{
					sessionId = line.split(" ")[1];
				}
				
				if(line.startsWith("session_token"))
				{
					sessionToken = line.split(" ")[1];
				}
				
				
				line = br.readLine();
			}
			br.close();
			
		} catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cmd = "sessionnum=" + sessionId + "&sessiontoken=" + sessionToken + "&grant_type=tool";		
		try
		{
			Process p = Runtime.getRuntime().exec(new String[] {"curl", "-XPOST" , "https://mygeohub.org/developer/oauth/token",
																"--data", cmd});
			output1 = Utils.readOutput(p.getInputStream());
			err1 = Utils.readOutput(p.getErrorStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	
		JSONObject jsonoutput = new JSONObject(output1.toString());
		String accessToken = jsonoutput.get("access_token").toString();
		String[] items = target.split("/");
		String projectName = items[3];
		String subDir = "";
		for(int i = 4; i < items.length - 1; i++)
		{
			subDir += items[i]; 
			subDir += "/";
		}
		String fileName = items[items.length - 1];
	
		cmd = "curl -H \"Authorization: Bearer " + accessToken + "\" -XGET https://mygeohub.org/api/projects/list";
		
		try
		{
			Process p = Runtime.getRuntime().exec(new String[] {"curl", "-H" , "Authorization: Bearer " + accessToken,
																"-XGET", "https://mygeohub.org/api/projects/list"});
			output1 = Utils.readOutput(p.getInputStream());
			err1 = Utils.readOutput(p.getErrorStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// get project id
		jsonoutput = new JSONObject(output1.toString());
		JSONArray projectArr = jsonoutput.getJSONArray("projects");
		String projectId = "";
		for(int i = 0; i < projectArr.length(); i++)
		{
			JSONObject item = projectArr.getJSONObject(i);
			if(item.get("alias").toString().equals(projectName))
			{
//				System.out.println(item.get("id"));
				projectId = item.get("id").toString();
			}
		}
			
		// get connectionId
		try
		{
			Process p = Runtime.getRuntime().exec(new String[] {"curl", "-H" , "Authorization: Bearer " + accessToken,
																"-XGET", "https://mygeohub.org/api/projects/" + projectId + "/files/connections"});
			output1 = Utils.readOutput(p.getInputStream());
			err1 = Utils.readOutput(p.getErrorStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		jsonoutput = new JSONObject(output1.toString());
		JSONArray conArr = jsonoutput.getJSONArray("connections");
		String connectionId = "";
		for(int i = 0; i < conArr.length(); i++)
		{
			JSONObject connection = conArr.getJSONObject(i);
			if(connection.has("id"))
			{
				connectionId = connection.get("id").toString();
				break;
			}
			
		}
				
		ArrayList<String> general = gui.getMetadataGeneral();
		ArrayList<String> user = gui.getMetadataUser();
		
		String metadataString = "&metadata[title]=" + general.get(0);
		metadataString += "&metadata[subject]=" + general.get(1);
		metadataString += "&metadata[contributor]=" + general.get(2);
		metadataString += "&metadata[publisher]=" + general.get(3);
		metadataString += "&metadata[source]=" + general.get(4);
		metadataString += "&metadata[creator]=" + general.get(5);
		metadataString += "&metadata[description]=" + general.get(6);
		
		metadataString += "&metadata[GCM:external]=" + user.get(0);
		metadataString += "&metadata[RCP:external]=" + user.get(1);
		metadataString += "&metadata[periods:external]=" + user.get(2);
		metadataString += "&metadata[ClimateName:external]=" + user.get(3);
		metadataString += "&metadata[Weight:external]=" + user.get(4);
		metadataString += "&metadata[Function:external]=" + user.get(5);
		metadataString += "&metadata[RegionalMap:external]=" + user.get(6);
		metadataString += "&metadata[GrowingSeason:external]=" + user.get(7);
				
		// file access
		try
		{
			Process p = Runtime.getRuntime().exec(new String[] {"curl", "-H" , "Authorization: Bearer " + accessToken,
																"-XPOST", "https://mygeohub.org/api/projects/" + projectId + "/files/connections/" + connectionId + "/setmetadata",
																"--data", "asset[]=" + fileName + "&subdir=" + subDir + metadataString});
			output1 = Utils.readOutput(p.getInputStream());
			err1 = Utils.readOutput(p.getErrorStream());
		} catch (IOException e)
		{
			e.printStackTrace();
		} 
		
		System.out.println(output1.toString());
		
		jsonoutput = new JSONObject(output1.toString());
		String check = jsonoutput.get("success").toString();
		
		if(check.equals("1"))
			System.out.println("SUCCESS");
		else
			System.out.println("FAILED");
		
	}
	
}
