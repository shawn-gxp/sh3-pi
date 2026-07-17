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
[XamlFilePath("Views/ManualDataSyncView.xaml")]
public class ManualDataSyncView : ContentPage
{
	public ManualDataSyncView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(ManualDataSyncView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/ManualDataSyncView.xaml",
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
		StaticResourceExtension staticResourceExtension = new StaticResourceExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Span span = new Span();
		string newLine = Environment.NewLine;
		Span span2 = new Span();
		Span span3 = new Span();
		string newLine2 = Environment.NewLine;
		Span span4 = new Span();
		Span span5 = new Span();
		FormattedString formattedString = new FormattedString();
		Label label = new Label();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Span span6 = new Span();
		string newLine3 = Environment.NewLine;
		Span span7 = new Span();
		Span span8 = new Span();
		string newLine4 = Environment.NewLine;
		Span span9 = new Span();
		Span span10 = new Span();
		string newLine5 = Environment.NewLine;
		Span span11 = new Span();
		Span span12 = new Span();
		FormattedString formattedString2 = new FormattedString();
		Label label2 = new Label();
		Span span13 = new Span();
		BindingExtension bindingExtension = new BindingExtension();
		Span span14 = new Span();
		FormattedString formattedString3 = new FormattedString();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
		Label label3 = new Label();
		Grid grid = new Grid();
		ManualDataSyncView manualDataSyncView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(manualDataSyncView = this) ?? new NameScope());
		NameScope.SetNameScope(manualDataSyncView, nameScope);
		manualDataSyncView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 17, 10);
		resourceDictionary.Add("ButtonColor", color);
		manualDataSyncView.SetValue(Page.TitleProperty, "データ同期");
		onPlatform.iOS = new Thickness(20.0, 20.0, 0.0, 20.0);
		onPlatform.Android = new Thickness(0.0, 10.0, 0.0, 20.0);
		manualDataSyncView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 11, 10);
		manualDataSyncView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 17, 10);
		grid.SetValue(AbsoluteLayout.LayoutFlagsProperty, AbsoluteLayoutFlags.All);
		grid.SetValue(AbsoluteLayout.LayoutBoundsProperty, new Rectangle(0.0, 0.0, 1.0, 1.0));
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		button.SetValue(Grid.RowProperty, 0);
		button.SetValue(Button.TextProperty, "データ同期");
		button.SetValue(VisualElement.WidthRequestProperty, 210.0);
		button.SetValue(Message.AttachProperty, "ManualDataSync");
		button.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.CenterAndExpand);
		button.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 3];
		array[0] = button;
		array[1] = grid;
		array[2] = manualDataSyncView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver.Add("system", "clr-namespace:System;assembly=System.Runtime.Extensions");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(ManualDataSyncView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(35, 153)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		button.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 35, 153);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 37, 18);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 35, 10);
		label.SetValue(Grid.RowProperty, 1);
		label.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 0.0));
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 3];
		array2[0] = label;
		array2[1] = grid;
		array2[2] = manualDataSyncView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, Label.FontSizeProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver2.Add("system", "clr-namespace:System;assembly=System.Runtime.Extensions");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(ManualDataSyncView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(41, 49)));
		label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Default", (IServiceProvider)xamlServiceProvider2));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 43, 18);
		span.SetValue(Span.TextProperty, "※データをサーバと同期します。");
		formattedString.Spans.Add(span);
		VisualDiagnostics.RegisterSourceInfo(span, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 47, 22);
		span2.SetValue(Span.TextProperty, newLine);
		VisualDiagnostics.RegisterSourceInfo(newLine, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 48, 27);
		formattedString.Spans.Add(span2);
		VisualDiagnostics.RegisterSourceInfo(span2, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 48, 22);
		span3.SetValue(Span.TextProperty, "ボタンを押さなくても、");
		formattedString.Spans.Add(span3);
		VisualDiagnostics.RegisterSourceInfo(span3, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 49, 22);
		span4.SetValue(Span.TextProperty, newLine2);
		VisualDiagnostics.RegisterSourceInfo(newLine2, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 50, 27);
		formattedString.Spans.Add(span4);
		VisualDiagnostics.RegisterSourceInfo(span4, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 50, 22);
		span5.SetValue(Span.TextProperty, "アプリ起動時にデータは同期されます。");
		formattedString.Spans.Add(span5);
		VisualDiagnostics.RegisterSourceInfo(span5, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 51, 22);
		label.SetValue(Label.FormattedTextProperty, formattedString);
		VisualDiagnostics.RegisterSourceInfo(formattedString, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 46, 18);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 41, 10);
		label2.SetValue(Grid.RowProperty, 2);
		label2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 0.0));
		BindableProperty fontSizeProperty2 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter2 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
		Type typeFromHandle5 = typeof(IProvideValueTarget);
		object[] array3 = new object[0 + 3];
		array3[0] = label2;
		array3[1] = grid;
		array3[2] = manualDataSyncView;
		object service3;
		xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array3, Label.FontSizeProperty, nameScope));
		xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
		Type typeFromHandle6 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
		xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver3.Add("system", "clr-namespace:System;assembly=System.Runtime.Extensions");
		xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(ManualDataSyncView).GetTypeInfo().Assembly));
		xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(56, 49)));
		label2.SetValue(fontSizeProperty2, ((IExtendedTypeConverter)fontSizeConverter2).ConvertFromInvariantString("Default", (IServiceProvider)xamlServiceProvider3));
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 58, 18);
		span6.SetValue(Span.TextProperty, "※データ件数が多くなると");
		formattedString2.Spans.Add(span6);
		VisualDiagnostics.RegisterSourceInfo(span6, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 62, 22);
		span7.SetValue(Span.TextProperty, newLine3);
		VisualDiagnostics.RegisterSourceInfo(newLine3, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 63, 27);
		formattedString2.Spans.Add(span7);
		VisualDiagnostics.RegisterSourceInfo(span7, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 63, 22);
		span8.SetValue(Span.TextProperty, "自動同期で処理が完了していない場合があります。");
		formattedString2.Spans.Add(span8);
		VisualDiagnostics.RegisterSourceInfo(span8, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 64, 22);
		span9.SetValue(Span.TextProperty, newLine4);
		VisualDiagnostics.RegisterSourceInfo(newLine4, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 65, 27);
		formattedString2.Spans.Add(span9);
		VisualDiagnostics.RegisterSourceInfo(span9, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 65, 22);
		span10.SetValue(Span.TextProperty, "上記「データ同期」ボタンを押して");
		formattedString2.Spans.Add(span10);
		VisualDiagnostics.RegisterSourceInfo(span10, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 66, 22);
		span11.SetValue(Span.TextProperty, newLine5);
		VisualDiagnostics.RegisterSourceInfo(newLine5, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 67, 27);
		formattedString2.Spans.Add(span11);
		VisualDiagnostics.RegisterSourceInfo(span11, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 67, 22);
		span12.SetValue(Span.TextProperty, "同期を完了させてください。");
		formattedString2.Spans.Add(span12);
		VisualDiagnostics.RegisterSourceInfo(span12, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 68, 22);
		label2.SetValue(Label.FormattedTextProperty, formattedString2);
		VisualDiagnostics.RegisterSourceInfo(formattedString2, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 61, 18);
		grid.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 56, 10);
		label3.SetValue(Grid.RowProperty, 3);
		label3.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 0.0));
		BindableProperty fontSizeProperty3 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter3 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider4 = new XamlServiceProvider();
		Type typeFromHandle7 = typeof(IProvideValueTarget);
		object[] array4 = new object[0 + 3];
		array4[0] = label3;
		array4[1] = grid;
		array4[2] = manualDataSyncView;
		object service4;
		xamlServiceProvider4.Add(typeFromHandle7, service4 = new SimpleValueTargetProvider(array4, Label.FontSizeProperty, nameScope));
		xamlServiceProvider4.Add(typeof(IReferenceProvider), service4);
		Type typeFromHandle8 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver4 = new XmlNamespaceResolver();
		xmlNamespaceResolver4.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver4.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver4.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver4.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver4.Add("system", "clr-namespace:System;assembly=System.Runtime.Extensions");
		xamlServiceProvider4.Add(typeFromHandle8, new XamlTypeResolver(xmlNamespaceResolver4, typeof(ManualDataSyncView).GetTypeInfo().Assembly));
		xamlServiceProvider4.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(73, 49)));
		label3.SetValue(fontSizeProperty3, ((IExtendedTypeConverter)fontSizeConverter3).ConvertFromInvariantString("Default", (IServiceProvider)xamlServiceProvider4));
		span13.SetValue(Span.TextProperty, "最終同期日時：");
		formattedString3.Spans.Add(span13);
		VisualDiagnostics.RegisterSourceInfo(span13, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 76, 22);
		bindingExtension.StringFormat = "{0:yyyy/MM/dd HH:mm}";
		bindingExtension.Path = "DisplayUpdatedAt";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		span14.SetBinding(Span.TextProperty, binding);
		formattedString3.Spans.Add(span14);
		VisualDiagnostics.RegisterSourceInfo(span14, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 77, 22);
		label3.SetValue(Label.FormattedTextProperty, formattedString3);
		VisualDiagnostics.RegisterSourceInfo(formattedString3, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 75, 18);
		label3.Effects.Add(fixedFontSizeLabelEffect3);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 81, 18);
		grid.Children.Add(label3);
		VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 73, 10);
		manualDataSyncView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 27, 6);
		VisualDiagnostics.RegisterSourceInfo(manualDataSyncView, new Uri("Views/ManualDataSyncView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(ManualDataSyncView));
	}
}
