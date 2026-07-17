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
[XamlFilePath("Views/NoticeDetailView.xaml")]
public class NoticeDetailView : ContentPage
{
	public NoticeDetailView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(NoticeDetailView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/NoticeDetailView.xaml",
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
		OnPlatform<Thickness> onPlatform = new OnPlatform<Thickness>();
		BindingExtension bindingExtension = new BindingExtension();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		RowDefinition rowDefinition5 = new RowDefinition();
		BindingExtension bindingExtension2 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		BindingExtension bindingExtension3 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Label label2 = new Label();
		BindingExtension bindingExtension4 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
		Label label3 = new Label();
		BindingExtension bindingExtension5 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect4 = new FixedFontSizeLabelEffect();
		Label label4 = new Label();
		StaticResourceExtension staticResourceExtension2 = new StaticResourceExtension();
		BindingExtension bindingExtension6 = new BindingExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid = new Grid();
		StackLayout stackLayout = new StackLayout();
		ScrollView scrollView = new ScrollView();
		NoticeDetailView noticeDetailView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(noticeDetailView = this) ?? new NameScope());
		NameScope.SetNameScope(noticeDetailView, nameScope);
		noticeDetailView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 14, 10);
		resourceDictionary.Add("PageBackgroundColor", color);
		resourceDictionary.Add("ButtonColor", color2);
		noticeDetailView.SetValue(NavigationPage.HasBackButtonProperty, true);
		noticeDetailView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		noticeDetailView.SetValue(Page.TitleProperty, "お知らせ");
		staticResourceExtension.Key = "PageBackgroundColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 1];
		array[0] = noticeDetailView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(NoticeDetailView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(11, 14)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		noticeDetailView.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 11, 14);
		noticeDetailView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 14, 10);
		onPlatform.iOS = new Thickness(0.0, 20.0, 0.0, 20.0);
		onPlatform.Android = new Thickness(0.0, 10.0, 0.0, 20.0);
		noticeDetailView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 32, 10);
		scrollView.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		scrollView.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		stackLayout.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Start);
		grid.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Fill);
		grid.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.Center);
		bindingExtension.Path = "IsNetworkEnabled";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		grid.SetBinding(VisualElement.IsVisibleProperty, binding);
		grid.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0));
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		rowDefinition5.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition5);
		label.SetValue(Grid.RowProperty, 0);
		bindingExtension2.StringFormat = "{0} からのお知らせ";
		bindingExtension2.Path = "NoticeDetail.Hospital.FacilitiesName";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		label.SetBinding(Label.TextProperty, binding2);
		label.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 52, 30);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 50, 22);
		label2.SetValue(Grid.RowProperty, 1);
		bindingExtension3.Path = "NoticeDetail.Notice.Title";
		BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		label2.SetBinding(Label.TextProperty, binding3);
		label2.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 5];
		array2[0] = label2;
		array2[1] = grid;
		array2[2] = stackLayout;
		array2[3] = scrollView;
		array2[4] = noticeDetailView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, Label.FontSizeProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(NoticeDetailView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(56, 115)));
		label2.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider2));
		label2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0));
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 58, 30);
		grid.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 56, 22);
		label3.SetValue(Grid.RowProperty, 2);
		bindingExtension4.StringFormat = "{0:yyyy/MM/dd(ddd) HH:mm}";
		bindingExtension4.Path = "NoticeDetail.Notice.ExpiryDateFrom";
		BindingBase binding4 = ((IMarkupExtension<BindingBase>)bindingExtension4).ProvideValue((IServiceProvider)null);
		label3.SetBinding(Label.TextProperty, binding4);
		label3.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
		label3.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.End);
		label3.Effects.Add(fixedFontSizeLabelEffect3);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 64, 30);
		grid.Children.Add(label3);
		VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 62, 22);
		label4.SetValue(Grid.RowProperty, 3);
		bindingExtension5.Path = "NoticeDetail.Notice.Detail";
		BindingBase binding5 = ((IMarkupExtension<BindingBase>)bindingExtension5).ProvideValue((IServiceProvider)null);
		label4.SetBinding(Label.TextProperty, binding5);
		label4.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Start"));
		label4.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		label4.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(2.0));
		label4.Effects.Add(fixedFontSizeLabelEffect4);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect4, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 70, 30);
		grid.Children.Add(label4);
		VisualDiagnostics.RegisterSourceInfo(label4, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 68, 22);
		button.SetValue(Grid.RowProperty, 4);
		button.SetValue(Button.TextProperty, "設定を確認する");
		button.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(2.0));
		button.SetValue(Message.AttachProperty, "OnInputButtonTapped");
		button.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension2.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
		Type typeFromHandle5 = typeof(IProvideValueTarget);
		object[] array3 = new object[0 + 5];
		array3[0] = button;
		array3[1] = grid;
		array3[2] = stackLayout;
		array3[3] = scrollView;
		array3[4] = noticeDetailView;
		object service3;
		xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array3, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
		Type typeFromHandle6 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
		xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(NoticeDetailView).GetTypeInfo().Assembly));
		xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(74, 129)));
		object obj2 = ((IMarkupExtension)staticResourceExtension2).ProvideValue((IServiceProvider)xamlServiceProvider3);
		button.BackgroundColor = (Color)obj2;
		VisualDiagnostics.RegisterSourceInfo(obj2, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 74, 129);
		bindingExtension6.Path = "IsTransitionableToRegisterHospitalView";
		BindingBase binding6 = ((IMarkupExtension<BindingBase>)bindingExtension6).ProvideValue((IServiceProvider)null);
		button.SetBinding(VisualElement.IsVisibleProperty, binding6);
		button.SetValue(Button.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		fixedFontSizeButtonEffect.FixedSize = 8;
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 77, 30);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 74, 22);
		stackLayout.Children.Add(grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 41, 18);
		scrollView.Content = stackLayout;
		VisualDiagnostics.RegisterSourceInfo(stackLayout, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 39, 14);
		noticeDetailView.SetValue(ContentPage.ContentProperty, scrollView);
		VisualDiagnostics.RegisterSourceInfo(scrollView, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 38, 10);
		VisualDiagnostics.RegisterSourceInfo(noticeDetailView, new Uri("Views/NoticeDetailView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(NoticeDetailView));
	}
}
