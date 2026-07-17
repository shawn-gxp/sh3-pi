using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Framework;
using NHL.Models;
using NHL.Services.DependencyService;
using NHL.Services.Support;
using Xamarin.Forms;

namespace NHL.Services.Impl;

public class DailyCommentService(IRestClient newRestClient) : SyncServiceBase("DailyComments", newRestClient), IDailyCommentService, ISyncServiceBase
{
	private readonly ILoggingService _log = Xamarin.Forms.DependencyService.Get<ILoggingService>();

	public async Task<BindableCollection<DailyComments>> GetAllDailyComment(bool isSync = false)
	{
		if (isSync)
		{
			_log.Info("【IG】【DailyCommentService】【GetAllDailyComment】Sync start");
			await Sync();
		}
		return new BindableCollection<DailyComments>(await SyncManager.GetInstance().DailyCommentsTable.ToEnumerableAsync());
	}

	public async Task RegisterDailyComment(DailyComments dailyComment)
	{
		try
		{
			await SyncManager.GetInstance().DailyCommentsTable.InsertAsync(dailyComment);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【DailyCommentService】【RegisterDailyComment】InsertAsync error: {ex}");
			throw;
		}
	}

	public async Task UpdateDailyComment(DailyComments dailyComments)
	{
		try
		{
			await SyncManager.GetInstance().DailyCommentsTable.UpdateAsync(dailyComments);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【DailyCommentService】【UpdateDailyComment】UpdateAsync error: {ex}");
			throw;
		}
	}

	public override async Task PullAsync()
	{
		await SyncManager.GetInstance().DailyCommentsPullAsync();
	}
}
