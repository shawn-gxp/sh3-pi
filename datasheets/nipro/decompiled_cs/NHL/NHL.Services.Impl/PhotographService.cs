using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Framework;
using NHL.Models;
using NHL.Services.DependencyService;
using NHL.Services.Support;
using Xamarin.Forms;

namespace NHL.Services.Impl;

public class PhotographService(IRestClient newRestClient) : SyncServiceBase("Photograph", newRestClient), IPhotographService, ISyncServiceBase
{
	private readonly ILoggingService _log = Xamarin.Forms.DependencyService.Get<ILoggingService>();

	public async Task<BindableCollection<Photograph>> GetAllPhotograph(bool isSync = false)
	{
		if (isSync)
		{
			_log.Info("【IG】【PhotographService】【GetAllPhotograph】Sync start");
			await Sync();
		}
		return new BindableCollection<Photograph>(await SyncManager.GetInstance().PhotographTable.ToEnumerableAsync());
	}

	public async Task RegisterPhotograph(Photograph photograph)
	{
		try
		{
			await SyncManager.GetInstance().PhotographTable.InsertAsync(photograph);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【PhotographService】【RegisterPhotograph】InsertAsync error: {ex}");
			throw;
		}
	}

	public async Task UpdatePhotograph(Photograph photograph)
	{
		try
		{
			await SyncManager.GetInstance().PhotographTable.UpdateAsync(photograph);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【PhotographService】【UpdatePhotograph】UpdateAsync error: {ex}");
			throw;
		}
	}

	public override async Task PullAsync()
	{
		await SyncManager.GetInstance().PhotographPullAsync();
	}
}
