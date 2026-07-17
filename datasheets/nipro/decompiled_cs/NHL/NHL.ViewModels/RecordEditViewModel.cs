using System;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models.Entity;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class RecordEditViewModel : ViewModelBase
{
	private readonly ILoggingService _log = DependencyService.Get<ILoggingService>();

	private BindableCollection<Measurement> _allMeasurementList;

	private BindableCollection<Measurement> _allStepMeasurementList;

	private bool _isEnableMeasurementAt;

	private bool _isEdited;

	private DateTime _selectedMeasurementAt;

	private DateTime? _measurementAt;

	private string _measurementValue;

	private Measurement _targetMeasurementModel;

	public IMeasurementService MeasurementService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public MeasurementResultViewModel MeasurementResultViewModel { get; set; }

	public HomeViewModel HomeViewModel { get; set; }

	public RecordViewModel RecordViewModel { get; set; }

	public MasterDetailRootViewModel MasterDetailRootViewModel { get; set; }

	public BindableCollection<Measurement> AllMeasurementList
	{
		get
		{
			return _allMeasurementList;
		}
		set
		{
			if (_allMeasurementList != value)
			{
				_allMeasurementList = value;
				NotifyOfPropertyChange(() => AllMeasurementList);
			}
		}
	}

	public bool IsEnableMeasurementAt
	{
		get
		{
			return _isEnableMeasurementAt;
		}
		set
		{
			if (_isEnableMeasurementAt != value)
			{
				_isEnableMeasurementAt = value;
				NotifyOfPropertyChange(() => IsEnableMeasurementAt);
			}
		}
	}

	public bool IsEdited
	{
		get
		{
			return _isEdited;
		}
		set
		{
			if (_isEdited != value)
			{
				_isEdited = value;
				NotifyOfPropertyChange(() => IsEdited);
			}
		}
	}

	public DateTime SelectedMeasurementAt
	{
		get
		{
			return _selectedMeasurementAt;
		}
		set
		{
			if (!(_selectedMeasurementAt == value))
			{
				_selectedMeasurementAt = value;
				NotifyOfPropertyChange(() => SelectedMeasurementAt);
			}
		}
	}

	public DateTime? MeasurementAt
	{
		get
		{
			return _measurementAt;
		}
		set
		{
			if (!(_measurementAt == value))
			{
				_measurementAt = value;
				NotifyOfPropertyChange(() => MeasurementAt);
				SetTargetMeasurementModel();
			}
		}
	}

	public string MeasurementValue
	{
		get
		{
			return _measurementValue;
		}
		set
		{
			if (!(_measurementValue == value))
			{
				_measurementValue = value;
				NotifyOfPropertyChange(() => MeasurementValue);
			}
		}
	}

	public Measurement TargetMeasurementModel
	{
		get
		{
			return _targetMeasurementModel;
		}
		set
		{
			if (_targetMeasurementModel != value)
			{
				_targetMeasurementModel = value;
				NotifyOfPropertyChange(() => TargetMeasurementModel);
			}
		}
	}

	protected override void OnActivate()
	{
		_allStepMeasurementList = new BindableCollection<Measurement>();
		if (AllMeasurementList != null && AllMeasurementList.Count > 0)
		{
			_allStepMeasurementList.AddRange(AllMeasurementList.Where((Measurement x) => x.MeasurementType == "15"));
		}
		MeasurementAt = new DateTime(1, 1, 1, 0, 0, 0);
		Execute.BeginOnUIThread(async delegate
		{
			await Task.Delay(10);
			MeasurementAt = SelectedMeasurementAt;
		});
		base.OnActivate();
	}

	protected override void OnDeactivate(bool close)
	{
		base.OnDeactivate(close);
		AllMeasurementList = new BindableCollection<Measurement>();
		MeasurementAt = null;
		MeasurementValue = string.Empty;
	}

	public RecordEditViewModel()
	{
		MasterDetailRootViewModel = IoC.Get<MasterDetailRootViewModel>();
		HomeViewModel = IoC.Get<HomeViewModel>();
		MeasurementResultViewModel = IoC.Get<MeasurementResultViewModel>();
	}

	private void SetTargetMeasurementModel()
	{
		if (_allStepMeasurementList == null)
		{
			_allStepMeasurementList = new BindableCollection<Measurement>();
		}
		Measurement measurement = _allStepMeasurementList.LastOrDefault(delegate(Measurement x)
		{
			DateTime? measurementAt = x.MeasurementAt;
			DateTime? measurementAt2 = MeasurementAt;
			if (measurementAt.HasValue != measurementAt2.HasValue)
			{
				return false;
			}
			return !measurementAt.HasValue || measurementAt.GetValueOrDefault() == measurementAt2.GetValueOrDefault();
		});
		if (measurement != null)
		{
			TargetMeasurementModel = measurement;
			MeasurementValue = measurement.MeasurementValue.ToString();
			IsEdited = true;
			return;
		}
		TargetMeasurementModel = new Measurement
		{
			IgUserId = base.UserManager.IgUser.Id,
			MeasurementType = "15",
			TimezoneDate = MeasurementAt,
			ExceedLimitType = "0"
		};
		MeasurementValue = string.Empty;
		IsEdited = false;
	}

	public async void OnRegisterTapped()
	{
		if (!(await IsValid()))
		{
			return;
		}
		await ExecAsync(async delegate
		{
			DateTime dateTime = DateTime.Parse(MeasurementAt.ToString());
			TargetMeasurementModel.MeasurementAt = new DateTime(dateTime.Year, dateTime.Month, dateTime.Day, 0, 0, 0);
			TargetMeasurementModel.MeasurementValue = double.Parse(MeasurementValue);
			if (IsEdited)
			{
				MeasurementService.UpdateMeasurement(TargetMeasurementModel);
			}
			else
			{
				MeasurementService.RegisterMeasurement(TargetMeasurementModel);
			}
			await Task.Run(async delegate
			{
				_log.Info("【IG】【RecordEditViewModel】【Save】Sync start");
				await MeasurementService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
			GoToMeasurementResult();
		});
	}

	public async void OnDeleteTapped()
	{
		if (await DialogProvider.ShowAlert("【確認】", "歩数データを削除しますか？", "はい", "キャンセル", null))
		{
			await MeasurementService.DeleteMeasurement(TargetMeasurementModel);
			await Task.Run(async delegate
			{
				_log.Info("【IG】【RecordEditViewModel】【Delete】Sync start");
				await MeasurementService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
			GoToMeasurementResult();
		}
	}

	private void GoToMeasurementResult()
	{
		_log.Info("【IG】【RecordEditViewModel】【GoToMeasurementResult】MeasurementResult(" + MeasurementAt.ToString() + ") - 測定結果表示");
		Task.Run(delegate
		{
			Execute.OnUIThread(delegate
			{
				MasterDetailRootViewModel.MeasurementResult(MeasurementAt);
				MasterDetailRootViewModel.DisplayName = "測定結果";
			});
		}).ConfigureAwait(continueOnCapturedContext: false);
	}

	private async Task<bool> IsValid()
	{
		if (!MeasurementAt.HasValue)
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "測定日を入力してください");
			return false;
		}
		DateTime dateTime = DateTime.Today + TimeSpan.FromDays(1.0);
		_ = dateTime.Date;
		if (MeasurementAt >= new DateTime(dateTime.Year, dateTime.Month, dateTime.Day, 0, 0, 0))
		{
			await DialogProvider.ShowAlert("書式チェックエラー", "測定日が無効です");
			return false;
		}
		if (string.IsNullOrEmpty(MeasurementValue))
		{
			await DialogProvider.ShowAlert("必須チェックエラー", "測定値を入力してください");
			return false;
		}
		if (!string.IsNullOrEmpty(MeasurementValue) && (!int.TryParse(MeasurementValue, out var result) || result > 999999 || result < 0))
		{
			await DialogProvider.ShowAlert("書式チェックエラー", "測定値が無効です");
			return false;
		}
		return true;
	}
}
