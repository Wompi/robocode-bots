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
package wompi.numbat.gun.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NumbatPointHandler
{
	private HashMap<Integer, NumbatRelativePoint>	mapRelative;
	private HashMap<Integer, NumbatAbsolutePoint>	mapAbsolute;

	private long									resetTime	= Long.MAX_VALUE;

	private StringBuilder							eRelativeHistory;
	private StringBuilder							eAbsoluteHistory;

	public NumbatPointHandler()
	{
		mapRelative = new HashMap<Integer, NumbatRelativePoint>();
		mapAbsolute = new HashMap<Integer, NumbatAbsolutePoint>();
	}

	public void registerAbsolutePoint(long time, long scanDiff, double x, double y, double velocity, double lastVelocity, double heading,
			double lastHeading)
	{
		NumbatAbsolutePoint absPoint = new NumbatAbsolutePoint();
		absPoint.registerState(x, y, velocity, lastVelocity, heading, lastHeading);

		NumbatAbsolutePoint point = mapAbsolute.get(absPoint.getHash());
		if (point == null)
		{
			mapAbsolute.put(absPoint.getHash(), point = absPoint);
		}
		point.increaseVisits();

		if (resetTime > time)
		{
			onPrintAbsolute(time);
			eAbsoluteHistory = new StringBuilder();
		}
		eAbsoluteHistory.insert(0, (char) point.getKey()).setLength(100);
		resetTime = time;

		if (point.getVisits() > 3)
		{
			System.out.format("[%03d][%03d:%03d] %04d p=%9s %s\n", time, point.getX(), point.getY(), point.getVisits(), point.getBinaryString(),
					eAbsoluteHistory.toString());
		}

	}

	public void registerRelativePoint(long time, long scanDiff, double velocity, double lastVelocity, double heading, double lastHeading)
	{
		NumbatRelativePoint relPoint = new NumbatRelativePoint();
		relPoint.registerState(velocity, lastVelocity, heading, lastHeading);

		NumbatRelativePoint point = mapRelative.get(relPoint.getKey());
		if (point == null)
		{
			mapRelative.put(relPoint.getKey(), point = relPoint);
		}
		point.increaseVisits();

		// System.out.format("[%03d] p=%9s --- (%s,%d)  v=%3.2f lv=%3.2f h=%3.2f lh=%3.2f (%d) \n", time,point.getBinaryString(),(char)
		// (point.getKey()),point.getKey(),velocity,lastVelocity,Math.toDegrees(heading),Math.toDegrees(lastHeading),scanDiff);

		if (resetTime > time)
		{
			// onPrintRelative(time);
			eRelativeHistory = new StringBuilder();
		}
		eRelativeHistory.insert(0, (char) point.getKey()).setLength(100);
		// System.out.format("[%03d] p=%9s %s\n", time,point.getBinaryString(),eHistory.toString());
		resetTime = time;
	}

	public void onPrintRelative(long time)
	{
		int count = 0;
		List<NumbatRelativePoint> sorted = new ArrayList<NumbatRelativePoint>(mapRelative.values());
		Collections.sort(sorted);

		for (NumbatRelativePoint point : sorted)
		{
			System.out.format("[%03d][%03d] p=%9s --- count: %d (%s,%d)\n", time, ++count, point.getBinaryString(), point.getVisits(),
					(char) (point.getKey()), point.getKey());

		}

	}

	public void onPrintAbsolute(long time)
	{
		int count = 0;
		List<NumbatAbsolutePoint> sorted = new ArrayList<NumbatAbsolutePoint>(mapAbsolute.values());
		Collections.sort(sorted);

		for (NumbatAbsolutePoint point : sorted)
		{
			if (point.getVisits() > 3)
			{
				System.out.format("[%03d][%03d] [%3d:%3d] --- count: %d \n", time, ++count, point.getX(), point.getY(), point.getVisits());
			}
		}
	}

}
