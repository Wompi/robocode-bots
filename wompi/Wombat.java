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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import wompi.paint.PaintHelper;
import wompi.teststuff.NatSim;

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
	private static final double				FIELD_W					= 1000.0;
	private static final double				FIELD_H					= 1000.0;

	private static final double				WZ						= 20.0;
	private static final double				WZ_W					= FIELD_W - 2 * WZ;
	private static final double				WZ_H					= FIELD_H - 2 * WZ;
	private static final double				WZ_G					= 17.0;
	private static final double				WZ_G_W					= FIELD_W - 2 * WZ_G;
	private static final double				WZ_G_H					= FIELD_H - 2 * WZ_G;

	private final static double				DIST					= 185;
	private final static double				DIST_REMAIN				= 20;

	private final static double				TARGET_FORCE			= 405000;								// 100000
																											// low dmg high surv - 10000 high dmg low surv
	private final static double				TARGET_DISTANCE			= 400.0;

	private final static double				PI_360					= Math.PI * 2.0;
	private final static double				DELTA_RISK_ANGLE		= Math.PI / 32.0;
	private final static double				MAX_HEAD_DIFF			= 0.1745329252;						// 10
																											// degree

	public final static int					MAX_PATTERN_LENGTH		= 30;
	public final static double				MAX_RADAR_RATE			= 0.05;
	public final static double				MAX_MOVE_ESCAPE_RATE	= 9.0;
	public final static int					MAX_ENDGAME_OPPONENTS	= 2;
	public final static double				DEFAULT_RADAR_WIDTH		= 2.0;
	private static final double				DEAULT_HALF_BOTWEIGHT	= 18;

	public static Rectangle2D				B_FIELD_GUN;
	public static Rectangle2D				B_FIELD_MOVE;

	static HashMap<String, WombatTarget>	allTargets				= new HashMap<String, WombatTarget>();
	static double							bPower;
	static boolean							canShoot;

	static double							eRate;
	static String							eName;

	// debug
	List<NatSim.PredictionStatus>			list;

	@Override
	public void run()
	{

		setAllColors(Color.ORANGE);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		for (WombatTarget target : allTargets.values())
		{
			target.init();
		}

		eRate = Double.POSITIVE_INFINITY;
		B_FIELD_GUN = new Rectangle2D.Double(WZ_G, WZ_G, getBattleFieldWidth() - 2 * WZ_G, getBattleFieldHeight() - 2 * WZ_G);
		B_FIELD_MOVE = new Rectangle2D.Double(WZ, WZ, getBattleFieldWidth() - 2 * WZ, getBattleFieldHeight() - 2 * WZ);

		while (true)
		{
			WombatTarget target = allTargets.get(eName);
			if (target != null)
			{
				doFire();
				canShoot = doGun(target);
			}
			execute();
		}

	}

	@Override
	public void onScannedRobot(final ScannedRobotEvent e)
	{
		WombatTarget enemy;
		if ((enemy = allTargets.get(e.getName())) == null)
		{
			allTargets.put(e.getName(), enemy = new WombatTarget());
		}
		enemy.isAlive = true;
		enemy.addScan(e, getHeadingRadians(), getX(), getY());
		enemy.calculateDisplaceVectors();

		if (eRate > e.getDistance() || eName == e.getName())
		{
			eRate = e.getDistance();
			eName = e.getName();

			if (getGunHeat() < 1.0 || getOthers() == 1)
			{
				double x;
				if (!Utils.isNear(
						0.0,
						x = Double.POSITIVE_INFINITY
								* Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians()))) setTurnRadarRightRadians(x);
			}

			doMove(enemy);
		}
	}

	private void doFire()
	{
		if (getGunTurnRemaining() == 0 && canShoot)
		{
			setFire(bPower);
		}
	}

	private void doMove(WombatTarget target)
	{

		double v0;
		double v1;
		double r1;
		double rM;
		double x;
		double y;

		WombatScan lastScan = target.getLastScan();

		rM = Double.MAX_VALUE;
		v1 = v0 = 0;
		double rDist = DIST;
		boolean isClose = false;
		double buffyDist = Math.max(100, (lastScan.distance(getX(), getY()) - 50) * 8.0 / Rules.getBulletSpeed(bPower));

		while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
		{
			x = buffyDist * Math.sin(v0);
			y = buffyDist * Math.cos(v0);

			if (B_FIELD_MOVE.contains(x + getX(), y + getY()))
			{
				r1 = Math.abs(Math.cos(Math.atan2(lastScan.x - getX() - x, lastScan.y - getY() - y) - v0));
				try
				{
					Iterator<WombatTarget> iter = allTargets.values().iterator();
					while (true)
					{
						WombatTarget coordinate = iter.next();
						if (coordinate.isAlive)
						{
							WombatScan s = coordinate.getLastScan();
							r1 += TARGET_FORCE / Point2D.distanceSq(s.x, s.y, x + getX(), y + getY());
							isClose |= s.distance(getX(), getY()) < rDist;
						}
					}
				}
				catch (Exception e1)
				{}

				if (Math.random() < 0.7 && r1 < rM)
				{
					rM = r1;
					v1 = v0;
				}
			}
		}
		if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || isClose)
		{
			setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
			setAhead(buffyDist * Math.cos(v1));

			list = NatSim.predict(new NatSim.PredictionStatus(getX(), getY(), getHeadingRadians(), getVelocity(), getTime()), getHeadingRadians()
					+ getTurnRemainingRadians(), 8.0, getDistanceRemaining());

		}
	}

	private boolean doGun(WombatTarget target)
	{
		WombatScan lastKeyScan;
		try
		{
			lastKeyScan = target.getLastKeyScan();

		}
		catch (Exception e)
		{
			return false;
		}

		WombatVectorHolder holder = target.mySnapShots.get(lastKeyScan.sKey);
		if (holder == null)
		{
			//System.out.format("[%d] NO HOLDER for key =%d \n", getTime(), lastKeyScan.sKey);
			return false;
		}

		double tDist = Point2D.distance(lastKeyScan.x, lastKeyScan.y, getX(), getY());
		bPower = Math.min(Rules.MAX_BULLET_POWER, Math.min(lastKeyScan.sEnergy / 3.0, TARGET_DISTANCE / tDist));

		double bTick = tDist / Rules.getBulletSpeed(bPower);
		double botSpeed = 8.0 * bTick;

		int minTick = (int) Math.round((tDist - botSpeed) / Rules.getBulletSpeed(bPower));
		int maxTick = (int) Math.round((tDist + botSpeed) / Rules.getBulletSpeed(bPower));

		double tDiff = getTime() - lastKeyScan.sTime; // / adjust time to the ticks if the last scan was not at this turn

		double xg = lastKeyScan.x;
		double yg = lastKeyScan.y;
		double vHead = 0;
		double vDist = 0;

		Graphics2D g = getGraphics(); // this is to prevent 1000 getXXX() calls
		int[][] mostUsed = new int[1000][1000];
		int maxCount = -1;
		int dx = 0;
		int dy = 0;

		double myX = getX();
		double myY = getY();

		int tickTaken = 0;

		for (int i = minTick; i <= maxTick; i++)
		{
			ArrayList<WombatDisplaceVector> myVectors = holder.myVectors.get(i);
			if (myVectors == null)
			{
				//System.out.format("[%d] NO VECTORS for bTicks =%d\n", getTime(), i);
				continue;
			}
			//System.out.format("[%d] key:%d --  %d - size: %d\n", getTime(), lastKeyScan.sKey, i, myVectors.size());

			// TODO: this should be grabbed straight from the holder
			for (WombatDisplaceVector vector : myVectors)
			{
				vHead = Utils.normalRelativeAngle(lastKeyScan.sHeading + vector.relAngle);
				vDist = vector.relDist;

				xg = Math.sin(vHead) * vDist;
				yg = Math.cos(vHead) * vDist;

				double bDist = Point2D.distance(myX, myY, xg + lastKeyScan.x, yg + lastKeyScan.y);
				int checkTick = (int) (bDist / Rules.getBulletSpeed(bPower));
				if (checkTick != i) continue;

				if (B_FIELD_GUN.contains(xg + lastKeyScan.x, yg + lastKeyScan.y))
				{
					//					int xIndex = (int) Math.round(xg / 36.0);
					//					int yIndex = (int) Math.round(yg / 36.0);

					int xIndex = (int) (Math.toDegrees(Utils.normalAbsoluteAngle(vHead)) / 720.0);
					int yIndex = (int) (vDist / 2.0);
					int count = mostUsed[xIndex][yIndex]++;
					PaintHelper.drawLine(lastKeyScan, new Point2D.Double(lastKeyScan.x + xg, lastKeyScan.y + yg), g, Color.GRAY);

					if (count > maxCount)
					{
						maxCount = count;
						dx = (int) xg;
						dy = (int) yg;
						tickTaken = i;
					}
					vector.takenCount++;
				}
			}
			//		System.out.format("[%d] maxVector: %d (%3.5f,%3.2f)\n", getTime(), maxCount, Math.toDegrees(maxVector.relAngle), maxVector.relDist);
		}

		double gx = lastKeyScan.x + dx;
		double gy = lastKeyScan.y + dy;

		//		System.out.format("[%d] tick:%d min:%d (%3.2f) max:%d (%3.2f) bPower=%3.2f taken=%d\n", getTime(), (int) Math.round(bTick), minTick, tDist
		//				- botSpeed, maxTick, tDist + botSpeed, bPower, tickTaken);

		if (tickTaken == 0) { return false; }

		PaintHelper.drawLine(lastKeyScan, new Point2D.Double(gx, gy), getGraphics(), Color.RED);
		double gunAngle = Math.atan2(gx - getX(), gy - getY());
		setTurnGunRightRadians(Utils.normalRelativeAngle(gunAngle - getGunHeadingRadians()));
		return true;
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		WombatTarget target = allTargets.get(e.getName());
		if (target != null)
		{
			if (eName == e.getName())
			{
				eName = null;
				eRate = Double.POSITIVE_INFINITY;
			}
			target.isAlive = false;
		}

	}

	@Override
	public void onPaint(Graphics2D g)
	{
		if (list != null)
		{
			for (NatSim.PredictionStatus status : list)
			{
				PaintHelper.drawPoint(status, Color.CYAN, g, 4);
			}
		}

	}
}

