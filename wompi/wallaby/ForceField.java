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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import robocode.util.Utils;
import wompi.paint.PaintHelper;
import wompi.robomath.RobotMath;

public class ForceField
{
	public double					xForce;
	public double					yForce;

	public ArrayList<ForcePoint>	allForcePoints	= new ArrayList<ForcePoint>();

	public ForceField()
	{
		for (int y = 0; y <= 1000; y += 50)
		{
			for (int x = 0; x <= 1000; x += 50)
			{
				ForcePoint fp = new ForcePoint();
				fp.x = x;
				fp.y = y;
				allForcePoints.add(fp);
			}
		}
	}

	public void resetForceField()
	{
		xForce = 0;
		yForce = 0;
	}

	public void calculateNewForce(AdvancedTarget enemy, double rHeading, int others)
	{
		double tAbsBearing = enemy.bearing + rHeading;

		xForce = xForce * 0.9 - (((Math.sin(tAbsBearing) - Math.sin(rHeading)) / others) / enemy.distance);
		yForce = yForce * 0.9 - (((Math.cos(tAbsBearing) - Math.cos(rHeading)) / others) / enemy.distance);

		for (ForcePoint fp : allForcePoints)
		{
			double dist = fp.distance(enemy);
			double angle = RobotMath.calculateAngle(fp, enemy);
			fp.xforce = fp.xforce * 0.9 - Math.sin(angle) / dist;
			fp.yforce = fp.yforce * 0.9 - Math.cos(angle) / dist;
		}
	}

	public void calculateForceForAll(Collection<AdvancedTarget> enemys, Point2D robotPos, double rHeading)
	{
		xForce = 0;
		yForce = 0;
		double force;
		double angle;
		double dist;
		int WF = 1000;
		int CF = 100;

		for (AdvancedTarget enemy : enemys)
		{
			force = enemy.energy / Math.pow(enemy.distance, 2);
			if (enemy.energySwitch < 0.3) force = -force;
			angle = Math.atan2(robotPos.getY() - enemy.y, robotPos.getX() - enemy.x);
			xForce -= Math.sin(angle) * force;
			yForce -= Math.cos(angle) * force;
		}

		// gravity in front of our robot to pull him a little bit
		// xForce -= Math.sin(rHeading) * 1/3000;
		// yForce -= Math.cos(rHeading) * 1/3000;

		// dist = robotPos.distance(500,500);
		// force = CF/Math.pow(dist, 2);
		// angle = Math.atan2(robotPos.getY() - 500, robotPos.getX() - 500);
		// xForce -= Math.sin(angle) * force;
		// yForce -= Math.cos(angle) * force;

		xForce += WF / Math.pow(Math.hypot(1000 - robotPos.getX(), 0), 2);
		xForce -= WF / Math.pow(Math.hypot(0 - robotPos.getX(), 0), 2);
		yForce += WF / Math.pow(Math.hypot(0, 1000 - robotPos.getY()), 2);
		yForce -= WF / Math.pow(Math.hypot(0, 0 - robotPos.getY()), 2);

		for (ForcePoint fp : allForcePoints)
		{
			fp.xforce = 0;
			fp.yforce = 0;

			for (AdvancedTarget enemy : enemys)
			{
				dist = fp.distance(enemy);
				force = enemy.energy / Math.pow(dist, 2);
				angle = Math.atan2(fp.x - enemy.y, fp.y - enemy.x);
				fp.xforce -= Math.sin(angle) * force;
				fp.yforce -= Math.cos(angle) * force;
			}

			// gravity in front of our robot to pull him a little bit
			// xForce -= Math.sin(rHeading) * 1/3000;
			// yForce -= Math.cos(rHeading) * 1/3000;
			//
			// dist = fp.distance(501,501);
			// force = CF/Math.pow(dist, 2);
			// angle = Math.atan2(fp.x - 501, fp.y - 501);
			// fp.xforce -= Math.sin(angle) * force;
			// fp.yforce -= Math.cos(angle) * force;
			//

			fp.xforce += WF / Math.pow(Math.hypot(1000 - fp.x, 0), 2);
			fp.xforce -= WF / Math.pow(Math.hypot(0 - fp.x, 0), 2);
			fp.yforce += WF / Math.pow(Math.hypot(0, 1000 - fp.y), 2);
			fp.yforce -= WF / Math.pow(Math.hypot(0, 0 - fp.y), 2);
		}
	}

	public double getTurnAngle(double rx, double ry, double rHeading, double bw, double bh)
	{
		return _nanoTurnHelper(rx, ry, rHeading, bw, bh, xForce, yForce);
	}

	private double _nanoTurnHelper(double _x, double _y, double rHeading, double bw, double bh, double xf, double yf)
	{
		double G = 1.4;
		double x = G / _x;
		double y = G / _y;
		double xb = G / (bw - _x);
		double yb = G / (bh - _y);
		double turn = Math.atan2(xf + x - xb, yf + y - yb) - rHeading;
		double delta = (turn + (7 * Math.PI)) % (2 * Math.PI);   // this should probably solved with utils.normal..
		return delta - Math.PI;
	}

	private double _turnHelper(double _x, double _y, double rHeading, double bw, double bh, double xf, double yf)
	{
		double turn = RobotMath.calculateAngle(new Point2D.Double(_x, _y), new Point2D.Double(_x + xf, _y + yf));
		double delta = turn - rHeading;
		delta = Utils.normalRelativeAngle(delta);
		return delta;
	}

	public void onPaint(Graphics2D g, double rHeading, double rx, double ry)
	{
		for (ForcePoint fp : allForcePoints)
		{
			double angle = _nanoTurnHelper(fp.x, fp.y, rHeading, 1000, 1000, fp.xforce, fp.yforce);

			Point2D helper = RobotMath.calculatePolarPoint(angle, 20, fp);
			PaintHelper.drawLine(fp, helper, g, Color.MAGENTA);
			PaintHelper.drawPoint(fp, Color.YELLOW, g, 4);
		}

		Point2D roboPos = new Point2D.Double(rx, ry);
		double angle = _nanoTurnHelper(rx, ry, rHeading, 1000, 1000, xForce, yForce);
		Point2D helper = RobotMath.calculatePolarPoint(angle, 100, roboPos);
		PaintHelper.drawLine(roboPos, helper, g, Color.GREEN);
	}

}

class ForcePoint extends Point2D.Double
{
	double	magnitude;
	double	xforce;
	double	yforce;
	double	sumDistance;
	boolean	isClosed;
}
