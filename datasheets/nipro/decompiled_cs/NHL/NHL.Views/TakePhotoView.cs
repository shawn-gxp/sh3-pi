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
[XamlFilePath("Views/TakePhotoView.xaml")]
public class TakePhotoView : ContentPage
{
	public TakePhotoView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(TakePhotoView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/TakePhotoView.xaml",
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
		TakePhotoView takePhotoView;
		NameScope value = (NameScope)(NameScope.GetNameScope(takePhotoView = this) ?? new NameScope());
		NameScope.SetNameScope(takePhotoView, value);
		takePhotoView.SetValue(NavigationPage.HasBackButtonProperty, true);
		takePhotoView.SetValue(Page.IconProperty, new FileImageSourceConverter().ConvertFromInvariantString("icon_photo.png"));
		takePhotoView.SetValue(Page.TitleProperty, "");
		takePhotoView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/TakePhotoView.xaml", UriKind.RelativeOrAbsolute), 8, 6);
		VisualDiagnostics.RegisterSourceInfo(takePhotoView, new Uri("Views/TakePhotoView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(TakePhotoView));
	}
}
