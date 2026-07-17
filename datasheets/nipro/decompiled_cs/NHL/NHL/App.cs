using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using C1.Xamarin.Forms.Core;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Common;
using NHL.Framework;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.Services.Support;
using NHL.ViewModels.Event;
using NHL.Views;
using Plugin.Media;
using Xamarin.Forms;

namespace NHL;

public class App : FormsApplication
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private readonly SimpleContainer container;

	public App(SimpleContainer container)
	{
		this.container = container;
		ContainerConfigure();
		LicenseManager.Key = "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";
		CrossMedia.Current.Initialize();
		Initialize();
		if (Task.Run(async () => await InitializeUser()).Result)
		{
			DisplayRootView<MasterDetailRootView>();
		}
		else
		{
			DisplayRootView<SetupView>();
		}
		IoC.Get<IDialogProvider>().Initialize(base.RootNavigationPage);
		DependencyService.Get<IApplicationSleepControlService>().SleepDisabled();
		Task.Run(async delegate
		{
			if (!(await VersionManager.CheckNewApplicationVersion()))
			{
				IoC.Get<IEventAggregator>().BeginPublishOnUIThread(new ShowVersionUpMessageEvent());
			}
		});
	}

	private async Task<bool> InitializeUser()
	{
		IUserManager userManager = IoC.Get<IUserManager>();
		userManager.Initialize(IoC.Get<IAuthenticatedUserService>(), IoC.Get<IAuthenticationService>(), IoC.Get<IHospitalInfoService>(), IoC.Get<IIgUserService>(), IoC.Get<ILinkHospitalService>(), IoC.Get<IUnlinkHospitalService>(), IoC.Get<IUpdateTimezoneService>(), IoC.Get<IUpdateIgUserService>(), IoC.Get<IUpdateIgPasswordService>(), IoC.Get<ILogfileTransferService>(), IoC.Get<IUpdateIgGlucoseTargetService>(), IoC.Get<ISharerRegisterService>(), IoC.Get<ISharerUpdateService>(), IoC.Get<IUpdateTimezoneOtherService>(), IoC.Get<IVersionService>());
		bool num = await userManager.LoadUserModel();
		bool flag = num;
		bool bRes = num;
		if (flag)
		{
			if (await userManager.Authenticate())
			{
				await userManager.SaveUserModel();
			}
			else
			{
				await userManager.Authenticate(userManager.AuthenticationToken);
				Log.Error("【IG】【App】【InitializeUser】既存ユーザー認証トークン取得エラー");
			}
		}
		return bRes;
	}

	protected override void PrepareViewFirst(NavigationPage navigationPage)
	{
		container.Instance((INavigationService)new NavigationPageAdapter(navigationPage));
	}

	protected void ContainerConfigure()
	{
		container.RegisterInstance(typeof(IEventAggregator), null, new EventAggregator());
		container.RegisterInstance(typeof(IRestClient), null, new RestClient());
		container.RegisterInstance(typeof(IDialogProvider), "DialogProvider", new DialogProvider());
		container.RegisterInstance(typeof(IUserManager), "UserManager", new UserManager());
		Assembly asm = Assembly.Load(new AssemblyName(GetType().Namespace));
		ContainerConfigureBySuffix(asm, "ViewModel", "Singleton");
		ContainerConfigureBySuffix(asm, "Context", "Singleton");
		ContainerConfigureForService(asm);
	}

	private void ContainerConfigureBySuffix(Assembly asm, string key, string registerMethod = "PerRequest")
	{
		foreach (Type item in asm.ExportedTypes.Where((Type x) => x.FullName.EndsWith(key)))
		{
			using IEnumerator<MethodInfo> enumerator2 = (from p in typeof(ContainerExtensions).GetRuntimeMethods()
				where p.Name == registerMethod && p.GetGenericArguments().Length == 1
				select p).GetEnumerator();
			if (enumerator2.MoveNext())
			{
				enumerator2.Current.MakeGenericMethod(item).Invoke(container, new object[2] { container, null });
			}
		}
	}

	private void ContainerConfigureForService(Assembly asm)
	{
		foreach (Type type in asm.ExportedTypes.Where((Type x) => x.FullName.EndsWith("Service") && typeof(ServiceProxyBase).GetTypeInfo().IsAssignableFrom(x.GetTypeInfo())))
		{
			Type type2 = asm.ExportedTypes.FirstOrDefault((Type x) => x.Name == $"I{type.Name}");
			using IEnumerator<MethodInfo> enumerator2 = (from p in typeof(ContainerExtensions).GetRuntimeMethods()
				where p.Name == "PerRequest" && p.GetGenericArguments().Length == 2
				select p).GetEnumerator();
			if (enumerator2.MoveNext())
			{
				enumerator2.Current.MakeGenericMethod(type2, type).Invoke(container, new object[2] { container, null });
			}
		}
	}

	protected override void OnSleep()
	{
		Log.Info("【IG】【App】【OnSleep】OnSleep");
		DependencyService.Get<IRequestBluetooth>();
		base.OnSleep();
	}

	protected override void OnResume()
	{
		Log.Info("【IG】【App】【OnResume】OnResume");
		DependencyService.Get<IRequestBluetooth>();
		base.OnResume();
	}

	public void EnterBackground()
	{
		Log.Info("【IG】【App】【EnterBackground】EnterBackground");
		IoC.Get<IEventAggregator>().PublishOnBackgroundThread(new LifecycleEvent
		{
			State = LifecycleEvent.Status.Sleep
		});
	}

	public void EnterForeground()
	{
		Log.Info("【IG】【App】【EnterForeground】EnterForeground");
		IoC.Get<IEventAggregator>().PublishOnBackgroundThread(new LifecycleEvent
		{
			State = LifecycleEvent.Status.Resume
		});
		base.OnResume();
	}
}
