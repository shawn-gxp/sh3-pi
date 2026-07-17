using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Contracts;

public interface IDevice : IDisposable
{
	Guid Id { get; }

	string Name { get; }

	int Rssi { get; }

	object NativeDevice { get; }

	DeviceState State { get; }

	IList<AdvertisementRecord> AdvertisementRecords { get; }

	Task<IList<IService>> GetServicesAsync(CancellationToken cancellationToken = default(CancellationToken));

	Task<IService> GetServiceAsync(Guid id, CancellationToken cancellationToken = default(CancellationToken));

	Task<bool> UpdateRssiAsync();

	Task<int> RequestMtuAsync(int requestValue);

	bool UpdateConnectionInterval(ConnectionInterval interval);
}
