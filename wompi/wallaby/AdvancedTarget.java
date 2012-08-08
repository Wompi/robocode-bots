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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import wompi.echidna.misc.SimpleAverage;
import wompi.echidna.stats.HitStats;

public class AdvancedTarget extends GravityPoint
{
	SimpleAverage				avgVelocity		= new SimpleAverage(30, "velo");
	SimpleAverage				avgHeading		= new SimpleAverage(1, "headdiff");
	TargetDistanceTracker		targetTracker	= new TargetDistanceTracker(1000);
	HitStats					hitStats		= new HitStats();

	double						heading;
	double						headDiff;

	long						lastScan;

	double						distance;
	double						absBearing;
	double						guessX;
	double						guessY;
	double						energy;
	double						energySwitch;
	double						velocity;
	double						bulletPower;

	double						bearing;

	// debug
	Vector<Point2D>				guessPointsHead	= new Vector<Point2D>();
	Vector<Point2D>				guessPointsBack	= new Vector<Point2D>();
	Point2D						firePoint;
	Point2D						lastFirePoint;

	String						name;

	ArrayList<AdvancedTarget>	nearestEnemys	= new ArrayList<AdvancedTarget>();

	public AdvancedTarget()
	{
		power = -1000.0;
	}

	public boolean isShoting(double newEnergy, long time)
	{
		// if ((time - lastScan) == 1)
		// {
		// double diff = energy - newEnergy;
		// if ( diff < 3.0 && diff > 0.1)
		// {
		// System.out.format("FIRE[%d]: %3.2f %s \n",time,diff,name);
		// return true;
		// }
		// }
		return false;
	}

	public void guessPosition(double avg, double botX, double botY, double headingDiff)
	{
		boolean check;
		double guessHeading = heading;
		int i = 0;
		guessX = x;
		guessY = y;

		// debug
		Vector<Point2D> debugPoints = guessPointsHead;
		// System.out.format("avg=%3.5f", avg);
		// boolean isOscilator = (targetTracker.isOscilator(name) > 40);
		// if (isOscilator)
		// {
		// avg = avg;
		// //System.out.format("Switch to HEAD_ON! avg=%3.2f\n",avg);
		// }
		// else
		// //System.out.format("Switch to CIRCULAR!\n");

		while (check = (++i * (20 - 3.0 * bulletPower) < Math.hypot(botX - guessX, botY - guessY)))   // / 0.8
		{
			// double dir = targetTracker.getDirectionForIndex(i);
			// if ( Math.signum(dir) != Math.signum(avg)) dir = 1;

			guessX = guessX + Math.sin(guessHeading) * avg;
			guessY = guessY + Math.cos(guessHeading) * avg;
			// /if (!isOscilator)
			guessHeading += headingDiff;

			debugPoints.add(new Point2D.Double(guessX, guessY));    // / just for debug
			if (!new Rectangle2D.Double(15, 15, 1000 - 30, 1000 - 30).contains(guessX, guessY) && check)
			{
				avg = -avg;
				headingDiff = -headingDiff;
				i += 2;     // if he changes direction he probably loose some moves
				debugPoints = guessPointsBack;
			}
		}
		firePoint = new Point2D.Double(guessX, guessY);
	}

	public void caculateNearestEnemyDist(ArrayList<AdvancedTarget> enemys)
	{
		enemys.remove(this);
		Comparator<AdvancedTarget> depp = new Comparator<AdvancedTarget>()
		{
			@Override
			public int compare(AdvancedTarget o1, AdvancedTarget o2)
			{
				return (o2.distance(AdvancedTarget.this) < o1.distance(AdvancedTarget.this)) ? 1 : -1;
			}
		};
		Collections.sort(enemys, depp);
		nearestEnemys.clear();
		// nearestEnemys.addAll(enemys.subList(0,enemys.size()));
		nearestEnemys.addAll(enemys);
	}

	public double getNearestDistance()
	{
		double result = 0;
		if (nearestEnemys.size() > 0) result = nearestEnemys.get(0).distance(this);
		return result;
	}

}
