package wompi.dingo;

public class DingoInactivity
{
	private boolean	isInactive;
	private long	iStartTime;

	public void setInactivity(long time)
	{
		iStartTime = time;
	}

	public void resetInactivity()
	{
		iStartTime = 0;

	}

	public boolean isInactive()
	{
		return isInactive;
	}
}
