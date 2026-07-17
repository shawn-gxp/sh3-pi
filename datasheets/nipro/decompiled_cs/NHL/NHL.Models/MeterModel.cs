using System;
using NHL.Models.Support;

namespace NHL.Models;

public class MeterModel : ModelBase
{
	private Guid id;

	private string name = string.Empty;

	private string serialNumber = string.Empty;

	private string userNo = string.Empty;

	private string colorCode = string.Empty;

	private bool isConnected;

	private int deviceType;

	public Guid Id
	{
		get
		{
			return id;
		}
		set
		{
			if (id != value)
			{
				id = value;
				NotifyOfPropertyChange(() => Id);
			}
		}
	}

	public string IdToString => id.ToString();

	public string Name
	{
		get
		{
			return name;
		}
		set
		{
			if (name != value)
			{
				name = value;
				NotifyOfPropertyChange(() => Name);
			}
		}
	}

	public string SerialNumber
	{
		get
		{
			return serialNumber;
		}
		set
		{
			if (serialNumber != value)
			{
				serialNumber = value;
				NotifyOfPropertyChange(() => SerialNumber);
			}
		}
	}

	public string UserNo
	{
		get
		{
			return userNo;
		}
		set
		{
			Set(ref userNo, value, "UserNo");
		}
	}

	public string ColorCode
	{
		get
		{
			return colorCode;
		}
		set
		{
			Set(ref colorCode, value, "ColorCode");
		}
	}

	public bool IsConnected
	{
		get
		{
			return isConnected;
		}
		set
		{
			if (isConnected != value)
			{
				isConnected = value;
				NotifyOfPropertyChange(() => IsConnected);
			}
		}
	}

	public int DeviceType
	{
		get
		{
			return deviceType;
		}
		set
		{
			if (deviceType != value)
			{
				deviceType = value;
				NotifyOfPropertyChange(() => DeviceType);
			}
		}
	}
}
