using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class UpdateIgUserService : ServiceProxyBase, IUpdateIgUserService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public UpdateIgUserService(IRestClient newRestClient)
		: base("UpdateIgUser", newRestClient)
	{
	}

	public async Task<IgUserModel> UpdateIgUser(UpdateIgUserModel updateIgUserModel)
	{
		return await base.RestClient.PostForObject<UpdateIgUserModel, IgUserModel>(CreateApiUrl(), updateIgUserModel);
	}
}
