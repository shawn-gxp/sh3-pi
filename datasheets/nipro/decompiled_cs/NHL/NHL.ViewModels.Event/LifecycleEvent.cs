namespace NHL.ViewModels.Event;

public class LifecycleEvent
{
	public enum Status
	{
		Sleep,
		Resume
	}

	public Status State { get; set; }
}
