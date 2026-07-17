using System;
using System.Threading;
using Plugin.BLE.Abstractions.Contracts;

namespace Plugin.BLE;

public static class CrossBluetoothLE
{
	private static readonly Lazy<IBluetoothLE> Implementation = new Lazy<IBluetoothLE>(CreateImplementation, LazyThreadSafetyMode.PublicationOnly);

	public static IBluetoothLE Current => Implementation.Value ?? throw NotImplementedInReferenceAssembly();

	private static IBluetoothLE CreateImplementation()
	{
		BleImplementation bleImplementation = new BleImplementation();
		bleImplementation.Initialize();
		return bleImplementation;
	}

	internal static Exception NotImplementedInReferenceAssembly()
	{
		return new NotImplementedException("This functionality is not implemented in the portable version of this assembly.  You should reference the NuGet package from your main application project in order to reference the platform-specific implementation.");
	}
}
