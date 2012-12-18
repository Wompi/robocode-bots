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
package wompi.robomath;

import java.awt.geom.Point2D;

import robocode.Rules;

public class RobotMath
{
	public static double calculateAngleDifference(double start, double end)
	{
		// mist es ist wichtig wierum die winkle angegeben werden ansonsten kommt immer der entgegengesetzte Winke raus
		// dafuer brauch ich noch eine Abfrage weil ich mit sicherheit nicht immer nachsehen will welcher winkle nun von wo und wie kommt
		// ich will doch nur die differenz und die kuerzeste Richtung um auf den zu kommen
		// hmm eigentich stimmt das schon so. das Problem ist eher bei Anzeigen zu sehen glaub ich, wel man dort angegen muss von wo es losgeht und
		// wies endet
		//
		// die Reihenfolge der Winkelangabe start und end ist wichtig fuer folgende Operationen, weil dadurch das vorzeichen fuer die drehung
		// angegeben wird
		// es macht einne unterschied ob ich mich + um start oder um den ednwinkle drehe dann. also aufpassen damit

		double diff = end - start;
		double delta = Math.abs(diff);
		if (delta > Math.PI)
		{
			// das hat ewig gedauert eh ich das raushatte ... wenn der diff winkel groesser 180grad ist veraendert sich das vorzeichen der
			// urspruenglichen Differenz ... dieses Vorzeichen wird dann mit der Differenz aus 360-|diff| verechnet
			// der generelle Ansatz zum finden von winkeldifferenzen ist min(|diff|,360-|diff|) da dabei aber die richtung verloren geht muss diese
			// hier noch
			// mit angegeben werden ... eventuell gehts auch ohne signum mit ner if abrage aber so versteh ichs besser
			diff = -Math.signum(diff) * (2 * Math.PI - delta);
		}
		return diff;
	}

	public static double limit(double min, double value, double max)
	{
		return Math.max(min, Math.min(value, max));
	}

	public static double limit(double minmax, double value)
	{
		return Math.max(-minmax, Math.min(value, minmax));
	}

	public static double getAcceleration(double velocity, double lastVelocity)
	{
		double sig = Math.signum(velocity);
		double lastSig = Math.signum(lastVelocity);
		if (sig == 0)
		{
			return (lastVelocity > 0) ? -lastVelocity : lastVelocity;
		}
		else if (lastSig == 0)
		{
			return Math.abs(velocity);
		}
		else if (Math.signum(lastVelocity) == sig)
		{
			sig = -sig;
		}
		return ((lastVelocity - velocity) * sig);
	}

	/**
	 * - gibt den Winkel zwischen zwei kartesischen Koordinaten zurueck<br>
	 * - unbedingt die Reihenfolge fuer P1 und P2 beachten. P1 sollten meine Koordinaten sein und P2 die Koordinaten des gesuchten Punktes. <br>
	 * <br>
	 * - Anmerkung: normalerweise mueste die Funktion mit atan2(dY,dX), um den Winkel fuer die robocode Standardansicht zu bekommen werden die dy und
	 * dx einfach vertauscht. Siehe dazu Robocode_Tutorial_1[1].4 dort wird das beschrieben
	 * 
	 * @param point2
	 *            - Zielpunkt
	 * @param point1
	 *            - Ursprungspunkt
	 * @return Winkel zwischen P1 und P2 (radiant) (-180 < A < 180 grad) damit ist es ein relativer Winkel in Robocode
	 */
	public static double calculateAngle(Point2D point1, Point2D point2)
	{
		return Math.atan2(point2.getX() - point1.getX(), point2.getY() - point1.getY());
	}

	/**
	 * - gibt Polarkoordinaten zu einem Winkel und der Entfernung zurueck <br>
	 * <br>
	 * - Anmerkung: Beachte das x:sin() und y:cos(). Robocode gibt Winkel im Uhrzeigersinn aus. Normalerweise werden Polarkoordinaten gegen den
	 * Uhrzeigersinn angegeben.
	 * 
	 * @param angle
	 *            - Winkel zum gesuchten Punkt (radiant)
	 * @param distance
	 *            - Entfernung zum gesuchten Punkt
	 * @param start
	 *            - Der Punkt von welchem aus der Winkle und die Enfernung gemessen wurden
	 * @return - Polarkoordinaten des gesuchten Punktes
	 */
	public static Point2D calculatePolarPoint(double angle, double distance, Point2D start)
	{
		double _x = start.getX() + Math.sin(angle) * distance;
		double _y = start.getY() + Math.cos(angle) * distance;
		return new Point2D.Double(_x, _y);
	}

