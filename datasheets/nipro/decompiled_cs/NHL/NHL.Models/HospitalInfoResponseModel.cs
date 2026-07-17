using System.Collections.Generic;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class HospitalInfoResponseModel : ModelBase
{
	private HospitalModel _hospital;

	private IEnumerable<HospitalShareSettingModel> _hospitalShareSettings;

	private IEnumerable<IgUserHospitalShareItemModel> _igUserHospitalShareItems;

	[JsonProperty("hospital")]
	public HospitalModel Hospital
	{
		get
		{
			return _hospital;
		}
		set
		{
			if (_hospital != value)
			{
				_hospital = value;
				NotifyOfPropertyChange(() => Hospital);
			}
		}
	}

	[JsonProperty("hospitalShareSettings")]
	public IEnumerable<HospitalShareSettingModel> HospitalShareSettings
	{
		get
		{
			return _hospitalShareSettings;
		}
		set
		{
			_hospitalShareSettings = value;
			NotifyOfPropertyChange(() => HospitalShareSettings);
		}
	}

	[JsonProperty("igUserHospitalShareItems")]
	public IEnumerable<IgUserHospitalShareItemModel> IgUserHospitalShareItems
	{
		get
		{
			return _igUserHospitalShareItems;
		}
		set
		{
			_igUserHospitalShareItems = value;
			NotifyOfPropertyChange(() => IgUserHospitalShareItems);
		}
	}
}
