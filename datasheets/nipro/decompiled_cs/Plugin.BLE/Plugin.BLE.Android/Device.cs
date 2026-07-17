using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Android.App;
using Android.Bluetooth;
using Android.Content;
using Android.OS;
using Java.Lang;
using Java.Lang.Reflect;
using Plugin.BLE.Abstractions;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.Utils;
using Plugin.BLE.Android.CallbackEventArgs;

namespace Plugin.BLE.Android;

public class Device : DeviceBase
{
	internal BluetoothGatt _gatt;

	private readonly GattCallback _gattCallback;

	public BluetoothDevice BluetoothDevice { get; private set; }

	public override object NativeDevice => BluetoothDevice;

	internal bool IsOperationRequested { get; set; }

	public Device(Adapter adapter, BluetoothDevice nativeDevice, BluetoothGatt gatt, int rssi, byte[] advertisementData = null)
		: base(adapter)
	{
		Update(nativeDevice, gatt);
		base.Rssi = rssi;
		base.AdvertisementRecords = ParseScanRecord(advertisementData);
		_gattCallback = new GattCallback(adapter, this);
	}

	public void Update(BluetoothDevice nativeDevice, BluetoothGatt gatt)
	{
		BluetoothDevice = nativeDevice;
		_gatt = gatt;
		base.Id = ParseDeviceId();
		base.Name = BluetoothDevice.Name;
	}

	protected override async Task<IEnumerable<IService>> GetServicesNativeAsync()
	{
		if (_gattCallback == null || _gatt == null)
		{
			return Enumerable.Empty<IService>();
		}
		return await TaskBuilder.FromEvent<IEnumerable<IService>, EventHandler<ServicesDiscoveredCallbackEventArgs>, EventHandler>(delegate
		{
			_gatt.DiscoverServices();
		}, (Action<IEnumerable<IService>> complete, Action<System.Exception> reject) => delegate
		{
			complete(_gatt.Services.Select((BluetoothGattService service) => new Service(service, _gatt, _gattCallback, this)));
		}, delegate(EventHandler<ServicesDiscoveredCallbackEventArgs> handler)
		{
			_gattCallback.ServicesDiscovered += handler;
		}, delegate(EventHandler<ServicesDiscoveredCallbackEventArgs> handler)
		{
			_gattCallback.ServicesDiscovered -= handler;
		}, (Action<System.Exception> reject) => delegate
		{
			reject(new System.Exception($"Device {base.Name} disconnected while fetching services."));
		}, delegate(EventHandler handler)
		{
			_gattCallback.ConnectionInterrupted += handler;
		}, delegate(EventHandler handler)
		{
			_gattCallback.ConnectionInterrupted -= handler;
		});
	}

	public void Connect(ConnectParameters connectParameters)
	{
		IsOperationRequested = true;
		if (connectParameters.ForceBleTransport)
		{
			ConnectToGattForceBleTransportAPI(connectParameters.AutoConnect);
		}
		else
		{
			BluetoothDevice.ConnectGatt(Application.Context, connectParameters.AutoConnect, _gattCallback);
		}
	}

	private void ConnectToGattForceBleTransportAPI(bool autoconnect)
	{
		if (Build.VERSION.SdkInt < BuildVersionCodes.Lollipop)
		{
			BluetoothDevice.ConnectGatt(Application.Context, autoconnect, _gattCallback);
		}
		else if (Build.VERSION.SdkInt < BuildVersionCodes.M)
		{
			Method declaredMethod = BluetoothDevice.Class.GetDeclaredMethod("connectGatt", Class.FromType(typeof(Context)), Java.Lang.Boolean.Type, Class.FromType(typeof(BluetoothGattCallback)), Integer.Type);
			int num = BluetoothDevice.Class.GetDeclaredField("TRANSPORT_LE").GetInt(null);
			declaredMethod.Invoke(BluetoothDevice, Application.Context, false, _gattCallback, num);
		}
		else
		{
			BluetoothDevice.ConnectGatt(Application.Context, autoconnect, _gattCallback, BluetoothTransports.Le);
		}
	}

	public void Disconnect()
	{
		if (_gatt != null)
		{
			IsOperationRequested = true;
			ClearServices();
			_gatt.Disconnect();
		}
		else
		{
			Trace.Message("[Warning]: Can't disconnect {0}. Gatt is null.", base.Name);
		}
	}

	public void CloseGatt()
	{
		_gatt?.Close();
		_gatt = null;
		ClearServices();
	}

	protected override DeviceState GetState()
	{
		switch (((BluetoothManager)Application.Context.GetSystemService("bluetooth")).GetConnectionState(BluetoothDevice, ProfileType.Gatt))
		{
		case ProfileState.Connected:
			if (_gatt == null)
			{
				return DeviceState.Limited;
			}
			return DeviceState.Connected;
		case ProfileState.Connecting:
			return DeviceState.Connecting;
		default:
			return DeviceState.Disconnected;
		}
	}

	private Guid ParseDeviceId()
	{
		byte[] array = new byte[16];
		string macWithoutColons = BluetoothDevice.Address.Replace(":", "");
		(from x in Enumerable.Range(0, macWithoutColons.Length)
			where x % 2 == 0
			select Convert.ToByte(macWithoutColons.Substring(x, 2), 16)).ToArray().CopyTo(array, 10);
		return new Guid(array);
	}

