using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IVersionService : IUseTokenService
{
	Task<VersionModel> GetVersion(string os);
}
