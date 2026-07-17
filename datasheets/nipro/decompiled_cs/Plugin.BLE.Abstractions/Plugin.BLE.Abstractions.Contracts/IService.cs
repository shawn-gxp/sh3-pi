using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Contracts;

public interface IService : IDisposable
{
	Guid Id { get; }

	string Name { get; }

	bool IsPrimary { get; }

	IDevice Device { get; }

	Task<IList<ICharacteristic>> GetCharacteristicsAsync();

	Task<ICharacteristic> GetCharacteristicAsync(Guid id);
}
