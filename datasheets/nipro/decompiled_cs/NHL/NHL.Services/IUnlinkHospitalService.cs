using System.Threading.Tasks;
using NHL.Services.Support;

namespace NHL.Services;

public interface IUnlinkHospitalService : IUseTokenService
{
	Task UnlinkHospital(string hospitalId);
}
