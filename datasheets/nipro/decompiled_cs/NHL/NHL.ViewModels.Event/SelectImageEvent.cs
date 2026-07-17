using System;
using Xamarin.Forms;

namespace NHL.ViewModels.Event;

public class SelectImageEvent
{
	public ImageSource ImageSource { get; set; }

	public DateTime? Date { get; set; }

	public string Test { get; set; }
}
