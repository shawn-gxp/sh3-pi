using System;
using System.CodeDom.Compiler;
using System.Reflection;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Views.Converters;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/RecordView.xaml")]
public class RecordView : ContentPage
{
	public RecordView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(RecordView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/RecordView.xaml",
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
		BooleanSwitchConverter value = new BooleanSwitchConverter();
		PresentedMenuOpenColorConverter value2 = new PresentedMenuOpenColorConverter();
		Base64ToImageSourceConverter value3 = new Base64ToImageSourceConverter();
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		ColumnDefinition columnDefinition = new ColumnDefinition();
		ColumnDefinition item = new ColumnDefinition();
		ColumnDefinition item2 = new ColumnDefinition();
		ColumnDefinition item3 = new ColumnDefinition();
		ColumnDefinition item4 = new ColumnDefinition();
		ColumnDefinition item5 = new ColumnDefinition();
		ColumnDefinition item6 = new ColumnDefinition();
		ColumnDefinition item7 = new ColumnDefinition();
		Image image = new Image();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid = new Grid();
		ScrollView scrollView = new ScrollView();
		Grid grid2 = new Grid();
		RecordView recordView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(recordView = this) ?? new NameScope());
		NameScope.SetNameScope(recordView, nameScope);
		recordView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 16, 10);
		resourceDictionary.Add("BooleanSwitchConverter", value);
		resourceDictionary.Add("PresentedMenuOpenColorConverter", value2);
		resourceDictionary.Add("Base64ToImageSourceConverter", value3);
		recordView.SetValue(NavigationPage.HasBackButtonProperty, false);
		recordView.SetValue(Page.IconProperty, new FileImageSourceConverter().ConvertFromInvariantString("icon_record.png"));
		recordView.SetValue(Page.TitleProperty, "");
		recordView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 16, 10);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("80"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("80"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		columnDefinition.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("80"));
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item2);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item3);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item4);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item5);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item6);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item7);
		image.SetValue(Grid.RowProperty, 0);
		image.SetValue(Grid.ColumnProperty, 0);
		image.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(5.0, 10.0, 5.0, 10.0));
		image.SetValue(Image.SourceProperty, new ImageSourceConverter().ConvertFromInvariantString("icon_walk_on"));
		image.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Center);
		image.SetValue(VisualElement.WidthRequestProperty, 80.0);
		image.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		image.SetValue(Image.AspectProperty, Aspect.AspectFit);
		grid.Children.Add(image);
		VisualDiagnostics.RegisterSourceInfo(image, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 45, 18);
		label.SetValue(Grid.RowProperty, 0);
		label.SetValue(Grid.ColumnProperty, 1);
		label.SetValue(Grid.ColumnSpanProperty, 6);
		label.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 20.0, 10.0, 10.0));
		label.SetValue(Label.TextProperty, "歩数");
		label.SetValue(Label.FontFamilyProperty, "Meiryo");
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 5];
		array[0] = label;
		array[1] = grid;
		array[2] = scrollView;
		array[3] = grid2;
		array[4] = recordView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, Label.FontSizeProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("carousel", "clr-namespace:CarouselView.FormsPlugin.Abstractions;assembly=CarouselView.FormsPlugin.Abstractions");
		xmlNamespaceResolver.Add("converter", "clr-namespace:NHL.Views.Converters");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver.Add("behavior", "clr-namespace:NHL.Views.Behaviors");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(RecordView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(46, 124)));
		label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider));
		label.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
		label.SetValue(Label.TextColorProperty, Color.Black);
		label.SetValue(Label.HorizontalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Start"));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 48, 26);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 46, 18);
		button.SetValue(Grid.RowProperty, 1);
		button.SetValue(Grid.ColumnProperty, 0);
		button.SetValue(Grid.ColumnSpanProperty, 8);
		button.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(30.0, 10.0, 30.0, 10.0));
		button.SetValue(Button.BorderWidthProperty, 1.0);
		button.SetValue(Button.TextProperty, "記録");
		button.SetValue(Message.AttachProperty, "RecordStepButtonTapped");
		BindableProperty fontSizeProperty2 = Button.FontSizeProperty;
		FontSizeConverter fontSizeConverter2 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 5];
		array2[0] = button;
		array2[1] = grid;
		array2[2] = scrollView;
		array2[3] = grid2;
		array2[4] = recordView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, Button.FontSizeProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("carousel", "clr-namespace:CarouselView.FormsPlugin.Abstractions;assembly=CarouselView.FormsPlugin.Abstractions");
		xmlNamespaceResolver2.Add("converter", "clr-namespace:NHL.Views.Converters");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver2.Add("behavior", "clr-namespace:NHL.Views.Behaviors");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(RecordView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(51, 166)));
		button.SetValue(fontSizeProperty2, ((IExtendedTypeConverter)fontSizeConverter2).ConvertFromInvariantString("Medium", (IServiceProvider)xamlServiceProvider2));
		button.SetValue(Button.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		button.SetValue(Button.TextColorProperty, Color.Blue);
		button.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		button.SetValue(Button.BorderColorProperty, Color.Black);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 53, 26);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 51, 18);
		scrollView.Content = grid;
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 26, 14);
		grid2.Children.Add(scrollView);
		VisualDiagnostics.RegisterSourceInfo(scrollView, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 25, 10);
		recordView.SetValue(ContentPage.ContentProperty, grid2);
		VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 24, 6);
		VisualDiagnostics.RegisterSourceInfo(recordView, new Uri("Views/RecordView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(RecordView));
	}
}