class WombatTarget
{
	final static int						DELTA_HEADING_INDEX	= 20;
	final static int						VELOCITY_INDEX		= 8;
	final static double						HEAD_FACTOR			= 2.0;
	final static double						VELO_FACTOR			= 1.0;

	LinkedList<WombatScan>					allScans;
	int										lastKeyScanIndex;

	HashMap<Integer, WombatVectorHolder>	mySnapShots			= new HashMap<Integer, WombatVectorHolder>();

	private final static int				MAX_SHOOT_TICKS		= 75;
	private final static double				MAX_HEAD_DIFF		= 0.1745329252;								// 10
																												// degree
	public boolean							isAlive;

	public WombatTarget()
	{
		init();
	}

	public void init()
	{
		allScans = new LinkedList<WombatScan>();
		lastKeyScanIndex = 0;
	}

	public WombatScan getLastScan()
	{
		return allScans.getLast();
	}

	public WombatScan getLastKeyScan()
	{
		return allScans.get(lastKeyScanIndex);
	}

	double	lastVelocity;

	public void addScan(ScannedRobotEvent e, double botHeading, double x, double y)
	{
		int key = -1;
		double headDiff = 0;
		try
		{
			WombatScan lastScan = getLastScan();
			long scanDiff = e.getTime() - lastScan.sTime;

			if (scanDiff == 1)
			{
				headDiff = Utils.normalRelativeAngle(e.getHeadingRadians() - lastScan.sHeading);
				int headInt = (int) Math.rint(Math.toDegrees(headDiff * HEAD_FACTOR)) + DELTA_HEADING_INDEX;
				int veoInt = (int) (Math.rint(e.getVelocity() * VELO_FACTOR)) + VELOCITY_INDEX;
				key = (((headInt) << 8) + (veoInt));
			}
		}
		catch (NoSuchElementException e0)
		{}

		double maxDiff = Rules.getTurnRateRadians(lastVelocity);
		lastVelocity = e.getVelocity();
		System.out.format("[%d] v=%3.2f dH=%3.5f ratioH=%3.4f\n", e.getTime(), e.getVelocity(), Math.toDegrees(headDiff), headDiff / maxDiff);
		WombatScan scan = new WombatScan();
		scan.x = x + Math.sin(botHeading + e.getBearingRadians()) * e.getDistance();
		scan.y = y + Math.cos(botHeading + e.getBearingRadians()) * e.getDistance();
		scan.sHeading = e.getHeadingRadians();
		scan.sKey = key;
		scan.sTime = e.getTime();
		scan.sEnergy = e.getEnergy();

		allScans.add(scan);
		if (key != -1) lastKeyScanIndex = allScans.size() - 1;
	}

