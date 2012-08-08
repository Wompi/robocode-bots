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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Rules;
import robocode.util.Utils;
import wompi.echidna.gun.fire.AFire;
import wompi.echidna.target.ATarget;
import wompi.robomath.RobotMath;

public final class WallabyPainter
{

	public static long	time;

	// target related
	static GeneralPath	targetPath;

	// robot related
	static GeneralPath	robotPath;

	// general variables

	public static void drawCenterPos(Graphics2D g, Point2D center, double radius)
	{
		PaintHelper.drawArc(center, radius, 0.0, PaintHelper.PI_CIRCLE, false, g, PaintHelper.whiteTrans);  // kreis center->maxradius
	}

	public static void drawAngleLine(Graphics2D g, Point2D center, double angle, double distance, Color color)
	{
		if (center == null) return;

		Point2D helper = RobotMath.calculatePolarPoint(angle, distance, center);

		PaintHelper.drawLine(center, helper, g, color);
	}

	public static void drawForce(Graphics2D g, Point2D forcePoint, Point2D robotPos)
	{
		forcePoint = new Point2D.Double(robotPos.getX() + forcePoint.getX(), robotPos.getY() + forcePoint.getY());
		double angle = RobotMath.calculateAngle(robotPos, forcePoint);
		Point2D forceHelperPoint = RobotMath.calculatePolarPoint(angle, 100, robotPos);
		PaintHelper.drawLine(robotPos, forceHelperPoint, g, Color.GREEN);
	}

	public static void drawEnemyGuessedFiredirection(Graphics2D g, Collection<ATarget> enemys, Point2D robotPos)
	{
		if (enemys.size() <= 2) return;
		for (ATarget enemy : enemys)
		{
			if (!enemy.isAlive()) continue;
			Point2D minPoint = null;
			for (ATarget target : enemys)
			{
				if (!target.isAlive()) continue;
				if (enemy != target)
				{
					double dist = Point2D.distance(enemy.getX(), enemy.getY(), target.getX(), target.getY());
					if (minPoint == null || dist < Point2D.distance(enemy.getX(), enemy.getY(), minPoint.getX(), minPoint.getY()))
					{
						minPoint = new Point2D.Double(target.getX(), target.getY());
					}
					// PaintHelper.drawLine(enemy, target, g, Color.LIGHT_GRAY);
				}
			}
			double distER = Point2D.distance(enemy.getX(), enemy.getY(), robotPos.getX(), robotPos.getY());

			if (minPoint == null)
			{
				minPoint = new Point2D.Double(enemy.getX(), enemy.getY());
			}

			double distEM = Point2D.distance(enemy.getX(), enemy.getY(), minPoint.getX(), minPoint.getY());
			Point2D eP = new Point2D.Double(enemy.getX() + robotPos.getX(), enemy.getY() + robotPos.getY());

			if (minPoint == null || distER < distEM)
			{
				PaintHelper.drawLine(eP, robotPos, g, Color.RED);
			}
			else
			{
				minPoint = new Point2D.Double(minPoint.getX() + robotPos.getX(), minPoint.getY() + robotPos.getY());
				double angle = RobotMath.calculateAngle(eP, minPoint);
				Point2D lP = RobotMath.calculatePolarPoint(angle, 1200, eP);
				// PaintHelper.drawLine(eP, minPoint, g, Color.BLUE);
				PaintHelper.drawLine(eP, lP, g, Color.BLUE);
			}
		}
	}

