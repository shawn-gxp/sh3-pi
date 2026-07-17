namespace NHL.ViewModels.Event;

public class LocationRequestEvent
{
	public enum LocationRequestStatus
	{
		Completed,
		Canceled
	}

	public LocationRequestStatus Status { get; set; }
}
