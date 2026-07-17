using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class SharerRegisterResponseModel : ModelBase
{
	private IgUserModel _igUser;

	private LoginModel _sharerLogin;

	private HospitalModel _sharerHospital;

	[JsonProperty("igUser")]
	public IgUserModel IgUser
	{
		get
		{
			return _igUser;
		}
		set
		{
			_igUser = value;
			NotifyOfPropertyChange(() => IgUser);
		}
	}

	[JsonProperty("sharerLogin")]
	public LoginModel SharerLogin
	{
		get
		{
			return _sharerLogin;
		}
		set
		{
			_sharerLogin = value;
			NotifyOfPropertyChange(() => SharerLogin);
		}
	}

	[JsonProperty("sharerHospital")]
	public HospitalModel SharerHospital
	{
		get
		{
			return _sharerHospital;
		}
		set
		{
			_sharerHospital = value;
			NotifyOfPropertyChange(() => SharerHospital);
		}
	}
}
