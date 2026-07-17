using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class AuthenticatedUserService : ServiceProxyBase, IAuthenticatedUserService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public AuthenticatedUserService(IRestClient newRestClient)
		: base("AuthenticatedIgUser", newRestClient)
	{
	}

	public async Task<AuthenticationModel> GetAuthenticatedUser()
	{
		return await base.RestClient.GetForObject<AuthenticationModel>(CreateApiUrl());
	}
}
