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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * What the ... is a Wallaby? (See: http://en.wikipedia.org/wiki/Wallaby)
 * If you are keen to read this ... be prepared for very bad English (i'm German)
 * some specials? (no)
 * - normal head-on gun
 * - endless spinning radar
 * 
 * - drives around in circles (the circle can offer some nice movement pattern)
 * Credit?
 * I learned a lot from open source robots on RoboWiki ... thanks for this mates
 * I didn't use code from them but many ideas come from there.
 * The movement is similar to ""tj.zombie1n 1.0.1" but he is closed source and i figured it out by myself. I saw him after
 * i entered the challenge and have to say i like this robot.
 * And of course, thanks to all the mates for robocode/RoboRumble.
 * Competitive?
 * The first version was not very well programmed and it stuck at 55 from 125 so i hope this one will do better
 * Size v1.0: lost
 * Unfortunately didn't i include the sources .. so this one is lost
 * Size v1.1: lost
 * see 1.0 ... i thought i included the source but it looked like the robocode packager don't like mac's
 * Size v1.2: 415
 * Well from now on i include the sources manually you can see the improvements(?)
 * This one is a little milestone, because i finally got the movement very smooth and there a lots of possibilities to
 * randomize the straight circle. i'm sure you will see in future releases nice ram/bullet dodge actions and crazy driving patterns
 * The gun is still very ugly ... i don't want to talk about it right now
 * The credit for the gun go's to RoboWiki tutorial ... linear targeting - it is taken from there
 * The fire rule is something i came up with, when i saw the Wallaby constantly draining energy from missed shots. It died to most 1on1 because
 * it shots missed shots drained the energy to zero. The rule to stop fire if energy is low would be improved in the future.
 * The radar is still very basic, but i don't really need it right now.
 * Size v1.3: 748
 * The last version did so damn horrible wrong that i decided to change the first idea and got back to a more common robot
 * I changed the movement massively and i like to see that my first thought about the randomness was right. We will see what it can do
 * at the competition.
 * The gun is still very rude and i wouldn't call it a gun .. its more an annoyance. I included a lot of rules to help the gun a little bit,
 * but this is not very good i think.
 * The radar is now not only spinning ... it locks in 1on1 and in certain combat situations. Again its more of a rule base than a good
 * program. But well you know ... sometimes you have to try out things.
 * I haven't really looked at the code size for this version and there are a lot of saves and things i can do better..
 * Credit: I will credit the Lunar robot from Sulibilune. He has a very nice implementation of anti gravity force and i liked this one so
 * much that i had to include it. I hope it is not a problem - if so please give me a message at RoboWiki to Wompi. It is just
 * the way how the force is calculated and i didn't took anything else.
 * Credit: the gun and radar are just copy past from the tutorials for linear targeting and 1on1 radar on RoboWiki. Thanks to this guys - it is a
 * pleasure to read this site and i always discover something new.
 * and off we go ... i'm so exited to see how it works
 * Size v1.4: 747
 * Well there we are again and Wallaby still dies like a fly. After a little combat research it turned out that the circular movement
 * is way to weak for all the corner sitters. If you want to survive you have to move to the save corner spots and try to capture the flag
 * there. But, well that is not my kind of fun. So i tried something different and we will see how far we can get with that.
 * From now on is Wallaby more like a Dingo and chases everyone and his dog. I implemented finally a better gun and hope this one will do.
 * Wallaby is searching for the nearest enemy and with some fancy avoid moving it closes in to combat distance. On my test runs this
 * was very funny to watch. Future versions need a little more survivability but for now it will do. The Radar is now way more aggressive
 * and locks the current target until it find by accident a new one ... or of course the old one is dead
 * The gun gets a little help from average Velocity calculations over all battles. I don't think it is overly important but my feeling about this
 * is good.
 * It has still some bad behavior. Wall stuck if the Math.random() not changes in a row. The center variables are not really necessary and the
 * Geometry is'nt perfect by now. So there is plenty of room for changes.
 * Credits: And again i have to thank some people for there work. First AntiGravityBot (Alisdair Owens), in the first place i took his
 * gun. But after a while i was disappointed about my implementation and changed it to what it is now. But thanks for the inspiration.
 * I like this robot and the movement is also very nice. I also had some code reading on certain 1v1 nano robots (sorry can't remember the names)
 * I didn't took any code but they are always nice to look at. So thanks to all who have decided to make there source code open public.
 * off we go..
 * Size v1.5: 747
 * Well, that worked out pretty well 7th after 120/125 pairings micro, 14th after 156/163 pairings mini, 53th after 306/320 pairings general
 * I'm well excited about that and will call it for now a success. Wallaby did very good as close combat melee robots but as i guessed it lacked
 * in survivability. After a massive test run i decided to make the gun more precise and fiddled in the targets heading change. I also changed
 * some combat variables to fit a little more survivability (hopefully). It still is not a match for the top robots but hopefully it will raise
 * some ranking. The code size is now pretty urgent and there is not much room for changes. The movement needs still some help and i'm quite
 * unhappy with the radar. The radar is a little to much flickering for my taste. To find some better combat setup i need a battle script that
 * figures out what is best and what not. But for now i'm not in the mood for this.
 * Credits: Nothing for today, i just watched my code an tried to make it better. No research so far. But anyway thanks for reading this :)
 * Maybe i should wait until the rumble is finished and all pairings equal. I'm really not sure how the ranking is working if i switch early to
 * another version. Unfortunately there are not many contributors these days, only MN is still doing quite nice contributions to the rumble.
 * Thanks for this, from me. I try to keep the contribution up and running but it's not enough for all rankings.
 * anyway off we go...
 * Size: v1.6 741
 * Oh lord what was i disappointed about the last version. Not only that i had overlooked a major bug it was also a disaster to see how terrible
 * the survivability of this version was. After a couple of fights i canceled this robot and switched back to 1.4. I decided that i need more
 * inspiration for the next version and found some source code of the top ranking robots. Well it turned out that my gun was nothing new or
 * special. Capulet 1.1, the top1 micro melee robot, has exactly the same gun and so the Credit goes to this robot. My gun does the same, but
 * with a minor advancement in backtracking the guessed Target points if the robot is near a wall. But one thing was really amazing of the
 * Capulet gun. And this is the bulletPower function. I played the whole day to find my own .. maybe better one, but it did'nt work. I decided
 * to take this bulletFunction. If this is a problem, please can someone give me a message on RoboWiki to Wompi. I'm not really sure what the
 * License about this is. I really hope this is ok.
 * The movement did very good and i did'nt change anything.
 * The radar is still annoying me.
 * Credits: Big credit goes to Capulet 1.1 (CrazyBassonist) he has an amazing robot. And it is very well programmed. I wish i could write code
 * like you. Same goes to Sprout 1.1.3 (name?). Unbelievable how this robot is programmed and it still fits the micro class. Awesome mates,
 * it was a pleasure to read this two robots.
 * well off we go ..
 * Size: v1.7 665 (yes unbelievable)
 * Well, let me say it first i'm not quite happy with this version, simple because it has another piece of code from CrazyBassonist in it.
 * I was looking for a simpler version of oscillator movement and found the nano-1v1 bot caligula ... and he did exactly what i was in need for.
 * First i used a modified version straight from the RoboWiki and it did the same what Caligula is doing. But it was 10 bytes more
 * and i thought it would be a waste not to use it straight away. Please CrazyBassonist if you read this (or someone who knows this mate) give me a
 * message at robocode wiki to wompi (unfortunately could'nt i found a contact otherwise i would ask you by myself). If it is against the robocode
 * License i will change this ASAP.
 * The last version did amazingly good and stuck at 4th with a very close look at the 3th kawigi.micro.Shiz 1.1. After this i played a little
 * to much with the NanoBots and all this awesome pieces of codes gave me a big headache. On this playing i realized that it is only the gun
 * that matter in melee .. every moving pattern fails if you have not the right gun at hand (or you have an excellent movement ... yes Lib again :)).
 * So i decided to give the simplest moving that i can come up with a try ... and surprisingly did it very good ... it almost won every test match
 * against the top10 MeleeBots even against capulet was it superior. I also adjusted the guessing of the gun a little more and now it spreads
 * way more. This gives a slightly better hit chance against other oscillators and i think it is worth a try.
 * By now i have plenty of bytes (something around 100 if full stripped) and have to think about something what can make my gun even better.
 * I have something in mind (classifying the enemy movement on displacement vectors) but it is not really finished yet. I just want to see who
 * well this version is doing at the rumble and after this i will give it a shot.
 * Credit: Biggest credit ever .. CrazyBassonist ... capulet, caligula. This mate has some impressive coding skills and also a very nice thinking in
 * terms of robocodeing. Unbelievable what caligula (nano weight) is doing. Please if you have'nt seen it till yet give this piece of code its
 * attention that it
 * deserves. I can not say it enough - well done mate.
 * off we go...
 * Size: v1.8 748
 * I'm somewhat disappointed that the last version was such a mess. It scored big against all the top nano bots but it was loosing so much points
 * against almost every low ranking robot. I don't know what's up there, my guess is, that i almost instantly die if it comes to pairings with other
 * weight class bots, say MegaBots. Because wallaby tries to go at close combat range it has no chance against this MegaBots and so it loose
 * a lot of scoring against the low ranking bots. I don't know if this is intended and the ranking is working this way but i have tried all pairings
 * local and it almost ever come up as first against my own class. If it works this way i have to change the bot almost completely and have to
 * implement
 * somewhat of a dodge movement. After a couple of tries with MiniumRiskMovement i decided to give the AntiGravity Movement a try and it worked
 * somehow
 * superior. I have to say that i took the implementation of DustBunny and it is a shame (i know that). But this days it is almost impossible to
 * invent
 * something equal without taking the thing which are still there. It is (like almost every nano code) an impressive piece of code and i can only
 * imagine
 * what kind of math genius must be behind such algorithm. I still try to combine the AntiGravity with OszillatorMovement but whatever i came up with
 * was not
 * small enough to fit in the micro class. I decide to give this one a try as it is just to see if the combination can raise some ranking. If it works
 * i
 * promise to change the code to somewhat more self invented. I don't fully understand the dynamics of MeleeRumble and have no idea how to test the
 * bot
 * without putting him at the rumble. I have tried RoboResearch but it is not working for melee very well. Even with the changes form voidius. Like
 * the last
 * version i run massive test against all bots within my weight class and it worked very well. But how can i be sure about this :( ... the last one
 * was
 * exactly the same. Well enough whining and we will see how it works.
 * Credit: The movement is fully taken 'stolen?' from DustBunny and Caligula and i deeply hope you guys are ok with that. If not just give me a short
 * message and i will instantly remove the bot and will come up with something other. I think the competition is very close to the edge and it is very
 * hard to come up with something real new. So it is almost left to shuffle what is there a little bit and hopefully it will work. The gun is nothing
 * new or original even if i came up with this by myself it was still there. Whoever wants to take the backtracking improvement is very welcome.
 * Well .. off we .. you know
 * Size: v1.9 745
 * I think it is time for a new version. Finally i got my own movement and i'm quite happy with that. The movement is based on MinimumRisk and
 * combines
 * anti gravity with oscillating. Basically what it does is to rate a couple of points around me, calculate the overall forces and the
 * perpendicularity
 * to the current target. That has two advantages. First wallaby stays, if the field is crowded, out of trouble and after a while the oscillating
 * kicks in.
 * This is mainly because the forces are not as strong as with 9 opponents. Second wallaby oscillates to close combat like version 1.8. the default
 * combat distance can be adjusted with the default power of the enemies, and this overcomes the mid field stuck. The rating between force and
 * perpendicularity
 * makes this working. One major surprise was my gun. I figured out that 1.8 had a bug and so it doesn't count the heading. This means i just had a
 * precise
 * linear head on gun. And this worked very well so far. But this means also i could think about a calculated solution over the iterated solution.
 * My tests showed that this version will probably do a little worse than 1.8 but i hope to see an improved survivabillaty.
 * I also figured out what the main difference between wallaby and the two top bots is. Both bots have way more fire power than wallaby. If i test my
 * bot
 * against the top ten i almost win and grab the survival points. But if it comes to pairings with other weaker bots both bots grab the survival
 * points
 * too, but because they make more damage they got more APS compared to wallaby. I have to look out for this in later versions.
 * The code is ugly and unfinished and i will tweak this later, for now i only want to see how it works.
 * Credit: Finally i could get rid of the "stolen" movement, it showed me that i was thinking in the right direction so thanks for this again to
 * CrazzyBassonist and MichaelDorgan. The fire power function is a little stripped but still Credit to CrazzyBassonist. I have an idea how to improve
 * this a little but for now it has to be enough.
 * Well, off we go
 * Size: v2.0 733
 * Hmm looks like i was wrong at most of my guessing for v1.9. Wallaby did surprisingly good and catch almost Capulets score. I don't know why i was
 * thinking v1.8 had a bug, because it turned out that everything was ok with it. So i decided to switch back to the precise circular targeting and
 * hopefully
 * this will give me some APS. It also turned out that even with a normal linear gun wallaby can still hold its score (remind this for later
 * versions). But the
 * biggest let down was to see that my movement isn't something new and that CrazzyBassonist did almost the same with his Mercutio bot. I didn't
 * noticed this bot
 * until it showed up as major thread for v1.9. Because i tend not to mix bots from same bot authors in my test runs this bot never showed up to me.
 * So the credit
 * goes back to CrazzyBassonist well done mate - you are still way ahead of me. After i was a little proud of me for finding a good movement i'm now
 * back to the old
 * thievery feeling, what a bummer.
 * Anyway i shuffled the code to something more compact, combined the gun with the movement calculations and changed the battlefield zone for the gun
 * and movement.
 * I also fixed some codeparts for the gun code (same logic but smaller code). And stripped the fire power function to a minimum (it turned out that i
 * don't need a min
 * check because it can not fall under the min bullet power). I also changed the movement from remaining distance to zero just to see if it also
 * works.
 * Credit: Well, back to CrazzyBassonist and his Mercutio 1.0 for the movement concept. Very nice mate.
 * And ... off we go (not as excited as last time, but still excited)
 * Size: v2.1 748
 * The last version was a complete fail because it had way to much bugs in it. I switched back to normal precise linear targeting because it turned
 * out
 * that i get almost the same results with it with the advantage to have less code size. Also back to own guess variables. The radar get some love
 * with
 * a 0.7 gun lock and now it has enough time to search and relock the target. I also switched back to a myTarget variable to hold the current target,
 * because
 * this makes wallaby only shooting if the radar and the gun is pointing at the enemy. It is not very much what i get out of this but is noticeable.
 * Otherwise
 * sometimes the gun shoots 'blind' on an old guessed position of the target. The radarGun adjustment had to go because of code size problems, but i
 * haven'd
 * seen any influence so far. For maintenance reasons i put all static variables to the head. The test works very well and it could be that this one
 * can
 * catch up very close to Capulet. It should score way better against nano bots than the former versions.
 * Credit: Still CrazzyBassionist for the overall movement concept.
 * Off we go...
 * Size: v2.2 746
 * I changed the combat variables a little bit to be more like version v1.9. There was not much success with the settings in v2.1 it was ok but not
 * perfect.
 * So hopefully this will do better. The code should be ok now and it works like v1.9. The tests show a little less performance than 1.9 but i want to
 * see
 * if the setup is ok.
 * Credit: well nothing changed :) see v2.1
 * Off we go ... uhh two versions on one day, i have a hunch this is not working very well
 * Size: v2.3 ...
 * Hmm v2.2 had still a bug and under certain circumstances the radar was stuck at one point and wallaby was a sitting duck. What a bummer. I don't
 * think
 * the DIST=210 and DIST_REMAIN=40 values wasn't working very well either. I hope the bug fix and the v1.9 combat setting brings wallaby back to the
 * top. If this
 * version fails again i will go back to v1.9 and start from there again. Looks like i missing the point what had made this version so strong. Nothing
 * else changed
 * and i'm a little disappointed about myself. I have to find something that cheer me up.. maybe a mini or 2v2 or so.
 * Credit: nothing changed
 * Go Go Go ...
 * Size: v2.4: 749
 * This one is probably the best i could come up with. I played with my nano bot (Quokka) and there i found out how to make the gun and movement
 * smaller.
 * I could save a lot of code size while taking out the getX() and getY() coordinate shift and just take the relative coordinates. This gave me the
 * opportunity
 * to fit in the heading change and the needed last scan check. The tests where superior compared to the last version but we will see how it works
 * against
 * the whole rumble. Wallaby has now a fully precise circular gun with wall handling and backtracking. The radar still locks on gun heat and the
 * movement
 * is a combination out of minimum risk, antigravity and oscillation. I have a hunch that i could save even more code size but for now i reached my
 * personal
 * limit. Version v2.3 did very well and scored with a measly 0.12% behind capulet.
 * Credit: nothing changed. But i want to give credit to all robocode veterans in general because they deserve it.
 * ... shy wallaby jumps at the battleground.
 * Size: v2.5 246
 * Uhh i'm to stupid to even bring one line of code without a bug. I forgot to set the lastScan variable in v2.4 and so it was exact the same like
 * v2.3 just a
 * normal linear gun. After putting this one in, i was back to code size problems for v2.5. I hope i got it finally right even without the lastScan
 * variable. The
 * heading difference will only kick in if it is in max turn rate limit. This solves the problem with scanned targets who haven't seen right before
 * shooting. And
 * also the reset on battle start. So we shoot sometimes linear and sometimes circular - i hope this make sense. Man, i'm quite a bit disappointed
 * about my
 * stupidity. Maybe i need a break but i'm totally be hooked and i smell somehow success :).
 * Credit: to all people who can code stuff without bugs :)
 * .. jump little wallaby jump.
 * Size: v2.6 742
 * Wallaby is holding the rumble and i think it can be a little better with the changes for this version. One thing was the fail against everything
 * what has wall in it's name (because i only shoot if they are in 20 wall zone). I changed the wall zone rectangle to be different for move and
 * shoot.
 * I'm not sure if it's a good idea to stop shooting near the end but i will try it with stop shooting at 6 energy. This give some room for wall hits
 * and a lot of 0.1 bullet hits. I have some code space left for other things but for now i have no idea how to spend it.
 * Credit: hmm, nothing really changed
 * .. off we go
 * Size: v2.7 738
 * Today i made an interesting observation and want to give it a try. I collected some stats about my fire frequency and it turned out, that i only
 * shoot 75% of the time. I loose a lot of turns with the getGunTurnRemain() call and the most interesting was that it happens mostly at the start of
 * the
 * battle. This means in this time i change the target to much often and loose the opportunity to fire. By just cutting out the function call, wallaby
 * is
 * shooting whenever it is possible and in the early game it has a good chance to hit something with this "air" shoots. At the end of the battle the
 * target is
 * mostly clear and there is no gun turning anymore.
 * I also switched the getEnergy() < 6 call. If the energy drops under 6 i still shoot 0.1 bullets till i'm dead - somehow this makes more sense to
 * me.
 * By now i have huge code size left and really should think about something to use it.
 * Credit: see last version
 * .. next jump
 * Size: v2.8 744
 * After the last both versions did not so well i got back to 2.5 and started from there again. This version holds a new, hopefully, improvement for
 * the
 * gun and should score a little more against nano/micro bot movements. The gun now shoots on zero velocity targets with the reversed former bot
 * direction.
 * This means if an oscillator moves back the gun shoots at the right direction. Against long distance mover the gun should score a little less. The
 * average
 * velocity is now a rolling average (by Paul Evans from the RoboWiki page). My tests showed that the normal average would be a little better, but i
 * didn't found
 * the required code space. The rolling average is over a very small (20) turn frame. I'm pretty sure there is space between the variables in
 * onScannedRobot() but
 * my brain refused to give me the right answer. Everything else is like version 2.5 and should work like it did.
 * Credit: Well, credit goes to Paul Evans for publishing the very code size efficiency rolling average formula.
 * .. off we go
 * Size: v2.9 743
 * Grr v2.8 was a complete fail. It lost almost 1%. Looks like i was wrong, as usual, with my predictions. It is sad that the zero velocity shooting
 * doesn't worked
 * like it should. I think to bring this to work it need more code and a lot of help from some stats about the velocity history. So i'm back to 2.5
 * with lot of
 * code size to spare and no really good idea to spend it. This time i try just some little things like onDeadRobot() only changes the target if it is
 * the
 * current target. Also i got back to a different field for move and gun, it should bring at least some points against wall movers. Well one bigger
 * change is
 * the randomization for the risk distance. I brings wallaby to change the heading more often, especially at corners. Also just for curiosity i
 * brought back the
 * setAdjust...(true) methods. There is enough code size left for really important stuff, if i only would find it.
 * Credit: well everything is back to normal :(
 * ... jumpy jump
 * Size: v3.0 742
 * Hmm, hmm and again hmm. As usual v2.9 was a big let down but it showed that i shoudn't mess with the movement. After a long test run with watching
 * almost
 * every battle it looked like that wallaby stays a little to much on the edge of the battlefield and as there it gets easy hits from whatever sits in
 * the corner
 * and shoots strait to the opposite corner. By lowering the target force it jumps more often out of the edge and is way more aggressive at mid game.
 * i'm not really sure
 * if this could help to gain some extra score but it is worth a try i think. My guess is that it now heavy depends on the opponents. So if i get most
 * battles with
 * strong opponents it will loose a big chunk against the lower bots. But in the opposite with only weak opponents it should score a little better.
 * Anyway i give it
 * a try just to see how it works. If this works i can think about a more dynamic system. Should be a combination of energy opponents left and force,
 * so it depends
 * what he does on the current battle state (this would be awesome i guess). I'm not very sure about the energy save if only one bot is left. On one
 * hand it could
 * give some survival points but on the other hand can the other bot collect more damage points and in the end it is even or less score. Well lets see
 * how this works.
 * Credit: hmm nothing new
 * ... boing boing
 * Size: v3.1 739
 * Well this one is just a quick check if the battle average of the velocity is better as the game average. The exciting thing about this is that i
 * have
 * found something that could give wallaby the needed edge to reach the top15 general but for that i first have to check the velocity average against
 * the rumble.
 * My guess is that the average is worst against mid/top bots and somewhat better against all nano/micro movement. I hope i can bring my new danger
 * system at work
 * for the next version. First tests showed that it is superior and the size still fits. But i need a more sophisticated formula to hold against every
 * possibilities and of course a whole new quite large test run.. man i'm quit excited right now. But for now ....
 * Credit: RoboWiki in general :) and everyone who think he deserves it
 * .... boing batz ... iiiiiihhhha
 * Size: v3.2 749
 * Hmm i'm curious about this one and i hope it will do well because it supports my new minimized chase bullet system. The tests where, let's say it,
 * superior but i had to give up some minor goodies like onRobotDeath target change and 1v1 energy save (not that Wallaby was somehow good at this
 * anyway)
 * I played recently with a lot of stuff like GF guns (unfortunately way to big), segmentations, smaller default radar and and and. This playing lead
 * to some
 * very nice insides about some of the more complex parts of robocode and i hope i can use it well in the future.
 * I found the necessary bytes to fit it in and are always surprised how many bytes i can spare just by looking at it again.
 * Credit: Well i saw that i should give credit to the one who invented minimum risk movement and to the one who made the goto stuff as easy as it is
 * now
 * Who ever you both are i credit you :).. i guess it is ancient robocode knowledge and only the veterans know where they are.
 * ... another jump on the field
 * Size: v3.3 746
 * The last version did not very well and i removed the chase bullets. This time i try the new danger rating i came up with, but i don't think it will
 * be an improvement. Maybe i should play with the setting a little more. I think i reached the limit of my bot (again :)) and should let it how it
 * was
 * in v3.1 and move on to something other. Like the twins or the next class. Well we will see.
 * The danger system counts the others if i die and adjust the target distance for the bullet power calculation. Basically this mean if i'm on the top
 * of the
 * list i increase my bullet power and vice versa.
 * Credit: as usual
 * .. off we go
 * Size: v3.4 742
 * Just a quick change of the danger system. The danger is now a very small increase in fire power if the bot dies before me. The max increase for all
 * 35 rounds won
 * would be 0.35. Lets see if this is sound or just another stupid idea. It is a burden to have 30 bytes to play with :). But i'm sure i get my 0.2%
 * score to
 * reach the top 15 general somehow.
 * Credit: ....
 * ... of we go
 * Size: v3.5 742
 * Playing with my big bots and my twins lead again to some thoughts about wallaby and here they are :). The damage system did not that well as i
 * hoped for,
 * but it is sound enough to be still in, i guess. This time i got back to the v=0 handling, so it shoots still with the average velocity if the
 * current velocity
 * is zero (not sure how fast disabled bots get hit after this). I also took the long lost adjustment from the v1.9 gun and hope it can do some stuff.
 * The most
 * important change is the new anti ram behavior. The combat distance is now adjusted to the nearest target and if the maxRate reaches a certain level
 * the
 * movement is not longer blocked by the distance remaining and is real time until the risk decrease to a normal level. This works very well in
 * crowded situations
 * and as well against RamBots. It is nice to look at how easy wallaby can jump out of the danger now. The energy save on 1v1 is again gone (i'm
 * really not sure if
 * it is paying or not). Well lets see what we can get :)
 * Credit: see Wallaby at RoboWiki .. i will give credit from no on there.
 * .. juuuuuuummmp
 * Size: v3.6 746
 * Nevermind the last version. I guess it was to much changing and it lost huge APS. This will be almost my last try to make something better but i'm
 * very sure
 * i will wallaby declare finished very soon. I removed the zero shooting and change to a "cowered" movement if only 2 opponents left, because wallaby
 * is not good
 * at all at this state of the battle and maybe it can grab some survival points out of this.
 * Credit: see Wallaby at RoboWiki
 * ... whoosh whoosh to the battle field
 * Size: v3.7 720
 * This is the last try for a longer time. If this fails again i move back to 3.1 and declare Wallaby as finished. I think i lost the point where
 * wallaby could be
 * made better and don't want to waste anymore time on it. I don't think this version will be an improvement to 3.1 because i just changed the fire
 * power
 * function to be turn based. This means it always select the fire power to reach the target in 22 turns. This leads to a very small distance window
 * where the fire power
 * will other than 3.0 and 0.1 (250-500). I had run a huge stats test and it turned out that this is the main combat distance in most battles. But as
 * i said i don't think
 * this is a remarkable improvement.
 * Credit: see Wallaby at RoboWiki
 * ... off we go
 * Size: v3.8 742
 * So far we tried every version something different and after playing with my new pattern matcher guns it came to me that an simple energy switch
 * for the targeting of the last three bots might be rewarding. All other versions failed so far and i think the energy 1vs1 rule is stronger than
 * i thought of. I'm still thinking the close combat rule is worth to put in but after the fail of the last versions i'm not that sure anymore and
 * raised the value to something that only happens in real close combat situations. I also checked the "trashing" and it turned out this is
 * nothing i have to worry about. Well lest see what we get :)
 * Credit: see Wallaby at Robowiki
 * ... back to work
 * Size: v3.9 742
 * Brr just a quick bug fix. The eEnergy variable was not set because the rule breaks on getOthers().
 * ... back to work
 * Size: v4.0 741
 * One sleepless night later Wallaby has made another jump in reduced code size and i hope some nice goodies to. The code size is down by almost 40
 * byte
 * because of the new radar. Some minor rules like shoot only e.energy/3 bullets a the target if it is almost dead, a little more force (hopefully
 * gains some
 * survival), the radar locks now in 1v1. Man i'm quite excited what is all possible with this small code size. I think if i strip all the rules and
 * minor
 * tweaks i could think of getting a PM or GF gun. This would be neat i guess. Well lets see what this version is all about.
 * Credit: see Wallaby at RoboWiki, Credit for the radar goes to Simonton because i saw it in one of his bots. Well done mate
 * ... jump
 * Size: v4.1 745
 * While working on my new bot i discovered a little hopefully improvement to the movement and i'm keen to try it on wallaby to. The basic idea was to
 * make the default movement a little bit more unpredictable and therefore i changed the perpendicular rate to something that gets randomized within
 * a small angle area. The test shows an increase in survivabillity in 1vs1 and if the field is not crowded anymore. I guess it is a little to much
 * for the first
 * rounds and so i start the randomization if the field is under 6 opponents. Maybe it is possible to find a more fitting value in the future but for
 * now
 * i just believe my gut feeling and we will see how it works.
 * Credit: as usual
 * ... jumpy jump
 * 
 * @author Wompi
 * @date 05/07/2012
 */
