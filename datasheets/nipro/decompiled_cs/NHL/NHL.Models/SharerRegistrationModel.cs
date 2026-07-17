using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class SharerRegistrationModel : ModelBase
{
	private string sharerCode;

	private string userId;

	[JsonProperty("sharerCode")]
	public string SharerCode
	{
		get
		{
			return sharerCode;
		}
		set
		{
			if (sharerCode != value)
			{
				sharerCode = value;
				NotifyOfPropertyChange(() => sharerCode);
			}
		}
	}

	[JsonProperty("userId")]
	public string UserId
	{
		get
		{
			return userId;
		}
		set
		{
			if (userId != value)
			{
				userId = value;
				NotifyOfPropertyChange(() => userId);
			}
		}
	}
}
