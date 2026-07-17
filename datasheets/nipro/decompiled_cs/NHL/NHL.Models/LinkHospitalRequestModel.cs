using System.Collections.Generic;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class LinkHospitalRequestModel : ModelBase
{
	private string _hospitalId;

	private IEnumerable<IgUserHospitalShareItemModel> _igUserHospitalShareItemDtoList;

	[JsonProperty("hospitalId")]
	public string HospitalId
	{
		get
		{
			return _hospitalId;
		}
		set
		{
			if (!(_hospitalId == value))
			{
				_hospitalId = value;
				NotifyOfPropertyChange(() => HospitalId);
			}
		}
	}

	[JsonProperty("igUserHospitalShareItemDtoList")]
	public IEnumerable<IgUserHospitalShareItemModel> IgUserHospitalShareItemDtoList
	{
		get
		{
			return _igUserHospitalShareItemDtoList;
		}
		set
		{
			if (_igUserHospitalShareItemDtoList != value)
			{
				_igUserHospitalShareItemDtoList = value;
				NotifyOfPropertyChange(() => IgUserHospitalShareItemDtoList);
			}
		}
	}
}
