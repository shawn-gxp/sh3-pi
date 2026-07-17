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
[XamlFilePath("Views/InformationView.xaml")]
public class InformationView : ContentPage
{
	public InformationView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(InformationView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/InformationView.xaml",
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
		Color color = Color.FromHex("#5B9BD5");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		RowDefinition rowDefinition5 = new RowDefinition();
		RowDefinition item = new RowDefinition();
		ColumnDefinition columnDefinition = new ColumnDefinition();
		ColumnDefinition columnDefinition2 = new ColumnDefinition();
		ColumnDefinition item2 = new ColumnDefinition();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Label label2 = new Label();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
		Label label3 = new Label();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect4 = new FixedFontSizeLabelEffect();
		Label label4 = new Label();
		BindingExtension bindingExtension = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect5 = new FixedFontSizeLabelEffect();
		Label label5 = new Label();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect6 = new FixedFontSizeLabelEffect();
		Label label6 = new Label();
		StaticResourceExtension staticResourceExtension = new StaticResourceExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid = new Grid();
		InformationView informationView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(informationView = this) ?? new NameScope());
		NameScope.SetNameScope(informationView, nameScope);
		informationView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 16, 10);
		resourceDictionary.Add("ButtonColor", color);
		informationView.SetValue(Page.TitleProperty, "情報");
		onPlatform.iOS = new Thickness(20.0, 20.0, 0.0, 20.0);
		onPlatform.Android = new Thickness(20.0, 10.0, 0.0, 20.0);
		informationView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 10, 10);
		informationView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 16, 10);
		grid.SetValue(AbsoluteLayout.LayoutFlagsProperty, AbsoluteLayoutFlags.All);
		grid.SetValue(AbsoluteLayout.LayoutBoundsProperty, new Rectangle(0.0, 0.0, 1.0, 1.0));
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("30"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("30"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("*"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		rowDefinition5.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("*"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition5);
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		columnDefinition.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition);
		columnDefinition2.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition2);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item2);
		label.SetValue(Grid.RowProperty, 0);
		label.SetValue(Label.TextProperty, "バージョン");
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 3];
		array[0] = label;
		array[1] = grid;
		array[2] = informationView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, Label.FontSizeProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(InformationView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(41, 42)));
		label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Small", (IServiceProvider)xamlServiceProvider));
		label.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.StartAndExpand);
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 43, 18);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 41, 10);
		label2.SetValue(Grid.RowProperty, 1);
		label2.SetValue(Label.TextProperty, "開発元");
		BindableProperty fontSizeProperty2 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter2 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 3];
		array2[0] = label2;
		array2[1] = grid;
		array2[2] = informationView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, Label.FontSizeProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(InformationView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(46, 40)));
		label2.SetValue(fontSizeProperty2, ((IExtendedTypeConverter)fontSizeConverter2).ConvertFromInvariantString("Small", (IServiceProvider)xamlServiceProvider2));
		label2.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.StartAndExpand);
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 48, 18);
		grid.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 46, 10);
		label3.SetValue(Grid.RowProperty, 0);
		label3.SetValue(Grid.ColumnProperty, 1);
		label3.SetValue(Label.TextProperty, "：");
		label3.Effects.Add(fixedFontSizeLabelEffect3);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 54, 18);
		grid.Children.Add(label3);
		VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 52, 10);
		label4.SetValue(Grid.RowProperty, 1);
		label4.SetValue(Grid.ColumnProperty, 1);
		label4.SetValue(Label.TextProperty, "：");
		label4.Effects.Add(fixedFontSizeLabelEffect4);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect4, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 59, 18);
		grid.Children.Add(label4);
		VisualDiagnostics.RegisterSourceInfo(label4, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 57, 10);
		label5.SetValue(Grid.RowProperty, 0);
		label5.SetValue(Grid.ColumnProperty, 2);
		bindingExtension.Path = "Version";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		label5.SetBinding(Label.TextProperty, binding);
		label5.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.StartAndExpand);
		label5.Effects.Add(fixedFontSizeLabelEffect5);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect5, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 65, 18);
		grid.Children.Add(label5);
		VisualDiagnostics.RegisterSourceInfo(label5, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 63, 10);
		label6.SetValue(Grid.RowProperty, 1);
		label6.SetValue(Grid.ColumnProperty, 2);
		label6.SetValue(Label.TextProperty, "ニプロ株式会社");
		label6.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.StartAndExpand);
		label6.Effects.Add(fixedFontSizeLabelEffect6);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect6, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 70, 18);
		grid.Children.Add(label6);
		VisualDiagnostics.RegisterSourceInfo(label6, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 68, 10);
		button.SetValue(Grid.RowProperty, 3);
		button.SetValue(Grid.ColumnSpanProperty, 3);
		button.SetValue(Button.TextProperty, "ログをサーバーへ送信");
		button.SetValue(VisualElement.WidthRequestProperty, 210.0);
		button.SetValue(Message.AttachProperty, "LogTransfer");
		button.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.CenterAndExpand);
		button.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
		Type typeFromHandle5 = typeof(IProvideValueTarget);
		object[] array3 = new object[0 + 3];
		array3[0] = button;
		array3[1] = grid;
		array3[2] = informationView;
		object service3;
		xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array3, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
		Type typeFromHandle6 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
		xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(InformationView).GetTypeInfo().Assembly));
		xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(74, 175)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider3);
		button.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 74, 175);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 76, 18);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 74, 10);
		informationView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 26, 6);
		VisualDiagnostics.RegisterSourceInfo(informationView, new Uri("Views/InformationView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(InformationView));
	}
}
