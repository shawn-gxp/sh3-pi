namespace NHL.ViewModels.Event;

public class CentralManagerEvent
{
	public enum CentralManagerState : long
	{
		Unknown,
		Resetting,
		Unsupported,
		Unauthorized,
		PoweredOff,
		PoweredOn
	}

	public CentralManagerState Status { get; set; }
}
