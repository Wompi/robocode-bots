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

public class DebugProperties
{
	private static boolean	isDebug	= false;

	public static void inti(boolean debug)
	{
		isDebug = debug;
	}

	// teststuff
	public static void debugTestStep(int key, double velocity, double lastvelo, double deltaH, String name)
	{
		if (!isDebug) return;
		// int headInt = (int) Math.rint(Math.toDegrees(deltaHeading*2));
		// int vint = (int) Math.rint(velocity*2);
		// int step = (17 * (headInt + 20) + (vint + 16));

		int hint = (int) Math.rint(Math.toDegrees(deltaH * 2)) + 20;
		int vint = (int) (Math.rint(velocity * 2)) + 16;
		int aInt = (int) (Math.rint(lastvelo - velocity)) + 2;

		int head = (hint) << 6;
		int v = (head + (vint)) << 3;
		int step = v + aInt;

		DebugBot.getBot().setDebugProperty(
				"Encode Test Step",
				String.format("%d v=%3.2f (%d)  dH=%3.2f (%d|%d) a=%3.2f (%d) %s", step, velocity, vint, Math.toDegrees(deltaH), hint, head, lastvelo
						- velocity, aInt, name));

		int bHead = step >> 9;
		int bvelo = (step >> 3) - (bHead << 6);
		int baccel = step - (bvelo << 3) - (bHead << 9);
		DebugBot.getBot().setDebugProperty(
				"Decode Step",
				String.format("%d v=%3.2f dH=%3.2f a=%3.2f %s", step, (double) ((bvelo - 16) / 2.0), (double) ((bHead - 20) / 2.0),
						(double) (baccel - 2), name));
	}
}
