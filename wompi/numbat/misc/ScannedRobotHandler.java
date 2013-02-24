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

import java.util.ArrayList;

import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.numbat.debug.DebugMiscProperties;

public class ScannedRobotHandler
{
	private ArrayList<ScannedRobotEvent>	turnEvents;

	private String							bugName;

	private boolean							isSkippedBug;

	public void onStatus(StatusEvent e)
	{
		turnEvents = new ArrayList<ScannedRobotEvent>();
		bugName = null;
		isSkippedBug = false;
	}

	/**
	 * Because of a bug with skipped turns, where it can happen that a target gets a second scan event and with this event holding false informations,
	 * it is necessary to collect all scan events and look if the bug may occur. Another nasty side, and the reason while not just use
	 * getAllScanEvents(..) is because the rumble server is right now mostly running by 1.7.3.0 clients and in those version this event is function is
	 * not implemented. So i need this little helper class to manage the scan events properly. The bug will send two scan events for the same target,
	 * both events one after each other, so it can detected just by compare the names. The second event can hold the right data but this is not sure.
	 * The reason why i reject all scan events of this turn, is because i spotted some bad behavior of all events, no matter if it has a second event
	 * too. So to be on the sure side i just reject them all. Maybe it needs further research for skipped turns in general, it might be that normal
	 * events have some bad data too
	 * 
	 * @param e
	 *            - scan event of the current turn
	 */
	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (isSkippedBug) return;
		String eName = e.getName();
		if (bugName == eName)
		{
			System.out.format("[%d] BUG: second scan event for %s - reject all scan events\n", e.getTime(), eName);
			DebugMiscProperties.debugScanEvents();
			turnEvents.clear();
			isSkippedBug = true;
			return;
		}
		bugName = eName;
		turnEvents.add(e);
	}

	public boolean hasEvents()
	{
		return (turnEvents.size() > 0);
	}

	public ArrayList<ScannedRobotEvent> getAllScanEvents()
	{
		return turnEvents;
	}
}
