using NHL.ViewModels.Event;

namespace NHL.Services.DependencyService;

public interface IRequestBluetooth
{
	void Request();

	void ResetCentralManager();

	CentralManagerEvent.CentralManagerState GetState();
}
