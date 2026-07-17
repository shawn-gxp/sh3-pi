using System;
using NHL.Services.DependencyService;
using NLog;

namespace NHL.Droid.DependencyService;

public class LoggingService : ILoggingService
{
	private ILogger logger = LogManager.GetCurrentClassLogger();

	public void Debug(string message)
	{
		logger?.Debug(message);
	}

	public void Debug(string format, params object[] args)
	{
		logger?.Debug(format, args);
	}

	public void Error(string message)
	{
		logger.Error(message);
	}

	public void Error(string format, params object[] args)
	{
		logger?.Error(format, args);
	}

	public void Error(Exception e, string message)
	{
		logger?.Error(message);
	}

	public void Error(Exception e, string format, params object[] args)
	{
		logger?.Error(e, format, args);
	}

	public void Fatal(string message)
	{
		logger?.Fatal(message);
	}

	public void Fatal(string format, params object[] args)
	{
		logger?.Fatal(format, args);
	}

	public void Fatal(Exception e, string message)
	{
		logger?.Fatal(message);
	}

	public void Fatal(Exception e, string format, params object[] args)
	{
		logger?.Fatal(e, format, args);
	}

	public void Info(string message)
	{
		logger?.Info(message);
	}

	public void Info(string format, params object[] args)
	{
		logger?.Info(format, args);
	}

	public void Trace(string message)
	{
		logger?.Trace(message);
	}

	public void Trace(string format, params object[] args)
	{
		logger?.Trace(format, args);
	}

	public void Warn(string message)
	{
		logger?.Warn(message);
	}

	public void Warn(string format, params object[] args)
	{
		logger?.Warn(format, args);
	}
}
