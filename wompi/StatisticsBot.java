package wompi;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import wompi.robomath.RobotMath;

// just extend from the BOT you want to have some statistics
public class StatisticsBot extends Kowari
{

	private RobotStatus								myStatus;
	private RobotStatus								myScanStatus;
	private RobotStatus								myLastStatus;
	private ScannedRobotEvent						myRobotEvent;
	private static PrintStream						w;

	private static final HashMap<EStats, StatsItem>	myStatsMap	= new HashMap<EStats, StatsItem>();
	private static final ArrayList<StatsItem>		myItems		= new ArrayList<StatsItem>();

	private long									lastHisHitTime;
	private long									lastMyHitTime;
	private long									lastMyMissTime;

	//@formatter:off
	private enum EStats 
	{
		RoundNumber,
		Time,
		MyVelocity,
		MyEnergy,
		MyGunHeat,
		HitByBulletPower,
		HitByBulletBearing,
		HisHitInterval,
		BulletHitPower,
		MyHitInterval,
		MyMissInterval,
		BulletHitBullet,
		Death,
		Win,
		CrashRobot,
		HisEnergy,
		HisDistance,
		HisVelocity,
		HisBearing,
		HisAbsbearing,
		HisLatVal,
		HisadvanceVal,
	}
	//@formatter:on

	public void onInit()
	{
		try
		{
			lastHisHitTime = 30;
			lastMyHitTime = 30;

			if (w != null) return;

			File dataDir = getDataDirectory();
			long dataQuota = getDataQuotaAvailable();

			Date now = new Date();
			String dataName = String.format("%s_%s.csv", StatisticsBot.class.getSuperclass().getName(), now.toString());
			File dataFile = getDataFile(dataName);

			System.out.format("OnInit: dataQuota=%3d\n", dataQuota);
			System.out.format("OnInit: dataName=%s\n", dataName);
			System.out.format("OnInit: dataDir=%s\n", dataDir.getAbsolutePath());
			w = new PrintStream(new RobocodeFileOutputStream(dataFile));

			String header = "";
			for (EStats entry : EStats.values())
			{
				StatsItem item = new StatsItem(entry.toString());
				myItems.add(item);
				myStatsMap.put(entry, item);
				header = header + "," + item.getName();
			}
			writeData(header);
		}
		catch (IOException e)
		{
			System.out.println("IOException trying to write: " + e);
		}
		catch (Exception e1)
		{
			System.out.format("Exception: %s\n", e1.getMessage());
		}
	}

	@Override
	public void run()
	{
		onInit();
		super.run(); // TODO: beware of endless loops within the extended BOT
		while (true)
		{
			myStatsMap.get(EStats.MyGunHeat).registerValue(myStatus.getGunHeat());

			String line = "";
			for (StatsItem entry : myItems)
			{
				line = line + "," + entry.getValue();
			}
			writeData(line);
			execute();
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myLastStatus = myStatus;
		myStatus = e.getStatus();
		if (w != null)
		{
			for (StatsItem entry : myStatsMap.values())
			{
				entry.clear();
			}

			myStatsMap.get(EStats.RoundNumber).registerValue(myStatus.getRoundNum());
			myStatsMap.get(EStats.Time).registerValue(myStatus.getTime());
			myStatsMap.get(EStats.MyVelocity).registerValue(myStatus.getVelocity());
			myStatsMap.get(EStats.MyEnergy).registerValue(myStatus.getEnergy());
		}
		super.onStatus(e);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotEvent = e;
		myScanStatus = myStatus;
		myStatsMap.get(EStats.HisEnergy).registerValue(e.getEnergy());
		myStatsMap.get(EStats.HisDistance).registerValue(e.getDistance());
		myStatsMap.get(EStats.HisVelocity).registerValue(e.getVelocity());
		myStatsMap.get(EStats.HisBearing).registerValue(e.getBearing());
		myStatsMap.get(EStats.HisAbsbearing).registerValue(e.getBearing() + myStatus.getHeading());
		myStatsMap.get(EStats.HisLatVal).registerValue(
				RobotMath.calculateLateralVelocity(myStatus.getHeadingRadians(), e.getBearingRadians(),
						e.getHeadingRadians(), e.getVelocity()));
		myStatsMap.get(EStats.HisadvanceVal).registerValue(
				RobotMath.calculateAdvancingVelocity(myStatus.getHeadingRadians(), e.getBearingRadians(),
						e.getHeadingRadians(), e.getVelocity()));
		super.onScannedRobot(e);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent e)
	{
		if (e != null)
		{
			myStatsMap.get(EStats.MyMissInterval).registerValue(e.getTime() - lastMyMissTime);
			lastMyMissTime = e.getTime();
		}
		super.onBulletMissed(e);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e)
	{
		if (e != null) myStatsMap.get(EStats.BulletHitBullet).registerValue(1);
		super.onBulletHitBullet(e);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		if (e != null)
		{
			myStatsMap.get(EStats.HitByBulletPower).registerValue(e.getPower());
			myStatsMap.get(EStats.HitByBulletBearing).registerValue(e.getBearing());
			myStatsMap.get(EStats.HisHitInterval).registerValue(e.getTime() - lastHisHitTime);
			lastHisHitTime = e.getTime();
		}
		super.onHitByBullet(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		if (e != null)
		{
			myStatsMap.get(EStats.BulletHitPower).registerValue(e.getBullet().getPower());
			myStatsMap.get(EStats.MyHitInterval).registerValue(e.getTime() - lastMyHitTime);
			lastMyHitTime = e.getTime();
		}
		super.onBulletHit(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		if (e != null) myStatsMap.get(EStats.CrashRobot).registerValue(1);
		super.onHitRobot(e);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		super.onPaint(g);
	}

	@Override
	public void onDeath(DeathEvent e)
	{
		if (e != null) myStatsMap.get(EStats.Death).registerValue(1);
		super.onDeath(e);
	}

	@Override
	public void onWin(WinEvent e)
	{
		if (e != null) myStatsMap.get(EStats.Win).registerValue(1);
		super.onWin(e);
	}

	private void writeData(String line)
	{
		w.println(line);

		if (w.checkError()) System.out.println("I could not write the stats!");
	}

}

interface IStats
{
	public void registerValue(long value);

	public void registerValue(double value);

	public String getName();

	public String getValue();

	public void clear();
}

class StatsItem implements IStats
{
	String	myName;
	String	myValue;

	public StatsItem(String name)
	{
		myName = name;
		clear();
	}

	@Override
	public void registerValue(long value)
	{
		myValue = String.format("%d", value);
	}

	@Override
	public void registerValue(double value)
	{
		myValue = String.format("%3.5f", value);
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public String getValue()
	{
		return myValue;
	}

	@Override
	public void clear()
	{
		myValue = "0";
	}
}
