using System.Threading.Tasks;
using Android.Content;
using Android.Media;
using Android.Net;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class MediaPlayerService : IMediaPlayerService
{
	private MediaPlayer player;

	public async Task PlayAsync(string title)
	{
		await StartPlayerAsync(title);
	}

	public void Stop()
	{
		StopPlayer();
	}

	private async Task StartPlayerAsync(string title)
	{
		int resourceId = (int)typeof(Resource.Raw).GetField(title).GetValue(null);
		await Task.Run(delegate
		{
			if (player == null)
			{
				player = new MediaPlayer();
				player.SetDataSource(Forms.Context, CreateResourceIdToUri(Forms.Context, resourceId));
				player.SetAudioStreamType(Stream.Ring);
				player.Looping = false;
				player.Prepare();
				player.Start();
			}
			else if (player.IsPlaying)
			{
				player.Pause();
			}
			else
			{
				player.Start();
			}
		});
	}

	private Uri CreateResourceIdToUri(Context context, int resId)
	{
		return Uri.Parse($"android.resource://{context.PackageName}/{resId}");
	}

	private void StopPlayer()
	{
		if (player != null)
		{
			if (player.IsPlaying)
			{
				player.Stop();
			}
			player.Release();
			player = null;
		}
	}
}
