using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class SelectedItemModel : ModelBase
{
	private string _name;

	private string _label;

	private bool _active;

	[DataMember]
	public string Name
	{
		get
		{
			return _name;
		}
		set
		{
			_name = value;
			NotifyOfPropertyChange(() => Name);
		}
	}

	[DataMember]
	public string Label
	{
		get
		{
			return _label;
		}
		set
		{
			_label = value;
			NotifyOfPropertyChange(() => Label);
		}
	}

	[DataMember]
	public bool Active
	{
		get
		{
			return _active;
		}
		set
		{
			_active = value;
			NotifyOfPropertyChange(() => Active);
		}
	}
}
