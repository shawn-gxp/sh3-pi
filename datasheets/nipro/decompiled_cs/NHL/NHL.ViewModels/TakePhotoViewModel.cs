using System;
using System.Threading.Tasks;
using NHL.Properties;
using NHL.ViewModels.Event;
using Plugin.Media;
using Plugin.Media.Abstractions;

namespace NHL.ViewModels;

public class TakePhotoViewModel : PhotoViewModelBase
{
	public override bool IsTakePhoto => true;

	protected override bool CanGetPhoto(PhotographingEvent message)
	{
		if (message.IsTakePhoto)
		{
			return CrossMedia.Current.IsTakePhotoSupported;
		}
		return false;
	}

	protected override async Task<MediaFile> DoGetPhoto(PhotographingEvent message)
	{
		return await CrossMedia.Current.TakePhotoAsync(new StoreCameraMediaOptions
		{
			DefaultCamera = CameraDevice.Rear,
			AllowCropping = false,
			SaveToAlbum = true
		});
	}

	protected override async Task DoGetPhotoOnError(Exception ex)
	{
		await base.DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_005);
	}
}
