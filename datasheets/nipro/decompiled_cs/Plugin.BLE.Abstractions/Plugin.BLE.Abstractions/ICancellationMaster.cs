using System.Threading;

namespace Plugin.BLE.Abstractions;

public interface ICancellationMaster
{
	CancellationTokenSource TokenSource { get; set; }
}
