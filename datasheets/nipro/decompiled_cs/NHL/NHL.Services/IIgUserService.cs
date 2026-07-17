using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IIgUserService : IUseTokenService
{
	Task<IgUserModel> IgUser(IgUserRequestModel user);

	Task<IgUserModel> RestoreIgUser(IgUserRestoreRequestModel backupCode);
}
