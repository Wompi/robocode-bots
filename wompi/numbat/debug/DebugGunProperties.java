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

import java.util.ArrayList;
import java.util.Iterator;

import robocode.Bullet;
import wompi.numbat.gun.misc.NumbatMultiHolder;
import wompi.numbat.gun.misc.NumbatSingleHolder;

public class DebugGunProperties
{
	private static boolean				isActive	= true;

	// single tick class stats
	private static String				singleClass;
	private static String				multiClass;

	// bullet related - maybe put this in extra classes
	private static ArrayList<Bullet>	myBullets	= new ArrayList<Bullet>();
	private static int					bFired;
	private static int					bMissed;
	private static int					bHit;
	private static int					missedShootings;

	private static String				gName;
	private static String				fName;

	public static void onKeyPressed(char c)
	{
		if ('g' != c) return;

		isActive = !isActive;
	}

	public static void debugCurrentGun(String gunName)
	{
		gName = gunName;
	}

	public static void debugCurrentFire(String fireName)
	{
		fName = fireName;
	}

	public static void debugPatternClasses()
	{
		if (NumbatSingleHolder.classCount > 0)
		{
			int max = NumbatSingleHolder.maxCount;
			int avg = NumbatSingleHolder.avgSum / NumbatSingleHolder.classCount;
			int idStats = NumbatSingleHolder.classIDStats.size();
			singleClass = String.format("%d IDs=%d  Count: max=%d avg=%d", NumbatSingleHolder.classCount, idStats, max, avg);
		}
		if (NumbatMultiHolder.classCount > 0)
		{
			multiClass = String.format("%d - avg: %3.2f max: %d", NumbatMultiHolder.classCount,
					(double) (NumbatMultiHolder.classElements / NumbatMultiHolder.classCount), NumbatMultiHolder.classMaxElements);
		}
	}

	public static void debugGunHitRate(Bullet newBullet)
	{
		if (newBullet == null) return;
		myBullets.add(newBullet);
		bFired++;

		Iterator<Bullet> iter = myBullets.iterator();
		while (iter.hasNext())
		{
			Bullet bullet = iter.next();
			if (!bullet.isActive())
			{
				if (bullet.getVictim() != null) bHit++;
				else bMissed++;
				iter.remove();
			}
		}
	}

	public static void debugMissedShootings()
	{
		if (DebugBot.getBot().getGunHeat() == 0)
		{
			missedShootings++;
		}
	}

	public static void execute()
	{
		if (isActive)
		{
			DebugBot.getBot().setDebugProperty("Gun", String.format("%s (%s)", gName, fName));
			DebugBot.getBot().setDebugProperty("SingleHolder", singleClass);
			DebugBot.getBot().setDebugProperty("MultiHolder", multiClass);
			DebugBot.getBot().setDebugProperty("GunRatio",
					String.format("%d hit: %d miss: %d ratio: %3.2f", bFired, bHit, bMissed, (double) bHit / bFired));
			DebugBot.getBot().setDebugProperty("MissedShootings", String.format("%d", missedShootings));
		}
		else
		{
			DebugBot.getBot().setDebugProperty("Gun", null);
			DebugBot.getBot().setDebugProperty("SingleHolder", null);
			DebugBot.getBot().setDebugProperty("MultiHolder", null);
			DebugBot.getBot().setDebugProperty("GunRatio", null);
			DebugBot.getBot().setDebugProperty("MissedShootings", null);
		}
	}
}
