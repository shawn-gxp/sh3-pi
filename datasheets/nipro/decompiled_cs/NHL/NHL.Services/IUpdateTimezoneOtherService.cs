using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IUpdateTimezoneOtherService : IUseTokenService
{
	Task<IgUserModel> UpdateTimezoneOther(UpdateTimezoneOtherModel updateTimezoneOtherModel);
}
