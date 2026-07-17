using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Utils;

public class BleCommandQueue
{
	private object _lock = new object();

	private IBleCommand _currentCommand;

	public Queue<IBleCommand> CommandQueue { get; set; }

	public Task<T> EnqueueAsync<T>(Func<Task<T>> bleCommand, int timeOutInSeconds = 10)
	{
		BleCommand<T> bleCommand2 = new BleCommand<T>(bleCommand, timeOutInSeconds);
		lock (_lock)
		{
			CommandQueue.Enqueue(bleCommand2);
		}
		TryExecuteNext();
		return bleCommand2.ExecutingTask;
	}

	public void CancelPending()
	{
		lock (_lock)
		{
			foreach (IBleCommand item in CommandQueue)
			{
				item.Cancel();
			}
			CommandQueue.Clear();
		}
	}

	private async void TryExecuteNext()
	{
		lock (_lock)
		{
			if (_currentCommand != null || !CommandQueue.Any())
			{
				return;
			}
			_currentCommand = CommandQueue.Dequeue();
		}
		await _currentCommand.ExecuteAsync();
		lock (_lock)
		{
			_currentCommand = null;
		}
		TryExecuteNext();
	}
}
