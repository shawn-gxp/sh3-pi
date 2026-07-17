using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Plugin.ImageEdit;
using Plugin.ImageEdit.Abstractions;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class ImageTimezoneChangeViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private Photograph photograph;

	private DateTime selectedPhotographRegistDateOfDate;

	private TimeSpan selectedPhotographRegistDateOfTime;

	public IMeasurementService MeasurementService { get; set; }

	public IPhotographService PhotographService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public Photograph Photograph
	{
		get
		{
			return photograph;
		}
		set
		{
			photograph = value;
			NotifyOfPropertyChange(() => Photograph);
		}
	}

	public DateTime SelectedPhotographRegistDateOfDate
	{
		get
		{
			return selectedPhotographRegistDateOfDate;
		}
		set
		{
			if (selectedPhotographRegistDateOfDate != value)
			{
				selectedPhotographRegistDateOfDate = value;
				NotifyOfPropertyChange(() => SelectedPhotographRegistDateOfDate);
			}
		}
	}

	public TimeSpan SelectedPhotographRegistDateOfTime
	{
		get
		{
			return selectedPhotographRegistDateOfTime;
		}
		set
		{
			if (selectedPhotographRegistDateOfTime != value)
			{
				selectedPhotographRegistDateOfTime = value;
				NotifyOfPropertyChange(() => SelectedPhotographRegistDateOfTime);
			}
		}
	}

	public async void Delete()
	{
		if (await DialogProvider.ShowAlert("", "削除しますか？", "はい", "キャンセル", null))
		{
			Photograph.Image = null;
			Photograph.Deleted = true;
			base.NavigationService.GoBackAsync();
		}
	}

	public async void Rotate()
	{
		await ExecAsync(async delegate
		{
			byte[] imageArray = Convert.FromBase64String(Photograph.Image.Trim());
			IEditableImage image = await CrossImageEdit.Current.CreateImageAsync(imageArray);
			try
			{
				IEditableImage editableImage = await Task.Run(() => image.Rotate(90f));
				Photograph.Image = Convert.ToBase64String(editableImage.ToJpeg());
			}
			finally
			{
				if (image != null)
				{
					image.Dispose();
				}
			}
		});
	}

	public async Task Save()
	{
		await ExecAsync(async delegate
		{
			Photograph.ShootingAt = new DateTime(SelectedPhotographRegistDateOfDate.Year, SelectedPhotographRegistDateOfDate.Month, SelectedPhotographRegistDateOfDate.Day, SelectedPhotographRegistDateOfTime.Hours, SelectedPhotographRegistDateOfTime.Minutes, SelectedPhotographRegistDateOfTime.Seconds);
			Photograph.TimezoneDate = CalcTimezoneDate(Photograph.ShootingAt.Value);
			await PhotographService.UpdatePhotograph(Photograph);
			await Task.Run(async delegate
			{
				Log.Info("【IG】【ImageTimezoneChangeViewModel】【Save】Sync start");
				await PhotographService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
		});
		base.EventAggregator.PublishOnUIThread(new RegistDateSaveEvent());
	}

	private DateTime CalcTimezoneDate(DateTime shootingAt)
	{
		DateTime dateTime = new DateTime(shootingAt.Year, shootingAt.Month, shootingAt.Day);
		TimeSpan timeSpan = shootingAt - dateTime;
		TimeSpan timeSpan2 = base.UserManager.IgUser.Timezone1.Value.Add(new TimeSpan(24, 0, 0));
		if (timeSpan >= base.UserManager.IgUser.Timezone1.Value && timeSpan < timeSpan2)
		{
			return dateTime;
		}
		return dateTime.AddDays(-1.0);
	}

	protected override async void OnDeactivate(bool close)
	{
		await Save();
		base.OnDeactivate(close);
	}
}
