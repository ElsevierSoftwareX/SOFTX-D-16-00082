package edu.purdue.rcac.climatedata;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import edu.purdue.rcac.climatedata.handler.AboutAction;
import edu.purdue.rcac.climatedata.handler.CheckBoxAction;
import edu.purdue.rcac.climatedata.handler.CheckBoxEnableGrowingAction;
import edu.purdue.rcac.climatedata.handler.Click1MouseHandler;
import edu.purdue.rcac.climatedata.handler.ClimateFileAction;
import edu.purdue.rcac.climatedata.handler.ClimateRadioButtonAction;
import edu.purdue.rcac.climatedata.handler.CreateButtonAction;
import edu.purdue.rcac.climatedata.handler.DownloadRawDataCitationHandler;
import edu.purdue.rcac.climatedata.handler.DownloadRawDataHandler;
import edu.purdue.rcac.climatedata.handler.FetchDataButtonHandler;
import edu.purdue.rcac.climatedata.handler.GCMRadioButtonAction;
import edu.purdue.rcac.climatedata.handler.GrowingFileAction;
import edu.purdue.rcac.climatedata.handler.MapFileAction;
import edu.purdue.rcac.climatedata.handler.RCPRadioButtonAction;
import edu.purdue.rcac.climatedata.handler.RegionFileAction;
import edu.purdue.rcac.climatedata.handler.RegionFileUploadAction;
import edu.purdue.rcac.climatedata.handler.SubmitButtonAction;
import edu.purdue.rcac.climatedata.handler.WeightFileAction;
import edu.purdue.rcac.climatedata.handler.WeightFileUploadAction;

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
 * A MainFrame of the CSA tool.
 * Most of the contents in this class are for the GUI components and 
 * their getter/setter.
 */
public class MainFrame extends JFrame
{
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	private String userHome = System.getProperty("user.home");
	private String outputPath = userHome + Vars.pathToOutputDir;
	private String tempPath = userHome + Vars.pathToTempDir;
	private String jarPathString = MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	private File jarPath = new File(jarPathString);
	private String jarParentString = jarPath.getParent();
	private File binPath = new File(jarParentString);
	private String homePath = binPath.getParent();
	private String defaultPath = homePath + Vars.pathToExamples;
	private String regionMapDir = defaultPath + Vars.pathToRegionMap;
	private String weightMapDir = defaultPath + Vars.pathToWeightMap;
	private String growingSeasonDir = defaultPath + Vars.pathToGS;
	private String delVersionFileName = outputPath + Vars.checkVersion;
	private String browserPath = homePath + Vars.pathToBrowser;

	private final Action aboutAction;
	private final Action regionFileAction;// = new RegionFileAction(this);
	private final Action regionFileUploadAction;// = new RegionFileUploadAction(this);
	private final Action weightFileAction;// = new WeightFileAction(this);
	private final Action weightFileUploadAction;// = new WeightFileUploadAction(this);
	private final Action climateFileAction;// = new ClimateFileAction(this);
	private final Action checkBoxAction;// = new CheckBoxAction(this);
	private final Action submitButtonAction;
	private final Action mapFileAction;// = new MapFileAction(this);
	private final Action growingFileAction;
	private final Action checkBoxEnableGrowingAction;
	private final Action exportIDataFileAction = new ExportIDataFileAction(this);
	private final MouseListener click1MouseHandler;

	private FileHandler fh;
	private static Boolean downloadProgress = false;
	private static Boolean runProgress = false;
	private static Boolean mapProgress = false;
	private String defaultDownloadPath = Vars.pathToDefaultDownload;
	private String downloadPath = defaultDownloadPath + Vars.pathToDownload;
//	private String prefix = "monthly.mean";
	private String prefix = Vars.prefix;
	private String climateFileName = Vars.defaultClimateField;
	private File climateFile = new File(climateFileName);
	private File[] climateFileList = { climateFile };
	private String defaultRegionFileName = regionMapDir + Vars.defaultRegionFileName;
	private String defaultWeightFileName = weightMapDir + Vars.defaultWeightFileName;
	private String defaultGrowingSeasonFileName = growingSeasonDir + Vars.defaultGSFileName;
//	private String climateRunName = defaultPath + "climate.run.r";
//	private String climateFnsName = defaultPath + "climate.fns.r";
	private String climateRunName = defaultPath + Vars.climateRunName;
	private String climateFnsName = defaultPath + Vars.climateFnsName;
	private String mapRunName = defaultPath + Vars.mapRunName;
	private String mapGeneratorName = defaultPath + Vars.mapGeneratorName;

	private File defaultRegionFile = new File(defaultRegionFileName);
	private File defaultWeightFile = new File(defaultWeightFileName);
	private File defaultGrowingSeasonFile = new File(defaultGrowingSeasonFileName);
	private File regionFile = defaultRegionFile;
	private File weightFile = defaultWeightFile;
	private File growingSeasonFile = defaultGrowingSeasonFile;

	private String functioName;
	private String citationFileName;
	private String mapPath;
	private String zipFile;
	private String imgZipFile;
	private String mapSourceFile;

	private Vector<String> outputFileList;
	private List<Pair> periods;
	private ArrayList<String> metadataGeneral; 
	private ArrayList<String> metadataUser;
	private String outputFile;
	// GUI
	private JPanel contentPane;
	private JPanel functionPanel;
	private final String mapFile = Vars.defaultMapFile;
	private URL mapURL = getClass().getResource(mapFile);
	private final MapCanvas canvas = new MapCanvas(mapURL);
	private JTextArea outputTextArea;
	private JTextArea scriptoutputTextArea;
	private JTextField txtPathfields;
	private JTextField txtClimate;
	private JTextField txtRegionFile;
	private JTextField txtWeightFile;
	private JTextField textMapField;
	private JTextField textGrwoingFile;
	private JButton btnGrowingSeasonButton;
	private JButton btnSubmit;
	private JButton btnReset;
	private JButton btnExportIData;
	private final ButtonGroup GCMButtonGroup = new ButtonGroup();
	private final ButtonGroup RCPButtonGroup = new ButtonGroup();
	private final ButtonGroup ClimateButtonGroup = new ButtonGroup();
	private final ButtonGroup AggregationButtonGroup = new ButtonGroup();
	private JRadioButton rdbtnMax;
	private JRadioButton rdbtnMin;
	private JRadioButton rdbtnMean;
	private JRadioButton rdbtnSd;
	private JProgressBar mainProgressBar;
	private JProgressBar commandProgressbar;
	private JProgressBar visualizeProgressBar;

	private JCheckBox chckbxFunctionEnable;
	private JCheckBox chckbxEnableGrowingSeasons;
	private JCheckBox chckbxByForce;
	private JLabel lblMaplabel;
	private JSlider slider;
	private String gcm = "", rcp = "", climate = "";
	private static int counter = 0;

	private int syear, eyear;
	private int minStartYear, maxEndYear;

	private HashMap<String, String> parameters;
	private String[] fullNameOfParameters = { "HadGEM2-ES", "IPSL-CM5A-LR", "MIROC-ESM-CHEM", "GFDL-ESM2M", "NorESM1-M",
			"Historical", "Spinup", "RCP8.5", "RCP6.0", "RCP4.5", "RCP2.6",
			"Average surface air temperature", "Maximum surface air temperature",
			"Minimum surface air temperature", "Precipitation"};

