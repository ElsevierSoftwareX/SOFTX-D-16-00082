package edu.purdue.rcac.climatedata;

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
 * This is a class to set variables used in the CSA application.
 * 
 */
public class Vars
{
	// AboutFrame
	public static String pathToReadme = "/doc/readme.html";
	public static int borderInset = 5;
	public static int aboutWidth = 450;
	public static int aboutHeight = 560;
	 
	// MainFrame
	public static int mainFrameWidth = 800;
	public static int mainFrameHeight = 600;
	public static int mainFramePaneWidth = 795;
	public static int mainFramePaneHeight = 527;
	
	
	public static String pathToOutputDir = "/ClimateOutput/";
	public static String pathToTempDir = "/ClimateOutput/temp/";
	public static String pathToExamples = "/examples/";
	public static String pathToRegionMap = "regionmap/";
	public static String pathToWeightMap = "weightmap/";
	public static String pathToGS = "growingseason/";
	public static String pathToBrowser = "/bin/filebrowser.py";
	public static String checkVersion = ".delVersion3";
	
	public static String pathToDefaultDownload = "/data/transfers/gotransfer/";
	public static String pathToDownload = "inputs/monthly.mean";
	public static String prefix = "inputs/monthly.mean";
	
	public static String defaultClimateField = "Select climate files";
	public static String defaultRegionFileName = "WorldId.csv";
	public static String defaultWeightFileName = "maize_hectares_30min.csv";
	public static String defaultGSFileName = "maizegrowing.season.unfilled.RData";
	
	public static String climateRunName = "do.r";
	public static String climateFnsName = "functions.r";
	public static String mapRunName = "map.run.r";
	public static String mapGeneratorName = "map.generator.r";
	public static String defaultMapFile = "/doc/worldmap.png";
	
	public static int imgWidth = 630;
	public static int imgHeight = 420;
	
}
