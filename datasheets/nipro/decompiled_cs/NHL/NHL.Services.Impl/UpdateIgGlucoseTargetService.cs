using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class UpdateIgGlucoseTargetService : ServiceProxyBase, IUpdateIgGlucoseTargetService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public UpdateIgGlucoseTargetService(IRestClient newRestClient)
		: base("UpdateGlucoseValue", newRestClient)
	{
	}

	public async Task<IgUserModel> UpdateGlucoseTargetThreshold(GlucoseTargetThresholdModel glucoseTargetThresholdModel)
	{
		return await base.RestClient.PostForObject<GlucoseTargetThresholdModel, IgUserModel>(CreateApiUrl(), glucoseTargetThresholdModel);
	}
}
