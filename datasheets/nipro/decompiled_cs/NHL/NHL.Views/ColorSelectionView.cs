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
[XamlFilePath("Views/ColorSelectionView.xaml")]
public class ColorSelectionView : ContentPage
{
	public ColorSelectionView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(ColorSelectionView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/ColorSelectionView.xaml",
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
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition item = new RowDefinition();
		ColumnDefinition columnDefinition = new ColumnDefinition();
		ColumnDefinition item2 = new ColumnDefinition();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		BindingExtension bindingExtension = new BindingExtension();
		BindingExtension bindingExtension2 = new BindingExtension();
		BindingExtension bindingExtension3 = new BindingExtension();
		FixedFontSizeTextBoxEffect fixedFontSizeTextBoxEffect = new FixedFontSizeTextBoxEffect();
		Picker picker = new Picker();
		StaticResourceExtension staticResourceExtension2 = new StaticResourceExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		StaticResourceExtension staticResourceExtension3 = new StaticResourceExtension();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect2 = new FixedFontSizeButtonEffect();
		Button button2 = new Button();
		Grid grid = new Grid();
		ColorSelectionView colorSelectionView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(colorSelectionView = this) ?? new NameScope());
		NameScope.SetNameScope(colorSelectionView, nameScope);
		colorSelectionView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 12, 10);
		resourceDictionary.Add("PageBackgroundColor", color);
		resourceDictionary.Add("ButtonColor", color2);
		colorSelectionView.SetValue(Page.TitleProperty, "色選択");
		staticResourceExtension.Key = "PageBackgroundColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 1];
		array[0] = colorSelectionView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(ColorSelectionView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(8, 14)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		colorSelectionView.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 8, 14);
		colorSelectionView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		colorSelectionView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 12, 10);
		grid.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(30.0));
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		columnDefinition.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition);
		((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item2);
		label.SetValue(Grid.RowProperty, 0);
		label.SetValue(Grid.ColumnProperty, 0);
		label.SetValue(Label.TextProperty, "表示色");
		label.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 45, 18);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 43, 10);
		picker.SetValue(Grid.RowProperty, 0);
		picker.SetValue(Grid.ColumnProperty, 1);
		bindingExtension.Path = "ColorList";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		picker.SetBinding(Picker.ItemsSourceProperty, binding);
		bindingExtension2.Path = "Name";
		BindingBase target = (picker.ItemDisplayBinding = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null));
		VisualDiagnostics.RegisterSourceInfo(target, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 48, 80);
		bindingExtension3.Mode = BindingMode.TwoWay;
		bindingExtension3.Path = "SelectedColor";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		picker.SetBinding(Picker.SelectedItemProperty, binding2);
		picker.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		picker.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(5.0, 0.0, 0.0, 0.0));
		picker.Effects.Add(fixedFontSizeTextBoxEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeTextBoxEffect, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 50, 18);
		grid.Children.Add(picker);
		VisualDiagnostics.RegisterSourceInfo(picker, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 48, 10);
		button.SetValue(Grid.RowProperty, 1);
		button.SetValue(Grid.ColumnSpanProperty, 2);
		button.SetValue(Button.TextProperty, "選択できる色を再取得");
		button.SetValue(Message.AttachProperty, "UpdateColorList");
		button.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		button.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension2.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 3];
		array2[0] = button;
		array2[1] = grid;
		array2[2] = colorSelectionView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(ColorSelectionView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(53, 158)));
		object obj2 = ((IMarkupExtension)staticResourceExtension2).ProvideValue((IServiceProvider)xamlServiceProvider2);
		button.BackgroundColor = (Color)obj2;
		VisualDiagnostics.RegisterSourceInfo(obj2, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 53, 158);
		button.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 0.0));
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 55, 18);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 53, 10);
		button2.SetValue(Grid.RowProperty, 2);
		button2.SetValue(Grid.ColumnSpanProperty, 2);
		button2.SetValue(Button.TextProperty, "決定");
		button2.SetValue(Message.AttachProperty, "Decision");
		button2.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		button2.SetValue(Button.TextColorProperty, Color.White);
		staticResourceExtension3.Key = "ButtonColor";
		XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
		Type typeFromHandle5 = typeof(IProvideValueTarget);
		object[] array3 = new object[0 + 3];
		array3[0] = button2;
		array3[1] = grid;
		array3[2] = colorSelectionView;
		object service3;
		xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array3, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
		Type typeFromHandle6 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
		xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(ColorSelectionView).GetTypeInfo().Assembly));
		xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(58, 143)));
		object obj3 = ((IMarkupExtension)staticResourceExtension3).ProvideValue((IServiceProvider)xamlServiceProvider3);
		button2.BackgroundColor = (Color)obj3;
		VisualDiagnostics.RegisterSourceInfo(obj3, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 58, 143);
		button2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 10.0, 10.0, 0.0));
		button2.Effects.Add(fixedFontSizeButtonEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect2, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 60, 18);
		grid.Children.Add(button2);
		VisualDiagnostics.RegisterSourceInfo(button2, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 58, 10);
		colorSelectionView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 31, 6);
		VisualDiagnostics.RegisterSourceInfo(colorSelectionView, new Uri("Views/ColorSelectionView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(ColorSelectionView));
	}
}
