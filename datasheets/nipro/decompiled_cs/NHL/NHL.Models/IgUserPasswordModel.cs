using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class IgUserPasswordModel : ModelBase
{
	private string _BackupPassword;

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
