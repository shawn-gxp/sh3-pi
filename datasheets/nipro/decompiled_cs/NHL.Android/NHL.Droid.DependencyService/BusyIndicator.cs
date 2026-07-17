using System.ComponentModel;
using Android.App;
using Caliburn.Micro;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class BusyIndicator : PropertyChangedBase, IBusyIndicator
{
	private ProgressDialog progress;

	private bool isBusy;

	private string message;

	public bool IsBusy
	{
		get
		{
			return isBusy;
		}
		set
		{
			if (isBusy != value)
			{
				isBusy = value;
				NotifyOfPropertyChange(() => IsBusy);
			}
		}
	}

	public string Message
	{
		get
		{
			return message;
		}
		set
		{
			if (message != value)
			{
				message = value;
				NotifyOfPropertyChange(() => Message);
			}
			if (IsBusy && progress != null && progress.IsShowing)
			{
				Execute.OnUIThread(delegate
				{
					progress.SetMessage(Message);
				});
			}
		}
	}

	public BusyIndicator()
	{
		Initialize();
		PropertyChanged += OnBusyIndicatorPropertyChanged;
	}

	public virtual void Initialize()
	{
		IsBusy = false;
		Message = "しばらくお待ちください...";
	}

	public void Show()
	{
		Execute.OnUIThread(delegate
		{
			isBusy = true;
			progress = new ProgressDialog(Forms.Context);
			progress.Indeterminate = true;
			progress.SetProgressStyle(ProgressDialogStyle.Spinner);
			progress.SetCancelable(flag: false);
			progress.SetMessage(message);
			progress.Show();
		});
	}

	public void Hide()
	{
		if (progress != null)
		{
			Execute.OnUIThread(delegate
			{
				isBusy = false;
				progress.Dismiss();
			});
		}
	}

	protected void OnBusyIndicatorPropertyChanged(object sender, PropertyChangedEventArgs e)
	{
		if (!(e.PropertyName != "IsBusy"))
		{
			if (IsBusy)
			{
				Show();
			}
			else
			{
				Hide();
			}
		}
	}
}
