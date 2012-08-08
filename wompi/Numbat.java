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
import java.awt.event.KeyEvent;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import wompi.echidna.misc.utils.BattleField;
import wompi.numbat.debug.DebugBot;
import wompi.numbat.debug.DebugGunProperties;
import wompi.numbat.debug.DebugMiscProperties;
import wompi.numbat.debug.DebugMoveProperties;
import wompi.numbat.debug.DebugRadarProperties;
import wompi.numbat.debug.DebugTargetProperties;
import wompi.numbat.debug.DebugTestProperties;
import wompi.numbat.gun.NumbatGunManager;
import wompi.numbat.misc.ScannedRobotHandler;
import wompi.numbat.misc.SkippedTurnHandler;
import wompi.numbat.move.NumbatMoveManager;
import wompi.numbat.radar.NumbatRadarManager;
import wompi.numbat.target.NumbatTargetManager;

/**
 * Code is open source, released under the RoboWiki Public Code License:
 * http://robowiki.net/cgi-bin/robowiki?RWPCL
 * What the ... is a Numbat? (See: http://en.wikipedia.org/wiki/Numbat)
 * Develop Notes:
 * - avgPattern guess to shot only if the pattern is valid - tried but no use because many bots have no good pattern and therefore it stops shooting
 * - chase bullets - only useful in certain situations
 * - danger system - not tried yet
 * - shooting at weak targets while moving along the main target - not tried yet
 * - while searching for the best angle - hold the avgGuessFactor for every bulletPower and take the best
 * - calclulate angle only if the gun is 1 tick before shoot ready and use the other turns to clean the map or something other (maybe vitual gun?)
 * - bulletpower decision on best pattern - tried but not good, superior against all samples almost 100k but all other bots are worst maybe more tweak
 * If you are keen to read this ... be prepared for very bad English (i'm German)
 * Size v1.0: 1864
 * New bot and new hopes :). This bot utilize a SingleTick pattern matcher (STPM) gun with a very nice pattern count of up to 5.000.000 saved pattern.
 * The radar is also something new (a least for me) and is invented with a very high lock count (to collect the pattern quickly) while still getting a
 * good field view. It basically does sweep the radar once than it locks to the nearest target until the other target radar circles reached the bot.
 * Something like a radar wave i guess. This means that i don't scan for targets in the opposite corner for a very long time because they are no
 * thread to me
 * at this position. The implementation is very rude right now because it turned out that this kind of radar has many special rules to handle and i'm
 * to stupid
 * to see how it goes easier. This will be fixed in future releases.
 * The gun is a first draft of my new STPM gun which has its root from the SingleTick bot of Simonton. in its first release the gun works the same
 * like SingleTicks
 * gun but with a very important change. The original gun was not capable to hold much pattern because of very rough memory usage. My data structure
 * can hold a
 * very large amount of visited patterns and as there it has a good base to build on in future releases.
 * The patterns are hold by a map of string hash codes and the pattern ticks. Most pattern are visited only once and it saves huge memory chunks if
 * the data holder
 * is tweaked to this.
 * The movement is the movement of Wallaby and i will use it for the first versions as it is.
 * Many things are open and have to be fixed and changed:
 * - right now the bot is mega class and it should be mini in the end
 * - the gun is not ready for wall avoid or collisions or other fancy pattern stuff
 * - the gun needs some research to find the best values to save the current state (shot at, distance to wall, velocity change and so on).
 * - the radar has to be smaller and the target search has to be faster (it sweeps right now the hole round even if the target is one tick in the
 * other direction)
 * - the movement is nice for crowded situations but way to weak to hold the 1v1 challenge and therefore it has to be changed
 * Credit: Simonton deserves credit for the invention of the SingleTick pattern concept and for the tutorial bot SingleTick
 * ... lets go hunting
 * Size v1.1: 1824
 * The first release was not that bad and hopefully this one will advance a little bit further. I cleaned the code and it is now almost mini class
 * size.
 * The gun got after a little research his first improvement and can now switch the pattern if the target would hit the wall. The guessed pattern
 * takes
 * in this case the most used next step and calculates the pattern based on this step till the bullet reaches the target. I also changed the combat
 * variables
 * to be more cowardly and the radar locks even more now. The radar got no change and is still bugging me.
 * The data structure for the patterns works now very smooth and can very quick give the related informations. Also are no longer pattern for size
 * zero saved,
 * which saved almost 400k entries per match.
 * The match key is decreased to 30 because it turned out that this quite a good match for almost every bot. The gun is now very fast in adapting
 * movement
 * changes.
 * The first test have shown that the performance against top 20 bots is worst than ever. But everything over top 20 should be hit quite well,
 * especially
 * almost every nano bot is now a good pray.
 * Credit: look at Numbat at RoboWiki
 * ... lets go
 * Size v1.2: 5000+
 * I decided not to hold me back in code size any longer and therefore i refactored the code to something more modular. The positive side effect is,
 * that i
 * don't get lost in the code anymore. The gun is still Single Tick Pattern but i enhanced the usability a little. The radar is still rude and i don't
 * like
 * it but for now it is enough (the radar has still a major bug where the bot ends as siting duck). The target holds now way more informations, like
 * start pattern length (correlates to HitRatio) and some other goodies. Finally i managed to bring everything ready to work within run (needs still
 * lots
 * of work to be perfect). The new score targeting rule worked very good in testings. The target will now not be changed if i collected a lot of score
 * and
 * would waste the bonus if i switch to another target.
 * Anyway, still lots of stuff to do and i don't think it will perform better then the last version but i give it a go.
 * Credit: Numbat at RoboWiki
 * .. lets go
 * Size v1.3: 6000+
 * Nice APS jump in the last version and hopefully this one will do also good. The gun got some minor tweaks, like saving patterns from just scanned
 * targets (not only main target). But most attention got the movement. It is still just a melee movement but i hope the little changes made it
 * competitive at least till top 10. The start movement is free and therefore no more wall hits at start and searching a good position to start the
 * rumble. This also increased the near bot behavior and should be way more agile then the last one. The radar got a little workaround for the bug
 * (still looking for it :(). I also fixed a couple of bugs which i can't remember anymore (mostly stupidity bugs like wrong delta calculations and
 * such stuff)
 * All in all i start to like this bot because it shows some potential and i haven't started to improve the gun by now. The hit rate against
 * nano/samples and
 * other weak mover is quite impressive.
 * The graphic is now enabled (press 'p') and the code goes up and up :)
 * Next step should be watching the gun a little closer, especially the memory use is something i haven't put interest after the last changes. But i
 * hope it is ok
 * and should be around 200MB per melee battle.
 * Credit: ...
 * .. lets go
 * Size v1.4: 7400+
 * Awesome ranking at 11 general melee and the crowd goes wild :). The gun starts to kick in and supported by the overall synergies it performs better
 * than Wallaby. This version has some minor tweaks to the radar. Look for the best angle to scan and find faster at start, and stop start scan if all
 * opponents found. The gun is now really good at memory management because of the dynamic match key ability. This brings the saved pattern from
 * almost
 * 1.000.000+ to just 100k per melee battle. The synergy is if the enemies have long pattern (up to 120 i have seen) they have not much pattern to
 * save,
 * the other side is a strong movement with pattern around 5 and they have also not much pattern because of the small key. Pretty impressive right :).
 * The gun holds his hit ratio and my gut tells me it could even be better with the dynamic match key.
 * I changed the fire power selection to something i have read on the RoboWiki. Paul Evans stated there it might be better to shoot just 1.75 bullets
 * because the net gain might be the same by increasing the overall survivabillity. You know what, i can say it magically works. I got the same score
 * against
 * all my test bets by just shooting 1.75 bullets most of the time. This guy is probably from another star or was raised by magical elves :). Anyway
 * he deserves
 * the credit and i i'm glad i can give it. Lets see how it works in the rumble.
 * The debug got some shiny new property printout and can be seen in the debug property tab (key 'd')
 * Credit: Paul Evans by finding a magical number
 * .. lets go
 * Size v1.5:
 * After a very long and frustrating time of debugging and refactoring i have hopefully found something that is worth a release. I have to admit, that
 * this
 * version is a very premature release and is not fully tweaked and reviewed. The reason behind this - i spend the whole day to find something that
 * could
 * increase my APS and right before i would give up it came to me. My test bets get busted with this version and i'm right now very exited to see how
 * it works
 * in the rumble. Right now i ask myself, why are always the obviously stupidest ideas the ones that bring most success. Well lets see what i have
 * changed.
 * First and most visible change is the code base. It got an complete overhaul and is now more OOP (still don't like it) and is highly modularized.
 * From now
 * on i can make new guns, radars, moves, fire strategies and what not and it is instantly ready to use. Maybe in future versions i switch guns and
 * radars
 * to fit the current battle situation better like i do now but only for maintenance reasons.
 * The radar is completely changed and finally deserve the name code. The function is still the same but with way less code and more stability. I
 * really like it
 * now and it works superior (by my standards). It is very embarrassing if i see how stupid the old 'code' was and now it has the same function and is
 * such
 * a tiny piece of code. Well it works good :)
 * Hmm what other stuff is new.... hmmm? Ah yap. The debug properties is now something that i have fallen in love with. From now on i can just tap
 * some keys
 * and can switch of and on the related debug informations. The informations will increase in future versions but the framework is ready and works.
 * (r = radar, g = gun, s = misc, m = move) (right now everything is enabled). The graphics are very basic and i disabled most of them. Only radar and
 * some
 * target informations will be shown. (at least it shows how the radar works).
 * Last, but not least, the main change and the one that i hope to get some APS for is the gun change. I spend a lot of time to research the gun
 * behavior,
 * tried to play with a dozen different state keys, reinforced pattern change (maybe the wrong term, i just tried to revisit the used pattern and
 * would adjust them)
 * This worked not very well but it looks like it could work somehow (maybe bearing offsets or something). In the end i was so frustrated that i just
 * made an 'rage'
 * change and the all used velocity to the mean velocity average. You will guess it - jep it worked not very well. But it showed that the influence of
 * the velocity
 * on the pattern has some correlation to the hit rate. A couple of cigarettes later i was pretty sure that a short time average might work better
 * than the current
 * velocity. First try was 10 ... to much 5 better but lets try 3 ... bingo! I'm sure it will not bring that much with just this little try. But the
 * direction is clear.
 * Hmm i guess it is enough text ...
 * Credit: as usual
 * ... lets go
 * Size v1.6:
 * Hmm the summer is killing my brain and i can't think straight anymore. I tested lots of stuff with the gun but it brought me nothing. I decided to
 * switch
 * back to a cleaner code base, mostly because the 'hidden' skipped turns drive me crazy right now. I changed just minor stuff to be more precise in
 * the gun,
 * got rid of the dynamic key length, brought back the zero patterns and some other stuff. What concerns me most is that i cannot hit Walls with a hit
 * ratio
 * above 90 - there is something really wrong with my gun :(. I have to admit that got a little lost with my code and maybe should take a break until
 * the
 * weather cools down a little.
 * All debug and paint things are disabled and commented in this version
 * Credit: hmm
 * .. toooo hot
 * 
 * Size v1.7:
 * Hmm my new formatter messed up the comments but for now i'm to lazy to fix this. Anyway i try to switch to github and comment the code while
 * punching it
 * in. After aeons of testing, searching and crying i finally found a huge bug within my gun and from now on i hit wall with an impressive hitratio.
 * The bug
 * was very well hidden and i stepped over it by accident. Well at least the gun is now a little better and i started to tweak some combat settings.
 * Maybe
 * i can go back to dynamic pattern and zero pattern we will see.
 * I put all my botcode on github and i'm quite happy about this. I changed a couple of minor things too but have forgotten was it was.
 * 
 * Credit: hmm
 * ... yehaaa a new version
 * 
 * @author Wompi
 * @date 08/08/2012
 */
