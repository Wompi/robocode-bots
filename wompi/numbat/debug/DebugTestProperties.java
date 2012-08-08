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

public class DebugTestProperties
{
	private static boolean	isActive	= true;

	// heading debug

	public static void onKeyPressed(char c)
	{
		if ('t' != c) return;
		isActive = !isActive;
	}

	public static void execute()
	{
		if (isActive)
		{}
		else
		{}
	}
}
