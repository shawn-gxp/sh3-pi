using System;
using System.Threading.Tasks;
using NHL.Services.DependencyService;
using PCLStorage;
using Xamarin.Forms;

namespace NHL.ViewModels.Utils;

public class IsolatedStorageUtils
{
	private static ILoggingService Log = DependencyService.Get<ILoggingService>();

	public static async Task<bool> SaveAsync(string text, string fileName)
	{
		_ = 1;
		try
		{
			await (await FileSystem.Current.LocalStorage.CreateFileAsync(fileName, CreationCollisionOption.ReplaceExisting)).WriteAllTextAsync(text);
			return true;
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【IsolatedStorageUtils】【SaveAsync】例外発生：{ex}");
			return false;
		}
	}

	public static async Task<string> LoadAsync(string fileName)
	{
		_ = 2;
		try
		{
			IFolder folder = FileSystem.Current.LocalStorage;
			if (await folder.CheckExistsAsync(fileName) == ExistenceCheckResult.NotFound)
			{
				return string.Empty;
			}
			return await (await folder.GetFileAsync(fileName)).ReadAllTextAsync();
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【IsolatedStorageUtils】【LoadAsync】例外発生：{ex}");
			return string.Empty;
		}
	}
}
