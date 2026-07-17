using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class IgUserHospitalShareItemModel : ModelBase
{
	private string _itemId;

	private string _value;

	[JsonProperty("itemId")]
	public string ItemId
	{
		get
		{
			return _itemId;
		}
		set
		{
			if (!(_itemId == value))
			{
				_itemId = value;
				NotifyOfPropertyChange(() => ItemId);
			}
		}
	}

	[JsonProperty("value")]
	public string Value
	{
		get
		{
			return _value;
		}
		set
		{
			if (!(_value == value))
			{
				_value = value;
				NotifyOfPropertyChange(() => Value);
			}
		}
	}
}
