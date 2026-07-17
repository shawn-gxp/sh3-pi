using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface ISharerUpdateService : IUseTokenService
{
	Task<SharerRegisterResponseModel> UpdateSharer(SharerRegisterRequestModel sharer);
}
