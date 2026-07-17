using System;
using System.CodeDom.Compiler;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using C1.Xamarin.Forms.Calendar;
using Caliburn.Micro;
using NHL.Models;
using NHL.Models.Entity;
using NHL.ViewModels.Contexts;
using NHL.ViewModels.Event;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/MeasurementCalendarView.xaml")]
public class MeasurementCalendarView : ContentPage
{
	[CompilerGenerated]
	private sealed class _003CInitializeComponent_003E_anonXamlCDataTemplate_7
	{
		internal object[] parentValues;

		internal MeasurementCalendarView root;

		internal object LoadDataTemplate()
		{
			RowDefinition item = new RowDefinition();
			RowDefinition item2 = new RowDefinition();
			RowDefinition item3 = new RowDefinition();
			BindingExtension bindingExtension = new BindingExtension();
			Label label = new Label();
			BindingExtension bindingExtension2 = new BindingExtension();
			Grid grid = new Grid();
			BindingExtension bindingExtension3 = new BindingExtension();
			Grid grid2 = new Grid();
			BindingExtension bindingExtension4 = new BindingExtension();
			Grid grid3 = new Grid();
			BindingExtension bindingExtension5 = new BindingExtension();
			Grid grid4 = new Grid();
			BindingExtension bindingExtension6 = new BindingExtension();
			Grid grid5 = new Grid();
			StackLayout stackLayout = new StackLayout();
			Grid grid6 = new Grid();
			NameScope nameScope = new NameScope();
			NameScope.SetNameScope(grid6, nameScope);
			((DefinitionCollection<RowDefinition>)grid6.GetValue(Grid.RowDefinitionsProperty)).Add(item);
			((DefinitionCollection<RowDefinition>)grid6.GetValue(Grid.RowDefinitionsProperty)).Add(item2);
			((DefinitionCollection<RowDefinition>)grid6.GetValue(Grid.RowDefinitionsProperty)).Add(item3);
			label.SetValue(Grid.RowProperty, 1);
			bindingExtension.Path = "Day";
			BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
			label.SetBinding(Label.TextProperty, binding);
			BindableProperty fontSizeProperty = Label.FontSizeProperty;
			FontSizeConverter fontSizeConverter = new FontSizeConverter();
			XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
			Type typeFromHandle = typeof(IProvideValueTarget);
			int length;
			object[] array = new object[(length = parentValues.Length) + 2];
			Array.Copy(parentValues, 0, array, 2, length);
			array[0] = label;
			array[1] = grid6;
			object service;
			xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, Label.FontSizeProperty, nameScope));
			xamlServiceProvider.Add(typeof(IReferenceProvider), service);
			Type typeFromHandle2 = typeof(IXamlTypeResolver);
			XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
			xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
			xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
			xmlNamespaceResolver.Add("c1", "clr-namespace:C1.Xamarin.Forms.Calendar;assembly=C1.Xamarin.Forms.Calendar");
			xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(_003CInitializeComponent_003E_anonXamlCDataTemplate_7).GetTypeInfo().Assembly));
			xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(36, 62)));
			label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("13", (IServiceProvider)xamlServiceProvider));
			label.SetValue(View.HorizontalOptionsProperty, LayoutOptions.Center);
			label.SetValue(View.VerticalOptionsProperty, LayoutOptions.Center);
			label.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			grid6.Children.Add(label);
			VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 36, 22);
			stackLayout.SetValue(Grid.RowProperty, 2);
			stackLayout.SetValue(View.HorizontalOptionsProperty, LayoutOptions.Center);
			stackLayout.SetValue(View.VerticalOptionsProperty, LayoutOptions.StartAndExpand);
			stackLayout.SetValue(StackLayout.OrientationProperty, StackOrientation.Horizontal);
			stackLayout.SetValue(StackLayout.SpacingProperty, 2.0);
			grid.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid.SetValue(VisualElement.BackgroundColorProperty, Color.Red);
			bindingExtension2.Path = "GlucoseDotVisible";
			BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
			grid.SetBinding(VisualElement.IsVisibleProperty, binding2);
			stackLayout.Children.Add(grid);
			VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 38, 26);
			grid2.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid2.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid2.SetValue(VisualElement.BackgroundColorProperty, Color.Orange);
			bindingExtension3.Path = "SphygmomanometerDotVisible";
			BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
			grid2.SetBinding(VisualElement.IsVisibleProperty, binding3);
			stackLayout.Children.Add(grid2);
			VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 40, 26);
			grid3.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid3.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid3.SetValue(VisualElement.BackgroundColorProperty, Color.Blue);
			bindingExtension4.Path = "TemperatureDotVisible";
			BindingBase binding4 = ((IMarkupExtension<BindingBase>)bindingExtension4).ProvideValue((IServiceProvider)null);
			grid3.SetBinding(VisualElement.IsVisibleProperty, binding4);
			stackLayout.Children.Add(grid3);
			VisualDiagnostics.RegisterSourceInfo(grid3, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 42, 26);
			grid4.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid4.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid4.SetValue(VisualElement.BackgroundColorProperty, Color.Green);
			bindingExtension5.Path = "CompositionMeterDotVisible";
			BindingBase binding5 = ((IMarkupExtension<BindingBase>)bindingExtension5).ProvideValue((IServiceProvider)null);
			grid4.SetBinding(VisualElement.IsVisibleProperty, binding5);
			stackLayout.Children.Add(grid4);
			VisualDiagnostics.RegisterSourceInfo(grid4, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 44, 26);
			grid5.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid5.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid5.SetValue(VisualElement.BackgroundColorProperty, Color.Brown);
			bindingExtension6.Path = "StepMeterDotVisible";
			BindingBase binding6 = ((IMarkupExtension<BindingBase>)bindingExtension6).ProvideValue((IServiceProvider)null);
			grid5.SetBinding(VisualElement.IsVisibleProperty, binding6);
			stackLayout.Children.Add(grid5);
			VisualDiagnostics.RegisterSourceInfo(grid5, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 46, 26);
			grid6.Children.Add(stackLayout);
			VisualDiagnostics.RegisterSourceInfo(stackLayout, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 37, 22);
			return grid6;
		}
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private C1Calendar calendar;

	public DotVisible DotVisible { get; set; }

	public MeasurementCalendarView()
	{
		InitializeComponent();
		MeasurementContext measurementContext = IoC.Get<MeasurementContext>();
		calendar.DisplayDate = measurementContext.CalendarDisplayDate;
		DotVisible = new DotVisible();
	}

	public async void OnSelectionChanged(object sender, CalendarSelectionChangedEventArgs e)
	{
		DateTime dateTime = e.SelectedDates.First();
		if (dateTime <= DateTime.Now)
		{
			IoC.Get<IEventAggregator>().PublishOnUIThread(new SelectedDateEvent
			{
				SelectDate = dateTime
			});
			await base.Navigation.PopAsync();
		}
		else
		{
			calendar.Refresh();
		}
	}

	public void OnDaySlotLoading(object sender, CalendarDaySlotLoadingEventArgs e)
	{
		DotVisible.Day = e.Date.Day;
		DotVisible.GlucoseDotVisible = false;
		DotVisible.TemperatureDotVisible = false;
		DotVisible.SphygmomanometerDotVisible = false;
		DotVisible.CompositionMeterDotVisible = false;
		DotVisible.StepMeterDotVisible = false;
		MeasurementContext measurementContext = IoC.Get<MeasurementContext>();
		if (e.Date > DateTime.Now)
		{
			e.DaySlot.BackgroundColor = Color.FromHex("#bebebe");
		}
		else if (e.Date <= DateTime.Now)
		{
			if (e.Date == DateTime.Now.Date)
			{
				e.DaySlot.BackgroundColor = Color.FromHex("#f5deb3");
			}
			if (measurementContext.GluMeasurementList?.FirstOrDefault(delegate(Measurement x)
			{
				DateTime? dateTime = x.TimezoneDate?.Date;
				DateTime date = e.Date.Date;
				if (!dateTime.HasValue)
				{
					return false;
				}
				return !dateTime.HasValue || dateTime.GetValueOrDefault() == date;
			}) != null)
			{
				DotVisible.GlucoseDotVisible = true;
			}
			if (measurementContext.TempMeasurementList?.FirstOrDefault(delegate(Measurement x)
			{
				DateTime? dateTime = x.TimezoneDate?.Date;
				DateTime date = e.Date.Date;
				if (!dateTime.HasValue)
				{
					return false;
				}
				return !dateTime.HasValue || dateTime.GetValueOrDefault() == date;
			}) != null)
			{
				DotVisible.TemperatureDotVisible = true;
			}
			if (measurementContext.PressureMeasurementList?.FirstOrDefault(delegate(Measurement x)
			{
				DateTime? dateTime = x.TimezoneDate?.Date;
				DateTime date = e.Date.Date;
				if (!dateTime.HasValue)
				{
					return false;
				}
				return !dateTime.HasValue || dateTime.GetValueOrDefault() == date;
			}) != null)
			{
				DotVisible.SphygmomanometerDotVisible = true;
			}
			if (measurementContext.CompMeasurementList?.FirstOrDefault(delegate(Measurement x)
			{
				DateTime? dateTime = x.TimezoneDate?.Date;
				DateTime date = e.Date.Date;
				if (!dateTime.HasValue)
				{
					return false;
				}
				return !dateTime.HasValue || dateTime.GetValueOrDefault() == date;
			}) != null)
			{
				DotVisible.CompositionMeterDotVisible = true;
			}
			if (measurementContext.StepMeasurementList?.FirstOrDefault(delegate(Measurement x)
			{
				DateTime? dateTime = x.TimezoneDate?.Date;
				DateTime date = e.Date.Date;
				if (!dateTime.HasValue)
				{
					return false;
				}
				return !dateTime.HasValue || dateTime.GetValueOrDefault() == date;
			}) != null)
			{
				DotVisible.StepMeterDotVisible = true;
			}
		}
		e.DaySlot.BindingContext = DotVisible;
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(MeasurementCalendarView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/MeasurementCalendarView.xaml",
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
		DataTemplate dataTemplate = new DataTemplate();
		C1Calendar c1Calendar = new C1Calendar();
		MeasurementCalendarView measurementCalendarView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(measurementCalendarView = this) ?? new NameScope());
		NameScope.SetNameScope(measurementCalendarView, nameScope);
		((INameScope)nameScope).RegisterName("calendar", (object)c1Calendar);
		if (c1Calendar.StyleId == null)
		{
			c1Calendar.StyleId = "calendar";
		}
		calendar = c1Calendar;
		measurementCalendarView.SetValue(NavigationPage.HasBackButtonProperty, true);
		measurementCalendarView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		measurementCalendarView.SetValue(Page.TitleProperty, "測定日選択");
		c1Calendar.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		c1Calendar.SetValue(C1Calendar.FontSizeProperty, 12.0);
		c1Calendar.SetValue(C1Calendar.BorderColorProperty, Color.Black);
		c1Calendar.SetValue(C1Calendar.FontFamilyProperty, "Meiryo");
		c1Calendar.SetValue(C1Calendar.DayBorderColorProperty, Color.Black);
		c1Calendar.SetValue(C1Calendar.DayOfWeekFormatProperty, "d");
		c1Calendar.SetValue(C1Calendar.DayOfWeekFontSizeProperty, 21.0);
		c1Calendar.SetValue(C1Calendar.HeaderBackgroundColorProperty, Color.Blue);
		c1Calendar.SetValue(C1Calendar.HeaderFontSizeProperty, 16.0);
		c1Calendar.SetValue(C1Calendar.HeaderTextColorProperty, Color.White);
		c1Calendar.SetValue(C1Calendar.DayOfWeekBackgroundColorProperty, new Color(0.9176470637321472, 0.9333333373069763, 0.9529411792755127, 1.0));
		c1Calendar.SetValue(C1Calendar.TextColorProperty, Color.Black);
		c1Calendar.SetValue(C1Calendar.MaxSelectionCountProperty, 1);
		c1Calendar.SelectionChanged += measurementCalendarView.OnSelectionChanged;
		c1Calendar.DaySlotLoading += measurementCalendarView.OnDaySlotLoading;
		object[] array = new object[0 + 3];
		array[0] = dataTemplate;
		array[1] = c1Calendar;
		array[2] = measurementCalendarView;
		object[] parentValues = array;
		MeasurementCalendarView root = measurementCalendarView;
		((IDataTemplate)dataTemplate).LoadTemplate = delegate
		{
			RowDefinition item = new RowDefinition();
			RowDefinition item2 = new RowDefinition();
			RowDefinition item3 = new RowDefinition();
			BindingExtension bindingExtension = new BindingExtension();
			Label label = new Label();
			BindingExtension bindingExtension2 = new BindingExtension();
			Grid grid = new Grid();
			BindingExtension bindingExtension3 = new BindingExtension();
			Grid grid2 = new Grid();
			BindingExtension bindingExtension4 = new BindingExtension();
			Grid grid3 = new Grid();
			BindingExtension bindingExtension5 = new BindingExtension();
			Grid grid4 = new Grid();
			BindingExtension bindingExtension6 = new BindingExtension();
			Grid grid5 = new Grid();
			StackLayout stackLayout = new StackLayout();
			Grid grid6 = new Grid();
			NameScope nameScope2 = new NameScope();
			NameScope.SetNameScope(grid6, nameScope2);
			((DefinitionCollection<RowDefinition>)grid6.GetValue(Grid.RowDefinitionsProperty)).Add(item);
			((DefinitionCollection<RowDefinition>)grid6.GetValue(Grid.RowDefinitionsProperty)).Add(item2);
			((DefinitionCollection<RowDefinition>)grid6.GetValue(Grid.RowDefinitionsProperty)).Add(item3);
			label.SetValue(Grid.RowProperty, 1);
			bindingExtension.Path = "Day";
			BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
			label.SetBinding(Label.TextProperty, binding);
			BindableProperty fontSizeProperty = Label.FontSizeProperty;
			FontSizeConverter fontSizeConverter = new FontSizeConverter();
			XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
			Type typeFromHandle = typeof(IProvideValueTarget);
			int length;
			object[] array2 = new object[(length = parentValues.Length) + 2];
			Array.Copy(parentValues, 0, array2, 2, length);
			array2[0] = label;
			array2[1] = grid6;
			object service;
			xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array2, Label.FontSizeProperty, nameScope2));
			xamlServiceProvider.Add(typeof(IReferenceProvider), service);
			Type typeFromHandle2 = typeof(IXamlTypeResolver);
			XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
			xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
			xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
			xmlNamespaceResolver.Add("c1", "clr-namespace:C1.Xamarin.Forms.Calendar;assembly=C1.Xamarin.Forms.Calendar");
			xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(_003CInitializeComponent_003E_anonXamlCDataTemplate_7).GetTypeInfo().Assembly));
			xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(36, 62)));
			label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("13", (IServiceProvider)xamlServiceProvider));
			label.SetValue(View.HorizontalOptionsProperty, LayoutOptions.Center);
			label.SetValue(View.VerticalOptionsProperty, LayoutOptions.Center);
			label.SetValue(Label.VerticalTextAlignmentProperty, new TextAlignmentConverter().ConvertFromInvariantString("Center"));
			grid6.Children.Add(label);
			VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 36, 22);
			stackLayout.SetValue(Grid.RowProperty, 2);
			stackLayout.SetValue(View.HorizontalOptionsProperty, LayoutOptions.Center);
			stackLayout.SetValue(View.VerticalOptionsProperty, LayoutOptions.StartAndExpand);
			stackLayout.SetValue(StackLayout.OrientationProperty, StackOrientation.Horizontal);
			stackLayout.SetValue(StackLayout.SpacingProperty, 2.0);
			grid.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid.SetValue(VisualElement.BackgroundColorProperty, Color.Red);
			bindingExtension2.Path = "GlucoseDotVisible";
			BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
			grid.SetBinding(VisualElement.IsVisibleProperty, binding2);
			stackLayout.Children.Add(grid);
			VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 38, 26);
			grid2.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid2.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid2.SetValue(VisualElement.BackgroundColorProperty, Color.Orange);
			bindingExtension3.Path = "SphygmomanometerDotVisible";
			BindingBase binding3 = ((IMarkupExtension<BindingBase>)bindingExtension3).ProvideValue((IServiceProvider)null);
			grid2.SetBinding(VisualElement.IsVisibleProperty, binding3);
			stackLayout.Children.Add(grid2);
			VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 40, 26);
			grid3.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid3.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid3.SetValue(VisualElement.BackgroundColorProperty, Color.Blue);
			bindingExtension4.Path = "TemperatureDotVisible";
			BindingBase binding4 = ((IMarkupExtension<BindingBase>)bindingExtension4).ProvideValue((IServiceProvider)null);
			grid3.SetBinding(VisualElement.IsVisibleProperty, binding4);
			stackLayout.Children.Add(grid3);
			VisualDiagnostics.RegisterSourceInfo(grid3, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 42, 26);
			grid4.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid4.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid4.SetValue(VisualElement.BackgroundColorProperty, Color.Green);
			bindingExtension5.Path = "CompositionMeterDotVisible";
			BindingBase binding5 = ((IMarkupExtension<BindingBase>)bindingExtension5).ProvideValue((IServiceProvider)null);
			grid4.SetBinding(VisualElement.IsVisibleProperty, binding5);
			stackLayout.Children.Add(grid4);
			VisualDiagnostics.RegisterSourceInfo(grid4, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 44, 26);
			grid5.SetValue(VisualElement.WidthRequestProperty, 7.0);
			grid5.SetValue(VisualElement.HeightRequestProperty, 7.0);
			grid5.SetValue(VisualElement.BackgroundColorProperty, Color.Brown);
			bindingExtension6.Path = "StepMeterDotVisible";
			BindingBase binding6 = ((IMarkupExtension<BindingBase>)bindingExtension6).ProvideValue((IServiceProvider)null);
			grid5.SetBinding(VisualElement.IsVisibleProperty, binding6);
			stackLayout.Children.Add(grid5);
			VisualDiagnostics.RegisterSourceInfo(grid5, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 46, 26);
			grid6.Children.Add(stackLayout);
			VisualDiagnostics.RegisterSourceInfo(stackLayout, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 37, 22);
			return grid6;
		};
		c1Calendar.SetValue(C1Calendar.DaySlotTemplateProperty, dataTemplate);
		VisualDiagnostics.RegisterSourceInfo(dataTemplate, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 29, 14);
		measurementCalendarView.SetValue(ContentPage.ContentProperty, c1Calendar);
		VisualDiagnostics.RegisterSourceInfo(c1Calendar, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 11, 6);
		VisualDiagnostics.RegisterSourceInfo(measurementCalendarView, new Uri("Views/MeasurementCalendarView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(MeasurementCalendarView));
		calendar = this.FindByName<C1Calendar>("calendar");
	}
}
