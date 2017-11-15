package edu.purdue.rcac.climatedata.handler;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.purdue.rcac.climatedata.MainFrame;

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
 * A mouse handler class to handle a click on "!" button in the tool.
 */
public class Click1MouseHandler implements MouseListener
{
	private MainFrame gui;
	private JLabel label;

	public Click1MouseHandler(MainFrame gui, JLabel label)
	{
		this.gui = gui;
		this.label = label;
		
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		JOptionPane.showMessageDialog(
				gui,
				"For more details of sources as well as data processing consult Villoria et al. (2015), "
				+ "\navailable for download in the main page of this tool",
				"Warning", JOptionPane.WARNING_MESSAGE);
	}
	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		label.setText("<HTML><U>( ! )</U></HTML>");
		
	}
	@Override
	public void mouseExited(MouseEvent arg0)
	{
		label.setText("<HTML>( ! )</HTML>");
	}
	@Override
	public void mousePressed(MouseEvent arg0){	}
	@Override
	public void mouseReleased(MouseEvent arg0){	}
}
