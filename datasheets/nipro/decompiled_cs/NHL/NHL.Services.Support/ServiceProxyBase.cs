using NHL.Framework;

namespace NHL.Services.Support;

public abstract class ServiceProxyBase
{
	private string baseAddress;

	public IRestClient RestClient { get; set; }

	public string BaseAddress => baseAddress;

	protected ServiceProxyBase(IRestClient newRestClient)
		: this(string.Empty, newRestClient)
	{
	}

	protected ServiceProxyBase(string newBaseAddress, IRestClient newRestClient)
	{
		baseAddress = newBaseAddress;
		RestClient = newRestClient;
	}

	public string CreateTableUrl()
	{
		return RestClient.BaseUrl + "/tables/";
	}

	public string CreateTableUrl(string path)
	{
		return CreateTableUrl() + "/" + BaseAddress + "/" + path;
	}

	public string CreateApiUrl()
	{
		return RestClient.BaseUrl + "/api/" + BaseAddress;
	}

	public string CreateApiUrl(string path)
	{
		return CreateApiUrl() + "/" + path;
	}
}
