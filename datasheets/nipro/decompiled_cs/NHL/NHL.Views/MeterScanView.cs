using System;
using System.CodeDom.Compiler;
using System.Reflection;
using System.Runtime.CompilerServices;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/MeterScanView.xaml")]
public class MeterScanView : ContentPage
{
	[CompilerGenerated]
	private sealed class _003CInitializeComponent_003E_anonXamlCDataTemplate_4
	{
		internal object[] parentValues;

		internal MeterScanView root;

		internal object LoadDataTemplate()
		{
			ColumnDefinition item = new ColumnDefinition();
			ColumnDefinition columnDefinition = new ColumnDefinition();
			ColumnDefinition columnDefinition2 = new ColumnDefinition();
			RowDefinition rowDefinition = new RowDefinition();
			RowDefinition rowDefinition2 = new RowDefinition();
			BindingExtension bindingExtension = new BindingExtension();
			FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
			Label label = new Label();
			BindingExtension bindingExtension2 = new BindingExtension();
			FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
			Label label2 = new Label();
			FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
			Label label3 = new Label();
			Grid grid = new Grid();
			ViewCell viewCell = new ViewCell();
			NameScope nameScope = new NameScope();
			NameScope.SetNameScope(viewCell, nameScope);
			grid.SetValue(View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
			grid.SetValue(Grid.RowSpacingProperty, 0.0);
			((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(item);
			columnDefinition.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("10"));
			((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition);
			columnDefinition2.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("60"));
			((DefinitionCollection<ColumnDefinition>)grid.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition2);
			rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("30"));
			((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
			rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
			((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
			label.SetValue(Grid.RowProperty, 0);
			label.SetValue(Grid.ColumnProperty, 0);
			bindingExtension.Path = "Name";
			BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
			label.SetBinding(Label.TextProperty, binding);
			BindableProperty fontSizeProperty = Label.FontSizeProperty;
			FontSizeConverter fontSizeConverter = new FontSizeConverter();
			XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
			Type typeFromHandle = typeof(IProvideValueTarget);
			int length;
			object[] array = new object[(length = parentValues.Length) + 3];
			Array.Copy(parentValues, 0, array, 3, length);
			array[0] = label;
			array[1] = grid;
			array[2] = viewCell;
			object service;
			xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, Label.FontSizeProperty, nameScope));
			xamlServiceProvider.Add(typeof(IReferenceProvider), service);
			Type typeFromHandle2 = typeof(IXamlTypeResolver);
			XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
			xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
			xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
			xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
			xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
			xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(_003CInitializeComponent_003E_anonXamlCDataTemplate_4).GetTypeInfo().Assembly));
			xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(64, 87)));
			label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider));
			label.SetValue(View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
			label.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			label.Effects.Add(fixedFontSizeLabelEffect);
			VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 66, 38);
			grid.Children.Add(label);
			VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 64, 30);
			label2.SetValue(Grid.RowProperty, 1);
			label2.SetValue(Grid.ColumnProperty, 0);
			bindingExtension2.Path = "IdToString";
			BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
			label2.SetBinding(Label.TextProperty, binding2);
			BindableProperty fontSizeProperty2 = Label.FontSizeProperty;
			FontSizeConverter fontSizeConverter2 = new FontSizeConverter();
			XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
			Type typeFromHandle3 = typeof(IProvideValueTarget);
			int length2;
			object[] array2 = new object[(length2 = parentValues.Length) + 3];
			Array.Copy(parentValues, 0, array2, 3, length2);
			array2[0] = label2;
			array2[1] = grid;
			array2[2] = viewCell;
			object service2;
			xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, Label.FontSizeProperty, nameScope));
			xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
			Type typeFromHandle4 = typeof(IXamlTypeResolver);
			XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
			xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
			xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
			xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
			xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
			xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(_003CInitializeComponent_003E_anonXamlCDataTemplate_4).GetTypeInfo().Assembly));
			xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(69, 93)));
			label2.SetValue(fontSizeProperty2, ((IExtendedTypeConverter)fontSizeConverter2).ConvertFromInvariantString("Micro", (IServiceProvider)xamlServiceProvider2));
			label2.SetValue(View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
			label2.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			label2.Effects.Add(fixedFontSizeLabelEffect2);
			VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 71, 38);
			grid.Children.Add(label2);
			VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 69, 30);
			label3.SetValue(Grid.RowProperty, 0);
			label3.SetValue(Grid.ColumnProperty, 2);
			label3.SetValue(Grid.RowSpanProperty, 2);
			label3.SetValue(Label.TextProperty, "接続＞");
			label3.SetValue(View.HorizontalOptionsProperty, LayoutOptions.EndAndExpand);
			label3.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			label3.Effects.Add(fixedFontSizeLabelEffect3);
			VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 76, 38);
			grid.Children.Add(label3);
			VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 74, 30);
			viewCell.View = grid;
			VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 53, 26);
			return viewCell;
		}
	}

	public MeterScanView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(MeterScanView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/MeterScanView.xaml",
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
		Color color2 = Color.FromHex("#CFCFCF");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		OnPlatform<Thickness> onPlatform = new OnPlatform<Thickness>();
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		BindingExtension bindingExtension = new BindingExtension();
		BindingExtension bindingExtension2 = new BindingExtension();
		DataTemplate dataTemplate = new DataTemplate();
		ListView listView = new ListView();
		BindingExtension bindingExtension3 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Label label2 = new Label();
		Grid grid = new Grid();
		MeterScanView meterScanView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(meterScanView = this) ?? new NameScope());
		NameScope.SetNameScope(meterScanView, nameScope);
		meterScanView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 10, 10);
		resourceDictionary.Add("PageBackgroundColor", color);
		resourceDictionary.Add("LineColor", color2);
		meterScanView.SetValue(Page.TitleProperty, "測定器通信中");
		staticResourceExtension.Key = "PageBackgroundColor";
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 1];
		array[0] = meterScanView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, VisualElement.BackgroundColorProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(MeterScanView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(8, 14)));
		object obj = ((IMarkupExtension)staticResourceExtension).ProvideValue((IServiceProvider)xamlServiceProvider);
		meterScanView.BackgroundColor = (Color)obj;
		VisualDiagnostics.RegisterSourceInfo(obj, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 8, 14);
		meterScanView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 10, 10);
		onPlatform.iOS = new Thickness(0.0, 20.0, 0.0, 20.0);
		onPlatform.Android = new Thickness(0.0, 10.0, 0.0, 20.0);
		meterScanView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 30, 10);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("30"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("200"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		label.SetValue(Grid.RowProperty, 0);
		label.SetValue(Label.TextProperty, "周辺の測定器");
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 46, 18);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 44, 10);
		listView.SetValue(Grid.RowProperty, 1);
		bindingExtension.Path = "MeterList";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		listView.SetBinding(ItemsView<Cell>.ItemsSourceProperty, binding);
		bindingExtension2.Mode = BindingMode.TwoWay;
		bindingExtension2.Path = "SelectedMeterModel";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		listView.SetBinding(ListView.SelectedItemProperty, binding2);
		listView.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		listView.SetValue(VisualElement.MinimumHeightRequestProperty, 30.0);
		listView.SetValue(ListView.HasUnevenRowsProperty, true);
		listView.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		object[] array2 = new object[0 + 4];
		array2[0] = dataTemplate;
		array2[1] = listView;
		array2[2] = grid;
		array2[3] = meterScanView;
		object[] parentValues = array2;
		MeterScanView root = meterScanView;
		((IDataTemplate)dataTemplate).LoadTemplate = delegate
		{
			ColumnDefinition item = new ColumnDefinition();
			ColumnDefinition columnDefinition = new ColumnDefinition();
			ColumnDefinition columnDefinition2 = new ColumnDefinition();
			RowDefinition rowDefinition5 = new RowDefinition();
			RowDefinition rowDefinition6 = new RowDefinition();
			BindingExtension bindingExtension4 = new BindingExtension();
			FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
			Label label3 = new Label();
			BindingExtension bindingExtension5 = new BindingExtension();
			FixedFontSizeLabelEffect fixedFontSizeLabelEffect4 = new FixedFontSizeLabelEffect();
			Label label4 = new Label();
			FixedFontSizeLabelEffect fixedFontSizeLabelEffect5 = new FixedFontSizeLabelEffect();
			Label label5 = new Label();
			Grid grid2 = new Grid();
			ViewCell viewCell = new ViewCell();
			NameScope nameScope2 = new NameScope();
			NameScope.SetNameScope(viewCell, nameScope2);
			grid2.SetValue(View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
			grid2.SetValue(Grid.RowSpacingProperty, 0.0);
			((DefinitionCollection<ColumnDefinition>)grid2.GetValue(Grid.ColumnDefinitionsProperty)).Add(item);
			columnDefinition.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("10"));
			((DefinitionCollection<ColumnDefinition>)grid2.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition);
			columnDefinition2.SetValue(ColumnDefinition.WidthProperty, new GridLengthTypeConverter().ConvertFromInvariantString("60"));
			((DefinitionCollection<ColumnDefinition>)grid2.GetValue(Grid.ColumnDefinitionsProperty)).Add(columnDefinition2);
			rowDefinition5.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("30"));
			((DefinitionCollection<RowDefinition>)grid2.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition5);
			rowDefinition6.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
			((DefinitionCollection<RowDefinition>)grid2.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition6);
			label3.SetValue(Grid.RowProperty, 0);
			label3.SetValue(Grid.ColumnProperty, 0);
			bindingExtension4.Path = "Name";
			BindingBase binding4 = ((IMarkupExtension<BindingBase>)bindingExtension4).ProvideValue((IServiceProvider)null);
			label3.SetBinding(Label.TextProperty, binding4);
			BindableProperty fontSizeProperty = Label.FontSizeProperty;
			FontSizeConverter fontSizeConverter = new FontSizeConverter();
			XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
			Type typeFromHandle3 = typeof(IProvideValueTarget);
			int length;
			object[] array3 = new object[(length = parentValues.Length) + 3];
			Array.Copy(parentValues, 0, array3, 3, length);
			array3[0] = label3;
			array3[1] = grid2;
			array3[2] = viewCell;
			object service2;
			xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array3, Label.FontSizeProperty, nameScope2));
			xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
			Type typeFromHandle4 = typeof(IXamlTypeResolver);
			XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
			xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
			xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
			xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
			xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
			xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(_003CInitializeComponent_003E_anonXamlCDataTemplate_4).GetTypeInfo().Assembly));
			xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(64, 87)));
			label3.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider2));
			label3.SetValue(View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
			label3.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			label3.Effects.Add(fixedFontSizeLabelEffect3);
			VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 66, 38);
			grid2.Children.Add(label3);
			VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 64, 30);
			label4.SetValue(Grid.RowProperty, 1);
			label4.SetValue(Grid.ColumnProperty, 0);
			bindingExtension5.Path = "IdToString";
			BindingBase binding5 = ((IMarkupExtension<BindingBase>)bindingExtension5).ProvideValue((IServiceProvider)null);
			label4.SetBinding(Label.TextProperty, binding5);
			BindableProperty fontSizeProperty2 = Label.FontSizeProperty;
			FontSizeConverter fontSizeConverter2 = new FontSizeConverter();
			XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
			Type typeFromHandle5 = typeof(IProvideValueTarget);
			int length2;
			object[] array4 = new object[(length2 = parentValues.Length) + 3];
			Array.Copy(parentValues, 0, array4, 3, length2);
			array4[0] = label4;
			array4[1] = grid2;
			array4[2] = viewCell;
			object service3;
			xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array4, Label.FontSizeProperty, nameScope2));
			xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
			Type typeFromHandle6 = typeof(IXamlTypeResolver);
			XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
			xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
			xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
			xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
			xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
			xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(_003CInitializeComponent_003E_anonXamlCDataTemplate_4).GetTypeInfo().Assembly));
			xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(69, 93)));
			label4.SetValue(fontSizeProperty2, ((IExtendedTypeConverter)fontSizeConverter2).ConvertFromInvariantString("Micro", (IServiceProvider)xamlServiceProvider3));
			label4.SetValue(View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
			label4.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			label4.Effects.Add(fixedFontSizeLabelEffect4);
			VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect4, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 71, 38);
			grid2.Children.Add(label4);
			VisualDiagnostics.RegisterSourceInfo(label4, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 69, 30);
			label5.SetValue(Grid.RowProperty, 0);
			label5.SetValue(Grid.ColumnProperty, 2);
			label5.SetValue(Grid.RowSpanProperty, 2);
			label5.SetValue(Label.TextProperty, "接続＞");
			label5.SetValue(View.HorizontalOptionsProperty, LayoutOptions.EndAndExpand);
			label5.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			label5.Effects.Add(fixedFontSizeLabelEffect5);
			VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect5, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 76, 38);
			grid2.Children.Add(label5);
			VisualDiagnostics.RegisterSourceInfo(label5, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 74, 30);
			viewCell.View = grid2;
			VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 53, 26);
			return viewCell;
		};
		listView.SetValue(ItemsView<Cell>.ItemTemplateProperty, dataTemplate);
		VisualDiagnostics.RegisterSourceInfo(dataTemplate, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 51, 18);
		grid.Children.Add(listView);
		VisualDiagnostics.RegisterSourceInfo(listView, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 49, 10);
		label2.SetValue(Grid.RowProperty, 2);
		label2.SetValue(Label.TextProperty, "ユーザーNoに空きがない場合、最も古いユーザーが削除されます。");
		bindingExtension3.Path = "ScanBCMeter";
		BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		label2.SetBinding(VisualElement.IsVisibleProperty, binding3);
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 87, 18);
		grid.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 85, 10);
		meterScanView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 35, 6);
		VisualDiagnostics.RegisterSourceInfo(meterScanView, new Uri("Views/MeterScanView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(MeterScanView));
	}
}
