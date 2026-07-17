using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class UpdateIgPasswordService : ServiceProxyBase, IUpdateIgPasswordService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public UpdateIgPasswordService(IRestClient newRestClient)
		: base("UpdateIgPassword", newRestClient)
	{
	}

	public async Task<IgUserModel> UpdatePassword(IgUserPasswordModel igUserPasswordModel)
	{
		return await base.RestClient.PostForObject<IgUserPasswordModel, IgUserModel>(CreateApiUrl(), igUserPasswordModel);
	}
}
