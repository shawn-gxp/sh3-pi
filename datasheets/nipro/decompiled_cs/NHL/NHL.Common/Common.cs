using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services.DependencyService;
using NHL.ViewModels.Utils;
using Newtonsoft.Json;
using PCLStorage;
using Xamarin.Forms;

namespace NHL.Common;

public class Common
{
	private static ILoggingService Log = DependencyService.Get<ILoggingService>();

	private static readonly string SETTING_FILE_NAME = "setting.txt";

	public static string GetIdfv()
	{
		return DependencyService.Get<IIdentifierForVendorService>().GetIdentifierForVendor();
	}

	public static bool GetCurrentOrientationIsPortrait()
	{
		return DependencyService.Get<ICurrentOrientationService>().GetCurrentOrientationIsPortrait();
	}

	public static async Task<SettingModel> LoadSetting()
	{
		SettingModel settingModel = null;
		try
		{
			if (await FileSystem.Current.LocalStorage.CheckExistsAsync(SETTING_FILE_NAME) == ExistenceCheckResult.FileExists)
			{
				string value = await IsolatedStorageUtils.LoadAsync(SETTING_FILE_NAME);
				if (!string.IsNullOrEmpty(value))
				{
					settingModel = JsonConvert.DeserializeObject<SettingModel>(value);
				}
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【Common】【LoadSetting】例外発生：{ex}");
		}
		return settingModel;
	}

	public static async Task SaveSetting(SettingModel setting)
	{
		try
		{
			await IsolatedStorageUtils.SaveAsync(JsonConvert.SerializeObject(setting), SETTING_FILE_NAME);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【Common】【SaveSetting】例外発生：{ex}");
		}
	}

	public static async Task<IList<IFile>> GetLogFiles()
	{
		IFolder localStorage = FileSystem.Current.LocalStorage;
		List<IFile> flieList = new List<IFile>();
		IList<IFolder> list = await GetFolders(localStorage);
		if (list != null)
		{
			IFolder folder;
			IFolder logFolder = (folder = list.Where((IFolder x) => x.Name == "Logs").FirstOrDefault());
			if (folder != null)
			{
				IList<IFile> list2 = await GetFiles(logFolder);
				if (list2 != null)
				{
					flieList.AddRange(list2);
				}
				IList<IFolder> list3 = await GetFolders(logFolder);
				IFolder folder2;
				if (list3 != null && (folder2 = list3.Where((IFolder x) => x.Name == "ArchiveFiles").FirstOrDefault()) != null)
				{
					IList<IFile> list4 = await GetFiles(folder2);
					if (list4 != null)
					{
						flieList.AddRange(list4);
					}
				}
			}
		}
		return flieList;
	}

	public static long GetFileSize(IFile file)
	{
		return DependencyService.Get<IFileControlService>().GetFileSize(file);
	}

	public static async Task<IList<IFolder>> GetFolders(IFolder folder)
	{
		return await folder.GetFoldersAsync();
	}

	public static async Task<IList<IFile>> GetFiles(IFolder folder)
	{
		return await folder.GetFilesAsync();
	}

	public static string GetTransferLogFilenameFormat()
	{
		IAssemblyService assemblyService = DependencyService.Get<IAssemblyService>();
		IIdentifierForVendorService identifierForVendorService = DependencyService.Get<IIdentifierForVendorService>();
		DateTime now = DateTime.Now;
		string identifierForVendor = identifierForVendorService.GetIdentifierForVendor();
		string oSName = assemblyService.GetOSName();
		string versionName = assemblyService.GetVersionName();
		string platformName = assemblyService.GetPlatformName();
		return now.ToString("yyyyMMddHHmmss") + "_{0}_" + identifierForVendor + "_" + platformName + "_" + oSName + "_" + versionName + ".txt";
	}

	public static bool IsiPad()
	{
		return DependencyService.Get<IAssemblyService>().GetPlatformName().Contains("iPad");
	}

	public static void ShowSnackBar(string text, int duration, string actionText = "", Action action = null)
	{
		Task.Run(delegate
		{
			Execute.OnUIThread(delegate
			{
				DependencyService.Get<ISnackBar>().Show(text, duration, actionText, action);
			});
		}).ConfigureAwait(continueOnCapturedContext: false);
	}
}
