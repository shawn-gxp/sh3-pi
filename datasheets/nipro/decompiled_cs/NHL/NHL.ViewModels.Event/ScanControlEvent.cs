namespace NHL.ViewModels.Event;

public class ScanControlEvent
{
	public enum CommandType
	{
		StartScan,
		StopScan,
		Restart
	}

	public CommandType Command { get; set; }
}
