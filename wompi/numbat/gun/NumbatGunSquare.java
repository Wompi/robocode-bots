package wompi.numbat.gun;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.echidna.misc.painter.PaintTargetSquare;
import wompi.numbat.gun.fire.ANumbatFire;
import wompi.numbat.gun.misc.NumbatRelativePoint;
import wompi.numbat.target.ITargetManager;
import wompi.numbat.target.NumbatTarget;
import wompi.paint.PaintHelper;

public class NumbatGunSquare extends ANumbatGun
{
	private final static int				MAX_KEY_LENGTH	= 30;
	final static char						LOST_SCAN		= '_';

	LinkedList<ISquareScan>					allTicks		= new LinkedList<ISquareScan>();
	StringBuilder							history			= new StringBuilder();
	HashMap<Integer, ArrayList<Integer>>	patternMap		= new HashMap<Integer, ArrayList<Integer>>();

	@Override
	void setGun(RobotStatus status, ITargetManager targetMan, ANumbatFire fire)
	{
		HashMap<String, Integer> test = new HashMap<String, Integer>();
		StringBuilder sb = new StringBuilder(history.substring(0, Math.min(MAX_KEY_LENGTH, history.length())));
		while (sb.length() > 0)
		{
			int len = sb.length();
			while (len > 0)
			{
				String sub = history.substring(0, len);
				ArrayList<Integer> indexList = patternMap.get(sub.hashCode());
				if (indexList != null && indexList.size() > 1)
				{
					//System.out.format("[%d] longest pattern %d (%d) - %s \n", status.getTime(), len, indexList.size(), sub);
					Integer bla = test.get(sub);
					if (bla == null) test.put(sub, indexList.size());
					break;
				}
				len--;
			}
			sb = sb.deleteCharAt(0);
		}

		for (Entry<String, Integer> entry : test.entrySet())
		{
			System.out.format("[%d] longest pattern %d (%d) - %s \n", status.getTime(), entry.getKey().length(), entry.getValue(), entry.getKey());
		}

		// later
		//		Iterator<ISquareScan> iter = allTicks.descendingIterator();
		//		int count = 0;
		//		//System.out.format("[%d] ", e.getTime());
		//		while (iter.hasNext() && count <= 20)
		//		{
		//			count++;
		//			ISquareScan lastScan = iter.next();
		//			if (!lastScan.isLostScan())
		//			{
		//				// transformation to relative coordinates
		//				double dx0 = sScan.getX() - lastScan.getX();
		//				double dy0 = sScan.getY() - lastScan.getY();
		//
		//				double dx1 = dy0 * Math.cos(sScan.heading) + dx0 * Math.sin(sScan.heading); // beware of the changed xy because of the robocode coordinates rotated
		//				double dy1 = dx0 * Math.cos(sScan.heading) - dy0 * Math.sin(sScan.heading);
		//
		//				int dx = (int) Math.round(dx1 / 36.0);
		//				int dy = (int) Math.round(dy1 / 36.0);
		//				//System.out.format("%d:[%d,%d] ", count, (int) dx1, (int) dy1);
		//			}
		//			else
		//			{
		//				//System.out.format("%d: lost ", count);
		//			}
		//		}
		//		//System.out.format("\n");

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e, RobotStatus myBotStatus, ITargetManager targetMan)
	{
		NumbatTarget target = targetMan.getGunTarget();
		long scanDiff = target.getLastScanDifference();

		NumbatRelativePoint relPoint = new NumbatRelativePoint();
		relPoint.registerState(e.getVelocity(), target.eLastVelocity, target.eHeading, target.eLastHeading);

		System.out.format("[%d] %s (%d) scandiff=%d velo=%3.5f lastVelo=%3.5f accel=%3.5f heading=%3.5f deltaHead=%3.5f patternMap(%d)\n",
				e.getTime(), relPoint.getBinaryString(), relPoint.getKey(), scanDiff, e.getVelocity(), target.eLastVelocity,
				target.getAccelleration(), e.getHeading(), Math.toDegrees(target.getHeadingDifference()), patternMap.size());
		for (int i = 0; i < (scanDiff - 1); i++)
		{
			allTicks.add(new LostSquareScan());
			history.insert(0, LOST_SCAN);
		}

		SquareScan sScan = new SquareScan();
		sScan.x = target.x;
		sScan.y = target.y;
		sScan.heading = target.eHeading;
		allTicks.add(sScan);

		history.insert(0, (char) relPoint.getKey());

		int index = allTicks.size();
		int len = Math.min(MAX_KEY_LENGTH, history.length());
		while (len > 1)
		{
			String sub = history.substring(0, len);
			int hash = sub.hashCode();
			ArrayList<Integer> indexList = patternMap.get(hash);
			if (indexList == null)
			{
				patternMap.put(hash, indexList = new ArrayList<Integer>());
			}
			indexList.add(index);
			len--;
		}
		System.out.format("History: %s\n", history.substring(0, Math.min(100, history.length())));
	}

	@Override
	String getName()
	{
		return "Square Gun";
	}

	@Override
	boolean checkActivateRule(RobotStatus status, ITargetManager targetMan)
	{
		return false;
	}

	@Override
	public void onPaint(Graphics2D g, RobotStatus status)
	{
		if (allTicks.isEmpty()) return;

		Iterator<ISquareScan> iter = allTicks.descendingIterator();
		int count = 0;
		while (iter.hasNext() && count <= 20)
		{
			count++;
			ISquareScan lastScan = iter.next();
			if (!lastScan.isLostScan())
			{
				PaintHelper.drawPoint((Point2D) lastScan, Color.GREEN, g, 4);
			}
			else
			{}
		}

		ISquareScan lastScan = allTicks.get(Math.max(0, allTicks.size() - 1));

		int grid = 4;
		for (int i = (1 - grid); i < grid; i++)
		{
			for (int j = (1 - grid); j < grid; j++)
			{
				PaintTargetSquare.drawTargetGrid(g, lastScan.getHeading(), lastScan.getX(), lastScan.getY(), false, Color.LIGHT_GRAY, i * 36, j * 36);
				PaintTargetSquare.drawTargetGrid(g, 0, lastScan.getX(), lastScan.getY(), false, Color.YELLOW, i * 36, j * 36);
			}
		}

	}
}

interface ISquareScan
{
	boolean isLostScan();

	int getKey();

	double getX();

	double getY();

	double getHeading();
}

class SquareScan extends Point2D.Double implements ISquareScan
{
	private static final long	serialVersionUID	= -5187590392980277455L;
	int							key;
	double						heading;

	@Override
	public boolean isLostScan()
	{
		return false;
	}

	@Override
	public int getKey()
	{
		return key;
	}

	@Override
	public double getHeading()
	{
		return heading;
	}
}

class LostSquareScan extends Point2D.Double implements ISquareScan
{
	private static final long	serialVersionUID	= -4677767679034325653L;

	@Override
	public boolean isLostScan()
	{
		return true;
	}

	@Override
	public int getKey()
	{
		return NumbatGunSquare.LOST_SCAN;
	}

	@Override
	public double getHeading()
	{
		return 0;
	}

}
