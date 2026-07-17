using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class IgUserService : ServiceProxyBase, IIgUserService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public IgUserService(IRestClient newRestClient)
		: base("IgUser", newRestClient)
	{
	}

	public async Task<IgUserModel> IgUser(IgUserRequestModel user)
	{
		return await base.RestClient.PostForObject<IgUserRequestModel, IgUserModel>(CreateTableUrl() + base.BaseAddress, user);
	}

	public async Task<IgUserModel> RestoreIgUser(IgUserRestoreRequestModel backupCode)
	{
		return await base.RestClient.PostForObject<IgUserRestoreRequestModel, IgUserModel>(base.RestClient.BaseUrl + "/api/RestoreIgUserV2", backupCode);
	}
}
