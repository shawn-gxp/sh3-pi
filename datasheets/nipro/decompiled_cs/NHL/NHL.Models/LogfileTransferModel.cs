using NHL.Models.Support;
using Newtonsoft.Json;

namespace NHL.Models;

public class LogfileTransferModel : ModelBase
{
	private string _Filename;

	private string _Log;

	[JsonProperty("filename")]
	public string Filename
	{
		get
		{
			return _Filename;
		}
		set
		{
			_Filename = value;
			NotifyOfPropertyChange(() => Filename);
		}
	}

	[JsonProperty("log")]
	public string Log
	{
		get
		{
			return _Log;
		}
		set
		{
			_Log = value;
			NotifyOfPropertyChange(() => Log);
		}
	}
}
