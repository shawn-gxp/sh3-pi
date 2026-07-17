using System;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using NHL.Models.Support;
using NHL.ViewModels.Utils;

namespace NHL.Framework;

public class RestClient : IRestClient
{
	private const int TIMEOUT_MINUTES = 3;

	private HttpClient httpClient;

	public string Token { get; set; }

	public string BaseUrl => "https://api.niprogenkinote.jp/";

	public async Task<string> Get(string path)
	{
		return await HttpCore(async () => await httpClient.GetStringAsync(path));
	}

	public async Task<HttpResponseMessage> GetForMessage(string path)
	{
		return await HttpCore(async () => await httpClient.GetAsync(path));
	}

	public async Task<TResult> GetForObject<TResult>(string path) where TResult : class
	{
		HttpResponseMessage response = await GetForMessage(path);
		ThrowExceptionIfErrorExists(response);
		return JsonUtils.ConvertResponseToModel<TResult>(response);
	}

	public async Task<HttpResponseMessage> PostForMessage<T>(string path, T payload) where T : ModelBase
	{
		return await HttpCore(async () => await httpClient.PostAsync(path, JsonUtils.ConvertDtoToHttpContent(payload)));
	}

	public async Task<TResult> PostForObject<T, TResult>(string path, T payload) where T : ModelBase where TResult : ModelBase
	{
		HttpResponseMessage response = await PostForMessage(path, payload);
		ThrowExceptionIfErrorExists(response);
		return JsonUtils.ConvertResponseToModel<TResult>(response);
	}

	public async Task<HttpResponseMessage> PutForMessage<T>(string path, T payload) where T : ModelBase
	{
		return await HttpCore(async () => await httpClient.PutAsync(path, JsonUtils.ConvertDtoToHttpContent(payload)));
	}

	public async Task<TResult> PutForObject<T, TResult>(string path, T payload) where T : ModelBase where TResult : ModelBase
	{
		HttpResponseMessage response = await PutForMessage(path, payload);
		ThrowExceptionIfErrorExists(response);
		return JsonUtils.ConvertResponseToModel<TResult>(response);
	}

	protected void ThrowExceptionIfErrorExists(HttpResponseMessage response)
	{
		if (response.StatusCode == HttpStatusCode.Unauthorized)
		{
			throw new HttpUnauthorizedErrorException();
		}
		switch ((int)response.StatusCode / 100)
		{
		case 4:
			throw new HttpClientErrorException(response.StatusCode);
		case 5:
			throw new HttpServerErrorException(response.StatusCode);
		}
	}

	private async Task<T> HttpCore<T>(Func<Task<T>> method) where T : class
	{
		new Uri(BaseUrl);
		using HttpClientHandler handler = new HttpClientHandler
		{
			UseCookies = true
		};
		if (httpClient != null)
		{
			httpClient.Dispose();
		}
		httpClient = new HttpClient(handler);
		httpClient.Timeout = new TimeSpan(0, 3, 0);
		httpClient.DefaultRequestHeaders.Add("X-Authentication-Token", Token);
		httpClient.DefaultRequestHeaders.Add("X-API-Version", "1.0");
		httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
		return await method();
	}
}
