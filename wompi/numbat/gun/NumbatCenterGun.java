package wompi.numbat.gun;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.misc.NumbatBattleField;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;
import wompi.robomath.RobotMath;

public class NumbatCenterGun extends ANumbatGun
{
	HashMap<String, StringBuilder>	allHistory;
	HashMap<String, helperSquare[]>	allLastCoord;

	public NumbatCenterGun()
	{
		allHistory = new HashMap<String, StringBuilder>();
		allLastCoord = new HashMap<String, helperSquare[]>();
	}

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent scan, RobotStatus status, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getLastScanTarget();
		StringBuilder history = allHistory.get(target.eName);
		helperSquare[] lastCoord = allLastCoord.get(target.eName);
		if (history == null)
		{
			allHistory.put(target.eName, history = new StringBuilder());
			allLastCoord.put(target.eName, lastCoord = new helperSquare[2]);
		}

		double centerAbsBearing = Math.atan2(target.x - NumbatBattleField.getCenterX(), target.y - NumbatBattleField.getCenterY());

		double centerLatVel = RobotMath.calculateLateralVelocity(0, centerAbsBearing, target.eHeading, target.eVelocity);
		double centerAdvVel = RobotMath.calculateAdvancingVelocity(0, centerAbsBearing, target.eHeading, target.eVelocity);

		double centerAngle = Math.atan(centerLatVel / target.distance(NumbatBattleField.getCenterX(), NumbatBattleField.getCenterY()));

		double latVel = RobotMath.calculateLateralVelocity(status.getHeadingRadians(), target.getAbsoluteBearing(status), target.eHeading,
				target.eVelocity);
		double advVel = RobotMath.calculateAdvancingVelocity(status.getHeadingRadians(), target.getAbsoluteBearing(status), target.eHeading,
				target.eVelocity);

		double angle = Math.atan(latVel / target.distance(status.getX(), status.getY()));

		//		System.out.format("[%d] cLat=%3.2f cAdv=%3.2f cAngle=%3.10f me (lat=%3.2f adv=%3.2f angle=%3.10f) \n", status.getTime(), centerLatVel,
		//				centerAdvVel, Math.toDegrees(centerAngle), latVel, advVel, angle);
		//		System.out.format("[%d] cLat=%d cAdv=%d cAngle=%d me (lat=%d adv=%d angle=%d) \n", status.getTime(), (int) Math.rint(centerLatVel),
		//				(int) Math.rint(centerAdvVel), (int) (Math.toDegrees(centerAngle) * 10), (int) Math.rint(latVel), (int) Math.rint(advVel),
		//				(int) (Math.toDegrees(angle) * 10));

		helperSquare s = new helperSquare();
		s.center = new Point2D.Double(target.x, target.y);
		s.color = target.getBotColor();

		if (lastCoord[0] == null)
		{
			lastCoord[0] = s;
		}
		lastCoord[1] = s;
	}

	ArrayList<helperSquare>	pointList	= new ArrayList<helperSquare>();

	long					lastTime;

	@Override
	public void onPaint(Graphics2D g, RobotStatus status)
	{
		if (status.getTime() < lastTime) pointList = new ArrayList<helperSquare>();
		lastTime = status.getTime();

		for (helperSquare[] field : allLastCoord.values())
		{
			if (field[0].center.distance(field[1].center) >= 36)
			{
				pointList.add(field[1]);
				field[0] = field[1];
			}
		}

		for (helperSquare point : pointList)
		{
			PaintTargetSquare.drawTargetSquare(g, 0, point.center.getX(), point.center.getY(), false, point.color);
		}
	}

	@Override
	String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		// TODO Auto-generated method stub
		return false;
	}

	class helperSquare
	{
		Point2D	center;
		Color	color;
	}
}
