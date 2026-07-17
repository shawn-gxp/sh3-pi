using System;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models.Entity;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class CompositionMeterDetailViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private BindableCollection<Measurement> _compMeasurementResultModelList;

	private string _textWeight;

	private string _textBmi;

	private string _textFatPercentage;

	private string _textFatMass;

	private string _textMusclePercentage;

	private string _textMuscleMass;

	private string _textWaterPercentage;

	private string _textWaterMass;

	private string _textBasalMetabolism;

	private string _weight;

	private string _bmi;

	private string _fatPercentage;

	private string _fatMass;

	private string _musclePercentage;

	private string _muscleMass;

	private string _waterPercentage;

	private string _waterMass;

	private string _basalMetabolism;

	public MeasurementResultViewModel MeasurementResultViewModel { get; set; }

	public MasterDetailRootViewModel MasterDetailRootViewModel { get; set; }

	public IMeasurementService MeasurementService { get; set; }

	public BindableCollection<Measurement> CompMeasurementResultModelList
	{
		get
		{
			return _compMeasurementResultModelList;
		}
		set
		{
			_compMeasurementResultModelList = value;
			NotifyOfPropertyChange(() => CompMeasurementResultModelList);
		}
	}

	public string TextWeight
	{
		get
		{
			return _textWeight;
		}
		set
		{
			_textWeight = value;
			NotifyOfPropertyChange(() => TextWeight);
		}
	}

	public string TextBmi
	{
		get
		{
			return _textBmi;
		}
		set
		{
			_textBmi = value;
			NotifyOfPropertyChange(() => TextBmi);
		}
	}

	public string TextFatPercentage
	{
		get
		{
			return _textFatPercentage;
		}
		set
		{
			_textFatPercentage = value;
			NotifyOfPropertyChange(() => TextFatPercentage);
		}
	}

	public string TextFatMass
	{
		get
		{
			return _textFatMass;
		}
		set
		{
			_textFatMass = value;
			NotifyOfPropertyChange(() => TextFatMass);
		}
	}

	public string TextMusclePercentage
	{
		get
		{
			return _textMusclePercentage;
		}
		set
		{
			_textMusclePercentage = value;
			NotifyOfPropertyChange(() => TextMusclePercentage);
		}
	}

	public string TextMuscleMass
	{
		get
		{
			return _textMuscleMass;
		}
		set
		{
			_textMuscleMass = value;
			NotifyOfPropertyChange(() => TextMuscleMass);
		}
	}

	public string TextWaterPercentage
	{
		get
		{
			return _textWaterPercentage;
		}
		set
		{
			_textWaterPercentage = value;
			NotifyOfPropertyChange(() => TextWaterPercentage);
		}
	}

	public string TextWaterMass
	{
		get
		{
			return _textWaterMass;
		}
		set
		{
			_textWaterMass = value;
			NotifyOfPropertyChange(() => TextWaterMass);
		}
	}

	public string TextBasalMetabolism
	{
		get
		{
			return _textBasalMetabolism;
		}
		set
		{
			_textBasalMetabolism = value;
			NotifyOfPropertyChange(() => TextBasalMetabolism);
		}
	}

	public string Weight
	{
		get
		{
			return _weight;
		}
		set
		{
			_weight = value;
			NotifyOfPropertyChange(() => Weight);
		}
	}

	public string Bmi
	{
		get
		{
			return _bmi;
		}
		set
		{
			_bmi = value;
			NotifyOfPropertyChange(() => Bmi);
		}
	}

	public string FatPercentage
	{
		get
		{
			return _fatPercentage;
		}
		set
		{
			_fatPercentage = value;
			NotifyOfPropertyChange(() => FatPercentage);
		}
	}

	public string FatMass
	{
		get
		{
			return _fatMass;
		}
		set
		{
			_fatMass = value;
			NotifyOfPropertyChange(() => FatMass);
		}
	}

	public string MusclePercentage
	{
		get
		{
			return _musclePercentage;
		}
		set
		{
			_musclePercentage = value;
			NotifyOfPropertyChange(() => MusclePercentage);
		}
	}

	public string MuscleMass
	{
		get
		{
			return _muscleMass;
		}
		set
		{
			_muscleMass = value;
			NotifyOfPropertyChange(() => MuscleMass);
		}
	}

	public string WaterPercentage
	{
		get
		{
			return _waterPercentage;
		}
		set
		{
			_waterPercentage = value;
			NotifyOfPropertyChange(() => WaterPercentage);
		}
	}

	public string WaterMass
	{
		get
		{
			return _waterMass;
		}
		set
		{
			_waterMass = value;
			NotifyOfPropertyChange(() => WaterMass);
		}
	}

	public string BasalMetabolism
	{
		get
		{
			return _basalMetabolism;
		}
		set
		{
			_basalMetabolism = value;
			NotifyOfPropertyChange(() => BasalMetabolism);
		}
	}

	public CompositionMeterDetailViewModel()
	{
		MasterDetailRootViewModel = IoC.Get<MasterDetailRootViewModel>();
		MeasurementResultViewModel = IoC.Get<MeasurementResultViewModel>();
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
	}

	protected override void OnActivate()
	{
		TextWeight = "体重";
		TextBmi = "BMI";
		TextFatPercentage = "体脂肪率";
		TextFatMass = "体脂肪量";
		TextMusclePercentage = "筋肉量率";
		TextMuscleMass = "筋肉量";
		TextWaterPercentage = "体水分率";
		TextWaterMass = "体水分量";
		TextBasalMetabolism = "基礎代謝";
		Measurement measurement = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "06");
		Weight = FormatMeasurementValueUnit(measurement, (double? value) => value, (measurement != null && measurement.MeasurementValue >= 100.0) ? "F1" : "F2", "kg");
		Measurement measurement2 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "07");
		Bmi = FormatMeasurementValueUnit(measurement2, (double? value) => value, "F1", string.Empty);
		Measurement measurement3 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "08");
		FatPercentage = FormatMeasurementValueUnit(measurement3, (double? value) => value, "F1", "%");
		Measurement measurement4 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "12");
		FatMass = FormatMeasurementValueUnit(measurement4, (double? value) => value, string.Empty, "kg");
		Measurement measurement5 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "13");
		MusclePercentage = FormatMeasurementValueUnit(measurement5, (double? value) => value, string.Empty, "%");
		Measurement measurement6 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "09");
		MuscleMass = FormatMeasurementValueUnit(measurement6, (double? value) => value, "F1", "kg");
		Measurement measurement7 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "14");
		WaterPercentage = FormatMeasurementValueUnit(measurement7, (double? value) => value, "F1", "%");
		Measurement measurement8 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "11");
		WaterMass = FormatMeasurementValueUnit(measurement8, (double? value) => value, string.Empty, "kg");
		Measurement measurement9 = CompMeasurementResultModelList.FirstOrDefault((Measurement x) => x.MeasurementType == "10");
		BasalMetabolism = FormatMeasurementValueUnit(measurement9, FormatUtils.ConvertKjToKCal, "F0", "kcal/日");
		base.OnActivate();
	}

	private static string FormatMeasurementValueUnit(Measurement measurement, Func<double?, double?> convertFunc, string format, string unit)
	{
		if (measurement == null || !measurement.MeasurementValue.HasValue)
		{
			return "--";
		}
		double? num = convertFunc(measurement.MeasurementValue);
		return ((!(format != string.Empty)) ? num?.ToString() : num?.ToString(format)) + unit;
	}

	protected override void OnDeactivate(bool close)
	{
		Weight = "--";
		Bmi = "--";
		FatPercentage = "--";
		FatMass = "--";
		MusclePercentage = "--";
		MuscleMass = "--";
		WaterPercentage = "--";
		WaterMass = "--";
		BasalMetabolism = "--";
		base.OnDeactivate(close);
	}

	public async void Delete()
	{
		if (!(await base.DialogProvider.ShowAlert("【確認】", "測定結果を1件削除しますか？", "はい", "キャンセル", null)))
		{
			return;
		}
		foreach (Measurement compMeasurementResultModel in CompMeasurementResultModelList)
		{
			await MeasurementService.DeleteMeasurement(compMeasurementResultModel);
		}
		await Task.Run(async delegate
		{
			Log.Info("【IG】【CompositionMeterDetailViewModel】【Delete】Sync start");
			await MeasurementService.Sync();
		}).ConfigureAwait(continueOnCapturedContext: false);
		await Execute.OnUIThreadAsync(async delegate
		{
			MeasurementResultViewModel.RefreshMeasurementResult();
			MasterDetailRootViewModel.MeasurementResult(CompMeasurementResultModelList.First().TimezoneDate);
			MasterDetailRootViewModel.DisplayName = "測定結果";
		});
	}
}
