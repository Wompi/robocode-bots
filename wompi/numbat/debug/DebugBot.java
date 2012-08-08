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
package wompi.numbat.debug;

import robocode.AdvancedRobot;

public class DebugBot
{
	private static Object	myBot;

	public static void init(AdvancedRobot bot)
	{
		myBot = bot;
	}

	public static AdvancedRobot getBot()
	{
		return (AdvancedRobot) myBot;
	}

}
