using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IUpdateIgGlucoseTargetService : IUseTokenService
{
	Task<IgUserModel> UpdateGlucoseTargetThreshold(GlucoseTargetThresholdModel glucoseTargetThresholdModel);
}
