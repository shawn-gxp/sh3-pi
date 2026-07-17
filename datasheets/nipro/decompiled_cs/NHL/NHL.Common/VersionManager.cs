using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Common;

public class VersionManager
{
	private static ILoggingService Log = DependencyService.Get<ILoggingService>();

	public static async void CheckVersion()
	{
		_ = 2;
		try
		{
			await IoC.Get<IMeasurementService>().GetAllMeasurement();
			IAssemblyService assemblyService = DependencyService.Get<IAssemblyService>();
			string newVersion = assemblyService.GetVersionName();
			SettingModel settingModel = await Common.LoadSetting();
			if (settingModel != null)
			{
				new Version(settingModel.Version);
			}
			else
			{
				settingModel = new SettingModel();
				settingModel.Version = newVersion;
			}
			await Common.SaveSetting(settingModel);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【VersionManager】【CheckVersion】例外発生：{ex}");
		}
	}

	public static async Task<bool> CheckNewApplicationVersion()
	{
		try
		{
			VersionModel versionModel = await IoC.Get<IVersionService>().GetVersion(Device.RuntimePlatform);
			IAssemblyService assemblyService = DependencyService.Get<IAssemblyService>();
			Log.Info("【IG】【VersionManager】【CheckNewApplicationVersion】【サーバー】バージョンコード:" + versionModel.VersionCode + " バージョン名:" + versionModel.VersionName);
			Log.Info("【IG】【VersionManager】【CheckNewApplicationVersion】【インストールアプリ】バージョンコード:" + assemblyService.GetVersionCode() + " バージョン名:" + assemblyService.GetVersionName());
			int num = int.Parse(versionModel.VersionCode.Replace(".", string.Empty));
			return int.Parse(assemblyService.GetVersionCode().Replace(".", string.Empty)) >= num;
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【VersionManager】【CheckNewApplicationVersion】例外発生：{ex}");
		}
		return true;
	}
}
