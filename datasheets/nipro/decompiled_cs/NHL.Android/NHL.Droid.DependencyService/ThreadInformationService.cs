using System;
using NHL.Services.DependencyService;

namespace NHL.Droid.DependencyService;

public class ThreadInformationService : IThreadInformationService
{
	public int GetCurrentThreadId()
	{
		return Environment.CurrentManagedThreadId;
	}
}
