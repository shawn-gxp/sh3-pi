using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class NoticeListRequestModel : ModelBase
{
	private string _igUserId;

	private int _count;

	[JsonProperty("igUserId")]
	public string IgUserId
	{
		get
		{
			return _igUserId;
		}
		set
		{
			if (!(_igUserId == value))
			{
				_igUserId = value;
				NotifyOfPropertyChange(() => IgUserId);
			}
		}
	}

	[JsonProperty("count")]
	public int Count
	{
		get
		{
			return _count;
		}
		set
		{
			if (_count != value)
			{
				_count = value;
				NotifyOfPropertyChange(() => Count);
			}
		}
	}
}
