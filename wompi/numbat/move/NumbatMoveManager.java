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
package wompi.numbat.move;

import java.awt.Graphics2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import wompi.numbat.debug.DebugMoveProperties;
import wompi.numbat.target.ITargetManager;

public class NumbatMoveManager
{
	private RobotStatus				botStatus;
	private ITargetManager			myTargetMan;

	private ANumbatMove				myMove;
	private ArrayList<ANumbatMove>	allMoves;

	public NumbatMoveManager()
	{
		allMoves = new ArrayList<ANumbatMove>();
		allMoves.add(new NumbatMovePatterChallenge());
		allMoves.add(new NumbatMinRiskMove());
		allMoves.add(new NumbatMoveNone());
	}

	public void init()
	{
		checkActivate();
		myMove.init(botStatus);
	}

	public void onRobotDeath(RobotDeathEvent e)
	{
		checkActivate();
	}

	public void onScannedRobot(ScannedRobotEvent e)
	{
		checkActivate();
		myMove.onScannedRobot(e, botStatus, myTargetMan);
	}

	public void onHitRobot(HitRobotEvent e)
	{
		myMove.onHitRobot(e, botStatus);
	}

	public void onPaint(Graphics2D g)
	{

	}

	public void setMove()
	{
		myMove.setMove(botStatus, myTargetMan);
	}

	public void excecute(AdvancedRobot bot)
	{
		myMove.excecute(bot);
	}

	private void checkActivate()
	{
		for (ANumbatMove move : allMoves)
		{
			if (move.checkActivateRule(botStatus, myTargetMan))
			{
				if (myMove != move)
				{
					myMove = move;
					myMove.init(botStatus);  // TODO: this is bad design and should be fixed, init has to be called only once a battle
					DebugMoveProperties.debugCurrentMove(myMove.getName());
				}
				return;
			}
		}
		throw new IllegalStateException("NumbatMoveManager no move fits");
	}

	public void setTargetManager(ITargetManager targetMan)
	{
		myTargetMan = targetMan;
	}

	public void setBotStatus(RobotStatus status)
	{
		botStatus = status;
	}
}
