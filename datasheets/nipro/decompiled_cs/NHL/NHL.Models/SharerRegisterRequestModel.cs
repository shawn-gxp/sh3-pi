using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class SharerRegisterRequestModel : ModelBase
{
	private string _sharerLoginId;

	private string _sharerPassword;

	private string _email;

	[JsonProperty("sharerLoginId")]
	public string SharerLoginId
	{
		get
		{
			return _sharerLoginId;
		}
		set
		{
			if (!(_sharerLoginId == value))
			{
				_sharerLoginId = value;
				NotifyOfPropertyChange(() => SharerLoginId);
			}
		}
	}

	[JsonProperty("sharerPassword")]
	public string SharerPassword
	{
		get
		{
			return _sharerPassword;
		}
		set
		{
			if (!(_sharerPassword == value))
			{
				_sharerPassword = value;
				NotifyOfPropertyChange(() => SharerPassword);
			}
		}
	}

	[JsonProperty("email")]
	public string Email
	{
		get
		{
			return _email;
		}
		set
		{
			if (!(_email == value))
			{
				_email = value;
				NotifyOfPropertyChange(() => _email);
			}
		}
	}
}
