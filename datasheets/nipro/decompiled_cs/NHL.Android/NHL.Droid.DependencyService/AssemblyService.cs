using Android.Content;
using Android.Content.PM;
using Android.OS;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class AssemblyService : IAssemblyService
{
	public string GetPackageName()
	{
		return GetPackageInfo()?.PackageName;
	}

	public string GetVersionCode()
	{
		return GetPackageInfo()?.VersionCode.ToString();
	}

	public string GetVersionName()
	{
		return GetPackageInfo()?.VersionName;
	}

	protected PackageInfo GetPackageInfo()
	{
		Context context = Forms.Context;
		try
		{
			return context.PackageManager.GetPackageInfo(context.PackageName, (PackageInfoFlags)0);
		}
		catch
		{
			return null;
		}
	}

	public string GetOSName()
	{
		return "Android" + Build.VERSION.Release;
	}

	public string GetPlatformName()
	{
		return Build.Manufacturer + "-" + Build.Model;
	}
}
