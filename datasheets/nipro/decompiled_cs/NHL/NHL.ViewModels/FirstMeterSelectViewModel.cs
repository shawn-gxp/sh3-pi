using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class FirstMeterSelectViewModel : ViewModelBase
{
	private bool _temperature;

	private bool _compositionMeter;

	private bool _sphygmomanometer;

	private bool _glucose;

	public bool Temperature
	{
		get
		{
			return _temperature;
		}
		set
		{
			_temperature = value;
			NotifyOfPropertyChange(() => Temperature);
		}
	}

	public bool CompositionMeter
	{
		get
		{
			return _compositionMeter;
		}
		set
		{
			_compositionMeter = value;
			NotifyOfPropertyChange(() => CompositionMeter);
		}
	}

	public bool Sphygmomanometer
	{
		get
		{
			return _sphygmomanometer;
		}
		set
		{
			_sphygmomanometer = value;
			NotifyOfPropertyChange(() => Sphygmomanometer);
		}
	}

	public bool Glucose
	{
		get
		{
			return _glucose;
		}
		set
		{
			_glucose = value;
			NotifyOfPropertyChange(() => Glucose);
		}
	}

	protected override void OnActivate()
	{
		Temperature = (bool)Application.Current.Properties["firstMeterSelectTemperature"];
		CompositionMeter = (bool)Application.Current.Properties["firstMeterSelectCompositionMeter"];
		Sphygmomanometer = (bool)Application.Current.Properties["firstMeterSelectSphygmomanometer"];
		Glucose = (bool)Application.Current.Properties["firstMeterSelectGlucose"];
		base.OnActivate();
	}

	public async void NextButton()
	{
		Application.Current.Properties["firstMeterSelectTemperature"] = Temperature;
		Application.Current.Properties["firstMeterSelectCompositionMeter"] = CompositionMeter;
		Application.Current.Properties["firstMeterSelectSphygmomanometer"] = Sphygmomanometer;
		Application.Current.Properties["firstMeterSelectGlucose"] = Glucose;
		await Navigate<UserInfoRegistViewModel>();
	}
}
