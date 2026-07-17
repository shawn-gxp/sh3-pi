using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class SharerUpdateService : ServiceProxyBase, ISharerUpdateService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public SharerUpdateService(IRestClient newRestClient)
		: base("SharerUpdate", newRestClient)
	{
	}

	public async Task<SharerRegisterResponseModel> UpdateSharer(SharerRegisterRequestModel sharer)
	{
		return await base.RestClient.PostForObject<SharerRegisterRequestModel, SharerRegisterResponseModel>(CreateApiUrl(), sharer);
	}
}
