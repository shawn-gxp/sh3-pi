using System;
using System.IO;
using System.Linq;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Threading.Tasks;
using NHL.Common;
using NHL.Services.DependencyService;
using PCLStorage;
using Xamarin.Forms;

namespace NHL.ViewModels.Utils;

public class MeterSettingFiles
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private string FolderName = "HNLSettingFolder";

	private string FileNameBase = "MeterSetting.txt";

	private string GetFileName(MeterCategory deviceType, string nurseId)
	{
		string empty = string.Empty;
		if (string.IsNullOrEmpty(nurseId))
		{
			nurseId = "UnknownUser";
		}
		switch (deviceType)
		{
		case MeterCategory.GL:
		{
			char[] chars = Path.GetInvalidFileNameChars();
			return string.Concat(string.Concat(nurseId.Select((char c) => (!chars.Contains(c)) ? c : '_')), "_GL", FileNameBase);
		}
		case MeterCategory.HT:
			return "HT" + FileNameBase;
		case MeterCategory.BP:
			return "BP" + FileNameBase;
		case MeterCategory.BF:
			return "BF" + FileNameBase;
		case MeterCategory.PL:
			return "PL" + FileNameBase;
		case MeterCategory.BC:
			return "BC" + FileNameBase;
		default:
			return FileNameBase;
		}
	}

	private async Task<IFolder> GetFolderAsync()
	{
		IFolder folder = FileSystem.Current.LocalStorage;
		return (await folder.CheckExistsAsync(FolderName) != ExistenceCheckResult.FolderExists) ? (await folder.CreateFolderAsync(FolderName, CreationCollisionOption.OpenIfExists)) : (await folder.GetFolderAsync(FolderName));
	}

	private async Task<string> ReadFileAsync(string fileName)
	{
		string str = string.Empty;
		IFolder subFolder = await GetFolderAsync();
		if (await subFolder.CheckExistsAsync(fileName) == ExistenceCheckResult.FileExists)
		{
			str = await (await subFolder.GetFileAsync(fileName)).ReadAllTextAsync();
		}
		return str;
	}

	private async Task WriteFileAsync(string data, string fileName)
	{
		await (await (await GetFolderAsync()).CreateFileAsync(fileName, CreationCollisionOption.ReplaceExisting)).WriteAllTextAsync(data);
	}

	private async Task DeleteFileAsync(string fileName)
	{
		IFolder subFolder = await GetFolderAsync();
		if (await subFolder.CheckExistsAsync(fileName) == ExistenceCheckResult.FileExists)
		{
			await (await subFolder.GetFileAsync(fileName)).DeleteAsync();
		}
	}

	public async Task WriteMeterSettingAsync(Guid id, string name, string serialNumber, MeterCategory deviceType, string nurseId = "UnknownUser", string userNo = "", string colorCode = "0")
	{
		MeterSetting meterSetting = new MeterSetting
		{
			Name = name,
			SerialNumber = serialNumber,
			UserNo = userNo,
			ColorCode = colorCode
		};
		meterSetting.Id = id.ToString("N");
		string text = string.Empty;
		using (MemoryStream memoryStream = new MemoryStream())
		{
			using StreamReader streamReader = new StreamReader(memoryStream);
			new DataContractJsonSerializer(typeof(MeterSetting)).WriteObject(memoryStream, meterSetting);
			memoryStream.Position = 0L;
			text = streamReader.ReadToEnd();
		}
		string fileName = GetFileName(deviceType, nurseId);
		Log.Info("【IG】【MeterSettingFiles】【WriteMeterSettingAsync】メーター情報の書き込み:filename=" + fileName + ", json=" + text);
		await WriteFileAsync(text, fileName);
	}

	public async Task<MeterSetting> ReadMeterSettingAsync(MeterCategory deviceType, string nurseId = "UnknownUser")
	{
		string filename = GetFileName(deviceType, nurseId);
		string text = await ReadFileAsync(filename);
		Log.Info("【IG】【MeterSettingFiles】【ReadMeterSettingAsync】メーター情報の読み込み:filename=" + filename + ", json=" + text);
		if (string.IsNullOrEmpty(text))
		{
			return null;
		}
		DataContractJsonSerializer dataContractJsonSerializer = new DataContractJsonSerializer(typeof(MeterSetting));
		byte[] bytes = Encoding.Unicode.GetBytes(text);
		MeterSetting result = null;
		using (MemoryStream stream = new MemoryStream(bytes))
		{
			result = (MeterSetting)dataContractJsonSerializer.ReadObject(stream);
		}
		return result;
	}

	public async Task<bool> DeleteMeterSettingAsync(MeterCategory deviceType, string nurseId = "UnknownUser")
	{
		try
		{
			string fileName = GetFileName(deviceType, nurseId);
			await DeleteFileAsync(fileName);
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MeterSettingFiles】【DeleteMeterSettingAsync】例外発生：{ex}");
			return false;
		}
		return true;
	}
}
