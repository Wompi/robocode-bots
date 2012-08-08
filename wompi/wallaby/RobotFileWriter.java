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
package wompi.wallaby;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;

public class RobotFileWriter
{

	public static void write(String test, AdvancedRobot robot, String robotName, boolean append) throws IOException
	{
		File depp = robot.getDataFile(robotName + ".csv");
		RobocodeFileOutputStream rout = new RobocodeFileOutputStream(depp.getAbsolutePath(), append);
		Writer out = new OutputStreamWriter(rout);
		try
		{
			out.write(test);
		}
		finally
		{
			out.close();
		}
	}

}
