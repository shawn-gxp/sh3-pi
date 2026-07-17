using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.Contracts;

namespace Plugin.BLE.Abstractions;

public abstract class ServiceBase : IService, IDisposable
{
	private readonly List<ICharacteristic> _characteristics = new List<ICharacteristic>();

	public string Name => KnownServices.Lookup(Id).Name;

	public abstract Guid Id { get; }

	public abstract bool IsPrimary { get; }

	public IDevice Device { get; }

	protected ServiceBase(IDevice device)
	{
		Device = device;
	}

	public async Task<IList<ICharacteristic>> GetCharacteristicsAsync()
	{
		if (!_characteristics.Any())
		{
			IEnumerable<ICharacteristic> collection = await GetCharacteristicsNativeAsync();
			_characteristics.AddRange(collection);
		}
		return _characteristics.ToList();
	}

	public async Task<ICharacteristic> GetCharacteristicAsync(Guid id)
	{
		return (await GetCharacteristicsAsync()).FirstOrDefault((ICharacteristic c) => c.Id == id);
	}

	protected abstract Task<IList<ICharacteristic>> GetCharacteristicsNativeAsync();

	public virtual void Dispose()
	{
	}
}
