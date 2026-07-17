using System;
using System.CodeDom.Compiler;
using System.Reflection;
using C1.Xamarin.Forms.Chart;
using Caliburn.Micro;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Models;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/GlucoseChartView.xaml")]
public class GlucoseChartView : ContentPage
{
	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private Label Dates;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private FlexChart chart;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries BeforeBreakFastLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries AfterBreakFastLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries BeforeLunchLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries AfterLunchLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries BeforeDinnerLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries AfterDinnerLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries BedTimeLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries NightLine;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private ChartSeries DefaultValue;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private Label BeforeMealsAverage;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private Label AfterMealsAverage;

	public GlucoseChartView()
	{
		InitializeComponent();
	}

	public void ChartView(BindableCollection<DisplayGluChartDataModel> data, decimal max)
	{
		chart.ItemsSource = data;
		chart.AxisY.Max = (double)max;
	}

	public void ChartVisible(BindableCollection<ChartSettingModel> chartSettings)
	{
		if (chartSettings == null || chartSettings.Count < 1)
		{
			string[] array = "朝食前,朝食後,昼食前,昼食後,夕食前,夕食後,就寝前,深夜".Split(new char[1] { ',' });
			foreach (string setting in array)
			{
				ChartSettingModel chartSettingModel = new ChartSettingModel();
				chartSettingModel.Setting = setting;
				chartSettingModel.Active = true;
				chartSettings.Add(chartSettingModel);
			}
		}
		else
		{
			if (chartSettings == null || chartSettings.Count <= 1)
			{
				return;
			}
			foreach (ChartSettingModel chartSetting in chartSettings)
			{
				switch (chartSetting.Setting)
				{
				case "朝食前":
					if (chartSetting.Active)
					{
						BeforeBreakFastLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						BeforeBreakFastLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "朝食後":
					if (chartSetting.Active)
					{
						AfterBreakFastLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						AfterBreakFastLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "昼食前":
					if (chartSetting.Active)
					{
						BeforeLunchLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						BeforeLunchLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "昼食後":
					if (chartSetting.Active)
					{
						AfterLunchLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						AfterLunchLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "夕食前":
					if (chartSetting.Active)
					{
						BeforeDinnerLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						BeforeDinnerLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "夕食後":
					if (chartSetting.Active)
					{
						AfterDinnerLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						AfterDinnerLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "就寝前":
					if (chartSetting.Active)
					{
						BedTimeLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						BedTimeLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				case "深夜":
					if (chartSetting.Active)
					{
						NightLine.Visibility = ChartSeriesVisibilityType.Visible;
					}
					else
					{
						NightLine.Visibility = ChartSeriesVisibilityType.Hidden;
					}
					break;
				}
			}
		}
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(GlucoseChartView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/GlucoseChartView.xaml",
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
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		RowDefinition rowDefinition5 = new RowDefinition();
		RowDefinition rowDefinition6 = new RowDefinition();
		RowDefinition rowDefinition7 = new RowDefinition();
		ColumnDefinition item = new ColumnDefinition();
		ColumnDefinition item2 = new ColumnDefinition();
		ColumnDefinition item3 = new ColumnDefinition();
		ColumnDefinition item4 = new ColumnDefinition();
		ColumnDefinition item5 = new ColumnDefinition();
		ColumnDefinition item6 = new ColumnDefinition();
		BindingExtension bindingExtension = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		Grid grid = new Grid();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		BindingExtension bindingExtension2 = new BindingExtension();
		ChartStyle chartStyle = new ChartStyle();
		ChartAxis chartAxis = new ChartAxis();
		ChartStyle chartStyle2 = new ChartStyle();
		ChartAxis chartAxis2 = new ChartAxis();
		ChartOptions chartOptions = new ChartOptions();
		BindingExtension bindingExtension3 = new BindingExtension();
		ChartStyle chartStyle3 = new ChartStyle();
		ChartSeries chartSeries = new ChartSeries();
		BindingExtension bindingExtension4 = new BindingExtension();
		ChartStyle chartStyle4 = new ChartStyle();
		ChartSeries chartSeries2 = new ChartSeries();
		BindingExtension bindingExtension5 = new BindingExtension();
		ChartStyle chartStyle5 = new ChartStyle();
		ChartSeries chartSeries3 = new ChartSeries();
		BindingExtension bindingExtension6 = new BindingExtension();
		ChartStyle chartStyle6 = new ChartStyle();
		ChartSeries chartSeries4 = new ChartSeries();
		BindingExtension bindingExtension7 = new BindingExtension();
		ChartStyle chartStyle7 = new ChartStyle();
		ChartSeries chartSeries5 = new ChartSeries();
		BindingExtension bindingExtension8 = new BindingExtension();
		ChartStyle chartStyle8 = new ChartStyle();
		ChartSeries chartSeries6 = new ChartSeries();
		BindingExtension bindingExtension9 = new BindingExtension();
		ChartStyle chartStyle9 = new ChartStyle();
		ChartSeries chartSeries7 = new ChartSeries();
		BindingExtension bindingExtension10 = new BindingExtension();
		ChartStyle chartStyle10 = new ChartStyle();
		ChartSeries chartSeries8 = new ChartSeries();
		ChartStyle chartStyle11 = new ChartStyle();
		ChartSeries chartSeries9 = new ChartSeries();
		FlexChart flexChart = new FlexChart();
		Grid grid2 = new Grid();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Label label2 = new Label();
		Grid grid3 = new Grid();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect3 = new FixedFontSizeLabelEffect();
		Label label3 = new Label();
		Grid grid4 = new Grid();
		BindingExtension bindingExtension11 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect4 = new FixedFontSizeLabelEffect();
		Label label4 = new Label();
		Grid grid5 = new Grid();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect5 = new FixedFontSizeLabelEffect();
		Label label5 = new Label();
		Grid grid6 = new Grid();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect6 = new FixedFontSizeLabelEffect();
		Label label6 = new Label();
		Grid grid7 = new Grid();
		BindingExtension bindingExtension12 = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect7 = new FixedFontSizeLabelEffect();
		Label label7 = new Label();
		Grid grid8 = new Grid();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect8 = new FixedFontSizeLabelEffect();
		Label label8 = new Label();
		Grid grid9 = new Grid();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect2 = new FixedFontSizeButtonEffect();
		Button button2 = new Button();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect3 = new FixedFontSizeButtonEffect();
		Button button3 = new Button();
		Grid grid10 = new Grid();
		ScrollView scrollView = new ScrollView();
		Grid grid11 = new Grid();
		GlucoseChartView glucoseChartView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(glucoseChartView = this) ?? new NameScope());
		NameScope.SetNameScope(glucoseChartView, nameScope);
		((INameScope)nameScope).RegisterName("Dates", (object)label);
		if (label.StyleId == null)
		{
			label.StyleId = "Dates";
		}
		((INameScope)nameScope).RegisterName("chart", (object)flexChart);
		if (flexChart.StyleId == null)
		{
			flexChart.StyleId = "chart";
		}
		((INameScope)nameScope).RegisterName("BeforeBreakFastLine", (object)chartSeries);
		((INameScope)nameScope).RegisterName("AfterBreakFastLine", (object)chartSeries2);
		((INameScope)nameScope).RegisterName("BeforeLunchLine", (object)chartSeries3);
		((INameScope)nameScope).RegisterName("AfterLunchLine", (object)chartSeries4);
		((INameScope)nameScope).RegisterName("BeforeDinnerLine", (object)chartSeries5);
		((INameScope)nameScope).RegisterName("AfterDinnerLine", (object)chartSeries6);
		((INameScope)nameScope).RegisterName("BedTimeLine", (object)chartSeries7);
		((INameScope)nameScope).RegisterName("NightLine", (object)chartSeries8);
		((INameScope)nameScope).RegisterName("DefaultValue", (object)chartSeries9);
		((INameScope)nameScope).RegisterName("BeforeMealsAverage", (object)label4);
		if (label4.StyleId == null)
		{
			label4.StyleId = "BeforeMealsAverage";
		}
		((INameScope)nameScope).RegisterName("AfterMealsAverage", (object)label7);
		if (label7.StyleId == null)
		{
			label7.StyleId = "AfterMealsAverage";
		}
		Dates = label;
		chart = flexChart;
		BeforeBreakFastLine = chartSeries;
		AfterBreakFastLine = chartSeries2;
		BeforeLunchLine = chartSeries3;
		AfterLunchLine = chartSeries4;
		BeforeDinnerLine = chartSeries5;
		AfterDinnerLine = chartSeries6;
		BedTimeLine = chartSeries7;
		NightLine = chartSeries8;
		DefaultValue = chartSeries9;
		BeforeMealsAverage = label4;
		AfterMealsAverage = label7;
		glucoseChartView.SetValue(NavigationPage.HasBackButtonProperty, true);
		glucoseChartView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		glucoseChartView.SetValue(Page.BackgroundImageProperty, "bg.png");
		glucoseChartView.SetValue(Page.TitleProperty, "詳細情報");
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("50"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("300"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		rowDefinition5.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition5);
		rowDefinition6.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition6);
		rowDefinition7.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid10.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition7);
		((DefinitionCollection<ColumnDefinition>)grid10.GetValue(Grid.ColumnDefinitionsProperty)).Add(item);
		((DefinitionCollection<ColumnDefinition>)grid10.GetValue(Grid.ColumnDefinitionsProperty)).Add(item2);
		((DefinitionCollection<ColumnDefinition>)grid10.GetValue(Grid.ColumnDefinitionsProperty)).Add(item3);
		((DefinitionCollection<ColumnDefinition>)grid10.GetValue(Grid.ColumnDefinitionsProperty)).Add(item4);
		((DefinitionCollection<ColumnDefinition>)grid10.GetValue(Grid.ColumnDefinitionsProperty)).Add(item5);
		((DefinitionCollection<ColumnDefinition>)grid10.GetValue(Grid.ColumnDefinitionsProperty)).Add(item6);
		grid.SetValue(Grid.RowProperty, 0);
		grid.SetValue(Grid.ColumnSpanProperty, 5);
		bindingExtension.Path = "DisplaySelectedDates";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		label.SetBinding(Label.TextProperty, binding);
		label.SetValue(Label.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		label.SetValue(Label.TextColorProperty, Color.Blue);
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 6];
		array[0] = label;
		array[1] = grid;
		array[2] = grid10;
		array[3] = scrollView;
		array[4] = grid11;
		array[5] = glucoseChartView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, Label.FontSizeProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(38, 120)));
		label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("20", (IServiceProvider)xamlServiceProvider));
		label.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.CenterAndExpand);
		label.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.CenterAndExpand);
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 40, 30);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 38, 22);
		grid10.Children.Add(grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 37, 18);
		button.SetValue(Message.AttachProperty, "OnCalendarTapped");
		button.SetValue(Grid.RowProperty, 0);
		button.SetValue(Grid.ColumnProperty, 5);
		button.SetValue(Button.TextProperty, "選択");
		button.SetValue(Button.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		button.SetValue(Button.TextColorProperty, Color.Blue);
		BindableProperty fontSizeProperty2 = Button.FontSizeProperty;
		FontSizeConverter fontSizeConverter2 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider2 = new XamlServiceProvider();
		Type typeFromHandle3 = typeof(IProvideValueTarget);
		object[] array2 = new object[0 + 5];
		array2[0] = button;
		array2[1] = grid10;
		array2[2] = scrollView;
		array2[3] = grid11;
		array2[4] = glucoseChartView;
		object service2;
		xamlServiceProvider2.Add(typeFromHandle3, service2 = new SimpleValueTargetProvider(array2, Button.FontSizeProperty, nameScope));
		xamlServiceProvider2.Add(typeof(IReferenceProvider), service2);
		Type typeFromHandle4 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver2 = new XmlNamespaceResolver();
		xmlNamespaceResolver2.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver2.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver2.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver2.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver2.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider2.Add(typeFromHandle4, new XamlTypeResolver(xmlNamespaceResolver2, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider2.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(46, 142)));
		button.SetValue(fontSizeProperty2, ((IExtendedTypeConverter)fontSizeConverter2).ConvertFromInvariantString("12", (IServiceProvider)xamlServiceProvider2));
		button.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		button.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.CenterAndExpand);
		button.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.EndAndExpand);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 48, 26);
		grid10.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 46, 18);
		flexChart.SetValue(ChartBase.LegendPositionProperty, ChartPositionType.Bottom);
		flexChart.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		flexChart.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(3.0, 3.0, 3.0, 3.0));
		flexChart.SetValue(Grid.RowProperty, 1);
		bindingExtension2.Path = "DispGluChartData";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		flexChart.SetBinding(ChartBase.BindingProperty, binding2);
		flexChart.SetValue(FlexChart.BindingXProperty, "Date");
		flexChart.SetValue(FlexChart.ChartTypeProperty, ChartType.Scatter);
		flexChart.SetValue(Grid.ColumnSpanProperty, 6);
		flexChart.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		flexChart.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		chartAxis.SetValue(ChartAxis.MinProperty, 20.0);
		chartAxis.SetValue(ChartAxis.AxisLineProperty, true);
		chartAxis.SetValue(ChartAxis.MajorTickMarksProperty, ChartTickMarkType.Outside);
		chartAxis.SetValue(ChartAxis.MajorUnitProperty, 20.0);
		chartAxis.SetValue(ChartAxis.MajorGridProperty, true);
		chartAxis.SetValue(ChartAxis.TitleProperty, "測定値");
		chartStyle.SetValue(ChartStyle.FontSizeProperty, 13.0);
		chartAxis.SetValue(ChartAxis.StyleProperty, chartStyle);
		VisualDiagnostics.RegisterSourceInfo(chartStyle, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 57, 34);
		flexChart.AxisY = chartAxis;
		VisualDiagnostics.RegisterSourceInfo(chartAxis, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 55, 26);
		chartAxis2.SetValue(ChartAxis.AxisLineProperty, true);
		chartAxis2.SetValue(ChartAxis.TitleProperty, "測定日");
		chartAxis2.SetValue(ChartAxis.MajorGridProperty, true);
		chartStyle2.SetValue(ChartStyle.FontSizeProperty, 13.0);
		chartAxis2.SetValue(ChartAxis.StyleProperty, chartStyle2);
		VisualDiagnostics.RegisterSourceInfo(chartStyle2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 64, 34);
		flexChart.AxisX = chartAxis2;
		VisualDiagnostics.RegisterSourceInfo(chartAxis2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 62, 26);
		chartOptions.SetValue(ChartOptions.InterpolateNullsProperty, false);
		flexChart.SetValue(FlexChart.OptionsProperty, chartOptions);
		VisualDiagnostics.RegisterSourceInfo(chartOptions, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 69, 26);
		chartSeries.SetValue(ChartSeries.SeriesNameProperty, "朝食前");
		bindingExtension3.Path = "BeforeBreakFastVisible";
		BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
		chartSeries.SetBinding(ChartSeries.VisibilityProperty, binding3);
		chartSeries.SetValue(ChartSeries.BindingProperty, "BeforeBreakFast");
		chartSeries.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle3.SetValue(ChartStyle.StrokeProperty, new Color(0.6274510025978088, 0.8470588326454163, 0.9372549057006836, 1.0));
		chartStyle3.SetValue(ChartStyle.FillProperty, new Color(0.6274510025978088, 0.8470588326454163, 0.9372549057006836, 1.0));
		chartSeries.SetValue(ChartSeries.StyleProperty, chartStyle3);
		VisualDiagnostics.RegisterSourceInfo(chartStyle3, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 75, 34);
		flexChart.Series.Add(chartSeries);
		chartSeries2.SetValue(ChartSeries.SeriesNameProperty, "朝食後");
		bindingExtension4.Path = "AfterBreakFastVisible";
		BindingBase binding4 = ((IMarkupExtension<BindingBase>)bindingExtension4).ProvideValue((IServiceProvider)null);
		chartSeries2.SetBinding(ChartSeries.VisibilityProperty, binding4);
		chartSeries2.SetValue(ChartSeries.BindingProperty, "AfterBreakFast");
		chartSeries2.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle4.SetValue(ChartStyle.StrokeProperty, new Color(0.2549019753932953, 0.4117647111415863, 0.8823529481887817, 1.0));
		chartStyle4.SetValue(ChartStyle.FillProperty, new Color(0.2549019753932953, 0.4117647111415863, 0.8823529481887817, 1.0));
		chartSeries2.SetValue(ChartSeries.StyleProperty, chartStyle4);
		VisualDiagnostics.RegisterSourceInfo(chartStyle4, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 80, 34);
		flexChart.Series.Add(chartSeries2);
		chartSeries3.SetValue(ChartSeries.SeriesNameProperty, "昼食前");
		bindingExtension5.Path = "BeforeLunchVisible";
		BindingBase binding5 = ((IMarkupExtension<BindingBase>)bindingExtension5).ProvideValue((IServiceProvider)null);
		chartSeries3.SetBinding(ChartSeries.VisibilityProperty, binding5);
		chartSeries3.SetValue(ChartSeries.BindingProperty, "BeforeLunch");
		chartSeries3.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle5.SetValue(ChartStyle.StrokeProperty, new Color(0.9333333373069763, 0.47058823704719543, 0.0, 1.0));
		chartStyle5.SetValue(ChartStyle.FillProperty, new Color(0.9333333373069763, 0.47058823704719543, 0.0, 1.0));
		chartSeries3.SetValue(ChartSeries.StyleProperty, chartStyle5);
		VisualDiagnostics.RegisterSourceInfo(chartStyle5, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 85, 34);
		flexChart.Series.Add(chartSeries3);
		chartSeries4.SetValue(ChartSeries.SeriesNameProperty, "昼食後");
		bindingExtension6.Path = "AfterLunchVisible";
		BindingBase binding6 = ((IMarkupExtension<BindingBase>)bindingExtension6).ProvideValue((IServiceProvider)null);
		chartSeries4.SetBinding(ChartSeries.VisibilityProperty, binding6);
		chartSeries4.SetValue(ChartSeries.BindingProperty, "AfterLunch");
		chartSeries4.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle6.SetValue(ChartStyle.StrokeProperty, new Color(0.5490196347236633, 0.3921568691730499, 0.3137255012989044, 1.0));
		chartStyle6.SetValue(ChartStyle.FillProperty, new Color(0.5490196347236633, 0.3921568691730499, 0.3137255012989044, 1.0));
		chartSeries4.SetValue(ChartSeries.StyleProperty, chartStyle6);
		VisualDiagnostics.RegisterSourceInfo(chartStyle6, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 90, 34);
		flexChart.Series.Add(chartSeries4);
		chartSeries5.SetValue(ChartSeries.SeriesNameProperty, "夕食前");
		bindingExtension7.Path = "BeforeDinnerVisible";
		BindingBase binding7 = ((IMarkupExtension<BindingBase>)bindingExtension7).ProvideValue((IServiceProvider)null);
		chartSeries5.SetBinding(ChartSeries.VisibilityProperty, binding7);
		chartSeries5.SetValue(ChartSeries.BindingProperty, "BeforeDinner");
		chartSeries5.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle7.SetValue(ChartStyle.StrokeProperty, new Color(0.7647058963775635, 0.5686274766921997, 0.26274511218070984, 1.0));
		chartStyle7.SetValue(ChartStyle.FillProperty, new Color(0.7647058963775635, 0.5686274766921997, 0.26274511218070984, 1.0));
		chartSeries5.SetValue(ChartSeries.StyleProperty, chartStyle7);
		VisualDiagnostics.RegisterSourceInfo(chartStyle7, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 95, 34);
		flexChart.Series.Add(chartSeries5);
		chartSeries6.SetValue(ChartSeries.SeriesNameProperty, "夕食後");
		bindingExtension8.Path = "AfterDinnerVisible";
		BindingBase binding8 = ((IMarkupExtension<BindingBase>)bindingExtension8).ProvideValue((IServiceProvider)null);
		chartSeries6.SetBinding(ChartSeries.VisibilityProperty, binding8);
		chartSeries6.SetValue(ChartSeries.BindingProperty, "AfterDinner");
		chartSeries6.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle8.SetValue(ChartStyle.StrokeProperty, new Color(0.4156862795352936, 0.7098039388656616, 0.27843138575553894, 1.0));
		chartStyle8.SetValue(ChartStyle.FillProperty, new Color(0.4156862795352936, 0.7098039388656616, 0.27843138575553894, 1.0));
		chartSeries6.SetValue(ChartSeries.StyleProperty, chartStyle8);
		VisualDiagnostics.RegisterSourceInfo(chartStyle8, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 100, 34);
		flexChart.Series.Add(chartSeries6);
		chartSeries7.SetValue(ChartSeries.SeriesNameProperty, "就寝前");
		bindingExtension9.Path = "BedTimeVisible";
		BindingBase binding9 = ((IMarkupExtension<BindingBase>)bindingExtension9).ProvideValue((IServiceProvider)null);
		chartSeries7.SetBinding(ChartSeries.VisibilityProperty, binding9);
		chartSeries7.SetValue(ChartSeries.BindingProperty, "BedTime");
		chartSeries7.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle9.SetValue(ChartStyle.StrokeProperty, new Color(0.9019607901573181, 0.0, 0.20000000298023224, 1.0));
		chartStyle9.SetValue(ChartStyle.FillProperty, new Color(0.9019607901573181, 0.0, 0.20000000298023224, 1.0));
		chartSeries7.SetValue(ChartSeries.StyleProperty, chartStyle9);
		VisualDiagnostics.RegisterSourceInfo(chartStyle9, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 105, 34);
		flexChart.Series.Add(chartSeries7);
		chartSeries8.SetValue(ChartSeries.SeriesNameProperty, "深夜");
		bindingExtension10.Path = "NightVisible";
		BindingBase binding10 = ((IMarkupExtension<BindingBase>)bindingExtension10).ProvideValue((IServiceProvider)null);
		chartSeries8.SetBinding(ChartSeries.VisibilityProperty, binding10);
		chartSeries8.SetValue(ChartSeries.BindingProperty, "Night");
		chartSeries8.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle10.SetValue(ChartStyle.StrokeProperty, new Color(0.9686274528503418, 0.6509804129600525, 0.9490196108818054, 1.0));
		chartStyle10.SetValue(ChartStyle.FillProperty, new Color(0.9686274528503418, 0.6509804129600525, 0.9490196108818054, 1.0));
		chartSeries8.SetValue(ChartSeries.StyleProperty, chartStyle10);
		VisualDiagnostics.RegisterSourceInfo(chartStyle10, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 110, 34);
		flexChart.Series.Add(chartSeries8);
		chartSeries9.SetValue(ChartSeries.BindingProperty, "DefaultValue");
		chartSeries9.SetValue(ChartSeries.SymbolSizeProperty, 10);
		chartStyle11.SetValue(ChartStyle.StrokeProperty, Color.Transparent);
		chartStyle11.SetValue(ChartStyle.FillProperty, Color.Transparent);
		chartSeries9.SetValue(ChartSeries.StyleProperty, chartStyle11);
		VisualDiagnostics.RegisterSourceInfo(chartStyle11, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 115, 34);
		flexChart.Series.Add(chartSeries9);
		grid10.Children.Add(flexChart);
		VisualDiagnostics.RegisterSourceInfo(flexChart, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 53, 18);
		grid2.SetValue(Grid.RowProperty, 3);
		grid2.SetValue(Grid.RowSpanProperty, 2);
		grid2.SetValue(Grid.ColumnSpanProperty, 6);
		grid2.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		grid2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(3.0, 3.0, 3.0, 3.0));
		grid10.Children.Add(grid2);
		VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 121, 18);
		grid3.SetValue(Grid.RowProperty, 2);
		grid3.SetValue(Grid.ColumnSpanProperty, 6);
		grid3.SetValue(Grid.RowSpacingProperty, 0.0);
		grid3.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		grid3.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(3.0, 3.0, 3.0, 3.0));
		label2.SetValue(Label.TextProperty, " 平均値");
		label2.SetValue(Label.TextColorProperty, Color.Blue);
		label2.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.End);
		label2.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.CenterAndExpand);
		BindableProperty fontSizeProperty3 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter3 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider3 = new XamlServiceProvider();
		Type typeFromHandle5 = typeof(IProvideValueTarget);
		object[] array3 = new object[0 + 6];
		array3[0] = label2;
		array3[1] = grid3;
		array3[2] = grid10;
		array3[3] = scrollView;
		array3[4] = grid11;
		array3[5] = glucoseChartView;
		object service3;
		xamlServiceProvider3.Add(typeFromHandle5, service3 = new SimpleValueTargetProvider(array3, Label.FontSizeProperty, nameScope));
		xamlServiceProvider3.Add(typeof(IReferenceProvider), service3);
		Type typeFromHandle6 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver3 = new XmlNamespaceResolver();
		xmlNamespaceResolver3.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver3.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver3.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver3.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver3.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider3.Add(typeFromHandle6, new XamlTypeResolver(xmlNamespaceResolver3, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider3.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(125, 115)));
		label2.SetValue(fontSizeProperty3, ((IExtendedTypeConverter)fontSizeConverter3).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider3));
		label2.SetValue(Label.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		label2.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 127, 30);
		grid3.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 125, 22);
		grid10.Children.Add(grid3);
		VisualDiagnostics.RegisterSourceInfo(grid3, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 124, 18);
		grid4.SetValue(Grid.RowProperty, 3);
		grid4.SetValue(Grid.ColumnSpanProperty, 3);
		grid4.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		grid4.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(15.0, 0.0, 0.0, 0.0));
		label3.SetValue(Label.TextProperty, "食前/眠前");
		label3.SetValue(Label.TextColorProperty, Color.Blue);
		label3.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.Center);
		label3.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.StartAndExpand);
		BindableProperty fontSizeProperty4 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter4 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider4 = new XamlServiceProvider();
		Type typeFromHandle7 = typeof(IProvideValueTarget);
		object[] array4 = new object[0 + 6];
		array4[0] = label3;
		array4[1] = grid4;
		array4[2] = grid10;
		array4[3] = scrollView;
		array4[4] = grid11;
		array4[5] = glucoseChartView;
		object service4;
		xamlServiceProvider4.Add(typeFromHandle7, service4 = new SimpleValueTargetProvider(array4, Label.FontSizeProperty, nameScope));
		xamlServiceProvider4.Add(typeof(IReferenceProvider), service4);
		Type typeFromHandle8 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver4 = new XmlNamespaceResolver();
		xmlNamespaceResolver4.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver4.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver4.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver4.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver4.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider4.Add(typeFromHandle8, new XamlTypeResolver(xmlNamespaceResolver4, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider4.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(134, 118)));
		label3.SetValue(fontSizeProperty4, ((IExtendedTypeConverter)fontSizeConverter4).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider4));
		label3.Effects.Add(fixedFontSizeLabelEffect3);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect3, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 136, 30);
		grid4.Children.Add(label3);
		VisualDiagnostics.RegisterSourceInfo(label3, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 134, 22);
		grid10.Children.Add(grid4);
		VisualDiagnostics.RegisterSourceInfo(grid4, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 133, 18);
		grid5.SetValue(Grid.RowProperty, 3);
		grid5.SetValue(Grid.ColumnProperty, 3);
		grid5.SetValue(Grid.ColumnSpanProperty, 2);
		grid5.SetValue(Grid.ColumnSpacingProperty, 0.0);
		grid5.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		bindingExtension11.Path = "BeforeMealsAverage";
		BindingBase binding11 = ((IMarkupExtension<BindingBase>)bindingExtension11).ProvideValue((IServiceProvider)null);
		label4.SetBinding(Label.TextProperty, binding11);
		label4.SetValue(Label.TextColorProperty, Color.Blue);
		label4.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.EndAndExpand);
		label4.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.End);
		BindableProperty fontSizeProperty5 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter5 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider5 = new XamlServiceProvider();
		Type typeFromHandle9 = typeof(IProvideValueTarget);
		object[] array5 = new object[0 + 6];
		array5[0] = label4;
		array5[1] = grid5;
		array5[2] = grid10;
		array5[3] = scrollView;
		array5[4] = grid11;
		array5[5] = glucoseChartView;
		object service5;
		xamlServiceProvider5.Add(typeFromHandle9, service5 = new SimpleValueTargetProvider(array5, Label.FontSizeProperty, nameScope));
		xamlServiceProvider5.Add(typeof(IReferenceProvider), service5);
		Type typeFromHandle10 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver5 = new XmlNamespaceResolver();
		xmlNamespaceResolver5.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver5.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver5.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver5.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver5.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider5.Add(typeFromHandle10, new XamlTypeResolver(xmlNamespaceResolver5, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider5.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(141, 164)));
		label4.SetValue(fontSizeProperty5, ((IExtendedTypeConverter)fontSizeConverter5).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider5));
		label4.SetValue(Label.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		label4.Effects.Add(fixedFontSizeLabelEffect4);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect4, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 143, 30);
		grid5.Children.Add(label4);
		VisualDiagnostics.RegisterSourceInfo(label4, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 141, 22);
		grid10.Children.Add(grid5);
		VisualDiagnostics.RegisterSourceInfo(grid5, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 140, 18);
		grid6.SetValue(Grid.RowProperty, 3);
		grid6.SetValue(Grid.ColumnProperty, 5);
		grid6.SetValue(Grid.ColumnSpacingProperty, 0.0);
		grid6.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		grid6.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0, 0.0, 5.0, 0.0));
		label5.SetValue(Label.TextProperty, "mg/dl");
		label5.SetValue(Label.TextColorProperty, Color.Black);
		label5.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.EndAndExpand);
		label5.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Start);
		BindableProperty fontSizeProperty6 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter6 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider6 = new XamlServiceProvider();
		Type typeFromHandle11 = typeof(IProvideValueTarget);
		object[] array6 = new object[0 + 6];
		array6[0] = label5;
		array6[1] = grid6;
		array6[2] = grid10;
		array6[3] = scrollView;
		array6[4] = grid11;
		array6[5] = glucoseChartView;
		object service6;
		xamlServiceProvider6.Add(typeFromHandle11, service6 = new SimpleValueTargetProvider(array6, Label.FontSizeProperty, nameScope));
		xamlServiceProvider6.Add(typeof(IReferenceProvider), service6);
		Type typeFromHandle12 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver6 = new XmlNamespaceResolver();
		xmlNamespaceResolver6.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver6.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver6.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver6.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver6.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider6.Add(typeFromHandle12, new XamlTypeResolver(xmlNamespaceResolver6, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider6.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(148, 116)));
		label5.SetValue(fontSizeProperty6, ((IExtendedTypeConverter)fontSizeConverter6).ConvertFromInvariantString("Default", (IServiceProvider)xamlServiceProvider6));
		label5.SetValue(Label.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		label5.Effects.Add(fixedFontSizeLabelEffect5);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect5, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 150, 30);
		grid6.Children.Add(label5);
		VisualDiagnostics.RegisterSourceInfo(label5, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 148, 22);
		grid10.Children.Add(grid6);
		VisualDiagnostics.RegisterSourceInfo(grid6, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 147, 18);
		grid7.SetValue(Grid.RowProperty, 4);
		grid7.SetValue(Grid.ColumnSpanProperty, 3);
		grid7.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		grid7.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(15.0, 0.0, 0.0, 5.0));
		label6.SetValue(Label.TextProperty, "食後");
		label6.SetValue(Label.TextColorProperty, Color.Blue);
		label6.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.Center);
		label6.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.StartAndExpand);
		BindableProperty fontSizeProperty7 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter7 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider7 = new XamlServiceProvider();
		Type typeFromHandle13 = typeof(IProvideValueTarget);
		object[] array7 = new object[0 + 6];
		array7[0] = label6;
		array7[1] = grid7;
		array7[2] = grid10;
		array7[3] = scrollView;
		array7[4] = grid11;
		array7[5] = glucoseChartView;
		object service7;
		xamlServiceProvider7.Add(typeFromHandle13, service7 = new SimpleValueTargetProvider(array7, Label.FontSizeProperty, nameScope));
		xamlServiceProvider7.Add(typeof(IReferenceProvider), service7);
		Type typeFromHandle14 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver7 = new XmlNamespaceResolver();
		xmlNamespaceResolver7.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver7.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver7.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver7.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver7.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider7.Add(typeFromHandle14, new XamlTypeResolver(xmlNamespaceResolver7, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider7.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(157, 115)));
		label6.SetValue(fontSizeProperty7, ((IExtendedTypeConverter)fontSizeConverter7).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider7));
		label6.Effects.Add(fixedFontSizeLabelEffect6);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect6, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 159, 30);
		grid7.Children.Add(label6);
		VisualDiagnostics.RegisterSourceInfo(label6, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 157, 22);
		grid10.Children.Add(grid7);
		VisualDiagnostics.RegisterSourceInfo(grid7, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 156, 18);
		grid8.SetValue(Grid.RowProperty, 4);
		grid8.SetValue(Grid.ColumnProperty, 3);
		grid8.SetValue(Grid.ColumnSpanProperty, 2);
		grid8.SetValue(Grid.ColumnSpacingProperty, 0.0);
		grid8.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		bindingExtension12.Path = "AfterMealsAverage";
		BindingBase binding12 = ((IMarkupExtension<BindingBase>)bindingExtension12).ProvideValue((IServiceProvider)null);
		label7.SetBinding(Label.TextProperty, binding12);
		label7.SetValue(Label.TextColorProperty, Color.Blue);
		label7.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.EndAndExpand);
		label7.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.End);
		BindableProperty fontSizeProperty8 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter8 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider8 = new XamlServiceProvider();
		Type typeFromHandle15 = typeof(IProvideValueTarget);
		object[] array8 = new object[0 + 6];
		array8[0] = label7;
		array8[1] = grid8;
		array8[2] = grid10;
		array8[3] = scrollView;
		array8[4] = grid11;
		array8[5] = glucoseChartView;
		object service8;
		xamlServiceProvider8.Add(typeFromHandle15, service8 = new SimpleValueTargetProvider(array8, Label.FontSizeProperty, nameScope));
		xamlServiceProvider8.Add(typeof(IReferenceProvider), service8);
		Type typeFromHandle16 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver8 = new XmlNamespaceResolver();
		xmlNamespaceResolver8.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver8.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver8.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver8.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver8.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider8.Add(typeFromHandle16, new XamlTypeResolver(xmlNamespaceResolver8, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider8.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(164, 162)));
		label7.SetValue(fontSizeProperty8, ((IExtendedTypeConverter)fontSizeConverter8).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider8));
		label7.SetValue(Label.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		label7.Effects.Add(fixedFontSizeLabelEffect7);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect7, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 166, 30);
		grid8.Children.Add(label7);
		VisualDiagnostics.RegisterSourceInfo(label7, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 164, 22);
		grid10.Children.Add(grid8);
		VisualDiagnostics.RegisterSourceInfo(grid8, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 163, 18);
		grid9.SetValue(Grid.RowProperty, 4);
		grid9.SetValue(Grid.ColumnProperty, 5);
		grid9.SetValue(Grid.ColumnSpacingProperty, 0.0);
		grid9.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		grid9.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(0.0, 0.0, 5.0, 5.0));
		label8.SetValue(Label.TextProperty, "mg/dl");
		label8.SetValue(Label.TextColorProperty, Color.Black);
		label8.SetValue(Xamarin.Forms.View.VerticalOptionsProperty, LayoutOptions.EndAndExpand);
		label8.SetValue(Xamarin.Forms.View.HorizontalOptionsProperty, LayoutOptions.Start);
		BindableProperty fontSizeProperty9 = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter9 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider9 = new XamlServiceProvider();
		Type typeFromHandle17 = typeof(IProvideValueTarget);
		object[] array9 = new object[0 + 6];
		array9[0] = label8;
		array9[1] = grid9;
		array9[2] = grid10;
		array9[3] = scrollView;
		array9[4] = grid11;
		array9[5] = glucoseChartView;
		object service9;
		xamlServiceProvider9.Add(typeFromHandle17, service9 = new SimpleValueTargetProvider(array9, Label.FontSizeProperty, nameScope));
		xamlServiceProvider9.Add(typeof(IReferenceProvider), service9);
		Type typeFromHandle18 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver9 = new XmlNamespaceResolver();
		xmlNamespaceResolver9.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver9.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver9.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver9.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver9.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider9.Add(typeFromHandle18, new XamlTypeResolver(xmlNamespaceResolver9, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider9.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(171, 116)));
		label8.SetValue(fontSizeProperty9, ((IExtendedTypeConverter)fontSizeConverter9).ConvertFromInvariantString("Default", (IServiceProvider)xamlServiceProvider9));
		label8.SetValue(Label.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		label8.Effects.Add(fixedFontSizeLabelEffect8);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect8, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 173, 30);
		grid9.Children.Add(label8);
		VisualDiagnostics.RegisterSourceInfo(label8, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 171, 22);
		grid10.Children.Add(grid9);
		VisualDiagnostics.RegisterSourceInfo(grid9, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 170, 18);
		button2.SetValue(Message.AttachProperty, "OnGluSettingTapped");
		button2.SetValue(Grid.RowProperty, 5);
		button2.SetValue(Grid.ColumnSpanProperty, 6);
		button2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(5.0, 5.0, 5.0, 5.0));
		button2.SetValue(Button.BorderColorProperty, Color.Black);
		BindableProperty fontSizeProperty10 = Button.FontSizeProperty;
		FontSizeConverter fontSizeConverter10 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider10 = new XamlServiceProvider();
		Type typeFromHandle19 = typeof(IProvideValueTarget);
		object[] array10 = new object[0 + 5];
		array10[0] = button2;
		array10[1] = grid10;
		array10[2] = scrollView;
		array10[3] = grid11;
		array10[4] = glucoseChartView;
		object service10;
		xamlServiceProvider10.Add(typeFromHandle19, service10 = new SimpleValueTargetProvider(array10, Button.FontSizeProperty, nameScope));
		xamlServiceProvider10.Add(typeof(IReferenceProvider), service10);
		Type typeFromHandle20 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver10 = new XmlNamespaceResolver();
		xmlNamespaceResolver10.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver10.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver10.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver10.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver10.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider10.Add(typeFromHandle20, new XamlTypeResolver(xmlNamespaceResolver10, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider10.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(179, 136)));
		button2.SetValue(fontSizeProperty10, ((IExtendedTypeConverter)fontSizeConverter10).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider10));
		button2.SetValue(Button.BorderWidthProperty, 2.0);
		button2.SetValue(Button.TextProperty, "グラフ表示項目設定");
		button2.SetValue(Button.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		button2.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		button2.Effects.Add(fixedFontSizeButtonEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 181, 26);
		grid10.Children.Add(button2);
		VisualDiagnostics.RegisterSourceInfo(button2, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 179, 18);
		button3.SetValue(Grid.RowProperty, 6);
		button3.SetValue(Grid.ColumnSpanProperty, 6);
		button3.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(5.0, 5.0, 5.0, 5.0));
		button3.SetValue(Button.BorderColorProperty, Color.Black);
		BindableProperty fontSizeProperty11 = Button.FontSizeProperty;
		FontSizeConverter fontSizeConverter11 = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider11 = new XamlServiceProvider();
		Type typeFromHandle21 = typeof(IProvideValueTarget);
		object[] array11 = new object[0 + 5];
		array11[0] = button3;
		array11[1] = grid10;
		array11[2] = scrollView;
		array11[3] = grid11;
		array11[4] = glucoseChartView;
		object service11;
		xamlServiceProvider11.Add(typeFromHandle21, service11 = new SimpleValueTargetProvider(array11, Button.FontSizeProperty, nameScope));
		xamlServiceProvider11.Add(typeof(IReferenceProvider), service11);
		Type typeFromHandle22 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver11 = new XmlNamespaceResolver();
		xmlNamespaceResolver11.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver11.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver11.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver11.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver11.Add("c1", "clr-namespace:C1.Xamarin.Forms.Chart;assembly=C1.Xamarin.Forms.Chart");
		xamlServiceProvider11.Add(typeFromHandle22, new XamlTypeResolver(xmlNamespaceResolver11, typeof(GlucoseChartView).GetTypeInfo().Assembly));
		xamlServiceProvider11.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(186, 95)));
		button3.SetValue(fontSizeProperty11, ((IExtendedTypeConverter)fontSizeConverter11).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider11));
		button3.SetValue(Button.BorderWidthProperty, 2.0);
		button3.SetValue(Message.AttachProperty, "OnGluDataTapped");
		button3.SetValue(Button.TextProperty, "データ表");
		button3.SetValue(Button.FontAttributesProperty, new FontAttributesConverter().ConvertFromInvariantString("Bold"));
		button3.SetValue(VisualElement.BackgroundColorProperty, Color.White);
		button3.Effects.Add(fixedFontSizeButtonEffect3);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect3, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 188, 26);
		grid10.Children.Add(button3);
		VisualDiagnostics.RegisterSourceInfo(button3, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 186, 18);
		scrollView.Content = grid10;
		VisualDiagnostics.RegisterSourceInfo(grid10, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 16, 14);
		grid11.Children.Add(scrollView);
		VisualDiagnostics.RegisterSourceInfo(scrollView, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 15, 10);
		glucoseChartView.SetValue(ContentPage.ContentProperty, grid11);
		VisualDiagnostics.RegisterSourceInfo(grid11, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 14, 6);
		VisualDiagnostics.RegisterSourceInfo(glucoseChartView, new Uri("Views/GlucoseChartView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(GlucoseChartView));
		Dates = this.FindByName<Label>("Dates");
		chart = this.FindByName<FlexChart>("chart");
		BeforeBreakFastLine = this.FindByName<ChartSeries>("BeforeBreakFastLine");
		AfterBreakFastLine = this.FindByName<ChartSeries>("AfterBreakFastLine");
		BeforeLunchLine = this.FindByName<ChartSeries>("BeforeLunchLine");
		AfterLunchLine = this.FindByName<ChartSeries>("AfterLunchLine");
		BeforeDinnerLine = this.FindByName<ChartSeries>("BeforeDinnerLine");
		AfterDinnerLine = this.FindByName<ChartSeries>("AfterDinnerLine");
		BedTimeLine = this.FindByName<ChartSeries>("BedTimeLine");
		NightLine = this.FindByName<ChartSeries>("NightLine");
		DefaultValue = this.FindByName<ChartSeries>("DefaultValue");
		BeforeMealsAverage = this.FindByName<Label>("BeforeMealsAverage");
		AfterMealsAverage = this.FindByName<Label>("AfterMealsAverage");
	}
}