	public static void drawSegmentCountField(Graphics2D g, SegmentedCountField dangerField, Point2D robotPos, SegmentPoint savePoint)
	{

		double max = (savePoint != null) ? savePoint.dangerCount : 0;
		for (SegmentPoint dangerPoint : dangerField.allDangerPoints)
		{
			// if (dangerPoint.x == 50.0) System.out.format("\n");
			// System.out.format("%3.0f %3.0f %d \n",dangerPoint.x,dangerPoint.y,dangerPoint.dangerCount);

			Color forceColor = Color.GREEN;
			int forceSize = 10;

			if (dangerPoint.dangerCount >= 0)
			{
				// System.out.format("%3.0f %3.0f %d \n",dangerPoint.x,dangerPoint.y,dangerPoint.dangerCount);
				forceSize = (int) (dangerPoint.dangerCount / 10) + 3;
			}

			if (dangerPoint.dangerCount < 0)
			{
				if (dangerPoint.dangerCount < -10)
				{
					forceColor = new Color(255, 0, 0);
					forceSize = 6;
				}
				else if (dangerPoint.dangerCount < -8)
				{
					forceColor = new Color(200, 0, 0);
					forceSize = 5;
				}
				else if (dangerPoint.dangerCount < -6)
				{
					forceColor = new Color(150, 0, 0);
					forceSize = 4;
				}
				else if (dangerPoint.dangerCount < -4)
				{
					forceColor = new Color(100, 0, 0);
					forceSize = 3;
				}
				else
				{
					forceColor = Color.YELLOW;
					forceSize = 2;
				}
			}

			if (dangerPoint.dangerCount == max)
			{
				forceColor = Color.PINK;
				forceSize = 20;
			}

			g.setColor(forceColor);
			g.drawString(String.format("%3.0f", dangerPoint.dangerCount), (int) dangerPoint.x, (int) dangerPoint.y);
			// PaintHelper.drawPoint(dangerPoint, forceColor, g, forceSize);
		}

		PaintHelper.drawArc(savePoint, 30, 0, PaintHelper.PI_CIRCLE, false, g, Color.RED);
		PaintHelper.drawLine(robotPos, savePoint, g, Color.DARK_GRAY);

	}

	public static void drawFieldPoints(Graphics2D g, Collection<AdvancedTarget> enemys)
	{
		// for (ForcePoint fix: allFixPoints)
		// {
		//
		// double angleHelp = RobotMath.calculateAngle(fix,new Point2D.Double(fix.x - fix.xforce, fix.y - fix.yforce));
		// Point2D forceHelperPoint = RobotMath.calculatePolarPoint(angleHelp, 15, fix);
		// //PaintHelper.drawLine(fixPoint, forceHelperPoint, g, Color.DARK_GRAY);
		// PaintHelper.drawLine(fix, forceHelperPoint, g, Color.MAGENTA);
		//
		// if (fix.isClosed) PaintHelper.drawPoint(fix, Color.RED, g, 4);
		// else PaintHelper.drawPoint(fix, Color.YELLOW, g, 8);
		// }
	}

	public static void drawFireMovement(Graphics2D g, HashMap<Bullet, Point2D> fireMoveMap, AdvancedTarget target)
	{
		for (Entry<Bullet, Point2D> entry : fireMoveMap.entrySet())
		{
			if (entry.getKey().isActive())
			{
				Point2D firePoint = entry.getValue();
				Point2D targetPos = new Point2D.Double(target.x, target.y);
				PaintHelper.drawArc(firePoint, firePoint.distance(targetPos), 0, PaintHelper.PI_CIRCLE, false, g, PaintHelper.whiteTrans);
				PaintHelper.drawPoint(firePoint, Color.PINK, g, 4);
			}
			// System.out.format("flying[] heading %3.2f bullet: %s\n",entry.getKey().getHeading(),entry.getKey().toString());
		}
	}

