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
package wompi.echidna.misc.painter;

public class PaintTargetStats
{

	public static void registerTargetStats()
	{
		// double bPower = myFire.getFirePower(target);
		// long bTurns = (long)(target.getDistance()/Rules.getBulletSpeed(bPower));
		// long maxBTurns = (long)(target.getDistance()/Rules.getBulletSpeed(Rules.MIN_BULLET_POWER));
		// long minBTurns = (long)(target.getDistance()/Rules.getBulletSpeed(Rules.MAX_BULLET_POWER));
		//
		// // int showX = (int)(robot.getX() + target.x);
		// // int showY = (int)(robot.getY() + target.y);
		// int showX = 700;
		// int showY = 700;
		// int count=0;
		//
		//
		// g.setColor(Color.GRAY);
		// g.setFont(PaintHelper.myFont);
		// g.drawString(String.format("BP: %3.2f BT: %d max: %d min: %d ",bPower,bTurns,maxBTurns,minBTurns),showX,showY-count++);
		// g.drawString(String.format("D: %3.2f V: %3.2f\n",target.getDistance(),target.getVelocity()),showX,showY-count++*PaintHelper.myFont.getSize());
		// g.drawString(String.format("N: %s\n",target.getName()),showX,showY-count++*PaintHelper.myFont.getSize());
		// g.drawString(String.format("V: %3.2f avg: %3.2f",target.getVelocity(),0D),showX,showY-count++*PaintHelper.myFont.getSize());
		// //g.drawString(String.format("LC:%d LD:%d",target.stats.lastChange,target.stats.lastDir),showX,showY-count++*PaintHelper.myFont.getSize());
		// //g.drawString(String.format("D: %d %d %d ",target.stats.dirs[0],target.stats.dirs[1],target.stats.dirs[2]),showX,showY-count++*PaintHelper.myFont.getSize());
		// //g.drawString(String.format("C: %d %d %d ",target.stats.dirCount[0],target.stats.dirCount[1],target.stats.dirCount[2]),showX,showY-count++*PaintHelper.myFont.getSize());
		// //g.drawString(String.format("A: %d %d %d ",target.stats.dirs[0]/target.stats.dirCount[0],target.stats.dirs[1]/target.stats.dirCount[1],target.stats.dirs[2]/target.stats.dirCount[2]),showX,showY-count++*PaintHelper.myFont.getSize());
	}

}

class PaintTargetHelper
{
	String	eName;
	double	ex;
	double	ey;
	double	eLastHeading;
	double	eHeading;
	double	latVel;
	double	advVel;
	double	eLastVelocity;
	double	eVelocity;
	double	eDistance;

}
