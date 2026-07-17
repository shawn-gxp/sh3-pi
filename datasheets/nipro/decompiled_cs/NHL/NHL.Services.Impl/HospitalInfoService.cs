using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class HospitalInfoService : ServiceProxyBase, IHospitalInfoService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public HospitalInfoService(IRestClient newRestClient)
		: base("HospitalInfoV2", newRestClient)
	{
	}

	public async Task<HospitalInfoResponseModel> GetHospitalInfo(string id)
	{
		return await base.RestClient.GetForObject<HospitalInfoResponseModel>(CreateApiUrl() + "?hospitalId=" + id);
	}
}
