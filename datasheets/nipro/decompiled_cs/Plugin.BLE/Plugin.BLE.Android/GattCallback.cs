using System;
using Android.Bluetooth;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Extensions;
using Plugin.BLE.Android.CallbackEventArgs;

namespace Plugin.BLE.Android;

public class GattCallback : BluetoothGattCallback, IGattCallback
{
	private readonly Adapter _adapter;

	private readonly Device _device;

	public event EventHandler<ServicesDiscoveredCallbackEventArgs> ServicesDiscovered;

	public event EventHandler<CharacteristicReadCallbackEventArgs> CharacteristicValueUpdated;

	public event EventHandler<CharacteristicWriteCallbackEventArgs> CharacteristicValueWritten;

	public event EventHandler<RssiReadCallbackEventArgs> RemoteRssiRead;

	public event EventHandler ConnectionInterrupted;

	public event EventHandler<DescriptorCallbackEventArgs> DescriptorValueWritten;

	public event EventHandler<DescriptorCallbackEventArgs> DescriptorValueRead;

	public event EventHandler<MtuRequestCallbackEventArgs> MtuRequested;

	public GattCallback(Adapter adapter, Device device)
	{
		_adapter = adapter;
		_device = device;
	}

	public override void OnConnectionStateChange(BluetoothGatt gatt, GattStatus status, ProfileState newState)
	{
		base.OnConnectionStateChange(gatt, status, newState);
		if (!gatt.Device.Address.Equals(_device.BluetoothDevice.Address))
		{
			Trace.Message($"Gatt callback for device {_device.BluetoothDevice.Address} was called for device with address {gatt.Device.Address}. This shoud not happen. Please log an issue.");
			return;
		}
		Trace.Message($"References of parent device and gatt callback device equal? {(_device.BluetoothDevice == gatt.Device).ToString().ToUpper()}");
		Trace.Message($"OnConnectionStateChange: GattStatus: {status}");
		switch (newState)
		{
		case ProfileState.Disconnected:
			CloseGattInstances(gatt);
			if (_device.IsOperationRequested)
			{
				Trace.Message("Disconnected by user");
				_device.IsOperationRequested = false;
				_adapter.ConnectedDeviceRegistry.Remove(gatt.Device.Address);
				if (status != GattStatus.Success)
				{
					Trace.Message($"Error while connecting '{_device.Name}'. Not raising disconnect event.");
					_adapter.HandleConnectionFail(_device, $"GattCallback error: {status}");
				}
				else
				{
					_adapter.HandleDisconnectedDevice(disconnectRequested: true, _device);
				}
			}
			else
			{
				Trace.Message($"Disconnected '{_device.Name}' by lost connection");
				_adapter.ConnectedDeviceRegistry.Remove(gatt.Device.Address);
				_adapter.HandleDisconnectedDevice(disconnectRequested: false, _device);
				this.ConnectionInterrupted?.Invoke(this, EventArgs.Empty);
			}
			break;
		case ProfileState.Connecting:
			Trace.Message("Connecting");
			break;
		case ProfileState.Connected:
			Trace.Message("Connected");
			if (_device.IsOperationRequested)
			{
				_device.Update(gatt.Device, gatt);
				_device.IsOperationRequested = false;
			}
			else
			{
				_device.Update(gatt.Device, gatt);
			}
			if (status != GattStatus.Success)
			{
				Trace.Message($"Error while connecting '{_device.Name}'. GattStatus: {status}. ");
				_adapter.HandleConnectionFail(_device, $"GattCallback error: {status}");
				CloseGattInstances(gatt);
			}
			else
			{
				_adapter.ConnectedDeviceRegistry[gatt.Device.Address] = _device;
				_adapter.HandleConnectedDevice(_device);
			}
			break;
		case ProfileState.Disconnecting:
			Trace.Message("Disconnecting");
			break;
		}
	}