	private String[] abbrNameOfParameters = { "HadGEM2-ES", "IPSL-CM5A-LR", "MIROC-ESM-CHEM", "GFDL-ESM2M", "NorESM1-M",
			"historical", "spinup", "rcp8p5", "rcp6p0", "rcp4p5", "rcp2p6",
			"tas", "tasmax",
			"tasmin", "pr"};

	/**
	 * A constructor of the MainFrame extends JFrame
	 * Most of the contents in this constructor are for GUI components
	 * 
	 */
	public MainFrame()
	{
		// File logger
		try
		{
			File outputDirectory = new File(outputPath);
			if (!outputDirectory.exists())
			{
				outputDirectory.mkdir();
				logger.info("Make new directory");
			}

			fh = new FileHandler(outputPath + "climateTool.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setLevel(Level.FINEST);

			File tempDirectory = new File(tempPath);
			if (!tempDirectory.exists())
			{
				tempDirectory.mkdir();
			} else
			{
				logger.info("Delete previous raw files in user home");
				File[] list = tempDirectory.listFiles();
				for (File f : list)
				{
					f.delete();
				}
			}

			File delVersionDir = new File(delVersionFileName);
			if(!delVersionDir.exists())
			{
				delVersionDir.mkdir();
				File[] list = outputDirectory.listFiles();

				for(File f : list)
				{
					if(f.isFile())
						f.delete();
				}
			}
			else
			{

			}

		} catch (SecurityException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// Actions
		regionFileAction = new RegionFileAction(this);
		regionFileUploadAction = new RegionFileUploadAction(this);
		weightFileAction = new WeightFileAction(this);
		weightFileUploadAction = new WeightFileUploadAction(this);
		climateFileAction = new ClimateFileAction(this);

		growingFileAction = new GrowingFileAction(this);
		checkBoxEnableGrowingAction = new CheckBoxEnableGrowingAction(this);
		checkBoxAction = new CheckBoxAction(this);
		submitButtonAction = new SubmitButtonAction(this);
		mapFileAction = new MapFileAction(this);

		// Print SysInfo
		logger.info("userHome:" + userHome);
		logger.info("jarPathString:" + jarPathString);
		logger.info("homePath:" + homePath);
		logger.info("defaultPath:" + defaultPath);

		// Make parameters <Full, abbr>
		parameters = new HashMap<String, String>();
		for (int i = 0; i < fullNameOfParameters.length; i++)
		{
			parameters.put(fullNameOfParameters[i], abbrNameOfParameters[i]);
		}

		// Title
		setTitle("Climate Scenario Aggregator 1.1");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, Vars.mainFrameWidth, Vars.mainFrameHeight);

		// Menubar
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.WHITE);
		setJMenuBar(menuBar);

		// Help menu item
		JButton btnHelp = new JButton("About");
		aboutAction = new AboutAction();
		btnHelp.setAction(aboutAction);
		btnHelp.setBackground(Color.WHITE);
		menuBar.add(btnHelp);

		// Content pane
		contentPane = new JPanel();
		int bi = Vars.borderInset;
		contentPane.setBorder(new EmptyBorder(bi, bi, bi, bi));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, Vars.mainFramePaneWidth, Vars.mainFramePaneHeight);
		contentPane.add(tabbedPane);

		// Download tab
		JPanel downPanel = new JPanel();
		tabbedPane.addTab("Download", null, downPanel, null);
		downPanel.setLayout(null);

		// Climate data panel
		JPanel climateDataPanel = new JPanel();
		climateDataPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Climate Data",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		climateDataPanel.setBounds(6, 6, 778, 352);
		downPanel.add(climateDataPanel);
		climateDataPanel.setLayout(null);

		// Climate
		JPanel climateSelectionPanel = new JPanel();
		climateSelectionPanel.setBounds(40, 34, 294, 232);
		climateDataPanel.add(climateSelectionPanel);
		climateSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Variable",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		climateSelectionPanel.setLayout(new GridLayout(0, 1, 0, 0));


		JRadioButton rdbtnAverageSurfaceAir = new JRadioButton("Average surface air temperature");
		climateSelectionPanel.add(rdbtnAverageSurfaceAir);
		rdbtnAverageSurfaceAir.setAction(new ClimateRadioButtonAction(rdbtnAverageSurfaceAir, this));
		ClimateButtonGroup.add(rdbtnAverageSurfaceAir);

		JRadioButton rdbtnMaximumSurfaceAir = new JRadioButton("Maximum surface air temperature");
		climateSelectionPanel.add(rdbtnMaximumSurfaceAir);
		rdbtnMaximumSurfaceAir.setAction(new ClimateRadioButtonAction(rdbtnMaximumSurfaceAir, this));
		ClimateButtonGroup.add(rdbtnMaximumSurfaceAir);

		JRadioButton rdbtnMinimumSurfaceAir = new JRadioButton("Minimum surface air temperature");
		climateSelectionPanel.add(rdbtnMinimumSurfaceAir);
		rdbtnMinimumSurfaceAir.setAction(new ClimateRadioButtonAction(rdbtnMinimumSurfaceAir, this));
		ClimateButtonGroup.add(rdbtnMinimumSurfaceAir);

		JRadioButton rdbtnPrecipitation = new JRadioButton("Precipitation");
		climateSelectionPanel.add(rdbtnPrecipitation);
		rdbtnPrecipitation.setAction(new ClimateRadioButtonAction(rdbtnPrecipitation, this));
		ClimateButtonGroup.add(rdbtnPrecipitation);

		// GCM
		JPanel GCMSelectionPanel = new JPanel();
		GCMSelectionPanel.setBounds(356, 34, 200, 232);
		climateDataPanel.add(GCMSelectionPanel);
		GCMSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "GCM", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GCMSelectionPanel.setLayout(new GridLayout(0, 1, 0, 0));

				JRadioButton rdbtnNewHadGEM2 = new JRadioButton("HadGEM2-ES");
				rdbtnNewHadGEM2.setAction(new GCMRadioButtonAction(rdbtnNewHadGEM2,
						this));
				GCMSelectionPanel.add(rdbtnNewHadGEM2);
				GCMButtonGroup.add(rdbtnNewHadGEM2);

		JLabel label_15 = new JLabel("");
		GCMSelectionPanel.add(label_15);

		JRadioButton rdbtnIpslcmalr = new JRadioButton("IPSL-CM5A-LR");
		rdbtnIpslcmalr
				.setAction(new GCMRadioButtonAction(rdbtnIpslcmalr, this));
		GCMSelectionPanel.add(rdbtnIpslcmalr);
		GCMButtonGroup.add(rdbtnIpslcmalr);

		JLabel label_16 = new JLabel("");
		GCMSelectionPanel.add(label_16);

		JRadioButton rdbtnMirocesmchem = new JRadioButton("MIROC-ESM-CHEM");
		rdbtnMirocesmchem.setAction(new GCMRadioButtonAction(rdbtnMirocesmchem,
				this));
		GCMSelectionPanel.add(rdbtnMirocesmchem);
		GCMButtonGroup.add(rdbtnMirocesmchem);

