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
package wompi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.robomath.RobotMath;
import wompi.wallaby.PaintHelper;

/**
 * Development Notes:
 * - try score handling and don't switch targets if the current target has some
 * nice score bonus if finished of. This should gain some extra points
 * once in a while and maybe an overall APS increase.
 * - find something that can handle enemy wall hits
 * - find smooth angle decision to take the right angle from the vector
 * collection. I guess it is not every time the best decision to take the
 * most used angle
 * 
 * @author rschott
 */
public class Wombat extends AdvancedRobot
{
	private static final double	WZ						= 20.0;
	private static final double	WZ_G					= 17.0;

	private final static double	DIST					= 185;
	private final static double	DIST_REMAIN				= 20;

	private final static double	TARGET_FORCE			= 65000;			// 100000
																			// low dmg high surv - 10000 high dmg low surv
	private final static double	TARGET_DISTANCE			= 600.0;

	private final static double	PI_360					= Math.PI * 2.0;
	private final static double	DELTA_RISK_ANGLE		= Math.PI / 32.0;
	private final static double	MAX_HEAD_DIFF			= 0.1745329252;	// 10
																			// degree

	public final static int		MAX_PATTERN_LENGTH		= 30;
	public final static double	MAX_RADAR_RATE			= 0.05;
	public final static double	MAX_MOVE_ESCAPE_RATE	= 9.0;
	public final static int		MAX_ENDGAME_OPPONENTS	= 2;
	public final static double	DEFAULT_RADAR_WIDTH		= 2.0;

	public static Rectangle2D	B_FIELD_GUN;
	public static Rectangle2D	B_FIELD_MOVE;

	static WombatTarget			myTarget;
	static double				bPower;

	@Override
	public void run()
	{

		setAllColors(Color.ORANGE);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		B_FIELD_GUN = new Rectangle2D.Double(WZ_G, WZ_G, getBattleFieldWidth() - 2 * WZ_G, getBattleFieldHeight() - 2 * WZ_G);
		B_FIELD_MOVE = new Rectangle2D.Double(WZ, WZ, getBattleFieldWidth() - 2 * WZ, getBattleFieldHeight() - 2 * WZ);

		if (myTarget != null)
		{
			myTarget.init();
		}

		while (true)
		{
			doFire();
			doGun();
			execute();
		}
	}

