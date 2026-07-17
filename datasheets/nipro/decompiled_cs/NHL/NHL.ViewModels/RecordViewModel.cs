using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Common;
using NHL.Models.Entity;
using NHL.Services;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class RecordViewModel : ViewModelBase, IHandle<HomeMasterDetailRootOnActivateEvent>, IHandle, IHandle<PresentedMenuEvent>
{
	private bool _isPresentedMenu;

	private BindableCollection<Measurement> _allMeasurementResultModelList;

	private bool _isiPad;

	public IMeasurementService MeasurementService { get; set; }

	public bool IsPresentedMenu
	{
		get
		{
			return _isPresentedMenu;
		}
		set
		{
			_isPresentedMenu = value;
			NotifyOfPropertyChange(() => IsPresentedMenu);
		}
	}

	public bool IsiPad
	{
		get
		{
			return _isiPad;
		}
		set
		{
			_isiPad = value;
			NotifyOfPropertyChange(() => IsiPad);
		}
	}

	public BindableCollection<Measurement> AllMeasurementResultModelList
	{
		get
		{
			return _allMeasurementResultModelList;
		}
		set
		{
			_allMeasurementResultModelList = value;
			NotifyOfPropertyChange(() => AllMeasurementResultModelList);
		}
	}

	public RecordViewModel()
	{
		IsiPad = NHL.Common.Common.IsiPad();
	}

	public async void Initialize()
	{
		base.EventAggregator.Unsubscribe(this);
		base.EventAggregator.Subscribe(this);
		await RefreshMeasurementResult();
	}

	public void RecordStepButtonTapped()
	{
		base.NavigationService.For<RecordEditViewModel>().WithParam((RecordEditViewModel x) => x.AllMeasurementList, AllMeasurementResultModelList).WithParam((RecordEditViewModel x) => x.SelectedMeasurementAt, DateTime.Today)
			.WithParam((RecordEditViewModel x) => x.IsEnableMeasurementAt, value: true)
			.Navigate();
	}

	public async Task RefreshMeasurementResult()
	{
		await ExecAsync(async delegate
		{
			AllMeasurementResultModelList = new BindableCollection<Measurement>();
			BindableCollection<Measurement> bindableCollection = await MeasurementService.GetAllMeasurement();
			if (bindableCollection != null && bindableCollection.Count > 0)
			{
				AllMeasurementResultModelList = bindableCollection;
			}
		});
	}

	void IHandle<PresentedMenuEvent>.Handle(PresentedMenuEvent message)
	{
		IsPresentedMenu = message.IsPresented;
	}

	void IHandle<HomeMasterDetailRootOnActivateEvent>.Handle(HomeMasterDetailRootOnActivateEvent message)
	{
		OnActivate();
	}
}
