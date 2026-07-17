using System;
using System.Threading;
using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Contracts;

public interface IDescriptor
{
	Guid Id { get; }

	string Name { get; }

	byte[] Value { get; }

	ICharacteristic Characteristic { get; }

	Task<byte[]> ReadAsync(CancellationToken cancellationToken = default(CancellationToken));

	Task WriteAsync(byte[] data, CancellationToken cancellationToken = default(CancellationToken));
}
