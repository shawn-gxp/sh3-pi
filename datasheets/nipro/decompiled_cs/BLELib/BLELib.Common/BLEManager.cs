using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using BLELib.BLEDevice;

namespace BLELib.Common;

public class BLEManager : IDisposable
{
	private List<IBLEDevice> DeviceList;

	private bool disposedValue;

	public BLEManager()
	{
		DeviceList = new List<IBLEDevice>();
	}

	public void Initialize()
	{
		foreach (Type item2 in Assembly.Load(new AssemblyName("BLELib")).ExportedTypes.Where((Type x) => typeof(IBLEDevice).GetTypeInfo().IsAssignableFrom(x.GetTypeInfo())))
		{
			if (!(item2.Name == "IBLEDevice") && Activator.CreateInstance(item2) is IBLEDevice item)
			{
				DeviceList.Add(item);
			}
		}
	}

	public IBLEDevice GetDevice(string name)
	{
		return DeviceList.FirstOrDefault((IBLEDevice x) => name.Contains(x.DeviceName));
	}

	protected virtual void Dispose(bool disposing)
	{
		if (disposedValue)
		{
			return;
		}
		if (disposing && DeviceList != null && DeviceList.Count > 0)
		{
			for (int num = DeviceList.Count - 1; num >= 0; num--)
			{
				DeviceList.Remove(DeviceList[num]);
			}
		}
		disposedValue = true;
	}

	public void Dispose()
	{
		Dispose(disposing: true);
	}
}
