using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class UpdateTimezoneService : ServiceProxyBase, IUpdateTimezoneService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public UpdateTimezoneService(IRestClient newRestClient)
		: base("UpdateTimezone", newRestClient)
	{
	}

	public async Task<IgUserModel> UpdateTimezone(UpdateTimezoneModel updateTimezoneModel)
	{
		return await base.RestClient.PostForObject<UpdateTimezoneModel, IgUserModel>(CreateApiUrl(), updateTimezoneModel);
	}
}
