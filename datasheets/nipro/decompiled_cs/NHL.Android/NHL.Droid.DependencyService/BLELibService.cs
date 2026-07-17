using BLELib;
using BLELib.Android;
using NHL.Services.DependencyService;

namespace NHL.Droid.DependencyService;

public class BLELibService : IBLELibService
{
	public IBLELib GetBLELibrary()
	{
		return new BLELibrary();
	}
}
