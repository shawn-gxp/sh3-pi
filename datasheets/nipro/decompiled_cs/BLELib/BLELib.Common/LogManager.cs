namespace BLELib.Common;

public class LogManager
{
	private static ILoggingService _LoggingService;

	public ILoggingService LoggingService { get; set; }

	public static ILoggingService GetLogger()
	{
		return _LoggingService;
	}

	public static void Initialize(object loggingService)
	{
		_LoggingService = new LoggingService(loggingService);
	}
}
