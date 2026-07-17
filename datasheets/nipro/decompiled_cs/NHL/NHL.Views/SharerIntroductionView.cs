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
[XamlFilePath("Views/SharerIntroductionView.xaml")]
public class SharerIntroductionView : ContentPage
{
	public SharerIntroductionView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(SharerIntroductionView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/SharerIntroductionView.xaml",
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
		Color color2 = Color.FromHex("#5B9BD5");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Span span = new Span();
		string newLine = Environment.NewLine;
		Span span2 = new Span();
		Span span3 = new Span();
		FormattedString formattedString = new FormattedString();
		Label label = new Label();
		StaticResourceExtension staticResourceExtension2 = new StaticResourceExtension();
		BindingExtension bindingExtension = new BindingExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid = new Grid();
		StackLayout stackLayout = new StackLayout();
		SharerIntroductionView sharerIntroductionView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(sharerIntroductionView = this) ?? new NameScope());
		NameScope.SetNameScope(sharerIntroductionView, nameScope);
		sharerIntroductionView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 15, 10);
		resourceDictionary.Add("PageBackgroundColor", color);
		resourceDictionary.Add("ButtonColor", color2);
		sharerIntroductionView.SetValue(NavigationPage.HasBackButtonProperty, true);
		sharerIntroductionView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		sharerIntroductionView.SetValue(Page.TitleProperty, "WEB機能（本人用）");
		staticResourceExtension.Key = "PageBackgroundColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 1];
		array[0] = sharerIntroductionView;
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
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(SharerIntroductionView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(12, 14)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		sharerIntroductionView.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 12, 14);
		sharerIntroductionView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 15, 10);
		stackLayout.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		stackLayout.SetValue(StackLayout.SpacingProperty, 0.0);
		grid.SetValue(Grid.RowSpacingProperty, 0.0);
		grid.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		label.SetValue(Grid.RowProperty, 0);
		label.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 0.0));
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 4];
		array2[0] = label;
		array2[1] = grid;
		array2[2] = stackLayout;
		array2[3] = sharerIntroductionView;
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
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(SharerIntroductionView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(41, 53)));
		label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider2));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 43, 22);
		span.SetValue(Span.TextProperty, "WEB機能用のアカウントを作成することで、家族や友人にデータを共有することが出来ます。");
		formattedString.Spans.Add(span);
		VisualDiagnostics.RegisterSourceInfo(span, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 47, 26);
		span2.SetValue(Span.TextProperty, newLine);
		VisualDiagnostics.RegisterSourceInfo(newLine, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 48, 31);
		formattedString.Spans.Add(span2);
		VisualDiagnostics.RegisterSourceInfo(span2, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 48, 26);
		span3.SetValue(Span.TextProperty, "ご利用にはご本人の「アカウント登録」とWEBページ内での設定が必要です。（無料）");
		formattedString.Spans.Add(span3);
		VisualDiagnostics.RegisterSourceInfo(span3, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 49, 26);
		label.SetValue(Label.FormattedTextProperty, formattedString);
		VisualDiagnostics.RegisterSourceInfo(formattedString, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 46, 22);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 41, 14);
		button.SetValue(Grid.RowProperty, 1);
		button.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 20.0));
		button.SetValue(Button.TextProperty, "アカウント登録");
		button.SetValue(Message.AttachProperty, "NextView");
		button.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		button.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension2.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
		Type typeFromHandle5 = typeof(IProvideValueTarget);
		object[] array3 = new object[0 + 4];
		array3[0] = button;
		array3[1] = grid;
		array3[2] = stackLayout;
		array3[3] = sharerIntroductionView;
		object service3;
		xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array3, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
		Type typeFromHandle6 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
		xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver3.Add("system", "clr-namespace:System;assembly=System.Runtime.Extensions");
		xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(SharerIntroductionView).GetTypeInfo().Assembly));
		xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(60, 17)));
		object obj2 = ((IMarkupExtension)staticResourceExtension2).ProvideValue((IServiceProvider)xamlServiceProvider3);
		button.BackgroundColor = (Color)obj2;
		VisualDiagnostics.RegisterSourceInfo(obj2, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 60, 17);
		bindingExtension.Path = "IsNetworkEnabled";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		button.SetBinding(VisualElement.IsEnabledProperty, binding);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 63, 22);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 54, 14);
		stackLayout.Children.Add(grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 34, 10);
		sharerIntroductionView.SetValue(ContentPage.ContentProperty, stackLayout);
		VisualDiagnostics.RegisterSourceInfo(stackLayout, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 33, 6);
		VisualDiagnostics.RegisterSourceInfo(sharerIntroductionView, new Uri("Views/SharerIntroductionView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(SharerIntroductionView));
	}
}
