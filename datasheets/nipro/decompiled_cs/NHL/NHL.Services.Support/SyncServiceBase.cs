using System.Threading.Tasks;
using NHL.Framework;

namespace NHL.Services.Support;

public abstract class SyncServiceBase : ServiceProxyBase, ISyncServiceBase
{
	public SyncServiceBase(IRestClient newRestClient)
		: this(string.Empty, newRestClient)
	{
	}

	public SyncServiceBase(string baseAddress, IRestClient newRestClient)
		: base(baseAddress, newRestClient)
	{
	}

	public async Task Sync()
	{
		await SyncManager.GetInstance().Sync();
	}

	public async Task PushAsync()
	{
		await SyncManager.GetInstance().PushAsync();
	}

	public abstract Task PullAsync();
}
