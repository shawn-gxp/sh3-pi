using System.Threading.Tasks;
using NHL.Framework;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class UnlinkHospitalService : ServiceProxyBase, IUnlinkHospitalService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public UnlinkHospitalService(IRestClient newRestClient)
		: base("UnlinkHospital", newRestClient)
	{
	}

	public async Task UnlinkHospital(string hospitalId)
	{
		await base.RestClient.GetForMessage(CreateApiUrl() + "?hospitalId=" + hospitalId);
	}
}
