using System;
using System.Threading.Tasks;
using NHL.Properties;
using NHL.ViewModels.Event;
using Plugin.Media;
using Plugin.Media.Abstractions;

namespace NHL.ViewModels;

public class PickPhotoViewModel : PhotoViewModelBase
{
	public override bool IsTakePhoto => false;

	protected override bool CanGetPhoto(PhotographingEvent message)
	{
		if (!message.IsTakePhoto)
		{
			return CrossMedia.Current.IsPickPhotoSupported;
		}
		return false;
	}

	protected override async Task<MediaFile> DoGetPhoto(PhotographingEvent message)
	{
		return await CrossMedia.Current.PickPhotoAsync(new PickMediaOptions
		{
			SaveMetaData = true
		});
	}

	protected override async Task DoGetPhotoOnError(Exception ex)
	{
		await base.DialogProvider.ShowAlert(string.Empty, Resources.MESSAGE_004);
	}
}
