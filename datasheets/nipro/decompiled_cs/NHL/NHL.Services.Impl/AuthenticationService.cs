using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class AuthenticationService : ServiceProxyBase, IAuthenticationService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public AuthenticationService(IRestClient newRestClient)
		: base("Authentication", newRestClient)
	{
	}

	public async Task<AuthenticationModel> Authenticate(AuthenticationRequestModel auth)
	{
		return await base.RestClient.PostForObject<AuthenticationRequestModel, AuthenticationModel>(CreateApiUrl(), auth);
	}
}