	public void calculateDisplaceVectors()
	{
		WombatScan lastScan = getLastScan();
		Iterator<WombatScan> iter = allScans.descendingIterator();
		int shootTick = 0;
		while (iter.hasNext())
		{
			WombatScan start = iter.next();
			if (start != lastScan) // do not use the lastScan to displace with himself
			{
				long scanDiff = lastScan.sTime - start.sTime;
				if (scanDiff <= MAX_SHOOT_TICKS)
				{
					// TODO: maybe take other states as well into account if the key is not valid 
					if (start.sKey != -1)
					{
						// TODO: categorize the displace vectors with a key to bind equal vectors together
						WombatDisplaceVector vector = new WombatDisplaceVector();
						double absAngle = Math.atan2(lastScan.x - start.x, lastScan.y - start.y);
						vector.relAngle = Utils.normalRelativeAngle(absAngle - start.sHeading);
						vector.relDist = start.distance(lastScan);
						WombatVectorHolder holder = mySnapShots.get(start.sKey);
						if (holder == null)
						{
							mySnapShots.put(start.sKey, holder = new WombatVectorHolder());
						}
						holder.addVector(shootTick, vector);
					}
				}
				else
				{
					iter.remove(); // TODO: re think about this, maybe the break is not right there
					lastKeyScanIndex--;
					break;
				}
			}
			shootTick++;
		}
	}

	public void onPaintOld(Graphics2D g, double bTicks, long time, double velocity)
	{}

	public void onPrint(long time, int bTicks)
	{}
}

class WombatScan extends Point2D.Double
{
	private static final long	serialVersionUID	= -4054906635108284565L;
	int							sKey				= -1;
	double						sHeading;
	long						sTime;
	double						sEnergy;
}

class WombatDisplaceVector
{
	// TODO: enhance this with additional informations like wall distance,
	// shoot, latVel and so on
	double	relAngle;
	double	relDist;
	long	takenCount;

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

		if (list.size() > 500)
		{
			Iterator<WombatDisplaceVector> iter = list.iterator();
			while (iter.hasNext())
			{
				WombatDisplaceVector v = iter.next();
				if (v.takenCount < 5)
				{
					iter.remove();
				}
			}
		}
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