	// standalone function
	public static void drawTargetStats(Graphics2D g, ATarget target, AdvancedRobot robot, AFire myFire)
	{
		double bPower = myFire.getFirePower(target);
		long bTurns = (long) (target.getDistance() / Rules.getBulletSpeed(bPower));
		long maxBTurns = (long) (target.getDistance() / Rules.getBulletSpeed(Rules.MIN_BULLET_POWER));
		long minBTurns = (long) (target.getDistance() / Rules.getBulletSpeed(Rules.MAX_BULLET_POWER));

		// int showX = (int)(robot.getX() + target.x);
		// int showY = (int)(robot.getY() + target.y);
		int showX = 700;
		int showY = 700;
		int count = 0;

		g.setColor(Color.GRAY);
		g.setFont(PaintHelper.myFont);
		g.drawString(String.format("BP: %3.2f BT: %d max: %d min: %d ", bPower, bTurns, maxBTurns, minBTurns), showX, showY - count++);
		g.drawString(String.format("D: %3.2f V: %3.2f\n", target.getDistance(), target.getVelocity()), showX,
				showY - count++ * PaintHelper.myFont.getSize());
		g.drawString(String.format("N: %s\n", target.getName()), showX, showY - count++ * PaintHelper.myFont.getSize());
		g.drawString(String.format("V: %3.2f avg: %3.2f", target.getVelocity(), 0D), showX, showY - count++ * PaintHelper.myFont.getSize());
		// g.drawString(String.format("LC:%d LD:%d",target.stats.lastChange,target.stats.lastDir),showX,showY-count++*PaintHelper.myFont.getSize());
		// g.drawString(String.format("D: %d %d %d ",target.stats.dirs[0],target.stats.dirs[1],target.stats.dirs[2]),showX,showY-count++*PaintHelper.myFont.getSize());
		// g.drawString(String.format("C: %d %d %d ",target.stats.dirCount[0],target.stats.dirCount[1],target.stats.dirCount[2]),showX,showY-count++*PaintHelper.myFont.getSize());
		// g.drawString(String.format("A: %d %d %d ",target.stats.dirs[0]/target.stats.dirCount[0],target.stats.dirs[1]/target.stats.dirCount[1],target.stats.dirs[2]/target.stats.dirCount[2]),showX,showY-count++*PaintHelper.myFont.getSize());
	}

	public static void drawTargetPath(Graphics2D g, Point2D targetPos, AdvancedRobot myRobot)
	{
		g.setColor(Color.YELLOW);
		if (myRobot.getTime() % 5 == 0)
		{
			targetPath = PaintHelper.addPathSegment(targetPath, targetPos);
		}
		if (targetPath != null) g.draw(targetPath);
	}

	public static void drawWallStick(Graphics2D g, double heading, double stickLength, double rX, double rY)
	{
		Point2D roboPos = new Point2D.Double(rX, rY);
		Point2D stickPoint = RobotMath.calculatePolarPoint(Utils.normalRelativeAngle(heading), stickLength, roboPos);

		PaintHelper.drawLine(roboPos, stickPoint, g, Color.GREEN);
	}

	public static void drawBulletGeometry(Graphics2D g, HashMap<Bullet, Point2D> bulletMap)
	{
		for (Entry<Bullet, Point2D> entry : bulletMap.entrySet())
		{
			if (entry.getKey().isActive())
			{
				Point2D firePoint = entry.getValue();
				Point2D bulletPoint = new Point2D.Double(entry.getKey().getX(), entry.getKey().getY());
				PaintHelper.drawLine(bulletPoint, firePoint, g, Color.YELLOW);
				PaintHelper.drawPoint(firePoint, Color.PINK, g, 4);
			}
			// System.out.format("flying[] heading %3.2f bullet: %s\n",entry.getKey().getHeading(),entry.getKey().toString());
		}
	}

