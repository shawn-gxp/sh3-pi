using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class SharerRegisterService : ServiceProxyBase, ISharerRegisterService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public SharerRegisterService(IRestClient newRestClient)
		: base("SharerRegister", newRestClient)
	{
	}

	public async Task<SharerRegisterResponseModel> RegisterSharer(SharerRegisterRequestModel sharer)
	{
		return await base.RestClient.PostForObject<SharerRegisterRequestModel, SharerRegisterResponseModel>(CreateApiUrl(), sharer);
	}
}
