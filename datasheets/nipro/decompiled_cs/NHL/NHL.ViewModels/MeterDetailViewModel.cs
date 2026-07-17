using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Common;
using NHL.Models;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class MeterDetailViewModel : ViewModelBase
{
	public MeterModel Meter { get; set; }

	public MeterContext MeterContext { get; set; }

	public MeterDetailViewModel()
	{
		MeterContext = IoC.Get<MeterContext>();
	}

	public async Task Unregister()
	{
		if (MeterContext != null)
		{
			string userName = "USERNAME";
			MeterCategory deviceType = MeterCategory.NONE;
			if (Meter.DeviceType == 5)
			{
				deviceType = MeterCategory.HT;
			}
			else if (Meter.DeviceType == 3)
			{
				deviceType = MeterCategory.BP;
			}
			else if (Meter.DeviceType == 4)
			{
				deviceType = MeterCategory.GL;
			}
			else if (Meter.DeviceType == 2)
			{
				deviceType = MeterCategory.BF;
			}
			else if (Meter.DeviceType == 6)
			{
				deviceType = MeterCategory.PL;
			}
			else if (Meter.DeviceType == 1)
			{
				deviceType = MeterCategory.BC;
			}
			bool num = await MeterContext.Unregister(deviceType, userName);
			if (MeterContext.IsReady)
			{
				base.EventAggregator.PublishOnBackgroundThread(new ScanControlEvent
				{
					Command = ScanControlEvent.CommandType.Restart
				});
			}
			else
			{
				base.EventAggregator.PublishOnBackgroundThread(new ScanControlEvent
				{
					Command = ScanControlEvent.CommandType.StopScan
				});
			}
			if (num)
			{
				await ShowAlert("", "測定器登録を解除しました。");
			}
			else
			{
				await ShowAlert("", "測定器登録の解除に失敗しました。");
			}
		}
		await base.NavigationService.GoBackAsync();
	}
}
