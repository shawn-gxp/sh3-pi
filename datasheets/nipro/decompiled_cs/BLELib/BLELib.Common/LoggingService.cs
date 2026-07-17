using System;

namespace BLELib.Common;

public class LoggingService : ILoggingService
{
	private static dynamic _LoggingService;

	public LoggingService(object loggingService)
	{
		_LoggingService = loggingService;
	}

	public void Debug(string message)
	{
		_LoggingService?.Debug(message);
	}

	public void Debug(string format, params object[] args)
	{
		_LoggingService?.Debug(format, args);
	}

	public void Error(string message)
	{
		_LoggingService?.Error(message);
	}

	public void Error(string format, params object[] args)
	{
		_LoggingService?.Error(format, args);
	}

	public void Error(Exception e, string message)
	{
		_LoggingService?.Error(e, message);
	}

	public void Error(Exception e, string format, params object[] args)
	{
		_LoggingService?.Error(e, format, args);
	}

	public void Fatal(string message)
	{
		_LoggingService?.Fatal(message);
	}

	public void Fatal(string format, params object[] args)
	{
		_LoggingService?.Fatal(format, args);
	}

	public void Fatal(Exception e, string message)
	{
		_LoggingService?.Fatal(e, message);
	}

	public void Fatal(Exception e, string format, params object[] args)
	{
		_LoggingService?.Fatal(e, format, args);
	}

	public void Info(string message)
	{
		_LoggingService?.Info(message);
	}

	public void Info(string format, params object[] args)
	{
		_LoggingService?.Info(format, args);
	}

	public void Trace(string message)
	{
		_LoggingService?.Trace(message);
	}

	public void Trace(string format, params object[] args)
	{
		_LoggingService?.Trace(format, args);
	}

	public void Warn(string message)
	{
		_LoggingService?.Warn(message);
	}

	public void Warn(string format, params object[] args)
	{
		_LoggingService?.Warn(format, args);
	}
}
