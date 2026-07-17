using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class NoticeDetailRequestModel : ModelBase
{
	private string _noticeId;

	[JsonProperty("noticeId")]
	public string NoticeId
	{
		get
		{
			return _noticeId;
		}
		set
		{
			if (!(_noticeId == value))
			{
				_noticeId = value;
				NotifyOfPropertyChange(() => NoticeId);
			}
		}
	}
}
