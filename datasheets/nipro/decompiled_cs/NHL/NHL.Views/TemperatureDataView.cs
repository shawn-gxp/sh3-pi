using System;
using System.CodeDom.Compiler;
using System.Reflection;
using C1.Xamarin.Forms.Grid;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/TemperatureDataView.xaml")]
public class TemperatureDataView : ContentPage
{
	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private FlexGrid grid;

	public TemperatureDataView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(TemperatureDataView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/TemperatureDataView.xaml",
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
		BindingExtension bindingExtension = new BindingExtension();
		GridColumn gridColumn = new GridColumn();
		GridColumn gridColumn2 = new GridColumn();
		FlexGrid flexGrid = new FlexGrid();
		Grid grid = new Grid();
		TemperatureDataView temperatureDataView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(temperatureDataView = this) ?? new NameScope());
		NameScope.SetNameScope(temperatureDataView, nameScope);
		((INameScope)nameScope).RegisterName("grid", (object)flexGrid);
		if (flexGrid.StyleId == null)
		{
			flexGrid.StyleId = "grid";
		}
		this.grid = flexGrid;
		temperatureDataView.SetValue(NavigationPage.HasBackButtonProperty, true);
		temperatureDataView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		temperatureDataView.SetValue(Page.TitleProperty, "データ表");
		grid.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(Grid.RowProperty, 0);
		bindingExtension.Path = "DisplayTemperatureChartData";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		flexGrid.SetBinding(FlexGrid.ItemsSourceProperty, binding);
		flexGrid.SetValue(FlexGrid.AutoGenerateColumnsProperty, false);
		flexGrid.SetValue(GridBase.HeadersVisibilityProperty, GridHeadersVisibility.Column);
		flexGrid.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(GridBase.SelectionModeProperty, GridSelectionMode.None);
		flexGrid.SetValue(View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		gridColumn.Binding = "MeasurementAt";
		gridColumn.Header = "日付";
		gridColumn.Format = "M/d";
		gridColumn.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn.IsReadOnly = true;
		gridColumn.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn);
		gridColumn2.Binding = "Temperature";
		gridColumn2.Header = "測定値";
		gridColumn2.Format = "F1";
		gridColumn2.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.IsReadOnly = true;
		gridColumn2.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn2);
		grid.Children.Add(flexGrid);
		VisualDiagnostics.RegisterSourceInfo(flexGrid, new Uri("Views/TemperatureDataView.xaml", UriKind.RelativeOrAbsolute), 11, 10);
		temperatureDataView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/TemperatureDataView.xaml", UriKind.RelativeOrAbsolute), 10, 6);
		VisualDiagnostics.RegisterSourceInfo(temperatureDataView, new Uri("Views/TemperatureDataView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(TemperatureDataView));
		grid = this.FindByName<FlexGrid>("grid");
	}
}
