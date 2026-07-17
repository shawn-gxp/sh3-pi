using System.Collections.Generic;

namespace NHL.ViewModels.Support;

public class ReceiveItem
{
	public enum ReceiveStatus
	{
		Wait,
		Start,
		Complete,
		NoData,
		Stop,
		Timeout,
		Error,
		ConnectionLost,
		None
	}

	public ReceiveStatus Status { get; set; } = ReceiveStatus.None;

	public string Name { get; set; }

	public IList<string> ResultString { get; set; } = new List<string>();
}
