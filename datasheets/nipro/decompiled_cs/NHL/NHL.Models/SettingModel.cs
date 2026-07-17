using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class SettingModel : ModelBase
{
	private string _Version;

	[JsonProperty("Version")]
	public string Version
	{
		get
		{
			return _Version;
		}
		set
		{
			_Version = value;
			NotifyOfPropertyChange(() => Version);
		}
	}
}
