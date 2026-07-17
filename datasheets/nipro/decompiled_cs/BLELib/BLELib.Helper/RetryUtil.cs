using System;
using System.Threading.Tasks;
using BLELib.Common;

namespace BLELib.Helper;

public static class RetryUtil
{
	private static ILoggingService Log = LogManager.GetLogger();

	public static async Task ExecuteAsync(Func<Task> action, int retryCount, int retryIntervalMillisec, Type expectedException)
	{
		Log.Info(string.Format("【BLELib】【RetryUtil】【ExecuteAsync】Start ExecuteAsync. retryCount: {0}, expectedException: {1}", new object[2] { retryCount, expectedException }));
		int count = 0;
		while (true)
		{
			try
			{
				await action();
				Log.Info("【BLELib】【RetryUtil】【ExecuteAsync】Successful ExecuteAsync.");
				break;
			}
			catch (Exception ex)
			{
				if (!((object)ex).GetType().Equals(expectedException) || count >= retryCount)
				{
					Log.Error("【BLELib】【RetryUtil】【ExecuteAsync】Insufficient retry condition, retry was aborted.");
					throw ex;
				}
				Log.Info($"【BLELib】【RetryUtil】【ExecuteAsync】action({count + 1} in {retryCount} times) failed at exception: {((object)ex).GetType()}, message: {ex.Message}. retring...");
				count++;
			}
			await Task.Delay(retryIntervalMillisec);
		}
	}
}
