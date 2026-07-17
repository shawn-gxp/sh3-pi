using System.Collections.Generic;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class NoticeListResponseModel : ModelBase
{
	private IEnumerable<NoticeDetailResponseModel> _notices;

	[JsonProperty("notices")]
	public IEnumerable<NoticeDetailResponseModel> Notices
	{
		get
		{
			return _notices;
		}
		set
		{
			_notices = value;
			NotifyOfPropertyChange(() => Notices);
		}
	}
}
