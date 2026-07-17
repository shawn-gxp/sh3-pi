using System;
using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Utils;

public class BleCommand<T> : IBleCommand
{
	private Func<Task<T>> _taskSource;

	private TaskCompletionSource<T> _taskCompletionSource;

	public int TimeoutInMiliSeconds { get; }

	public Task<T> ExecutingTask => _taskCompletionSource.Task;

	public bool IsExecuting { get; private set; }

	public BleCommand(Func<Task<T>> taskSource, int timeoutInSeconds)
	{
		_taskSource = taskSource;
		TimeoutInMiliSeconds = timeoutInSeconds;
		_taskCompletionSource = new TaskCompletionSource<T>();
	}

	public async Task ExecuteAsync()
	{
		_ = 1;
		try
		{
			IsExecuting = true;
			Task<T> source = _taskSource();
			object obj = source;
			if (obj != await Task.WhenAny(source, Task.Delay(TimeoutInMiliSeconds)))
			{
				throw new TimeoutException("Timed out while executing ble task.");
			}
			TaskCompletionSource<T> taskCompletionSource = _taskCompletionSource;
			taskCompletionSource.TrySetResult(await source);
		}
		catch (TaskCanceledException)
		{
			_taskCompletionSource.TrySetCanceled();
		}
		catch (Exception exception)
		{
			_taskCompletionSource.TrySetException(exception);
		}
		finally
		{
			IsExecuting = false;
		}
	}

	public void Cancel()
	{
		_taskCompletionSource.TrySetCanceled();
	}
}
