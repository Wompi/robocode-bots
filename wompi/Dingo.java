package wompi;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.mics.TimeProfile;
import wompi.radar.ARadar;
import wompi.radar.IRadar;
import wompi.radar.RadarNone;
import wompi.radar.RadarStartSearch;
import wompi.robotcontrol.IRobotTurn;
import wompi.target.ATargetHandler;
import wompi.target.ITargetHandler;
import wompi.target.SimpleTargetHandler;

public class Dingo extends AdvancedRobot implements IRadar, IRobotTurn, ITargetHandler
{
	private ARadar					myRadar;
	private static ATargetHandler	myTargetHandler;

	// TODO: this should be a list or map where every device can register its amount 
	// then the robot can decide which one it will take
	// Lets say the radar wants to turn the body but the body wants something other so decide wich one is
	// the best choice for now
	private double					myRadarTurnAmount;
	private double					myGunTurnAmount;
	private double					myBodyTurnAmount;

	public Dingo()
	{
		//myRadar = new RadarStartSearch(new RadarSingleLock(new RadarNone()));
		myRadar = new RadarStartSearch(new RadarNone());
		System.out.format("Start ...\n");

		if (myTargetHandler == null)
		{
			myTargetHandler = new SimpleTargetHandler();
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{

		myRadar.onStatus(e);
		myTargetHandler.onStatus(e);
	}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		TimeProfile.initRound();
		myTargetHandler.onInit(this);
		myRadar.onInit(this);

		while (true)
		{
			setTurnRadarRightRadians(myRadarTurnAmount);
			setTurnGunRightRadians(myGunTurnAmount);
			setTurnRightRadians(myBodyTurnAmount);
			TimeProfile.setRoundProperties(this);
			execute();
		}
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		myRadar.onHitRobot(e);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRadar.onScannedRobot(e);
		myTargetHandler.onScannedRobot(e);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		myTargetHandler.onPaint(g);
	}

	// Interface functions for all devices t register there values and states

	@Override
	public void setRadarTurnAmount(double turnAmount, String turnSource)
	{
		myRadarTurnAmount = turnAmount;
	}

	@Override
	public void setGunTurnAmount(double turnAmount, String turnSource)
	{
		myGunTurnAmount = turnAmount;
	}

	@Override
	public void setBodyTurnAmount(double turnAmount, String turnSource)
	{
		myBodyTurnAmount = turnAmount;
	}

	@Override
	public void setRadar(ARadar newRadar)
	{
		if (newRadar != myRadar)
		{
			newRadar.onInit(this);
		}

		myRadar = newRadar;
	}

	@Override
	public ATargetHandler getTargetHandler()
	{
		return myTargetHandler;
	}

	@Override
	public void setTargetHandler(ATargetHandler tHandler)
	{
		myTargetHandler = tHandler;
	}
}
