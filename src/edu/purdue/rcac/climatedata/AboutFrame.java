package edu.purdue.rcac.climatedata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
 * This class is for a JFrame used in MainFrame to show a tool description
 * and link to tool's manual
 */
public class AboutFrame extends JFrame {
	private JPanel contentPane;

	
	/**
	 * Constructor to make the AboutFrame 
	 */
	public AboutFrame() {
		setTitle("About This Tool");
		String helpmessage = "";
		try {
			// read contents from the readme.html file
			BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(Vars.pathToReadme)));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			br.close();
			helpmessage = sb.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		contentPane = new JPanel();
		int bi = Vars.borderInset;
		contentPane.setBorder(new EmptyBorder(bi, bi, bi, bi));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, Vars.aboutWidth, Vars.aboutHeight);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JEditorPane dtrpnAbout = new JEditorPane();
		dtrpnAbout.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				URL url = e.getURL();
				HyperlinkEvent.EventType type = e.getEventType();
				
				try {
					if(type == HyperlinkEvent.EventType.ACTIVATED) {
						Utils.viewExternally(url);
					}
					
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		dtrpnAbout.setBounds(26, 15, 400, 450);
		dtrpnAbout.setContentType("text/html");
		dtrpnAbout.setText(helpmessage);
		dtrpnAbout.setBackground(UIManager.getColor("Panel.background"));
		dtrpnAbout.setEditable(false);
		panel.add(dtrpnAbout);
		
	}
}