	@Override
	public void onScannedRobot(final ScannedRobotEvent e)
	{

		if (myTarget == null) myTarget = new WombatTarget();

		myTarget.addScan(e, getHeadingRadians(), getX(), getY());

		// int bTick = (int)(e.getDistance()/Rules.getBulletSpeed(0.1));
		myTarget.calculateDisplaceVectors(getTime());
		// myTarget.onPaint(getGraphics(),bTick,getTime(),e.getVelocity());
		// myTarget.onPrint(getTime(),bTick);

		setTurnRadarRightRadians(Double.POSITIVE_INFINITY
				* Math.signum(Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians()) + 0.0001)); // TODO:
		// tweak the radar
		doMove();
	}

	private void doFire()
	{
		if (getGunTurnRemaining() == 0 /* && (getTime()-target.eScan) == 0 */)
		{
			setFire(bPower);
		}
	}

	private void doMove()
	{
		double maxRate = Double.MAX_VALUE;
		double riskAngle;
		double mx;
		double my;
		double moveTurn;
		riskAngle = moveTurn = 0;

		while ((riskAngle += DELTA_RISK_ANGLE) <= PI_360)
		{
			if (B_FIELD_MOVE.contains(mx = DIST * Math.sin(riskAngle) + getX(), my = DIST * Math.cos(riskAngle) + getY()))
			{
				WombatScan lastScan = myTarget.getLastScan();
				double riskRate = TARGET_FORCE / Point2D.distanceSq(lastScan.x, lastScan.y, mx, my);

				double perpRate = Math.abs(Math.cos(Math.atan2(lastScan.x - mx, lastScan.y - my) - riskAngle));

				if (getOthers() <= 5 && perpRate < 0.5)
				{
					riskRate += (perpRate = (0.5 * Math.random()));
				}
				else
				{
					riskRate += perpRate;
				}

				if (riskRate < maxRate)
				{
					maxRate = riskRate;
					moveTurn = riskAngle;
				}

				// debugRiskPerp.registerRiskPoint(getTime(), mx, my, perpRate,
				// getX(), getY(), DIST);
			}
		}
		if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || maxRate > MAX_MOVE_ESCAPE_RATE)
		{
			setTurnRightRadians(Math.tan(moveTurn -= getHeadingRadians()));
			setAhead(DIST * Math.cos(moveTurn));
		}
	}

	private void doGun()
	{
		if (myTarget == null)
		{
			System.out.format("[%d] Sorry no Traget yet!\n", getTime());
			return;
		}

		WombatScan lastKeyScan = myTarget.getLastKeyScan();

		if (lastKeyScan == null)
		{
			System.out.format("[%d] Soory no keyScan available!\n", getTime());
			return;
		}

		double tDist = Point2D.distance(lastKeyScan.x, lastKeyScan.y, getX(), getY());
		bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(lastKeyScan.sEvent.getEnergy() / 3.0, 600 / tDist));

		double heading = lastKeyScan.sEvent.getHeadingRadians();

		double tDiff = getTime() - lastKeyScan.sEvent.getTime();      // / adjust
		// time to the ticks if the last scan was not at this turn

		// extract displace vector
		WombatVectorHolder holder = myTarget.mySnapShots.get(lastKeyScan.sKey);
		if (holder == null)
		{
			System.out.format("[%d] NO HOLDER for key =%d \n", getTime(), lastKeyScan.sKey);
			return;
		}

		int bTicks = 0;
		double xg = lastKeyScan.x;
		double yg = lastKeyScan.y;
		double vHead = 0;
		double vDist = 0;

		Graphics2D g = getGraphics();

		while (bTicks++ * Rules.getBulletSpeed(bPower) < Point2D.distance(getX(), getY(), xg, yg))
		{

			ArrayList<WombatDisplaceVector> myVectors = holder.myVectors.get(bTicks);
			if (myVectors == null)
			{
				System.out.format("[%d] NO VECTORS for bTicks =%d \n", getTime(), bTicks);
				return;
			}

			// right here you have to take some advanced filter to select the
			// right displace vector
			// maybe a fast pre selection within the holder class or some
			// additional informations like wall distance, distance or what not
			// i guess the mean average is not good at all ... every group of
			// displacement vectors has its own special additional reason. the stronger this
			// group selection the stronger the overall weapon rate. but be
			// careful not to granulate it to much because the learning factor will increas
			// to and
			// it can take longer to reach the hitrate.

			HashMap<Integer, Integer> mostUsedVectors = new HashMap<Integer, Integer>();

			int maxCount = -1;
			WombatDisplaceVector maxVector = null;
			for (WombatDisplaceVector vector : myVectors)
			{
				int dist = (int) Math.sqrt((vector.relAngle * vector.relAngle) + (vector.relDist * vector.relDist));
				Integer test = mostUsedVectors.get(new Integer(dist));
				if (test == null)
				{
					mostUsedVectors.put(dist, test = new Integer(0));
				}
				mostUsedVectors.put(dist, test = new Integer(test.intValue() + 1));

				// System.out.format("[%d] vAngle=%3.2f vDist=%3.2f\n",
				// getTime(),vector.relAngle,vector.relDist);
				// System.out.format("[%d] dist=%d count=%d\n",
				// getTime(),dist,test);

				if (test.intValue() > maxCount)
				{
					maxCount = test;
					maxVector = vector;
					// debug
					Point2D vP = RobotMath.calculatePolarPoint(Utils.normalRelativeAngle(lastKeyScan.sEvent.getHeadingRadians() + vector.relAngle),
							vector.relDist, lastKeyScan);
					PaintHelper.drawLine(lastKeyScan, vP, g, Color.GRAY);
				}

			}

			if (maxVector == null)
			{
				System.out.format("[%d] Sorry no maxVector found!\n", getTime());
				continue;
			}

			vHead = Utils.normalRelativeAngle(lastKeyScan.sEvent.getHeadingRadians() + maxVector.relAngle); // TODO: extract
			// from displace vectors
			vDist = maxVector.relDist; // TODO: extract from displace vectors

			xg = lastKeyScan.x + Math.sin(vHead) * vDist;
			yg = lastKeyScan.y + Math.cos(vHead) * vDist;

		}
		Point2D vP = RobotMath.calculatePolarPoint(vHead, vDist, lastKeyScan);
		PaintHelper.drawLine(lastKeyScan, vP, g, Color.RED);
		double gunAngle = Math.atan2(xg - getX(), yg - getY());

		setTurnGunRightRadians(Utils.normalRelativeAngle(gunAngle - getGunHeadingRadians()));
	}

	@Override
	public void onPaint(Graphics2D g)
	{}
}

