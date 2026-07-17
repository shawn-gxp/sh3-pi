using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using BLELib.Common;

namespace BLELib;

public interface IBLELib
{
	int Mode { get; set; }

	object ILoggingService { set; }

	bool IsScanning { get; }

	event EventHandler<BLELibStatusEventArgs> StateChanged;

	void Initialize();

	Task<IList<string>> Pairing(Guid id, string name, IList<string> param = null);

	Task<object> ReceiveStart(string name, Action<int, int> handler, int timeout, IList<string> param = null, string serialNumber = "");

	Task ReceiveStop();

	Task ReceiveWait(IList<string> names, int timeout, Action<IList<string>> handler);

	Task ScanStart(IList<string> names, int timeout, Action<IList<string>> handler);

	Task ScanStop();

	void Dispose();

	Task ScanPause();

	Task ScanPauseRestart(int msec);

	Task<bool> DeleteUser(string name, int timeout, IList<string> param);

	Task<IList<object>> GetFreeColor(string name, int timeout, IList<string> param);

	Task<bool> RegisterUserColor(string name, int timeout, IList<string> param);
}
