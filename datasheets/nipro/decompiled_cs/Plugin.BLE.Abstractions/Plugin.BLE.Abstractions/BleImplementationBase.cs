using System;
using System.Threading;
using Plugin.BLE.Abstractions.Contracts;
using Plugin.BLE.Abstractions.EventArgs;
using Plugin.BLE.Abstractions.Utils;

namespace Plugin.BLE.Abstractions;

public abstract class BleImplementationBase : IBluetoothLE
{
	private readonly Lazy<IAdapter> _adapter;

	private BluetoothState _state;

	public bool IsAvailable => _state != BluetoothState.Unavailable;

	public bool IsOn => _state == BluetoothState.On;

	public IAdapter Adapter => _adapter.Value;

	public BluetoothState State
	{
		get
		{
			return _state;
		}
		protected set
		{
			if (_state != value)
			{
				BluetoothState state = _state;
				_state = value;
				this.StateChanged?.Invoke(this, new BluetoothStateChangedArgs(state, _state));
			}
		}
	}

	public event EventHandler<BluetoothStateChangedArgs> StateChanged;

	protected BleImplementationBase()
	{
		_adapter = new Lazy<IAdapter>(CreateAdapter, LazyThreadSafetyMode.PublicationOnly);
	}

	public void Initialize()
	{
		InitializeNative();
		State = GetInitialStateNative();
	}

	private IAdapter CreateAdapter()
	{
		if (!IsAvailable)
		{
			return new FakeAdapter();
		}
		return CreateNativeAdapter();
	}

	protected abstract void InitializeNative();

	protected abstract BluetoothState GetInitialStateNative();

	protected abstract IAdapter CreateNativeAdapter();
}
