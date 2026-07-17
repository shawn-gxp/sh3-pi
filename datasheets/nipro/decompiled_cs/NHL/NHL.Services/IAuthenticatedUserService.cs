using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IAuthenticatedUserService : IUseTokenService
{
	Task<AuthenticationModel> GetAuthenticatedUser();
}
