using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;

namespace Plugin.BLE.Abstractions;

public abstract class CharacteristicBase : ICharacteristic
{
	private IList<IDescriptor> _descriptors;

	private CharacteristicWriteType _writeType;

	public abstract Guid Id { get; }

	public abstract string Uuid { get; }

	public abstract byte[] Value { get; }

	public string Name => KnownCharacteristics.Lookup(Id).Name;

	public abstract CharacteristicPropertyType Properties { get; }

	public IService Service { get; }

	public CharacteristicWriteType WriteType
	{
		get
		{
			return _writeType;
		}
		set
		{
			if ((value == CharacteristicWriteType.WithResponse && !Properties.HasFlag(CharacteristicPropertyType.Write)) || (value == CharacteristicWriteType.WithoutResponse && !Properties.HasFlag(CharacteristicPropertyType.WriteWithoutResponse)))
			{
				throw new InvalidOperationException($"Write type {value} is not supported");
			}
			_writeType = value;
		}
	}

	public bool CanRead => Properties.HasFlag(CharacteristicPropertyType.Read);

	public bool CanUpdate => Properties.HasFlag(CharacteristicPropertyType.Notify) | Properties.HasFlag(CharacteristicPropertyType.Indicate);

	public bool CanWrite => Properties.HasFlag(CharacteristicPropertyType.Write) | Properties.HasFlag(CharacteristicPropertyType.WriteWithoutResponse);

	public string StringValue
	{
		get
		{
			byte[] value = Value;
			if (value == null)
			{
				return string.Empty;
			}
			return Encoding.UTF8.GetString(value, 0, value.Length);
		}
	}

	public abstract event EventHandler<CharacteristicUpdatedEventArgs> ValueUpdated;

	protected CharacteristicBase(IService service)
	{
		Service = service;
	}

	public async Task<byte[]> ReadAsync(CancellationToken cancellationToken = default(CancellationToken))
	{
		if (!CanRead)
		{
			throw new InvalidOperationException("Characteristic does not support read.");
		}
		Trace.Message("Characteristic.ReadAsync");
		return await ReadNativeAsync();
	}

	public async Task<bool> WriteAsync(byte[] data, CancellationToken cancellationToken = default(CancellationToken))
	{
		if (data == null)
		{
			throw new ArgumentNullException("data");
		}
		if (!CanWrite)
		{
			throw new InvalidOperationException("Characteristic does not support write.");
		}
		CharacteristicWriteType writeType = GetWriteType();
		Trace.Message("Characteristic.WriteAsync");
		return await WriteNativeAsync(data, writeType);
	}

	private CharacteristicWriteType GetWriteType()
	{
		if (WriteType != CharacteristicWriteType.Default)
		{
			return WriteType;
		}
		if (!Properties.HasFlag(CharacteristicPropertyType.Write))
		{
			return CharacteristicWriteType.WithoutResponse;
		}
		return CharacteristicWriteType.WithResponse;
	}

	public Task StartUpdatesAsync()
	{
		if (!CanUpdate)
		{
			throw new InvalidOperationException("Characteristic does not support update.");
		}
		Trace.Message("Characteristic.StartUpdates");
		return StartUpdatesNativeAsync();
	}

	public Task StopUpdatesAsync()
	{
		if (!CanUpdate)
		{
			throw new InvalidOperationException("Characteristic does not support update.");
		}
		return StopUpdatesNativeAsync();
	}

	public async Task<IList<IDescriptor>> GetDescriptorsAsync(CancellationToken cancellationToken = default(CancellationToken))
	{
		if (_descriptors == null)
		{
			CharacteristicBase characteristicBase = this;
			_ = characteristicBase._descriptors;
			characteristicBase._descriptors = await GetDescriptorsNativeAsync();
		}
		return _descriptors;
	}

	public async Task<IDescriptor> GetDescriptorAsync(Guid id, CancellationToken cancellationToken = default(CancellationToken))
	{
		return (await GetDescriptorsAsync().ConfigureAwait(continueOnCapturedContext: false)).FirstOrDefault((IDescriptor d) => d.Id == id);
	}

	protected abstract Task<IList<IDescriptor>> GetDescriptorsNativeAsync();

	protected abstract Task<byte[]> ReadNativeAsync();

	protected abstract Task<bool> WriteNativeAsync(byte[] data, CharacteristicWriteType writeType);

	protected abstract Task StartUpdatesNativeAsync();

	protected abstract Task StopUpdatesNativeAsync();
}
