package wompi.paint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;

import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import wompi.paint.helper.EnemyStatus;
import wompi.robomath.RobotMath;

public class PaintGunProfile
{

	private final HashMap<Long, RobotStatus>	myStatusMap;
	private final HashMap<Long, EnemyStatus>	myRobotMap;
	private final HashMap<Long, GunStatus>		myGunMap;

	private RobotStatus							myStatus;

	public PaintGunProfile()
	{
		myStatusMap = new HashMap<Long, RobotStatus>();
		myRobotMap = new HashMap<Long, EnemyStatus>();
		myGunMap = new HashMap<Long, GunStatus>();
	}

	public void setGunTargetPoint(double x, double y, double bPower, boolean isReal, double gunHeading)
	{
		myGunMap.put(PaintFunctions.getAbsoutTime(myStatus), new GunStatus(myStatus, x, y, bPower, isReal, gunHeading));
	}

	public void onStatus(StatusEvent e)
	{
		myStatus = e.getStatus();
		myStatusMap.put(PaintFunctions.getAbsoutTime(myStatus), e.getStatus());
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		myRobotMap.put(PaintFunctions.getAbsoutTime(myStatus), new EnemyStatus(e, myStatus));
	}

	public void onPaint(Graphics2D g)
	{
		int i = 60;

		long time = PaintFunctions.getAbsoutTime(myStatus);

		RobotStatus _rStatus;
		EnemyStatus _eStatus;
		GunStatus _gStatus;

		while (i > 0)
		{
			long tDelta = time - i;

			_rStatus = myStatusMap.get(tDelta);
			_eStatus = myRobotMap.get(tDelta);
			_gStatus = myGunMap.get(tDelta);

			if (_rStatus != null)
			{
				PaintHelper.drawPoint(new Point2D.Double(_rStatus.getX(), _rStatus.getY()), Color.DARK_GRAY, g, 1);
			}
			if (_eStatus != null)
			{
				PaintHelper.drawPoint(new Point2D.Double(_eStatus.getX(), _eStatus.getY()), Color.BLUE, g, 1);
			}

			if (_gStatus != null)
			{
				PaintHelper.drawPoint(new Point2D.Double(_gStatus.getX(), _gStatus.getY()), Color.RED, g, 1);
				if (_gStatus.isReal())
					PaintHelper.drawPoint(_gStatus.getTimePoint(i), Color.YELLOW, g, 1);
				else
					PaintHelper.drawPoint(_gStatus.getTimePoint(i), Color.GREEN, g, 4);

			}
			i--;
		}
	}
}

class GunStatus
{
	private final RobotStatus	myGunStatus;

	private final double		xTarget;
	private final double		yTarget;
	private final double		myPower;

	private final boolean		isBullet;
	private final double		gunHeading;

	public GunStatus(RobotStatus s, double x, double y, double bPower, boolean isReal, double gunHead)
	{
		myGunStatus = s;
		xTarget = x;
		yTarget = y;
		myPower = bPower;
		isBullet = isReal;
		gunHeading = gunHead;
	}

	public boolean isReal()
	{
		return isBullet;
	}

	public double getGunHeading()
	{
		return gunHeading;
	}

	public double getPower()
	{
		return myPower;
	}

	public Point2D getTimePoint(long tDelta)
	{
		Point2D start = new Point2D.Double(myGunStatus.getX(), myGunStatus.getY());
		Point2D end = new Point2D.Double(getX(), getY());
		double angle = RobotMath.calculateAngle(start, end);
		double dist = tDelta * Rules.getBulletSpeed(myPower);
		return RobotMath.calculatePolarPoint(angle, dist, start);
	}

	public double getX()
	{
		return xTarget;
	}

	public double getY()
	{
		return yTarget;
	}
}