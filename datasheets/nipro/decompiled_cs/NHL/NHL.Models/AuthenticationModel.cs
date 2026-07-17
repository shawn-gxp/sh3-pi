using System.Collections.Generic;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class AuthenticationModel : ModelBase
{
	private string _authenticationToken;

	private IgUserModel _igUser;

	private HospitalModel _hospital;

	private IEnumerable<HospitalModel> _hospitals;

	private LoginModel _sharerLogin;

	private HospitalModel _sharerHospital;

	[JsonProperty("authenticationToken")]
	public string AuthenticationToken
	{
		get
		{
			return _authenticationToken;
		}
		set
		{
			_authenticationToken = value;
			NotifyOfPropertyChange(() => AuthenticationToken);
		}
	}

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

	[JsonProperty("hospital")]
	public HospitalModel Hospital
	{
		get
		{
			return _hospital;
		}
		set
		{
			_hospital = value;
			NotifyOfPropertyChange(() => Hospital);
		}
	}

	[JsonProperty("hospitals")]
	public IEnumerable<HospitalModel> Hospitals
	{
		get
		{
			return _hospitals;
		}
		set
		{
			_hospitals = value;
			NotifyOfPropertyChange(() => Hospitals);
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