	/**
	 * simple distance
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double calculateDistance(double x1, double y1, double x2, double y2)
	{
		double xo = x2 - x1;
		double yo = y2 - y1;
		return Math.sqrt(xo * xo + yo * yo);
	}

	/**
	 * - Dies ist die Geschwindigkeit mit welcher sich das Target rechtwinklig zu meiner Position bewegt <br>
	 * 
	 * @param myHeading
	 *            - Blickrichtung des Robots <b>(radant)</b>
	 * @param target
	 *            - Das Ziel von welchem ich die laterale Geschwindigkeit haben moechte
	 * @return Geschwindigkeit des Ziels rechtwinklig zu mir
	 */
	public static double calculateLateralVelocity(double myHeading, double tBearing, double tHeading, double tVelo)
	{
		return (tVelo * Math.sin(tHeading - (tBearing + myHeading)));
	}

	/**
	 * - Dies berechnet die Geschwindigkeit des Ziels parallel zu meiner Blickrichtung <br>
	 * 
	 * @param myHeading
	 *            - Blickrichtung meines Robots <b>(radiant)</b>
	 * @param target
	 *            - Das Ziel von welchem ich die advancing Geschwindigeit haben moechte
	 * @return Gewschindigkeit des Ziels parallel zu meiner Blickrichtung
	 */
	public static double calculateAdvancingVelocity(double myHeading, double tBearing, double tHeading, double tVelo)
	{
		return (tVelo * -1 * Math.cos(tHeading - (tBearing + myHeading)));
	}

	/**
	 * - Gibt die Beschleunigung des Targets an. Berechnet auf die Geschwindigeit zum Scan
	 * 
	 * @see FloodMini.java
	 * @param lastVelocity
	 * @param nowVelocity
	 * @return - Beschleunigung des Ziels <br>
	 *         - 0 -> keine beschleunigung - 1 -> beschleunigt - 2 -> bremmst ab
	 */
	public static int calculateAbsoluteAcceleration(double lastVelocity, double nowVelocity)
	{
		int result = (int) Math.round(Math.abs(lastVelocity) - Math.abs(nowVelocity));
		if (result != 0) result = (result < 0) ? 1 : 2;
		return result;
	}

	/**
	 * - berechnet die Flugzeit einer Kugel ueber eine gesuchte Distance bezogen auf ihre Power <br>
	 * - Dies kann dazu verwendet werden um zu berechen wann die Kugel ein Ziel trefen wuerde <br>
	 * - der Rueckgabewert wird in ganzen Turns angegeben und bedeutet das die Kugel im Rueckgabewert + 1 treffen wuerde da die Kugeln vor der
	 * Bewewegungsphase des Gegners berechnet werden <br>
	 * - Anmerkung: Ausserdem ist bei sehr kurzen Entfernungen der Rueckgabewert sehr klein<br>
	 * Default Flugweite pro Turn fuer min/max Power: 0.1 = 19.7 , 3.0 = 11
	 * 
	 * @param distance
	 *            - Entfernung die die Kugel zurecklegen muesste um ihr Ziel zu erreichen
	 * @param bulletPower
	 *            - Das Kaliber der Kugel (Power).<br>
	 *            - Wenn dieser Wert groesser als {@link Rules.MAX_BULLET_POWER} ist wird der Wert darauf angepasst.<br>
	 *            - Genauso wird der Wert angepasst wenn er unter {@link Rules.MIN_BULLET_POWER} ist.
	 * @return Ticks bis die Kugel treffen wuerde
	 */
	public static int calculateBulletFlightTime(double distance, double bulletPower)
	{
		if (bulletPower > Rules.MAX_BULLET_POWER) bulletPower = Rules.MAX_BULLET_POWER;
		if (bulletPower < Rules.MIN_BULLET_POWER) bulletPower = Rules.MIN_BULLET_POWER;
		return (int) (distance / Rules.getBulletSpeed(bulletPower));
	}

	public static double getSimpleEscapeAngle(double bSpeed)
	{
		return Math.asin(Rules.MAX_VELOCITY / bSpeed);
	}

	/**
	 * TODO: nochmal drueber nachdenken - die Beschleunigung des Targets berechnet auf die Bearing-Differencen <br>
	 * - es gibt noch einen anderen Ansatz ueber den Unterschied der Beschleunigung - dies muss noch verglichen werden - eventuell ist dies hier die
	 * absolute beschleunigung oder sowas ...
	 * 
	 * @param deltaBearing
	 * @param oldDeltaBearing
	 * @return
	 */
	// public static int calculateAcceleration(double deltaBearing, double oldDeltaBearing)
	// {
	// int delta = (int)(Math.round(5 * enemyDistance * (Math.abs(deltaBearing) - Math.abs(oldDeltaBearing))));
	// if (delta < 0) {
	// return 0;
	// }
	// else if (delta > 0) {
	// return 2;
	// }
	// return 1;
	// }
}
