using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Models;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Event;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class EditDailyCommentViewModel : ViewModelBase
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private DailyComments dailyCommentModel;

	private string comment;

	private bool commentFlag;

	public IDailyCommentService DailyCommentService { get; set; }

	public DailyComments DailyComments
	{
		get
		{
			return dailyCommentModel;
		}
		set
		{
			if (dailyCommentModel != value)
			{
				dailyCommentModel = value;
				NotifyOfPropertyChange(() => DailyComments);
			}
		}
	}

	public string Comment
	{
		get
		{
			return comment;
		}
		set
		{
			if (comment != value)
			{
				comment = value;
				NotifyOfPropertyChange(() => Comment);
			}
			CommentFlag = string.IsNullOrEmpty(comment);
		}
	}

	public bool CommentFlag
	{
		get
		{
			return commentFlag;
		}
		set
		{
			commentFlag = value;
			NotifyOfPropertyChange(() => CommentFlag);
		}
	}

	public async Task Save()
	{
		await ExecAsync(async delegate
		{
			DailyComments.Comment = Comment;
			if (!string.IsNullOrEmpty(DailyComments.Id))
			{
				await DailyCommentService.UpdateDailyComment(DailyComments);
			}
			else
			{
				await DailyCommentService.RegisterDailyComment(DailyComments);
			}
			await Task.Run(async delegate
			{
				Log.Info("【IG】【EditDailyCommentViewModel】【Save】Sync start");
				await DailyCommentService.Sync();
			}).ConfigureAwait(continueOnCapturedContext: false);
		});
		base.EventAggregator.PublishOnUIThread(new UpdateMeasurementEvent());
		base.EventAggregator.PublishOnUIThread(new HomeUpdateMeasurementEvent());
	}

	protected override void OnActivate()
	{
		Comment = DailyComments.Comment;
		base.OnActivate();
	}

	protected override async void OnDeactivate(bool close)
	{
		await Save();
		base.OnDeactivate(close);
	}
}
