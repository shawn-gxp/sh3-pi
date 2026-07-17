using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class NoticeDetailResponseModel : ModelBase
{
	private NoticeModel _notice;

	private HospitalModel _hospital;

	[JsonProperty("notice")]
	public NoticeModel Notice
	{
		get
		{
			return _notice;
		}
		set
		{
			if (_notice != value)
			{
				_notice = value;
				NotifyOfPropertyChange(() => Notice);
			}
		}
	}

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
}
