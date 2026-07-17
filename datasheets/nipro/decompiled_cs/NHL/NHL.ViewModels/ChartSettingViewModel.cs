using Caliburn.Micro;
using NHL.Models;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class ChartSettingViewModel : ViewModelBase
{
	private BindableCollection<ChartSettingModel> _chartSettings;

	public BindableCollection<ChartSettingModel> ChartSettings
	{
		get
		{
			return _chartSettings;
		}
		set
		{
			_chartSettings = value;
			NotifyOfPropertyChange(() => ChartSettings);
		}
	}

	public void Initialize()
	{
		base.EventAggregator.Unsubscribe(this);
		base.EventAggregator.Subscribe(this);
		base.OnInitialize();
	}

	protected override void OnActivate()
	{
		base.OnActivate();
	}

	protected override void OnDeactivate(bool close)
	{
		IoC.Get<ChartContext>().ChartSettings = ChartSettings;
		base.OnDeactivate(close);
	}
}
