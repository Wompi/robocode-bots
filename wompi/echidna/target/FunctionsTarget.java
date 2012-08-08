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
package wompi.echidna.target;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import robocode.AdvancedRobot;
import wompi.echidna.misc.DebugPointLists;

public class FunctionsTarget
{

	/**
	 * the target holds relative coodinates - be carefull with that
	 * 
	 * @param target
	 * @param firePower
	 * @param isLinear
	 * @param debugPoints
	 * @return
	 */
	public static Point2D calculateGuessPosition(ATarget target, double firePower, boolean isLinear, DebugPointLists debugPoints)
	{
		AdvancedRobot myRobot = target.getRobot();
		double guessHeading = target.getHeading();
		int i = 0;

		double gx = target.getX();
		double gy = target.getY();

		// debug
		ArrayList<Point2D> debugBuffy = null;
		if (debugPoints != null)
		{
			debugPoints.reset();
			debugBuffy = debugPoints.goodPoints;
		}

		double velocity = target.getVelocity();
		double headDiff = target.getHeadDiff();

		while (++i * (18 - 2.7 * firePower) < Math.hypot(gx, gy))
		{
			gx += Math.sin(guessHeading) * velocity;    // improve this with avgVelocity
			gy += Math.cos(guessHeading) * velocity;
			if (!isLinear) guessHeading += headDiff;

			// debug
			if (debugPoints != null)
			{
				debugBuffy.add(new Point2D.Double(gx + myRobot.getX(), gy + myRobot.getY()));
			}

			if (!new Rectangle2D.Double(17, 17, myRobot.getBattleFieldWidth() - 2 * 17, myRobot.getBattleFieldWidth() - 2 * 17).contains(
					gx + myRobot.getX(), gy + myRobot.getY()))
			{
				velocity = -velocity;
				headDiff = -headDiff;
				// i+=2; // if he changes direction he probably loose some moves

				// debug
				if (debugPoints != null)
				{
					debugBuffy.remove(debugBuffy.size() - 1);
					debugBuffy = debugPoints.badPoints;
					debugBuffy.add(new Point2D.Double(gx + myRobot.getX(), gy + myRobot.getY()));
				}
			}
		}
		return new Point2D.Double(gx, gy);  // beware these are relative coordinates
	}

	public static ATarget caculateNearestEnemyDist(final ATarget target, ArrayList<ATarget> enemys)
	{
		enemys.remove(target);   // just in case that the target is included

		if (enemys.size() < 1) return null;

		Comparator<ATarget> depp = new Comparator<ATarget>()
		{
			@Override
			public int compare(ATarget o1, ATarget o2)
			{
				double d2 = Point2D.distance(o2.getX(), o2.getY(), target.getX(), target.getY());
				double d1 = Point2D.distance(o1.getX(), o1.getY(), target.getX(), target.getY());
				return Double.compare(d1, d2);
			}
		};
		Collections.sort(enemys, depp);

		// TODO: it might be usefull to have the whole enemys in sorted order to iterate over the enemys ... if so buid this out
		// nearestEnemys.clear();
		// nearestEnemys.addAll(enemys.subList(0,enemys.size()));
		// nearestEnemys.addAll(enemys);

		// for (ATarget next : enemys)
		// {
		// if (target.isAlive) return next;
		// }
		// return null;

		return enemys.get(0);
	}

}
