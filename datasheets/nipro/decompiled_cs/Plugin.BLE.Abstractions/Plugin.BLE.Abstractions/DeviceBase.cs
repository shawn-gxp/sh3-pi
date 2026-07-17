using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.Contracts;

namespace Plugin.BLE.Abstractions;

public abstract class DeviceBase : IDevice, IDisposable, ICancellationMaster
{
	protected readonly IAdapter Adapter;

	protected readonly List<IService> KnownServices = new List<IService>();

	public Guid Id { get; protected set; }

	public string Name { get; protected set; }

	public int Rssi { get; protected set; }

	public DeviceState State => GetState();

	public IList<AdvertisementRecord> AdvertisementRecords { get; protected set; }

	public abstract object NativeDevice { get; }

	CancellationTokenSource ICancellationMaster.TokenSource { get; set; } = new CancellationTokenSource();

	protected DeviceBase(IAdapter adapter)
	{
		Adapter = adapter;
	}

	public async Task<IList<IService>> GetServicesAsync(CancellationToken cancellationToken = default(CancellationToken))
	{
		if (!KnownServices.Any())
		{
			using (this.GetCombinedSource(cancellationToken))
			{
				IEnumerable<IService> collection = await GetServicesNativeAsync();
				KnownServices.AddRange(collection);
			}
		}
		return KnownServices;
	}

	public async Task<IService> GetServiceAsync(Guid id, CancellationToken cancellationToken = default(CancellationToken))
	{
		return (await GetServicesAsync(cancellationToken)).FirstOrDefault((IService x) => x.Id == id);
	}

	public async Task<int> RequestMtuAsync(int requestValue)
	{
		return await RequestMtuNativeAsync(requestValue);
	}

	public bool UpdateConnectionInterval(ConnectionInterval interval)
	{
		return UpdateConnectionIntervalNative(interval);
	}

	public abstract Task<bool> UpdateRssiAsync();

	protected abstract DeviceState GetState();

	protected abstract Task<IEnumerable<IService>> GetServicesNativeAsync();

	protected abstract Task<int> RequestMtuNativeAsync(int requestValue);

	protected abstract bool UpdateConnectionIntervalNative(ConnectionInterval interval);

	public override string ToString()
	{
		return Name;
	}

	public void Dispose()
	{
		Adapter.DisconnectDeviceAsync(this);
	}

	public void ClearServices()
	{
		this.CancelEverythingAndReInitialize();
		foreach (IService knownService in KnownServices)
		{
			try
			{
				knownService.Dispose();
			}
			catch (Exception ex)
			{
				Trace.Message("Exception while cleanup of service: {0}", ex.Message);
			}
		}
		KnownServices.Clear();
	}

	public override bool Equals(object other)
	{
		if (other == null)
		{
			return false;
		}
		if ((object)other.GetType() != GetType())
		{
			return false;
		}
		DeviceBase deviceBase = (DeviceBase)other;
		return Id == deviceBase.Id;
	}

	public override int GetHashCode()
	{
		return Id.GetHashCode();
	}
}
