using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class VersionService : ServiceProxyBase, IVersionService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public VersionService(IRestClient newRestClient)
		: base("Version", newRestClient)
	{
	}

	public async Task<VersionModel> GetVersion(string os)
	{
		return await base.RestClient.GetForObject<VersionModel>(CreateApiUrl() + "?os=" + os);
	}
}
