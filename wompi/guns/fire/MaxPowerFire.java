package wompi.guns.fire;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class MaxPowerFire
{
	private static final double	bPower	= Rules.MIN_BULLET_POWER;

	private AdvancedRobot		myBot;
	private Bullet				myBullet;

	public MaxPowerFire()
	{

	}

	public void onInit(AdvancedRobot bot)
	{
		myBot = bot;
	}

	public void onStatus(StatusEvent e)
	{
		myBullet = null;
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (Utils.isNear(0.0, myBot.getGunTurnRemainingRadians()))
		{
			myBullet = myBot.setFireBullet(bPower);
		}
	}

	public boolean hasFired()
	{
		return myBullet != null;
	}
}
