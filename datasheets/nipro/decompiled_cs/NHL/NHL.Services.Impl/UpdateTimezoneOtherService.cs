using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class UpdateTimezoneOtherService : ServiceProxyBase, IUpdateTimezoneOtherService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public UpdateTimezoneOtherService(IRestClient newRestClient)
		: base("UpdateTimezoneOther", newRestClient)
	{
	}

	public async Task<IgUserModel> UpdateTimezoneOther(UpdateTimezoneOtherModel updateTimezoneOtherModel)
	{
		return await base.RestClient.PostForObject<UpdateTimezoneOtherModel, IgUserModel>(CreateApiUrl(), updateTimezoneOtherModel);
	}
}
