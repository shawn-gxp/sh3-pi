using System.Runtime.Serialization;
using Caliburn.Micro;
using Newtonsoft.Json;

namespace NHL.Models.Support;

[DataContract]
public abstract class ModelBase : PropertyChangedBase
{
	public override string ToString()
	{
		return JsonConvert.SerializeObject(this, Formatting.Indented);
	}
}
