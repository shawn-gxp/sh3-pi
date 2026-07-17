using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Common;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using PCLStorage;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class InformationViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private string applicationName;

	private string version;

	public IMeasurementService MeasurementService { get; set; }

	public ILogfileTransferService LogfileTransferService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public string ApplicationName
	{
		get
		{
			return applicationName;
		}
		set
		{
			if (applicationName != value)
			{
				applicationName = value;
				NotifyOfPropertyChange(() => ApplicationName);
			}
		}
	}

	public string Version
	{
		get
		{
			return version;
		}
		set
		{
			if (version != value)
			{
				version = value;
				NotifyOfPropertyChange(() => Version);
			}
		}
	}

	public async Task SyncMeasurement()
	{
		await MeasurementService.Sync();
	}

	public async void LogTransfer()
	{
		IList<IFile> logFileList = await NHL.Common.Common.GetLogFiles();
		if (logFileList.Count == 0)
		{
			await DialogProvider.ShowAlert("", "送信対象のログファイルがありませんでした");
			return;
		}
		await ExecAsync(async delegate
		{
			try
			{
				int counter = 0;
				string format = NHL.Common.Common.GetTransferLogFilenameFormat();
				foreach (IFile item in logFileList)
				{
					counter++;
					string s = await item.ReadAllTextAsync();
					string log = Convert.ToBase64String(Encoding.GetEncoding("UTF-8").GetBytes(s));
					LogfileTransferModel log2 = new LogfileTransferModel
					{
						Filename = string.Format(format, new object[1] { counter }),
						Log = log
					};
					await LogfileTransferService.Transfer(log2);
				}
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowAlert("", "ログを送信しました");
				});
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【InformationViewModel】【LogTransfer】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
	}

	protected override void OnActivate()
	{
		IAssemblyService assemblyService = DependencyService.Get<IAssemblyService>();
		ApplicationName = assemblyService.GetPackageName();
		Version = assemblyService.GetVersionCode() ?? "";
		base.OnActivate();
	}
}
