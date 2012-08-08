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
package wompi.echidna.stats;

import java.util.ArrayList;

import robocode.Bullet;

public class HitStats
{
	public ArrayList<Bullet>	myBullets	= new ArrayList<Bullet>();

	double						dmgMade;
	double						dmgTaken;
	double						drained;

	public void printStats(String name)
	{
		// System.out.format("dmg:[%3.2f,%3.2f] fired:[%d,%d,%d] ratio: %3.2f drain: %3.2f %s\n",dmgMade,dmgTaken,myBullets.size(),getHits(),getMissed(),getHits()/myBullets.size(),drained,name);
		System.out
				.format("fired:[%d,%d,%d] ratio: %3.2f %s\n", myBullets.size(), getHits(), getMissed(), (double) getHits() / myBullets.size(), name);
	}

	private int getHits()
	{
		return _helper(true);
	}

	private int getMissed()
	{
		return _helper(false);
	}

	private int _helper(boolean onlyHits)
	{
		int result = 0;

		for (Bullet shot : myBullets)
		{
			if (onlyHits)
			{
				if (shot.getVictim() != null) result++;
			}
			else
			{
				if (shot.getVictim() == null) result++;
			}
		}
		return result;
	}
}
