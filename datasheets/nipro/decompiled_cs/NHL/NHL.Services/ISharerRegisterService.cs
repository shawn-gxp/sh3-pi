using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface ISharerRegisterService : IUseTokenService
{
	Task<SharerRegisterResponseModel> RegisterSharer(SharerRegisterRequestModel sharer);
}
