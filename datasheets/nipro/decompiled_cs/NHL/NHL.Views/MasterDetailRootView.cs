using System;
using System.CodeDom.Compiler;
using System.Reflection;
using NHL.Views.Renderer;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/MasterDetailRootView.xaml")]
public class MasterDetailRootView : MasterDetailPage
{
	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private SettingView MasterPage;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private BottomBarPage tab;

	public MasterDetailRootView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(MasterDetailRootView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/MasterDetailRootView.xaml",
			Instance = this
		}))
		{
			__InitComponentRuntime();
			return;
		}
		if (XamlLoader.XamlFileProvider != null && XamlLoader.XamlFileProvider(GetType()) != null)
		{
			__InitComponentRuntime();
			return;
		}
		BindingExtension bindingExtension = new BindingExtension();
		BindingExtension bindingExtension2 = new BindingExtension();
		SettingView settingView = new SettingView();
		BindingExtension bindingExtension3 = new BindingExtension();
		BindingExtension bindingExtension4 = new BindingExtension();
		BindingExtension bindingExtension5 = new BindingExtension();
		HomeView homeView = new HomeView();
		BindingExtension bindingExtension6 = new BindingExtension();
		MeasurementResultView measurementResultView = new MeasurementResultView();
		BindingExtension bindingExtension7 = new BindingExtension();
		PickPhotoView pickPhotoView = new PickPhotoView();
		BindingExtension bindingExtension8 = new BindingExtension();
		RecordView recordView = new RecordView();
		BottomBarPage bottomBarPage;
		NavigationPage navigationPage = new NavigationPage(bottomBarPage = new BottomBarPage());
		MasterDetailRootView masterDetailRootView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(masterDetailRootView = this) ?? new NameScope());
		NameScope.SetNameScope(masterDetailRootView, nameScope);
		((INameScope)nameScope).RegisterName("MasterPage", (object)settingView);
		if (settingView.StyleId == null)
		{
			settingView.StyleId = "MasterPage";
		}
		((INameScope)nameScope).RegisterName("tab", (object)bottomBarPage);
		if (bottomBarPage.StyleId == null)
		{
			bottomBarPage.StyleId = "tab";
		}
		MasterPage = settingView;
		tab = bottomBarPage;
		masterDetailRootView.SetValue(NavigationPage.HasBackButtonProperty, false);
		masterDetailRootView.SetValue(NavigationPage.HasNavigationBarProperty, false);
		masterDetailRootView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		bindingExtension.Mode = BindingMode.TwoWay;
		bindingExtension.Path = "IsPresentedMenu";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		masterDetailRootView.SetBinding(MasterDetailPage.IsPresentedProperty, binding);
		bindingExtension2.Path = "SettingViewModel";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		settingView.SetBinding(BindableObject.BindingContextProperty, binding2);
		masterDetailRootView.Master = settingView;
		VisualDiagnostics.RegisterSourceInfo(settingView, new Uri("Views/MasterDetailRootView.xaml", UriKind.RelativeOrAbsolute), 14, 10);
		navigationPage.SetValue(NavigationPage.HasBackButtonProperty, false);
		navigationPage.SetValue(NavigationPage.HasNavigationBarProperty, false);
		bindingExtension3.Path = "DisplayName";
		BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		bottomBarPage.SetBinding(Page.TitleProperty, binding3);
		bottomBarPage.FixedMode = true;
		bindingExtension4.Path = "IsTabVisible";
		BindingBase binding4 = ((IMarkupExtension<BindingBase>)bindingExtension4).ProvideValue((IServiceProvider)null);
		bottomBarPage.SetBinding(VisualElement.IsVisibleProperty, binding4);
		bindingExtension5.Path = "HomeViewModel";
		BindingBase binding5 = ((IMarkupExtension<BindingBase>)bindingExtension5).ProvideValue((IServiceProvider)null);
		homeView.SetBinding(BindableObject.BindingContextProperty, binding5);
		bottomBarPage.Children.Add(homeView);
		bindingExtension6.Path = "MeasurementResultViewModel";
		BindingBase binding6 = ((IMarkupExtension<BindingBase>)bindingExtension6).ProvideValue((IServiceProvider)null);
		measurementResultView.SetBinding(BindableObject.BindingContextProperty, binding6);
		bottomBarPage.Children.Add(measurementResultView);
		bindingExtension7.Path = "PickPhotoViewModel";
		BindingBase binding7 = ((IMarkupExtension<BindingBase>)bindingExtension7).ProvideValue((IServiceProvider)null);
		pickPhotoView.SetBinding(BindableObject.BindingContextProperty, binding7);
		bottomBarPage.Children.Add(pickPhotoView);
		bindingExtension8.Path = "RecordViewModel";
		BindingBase binding8 = ((IMarkupExtension<BindingBase>)bindingExtension8).ProvideValue((IServiceProvider)null);
		recordView.SetBinding(BindableObject.BindingContextProperty, binding8);
		bottomBarPage.Children.Add(recordView);
		masterDetailRootView.Detail = navigationPage;
		VisualDiagnostics.RegisterSourceInfo(navigationPage, new Uri("Views/MasterDetailRootView.xaml", UriKind.RelativeOrAbsolute), 17, 10);
		VisualDiagnostics.RegisterSourceInfo(masterDetailRootView, new Uri("Views/MasterDetailRootView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(MasterDetailRootView));
		MasterPage = this.FindByName<SettingView>("MasterPage");
		tab = this.FindByName<BottomBarPage>("tab");
	}
}