public class Numbat extends AdvancedRobot
{
	public static NumbatRadarManager	myRadarMan	= new NumbatRadarManager();
	public static NumbatGunManager		myGunMan	= new NumbatGunManager();
	public static NumbatTargetManager	myTargetMan	= new NumbatTargetManager();
	public static NumbatMoveManager		myMoveMan	= new NumbatMoveManager();

	// debug stuff - refactor this a little
	// private boolean isTimeDebug = true;

	// related to skipped turns bug, where double scan events occur
	private final SkippedTurnHandler	mySkipped;
	private final ScannedRobotHandler	myScans;

	// public static TestPatternString myTestPattern = new TestPatternString();

	public Numbat()
	{
		DebugBot.init(this);
		mySkipped = new SkippedTurnHandler();
		myScans = new ScannedRobotHandler();
		myRadarMan.setTargetManager(myTargetMan);
		myGunMan.setTargetManager(myTargetMan);
		myMoveMan.setTargetManager(myTargetMan);
	}

	@Override
	public void run()
	{
		// setAllColors(Color.RED);
		setColors(new Color(0xB9, 0x87, 0x56), Color.BLACK, Color.WHITE, Color.ORANGE, Color.RED);
		BattleField.BATTLE_FIELD_H = getBattleFieldHeight();
		BattleField.BATTLE_FIELD_W = getBattleFieldWidth();
		// if (isTimeDebug) TimeProfile.initRound();
		myRadarMan.init();
		myGunMan.init();
		myMoveMan.init();
		myTargetMan.init();

		while (true)
		{
			// if (isTimeDebug) TimeProfile.DEBUG_TIME_01.start();
			handleScannedRobotEvents();

			myTargetMan.setTargets();
			myTargetMan.execute(this);

			myRadarMan.setRadar();
			myRadarMan.excecute(this);

			myMoveMan.setMove();
			myMoveMan.excecute(this);

			// if (isTimeDebug) TimeProfile.GUN_EXECUTE.start();
			myGunMan.setGun();
			myGunMan.excecute(this);
			// if (isTimeDebug) TimeProfile.GUN_EXECUTE.stop();

			DebugGunProperties.debugPatternClasses();
			//
			// DebugGunProperties.execute();
			DebugRadarProperties.execute();
			DebugGunProperties.execute();
			DebugMiscProperties.execute();
			DebugMoveProperties.execute();
			DebugTargetProperties.execute();
			// DebugTestProperties.execute();
			//
			// if (isTimeDebug)
			// {
			// TimeProfile.TURN_TIME.stop();
			// TimeProfile.DEBUG_TIME_01.stop();
			// TimeProfile.setRoundProperties(this);
			// }

			// myTestPattern.printHistory(getTime());
			execute();
		}
	}