public class Wallaby extends AdvancedRobot
{
	private static final double				FIELD_W				= 1000.0;
	private static final double				FIELD_H				= 1000.0;

	private static final double				WZ					= 20.0;
	private static final double				WZ_SIZE_W			= FIELD_W - 2 * WZ;
	private static final double				WZ_SIZE_H			= FIELD_H - 2 * WZ;
	private static final double				WZ_G				= 17.0;
	private static final double				WZ_G_SIZE_W			= FIELD_W - 2 * WZ_G;
	private static final double				WZ_G_SIZE_H			= FIELD_H - 2 * WZ_G;

	private final static double				DIST				= 185;
	private final static double				CLOSE_DIST			= 150;
	private final static double				DIST_REMAIN			= 20;

	private final static double				RADAR_GUNLOCK		= 1.0;
	private final static double				RADAR_WIDE			= 3.0;
	private final static double				TARGET_FORCE		= 65000;								// 100000 low dmg high surv - 10000 high dmg
																										// low surv
	private final static double				TARGET_DISTANCE		= 600.0;

	private final static double				PI_360				= Math.PI * 2.0;
	private final static double				DELTA_RISK_ANGLE	= Math.PI / 32.0;
	private final static double				MAX_HEAD_DIFF		= 0.161442955809475;					// 9.25 degree
	private final static double				DEFAULT_RANDOM_RATE	= 0.5;

