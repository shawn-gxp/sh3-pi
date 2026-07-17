using System;
using System.CodeDom.Compiler;
using System.Reflection;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/MeterDetailView.xaml")]
public class MeterDetailView : ContentPage
{
	public MeterDetailView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(MeterDetailView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/MeterDetailView.xaml",
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
		StaticResourceExtension staticResourceExtension = new StaticResourceExtension();
		Color color = Color.FromHex("#EFEFF4");
		Color color2 = Color.FromHex("#F44336");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		OnPlatform<Thickness> onPlatform = new OnPlatform<Thickness>();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		RowDefinition rowDefinition5 = new RowDefinition();
		RowDefinition item = new RowDefinition();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		BindingExtension bindingExtension = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Label label2 = new Label();
		Grid grid = new Grid();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
		Label label3 = new Label();
		BindingExtension bindingExtension2 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect4 = new FixedFontSizeLabelEffect();
		Label label4 = new Label();
		Grid grid2 = new Grid();
		StaticResourceExtension staticResourceExtension2 = new StaticResourceExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid3 = new Grid();
		MeterDetailView meterDetailView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(meterDetailView = this) ?? new NameScope());
		NameScope.SetNameScope(meterDetailView, nameScope);
		meterDetailView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 11, 10);
		resourceDictionary.Add("PageBackgroundColor", color);
		resourceDictionary.Add("WarnButtonColor", color2);
		meterDetailView.SetValue(Page.TitleProperty, "測定器詳細");
		staticResourceExtension.Key = "PageBackgroundColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 1];
		array[0] = meterDetailView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(MeterDetailView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(8, 14)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		meterDetailView.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 8, 14);
		meterDetailView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 11, 10);
		onPlatform.iOS = new Thickness(0.0, 20.0, 0.0, 20.0);
		onPlatform.Android = new Thickness(0.0, 10.0, 0.0, 20.0);
		meterDetailView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 30, 10);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		rowDefinition5.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition5);
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		label.SetValue(Grid.RowProperty, 0);
		label.SetValue(Label.TextProperty, "名前");
		label.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 47, 18);
		grid3.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 45, 10);
		grid.SetValue(Grid.RowProperty, 1);
		grid.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		grid.SetValue(VisualElement.HeightRequestProperty, 45.0);
		bindingExtension.Path = "Meter.Name";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		label2.SetBinding(Label.TextProperty, binding);
		label2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
		label2.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.Center);
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 53, 22);
		grid.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 51, 14);
		grid3.Children.Add(grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 50, 10);
		label3.SetValue(Grid.RowProperty, 2);
		label3.SetValue(Label.TextProperty, "シリアル");
		label3.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
		label3.Effects.Add(fixedFontSizeLabelEffect3);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 60, 18);
		grid3.Children.Add(label3);
		VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 58, 10);
		grid2.SetValue(Grid.RowProperty, 3);
		grid2.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		grid2.SetValue(VisualElement.HeightRequestProperty, 45.0);
		bindingExtension2.Path = "Meter.SerialNumber";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		label4.SetBinding(Label.TextProperty, binding2);
		label4.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
		label4.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.Center);
		label4.Effects.Add(fixedFontSizeLabelEffect4);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect4, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 66, 22);
		grid2.Children.Add(label4);
		VisualDiagnostics.RegisterSourceInfo(label4, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 64, 14);
		grid3.Children.Add(grid2);
		VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 63, 10);
		button.SetValue(Grid.RowProperty, 4);
		button.SetValue(Message.AttachProperty, "Unregister");
		button.SetValue(Button.TextProperty, "この測定器の登録を解除");
		button.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(30.0, 0.0));
		button.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension2.Key = "WarnButtonColor";
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 3];
		array2[0] = button;
		array2[1] = grid3;
		array2[2] = meterDetailView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(MeterDetailView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(71, 114)));
		object obj2 = ((IMarkupExtension)staticResourceExtension2).ProvideValue((IServiceProvider)xamlServiceProvider2);
		button.BackgroundColor = (Color)obj2;
		VisualDiagnostics.RegisterSourceInfo(obj2, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 71, 114);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 73, 18);
		grid3.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 71, 10);
		meterDetailView.SetValue(ContentPage.ContentProperty, grid3);
		VisualDiagnostics.RegisterSourceInfo(grid3, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 35, 6);
		VisualDiagnostics.RegisterSourceInfo(meterDetailView, new Uri("Views/MeterDetailView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(MeterDetailView));
	}
}
