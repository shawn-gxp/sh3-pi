using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models.Entity;
using NHL.Services.Support;

namespace NHL.Services;

public interface IMeasurementService : ISyncServiceBase
{
	Task<BindableCollection<Measurement>> GetAllMeasurement(bool isSync = false);

	Task RegisterMeasurement(Measurement measurement);

	Task UpdateMeasurement(Measurement measurement);

	Task DeleteMeasurement(Measurement measurement);
}
