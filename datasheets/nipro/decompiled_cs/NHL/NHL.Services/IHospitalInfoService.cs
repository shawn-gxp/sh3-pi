using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IHospitalInfoService : IUseTokenService
{
	Task<HospitalInfoResponseModel> GetHospitalInfo(string id);
}
