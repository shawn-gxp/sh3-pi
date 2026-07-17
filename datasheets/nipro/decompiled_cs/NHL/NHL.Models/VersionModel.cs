using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class VersionModel : ModelBase
{
	private string versionCode;

	private string versionName;

	[JsonProperty("versionCode")]
	public string VersionCode
	{
		get
		{
			return versionCode;
		}
		set
		{
			Set(ref versionCode, value, VersionCode);
		}
	}

	[JsonProperty("versionName")]
	public string VersionName
	{
		get
		{
			return versionName;
		}
		set
		{
			Set(ref versionName, value, VersionName);
		}
	}
}
