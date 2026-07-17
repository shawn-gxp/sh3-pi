using System;
using System.Net;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.WindowsAzure.MobileServices;
using Microsoft.WindowsAzure.MobileServices.SQLiteStore;
using Microsoft.WindowsAzure.MobileServices.Sync;
using NHL.Common;
using NHL.Models;
using NHL.Models.Entity;
using NHL.Services.DependencyService;
using Xamarin.Essentials;
using Xamarin.Forms;

namespace NHL.Services.Support;

public sealed class SyncManager
{
	public class SyncManagerHandler : DelegatingHandler
	{
		public static string Token { get; set; }

		protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
		{
			request.Headers.Add("X-Authentication-Token", Token);
			request.Headers.Add("X-API-Version", "1.0");
			return base.SendAsync(request, cancellationToken);
		}
	}

	private ILoggingService Log = Xamarin.Forms.DependencyService.Get<ILoggingService>();

	private const int SYNC_RETRY_MAX = 3;

	private static SyncManager instance = new SyncManager();

	private MobileServiceClient serviceClient;

	private IMobileServiceSyncTable<Measurement> measurementTable;

	private IMobileServiceSyncTable<Photograph> photographTable;

	private IMobileServiceSyncTable<DailyComments> dailyCommentsTable;

	private HttpMessageHandler[] handler;

	public IMobileServiceSyncTable<Measurement> MeasurementTable => measurementTable;

	public IMobileServiceSyncTable<Photograph> PhotographTable => photographTable;

	public IMobileServiceSyncTable<DailyComments> DailyCommentsTable => dailyCommentsTable;

	public string Token
	{
		set
		{
			SyncManagerHandler.Token = value;
		}
	}

	private SyncManager()
	{
		Initialize();
	}

	public static SyncManager GetInstance()
	{
		return instance;
	}

	public async Task Login()
	{
		await serviceClient.LoginAsync(MobileServiceAuthenticationProvider.WindowsAzureActiveDirectory, null);
	}

	public async Task Sync()
	{
		Log.Debug("【IG】【SyncManager】【Sync】sync start");
		if (Connectivity.NetworkAccess != NetworkAccess.Internet)
		{
			Log.Info("【IG】【SyncManager】【Sync】ネットワークに接続していないため同期処理をパスします");
			return;
		}
		bool flag = !(await PushAsync());
		if (!flag)
		{
			flag = !(await PullAsyncTable());
		}
		if (flag)
		{
			Log.Error("【IG】【SyncManager】【Sync】同期に失敗しました");
			NHL.Common.Common.ShowSnackBar("サーバとの同期に失敗しました。通信環境の確認をお願い致します。", 5000);
		}
		else
		{
			Application.Current.Properties["latestUpdatedAt"] = DateTime.Now.ToString("yyyy/MM/dd HH:mm");
			Log.Info("【IG】【SyncManager】【Sync】同期に成功しました");
		}
	}

	private async Task<bool> PullAsyncTable()
	{
		for (int i = 1; i <= 3; i++)
		{
			try
			{
				Log.Info("【IG】【SyncManager】【PullAsyncTable】allMeasurement start");
				await measurementTable.PullAsync("allMeasurement", measurementTable.CreateQuery());
				Log.Info("【IG】【SyncManager】【PullAsyncTable】allPhotograph start");
				await photographTable.PullAsync("allPhotograph", photographTable.CreateQuery());
				Log.Info("【IG】【SyncManager】【PullAsyncTable】allDailyComments start");
				await dailyCommentsTable.PullAsync("allDailyComments", dailyCommentsTable.CreateQuery());
				Log.Info("【IG】【SyncManager】【PullAsyncTable】all complete");
				return true;
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【SyncManager】【PullAsyncTable】PullAsync error: {ex}");
				Log.Info($"【IG】【SyncManager】【PullAsyncTable】リトライ回数: {i}回目");
				await Task.Delay(1000);
			}
		}
		Log.Error("【IG】【SyncManager】【PullAsyncTable】同期に失敗しました");
		return false;
	}

