using System;
using System.Net.Http;
using System.Text;
using NHL.Models.Support;
using NHL.Services.DependencyService;
using Newtonsoft.Json;
using Xamarin.Forms;

namespace NHL.ViewModels.Utils;

public sealed class JsonUtils
{
	private static ILoggingService Log = DependencyService.Get<ILoggingService>();

	public static T ConvertResponseToModel<T>(HttpResponseMessage response) where T : class
	{
		try
		{
			return JsonConvert.DeserializeObject<T>(response.Content.ReadAsStringAsync().Result);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【JsonUtils】【ConvertResponseToModel】パースに失敗しました:{ex}");
			return null;
		}
	}

	public static HttpContent ConvertDtoToHttpContent<T>(T dto) where T : ModelBase
	{
		try
		{
			return new StringContent(JsonConvert.SerializeObject(dto, Formatting.Indented), Encoding.UTF8, "application/json");
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【JsonUtils】【ConvertDtoToHttpContent】シリアライズに失敗しました:{ex}");
			return null;
		}
	}
}
