using System.Threading.Tasks;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class TermsOfServiceViewModel : ViewModelBase
{
	private string contentUrl;

	private bool _IsNavigating = true;

	private bool _IsConfirmButtonEnable;

	public string ContentUrl
	{
		get
		{
			return contentUrl;
		}
		set
		{
			contentUrl = value;
			NotifyOfPropertyChange(() => ContentUrl);
		}
	}

	public bool IsNavigating
	{
		get
		{
			return _IsNavigating;
		}
		set
		{
			_IsNavigating = value;
			NotifyOfPropertyChange(() => IsNavigating);
		}
	}

	public bool IsConfirmButtonEnable
	{
		get
		{
			return _IsConfirmButtonEnable;
		}
		set
		{
			_IsConfirmButtonEnable = value;
			NotifyOfPropertyChange(() => IsConfirmButtonEnable);
		}
	}

	public async Task Agree()
	{
		Application.Current.Properties["firstMeterSelectTemperature"] = true;
		Application.Current.Properties["firstMeterSelectCompositionMeter"] = true;
		Application.Current.Properties["firstMeterSelectSphygmomanometer"] = true;
		Application.Current.Properties["firstMeterSelectGlucose"] = true;
		await Navigate<FirstMeterSelectViewModel>();
	}

	protected override void OnInitialize()
	{
		ContentUrl = "https://api.niprogenkinote.jp/TermsOfService.html";
		base.OnInitialize();
	}
}
