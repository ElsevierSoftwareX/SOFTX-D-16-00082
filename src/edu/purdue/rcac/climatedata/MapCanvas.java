package edu.purdue.rcac.climatedata;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
 * This is a Canvas class to show result maps.
 */
public class MapCanvas extends Canvas {

	private URL fileURL;
	private Image map;

	MapCanvas(URL url) {
		fileURL = url;
		map = Utils.scaleImage(Vars.imgWidth, Vars.imgHeight, fileURL);
	}

	public void paint(Graphics g) {
		update(g);
	}

	public void update(Graphics g) {

		g.drawImage(map, 0, 0, this);
	}
	
	public void setMap(URL url) {
		fileURL = url;
		map = Utils.scaleImage(Vars.imgWidth, Vars.imgHeight, fileURL);
	}
	public void setMap(File file) {
		try {
			fileURL = file.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map = Utils.scaleImage(Vars.imgWidth, Vars.imgHeight, fileURL);
	}
}
