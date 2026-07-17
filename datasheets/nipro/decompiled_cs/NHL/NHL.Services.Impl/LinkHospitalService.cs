using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class LinkHospitalService : ServiceProxyBase, ILinkHospitalService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public LinkHospitalService(IRestClient newRestClient)
		: base("LinkHospital", newRestClient)
	{
	}

	public async Task<LinkHospitalResultModel> PostLinkHospital(LinkHospitalRequestModel linkHospitalRequest)
	{
		return await base.RestClient.PostForObject<LinkHospitalRequestModel, LinkHospitalResultModel>(CreateApiUrl(), linkHospitalRequest);
	}

	public async Task<LinkHospitalResultModel> PutLinkHospital(LinkHospitalRequestModel linkHospitalRequest)
	{
		return await base.RestClient.PutForObject<LinkHospitalRequestModel, LinkHospitalResultModel>(CreateApiUrl(), linkHospitalRequest);
	}
}
