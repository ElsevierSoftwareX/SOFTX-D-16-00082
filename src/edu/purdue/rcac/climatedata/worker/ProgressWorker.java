package edu.purdue.rcac.climatedata.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.MainFrame.Pair;
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
 * A helper class to execute Rscripts to aggregate input files.
 * 
 */
public class ProgressWorker extends SwingWorker<String, Void> {

	private Vector<String> commandlist;
	private MainFrame gui;
	
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());

	public ProgressWorker(Vector<String> cmds, MainFrame gui) {
		this.commandlist = cmds;
		this.gui = gui;
	}

	@Override
	public String doInBackground() throws IOException {
		String output = runCommand(commandlist);
		return output;
	}

	@Override
	public void done() {
		try 
		{
			String output = get();
			gui.getScriptoutputTextArea().append(output);
			logger.info(output);
			gui.getCommandProgressbar().setIndeterminate(false);
			gui.setRunProgress(false);
			gui.getBtnSubmit().setEnabled(true);
			gui.getBtnReset().setEnabled(true);
			gui.getBtnExportIData().setEnabled(true);
			
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
//		gui.getCommandProgressbar().setIndeterminate(false);
		
	}
	
	/**
	 * @return a citation string for selected data and parameters
	 * 
	 *  A helper method to make a citation string for selected data/parameters.
	 *  This will be called in runCommand() method.
	 */
	private StringBuffer makeCitationString(){
		/**
		 * make suggested citation
		 */
		// calculate periods
		StringBuffer citationString = new StringBuffer();
		
		Iterator<Pair> ir = gui.getPeriods().iterator();
		logger.info("making citaion: climateFileName " + gui.getClimateFileName());
		logger.info("climate " + gui.getClimate());
		String c = gui.getClimate();
		String climateName;	//////////////////////////// reduce to 4 variables and put '_' 
		String unitDes;
		
		String metadataGCM = "";
		String metadataPathway = "";
		String metadataPeriod = "";
		String metadataClimateName = "";
		String metadataWeight = "";
		String metadataFunctionName = "";
		String metadataRegionalMap = "";
		String metadataGrowingSeason = "";
		
		
		if (c.equals("tas")) {
			climateName = "Average Surface Air Temperature";
			unitDes = "(in degree Celsius)";
		} else if (c.equals("tasmax")) {
			climateName = "Maximum Surface Air Temperature";
			unitDes = "(in degree Celsius)";
		} else if (c.equals("tasmin")) {
			climateName = "Minimum Surface Air Temperature";
			unitDes = "(in degree Celsius)";
		} else if (c.equals("pr")) {
			climateName = "Precipitation";
			unitDes = "(in unit mm/month)";
		} else {
			climateName = "Others";
			unitDes = "(unkown)";
		}
		logger.info("climateName " + climateName);
		metadataClimateName = climateName + " " + unitDes;
		
		String rcp = gui.getFullNameOfParameter(gui.getRcp());
		metadataPathway = rcp;
		
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
		
		metadataGCM = gcmName;
		
		citationString.append(climateName + " " + unitDes + " from " + gcmName + " "  
								+ "under representative concentration pathway " + rcp + " "
								+ "for the period ");
		
		int min = 10000;
		int max = 0;
		
		while (ir.hasNext())
		{
			Pair oneperiod = ir.next();
		
			if(oneperiod.getL() < min)
				min = oneperiod.getL();
			
			if(oneperiod.getR() > max)
				max = oneperiod.getR();
		
		}
		
		citationString.append(min + "-" + max + ". ");
		metadataPeriod = min + "-" + max;
		
		if(gui.getGrowingSeasonFile() != null)
			citationString.append("Growing season averages are obtained using planting and harvesting dates from Sacks et al. 2010. ");
		
		
		
		citationString.append(" For more details of sources as well as data processing see Villoria et al. (2016), available for download in the main page of this tool. Aggregation weights for area (or production) weighted averages (if used) are from Monfreda et al.");
		citationString.append("\r\n\r\n");
		
		String regionalMap, growingSeason;
		String functionName; 
		String weightFileName;
		
		if(gui.getWeightFile() == null)
		{
			weightFileName = "NULL";
			
			if(gui.getFunctioName().equals("max"))
				functionName = "Maximum";
			else if(gui.getFunctioName().equals("mean"))
				functionName = "Mean";
			else if(gui.getFunctioName().equals("min"))
				functionName = "Minimum";
			else if(gui.getFunctioName().equals("sd"))
				functionName = "Standard deviation";
			else
				functionName = "?";
		}
		else
		{
			weightFileName = gui.getWeightFile().getName();
			functionName = "NULL";
		}
		
		metadataWeight = weightFileName;
		metadataFunctionName = functionName;
		
		regionalMap = gui.getRegionFile().getName();
		growingSeason = gui.getGrowingSeasonFile().getName();
		
		metadataRegionalMap = regionalMap;
		metadataGrowingSeason = growingSeason;
		
		citationString.append("o   ");
		citationString.append("Regional Map: " + regionalMap + "\r\n");
		citationString.append("o   ");
		citationString.append("Growing season file: " + growingSeason + "\r\n");
		citationString.append("o   ");
		citationString.append("Functions: " + functionName + "\r\n");
		citationString.append("o   ");
		citationString.append("Weight file: " + weightFileName);
		
		
		citationString.append("\r\n\r\n\r\n");
		citationString.append("References:");
		citationString.append("\r\n\r\n\r\n");
		
		citationString.append("Sacks et al. 2010. \"Crop Planting Dates: An Analysis of Global Patterns.\" Global Ecology and Biogeography 19 (5): 607-20. ");
		citationString.append("\r\n\r\n");
		
		citationString.append("Monfreda et al. \"Farming the Planet: 2. Geographic Distribution of Crop Areas, Yields, Physiological Types, and Net Primary Production in the Year 2000.\" Global Biogeochemical Cycles, March, 1:19. ");
		citationString.append("\r\n\r\n");
		
		citationString.append("Villoria N.B, J. Elliot , C. Mueller, J. Shin, L. Zhao, C. Song. (2016). \"Web-based access, aggregation, and visualization of future climate projections with emphasis on agricultural assessments.\" https://mygeohub.org/tools/climatetool/");
	
		///// FOR METADATA
		// Handled in FileBrowser.java
		String metadataDescription = citationString.toString();
		
		ArrayList<String> metadataGeneral = new ArrayList<String>();
		ArrayList<String> metadataUser = new ArrayList<String>();
		gui.setMetadataGeneral(metadataGeneral); 
		gui.setMetadataUser(metadataUser); 
		
		String metadataTitle = "Climate Scenario Aggregator-Output and Citation file";
		String metadataSubject = "CMIP5";
		String metadataContributer = "Villoria, Nelson B., Joshua Elliott, Christoph Mueller, Jaewoo Shin, Lan Zhao and Carol Song";
		String metadataPublisher = "Climate Scenario Aggregator @mygeohub v.1.0";
		String metadataSource = "Climate Scenario Aggregator from mygeohub";
		String metadataCreator = "Climate Scenario Aggregator Tool"; 
		
		metadataGeneral.add(metadataTitle);
		metadataGeneral.add(metadataSubject);
		metadataGeneral.add(metadataContributer);
		metadataGeneral.add(metadataPublisher);
		metadataGeneral.add(metadataSource);
		metadataGeneral.add(metadataCreator);
		metadataGeneral.add(metadataDescription);
		
		metadataUser.add(metadataGCM);
		metadataUser.add(metadataPathway);
		metadataUser.add(metadataPeriod);
		metadataUser.add(metadataClimateName);
		metadataUser.add(metadataWeight);
		metadataUser.add(metadataFunctionName);
		metadataUser.add(metadataRegionalMap);
		metadataUser.add(metadataGrowingSeason);
		
		return citationString;
	}

	/**
	 * @param rcommands linux command line to be executed in a process. 
	 * @return a output message string.
	 * @throws IOException
	 * 
	 * A helper function to execute a Rscript command in a separated process
	 * to aggregate input files. After the scripts are done, this function makes
	 * a citation strings, sets metadata and makes output zip file. 
	 */
	private String runCommand(Vector<String> rcommands) {
		Iterator<String> it = rcommands.iterator();
		try
		{
			while (it.hasNext()) {
				String cmd = it.next();
				gui.getScriptoutputTextArea().append(cmd + "\n");
				logger.info(cmd);
				Process p;
				
				p = Runtime.getRuntime().exec(cmd);
				String outputString = Utils.readOutput(p.getInputStream());
				String errorString = Utils.readOutput(p.getErrorStream());
	
				if (!outputString.isEmpty()) {
					gui.getScriptoutputTextArea().append(outputString);
					logger.info(outputString);
				} else if (!errorString.isEmpty()) {
					gui.getScriptoutputTextArea().append(errorString);
					logger.info(outputString);
				} else {
					return "unexpected error occured while executing r script";
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		gui.setRunProgress(false);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(Calendar.getInstance().getTime());
		gui.setZipFile( gui.getOutputPath() + "climate_aggregation_" + timeStamp + ".zip" );
		gui.setCitationFileName(gui.getOutputPath() + "documentation_" + timeStamp + ".txt" );

		StringBuffer citationString = null;
		try{
			// make citation
			citationString = makeCitationString();
		} catch (Exception e1) {
			logger.info(e1.toString());
			e1.printStackTrace();
		}

		logger.info("document file: " + gui.getCitationFileName());
		logger.info(citationString.toString());

		
		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(gui.getCitationFileName()), "UTF-8"));
			out.write(citationString.toString());
			out.flush();
			out.close();
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String mergedFile = gui.getOutputFileList().get(0);
		
		int size = gui.getOutputFileList().size();
		
		String headerString;
		if(gui.getClimate().equals("tas"))
			headerString = "\"\",\"id\",\"time\",\"tas\"";
		else if(gui.getClimate().equals("tasmax"))
			headerString = "\"\",\"id\",\"time\",\"tasmax\"";
		else if(gui.getClimate().equals("tasmin"))
			headerString = "\"\",\"id\",\"time\",\"tasmin\"";
		else
			headerString = "\"\",\"id\",\"time\",\"pr\"";
		
		// If there is one or more output file, we will merge them to one file.
		if(size > 1)
		{
		
			logger.info("------------------------------------");
			
			
			String climateFileName = gui.getOutputFileList().get(0);
			String subfilename = climateFileName.substring(0,
					climateFileName.lastIndexOf(".csv"));
			
			StringTokenizer st = new StringTokenizer(subfilename,"_");
			int totalTokens = st.countTokens();
			
			
			String header = st.nextToken();
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
			
			String weight = st.nextToken();
			String growing = st.nextToken();
			
			String region = "";
			if(totalTokens == 10)
				region = st.nextToken();
			
			String fromYear =  String.valueOf(gui.getMinStartYear());
			String toYear =  String.valueOf(gui.getMaxEndYear());
			
			// merged file name
			mergedFile = header + "_" + bced + "_" + observeFromYear + "_" + observeToYear + "_" 
					+ gcm + "_" + rcp + "_" + fromYear + "-" + toYear + "_" + weight + "_" 
					+ growing + "_" + region + ".csv";
			
			logger.info("mergedfile name : " + mergedFile);
	
			try
			{
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(mergedFile), "UTF-8"));
				bw.write(headerString);
				
				int number = 1;
			
				for(int i = 0; i < size; i++)
				{
					String file = gui.getOutputFileList().get(i);
					logger.info("what file now processed: " + file);
					BufferedReader br = new BufferedReader(new InputStreamReader(
												new FileInputStream(file), "UTF-8"));
					
					String line = br.readLine();
					line = br.readLine();
		
					String num = "";
					String id = "";
					String time = "";
					String yield = "";
					
					while (line != null) 
					{
						StringTokenizer items = new StringTokenizer(line, ",");
						
						num = items.nextToken();
						id = items.nextToken();
						time = items.nextToken();
						yield = items.nextToken();
						num = "\"" + String.valueOf(number) + "\"";
						
						bw.write("\n" + num + "," + id + "," + time + "," + yield);
						
						number++;
						
						line = br.readLine();
					}
					br.close();
				}
				
				bw.flush();
				bw.close();
			
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			for(int i = 0; i < size; i++)
			{
				logger.info(gui.getOutputFileList().get(i));
				File f = new File(gui.getOutputFileList().get(i));
				f.delete();
			}
			
			gui.getOutputFileList().clear();
			gui.getOutputFileList().add(mergedFile);
		}
		else
		{
			String tmpFile = mergedFile.substring(0, mergedFile.length()-4) + "1.csv"; 
			BufferedWriter bw;
			try
			{
				bw = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(tmpFile), "UTF-8"));
			
			
				bw.write(headerString);
				int number = 1;
			
				
				logger.info("what file now processed: " + mergedFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(
											new FileInputStream(mergedFile), "UTF-8"));
				
				String line = br.readLine();
				line = br.readLine();
	
				String num = "";
				String id = "";
				String time = "";
				String yield = "";
				
				while (line != null) 
				{
					StringTokenizer items = new StringTokenizer(line, ",");
					
					num = items.nextToken();
					id = items.nextToken();
					time = items.nextToken();
					yield = items.nextToken();
					num = "\"" + String.valueOf(number) + "\"";
					
					bw.write("\n" + num + "," + id + "," + time + "," + yield);
					
					number++;
					
					line = br.readLine();
				}
				br.close();
				
				
				bw.flush();
				bw.close();
			} catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	      // Once everything is complete, delete old file..
	      File oldFile = new File(mergedFile);
	      oldFile.delete();

	      // And rename tmp file's name to old file name
	      File newFile = new File(tmpFile);
	      newFile.renameTo(oldFile);
		}
		
		gui.getTextMapField().setText(mergedFile);
		gui.setMapSourceFile(mergedFile);
		logger.info("mapsourcefile:" + gui.getMapSourceFile());
		logger.info("------------------------------------");
		
		File file = new File(gui.getCitationFileName());
		String outmsg;
		if (file.isFile() && file.exists()) {
			gui.getOutputFileList().add(gui.getCitationFileName());
			logger.info(gui.getCitationFileName() + " added");
		} else {
			outmsg = "Process completed abnomally: No citation file";
			logger.info(outmsg);
			return outmsg;
		}
		
		if (!gui.getRegionFile().getName().equals("WorldId.csv")) 
		{
			gui.setZipFile(gui.getOutputFile().substring(0, gui.getOutputFile().length() - 4)
							+ ".zip");
		}

		try {
			logger.info("zipfile: " + gui.getZipFile());
			logger.info("outputfilelist: " + gui.getOutputFileList().toString());

			Utils.createZipArchive(gui.getOutputFileList(), gui.getZipFile(), false,
					false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File zfile = new File(gui.getZipFile());
		if (!zfile.exists()) {
			String output = "Creating an archive failed";
			return output;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("CSV file saved as \n" + mergedFile + "\n");
		sb.append("Creating an archive (ZIP-format) file : \n");
		sb.append(gui.getZipFile() + "\n");
		sb.append("* R completed! *\n");
		return sb.toString();
	}
}