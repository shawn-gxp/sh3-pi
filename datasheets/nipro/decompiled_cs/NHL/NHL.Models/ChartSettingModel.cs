using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class ChartSettingModel : ModelBase
{
	private string setting;

	private bool active;

	[DataMember]
	public string Setting
	{
		get
		{
			return setting;
		}
		set
		{
			setting = value;
			NotifyOfPropertyChange(() => Setting);
		}
	}

	[DataMember]
	public bool Active
	{
		get
		{
			return active;
		}
		set
		{
			active = value;
			NotifyOfPropertyChange(() => Active);
		}
	}
}
