using System;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Models.Entity;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class ChangeTimeZoneViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private MeasurementResultModel measurementResult;

	private BindableCollection<TimeZoneModel> timezoneList;

	private TimeZoneModel selectedTimeZone;

	private BindableCollection<TimeZoneModel> timezoneOtherList;

	private TimeZoneModel selectedTimeZoneOther;

	private bool flgList = true;

	public IMeasurementService MeasurementService { get; set; }

	public MeasurementResultModel MeasurementResult
	{
		get
		{
			return measurementResult;
		}
		set
		{
			if (measurementResult != value)
			{
				measurementResult = value;
				NotifyOfPropertyChange(() => MeasurementResult);
			}
		}
	}

	public BindableCollection<TimeZoneModel> TimezoneList
	{
		get
		{
			return timezoneList;
		}
		set
		{
			if (timezoneList != value)
			{
				timezoneList = value;
				NotifyOfPropertyChange(() => TimezoneList);
			}
		}
	}

	public TimeZoneModel SelectedTimeZone
	{
		get
		{
			return selectedTimeZone;
		}
		set
		{
			if (selectedTimeZone != value)
			{
				selectedTimeZone = value;
				NotifyOfPropertyChange(() => SelectedTimeZone);
			}
		}
	}

	public BindableCollection<TimeZoneModel> TimezoneOtherList
	{
		get
		{
			return timezoneOtherList;
		}
		set
		{
			if (timezoneOtherList != value)
			{
				timezoneOtherList = value;
				NotifyOfPropertyChange(() => TimezoneOtherList);
			}
		}
	}

	public TimeZoneModel SelectedTimeZoneOther
	{
		get
		{
			return selectedTimeZoneOther;
		}
		set
		{
			if (selectedTimeZoneOther != value)
			{
				selectedTimeZoneOther = value;
				NotifyOfPropertyChange(() => SelectedTimeZoneOther);
			}
		}
	}

	public bool FlgList
	{
		get
		{
			return flgList;
		}
		set
		{
			if (flgList != value)
			{
				flgList = value;
				NotifyOfPropertyChange(() => FlgList);
			}
		}
	}

	public async Task Save()
	{
		await ExecAsync(async delegate
		{
			switch (MeasurementResult.Source.MeasurementType)
			{
			case "01":
				MeasurementResult.Source.Timezone = SelectedTimeZone.Code;
				await MeasurementService.UpdateMeasurement(MeasurementResult.Source);
				break;
			case "03":
			{
				BindableCollection<Measurement> source = await MeasurementService.GetAllMeasurement();
				BindableCollection<Measurement> bindableCollection = new BindableCollection<Measurement>();
				bindableCollection.AddRange(source.Where(delegate(Measurement x)
				{
					DateTime? measurementAt = x.MeasurementAt;
					DateTime? measurementAt2 = measurementResult.Source.MeasurementAt;
					if (measurementAt.HasValue != measurementAt2.HasValue)
					{
						return false;
					}
					return !measurementAt.HasValue || measurementAt.GetValueOrDefault() == measurementAt2.GetValueOrDefault();
				}));
				foreach (Measurement item in bindableCollection)
				{
					item.Timezone = SelectedTimeZoneOther.Code;
					await MeasurementService.UpdateMeasurement(item);
				}
				break;
			}
			}
			await Task.Run(async delegate
			{
				Log.Info("【IG】【ChangeTimeZoneViewModel】【Save】Sync start");
				await MeasurementService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
		});
		base.EventAggregator.PublishOnUIThread(new UpdateMeasurementEvent());
		base.EventAggregator.PublishOnUIThread(new HomeUpdateMeasurementEvent());
	}

	protected override void OnInitialize()
	{
		TimezoneList = CreateTimezoneList();
		TimezoneOtherList = CreateTimezoneOtherList();
		base.OnInitialize();
	}

	protected override void OnActivate()
	{
		if (MeasurementResult.Source.MeasurementType == "01")
		{
			SelectedTimeZone = TimezoneList.FirstOrDefault((TimeZoneModel x) => x.Code == MeasurementResult.TimezoneType);
			FlgList = true;
		}
		if (MeasurementResult.Source.MeasurementType == "03")
		{
			SelectedTimeZoneOther = TimezoneOtherList.FirstOrDefault((TimeZoneModel x) => x.Code == MeasurementResult.TimezoneType);
			FlgList = false;
		}
		base.OnActivate();
	}

	protected override async void OnDeactivate(bool close)
	{
		await Save();
		base.OnDeactivate(close);
	}

	private BindableCollection<TimeZoneModel> CreateTimezoneList()
	{
		return new BindableCollection<TimeZoneModel>
		{
			new TimeZoneModel
			{
				Code = "01",
				Name = "朝食前"
			},
			new TimeZoneModel
			{
				Code = "02",
				Name = "朝食後"
			},
			new TimeZoneModel
			{
				Code = "03",
				Name = "昼食前"
			},
			new TimeZoneModel
			{
				Code = "04",
				Name = "昼食後"
			},
			new TimeZoneModel
			{
				Code = "05",
				Name = "夕食前"
			},
			new TimeZoneModel
			{
				Code = "06",
				Name = "夕食後"
			},
			new TimeZoneModel
			{
				Code = "07",
				Name = "就寝前"
			},
			new TimeZoneModel
			{
				Code = "08",
				Name = "深夜"
			}
		};
	}

	private BindableCollection<TimeZoneModel> CreateTimezoneOtherList()
	{
		return new BindableCollection<TimeZoneModel>
		{
			new TimeZoneModel
			{
				Code = "01",
				Name = "朝"
			},
			new TimeZoneModel
			{
				Code = "02",
				Name = "晩"
			}
		};
	}
}
