using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class AuthenticationRequestModel : ModelBase
{
	private string _IgUserId;

	private string _Idfv;

	[JsonProperty("igUserId")]
	public string IgUserId
	{
		get
		{
			return _IgUserId;
		}
		set
		{
			_IgUserId = value;
			NotifyOfPropertyChange(() => IgUserId);
		}
	}

	[JsonProperty("idfv")]
	public string Idfv
	{
		get
		{
			return _Idfv;
		}
		set
		{
			_Idfv = value;
			NotifyOfPropertyChange(() => Idfv);
		}
	}
}