		JLabel label_17 = new JLabel("");
		GCMSelectionPanel.add(label_17);

		JRadioButton rdbtnGfdlesmm = new JRadioButton("GFDL-ESM2M");
		rdbtnGfdlesmm.setAction(new GCMRadioButtonAction(rdbtnGfdlesmm, this));
		GCMSelectionPanel.add(rdbtnGfdlesmm);
		GCMButtonGroup.add(rdbtnGfdlesmm);

		JLabel label_18 = new JLabel("");
		GCMSelectionPanel.add(label_18);

		JRadioButton rdbtnNoresmm = new JRadioButton("NorESM1-M");
		rdbtnNoresmm.setAction(new GCMRadioButtonAction(rdbtnNoresmm, this));
		GCMSelectionPanel.add(rdbtnNoresmm);
		GCMButtonGroup.add(rdbtnNoresmm);

		JLabel label_19 = new JLabel("");
		GCMSelectionPanel.add(label_19);

		// RCP
		JPanel RCPSelectionPanel = new JPanel();
		RCPSelectionPanel.setBounds(578, 34, 145, 232);
		climateDataPanel.add(RCPSelectionPanel);
		RCPSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "RCP", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		RCPSelectionPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Historical");
		rdbtnNewRadioButton_1.setAction(new RCPRadioButtonAction(
				rdbtnNewRadioButton_1, this));
		RCPSelectionPanel.add(rdbtnNewRadioButton_1);
		RCPButtonGroup.add(rdbtnNewRadioButton_1);

//		JRadioButton rdbtnNewRadioButton_6 = new JRadioButton("Spinup");
//		rdbtnNewRadioButton_6.setAction(new RCPRadioButtonAction(
//				rdbtnNewRadioButton_6, this));
//		RCPSelectionPanel.add(rdbtnNewRadioButton_6);
//		RCPButtonGroup.add(rdbtnNewRadioButton_6);

		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("RCP8.5");
		rdbtnNewRadioButton_2.setAction(new RCPRadioButtonAction(
				rdbtnNewRadioButton_2, this));
		RCPSelectionPanel.add(rdbtnNewRadioButton_2);
		RCPButtonGroup.add(rdbtnNewRadioButton_2);

		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("RCP6.0");
		rdbtnNewRadioButton_3.setAction(new RCPRadioButtonAction(
				rdbtnNewRadioButton_3, this));
		RCPSelectionPanel.add(rdbtnNewRadioButton_3);
		RCPButtonGroup.add(rdbtnNewRadioButton_3);

		JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("RCP4.5");
		rdbtnNewRadioButton_4.setAction(new RCPRadioButtonAction(
				rdbtnNewRadioButton_4, this));
		RCPSelectionPanel.add(rdbtnNewRadioButton_4);
		RCPButtonGroup.add(rdbtnNewRadioButton_4);

		JRadioButton rdbtnNewRadioButton_5 = new JRadioButton("RCP2.6");
		rdbtnNewRadioButton_5.setAction(new RCPRadioButtonAction(
				rdbtnNewRadioButton_5, this));
		RCPSelectionPanel.add(rdbtnNewRadioButton_5);
		RCPButtonGroup.add(rdbtnNewRadioButton_5);

		// fetch files from Globus Online
		JButton btnDownload = new JButton("Fetch Data");
		btnDownload.addActionListener(new FetchDataButtonHandler(this));

		btnDownload.setBounds(43, 278, 117, 30);
		climateDataPanel.add(btnDownload);

		// Force download
		chckbxByForce = new JCheckBox("<html>Clear cache<br/>before fetching data<html>");
		chckbxByForce.setBounds(39, 310, 188, 33);
		climateDataPanel.add(chckbxByForce);

		// progress bar
		mainProgressBar = new JProgressBar();
		mainProgressBar.setToolTipText("transfering files...");
		mainProgressBar.setBounds(171, 278, 571, 30);
		climateDataPanel.add(mainProgressBar);

		// download raw data button
		JButton btnDownloadRawData = new JButton("Download NetCDF files");
		btnDownloadRawData
				.setToolTipText("<html>Download NetCDF file or<br/>Click on \"Aggregate\" tab.</html>");
		btnDownloadRawData.setBounds(350, 314, 200, 29);
		climateDataPanel.add(btnDownloadRawData);

		JButton btnRawCitation = new JButton("Data Description");
		btnRawCitation.setBounds(562, 314, 180, 29);
		climateDataPanel.add(btnRawCitation);

		DownloadRawDataCitationHandler drdch = new DownloadRawDataCitationHandler(this);
		btnRawCitation.addActionListener(drdch);

		DownloadRawDataHandler dlrdh = new DownloadRawDataHandler(this);
		btnDownloadRawData.addActionListener(dlrdh);

