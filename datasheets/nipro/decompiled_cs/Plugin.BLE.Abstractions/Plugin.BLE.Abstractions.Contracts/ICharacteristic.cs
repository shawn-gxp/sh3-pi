using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.EventArgs;

namespace Plugin.BLE.Abstractions.Contracts;

public interface ICharacteristic
{
	Guid Id { get; }

	string Uuid { get; }

	string Name { get; }

	byte[] Value { get; }

	string StringValue { get; }

	CharacteristicPropertyType Properties { get; }

	CharacteristicWriteType WriteType { get; set; }

	bool CanRead { get; }

	bool CanWrite { get; }

	bool CanUpdate { get; }

	IService Service { get; }

	event EventHandler<CharacteristicUpdatedEventArgs> ValueUpdated;

	Task<byte[]> ReadAsync(CancellationToken cancellationToken = default(CancellationToken));

	Task<bool> WriteAsync(byte[] data, CancellationToken cancellationToken = default(CancellationToken));

	Task StartUpdatesAsync();

	Task StopUpdatesAsync();

	Task<IList<IDescriptor>> GetDescriptorsAsync(CancellationToken cancellationToken = default(CancellationToken));

	Task<IDescriptor> GetDescriptorAsync(Guid id, CancellationToken cancellationToken = default(CancellationToken));
}
