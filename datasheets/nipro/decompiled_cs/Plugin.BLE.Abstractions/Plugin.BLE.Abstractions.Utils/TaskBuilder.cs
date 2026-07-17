using System;
using System.Threading;
using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Utils;

public static class TaskBuilder
{
	public static Task<TReturn> FromEvent<TReturn, TEventHandler>(Action execute, Func<Action<TReturn>, Action<Exception>, TEventHandler> getCompleteHandler, Action<TEventHandler> subscribeComplete, Action<TEventHandler> unsubscribeComplete, CancellationToken token = default(CancellationToken))
	{
		return FromEvent(execute, getCompleteHandler, subscribeComplete, unsubscribeComplete, (Action<Exception> reject) => (object)null, delegate
		{
		}, delegate
		{
		}, token);
	}

	public static async Task<TReturn> FromEvent<TReturn, TEventHandler, TRejectHandler>(Action execute, Func<Action<TReturn>, Action<Exception>, TEventHandler> getCompleteHandler, Action<TEventHandler> subscribeComplete, Action<TEventHandler> unsubscribeComplete, Func<Action<Exception>, TRejectHandler> getRejectHandler, Action<TRejectHandler> subscribeReject, Action<TRejectHandler> unsubscribeReject, CancellationToken token = default(CancellationToken))
	{
		TaskCompletionSource<TReturn> tcs = new TaskCompletionSource<TReturn>();
		Action<TReturn> arg = delegate(TReturn args)
		{
			tcs.TrySetResult(args);
		};
		Action<Exception> arg2 = delegate(Exception ex)
		{
			tcs.TrySetException(ex);
		};
		Action<Exception> arg3 = delegate(Exception ex)
		{
			tcs.TrySetException(ex);
		};
		TEventHandler handler = getCompleteHandler(arg, arg2);
		TRejectHandler rejectHandler = getRejectHandler(arg3);
		try
		{
			subscribeComplete(handler);
			subscribeReject(rejectHandler);
			using (token.Register(delegate
			{
				tcs.TrySetCanceled();
			}, useSynchronizationContext: false))
			{
				execute();
				return await tcs.Task;
			}
		}
		finally
		{
			unsubscribeReject(rejectHandler);
			unsubscribeComplete(handler);
		}
	}
}