	static HashMap<String, WallabyTarget>	allTargets			= new HashMap<String, WallabyTarget>();

	static String							eName;
	static double							eDistance;
	static double							eEnergy;

	@Override
	public void run()
	{
		// setAllColors(Color.RED);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRightRadians(eDistance = eEnergy = Double.POSITIVE_INFINITY);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e)
	{
		WallabyTarget enemy;
		if ((enemy = allTargets.get(e.getName())) == null)
		{
			allTargets.put(e.getName(), enemy = new WallabyTarget());
		}
		double v0;
		double xg;
		double yg;
		double v1;
		double h0;
		double h1;
		double i;
		double r1;
		double rM;
		double bPower;
		double v2;
		double x;
		double y;

		xg = enemy.x = Math.sin((rM = (getHeadingRadians() + e.getBearingRadians()))) * (v0 = e.getDistance());
		yg = enemy.y = Math.cos(rM) * v0;
		v2 = ((enemy.vAvg += (Math.abs(v1 = e.getVelocity()))) * Math.signum(v1)) / ++enemy.avgCount;

		if (Math.abs(h0 = (-enemy.eHeading + (h1 = enemy.eHeading = e.getHeadingRadians()))) > MAX_HEAD_DIFF) h0 = 0;

		if (((getOthers() <= 2) ? (eEnergy > e.getEnergy()) : (eDistance > v0)) || eName == e.getName())
		{
			eName = e.getName();

			// bPower = Math.min(Rules.MAX_BULLET_POWER,Math.min(((eEnergy=e.getEnergy())/3.0),TARGET_DISTANCE/(eDistance = v0)));
			bPower = Math.min((eEnergy = e.getEnergy()) / 3, ((eDistance = v0) <= 100) ? 3.0 : ((v0 <= 500) ? 1.75 : 0.3));

			// if (eEnergy < getEnergy() && getOthers() == 1) bPower = 0.1;
			if (getGunTurnRemaining() == 0) setFire(bPower);
			if (getGunHeat() < RADAR_GUNLOCK || getOthers() == 1) setTurnRadarRightRadians(Double.POSITIVE_INFINITY
					* Utils.normalRelativeAngle(rM - getRadarHeadingRadians()));

			rM = Double.MAX_VALUE;
			v0 = i = 0;

			// boolean isClose = false;
			while ((v0 += DELTA_RISK_ANGLE) <= PI_360)
			{
				if ((++i * (18.0 - 2.7 * bPower) < Math.hypot(xg, yg)))
				{
					if (!new Rectangle2D.Double(WZ_G, WZ_G, WZ_G_SIZE_W, WZ_G_SIZE_H).contains((xg += (Math.sin(h1) * v2)) + getX(),
							(yg += (Math.cos(h1) * v2)) + getY()))
					{
						v2 = -v2;
					}
					h1 += h0;
				}

				if (new Rectangle2D.Double(WZ, WZ, WZ_SIZE_W, WZ_SIZE_H).contains((x = (DIST * Math.sin(v0))) + getX(), (y = (DIST * Math.cos(v0)))
						+ getY()))
				{
					if (((r1 = Math.abs(Math.cos(Math.atan2(enemy.x - x, enemy.y - y) - v0))) < DEFAULT_RANDOM_RATE && getOthers() <= 5))
					{
						r1 = DEFAULT_RANDOM_RATE * Math.random();
					}

					try
					{
						Iterator<WallabyTarget> iter = allTargets.values().iterator();
						while (true)
						{
							// WallabyTarget target;
							// if ( Math.hypot((target=iter.next()).x,target.y) < CLOSE_DIST) isClose = true;
							r1 += TARGET_FORCE / ((iter.next()).distanceSq(x, y));
						}
					}
					catch (Exception e1)
					{}

					if (r1 < rM)
					{
						rM = r1;
						v1 = v0;
					}
				}
			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(xg, yg) - getGunHeadingRadians()));
			if (Math.abs(getDistanceRemaining()) <= DIST_REMAIN || rM > 9)
			{
				setTurnRightRadians(Math.tan(v1 -= getHeadingRadians()));
				setAhead(DIST * Math.cos(v1));
			}
		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e)
	{
		eDistance = eEnergy = Double.POSITIVE_INFINITY;
		allTargets.remove(e.getName());
	}

	// public void onBulletHit(BulletHitEvent e)
	// {
	// allTargets.get(e.getName()).eScore += Rules.getBulletDamage(e.getBullet().getPower());
	// //eEnergy -= dmg;
	// }

}

class WallabyTarget extends Point2D.Double
{
	private static final long	serialVersionUID	= -5406737205536713408L;

	double						eHeading;
	double						vAvg;
	long						avgCount;
	double						eVelocity;

	double						eEnergy;

	// double eScore;
}