		// log panel
		JPanel logPanel = new JPanel();
		logPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207,
				229)), "Log", TitledBorder.LEADING, TitledBorder.TOP, null,
				null));
		logPanel.setBounds(16, 363, 750, 120);
		downPanel.add(logPanel);
		logPanel.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 17, 740, 97);
		logPanel.add(scrollPane);

		outputTextArea = new JTextArea();
		DefaultCaret caret = (DefaultCaret) outputTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(outputTextArea);

		// textfield for path
		txtPathfields = new JTextField();
		txtPathfields.setEditable(false);
		txtPathfields.setBounds(74, 265, 472, 28);
		// cropDataPanel.add(txtPathfields);
		txtPathfields.setColumns(10);

		// Aggregate
		JPanel aggregatePanel = new JPanel();
		tabbedPane.addTab("Aggregate", null, aggregatePanel, null);
		aggregatePanel.setLayout(null);

		JPanel inputFilePanel = new JPanel();
		inputFilePanel.setBounds(20, 6, 721, 312);
		aggregatePanel.add(inputFilePanel);
		inputFilePanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Input Files",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		inputFilePanel.setLayout(null);

		JLabel lblNewLabel = new JLabel("1.Climate Data:");
		lblNewLabel.setBounds(13, 29, 109, 16);
		inputFilePanel.add(lblNewLabel);

		setTxtClimate(new JTextField());
		getTxtClimate().setEditable(false);
		getTxtClimate().setBounds(140, 23, 450, 28);
		inputFilePanel.add(getTxtClimate());
		getTxtClimate().setColumns(41);
		getTxtClimate().setText(getClimateFileName());

		JLabel label = new JLabel("");
		label.setBounds(593, 37, 0, 0);
		inputFilePanel.add(label);

		JButton btnFile = new JButton("Browse");
		btnFile.setBounds(596, 23, 100, 29);
		btnFile.setAction(climateFileAction);
		inputFilePanel.add(btnFile);

		JLabel lblRegionFile = new JLabel("2.Regional Map:");
		lblRegionFile.setBounds(13, 63, 109, 16);
		inputFilePanel.add(lblRegionFile);

		JLabel label_1 = new JLabel("");
		label_1.setBounds(107, 71, 0, 0);
		inputFilePanel.add(label_1);

		txtRegionFile = new JTextField();
		txtRegionFile.setEditable(false);
		txtRegionFile.setBounds(140, 57, 346, 28);
		inputFilePanel.add(txtRegionFile);
		txtRegionFile.setColumns(30);
		txtRegionFile.setText(defaultRegionFileName);

		JButton btnRegionButton = new JButton("Browse");
		btnRegionButton.setBounds(491, 57, 100, 29);
		btnRegionButton.setAction(regionFileAction);
		inputFilePanel.add(btnRegionButton);

		JButton btnUpload = new JButton("Upload");
		btnUpload.setBounds(596, 57, 100, 29);
		btnUpload.setAction(regionFileUploadAction);
		inputFilePanel.add(btnUpload);

		JLabel label_2 = new JLabel("");
		label_2.setBounds(701, 71, 0, 0);
		inputFilePanel.add(label_2);

		functionPanel = new JPanel();
		functionPanel.setBounds(13, 150, 693, 130);
		inputFilePanel.add(functionPanel);
		functionPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null),
				"3. Summary Statistics", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		functionPanel.setLayout(null);

		JLabel lblWeightmap = new JLabel("Choose grid-cell level weights for weighting regional averages:");
		lblWeightmap.setBounds(16, 20, 456, 16);
		functionPanel.add(lblWeightmap);

		txtWeightFile = new JTextField();
		txtWeightFile.setEditable(false);
		txtWeightFile.setBounds(130, 38, 342, 28);
		functionPanel.add(txtWeightFile);
		txtWeightFile.setColumns(30);
		txtWeightFile.setText(defaultWeightFileName);
		txtWeightFile.setForeground(Color.BLACK);

		JButton btnWeight = new JButton("Browse");
		btnWeight.setBounds(477, 38, 100, 29);
		functionPanel.add(btnWeight);
		btnWeight.setAction(weightFileAction);

		JButton btnUpload_1 = new JButton("Upload");
		btnUpload_1.setBounds(582, 38, 100, 29);
		btnUpload_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
			}
		});
		functionPanel.add(btnUpload_1);
		btnUpload_1.setAction(weightFileUploadAction);

		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "...or select a summary statistic:",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_7.setBounds(16, 70, 660, 55);
		functionPanel.add(panel_7);
		GridBagLayout gbl_panel_7 = new GridBagLayout();
		gbl_panel_7.columnWidths = new int[] { 108, 108, 108, 108, 108, 0 };
		gbl_panel_7.rowHeights = new int[] { 33, 0 };
		gbl_panel_7.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel_7.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_7.setLayout(gbl_panel_7);

		chckbxFunctionEnable = new JCheckBox("Enable Functions");
		chckbxFunctionEnable.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		chckbxFunctionEnable.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_chckbxFunctionEnable = new GridBagConstraints();
		gbc_chckbxFunctionEnable.fill = GridBagConstraints.BOTH;
		gbc_chckbxFunctionEnable.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxFunctionEnable.gridx = 0;
		gbc_chckbxFunctionEnable.gridy = 0;
		panel_7.add(chckbxFunctionEnable, gbc_chckbxFunctionEnable);
		chckbxFunctionEnable.setAction(checkBoxAction);
		chckbxFunctionEnable.setToolTipText("check to enable functions");


		rdbtnMax = new JRadioButton("MAX");
		rdbtnMax.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		GridBagConstraints gbc_rdbtnMax = new GridBagConstraints();
		gbc_rdbtnMax.fill = GridBagConstraints.BOTH;
		gbc_rdbtnMax.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnMax.gridx = 1;
		gbc_rdbtnMax.gridy = 0;
		panel_7.add(rdbtnMax, gbc_rdbtnMax);
		rdbtnMax.setEnabled(false);
		rdbtnMax.setHorizontalAlignment(SwingConstants.CENTER);
		AggregationButtonGroup.add(rdbtnMax);

		rdbtnMin = new JRadioButton("MIN");
		rdbtnMin.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		GridBagConstraints gbc_rdbtnMin = new GridBagConstraints();
		gbc_rdbtnMin.fill = GridBagConstraints.BOTH;
		gbc_rdbtnMin.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnMin.gridx = 2;
		gbc_rdbtnMin.gridy = 0;
		panel_7.add(rdbtnMin, gbc_rdbtnMin);
		rdbtnMin.setEnabled(false);
		rdbtnMin.setHorizontalAlignment(SwingConstants.CENTER);
		AggregationButtonGroup.add(rdbtnMin);

		rdbtnMean = new JRadioButton("MEAN");
		rdbtnMean.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		GridBagConstraints gbc_rdbtnMean = new GridBagConstraints();
		gbc_rdbtnMean.fill = GridBagConstraints.BOTH;
		gbc_rdbtnMean.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnMean.gridx = 3;
		gbc_rdbtnMean.gridy = 0;
		panel_7.add(rdbtnMean, gbc_rdbtnMean);
		rdbtnMean.setEnabled(false);
		rdbtnMean.setHorizontalAlignment(SwingConstants.CENTER);
		AggregationButtonGroup.add(rdbtnMean);

		rdbtnSd = new JRadioButton("SD");
		rdbtnSd.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		GridBagConstraints gbc_rdbtnSd = new GridBagConstraints();
		gbc_rdbtnSd.fill = GridBagConstraints.BOTH;
		gbc_rdbtnSd.gridx = 4;
		gbc_rdbtnSd.gridy = 0;
		panel_7.add(rdbtnSd, gbc_rdbtnSd);
		rdbtnSd.setEnabled(false);
		rdbtnSd.setHorizontalAlignment(SwingConstants.CENTER);
		AggregationButtonGroup.add(rdbtnSd);


		JLabel label_3 = new JLabel("");
		label_3.setBounds(477, 68, 0, 0);
		functionPanel.add(label_3);

		JLabel label_4 = new JLabel("");
		label_4.setBounds(482, 68, 0, 0);
		functionPanel.add(label_4);

		JLabel label_5 = new JLabel("");
		label_5.setBounds(487, 68, 0, 0);
		functionPanel.add(label_5);

		JLabel label_6 = new JLabel("");
		label_6.setBounds(492, 68, 0, 0);
		functionPanel.add(label_6);

		JLabel label_7 = new JLabel("");
		label_7.setBounds(497, 68, 0, 0);
		functionPanel.add(label_7);

		JLabel label_8 = new JLabel("");
		label_8.setBounds(502, 68, 0, 0);
		functionPanel.add(label_8);

		JLabel label_9 = new JLabel("");
		label_9.setBounds(507, 68, 0, 0);
		functionPanel.add(label_9);

		JLabel label_10 = new JLabel("");
		label_10.setBounds(512, 68, 0, 0);
		functionPanel.add(label_10);

		JLabel label_11 = new JLabel("");
		label_11.setBounds(517, 68, 0, 0);
		functionPanel.add(label_11);

		JLabel label_12 = new JLabel("");
		label_12.setBounds(522, 68, 0, 0);
		functionPanel.add(label_12);

		JLabel label_13 = new JLabel("");
		label_13.setBounds(527, 68, 0, 0);
		functionPanel.add(label_13);

		JLabel label_14 = new JLabel("");
		label_14.setBounds(532, 68, 0, 0);
		functionPanel.add(label_14);

		btnReset = new JButton("Reset");
		btnReset.setBounds(221, 281, 100, 24);
		inputFilePanel.add(btnReset);
		btnReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				txtClimate.setText(climateFileName);
				txtRegionFile.setText(defaultRegionFileName);
				txtWeightFile.setText(defaultWeightFileName);
				txtWeightFile.setEnabled(true);
				txtWeightFile.setForeground(Color.BLACK);


				climateFile = null;
				climateFileList = null;
				regionFile = defaultRegionFile;
				weightFile = defaultWeightFile;

				textGrwoingFile.setText(defaultGrowingSeasonFileName);
				textGrwoingFile.setEnabled(true);
				textGrwoingFile.setForeground(Color.BLACK);

				growingSeasonFile = defaultGrowingSeasonFile;
				chckbxEnableGrowingSeasons.setSelected(true);
				chckbxEnableGrowingSeasons.setEnabled(true);
				btnGrowingSeasonButton.setEnabled(true);

				rdbtnMax.setSelected(false);
				rdbtnMin.setSelected(false);
				rdbtnMean.setSelected(false);
				rdbtnSd.setSelected(false);
				rdbtnMax.setEnabled(false);
				rdbtnMin.setEnabled(false);
				rdbtnMean.setEnabled(false);
				rdbtnSd.setEnabled(false);
				
				btnExportIData.setEnabled(false);

				chckbxFunctionEnable.setSelected(false);
				scriptoutputTextArea.setText("");
				commandProgressbar.setIndeterminate(false);
				runProgress = false;

			}
		});

		btnSubmit = new JButton("Run");
		btnSubmit.setBounds(333, 281, 100, 24);
		inputFilePanel.add(btnSubmit);
		btnSubmit.setAction(submitButtonAction);

		commandProgressbar = new JProgressBar();
		commandProgressbar.setBounds(460, 284, 236, 18);
		inputFilePanel.add(commandProgressbar);

		chckbxEnableGrowingSeasons = new JCheckBox("Obtain annual growing season averages");
		chckbxEnableGrowingSeasons.setBounds(13, 91, 285, 23);
		chckbxEnableGrowingSeasons.setAction(checkBoxEnableGrowingAction);
		chckbxEnableGrowingSeasons.setSelected(true);
		chckbxEnableGrowingSeasons.setEnabled(true);
		inputFilePanel.add(chckbxEnableGrowingSeasons);
		chckbxEnableGrowingSeasons.setForeground(Color.BLACK);

		textGrwoingFile = new JTextField();
		textGrwoingFile.setBounds(140, 115, 346, 28);
		inputFilePanel.add(textGrwoingFile);
		textGrwoingFile.setColumns(10);
		textGrwoingFile.setEditable(false);
		textGrwoingFile.setEnabled(true);
		textGrwoingFile.setText(getDefaultGrowingSeasonFileName());
		textGrwoingFile.setForeground(Color.BLACK);

		btnGrowingSeasonButton = new JButton("Browse");
		btnGrowingSeasonButton.setEnabled(true);
		btnGrowingSeasonButton.setBounds(491, 115, 100, 29);
		btnGrowingSeasonButton.setAction(growingFileAction);
		inputFilePanel.add(btnGrowingSeasonButton);

		JLabel label_20 = new JLabel("( ! )");
		label_20.setFont(new Font("Calibri", Font.PLAIN, 12));
		label_20.setForeground(Color.BLUE);
		label_20.setBounds(298, 94, 26, 16);
		inputFilePanel.add(label_20);

		click1MouseHandler = new Click1MouseHandler(this, label_20);

		label_20.addMouseListener(click1MouseHandler);

		// Log panel for aggregation
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Log", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		panel_8.setBounds(20, 320, 721, 170);
		aggregatePanel.add(panel_8);
		panel_8.setLayout(null);

		// download output file to user's local diks
		JButton downloadButton = new JButton("Download Output File");
		downloadButton.setToolTipText("Download Aggregated Yields & Metadata");
		downloadButton.setBounds(468, 140, 230, 22);
		panel_8.add(downloadButton);

		// download a output file to local directory
		downloadButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				logger.info("downloadButton clicked: " + zipFile);

				if (zipFile == null)
				{
					scriptoutputTextArea.append("Output file is null\n");
					logger.info("Output file is null\n");
				}
				File f = new File(zipFile);
				if (f.exists())
				{
					Utils.downloadFile(f.getPath());
				} else
				{
					scriptoutputTextArea.append("No Output File Exists\n");
					logger.info("No Output File Exists\n");
				}
			}
		});

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(17, 23, 686, 114);
		panel_8.add(scrollPane_1);

		scriptoutputTextArea = new JTextArea();
		DefaultCaret caret2 = (DefaultCaret) scriptoutputTextArea.getCaret();
		caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane_1.setViewportView(scriptoutputTextArea);

		JButton btnDownloadSuggestedCitation = new JButton("Documentation");
		btnDownloadSuggestedCitation.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				logger.info("btnDownloadSuggestedCitation clicked: "
						+ citationFileName);
				if (citationFileName == null)
				{
					scriptoutputTextArea.append("Citation file is null\n");
					logger.info("Citation file is null\n");
				}
				File f = new File(citationFileName);
				if (!f.exists())
				{
					scriptoutputTextArea.append("No citation file exists\n");
					logger.info("citation file is null\n");
				} else
				{
					Utils.downloadFile(f.getPath());
				}
			}
		});

		btnDownloadSuggestedCitation.setBounds(226, 140, 230, 22);
		panel_8.add(btnDownloadSuggestedCitation);
		
		btnExportIData = new JButton("Save to project files");
		btnExportIData.addActionListener(exportIDataFileAction);
		btnExportIData.setBounds(20, 140, 180, 22);
		btnExportIData.setEnabled(false);
		panel_8.add(btnExportIData);

		// Visualize panel
		JPanel mapPanel = new JPanel();
		tabbedPane.addTab("Visualize", null, mapPanel, null);
		canvas.setBounds(22, 45, 630, 420);

		JButton btnMapButton = new JButton("Create");
		btnMapButton.setToolTipText("Create Map Images");
		btnMapButton.setBounds(659, 12, 89, 35);
		btnMapButton.addActionListener(new CreateButtonAction(this));

		mapPanel.setLayout(null);
		mapPanel.add(btnMapButton);

		JPanel panel_10 = new JPanel();
		panel_10.setBorder(new LineBorder(UIManager.getColor("Button.focus")));
		panel_10.setBounds(8, 6, 630, 41);
		mapPanel.add(panel_10);
		panel_10.setLayout(null);

		textMapField = new JTextField();
		textMapField.setBounds(106, 14, 401, 19);
		panel_10.add(textMapField);
		textMapField.setToolTipText("Source Data File to Create MAP Images");
		textMapField.setEditable(false);
		textMapField.setColumns(10);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setBounds(518, 9, 100, 25);
		panel_10.add(btnBrowse);
		btnBrowse.setAction(mapFileAction);

		JLabel lblNewLabel_1 = new JLabel("Source File:");
		lblNewLabel_1.setBounds(12, 16, 93, 15);
		panel_10.add(lblNewLabel_1);

		JButton btnRepaint = new JButton("Repaint");
		btnRepaint.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				File mapfile = new File(mapPath);
				logger.info("mappath: " + mapPath);
				lblMaplabel.setIcon(new ImageIcon(Utils.scaleImage(Vars.imgWidth, Vars.imgHeight,
						mapfile)));
			}
		});
		btnRepaint.setBounds(676, 113, 89, 25);

		try
		{
			lblMaplabel = new JLabel("MapLabel");
			lblMaplabel.setIcon(new ImageIcon(Utils
					.scaleImage(Vars.imgWidth, Vars.imgHeight, mapURL)));
			lblMaplabel.setBounds(13, 60, Vars.imgWidth, Vars.imgHeight);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		mapPanel.add(lblMaplabel);
		Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(0, new JLabel("<html>" + syear + "</html>"));
		labels.put(100, new JLabel("<html>" + eyear + "</html>"));

		JPanel sliderpanel = new JPanel();
		sliderpanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Year", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		sliderpanel.setBounds(659, 59, 124, 369);
		mapPanel.add(sliderpanel);
		sliderpanel.setLayout(null);

		slider = new JSlider();
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting() && mapPath != null)
				{
					int year = (int) source.getValue();

					String subfilename = mapPath.substring(0, mapPath.length() - 4);
					String mappath = subfilename + "_" + year + ".png";
					logger.info("image file:" + mappath);
					File mapfile = new File(mappath);
					lblMaplabel.setIcon(new ImageIcon(Utils.scaleImage(630,
							420, mapfile)));
					lblMaplabel.revalidate();
				}
			}
		});
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setBounds(5, 17, 107, 340);
		slider.setPaintLabels(true);
		slider.setMinimum(0);
		slider.setMaximum(100);
		slider.setOrientation(JSlider.VERTICAL);
		slider.setToolTipText("Aggregation year");

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		for (int i = 0; i <= 100; i++)
		{
			if (i % 20 == 0)
				labelTable.put(new Integer(i), new JLabel(Integer.toString(i)));
		}

		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		sliderpanel.add(slider);

		JButton btnNewButton = new JButton("Download");
		btnNewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				logger.info("map download: " + imgZipFile);
				if (imgZipFile == null)
				{
					JOptionPane.showMessageDialog(null, "Output file is null",
							"Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				File f = new File(imgZipFile);
				if (f.exists())
				{
					Utils.downloadFile(f.getPath());
				} else
				{
					JOptionPane.showMessageDialog(null,
							"No Output File Exists", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		});
		// btnNewButton.setToolTipText("Download Map Images into Local Directory");
		btnNewButton.setToolTipText("Download Aggregated Yields & Metadata");
		btnNewButton.setBounds(663, 440, 120, 35);
		mapPanel.add(btnNewButton);

		visualizeProgressBar = new JProgressBar();
		visualizeProgressBar.setToolTipText("Progress Bar");
		visualizeProgressBar.setOrientation(SwingConstants.VERTICAL);
		visualizeProgressBar.setBounds(760, 12, 18, 35);
		mapPanel.add(visualizeProgressBar);

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}

	/**
	 * @param parameterButtonGroup
	 * @return
	 * get a text of the selected button
	 */
	public String getSelectedButtonText(ButtonGroup parameterButtonGroup)
	{
		for (Enumeration<AbstractButton> buttons = parameterButtonGroup
				.getElements(); buttons.hasMoreElements();)
		{
			AbstractButton button = buttons.nextElement();

			if (button.isSelected())
			{
				return parameters.get(button.getText());
			}
		}

		return "";
	}

	/**
	 * @author nujwoo
	 * This is helper class used for calculating consecutive years. 
	 */
	public class Pair {
		private int l;
		private int r;

		public Pair(int l, int r) {
			this.l = l;
			this.r = r;
		}

		public int getL() {
			return l;
		}

		public int getR() {
			return r;
		}

		public void setL(int l) {
			this.l = l;
		}

		public void setR(int r) {
			this.r = r;
		}
	}
	
	/**
	 * @author nujwoo
	 * A class to call filebrowswer to save data to iData
	 */
	public class ExportIDataFileAction extends AbstractAction
	{
		private MainFrame gui;
		
		public ExportIDataFileAction(MainFrame gui)
		{
			this.gui = gui;
			putValue(NAME, "iData");
			putValue(SHORT_DESCRIPTION, "select project location for file export");
		}

		public void actionPerformed(ActionEvent e)
		{
			logger.info(outputFileList.toString());
			logger.info(zipFile);
			new FileBrowser(gui, "saveToIdata").execute();
		}
	}

	public static Boolean getDownloadProgress()
	{
		return downloadProgress;
	}

	public static void setDownloadProgress(Boolean downloadprogress)
	{
		MainFrame.downloadProgress = downloadprogress;
	}

	public ButtonGroup getGCMButtonGroup()
	{
		return GCMButtonGroup;
	}

	public ButtonGroup getRCPButtonGroup()
	{
		return RCPButtonGroup;
	}

	public ButtonGroup getClimateButtonGroup()
	{
		return ClimateButtonGroup;
	}

	public String getDefaultDownloadPath()
	{
		return defaultDownloadPath;
	}

	public void setDefaultDownloadPath(String defaultDownloadpath)
	{
		this.defaultDownloadPath = defaultDownloadpath;
	}

	public String getDownloadPath()
	{
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath)
	{
		this.downloadPath = downloadPath;
	}

	public JTextField getTxtPathfields()
	{
		return txtPathfields;
	}

	public void setTxtPathfields(JTextField txtPathfields)
	{
		this.txtPathfields = txtPathfields;
	}

	public String getGcm()
	{
		return gcm;
	}

	public void setGcm(String gcm)
	{
		this.gcm = gcm;
	}

	public String getRcp()
	{
		return rcp;
	}

	public void setRcp(String rcp)
	{
		this.rcp = rcp;
	}

	public String getClimate()
	{
		return climate;
	}

	public void setClimate(String climate)
	{
		this.climate = climate;
	}

	public static int getCounter()
	{
		return counter;
	}

	public static void setCounter(int counter)
	{
		MainFrame.counter = counter;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public JProgressBar getMainProgressBar()
	{
		return mainProgressBar;
	}

	public void setMainProgressBar(JProgressBar mainProgressBar)
	{
		this.mainProgressBar = mainProgressBar;
	}

	public JTextField getTxtClimate()
	{
		return txtClimate;
	}

	public void setTxtClimate(JTextField txtClimate)
	{
		this.txtClimate = txtClimate;
	}

	public File[] getClimateFileList()
	{
		return climateFileList;
	}

	public void setClimateFileList(File[] climateFileList)
	{
		this.climateFileList = climateFileList;
	}

	public File getClimateFile()
	{
		return climateFile;
	}

	public void setClimateFile(File climateFile)
	{
		this.climateFile = climateFile;
	}

	public String getClimateFileName()
	{
		return climateFileName;
	}

	public void setClimateFileName(String climateFileName)
	{
		this.climateFileName = climateFileName;
	}

	public String getRegionMapDir()
	{
		return regionMapDir;
	}

	public void setRegionMapDir(String regionMapDir)
	{
		this.regionMapDir = regionMapDir;
	}

	public String getWeightMapDir()
	{
		return weightMapDir;
	}

	public void setWeightMapDir(String weightMapDir)
	{
		this.weightMapDir = weightMapDir;
	}

	public JTextField getTxtRegionFile()
	{
		return txtRegionFile;
	}

	public void setTxtRegionFile(JTextField txtRegionfile)
	{
		this.txtRegionFile = txtRegionfile;
	}

	public JTextField getTxtWeightFile()
	{
		return txtWeightFile;
	}

	public void setTxtWeightFile(JTextField txtWeightfile)
	{
		this.txtWeightFile = txtWeightfile;
	}

	public File getWeightFile()
	{
		return weightFile;
	}

	public void setWeightFile(File weightFile)
	{
		this.weightFile = weightFile;
	}

	public File getRegionFile()
	{
		return regionFile;
	}

	public void setRegionFile(File regionFile)
	{
		this.regionFile = regionFile;
	}

	public JRadioButton getRdbtnMax()
	{
		return rdbtnMax;
	}

	public void setRdbtnMax(JRadioButton rdbtnMax)
	{
		this.rdbtnMax = rdbtnMax;
	}

	public JRadioButton getRdbtnMin()
	{
		return rdbtnMin;
	}

	public void setRdbtnMin(JRadioButton rdbtnMin)
	{
		this.rdbtnMin = rdbtnMin;
	}

	public JRadioButton getRdbtnMean()
	{
		return rdbtnMean;
	}

	public void setRdbtnMean(JRadioButton rdbtnMean)
	{
		this.rdbtnMean = rdbtnMean;
	}

	public JRadioButton getRdbtnSd()
	{
		return rdbtnSd;
	}

	public void setRdbtnSd(JRadioButton rdbtnSd)
	{
		this.rdbtnSd = rdbtnSd;
	}

	public String getUserHome()
	{
		return userHome;
	}

	public void setUserHome(String userHome)
	{
		this.userHome = userHome;
	}

	public JCheckBox getChckbxFunctionEnable()
	{
		return chckbxFunctionEnable;
	}

	public void setChckbxFunctionEnable(JCheckBox chckbxFunctionEnable)
	{
		this.chckbxFunctionEnable = chckbxFunctionEnable;
	}

	public ButtonGroup getAggregationButtonGroup()
	{
		return AggregationButtonGroup;
	}

	public String getFunctioName()
	{
		return functioName;
	}

	public void setFunctioName(String functioName)
	{
		this.functioName = functioName;
	}

	public static Logger getLogger()
	{
		return logger;
	}

	public static void setLogger(Logger logger)
	{
		MainFrame.logger = logger;
	}

	public String getOutputPath()
	{
		return outputPath;
	}

	public void setOutputPath(String outputPath)
	{
		this.outputPath = outputPath;
	}

	public String getTempPath()
	{
		return tempPath;
	}

	public void setTempPath(String tempPath)
	{
		this.tempPath = tempPath;
	}

	public String getJarPathString()
	{
		return jarPathString;
	}

	public void setJarPathString(String jarPathString)
	{
		this.jarPathString = jarPathString;
	}

	public File getJarPath()
	{
		return jarPath;
	}

	public void setJarPath(File jarPath)
	{
		this.jarPath = jarPath;
	}

	public String getJarParentString()
	{
		return jarParentString;
	}

	public void setJarParentString(String jarParentString)
	{
		this.jarParentString = jarParentString;
	}

	public File getBinPath()
	{
		return binPath;
	}

	public void setBinPath(File binPath)
	{
		this.binPath = binPath;
	}

	public String getHomePath()
	{
		return homePath;
	}

	public void setHomePath(String homePath)
	{
		this.homePath = homePath;
	}

	public String getDefaultPath()
	{
		return defaultPath;
	}

	public void setDefaultPath(String defaultPath)
	{
		this.defaultPath = defaultPath;
	}

	public FileHandler getFh()
	{
		return fh;
	}

	public void setFh(FileHandler fh)
	{
		this.fh = fh;
	}

	public static Boolean getRunProgress()
	{
		return runProgress;
	}

	public static void setRunProgress(Boolean runProgress)
	{
		MainFrame.runProgress = runProgress;
	}

	public static Boolean getMapProgress()
	{
		return mapProgress;
	}

	public static void setMapProgress(Boolean mapProgress)
	{
		MainFrame.mapProgress = mapProgress;
	}

	public String getDefaultRegionFileName()
	{
		return defaultRegionFileName;
	}

	public void setDefaultRegionFileName(String defaultRegionFileName)
	{
		this.defaultRegionFileName = defaultRegionFileName;
	}

	public String getDefaultWeightFileName()
	{
		return defaultWeightFileName;
	}

	public void setDefaultWeightFileName(String defaultWeightFileName)
	{
		this.defaultWeightFileName = defaultWeightFileName;
	}

	public File getDefaultRegionFile()
	{
		return defaultRegionFile;
	}

	public void setDefaultRegionFile(File defaultRegionFile)
	{
		this.defaultRegionFile = defaultRegionFile;
	}

	public File getDefaultWeightFile()
	{
		return defaultWeightFile;
	}

	public void setDefaultWeightFile(File defaultWeightFile)
	{
		this.defaultWeightFile = defaultWeightFile;
	}

	public String getCitationFileName()
	{
		return citationFileName;
	}

	public void setCitationFileName(String citationFileName)
	{
		this.citationFileName = citationFileName;
	}

	public String getMapPath()
	{
		return mapPath;
	}

	public void setMapPath(String mapPath)
	{
		this.mapPath = mapPath;
	}

	public String getZipFile()
	{
		return zipFile;
	}

	public void setZipFile(String zipFile)
	{
		this.zipFile = zipFile;
	}

	public String getImgZipFile()
	{
		return imgZipFile;
	}

	public void setImgZipFile(String imgZipFile)
	{
		this.imgZipFile = imgZipFile;
	}

	public JPanel getContentPane()
	{
		return contentPane;
	}

	public JPanel getFunctionPanel()
	{
		return functionPanel;
	}

	public void setFunctionPanel(JPanel functionPanel)
	{
		this.functionPanel = functionPanel;
	}

	public URL getMapURL()
	{
		return mapURL;
	}

	public void setMapURL(URL mapURL)
	{
		this.mapURL = mapURL;
	}

	public JTextArea getOutputTextArea()
	{
		return outputTextArea;
	}

	public void setOutputTextArea(JTextArea outputTextArea)
	{
		this.outputTextArea = outputTextArea;
	}

	public JTextArea getScriptoutputTextArea()
	{
		return scriptoutputTextArea;
	}

	public void setScriptoutputTextArea(JTextArea scriptoutputTextArea)
	{
		this.scriptoutputTextArea = scriptoutputTextArea;
	}

	public JTextField getTextMapField()
	{
		return textMapField;
	}

	public void setTextMapField(JTextField textMapField)
	{
		this.textMapField = textMapField;
	}

	public JProgressBar getCommandProgressbar()
	{
		return commandProgressbar;
	}

	public void setCommandProgressbar(JProgressBar commandProgressbar)
	{
		this.commandProgressbar = commandProgressbar;
	}

	public JProgressBar getVisualizeProgressBar()
	{
		return visualizeProgressBar;
	}

	public void setVisualizeProgressBar(JProgressBar visualizeProgressBar)
	{
		this.visualizeProgressBar = visualizeProgressBar;
	}

	public JLabel getLblMaplabel()
	{
		return lblMaplabel;
	}

	public void setLblMaplabel(JLabel lblMaplabel)
	{
		this.lblMaplabel = lblMaplabel;
	}

	public JSlider getSlider()
	{
		return slider;
	}

	public void setSlider(JSlider slider)
	{
		this.slider = slider;
	}

	public int getSyear()
	{
		return syear;
	}

	public void setSyear(int syear)
	{
		this.syear = syear;
	}

	public int getEyear()
	{
		return eyear;
	}

	public void setEyear(int eyear)
	{
		this.eyear = eyear;
	}

	public HashMap<String, String> getParameters()
	{
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters)
	{
		this.parameters = parameters;
	}

	public String[] getFullNameOfParameters()
	{
		return fullNameOfParameters;
	}

	public void setFullNameOfParameters(String[] fullNameOfParameters)
	{
		this.fullNameOfParameters = fullNameOfParameters;
	}

	public String[] getAbbrNameOfParameters()
	{
		return abbrNameOfParameters;
	}

	public void setAbbrNameOfParameters(String[] abbrNameOfParameters)
	{
		this.abbrNameOfParameters = abbrNameOfParameters;
	}

	public Action getAboutAction()
	{
		return aboutAction;
	}

	public Action getRegionFileAction()
	{
		return regionFileAction;
	}

	public Action getRegionFileUploadAction()
	{
		return regionFileUploadAction;
	}

	public Action getWeightFileAction()
	{
		return weightFileAction;
	}

	public Action getWeightFileUploadAction()
	{
		return weightFileUploadAction;
	}

	public Action getClimateFileAction()
	{
		return climateFileAction;
	}

	public Action getCheckBoxAction()
	{
		return checkBoxAction;
	}

	public String getMapFile()
	{
		return mapFile;
	}

	public MapCanvas getCanvas()
	{
		return canvas;
	}

	public String getMapSourceFile()
	{
		return mapSourceFile;
	}

	public void setMapSourceFile(String mapSourceFile)
	{
		this.mapSourceFile = mapSourceFile;
	}

	public Vector<String> getOutputFileList()
	{
		return outputFileList;
	}

	public void setOutputFileList(Vector<String> outputFileList)
	{
		this.outputFileList = outputFileList;
	}

	public List<Pair> getPeriods()
	{
		return periods;
	}

	public void setPeriods(List<Pair> periods)
	{
		this.periods = periods;
	}

	public String getOutputFile()
	{
		return outputFile;
	}

	public void setOutputFile(String outputFile)
	{
		this.outputFile = outputFile;
	}

	public String getClimateRunName()
	{
		return climateRunName;
	}

	public void setClimateRunName(String climateRunName)
	{
		this.climateRunName = climateRunName;
	}

	public String getClimateFnsName()
	{
		return climateFnsName;
	}

	public void setClimateFnsName(String climateFnsName)
	{
		this.climateFnsName = climateFnsName;
	}

	public String getMapRunName()
	{
		return mapRunName;
	}

	public void setMapRunName(String mapRunName)
	{
		this.mapRunName = mapRunName;
	}

	public String getMapGeneratorName()
	{
		return mapGeneratorName;
	}

	public void setMapGeneratorName(String mapGeneratorName)
	{
		this.mapGeneratorName = mapGeneratorName;
	}

	public JTextField getTextGrwoingFile()
	{
		return textGrwoingFile;
	}

	public void setTextGrwoingFile(JTextField textGrwoingFile)
	{
		this.textGrwoingFile = textGrwoingFile;
	}

	public JCheckBox getChckbxEnableGrowingSeasons()
	{
		return chckbxEnableGrowingSeasons;
	}

	public void setChckbxEnableGrowingSeasons(JCheckBox chckbxEnableGrowingSeasons)
	{
		this.chckbxEnableGrowingSeasons = chckbxEnableGrowingSeasons;
	}

	public JButton getBtnGrowingButton()
	{
		return btnGrowingSeasonButton;
	}

	public void setBtnGrowingButton(JButton btnGrowingButton)
	{
		this.btnGrowingSeasonButton = btnGrowingButton;
	}

	public String getGrowingSeasonDir()
	{
		return growingSeasonDir;
	}

	public void setGrowingSeasonDir(String growingSeasonDir)
	{
		this.growingSeasonDir = growingSeasonDir;
	}

	public String getDefaultGrowingSeasonFileName()
	{
		return defaultGrowingSeasonFileName;
	}

	public void setDefaultGrowingSeasonFileName(String defaultGrowingSeasonFileName)
	{
		this.defaultGrowingSeasonFileName = defaultGrowingSeasonFileName;
	}

	public File getDefaultGrowingSeasonFile()
	{
		return defaultGrowingSeasonFile;
	}

	public void setDefaultGrowingSeasonFile(File defaultGrowingSeasonFile)
	{
		this.defaultGrowingSeasonFile = defaultGrowingSeasonFile;
	}

	public File getGrowingSeasonFile()
	{
		return growingSeasonFile;
	}

	public void setGrowingSeasonFile(File growingSeasonFile)
	{
		this.growingSeasonFile = growingSeasonFile;
	}

	public JButton getBtnGrowingSeasonButton()
	{
		return btnGrowingSeasonButton;
	}

	public void setBtnGrowingSeasonButton(JButton btnGrowingSeasonButton)
	{
		this.btnGrowingSeasonButton = btnGrowingSeasonButton;
	}

	public JButton getBtnSubmit()
	{
		return btnSubmit;
	}

	public void setBtnSubmit(JButton btnSubmit)
	{
		this.btnSubmit = btnSubmit;
	}

	public Action getSubmitButtonAction()
	{
		return submitButtonAction;
	}

	public Action getMapFileAction()
	{
		return mapFileAction;
	}

	public Action getGrowingFileAction()
	{
		return growingFileAction;
	}

	public JButton getBtnReset()
	{
		return btnReset;
	}

	public void setBtnReset(JButton btnReset)
	{
		this.btnReset = btnReset;
	}

	public String getFullNameOfParameter(String abbr)
	{
		int size = abbrNameOfParameters.length;

		for(int i = 0; i < size; i++)
		{
			if(abbr.equals(abbrNameOfParameters[i]))
				return fullNameOfParameters[i];
		}

		return "";
	}

	public int getMinStartYear()
	{
		return minStartYear;
	}

	public void setMinStartYear(int minStartYear)
	{
		this.minStartYear = minStartYear;
	}

	public int getMaxEndYear()
	{
		return maxEndYear;
	}

	public void setMaxEndYear(int maxEndYear)
	{
		this.maxEndYear = maxEndYear;
	}

	public JCheckBox getChckbxByForce()
	{
		return chckbxByForce;
	}

	public String getBrowserPath()
	{
		return browserPath;
	}

	public ArrayList<String> getMetadataGeneral()
	{
		return metadataGeneral;
	}

	public void setMetadataGeneral(ArrayList<String> metadataGeneral)
	{
		this.metadataGeneral = metadataGeneral;
	}

	public ArrayList<String> getMetadataUser()
	{
		return metadataUser;
	}

	public void setMetadataUser(ArrayList<String> metadataUser)
	{
		this.metadataUser = metadataUser;
	}

	public JButton getBtnExportIData()
	{
		return btnExportIData;
	}
}
