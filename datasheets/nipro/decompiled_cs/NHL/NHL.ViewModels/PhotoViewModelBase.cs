using System;
using System.Globalization;
using System.IO;
using System.Threading.Tasks;
using Caliburn.Micro;
using ExifLib;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Plugin.Media.Abstractions;
using Xamarin.Forms;

namespace NHL.ViewModels;

public abstract class PhotoViewModelBase : ViewModelBase, IHandle<PhotographingEvent>, IHandle
{
	public const int PHOTO_WIDTH = 640;

	public const int PHOTO_HEIGHT = 640;

	public const int PHOTO_QUALITY = 100;

	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	public IPhotographService PhotographService { get; set; }

	public new IDialogProvider DialogProvider { get; set; }

	public abstract bool IsTakePhoto { get; }

	public void Initialize()
	{
		base.EventAggregator.Subscribe(this);
	}

	public void Start()
	{
		Execute.BeginOnUIThread(async delegate
		{
			await Task.Delay(10);
			base.EventAggregator.PublishOnUIThread(new PhotographingEvent
			{
				IsTakePhoto = IsTakePhoto
			});
		});
	}

	protected abstract bool CanGetPhoto(PhotographingEvent message);

	protected abstract Task<MediaFile> DoGetPhoto(PhotographingEvent message);

	protected abstract Task DoGetPhotoOnError(Exception ex);

	protected virtual async Task<Photograph> AppendPhoto(MediaFile photo)
	{
		if (photo == null)
		{
			return null;
		}
		DateTime createTime = GetCreateTime(photo);
		string image = Convert.ToBase64String(DependencyService.Get<IImageService>().ResizeImage(photo.Path, 640, 640, 100));
		Photograph item = new Photograph
		{
			IGUserId = base.UserManager.IgUser.Id,
			Image = image,
			ShootingAt = createTime,
			TimezoneDate = CalcTimezoneDate(createTime)
		};
		await PhotographService.RegisterPhotograph(item);
		return item;
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

	private DateTime GetCreateTime(MediaFile photo)
	{
		using (Stream stream = photo.GetStream())
		{
			JpegInfo jpegInfo = null;
			try
			{
				jpegInfo = ExifReader.ReadJpeg(stream);
				return DateTime.ParseExact(jpegInfo.DateTimeOriginal, "yyyy:MM:dd HH:mm:ss", DateTimeFormatInfo.InvariantInfo, DateTimeStyles.None);
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【PhotoViewModelBase】【GetCreateTime】例外発生：{ex}");
				if (DateTime.TryParse(jpegInfo?.DateTime, out var result))
				{
					return result;
				}
			}
		}
		IImageService imageService = DependencyService.Get<IImageService>();
		DateTime? dateTime = null;
		try
		{
			dateTime = imageService.GetCreateTime(photo);
			Log.Info($"【IG】【ImageService】【service.GetCreateTime()】:{dateTime}");
		}
		catch (Exception ex2)
		{
			Log.Error($"【IG】【ImageService】【GetCreateTime】例外発生：{ex2}");
		}
		if (!dateTime.HasValue)
		{
			dateTime = DateTime.Now;
		}
		return dateTime.Value;
	}

	void IHandle<PhotographingEvent>.Handle(PhotographingEvent message)
	{
		if (!CanGetPhoto(message))
		{
			return;
		}
		Execute.BeginOnUIThread(async delegate
		{
			try
			{
				MediaFile mediaFile = await DoGetPhoto(message);
				if (mediaFile == null)
				{
					base.EventAggregator.PublishOnUIThread(new TabChangeEvent
					{
						RequestDate = null
					});
					return;
				}
				Photograph model = await AppendPhoto(mediaFile);
				await Task.Run(async delegate
				{
					Log.Info("【IG】【PhotoViewModelBase】【IHandle<PhotographingEvent>】Sync start");
					await PhotographService.Sync();
				}).ConfigureAwait(continueOnCapturedContext: false);
				base.EventAggregator.PublishOnUIThread(new TabChangeEvent
				{
					RequestDate = model?.TimezoneDate
				});
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【PhotoViewModelBase】【IHandle<PhotographingEvent>.Handle】例外発生：{ex}");
				await DoGetPhotoOnError(ex);
			}
		});
	}
}
