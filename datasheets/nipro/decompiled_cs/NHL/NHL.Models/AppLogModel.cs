using System;
using System.Runtime.Serialization;
using NHL.Models.Support;

namespace NHL.Models;

[DataContract]
public class AppLogModel : ModelBase
{
	private DateTime dateTime;

	private string tag;

	private string message;

	[DataMember]
	public DateTime DateTime
	{
		get
		{
			return dateTime;
		}
		set
		{
			if (dateTime != value)
			{
				dateTime = value;
				NotifyOfPropertyChange(() => DateTime);
			}
		}
	}

	[DataMember]
	public string Tag
	{
		get
		{
			return tag;
		}
		set
		{
			if (tag != value)
			{
				tag = value;
				NotifyOfPropertyChange(() => Tag);
			}
		}
	}

	[DataMember]
	public string Message
	{
		get
		{
			return message;
		}
		set
		{
			if (message != value)
			{
				message = value;
				NotifyOfPropertyChange(() => Message);
			}
		}
	}
}
