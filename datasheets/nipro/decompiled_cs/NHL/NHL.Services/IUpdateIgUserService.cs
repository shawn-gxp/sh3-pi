using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IUpdateIgUserService : IUseTokenService
{
	Task<IgUserModel> UpdateIgUser(UpdateIgUserModel updateIgUserModel);
}
