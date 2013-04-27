package wompi;

import java.awt.Color;
import java.awt.Graphics2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import wompi.echidna.misc.painter.PaintDiagramm;
import wompi.echidna.misc.painter.PaintSegmentDiagramm;

public class Kowari extends AdvancedRobot
{
	private static final double	ADVANCE_FACTOR	= 1.0 / 2000.0;					// 2500 = 16deg 2000 = 20deg
	private static final double	DISTANCE_FACTOR	= 176;								// 3.0 and 16 tick cooldown = 11*16= 176 = only one bullet in the air
	private static final double	HIT_FACTOR		= Math.PI / 8.0;
	private static final double	BPOWER			= 2.3333;
	private static final double	BSPEED			= Rules.getBulletSpeed(BPOWER);
	private static final double	GUN_TURNS		= Rules.getGunHeat(BPOWER) * 10;

	private static double		eEnergy;
	private static int			dir;
	private static double		dirChange;
	private static long			lastHit;
	private static double		dFactor;

	private static double		eVelo;

	private static double		eShootVelo;
	private static double		eMiddleVelo;

	static PaintDiagramm		paintVelo		= new PaintDiagramm();
	RobotStatus					myStatus;
	double						enemyVelo;

	static PaintSegmentDiagramm	segDia			= new PaintSegmentDiagramm();
	static double				eVeloSeg[]		= new double[17];

	public Kowari()
	{}

	@Override
	public void run()
	{
		setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		//dir = 100;
		lastHit = dir = 30;
		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
	}

	@Override
	public void onScannedRobot(final ScannedRobotEvent e)
	{
		int index = (int) (Math.round(e.getVelocity()) + Rules.MAX_VELOCITY);
		eVeloSeg[index]++;
		enemyVelo = e.getVelocity();
		double aBear;
		//double bPower;
		//@formatter:off
		setTurnRightRadians(
				(dFactor  - e.getDistance())
				* getVelocity() 
				* ADVANCE_FACTOR
				+ Math.cos(aBear = e.getBearingRadians())
				);
		
	
		double v = ((eVelo +=e.getVelocity())/GUN_TURNS);
		if (getGunHeat() < 0.2)
		{
			if (eShootVelo != 0 && 	 eMiddleVelo != eShootVelo)
			{
				setAllColors(Color.YELLOW);
				v = -v * 1.2;
			}
			else
			{
				setAllColors(Color.GREEN);
			}
		}		
		setTurnGunRightRadians(
				Utils.normalRelativeAngle(
						(aBear+=getHeadingRadians()) 
						- getGunHeadingRadians() 
						+ 
						(
						   v 
						   * Math.sin(
								e.getHeadingRadians() - aBear
							) 
							/ BSPEED
						)
				)
		);
		//@formatter:on
		setTurnRadarLeftRadians(getRadarTurnRemaining());

		if (((eEnergy - (eEnergy = e.getEnergy()))) > 0)
		{
			onBulletMissed(null);
			if (Math.cos(dirChange) < 0) onHitWall(null); // saves 2 byte compared to dir = - dir
		}

		System.out.format("[%04d] velo=%3.5f avg=%3.5f gunHeat=%3.5f \n", getTime(), e.getVelocity(),
				eVelo / GUN_TURNS, getGunHeat());

		if (getGunHeat() < 0.8 && eMiddleVelo == 0)
		{
			eMiddleVelo = Math.signum(e.getVelocity());
			System.out.format("[%04d] set eMiddleVelo=%3.5f\n", getTime(), eMiddleVelo);
		}

		if (setFireBullet(BPOWER) != null)
		{
			System.out.format("[%04d] fire  eShootVeo=%3.0f eMiddleVelo=%3.5f shootv=%3.5f\n", getTime(), eShootVelo,
					eMiddleVelo, v);
			onBulletMissed(null);
			eShootVelo = Math.signum(e.getVelocity());
			eMiddleVelo = 0;
			eVelo = 0;

		}
		setMaxVelocity(1800 / e.getDistance());
		setAhead(dir);
	}

	@Override
	public void onHitWall(HitWallEvent e)
	{
		dir = -dir;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event)
	{
		dFactor = Math.random() * 200 + 100;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		if ((lastHit - (lastHit = getTime())) > -24)
		{
			dirChange += HIT_FACTOR; // / delta;
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		eEnergy = e.getEnergy();
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		paintVelo.onPaint(g, myStatus, enemyVelo, Color.yellow, "Enemy");
		segDia.onPaint(g, this, eVeloSeg, Color.GREEN);
	}
}
