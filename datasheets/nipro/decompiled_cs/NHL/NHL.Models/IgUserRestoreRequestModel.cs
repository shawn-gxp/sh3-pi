using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class IgUserRestoreRequestModel : ModelBase
{
	private string _BackupCode;

	private string _BackupPassword;

	[JsonProperty("backupCode")]
	public string BackupCode
	{
		get
		{
			return _BackupCode;
		}
		set
		{
			_BackupCode = value;
			NotifyOfPropertyChange(() => BackupCode);
		}
	}

	[JsonProperty("backupPassword")]
	public string BackupPassword
	{
		get
		{
			return _BackupPassword;
		}
		set
		{
			_BackupPassword = value;
			NotifyOfPropertyChange(() => BackupPassword);
		}
	}
}
