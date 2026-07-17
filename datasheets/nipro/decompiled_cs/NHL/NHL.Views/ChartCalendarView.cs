using System;
using System.CodeDom.Compiler;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using C1.Xamarin.Forms.Calendar;
using Caliburn.Micro;
using NHL.ViewModels.Contexts;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/ChartCalendarView.xaml")]
public class ChartCalendarView : ContentPage
{
	private List<DateTime> selectedDates;

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private C1Calendar calendar;

	public List<DateTime> SelectedDates
	{
		get
		{
			return selectedDates;
		}
		set
		{
			if (selectedDates != value)
			{
				selectedDates = value;
			}
		}
	}

	public ChartCalendarView()
	{
		InitializeComponent();
		ChartContext chartContext = IoC.Get<ChartContext>();
		SelectedDates = chartContext.SelectedDates;
		calendar.SelectedDates = SelectedDates;
	}

	protected override void OnDisappearing()
	{
		IoC.Get<ChartContext>().SelectedDates = calendar.SelectedDates.ToList();
		base.OnDisappearing();
	}

	public void OnDaySlotLoading(object sender, CalendarDaySlotLoadingEventArgs e)
	{
		if (e.Date > DateTime.Now)
		{
			e.DaySlot.BackgroundColor = Color.FromHex("#bebebe");
		}
	}

	private void OnSelectionChanging(object sender, CalendarSelectionChangingEventArgs e)
	{
		DateTime[] array = e.SelectedDates.ToArray();
		foreach (DateTime dateTime in array)
		{
			if (dateTime > DateTime.Now)
			{
				e.SelectedDates.Remove(dateTime);
			}
		}
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(ChartCalendarView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/ChartCalendarView.xaml",
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
		C1Calendar c1Calendar = new C1Calendar();
		ChartCalendarView chartCalendarView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(chartCalendarView = this) ?? new NameScope());
		NameScope.SetNameScope(chartCalendarView, nameScope);
		((INameScope)nameScope).RegisterName("calendar", (object)c1Calendar);
		if (c1Calendar.StyleId == null)
		{
			c1Calendar.StyleId = "calendar";
		}
		calendar = c1Calendar;
		chartCalendarView.SetValue(NavigationPage.HasBackButtonProperty, true);
		chartCalendarView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		chartCalendarView.SetValue(Page.TitleProperty, "測定日選択");
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
		c1Calendar.DaySlotLoading += chartCalendarView.OnDaySlotLoading;
		c1Calendar.SetValue(C1Calendar.MaxSelectionCountProperty, -1);
		chartCalendarView.SetValue(ContentPage.ContentProperty, c1Calendar);
		VisualDiagnostics.RegisterSourceInfo(c1Calendar, new Uri("Views/ChartCalendarView.xaml", UriKind.RelativeOrAbsolute), 11, 6);
		VisualDiagnostics.RegisterSourceInfo(chartCalendarView, new Uri("Views/ChartCalendarView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(ChartCalendarView));
		calendar = this.FindByName<C1Calendar>("calendar");
	}
}
