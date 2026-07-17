using Xamarin.Forms;

namespace NHL.Views.Renderer;

public class TimeZoneOtherPicker : Picker
{
	public static readonly BindableProperty SelectedValueProperty = BindableProperty.Create("SelectedValue", typeof(string), typeof(TimeZoneOtherPicker), null, BindingMode.TwoWay);

	public string SelectedValue
	{
		get
		{
			return (string)GetValue(SelectedValueProperty);
		}
		set
		{
			SetValue(SelectedValueProperty, value);
		}
	}

	public new int FontSize { get; set; } = 22;
}
