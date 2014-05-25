package wompi.robotcontrol;

public interface IRobotTurn
{
	public void setRadarTurnAmount(double turnAmount, String turnSource);

	public void setGunTurnAmount(double turnAmount, String turnSource);

	public void setBodyTurnAmount(double turnAmount, String turnSource);
}
