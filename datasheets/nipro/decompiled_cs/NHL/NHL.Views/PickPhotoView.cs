using System;
using System.CodeDom.Compiler;
using System.Reflection;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/PickPhotoView.xaml")]
public class PickPhotoView : ContentPage
{
	public PickPhotoView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(PickPhotoView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/PickPhotoView.xaml",
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
		Grid grid = new Grid();
		PickPhotoView pickPhotoView;
		NameScope value = (NameScope)(NameScope.GetNameScope(pickPhotoView = this) ?? new NameScope());
		NameScope.SetNameScope(pickPhotoView, value);
		pickPhotoView.SetValue(NavigationPage.HasBackButtonProperty, true);
		pickPhotoView.SetValue(Page.IconProperty, new FileImageSourceConverter().ConvertFromInvariantString("icon_cameraroll.png"));
		pickPhotoView.SetValue(Page.TitleProperty, "");
		pickPhotoView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/PickPhotoView.xaml", UriKind.RelativeOrAbsolute), 8, 6);
		VisualDiagnostics.RegisterSourceInfo(pickPhotoView, new Uri("Views/PickPhotoView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(PickPhotoView));
	}
}
