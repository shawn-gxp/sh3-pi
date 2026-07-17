using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IUpdateTimezoneService : IUseTokenService
{
	Task<IgUserModel> UpdateTimezone(UpdateTimezoneModel updateTimezoneModel);
}
