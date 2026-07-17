using System;

namespace NHL.ViewModels.Event;

public class TabChangeEvent
{
	public int TabIndex { get; set; } = 1;

	public DateTime? RequestDate { get; set; }
}
