using System.Threading.Tasks;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services;

public interface INoticeService : IUseTokenService
{
	Task<NoticeListResponseModel> GetNoticeList(NoticeListRequestModel req);

	Task<NoticeDetailResponseModel> GetNoticeDetail(NoticeDetailRequestModel req);
}