	private void drawTargetgeometry(Graphics2D g)
	{
		// if (mainTarget == null) return;
		//
		//
		// if (lastMainTarget != mainTarget)
		// {
		// if (!isFlying)
		// {
		// double angle = Utils.normalAbsoluteAngle(myRobot.getHeadingRadians() + mainTarget.getBearingRadians());
		// lastTargetPoint = RobotMath.calculatePolarPoint(angle, mainTarget.getDistance(), robotPos);
		// lastMainTarget = mainTarget;
		// //double power = 0.035 * myRobot.getEnergy() * (1.0-lastMainTarget.getDistance()/1400.0);
		// double power = Math.min(Math.random() * 3.0,0.1);
		// //predictedDist = lastMainTarget.getVelocity() * (RobotMath.calculateBulletFlightTime(lastMainTarget.getDistance(), power));
		// predictedDist = (Math.random() *Math.abs(lastMainTarget.getVelocity())) *
		// (RobotMath.calculateBulletFlightTime(lastMainTarget.getDistance(), power));
		//
		// bulletSpeed = RobotMath.calculateBulletSpeed(0.035 * myRobot.getEnergy() * (1.0-lastMainTarget.getDistance()/1400.0));
		// flyCount = 0;
		// lastRobotPoint = robotPos;
		// }
		// }
		//
		// drawArc(lastTargetPoint, predictedDist, 0, Math.PI*2.0, true, g, whiteTrans);
		//
		// // 1. polarangle from target->point heading depend on bulletpower
		// // 2. polarpoint from targetpoint -> depentds on bulletpower dist
		// // 3. line robot-> predicted head-on point
		// // 4 just the point
		// double targetHead = Utils.normalRelativeAngle(lastMainTarget.getHeadingRadians());
		// Point2D predictedHeadPoint = RobotMath.calculatePolarPoint(targetHead, predictedDist,lastTargetPoint);
		// drawLine(lastRobotPoint, predictedHeadPoint, g, whiteTrans);
		// drawPoint(predictedHeadPoint, Color.RED, g);
		//
		//
		// // 1. polarangle robot->predicted head point
		// // 2. ....
		// double angleRobotPredict = RobotMath.calculateAngle(lastRobotPoint, predictedHeadPoint);
		// Point2D virtualBulletPoint = RobotMath.calculatePolarPoint(angleRobotPredict, bulletSpeed*flyCount, lastRobotPoint);
		// drawPoint(virtualBulletPoint, Color.GREEN, g);
		// isFlying = (bulletSpeed*flyCount < lastRobotPoint.distance(predictedHeadPoint));
		// flyCount++;
		//
		// /// gun ---------------
		// // double gunHead = Utils.normalRelativeAngle(myRobot.getGunHeadingRadians());
		// // Point2D gunPoint = RobotMath.calculatePolarPoint(myRobot.getGunHeadingRadians(),lastMainTarget.getDistance(), robotPoint);
		// // drawArc(robotPoint, 200.0, 0, gunHead, true, g, greenTrans);
		//
		// // fluchtkreise
		// //1. distance den das target bis zum naechsten scan zuruecklegt
		// //2. der kreis dazu
		// double distance = Math.abs(lastMainTarget.getVelocity())*8.0;
		// drawArc(lastTargetPoint, distance, 0.0,Math.PI*2.0, false, g, whiteTrans);
		//
		// // der winkel bogen fuer das den targethead zum zeitpunkt des scanns
		// drawArc(lastTargetPoint, 200.0, 0.0,targetHead, true, g, greenTrans);
		//
		//
		// // die sehne die das heading des targets zum scan aufbaut - nur zum visualisieren
		// // 1. polarpunkt target (zum zeitpunkt scan) -> richtung die das target geschaut hat
		// // 2. polarpunkt auf der gegenuberliegenden seite
		// // 3-4. die beiden linien
		// Point2D tHeadPoint = RobotMath.calculatePolarPoint(targetHead, 1000.0, lastTargetPoint);
		// Point2D tCounterHeadPoint = RobotMath.calculatePolarPoint(targetHead + Math.PI, 1000.0, lastTargetPoint);
		// drawLine(lastTargetPoint, tHeadPoint, g, whiteTrans); // linie tarrget-headcircle
		// drawLine(lastTargetPoint, tCounterHeadPoint, g, whiteTrans); // linie taerget - counterheadcircle
		//
		//
		// // just geometry visibilitys
		// drawLine(lastRobotPoint, lastTargetPoint, g, whiteTrans); // linie robot->target
		// drawPoint(lastTargetPoint, Color.RED, g); // last targetpoint
		//
		// //drawLine(robotPoint, gunPoint, g, whiteTrans); // linie robot->gunheading
		// //drawPoint(gunPoint, Color.RED, g); // last targetpoint
		//
		//
		//
		//
		//
		// // drawArc(robotPoint, 300.0, 0.0,PI_CIRCLE, false, g, Color.YELLOW); // firecircle -> 300

	}

