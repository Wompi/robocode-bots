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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SegmentedCountField
{
	public ArrayList<SegmentPoint>	allDangerPoints	= new ArrayList<SegmentPoint>();
	SegmentPoint					lastSavePoint;

	public SegmentedCountField()
	{
		for (int y = 50; y < 1000; y += 50)
		{
			for (int x = 50; x < 1000; x += 50)
			{

				if (x == 50 && y == 50) continue;
				if (x == 100 && y == 50) continue;
				if (x == 50 && y == 100) continue;
				if (x == 950 && y == 50) continue;
				if (x == 900 && y == 50) continue;
				if (x == 950 && y == 100) continue;

				if (y == 950 && x == 50) continue;
				if (y == 900 && x == 50) continue;
				if (y == 950 && x == 100) continue;
				if (x == 950 && y == 950) continue;
				if (x == 900 && y == 950) continue;
				if (x == 950 && y == 900) continue;

				SegmentPoint dp = new SegmentPoint();
				dp.x = x;
				dp.y = y;
				allDangerPoints.add(dp);
			}
		}
	}

	public SegmentPoint getMaxSave(final Point2D robotPos)
	{
		Comparator<SegmentPoint> buffy = new Comparator<SegmentPoint>()
		{
			@Override
			public int compare(SegmentPoint o1, SegmentPoint o2)
			{
				if (o2.dangerCount > o1.dangerCount) return 1;
				else if (o2.dangerCount == o1.dangerCount)
				{
					if (o2.distance(robotPos) < o1.distance(robotPos)) { return 1; }
				}
				return 0;
			}
		};
		Collections.sort(allDangerPoints, buffy);
		lastSavePoint = allDangerPoints.get(0);
		if (lastSavePoint.distance(robotPos) < 20)
		{
			lastSavePoint.dangerCount -= 5;
		}
		return lastSavePoint;
	}

	// public void getNextFieldPoint(double x, double y)
	// {
	// System.out.format("x=%3.2f y=%3.2f ...",x,y);
	// x = Math.max(1,Math.round(x/50)) * 50;
	// y = Math.max(1,Math.round(y/50)) * 50;
	// System.out.format("xf=%3.2f y=%3.2f \n",x,y);
	// }

	public void addDangerPoints(ArrayList<AdvancedTarget> enemys, boolean changeDanger)
	{

		for (SegmentPoint dangerPoint : allDangerPoints)
		{
			dangerPoint.dangerCount += dangerPoint.lastAdd;
			dangerPoint.lastAdd = 0;

			for (AdvancedTarget enemy : enemys)
			{
				double distance = dangerPoint.distance(enemy);

				int near = (int) ((enemy.distance * Math.min(enemy.energySwitch, 1.2)) / 50);   // cap the energy switch at 1.5 to get a escape
																								// posibility
				if (enemy.distance <= enemy.getNearestDistance())
				{
					near = (int) ((enemy.getNearestDistance() + 50) / 50);
				}

				int count = (int) (Math.max(0, near - distance / 50));
				if (count > 0)
				{

					if (changeDanger) count = 1;
					if (enemy.distance <= enemy.getNearestDistance())
					{
						count += 20;
					}
					dangerPoint.dangerCount -= count * enemy.energySwitch; // 1 * enemy.energySwitch;
					dangerPoint.lastAdd += count * enemy.energySwitch; // 1 * enemy.energySwitch;
				}
			}
		}
	}
}

class SegmentPoint extends Point2D.Double
{
	double	dangerCount;
	double	lastAdd;
}
