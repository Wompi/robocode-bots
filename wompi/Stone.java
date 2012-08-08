/*******************************************************************************
 * Copyright (c)  2012  Wompi 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the ZLIB
 * which accompanies this distribution, and is available at
 * http://robowiki.net/wiki/ZLIB
 * 
 * Contributors:
 *     Wompi - initial API and implementation
 ******************************************************************************/
package wompi;

import java.awt.Color;

import robocode.Robot;

/**
 * Simple stone like target robot for Wombot
 * 
 * @author rschott
 */
public class Stone extends Robot
{
	public void run()
	{

		setBodyColor(Color.DARK_GRAY);
		setRadarColor(Color.RED);
		setGunColor(Color.GRAY);
	}
}