class WombatTarget
{
	WombatScan[]							myScans;
	long									lastScan;
	long									lastKey;

	HashMap<Integer, WombatVectorHolder>	mySnapShots		= new HashMap<Integer, WombatVectorHolder>();

	private final static int				MAX_SHOOT_TICKS	= 60;
	private final static double				MAX_HEAD_DIFF	= 0.1745329252;								// 10
																											// degree

	public WombatTarget()
	{
		init();
	}

	public void init()
	{
		myScans = new WombatScan[3000];
	}

	public WombatScan getLastScan()
	{
		return myScans[(int) lastScan];
	}

	public WombatScan getLastKeyScan()
	{
		return myScans[(int) lastKey];
	}

	public void addScan(ScannedRobotEvent e, double botHeading, double x, double y)
	{
		WombatScan diffScan = myScans[(int) e.getTime() - 1];
		int key = -1;

		if (diffScan != null)
		{
			double headDiff = Utils.normalRelativeAngle(e.getHeadingRadians() - diffScan.sEvent.getHeadingRadians());
			// don't use Rules.MAX... because of the rounding errors
			// this should never happen with timediff 1 scans but i check it
			// anyway
			if (Math.abs(headDiff) <= MAX_HEAD_DIFF)
			{
				key = (17 * ((int) Math.rint(Math.toDegrees(headDiff)) + 10) + ((int) Math.rint(e.getVelocity()) + 8));
			}
			else
			{
				System.out.format("[%d] ERROR: heading difference is out of value\n", e.getTime());
			}

		}

		WombatScan scan = new WombatScan();
		scan.x = x + Math.sin(botHeading + e.getBearingRadians()) * e.getDistance();
		scan.y = y + Math.cos(botHeading + e.getBearingRadians()) * e.getDistance();
		scan.sEvent = e;
		scan.sKey = key;
		myScans[(int) e.getTime()] = scan;
		lastScan = e.getTime();
		if (key != -1) lastKey = e.getTime();
	}

	public void calculateDisplaceVectors(long time)
	{

		// TODO: re think about the key usage. It might be wrong to save the key
		// an the scan and use it
		int shootTick = 0;
		int maxTicks = Math.min((int) time, MAX_SHOOT_TICKS);
		WombatScan end = myScans[(int) time];
		while (++shootTick <= maxTicks)
		{
			WombatScan start = myScans[(int) time - shootTick];
			if (start != null && start.sKey != -1)
			{
				WombatDisplaceVector vector = new WombatDisplaceVector();
				double absAngle = Math.atan2(end.x - start.x, end.y - start.y);
				vector.relAngle = Utils.normalRelativeAngle(absAngle - start.sEvent.getHeadingRadians());
				vector.relDist = start.distance(end);
				WombatVectorHolder holder = mySnapShots.get(start.sKey);
				if (holder == null)
				{
					mySnapShots.put(start.sKey, holder = new WombatVectorHolder());
				}
				holder.addVector(shootTick, vector);
				// System.out.format("[%d] new vector for key=%d and tick=%d   vAngle=%3.2f vDist=%3.2f \n",
				// time,start.sKey,shootTick,vector.relAngle,vector.relDist);
			}

			// else
			// {
			// System.out.format("[%d] no scan for time %d\n", time,maxTicks-shootTick);
			// }
		}
	}

