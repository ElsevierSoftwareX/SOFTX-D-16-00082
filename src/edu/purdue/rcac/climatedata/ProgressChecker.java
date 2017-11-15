package edu.purdue.rcac.climatedata;

import javax.swing.JProgressBar;

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
 * This is a Thread class to update a ProgressBar's value.
 */
public class ProgressChecker extends Thread {
	
	private JProgressBar progressBar;
	public ProgressChecker(JProgressBar bar) {
		progressBar = bar;
	}
	public void run() {
		progressBar.setStringPainted(true);
		while(MainFrame.getCounter() != progressBar.getMaximum()) {
			progressBar.setValue(MainFrame.getCounter());
			progressBar.setString(MainFrame.getCounter() + "%");
		}
		progressBar.setValue(MainFrame.getCounter());
		progressBar.setString(MainFrame.getCounter() + "%");
	}
}
