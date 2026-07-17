using System;
using Plugin.BLE.Android.CallbackEventArgs;

namespace Plugin.BLE.Android;

public interface IGattCallback
{
	event EventHandler<ServicesDiscoveredCallbackEventArgs> ServicesDiscovered;

	event EventHandler<CharacteristicReadCallbackEventArgs> CharacteristicValueUpdated;

	event EventHandler<CharacteristicWriteCallbackEventArgs> CharacteristicValueWritten;

	event EventHandler<DescriptorCallbackEventArgs> DescriptorValueWritten;

	event EventHandler<DescriptorCallbackEventArgs> DescriptorValueRead;

	event EventHandler<RssiReadCallbackEventArgs> RemoteRssiRead;

	event EventHandler ConnectionInterrupted;

	event EventHandler<MtuRequestCallbackEventArgs> MtuRequested;
}
