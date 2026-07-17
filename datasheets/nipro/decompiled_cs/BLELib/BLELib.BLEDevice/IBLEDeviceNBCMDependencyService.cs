using Plugin.BLE.Abstractions.Contracts;

namespace BLELib.BLEDevice;

public interface IBLEDeviceNBCMDependencyService
{
	void EnableBodyComposition(IDevice device);
}