	public static void drawRobotPath(Graphics2D g, Point2D robotPos, AdvancedRobot myRobot)
	{
		g.setColor(Color.GREEN);
		if (myRobot.getTime() % 5 == 0)
		{
			robotPath = PaintHelper.addPathSegment(robotPath, robotPos);
		}
		if (robotPath != null) g.draw(robotPath);						// robot path line
	}

	public static void drawSimpleShapes(Graphics2D g)
	{
		g.setColor(PaintHelper.whiteTrans);
		g.draw(new RoundRectangle2D.Double(20, 20, 960, 960, 300, 300));
	}

	public static void drawFirePower(Graphics2D g, double bulletPower, AdvancedRobot robot)
	{
		double x = robot.getBattleFieldWidth() * 0.1;
		double y = robot.getBattleFieldHeight() * 0.025;

		g.setColor(Color.RED);
		double percent = bulletPower / 3.0;
		g.fill(new Rectangle2D.Double((robot.getBattleFieldWidth() - x) / 2.0, robot.getBattleFieldHeight() - 4 * y, x * percent, y));
		g.setColor(Color.WHITE);
		g.draw(new Rectangle2D.Double((robot.getBattleFieldWidth() - x) / 2.0, robot.getBattleFieldHeight() - 4 * y, x, y));
		PaintHelper.drawString(g, String.format("%1.2f", bulletPower), (robot.getBattleFieldWidth() / 2.0 + x / 2.0 + 4.0),
				robot.getBattleFieldHeight() - 4 * y, Color.WHITE);
	}

	public static void drawVelocity(Graphics2D g, double velocity)
	{
		double percent = velocity / Rules.MAX_VELOCITY;
		if (percent > 0) g.setColor(Color.RED);
		else
		{
			percent = -percent;
			g.setColor(Color.YELLOW);
		}
		g.fill(new Rectangle2D.Double(451, 950 - 26, 100 * percent, 20));
		g.setColor(Color.WHITE);
		g.draw(new Rectangle2D.Double(447, 948 - 26, 104, 25));
	}

	public static void drawGunHeat(Graphics2D g, double gunHeat, AdvancedRobot robot)
	{
		double percent = gunHeat / Rules.getGunHeat(Rules.MAX_BULLET_POWER);

		double x = robot.getBattleFieldWidth() * 0.1;
		double y = robot.getBattleFieldHeight() * 0.025;

		if (gunHeat >= 0.5) g.setColor(Color.RED);
		else g.setColor(Color.YELLOW);
		g.fill(new Rectangle2D.Double((robot.getBattleFieldWidth() - x) / 2.0, robot.getBattleFieldHeight() - 2 * y, x * percent, y));
		g.setColor(Color.WHITE);
		g.draw(new Rectangle2D.Double((robot.getBattleFieldWidth() - x) / 2.0, robot.getBattleFieldHeight() - 2 * y, x, y));

	}

