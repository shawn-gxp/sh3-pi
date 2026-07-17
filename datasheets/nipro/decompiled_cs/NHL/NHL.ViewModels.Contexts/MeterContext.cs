using System;
using System.Threading.Tasks;
using NHL.Common;
using NHL.Models;
using NHL.Services.DependencyService;
using NHL.ViewModels.Utils;
using Xamarin.Forms;

namespace NHL.ViewModels.Contexts;

public class MeterContext
{
	private static ILoggingService Log = DependencyService.Get<ILoggingService>();

	public const string USER_NAME = "USERNAME";

	public static readonly string NON_REGIST_NAME = "未登録";

	public MeterModel BCMeter { get; set; }

	public MeterModel BFMeter { get; set; }

	public MeterModel BPMeter { get; set; }

	public MeterModel HTMeter { get; set; }

	public MeterModel PLMeter { get; set; }

	public MeterModel GLMeter { get; set; }

	public bool ReturnedFromColorSelection { get; set; }

	public bool IsReady
	{
		get
		{
			if (BCMeter != null && BCMeter.Name != NON_REGIST_NAME)
			{
				return true;
			}
			if (BFMeter != null && BFMeter.Name != NON_REGIST_NAME)
			{
				return true;
			}
			if (BPMeter != null && BPMeter.Name != NON_REGIST_NAME)
			{
				return true;
			}
			if (HTMeter != null && HTMeter.Name != NON_REGIST_NAME)
			{
				return true;
			}
			if (PLMeter != null && PLMeter.Name != NON_REGIST_NAME)
			{
				return true;
			}
			if (GLMeter != null && GLMeter.Name != NON_REGIST_NAME)
			{
				return true;
			}
			return false;
		}
	}

	public void Initialize(string userName)
	{
		ReturnedFromColorSelection = false;
		Task.Run(async delegate
		{
			MeterModel bCMeter = await GetMeterContextFromSetting(userName, MeterCategory.BC);
			BCMeter = bCMeter;
			bCMeter = await GetMeterContextFromSetting(userName, MeterCategory.BF);
			BFMeter = bCMeter;
			bCMeter = await GetMeterContextFromSetting(userName, MeterCategory.BP);
			BPMeter = bCMeter;
			bCMeter = await GetMeterContextFromSetting(userName, MeterCategory.GL);
			GLMeter = bCMeter;
			bCMeter = await GetMeterContextFromSetting(userName, MeterCategory.HT);
			HTMeter = bCMeter;
			bCMeter = await GetMeterContextFromSetting(userName, MeterCategory.PL);
			PLMeter = bCMeter;
		}).Wait(1000);
	}

	public async Task<bool> Unregister(MeterCategory deviceType, string userName)
	{
		Log.Debug(string.Format("【IG】【MeterContext】【Unregister】論理ペアリング解除 deviceType:{0} userName:{1}", new object[2] { deviceType, userName }));
		if (!(await new MeterSettingFiles().DeleteMeterSettingAsync(deviceType, userName)))
		{
			Log.Debug("【IG】【MeterContext】【Unregister】論理ペアリング解除失敗");
			return false;
		}
		Log.Debug("【IG】【MeterContext】【Unregister】論理ペアリング解除成功 ");
		switch (deviceType)
		{
		case MeterCategory.BC:
			BCMeter = new MeterModel
			{
				Name = NON_REGIST_NAME,
				DeviceType = 1
			};
			break;
		case MeterCategory.GL:
			GLMeter = new MeterModel
			{
				Name = NON_REGIST_NAME,
				DeviceType = 4
			};
			break;
		case MeterCategory.HT:
			HTMeter = new MeterModel
			{
				Name = NON_REGIST_NAME,
				DeviceType = 5
			};
			break;
		case MeterCategory.BP:
			BPMeter = new MeterModel
			{
				Name = NON_REGIST_NAME,
				DeviceType = 3
			};
			break;
		case MeterCategory.BF:
			BFMeter = new MeterModel
			{
				Name = NON_REGIST_NAME,
				DeviceType = 2
			};
			break;
		case MeterCategory.PL:
			PLMeter = new MeterModel
			{
				Name = NON_REGIST_NAME,
				DeviceType = 6
			};
			break;
		}
		return true;
	}

	private static async Task<MeterModel> GetMeterContextFromSetting(string userName, MeterCategory deviceType)
	{
		MeterModel contextMeter = new MeterModel
		{
			Name = NON_REGIST_NAME,
			DeviceType = (int)deviceType
		};
		try
		{
			MeterSetting meterSetting = await new MeterSettingFiles().ReadMeterSettingAsync(deviceType, userName);
			if (meterSetting != null && !string.IsNullOrEmpty(meterSetting.Name))
			{
				contextMeter = new MeterModel
				{
					Name = meterSetting.Name,
					SerialNumber = meterSetting.SerialNumber,
					DeviceType = (int)deviceType,
					UserNo = meterSetting.UserNo,
					ColorCode = meterSetting.ColorCode
				};
				if (!string.IsNullOrEmpty(meterSetting.Id))
				{
					contextMeter.Id = Guid.Parse(meterSetting.Id);
				}
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【MeterContext】【GetMeterContextFromSetting】例外発生：{ex}");
		}
		return contextMeter;
	}

	public async Task Pair(MeterModel meter, string userName, MeterCategory deviceType)
	{
		await new MeterSettingFiles().WriteMeterSettingAsync(meter.Id, meter.Name, meter.SerialNumber, deviceType, userName, meter.UserNo, meter.ColorCode);
		switch (deviceType)
		{
		case MeterCategory.BC:
			BCMeter = meter;
			break;
		case MeterCategory.BP:
			BPMeter = meter;
			break;
		case MeterCategory.HT:
			HTMeter = meter;
			break;
		case MeterCategory.GL:
			GLMeter = meter;
			break;
		case MeterCategory.BF:
			BFMeter = meter;
			break;
		case MeterCategory.PL:
			PLMeter = meter;
			break;
		}
	}

	public async Task<bool> CheckPairing(string userName, string id)
	{
		MeterSettingFiles meterFile = new MeterSettingFiles();
		foreach (MeterCategory value in Enum.GetValues(typeof(MeterCategory)))
		{
			MeterSetting meterSetting = await meterFile.ReadMeterSettingAsync(value, userName);
			if (meterSetting != null && meterSetting.Id.Equals(id.Replace("-", string.Empty)))
			{
				return true;
			}
		}
		return false;
	}
}
