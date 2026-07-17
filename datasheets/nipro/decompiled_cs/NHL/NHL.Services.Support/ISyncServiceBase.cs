using System.Threading.Tasks;

namespace NHL.Services.Support;

public interface ISyncServiceBase
{
	Task Sync();

	Task PushAsync();

	Task PullAsync();
}