	public async Task<bool> PushAsync()
	{
		for (int i = 1; i <= 3; i++)
		{
			try
			{
				Log.Info("【IG】【SyncManager】【PushAsync】start");
				await serviceClient.SyncContext.PushAsync();
				Log.Info("【IG】【SyncManager】【PushAsync】complete");
				return true;
			}
			catch (MobileServicePushFailedException ex)
			{
				Log.Warn($"【IG】【SyncManager】【PushAsync】MobileServicePushFailedException occured. PushResult status: {ex.PushResult?.Status}");
				MobileServicePushCompletionResult pushResult = ex.PushResult;
				if (pushResult != null && pushResult.Errors?.Count > 0)
				{
					foreach (MobileServiceTableOperationError error in ex.PushResult.Errors)
					{
						Log.Debug(string.Format("【IG】【SyncManager】【PushAsync】TableName: {0}, OperationKind: {1}, HTTP Status: {2}", new object[3] { error.TableName, error.OperationKind, error.Status }));
						Log.Debug($"【IG】【SyncManager】【PushAsync】server result: {error.Result}");
						Log.Debug($"【IG】【SyncManager】【PushAsync】local item: {error.Item}");
						if (object.Equals(HttpStatusCode.Conflict, error.Status))
						{
							Log.Info("【IG】【SyncManager】【PushAsync】サーバーとローカルの情報がコンフリクトしているため、ローカルアイテムを破棄します。");
							await error.CancelAndDiscardItemAsync();
						}
						else
						{
							Log.Error("【IG】【SyncManager】【PushAsync】データの同期に失敗しました。エラー内容とデータを確認して下さい。");
						}
					}
				}
				Log.Info($"【IG】【SyncManager】【PushAsync】リトライ回数: {i}回目");
				await Task.Delay(1000);
			}
			catch (Exception ex2)
			{
				Log.Error($"【IG】【SyncManager】【PushAsync】PushAsync error: {ex2}");
				Log.Info($"【IG】【SyncManager】【PushAsync】リトライ回数: {i}回目");
				await Task.Delay(1000);
			}
		}
		Log.Error("【IG】【SyncManager】【PushAsync】同期に失敗しました");
		return false;
	}

	public async Task MeasurementPullAsync()
	{
		try
		{
			Log.Info("【IG】【SyncManager】【MeasurementPullAsync】Measurement PullAsync start");
			await measurementTable.PullAsync("allMeasurement", measurementTable.CreateQuery());
			Log.Info("【IG】【SyncManager】【MeasurementPullAsync】Measurement PullAsync complete");
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【SyncManager】【MeasurementPullAsync】Measurement PullAsync error: {ex}");
			throw;
		}
	}

	public async Task PhotographPullAsync()
	{
		try
		{
			Log.Info("【IG】【SyncManager】【PhotographPullAsync】Photograph PullAsync start");
			await photographTable.PullAsync("allPhotograph", photographTable.CreateQuery());
			Log.Info("【IG】【SyncManager】【PhotographPullAsync】Photograph PullAsync complete");
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【SyncManager】【PhotographPullAsync】Photograph PullAsync error: {ex}");
			throw;
		}
	}

	public async Task DailyCommentsPullAsync()
	{
		try
		{
			Log.Info("【IG】【SyncManager】【DailyCommentsPullAsync】DailyComments PullAsync start");
			await dailyCommentsTable.PullAsync("allDailyComments", dailyCommentsTable.CreateQuery());
			Log.Info("【IG】【SyncManager】【DailyCommentsPullAsync】DailyComments PullAsync complete");
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【SyncManager】【DailyCommentsPullAsync】DailyComments PullAsync error: {ex}");
			throw;
		}
	}

	private void Initialize()
	{
		try
		{
			serviceClient = new MobileServiceClient("https://api.niprogenkinote.jp/", new SyncManagerHandler());
			MobileServiceSQLiteStore store = new MobileServiceSQLiteStore("localstore.db");
			store.DefineTable<Measurement>();
			store.DefineTable<Photograph>();
			store.DefineTable<DailyComments>();
			serviceClient.SyncContext.InitializeAsync(store);
			measurementTable = serviceClient.GetSyncTable<Measurement>();
			photographTable = serviceClient.GetSyncTable<Photograph>();
			dailyCommentsTable = serviceClient.GetSyncTable<DailyComments>();
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【SyncManager】【Initialize】Sync error: {ex}");
		}
	}

	public async Task PurgeLocalDB()
	{
		await measurementTable.PurgeAsync(null, null, force: true, CancellationToken.None);
		await photographTable.PurgeAsync(null, null, force: true, CancellationToken.None);
		await dailyCommentsTable.PurgeAsync(null, null, force: true, CancellationToken.None);
	}
}
