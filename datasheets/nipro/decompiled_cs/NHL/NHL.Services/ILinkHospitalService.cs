using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface ILinkHospitalService : IUseTokenService
{
	Task<LinkHospitalResultModel> PostLinkHospital(LinkHospitalRequestModel linkHospitalRequest);

	Task<LinkHospitalResultModel> PutLinkHospital(LinkHospitalRequestModel linkHospitalRequest);
}