	public void onPaint(Graphics2D g, double bTicks, long time, double velocity)
	{
		int key = myScans[(int) time].sKey;
		if (key == -1) return; // TODO: maybe wrong
		WombatVectorHolder holder = mySnapShots.get(key);
		if (holder == null)
		{
			System.out.format("NO HOLDER for key =%d \n", key);
			return;
		}

		ArrayList<WombatDisplaceVector> myVectors = holder.myVectors.get((int) bTicks);

		if (myVectors == null)
		{
			System.out.format("NO VECTORS for bTicks =%3.2f \n", bTicks);
			return;
		}

		WombatScan lastScan = myScans[(int) time];

		for (WombatDisplaceVector vector : myVectors)
		{
			Point2D vP = RobotMath.calculatePolarPoint(Utils.normalRelativeAngle(lastScan.sEvent.getHeadingRadians() + vector.relAngle),
					vector.relDist, lastScan);
			PaintHelper.drawLine(lastScan, vP, g, Color.GRAY);
		}
	}

	public void onPrint(long time, int bTicks)
	{
		// for(Entry<Integer,WombatVectorHolder> entry: mySnapShots.entrySet())
		// {
		// System.out.format("[%d][key=%d][btick=%d] KEY: %d VALUE: %s\n",time,key,bTicks,entry.getKey(),entry.getValue().toString());
		// }

		// int key = myScans[(int)time].sKey;
		// if (key == -1) return; // TODO: maybe wrong
		//
		// WombatVectorHolder holder = mySnapShots.get(key);
		// if (holder == null) return;
		// System.out.format("[%d][key=%d][btick=%d] VALUE: %s\n",time,key,bTicks,holder.toString());
		//
		// ArrayList<WombatDisplaceVector> tickList = holder.myVectors.get(bTicks);
		// if (tickList == null) return;
		// for (WombatDisplaceVector vector: tickList)
		// {
		// System.out.format("[%d] angle=%3.4f dist=%3.2f \n",
		// bTicks,Math.toDegrees(vector.relAngle),vector.relDist);
		// }
	}
}

class WombatScan extends Point2D.Double
{
	private static final long	serialVersionUID	= -4054906635108284565L;
	ScannedRobotEvent			sEvent;
	int							sKey;
}

class WombatDisplaceVector
{
	double	relAngle;
	double	relDist;

	// TODO: enhance this with additional informations like wall distance,
	// shoot, latVel and so on
}

class WombatVectorHolder
{
	// key = tick value = list of displace vectors for this count of ticks
	HashMap<Integer, ArrayList<WombatDisplaceVector>>	myVectors	= new HashMap<Integer, ArrayList<WombatDisplaceVector>>();

	public void addVector(int ticks, WombatDisplaceVector vector)
	{
		ArrayList<WombatDisplaceVector> list = myVectors.get(ticks);
		if (list == null)
		{
			myVectors.put(ticks, list = new ArrayList<WombatDisplaceVector>());
		}
		list.add(vector);
		// System.out.format("\tvector for tick %d size now %d \n",
		// ticks,list.size());
	}

	@Override
	public String toString()
	{
		String result = "";
		for (Entry<Integer, ArrayList<WombatDisplaceVector>> entry : myVectors.entrySet())
		{
			result = String.format("%s [%d:%d] ", result, entry.getKey(), entry.getValue().size());
		}
		return result;
	}
}

class WombatNewVectorHolder
{
	HashMap<Integer, HashMap<Integer, Integer>>	myNewVectors	= new HashMap<Integer, HashMap<Integer, Integer>>();

	public void addNewVector(int ticks, Integer vectorKey)
	{
		HashMap<Integer, Integer> map = myNewVectors.get(ticks);
		if (map == null)
		{
			myNewVectors.put(ticks, map = new HashMap<Integer, Integer>());
		}

		Integer count = map.get(vectorKey);
		if (count == null)
		{
			map.put(vectorKey, count = 0);
		}
		map.put(vectorKey, count++);
	}

	@Override
	public String toString()
	{
		String result = "";
		for (Entry<Integer, HashMap<Integer, Integer>> entry : myNewVectors.entrySet())
		{
			for (Entry<Integer, Integer> countEntry : entry.getValue().entrySet())
			{
				result = String.format("%s [tick=%d]{key=%d,count=%d} ", result, entry.getKey(), countEntry.getKey(), countEntry.getValue());
			}
		}
		return result;
	}
}
