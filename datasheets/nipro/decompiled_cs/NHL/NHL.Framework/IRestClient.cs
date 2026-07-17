using System.Net.Http;
using System.Threading.Tasks;
using NHL.Models.Support;

namespace NHL.Framework;

public interface IRestClient
{
	string Token { get; set; }

	string BaseUrl { get; }

	Task<string> Get(string path);

	Task<HttpResponseMessage> GetForMessage(string path);

	Task<TResult> GetForObject<TResult>(string path) where TResult : class;

	Task<HttpResponseMessage> PostForMessage<T>(string path, T payload) where T : ModelBase;

	Task<TResult> PostForObject<T, TResult>(string path, T payload) where T : ModelBase where TResult : ModelBase;

	Task<HttpResponseMessage> PutForMessage<T>(string path, T payload) where T : ModelBase;

	Task<TResult> PutForObject<T, TResult>(string path, T payload) where T : ModelBase where TResult : ModelBase;
}