	@Override
	public void onStatus(StatusEvent e)
	{
		// if (isTimeDebug) TimeProfile.TURN_TIME.start();

		// myTestPattern.registerHistoryPlaceHolder(e.getTime());

		mySkipped.onStatus(e);
		myScans.onStatus(e); // skipped turn bug prevent
		myTargetMan.setBotStatus(e.getStatus());
		myRadarMan.setBotStatus(e.getStatus());
		myGunMan.setBotStatus(e.getStatus());
		myMoveMan.setBotStatus(e.getStatus());
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		// scan events will be handled all together in run
		myScans.onScannedRobot(e);
		mySkipped.onScannedRobot(e);
	}

	/**
	 * For more information look at ScannedRobotHandler
	 * This might be cause some problems with onPaint events because the scan events are now handled in run and
	 * after onPaint();
	 */
	private void handleScannedRobotEvents()
	{
		if (myScans.hasEvents())
		{
			// if (isTimeDebug) TimeProfile.GUN_UPDATE.start();
			for (ScannedRobotEvent scan : myScans.getAllScanEvents())
			{
				myTargetMan.onScannedRobot(scan); // sets also the targets new
				myGunMan.onScannedRobot(scan);
				myRadarMan.onScannedRobot(scan);
				myMoveMan.onScannedRobot(scan);
			}
			// if (isTimeDebug) TimeProfile.GUN_UPDATE.stop();
		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		myTargetMan.onRobotDeath(e);
		myRadarMan.onRobotDeath(e);
		myGunMan.onRobotDeath(e);
		myMoveMan.onRobotDeath(e);
	}

	@Override
	public void onHitRobot(HitRobotEvent e)
	{
		myTargetMan.onHitRobot(e);
		myMoveMan.onHitRobot(e);
	}

	@Override
	public void onBulletHit(BulletHitEvent e)
	{
		myTargetMan.onBulletHit(e);
	}

	@Override
	public void onSkippedTurn(SkippedTurnEvent e)
	{
		mySkipped.onSkippedTurn(e);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e)
	{
		// PaintHitCloud.registerHit(e);
	}

	@Override
	public void onPaint(Graphics2D g)
	{
		// PaintRobotPath.onPaint(g, getName(), getTime(),getX(), getY(), Color.GREEN);
		// PaintHitCloud.onPaint(g);
		myTargetMan.onPaint(g);
		myMoveMan.onPaint(g);
		myGunMan.onPaint(g);
	}

	@Override
	public void onDeath(DeathEvent event)
	{
		DebugMiscProperties.debugWinStats(getOthers());
	}

	@Override
	public void onWin(WinEvent event)
	{
		DebugMiscProperties.debugWinStats(getOthers());
	}

	@Override
	public void onKeyPressed(KeyEvent e)
	{
		char c = e.getKeyChar();
		DebugGunProperties.onKeyPressed(c);
		DebugMiscProperties.onKeyPressed(c);
		DebugRadarProperties.onKeyPressed(c);
		DebugMoveProperties.onKeyPressed(c);
		DebugTargetProperties.onKeyPressed(c);
		DebugTestProperties.onKeyPressed(c);
	}
}
