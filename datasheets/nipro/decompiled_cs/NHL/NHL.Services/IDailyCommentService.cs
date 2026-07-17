using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface IDailyCommentService : ISyncServiceBase
{
	Task<BindableCollection<DailyComments>> GetAllDailyComment(bool isSync = false);

	Task RegisterDailyComment(DailyComments dailyComment);

	Task UpdateDailyComment(DailyComments dailyComments);
}
