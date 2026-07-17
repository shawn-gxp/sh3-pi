using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IPhotographService : ISyncServiceBase
{
	Task<BindableCollection<Photograph>> GetAllPhotograph(bool isSync = false);

	Task RegisterPhotograph(Photograph photograph);

	Task UpdatePhotograph(Photograph photograph);
}
