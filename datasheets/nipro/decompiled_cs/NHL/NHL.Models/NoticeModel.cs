using System;
using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class NoticeModel : ModelBase
{
	private string _id;

	private string _title;

	private string _detail;

	private DateTime _expiryDateFrom;

	private DateTime? _expiryDateTo;

	private int _transitionType;

	private DateTime? _createdAt;

	private DateTime? _updatedAt;

	private bool _deleted;

	private string _version;

	[JsonProperty("id")]
	public string Id
	{
		get
		{
			return _id;
		}
		set
		{
			if (!(_id == value))
			{
				_id = value;
				NotifyOfPropertyChange(() => Id);
			}
		}
	}

	[JsonProperty("title")]
	public string Title
	{
		get
		{
			return _title;
		}
		set
		{
			if (!(_title == value))
			{
				_title = value;
				NotifyOfPropertyChange(() => Title);
			}
		}
	}

	[JsonProperty("detail")]
	public string Detail
	{
		get
		{
			return _detail;
		}
		set
		{
			if (!(_detail == value))
			{
				_detail = value;
				NotifyOfPropertyChange(() => Detail);
			}
		}
	}

	[JsonProperty("expiryDateFrom")]
	public DateTime ExpiryDateFrom
	{
		get
		{
			return _expiryDateFrom;
		}
		set
		{
			if (!(_expiryDateFrom == value))
			{
				_expiryDateFrom = value;
				NotifyOfPropertyChange(() => ExpiryDateFrom);
			}
		}
	}

	[JsonProperty("expiryDateTo")]
	public DateTime? ExpiryDateTo
	{
		get
		{
			return _expiryDateTo;
		}
		set
		{
			if (!(_expiryDateTo == value))
			{
				_expiryDateTo = value;
				NotifyOfPropertyChange(() => ExpiryDateTo);
			}
		}
	}

	[JsonProperty("transitionType")]
	public int TransitionType
	{
		get
		{
			return _transitionType;
		}
		set
		{
			if (_transitionType != value)
			{
				_transitionType = value;
				NotifyOfPropertyChange(() => TransitionType);
			}
		}
	}

	[JsonProperty("createdAt")]
	public DateTime? CreatedAt
	{
		get
		{
			return _createdAt;
		}
		set
		{
			if (_createdAt != value)
			{
				_createdAt = value;
				NotifyOfPropertyChange(() => CreatedAt);
			}
		}
	}

	[JsonProperty("updatedAt")]
	public DateTime? UpdatedAt
	{
		get
		{
			return _updatedAt;
		}
		set
		{
			if (_updatedAt != value)
			{
				_updatedAt = value;
				NotifyOfPropertyChange(() => UpdatedAt);
			}
		}
	}

	[JsonProperty("deleted")]
	public bool Deleted
	{
		get
		{
			return _deleted;
		}
		set
		{
			if (_deleted != value)
			{
				_deleted = value;
				NotifyOfPropertyChange(() => Deleted);
			}
		}
	}

	[JsonProperty("version")]
	public string Version
	{
		get
		{
			return _version;
		}
		set
		{
			if (_version != value)
			{
				_version = value;
				NotifyOfPropertyChange(() => Version);
			}
		}
	}
}
