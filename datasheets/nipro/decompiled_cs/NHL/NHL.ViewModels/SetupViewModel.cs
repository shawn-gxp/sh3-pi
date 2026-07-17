using NHL.Models;
using NHL.Services.Support;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Support;

namespace NHL.ViewModels;

public class SetupViewModel : ViewModelBase
{
	public MeterContext MeterContext { get; set; }

	public async void StartNew()
	{
		await ExecAsync(async delegate
		{
			await SyncManager.GetInstance().PurgeLocalDB();
			base.UserManager.IgUser = new IgUserModel();
			await Navigate<TermsOfServiceViewModel>();
		});
	}

	public async void Restore()
	{
		await SyncManager.GetInstance().PurgeLocalDB();
		await Navigate<RestoreViewModel>();
	}

	protected override void OnInitialize()
	{
		base.OnInitialize();
	}

	protected override void OnActivate()
	{
		base.UserManager.ResetUser();
		base.OnActivate();
	}
}
