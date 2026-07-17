using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IAuthenticationService : IUseTokenService
{
	Task<AuthenticationModel> Authenticate(AuthenticationRequestModel auth);
}
