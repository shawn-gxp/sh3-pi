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
[XamlFilePath("Views/ImageTimezoneChangeView.xaml")]
public class ImageTimezoneChangeView : ContentPage
{
	public ImageTimezoneChangeView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(ImageTimezoneChangeView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/ImageTimezoneChangeView.xaml",
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
		Base64ToImageSourceConverter value = new Base64ToImageSourceConverter();
		Color color = Color.FromHex("#4573c4");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		OnPlatform<Thickness> onPlatform = new OnPlatform<Thickness>();
		RowDefinition item = new RowDefinition();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		Grid grid = new Grid();
		StaticResourceExtension staticResourceExtension = new StaticResourceExtension();
		BindingExtension bindingExtension = new BindingExtension();
		Image image = new Image();
		ColumnDefinition item2 = new ColumnDefinition();
		ColumnDefinition item3 = new ColumnDefinition();
		BindingExtension bindingExtension2 = new BindingExtension();
		FixedFontSizeTextBoxEffect fixedFontSizeTextBoxEffect = new FixedFontSizeTextBoxEffect();
		DatePicker datePicker = new DatePicker();
		BindingExtension bindingExtension3 = new BindingExtension();
		FixedFontSizeTextBoxEffect fixedFontSizeTextBoxEffect2 = new FixedFontSizeTextBoxEffect();
		TimePicker timePicker = new TimePicker();
		Grid grid2 = new Grid();
		Image image2 = new Image();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Image image3 = new Image();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect2 = new FixedFontSizeButtonEffect();
		Button button2 = new Button();
		Grid grid3 = new Grid();
		ImageTimezoneChangeView imageTimezoneChangeView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(imageTimezoneChangeView = this) ?? new NameScope());
		NameScope.SetNameScope(imageTimezoneChangeView, nameScope);
		imageTimezoneChangeView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 12, 10);
		resourceDictionary.Add("Base64ToImageSourceConverter", value);
		resourceDictionary.Add("TimezoneLabel", color);
		imageTimezoneChangeView.SetValue(NavigationPage.HasBackButtonProperty, true);
		imageTimezoneChangeView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		imageTimezoneChangeView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 12, 10);
		onPlatform.iOS = new Thickness(0.0, 30.0, 0.0, 0.0);
		onPlatform.Android = new Thickness(0.0, 10.0, 0.0, 0.0);
		imageTimezoneChangeView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 24, 10);
		grid3.SetValue(Grid.RowSpacingProperty, 0.0);
		grid3.SetValue(Grid.ColumnSpacingProperty, 0.0);
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("10"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("40"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("10"));
		((DefinitionCollection<RowDefinition>)grid3.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		grid.SetValue(Grid.RowProperty, 0);
		grid.SetValue(VisualElement.BackgroundColorProperty, Color.Black);
		grid3.Children.Add(grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 38, 10);
		image.SetValue(Grid.RowProperty, 0);
		staticResourceExtension.Key = "Base64ToImageSourceConverter";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 4];
		array[0] = bindingExtension;
		array[1] = image;
		array[2] = grid3;
		array[3] = imageTimezoneChangeView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, typeof(BindingExtension).GetRuntimeProperty("Converter"), nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("converter", "clr-namespace:NHL.Views.Converters");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(ImageTimezoneChangeView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(39, 29)));
		object target = (bindingExtension.Converter = (IValueConverter)((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider));
		VisualDiagnostics.RegisterSourceInfo(target, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 39, 29);
		bindingExtension.Path = "Photograph.Image";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		image.SetBinding(Image.SourceProperty, binding);
		image.SetValue(Image.AspectProperty, Aspect.AspectFit);
		grid3.Children.Add(image);
		VisualDiagnostics.RegisterSourceInfo(image, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 39, 10);
		grid2.SetValue(Grid.RowProperty, 1);
		((DefinitionCollection<ColumnDefinition>)grid2.GetValue(Grid.ColumnDefinitionsProperty)).Add(item2);
		((DefinitionCollection<ColumnDefinition>)grid2.GetValue(Grid.ColumnDefinitionsProperty)).Add(item3);
		datePicker.SetValue(Grid.ColumnProperty, 0);
		bindingExtension2.Mode = BindingMode.TwoWay;
		bindingExtension2.Path = "SelectedPhotographRegistDateOfDate";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		datePicker.SetBinding(DatePicker.DateProperty, binding2);
		datePicker.SetValue(DatePicker.FormatProperty, "yyyy/MM/dd");
		datePicker.Effects.Add(fixedFontSizeTextBoxEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeTextBoxEffect, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 49, 22);
		grid2.Children.Add(datePicker);
		VisualDiagnostics.RegisterSourceInfo(datePicker, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 47, 14);
		timePicker.SetValue(Grid.ColumnProperty, 1);
		bindingExtension3.Mode = BindingMode.TwoWay;
		bindingExtension3.Path = "SelectedPhotographRegistDateOfTime";
		BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		timePicker.SetBinding(TimePicker.TimeProperty, binding3);
		timePicker.SetValue(TimePicker.FormatProperty, "HH:mm");
		timePicker.Effects.Add(fixedFontSizeTextBoxEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeTextBoxEffect2, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 54, 22);
		grid2.Children.Add(timePicker);
		VisualDiagnostics.RegisterSourceInfo(timePicker, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 52, 14);
		grid3.Children.Add(grid2);
		VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 41, 10);
		image2.SetValue(Grid.RowProperty, 3);
		image2.SetValue(Image.SourceProperty, new ImageSourceConverter().ConvertFromInvariantString("icon_right_rotate_on.png"));
		image2.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Start);
		image2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0, 0.0, 10.0, 0.0));
		grid3.Children.Add(image2);
		VisualDiagnostics.RegisterSourceInfo(image2, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 59, 10);
		button.SetValue(Grid.RowProperty, 3);
		button.SetValue(Message.AttachProperty, "Rotate");
		button.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Start);
		button.SetValue(VisualElement.WidthRequestProperty, 50.0);
		button.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		button.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0, 0.0, 10.0, 0.0));
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 62, 18);
		grid3.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 60, 10);
		image3.SetValue(Grid.RowProperty, 3);
		image3.SetValue(Image.SourceProperty, new ImageSourceConverter().ConvertFromInvariantString("icon_GarbageCan_on.png"));
		image3.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.End);
		image3.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0, 0.0, 10.0, 0.0));
		grid3.Children.Add(image3);
		VisualDiagnostics.RegisterSourceInfo(image3, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 65, 10);
		button2.SetValue(Grid.RowProperty, 3);
		button2.SetValue(Message.AttachProperty, "Delete");
		button2.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.End);
		button2.SetValue(VisualElement.WidthRequestProperty, 50.0);
		button2.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		button2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0, 0.0, 10.0, 0.0));
		button2.Effects.Add(fixedFontSizeButtonEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect2, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 68, 18);
		grid3.Children.Add(button2);
		VisualDiagnostics.RegisterSourceInfo(button2, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 66, 10);
		imageTimezoneChangeView.SetValue(ContentPage.ContentProperty, grid3);
		VisualDiagnostics.RegisterSourceInfo(grid3, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 29, 6);
		VisualDiagnostics.RegisterSourceInfo(imageTimezoneChangeView, new Uri("Views/ImageTimezoneChangeView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(ImageTimezoneChangeView));
	}
}
