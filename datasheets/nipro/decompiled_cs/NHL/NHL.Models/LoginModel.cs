using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class LoginModel : ModelBase
{
	private string _id;

	private string _loginId;

	private string _password;

	private string _name;

	private string _nameKana;

	private string _email;

	private string _type;

	private string _memo;

	private string _hospitalId;

	[JsonProperty("id")]
	public string Id
	{
		get
		{
			return _id;
		}
		set
		{
			if (_id != value)
			{
				_id = value;
				NotifyOfPropertyChange(() => Id);
			}
		}
	}

	[JsonProperty("loginId")]
	public string LoginId
	{
		get
		{
			return _loginId;
		}
		set
		{
			if (_loginId != value)
			{
				_loginId = value;
				NotifyOfPropertyChange(() => LoginId);
			}
		}
	}

	[JsonProperty("password")]
	public string Password
	{
		get
		{
			return _password;
		}
		set
		{
			if (_password != value)
			{
				_password = value;
				NotifyOfPropertyChange(() => Password);
			}
		}
	}

	[JsonProperty("name")]
	public string Name
	{
		get
		{
			return _name;
		}
		set
		{
			if (_name != value)
			{
				_name = value;
				NotifyOfPropertyChange(() => Name);
			}
		}
	}

	[JsonProperty("nameKana")]
	public string NameKana
	{
		get
		{
			return _nameKana;
		}
		set
		{
			if (_nameKana != value)
			{
				_nameKana = value;
				NotifyOfPropertyChange(() => NameKana);
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
			if (_email != value)
			{
				_email = value;
				NotifyOfPropertyChange(() => Email);
			}
		}
	}

	[JsonProperty("type")]
	public string Type
	{
		get
		{
			return _type;
		}
		set
		{
			if (_type != value)
			{
				_type = value;
				NotifyOfPropertyChange(() => Type);
			}
		}
	}

	[JsonProperty("memo")]
	public string Memo
	{
		get
		{
			return _memo;
		}
		set
		{
			if (_memo != value)
			{
				_memo = value;
				NotifyOfPropertyChange(() => Memo);
			}
		}
	}

	[JsonProperty("hospitalId")]
	public string HospitalId
	{
		get
		{
			return _hospitalId;
		}
		set
		{
			if (_hospitalId != value)
			{
				_hospitalId = value;
				NotifyOfPropertyChange(() => HospitalId);
			}
		}
	}
}
