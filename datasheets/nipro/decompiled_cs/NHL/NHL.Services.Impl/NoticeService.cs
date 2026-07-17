using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class NoticeService : ServiceProxyBase, INoticeService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public NoticeService(IRestClient newRestClient)
		: base("Notice", newRestClient)
	{
	}

	public async Task<NoticeListResponseModel> GetNoticeList(NoticeListRequestModel req)
	{
		return await base.RestClient.GetForObject<NoticeListResponseModel>(CreateApiUrl() + $"?count={req.Count}");
	}

	public async Task<NoticeDetailResponseModel> GetNoticeDetail(NoticeDetailRequestModel req)
	{
		return await base.RestClient.GetForObject<NoticeDetailResponseModel>(CreateApiUrl() + "?id=" + req.NoticeId);
	}
}