	private void CloseGattInstances(BluetoothGatt gatt)
	{
		Trace.Message($"References of parnet device gatt and callback gatt equal? {(_device._gatt == gatt).ToString().ToUpper()}");
		if (gatt != _device._gatt)
		{
			gatt.Close();
		}
		_device.CloseGatt();
	}

	public override void OnServicesDiscovered(BluetoothGatt gatt, GattStatus status)
	{
		base.OnServicesDiscovered(gatt, status);
		Trace.Message("OnServicesDiscovered: {0}", status.ToString());
		this.ServicesDiscovered?.Invoke(this, new ServicesDiscoveredCallbackEventArgs());
	}

	public override void OnCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, GattStatus status)
	{
		base.OnCharacteristicRead(gatt, characteristic, status);
		Trace.Message("OnCharacteristicRead: value {0}; status {1}", characteristic.GetValue().ToHexString(), status);
		this.CharacteristicValueUpdated?.Invoke(this, new CharacteristicReadCallbackEventArgs(characteristic));
	}

	public override void OnCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
	{
		base.OnCharacteristicChanged(gatt, characteristic);
		this.CharacteristicValueUpdated?.Invoke(this, new CharacteristicReadCallbackEventArgs(characteristic));
	}

	public override void OnCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, GattStatus status)
	{
		base.OnCharacteristicWrite(gatt, characteristic, status);
		Trace.Message("OnCharacteristicWrite: value {0} status {1}", characteristic.GetValue().ToHexString(), status);
		this.CharacteristicValueWritten?.Invoke(this, new CharacteristicWriteCallbackEventArgs(characteristic, GetExceptionFromGattStatus(status)));
	}

	public override void OnReliableWriteCompleted(BluetoothGatt gatt, GattStatus status)
	{
		base.OnReliableWriteCompleted(gatt, status);
		Trace.Message("OnReliableWriteCompleted: {0}", status);
	}

	public override void OnMtuChanged(BluetoothGatt gatt, int mtu, GattStatus status)
	{
		base.OnMtuChanged(gatt, mtu, status);
		Trace.Message("OnMtuChanged to value: {0}", mtu);
		this.MtuRequested?.Invoke(this, new MtuRequestCallbackEventArgs(GetExceptionFromGattStatus(status), mtu));
	}

	public override void OnReadRemoteRssi(BluetoothGatt gatt, int rssi, GattStatus status)
	{
		base.OnReadRemoteRssi(gatt, rssi, status);
		Trace.Message("OnReadRemoteRssi: device {0} status {1} value {2}", gatt.Device.Name, status, rssi);
		this.RemoteRssiRead?.Invoke(this, new RssiReadCallbackEventArgs(GetExceptionFromGattStatus(status), rssi));
	}

	public override void OnDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, GattStatus status)
	{
		base.OnDescriptorWrite(gatt, descriptor, status);
		Trace.Message("OnDescriptorWrite: {0}", descriptor.GetValue()?.ToHexString());
		this.DescriptorValueWritten?.Invoke(this, new DescriptorCallbackEventArgs(descriptor, GetExceptionFromGattStatus(status)));
	}

	public override void OnDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, GattStatus status)
	{
		base.OnDescriptorRead(gatt, descriptor, status);
		Trace.Message("OnDescriptorRead: {0}", descriptor.GetValue()?.ToHexString());
		this.DescriptorValueRead?.Invoke(this, new DescriptorCallbackEventArgs(descriptor, GetExceptionFromGattStatus(status)));
	}

	private Exception GetExceptionFromGattStatus(GattStatus status)
	{
		Exception result = null;
		switch (status)
		{
		case GattStatus.ReadNotPermitted:
		case GattStatus.WriteNotPermitted:
		case GattStatus.InsufficientAuthentication:
		case GattStatus.RequestNotSupported:
		case GattStatus.InvalidOffset:
		case GattStatus.InvalidAttributeLength:
		case GattStatus.InsufficientEncryption:
		case GattStatus.Failure:
			result = new Exception(status.ToString());
			break;
		}
		return result;
	}
}
