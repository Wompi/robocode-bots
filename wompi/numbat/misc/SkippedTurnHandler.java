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
package wompi.numbat.misc;

import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import wompi.numbat.debug.DebugMiscProperties;

public class SkippedTurnHandler
{
	private long	lastTime;
	private int		turnBlindSkipped;

	public void onStatus(StatusEvent e)
	{
		turnBlindSkipped = 0;
		if (e.getTime() - lastTime > 1)
		{
			turnBlindSkipped++;
		}
		lastTime = e.getTime();

		DebugMiscProperties.debugBlindSkippedTurns(e.getTime());
	}

	public void onSkippedTurn(SkippedTurnEvent e)
	{
		DebugMiscProperties.debugSkippedTurns();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (turnBlindSkipped > 0)
		{
			System.out.format("[%d] hmm not skipped at all!\n", e.getTime());
		}
	}

}