	private void drawCircleGeometry(Graphics2D g)
	{
		// if (forwardPoint == null) return;
		// double aCR = Math.atan2(robotPos.getX()-centerPos.getX(), robotPos.getY()-centerPos.getY()); // polarwinkel center->robot
		// distPoint = RobotMath.calculatePolarPoint(aCR, maxRadius, centerPos);
		// //forwardPoint = RobotMath.calculatePolarPoint(aCR - Math.toRadians(10.0), maxRadius, centerPoint);
		// double aRF = Math.atan2(forwardPoint.getX()-robotPos.getX(),forwardPoint.getY()-robotPos.getY()); // polarwinkel robot->forward
		// //double aRF = RobotMath.calculateAngle(robotPoint, forwardPoint);
		//
		// double head = myRobot.getHeadingRadians();
		// if (myRobot.getDistanceRemaining() < 0) head = head + Math.PI;
		// head = Utils.normalRelativeAngle(head);
		//
		// double result = Utils.normalRelativeAngle(-head + aRF);
		//
		// // robot -> forward point rerlations
		// drawArc(robotPos, forwardPoint.distance(robotPos)*2.0, 0, head, true, g, greenTrans); // relativer winkel robot heading
		// drawArc(robotPos, forwardPoint.distance(robotPos)*2.0, 0, aRF, true, g, new Color(0x50, 0x50, 0x50, 0x50)); // polarwinkel winkel
		// robot->forward
		// drawArc(robotPos, forwardPoint.distance(robotPos)*1.8, aRF, -result, true, g, new Color(0x50, 0x00, 0x00, 0x50)); // difference winkel
		// robotHead->forward
		//
		// // just geometry visibilitys
		// drawPoint(forwardPoint, Color.PINK, g); // forwardpoint
		// drawPoint(distPoint, Color.RED, g); // distpoint
		// drawLine(distPoint, centerPos, g, whiteTrans); // linie distpoint-center
		// drawLine(forwardPoint, centerPos, g, whiteTrans); // linie forward-center
		// drawLine(centerPos, robotPos, g, whiteTrans); // linie center->robot
		// drawLine(robotPos, forwardPoint, g, whiteTrans); // linie robot->forwardPoint
	}

	public static void drawSavePoint(Graphics2D g, ArrayList<Point2D> savePoints, Point2D targetPos, Point2D robotPos, double maxRadius)
	{
		PaintHelper.drawPoint(targetPos, Color.WHITE, g, 2);

		for (Point2D savePos : savePoints)
		{
			PaintHelper.drawLine(targetPos, savePos, g, PaintHelper.whiteTrans);			// linie center->savepoint
			double aCS = Math.atan2(savePos.getX() - targetPos.getX(), savePos.getY() - targetPos.getY());   // polarwinkle cenetr->savepoint
			double aCR = Math.atan2(robotPos.getX() - targetPos.getX(), robotPos.getY() - targetPos.getY());  // polarwinkel center->robot
			PaintHelper.drawPoint(savePos, Color.RED, g, 10);
			PaintHelper.drawArc(targetPos, maxRadius, 0, aCS, true, g, new Color(0x50, 0x50, 0x50, 0x50));	 // difference winkel robotHead->forward
			PaintHelper.drawArc(targetPos, maxRadius, 0, aCR, true, g, new Color(0x50, 0x50, 0x50, 0x50));	 // difference winkel robotHead->forward
			PaintHelper.drawArc(targetPos, maxRadius, aCS, Utils.normalRelativeAngle(aCR - aCS), true, g, new Color(0x00, 0x50, 0x00, 0x50));	 // difference
																																				// winkel
																																				// robotHead->forward
		}
	}

	private void drawFieldSplit(Graphics2D g)
	{
		// Point2D start = new Point2D.Double(0.0, myRobot.getBattleFieldHeight()/2.0);
		// Point2D end = new Point2D.Double(myRobot.getBattleFieldWidth(), myRobot.getBattleFieldHeight()/2.0);
		// drawLine(start, end, g, Color.LIGHT_GRAY);
		//
		// start = new Point2D.Double(myRobot.getBattleFieldWidth()/2.0, 0.0);
		// end = new Point2D.Double(myRobot.getBattleFieldWidth()/2.0, myRobot.getBattleFieldHeight());
		// drawLine(start, end, g, Color.LIGHT_GRAY);
		//
	}

	public static void resetPainter()
	{
		robotPath = null;
		targetPath = null;
	}
}
