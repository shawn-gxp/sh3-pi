using System.Threading.Tasks;

namespace Plugin.BLE.Abstractions.Utils;

public interface IBleCommand
{
	bool IsExecuting { get; }

	int TimeoutInMiliSeconds { get; }

	Task ExecuteAsync();

	void Cancel();
}
