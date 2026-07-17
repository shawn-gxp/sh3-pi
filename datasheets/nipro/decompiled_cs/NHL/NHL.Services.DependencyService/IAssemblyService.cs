namespace NHL.Services.DependencyService;

public interface IAssemblyService
{
	string GetPackageName();

	string GetVersionName();

	string GetVersionCode();

	string GetOSName();

	string GetPlatformName();
}