	public static List<AdvertisementRecord> ParseScanRecord(byte[] scanRecord)
	{
		List<AdvertisementRecord> list = new List<AdvertisementRecord>();
		if (scanRecord == null)
		{
			return list;
		}
		byte b;
		for (int i = 0; i < scanRecord.Length; i += b)
		{
			b = scanRecord[i++];
			if (b == 0)
			{
				break;
			}
			int num = scanRecord[i];
			if (num == 0)
			{
				break;
			}
			if (!System.Enum.IsDefined(typeof(AdvertisementRecordType), num))
			{
				Trace.Message("Advertisment record type not defined: {0}", num);
				break;
			}
			byte[] array = new byte[b - 1];
			Array.Copy(scanRecord, i + 1, array, 0, b - 1);
			switch ((AdvertisementRecordType)num)
			{
			case AdvertisementRecordType.UuidsIncomple16Bit:
			case AdvertisementRecordType.UuidsComplete16Bit:
			case AdvertisementRecordType.UuidCom32Bit:
			case AdvertisementRecordType.UuidsIncomplete128Bit:
			case AdvertisementRecordType.UuidsComplete128Bit:
			case AdvertisementRecordType.SsUuids16Bit:
			case AdvertisementRecordType.SsUuids128Bit:
			case AdvertisementRecordType.SsUuids32Bit:
			case AdvertisementRecordType.ServiceDataUuid32Bit:
				Array.Reverse((Array)array);
				break;
			}
			AdvertisementRecord advertisementRecord = new AdvertisementRecord((AdvertisementRecordType)num, array);
			Trace.Message(advertisementRecord.ToString());
			list.Add(advertisementRecord);
		}
		return list;
	}

	public override async Task<bool> UpdateRssiAsync()
	{
		if (_gatt == null || _gattCallback == null)
		{
			Trace.Message("You can't read the RSSI value for disconnected devices except on discovery on Android. Device is {0}", State);
			return false;
		}
		return await TaskBuilder.FromEvent<bool, EventHandler<RssiReadCallbackEventArgs>, EventHandler>(delegate
		{
			_gatt.ReadRemoteRssi();
		}, (Action<bool> complete, Action<System.Exception> reject) => delegate(object sender, RssiReadCallbackEventArgs args)
		{
			if (args.Error == null)
			{
				Trace.Message("Read RSSI for {0} {1}: {2}", base.Id, base.Name, args.Rssi);
				base.Rssi = args.Rssi;
				complete(obj: true);
			}
			else
			{
				Trace.Message($"Failed to read RSSI for device {base.Id}-{base.Name}. {args.Error.Message}");
				complete(obj: false);
			}
		}, delegate(EventHandler<RssiReadCallbackEventArgs> handler)
		{
			_gattCallback.RemoteRssiRead += handler;
		}, delegate(EventHandler<RssiReadCallbackEventArgs> handler)
		{
			_gattCallback.RemoteRssiRead -= handler;
		}, (Action<System.Exception> reject) => delegate
		{
			reject(new System.Exception($"Device {base.Name} disconnected while updating rssi."));
		}, delegate(EventHandler handler)
		{
			_gattCallback.ConnectionInterrupted += handler;
		}, delegate(EventHandler handler)
		{
			_gattCallback.ConnectionInterrupted -= handler;
		});
	}

	protected override async Task<int> RequestMtuNativeAsync(int requestValue)
	{
		if (_gatt == null || _gattCallback == null)
		{
			Trace.Message("You can't request a MTU for disconnected devices. Device is {0}", State);
			return -1;
		}
		if (Build.VERSION.SdkInt < BuildVersionCodes.Lollipop)
		{
			Trace.Message("Request MTU not supported in this Android API level");
			return -1;
		}
		return await TaskBuilder.FromEvent<int, EventHandler<MtuRequestCallbackEventArgs>, EventHandler>(delegate
		{
			_gatt.RequestMtu(requestValue);
		}, (Action<int> complete, Action<System.Exception> reject) => delegate(object sender, MtuRequestCallbackEventArgs args)
		{
			if (args.Error != null)
			{
				Trace.Message($"Failed to request MTU ({requestValue}) for device {base.Id}-{base.Name}. {args.Error.Message}");
				reject(new System.Exception($"Request MTU error: {args.Error.Message}"));
			}
			else
			{
				complete(args.Mtu);
			}
		}, delegate(EventHandler<MtuRequestCallbackEventArgs> handler)
		{
			_gattCallback.MtuRequested += handler;
		}, delegate(EventHandler<MtuRequestCallbackEventArgs> handler)
		{
			_gattCallback.MtuRequested -= handler;
		}, (Action<System.Exception> reject) => delegate
		{
			reject(new System.Exception($"Device {base.Name} disconnected while requesting MTU."));
		}, delegate(EventHandler handler)
		{
			_gattCallback.ConnectionInterrupted += handler;
		}, delegate(EventHandler handler)
		{
			_gattCallback.ConnectionInterrupted -= handler;
		});
	}

	protected override bool UpdateConnectionIntervalNative(ConnectionInterval interval)
	{
		if (_gatt == null || _gattCallback == null)
		{
			Trace.Message("You can't update a connection interval for disconnected devices. Device is {0}", base.State);
			return false;
		}
		if (Build.VERSION.SdkInt < BuildVersionCodes.Lollipop)
		{
			Trace.Message("Update connection interval paramter in this Android API level");
			return false;
		}
		try
		{
			return _gatt.RequestConnectionPriority((GattConnectionPriority)interval);
		}
		catch (System.Exception ex)
		{
			throw new System.Exception($"Update Connection Interval fails with error. {ex.Message}");
		}
	}
}
