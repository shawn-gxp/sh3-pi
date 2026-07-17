using System;
using System.CodeDom.Compiler;
using System.Reflection;
using Caliburn.Micro.Xamarin.Forms;
using NHL.ViewModels;
using NHL.Views.Converters;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/TermsOfServiceView.xaml")]
public class TermsOfServiceView : ContentPage
{
	public TermsOfServiceView()
	{
		InitializeComponent();
	}

	private void WebView_Navigated(object sender, WebNavigatedEventArgs e)
	{
		TermsOfServiceViewModel termsOfServiceViewModel = base.BindingContext as TermsOfServiceViewModel;
		if (termsOfServiceViewModel != null)
		{
			termsOfServiceViewModel.IsNavigating = false;
		}
		if (e.Result == WebNavigationResult.Success)
		{
			termsOfServiceViewModel.IsConfirmButtonEnable = true;
			return;
		}
		termsOfServiceViewModel.IsConfirmButtonEnable = false;
		DisplayAlert("", "インターネット接続を確認して、再度試してください", "OK");
	}

	private void WebView_MeasureInvalidated(object sender, EventArgs e)
	{
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(TermsOfServiceView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/TermsOfServiceView.xaml",
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
		OnPlatform<Thickness> onPlatform = new OnPlatform<Thickness>();
		BooleanSwitchConverter value = new BooleanSwitchConverter();
		Color color = Color.FromHex("#5B9BD5");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		RowDefinition item = new RowDefinition();
		RowDefinition rowDefinition = new RowDefinition();
		BindingExtension bindingExtension = new BindingExtension();
		WebView webView = new WebView();
		BindingExtension bindingExtension2 = new BindingExtension();
		ActivityIndicator activityIndicator = new ActivityIndicator();
		StaticResourceExtension staticResourceExtension = new StaticResourceExtension();
		BindingExtension bindingExtension3 = new BindingExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid = new Grid();
		TermsOfServiceView termsOfServiceView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(termsOfServiceView = this) ?? new NameScope());
		NameScope.SetNameScope(termsOfServiceView, nameScope);
		termsOfServiceView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 18, 10);
		resourceDictionary.Add("BooleanSwitchConverter", value);
		resourceDictionary.Add("ButtonColor", color);
		termsOfServiceView.SetValue(NavigationPage.HasBackButtonProperty, true);
		termsOfServiceView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		termsOfServiceView.SetValue(Page.TitleProperty, "利用規約");
		onPlatform.iOS = new Thickness(20.0, 20.0, 20.0, 20.0);
		onPlatform.Android = new Thickness(20.0, 10.0, 20.0, 20.0);
		termsOfServiceView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 13, 10);
		termsOfServiceView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 18, 10);
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		webView.SetValue(Grid.RowProperty, 0);
		bindingExtension.Path = "ContentUrl";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		webView.SetBinding(WebView.SourceProperty, binding);
		webView.Navigated += termsOfServiceView.WebView_Navigated;
		webView.MeasureInvalidated += termsOfServiceView.WebView_MeasureInvalidated;
		grid.Children.Add(webView);
		VisualDiagnostics.RegisterSourceInfo(webView, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 37, 10);
		bindingExtension2.Mode = BindingMode.OneWay;
		bindingExtension2.Path = "IsNavigating";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		activityIndicator.SetBinding(ActivityIndicator.IsRunningProperty, binding2);
		activityIndicator.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.Center);
		activityIndicator.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Center);
		grid.Children.Add(activityIndicator);
		VisualDiagnostics.RegisterSourceInfo(activityIndicator, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 40, 10);
		button.SetValue(Grid.RowProperty, 1);
		button.SetValue(Message.AttachProperty, "Agree");
		button.SetValue(Button.TextProperty, "同意します。");
		staticResourceExtension.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 3];
		array[0] = button;
		array[1] = grid;
		array[2] = termsOfServiceView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("converter", "clr-namespace:NHL.Views.Converters");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(TermsOfServiceView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(42, 72)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		button.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 42, 72);
		bindingExtension3.Path = "IsConfirmButtonEnable";
		BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		button.SetBinding(VisualElement.IsEnabledProperty, binding3);
		button.SetValue(Button.TextColorProperty, Color.White);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 44, 18);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 42, 10);
		termsOfServiceView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 31, 6);
		VisualDiagnostics.RegisterSourceInfo(termsOfServiceView, new Uri("Views/TermsOfServiceView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(TermsOfServiceView));
	}
}
