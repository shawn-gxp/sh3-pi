using System.Threading.Tasks;
using NHL.Framework;
using NHL.Models;
using NHL.Services.Support;

namespace NHL.Services.Impl;

public class LogfileTransferService : ServiceProxyBase, ILogfileTransferService, IUseTokenService
{
	public string Token
	{
		set
		{
			base.RestClient.Token = value;
		}
	}

	public LogfileTransferService(IRestClient newRestClient)
		: base("LogfileTransferService", newRestClient)
	{
	}

	public async Task Transfer(LogfileTransferModel log)
	{
		await base.RestClient.PostForMessage("https://nhlfuncappsprd.azurewebsites.net/api/CrashReportJSWebhook?code=nAG3kdqpjMuNsN04WncLzsVSaTrxMmMkcR1z5ymCgxaq4a0kjpz8dQ==", log);
	}
}
