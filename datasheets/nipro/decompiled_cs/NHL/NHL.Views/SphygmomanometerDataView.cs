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
[XamlFilePath("Views/SphygmomanometerDataView.xaml")]
public class SphygmomanometerDataView : ContentPage
{
	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private FlexGrid grid;

	public SphygmomanometerDataView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(SphygmomanometerDataView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/SphygmomanometerDataView.xaml",
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
		GridColumn gridColumn3 = new GridColumn();
		GridColumn gridColumn4 = new GridColumn();
		GridColumn gridColumn5 = new GridColumn();
		FlexGrid flexGrid = new FlexGrid();
		Grid grid = new Grid();
		SphygmomanometerDataView sphygmomanometerDataView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(sphygmomanometerDataView = this) ?? new NameScope());
		NameScope.SetNameScope(sphygmomanometerDataView, nameScope);
		((INameScope)nameScope).RegisterName("grid", (object)flexGrid);
		if (flexGrid.StyleId == null)
		{
			flexGrid.StyleId = "grid";
		}
		this.grid = flexGrid;
		sphygmomanometerDataView.SetValue(NavigationPage.HasBackButtonProperty, true);
		sphygmomanometerDataView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		sphygmomanometerDataView.SetValue(Page.TitleProperty, "データ表");
		grid.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(Grid.RowProperty, 0);
		bindingExtension.Path = "DisplaySphygChartData";
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
		gridColumn2.Binding = "TimezoneType";
		gridColumn2.Header = "区分";
		gridColumn2.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.IsReadOnly = true;
		gridColumn2.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn2);
		gridColumn3.Binding = "MaxPressure";
		gridColumn3.Header = "最高血圧";
		gridColumn3.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn3.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn3.IsReadOnly = true;
		gridColumn3.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn3);
		gridColumn4.Binding = "MinPressure";
		gridColumn4.Header = "最低血圧";
		gridColumn4.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn4.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn4.IsReadOnly = true;
		gridColumn4.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn4);
		gridColumn5.Binding = "Pulse";
		gridColumn5.Header = "脈拍";
		gridColumn5.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn5.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn5.IsReadOnly = true;
		gridColumn5.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn5);
		grid.Children.Add(flexGrid);
		VisualDiagnostics.RegisterSourceInfo(flexGrid, new Uri("Views/SphygmomanometerDataView.xaml", UriKind.RelativeOrAbsolute), 11, 10);
		sphygmomanometerDataView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/SphygmomanometerDataView.xaml", UriKind.RelativeOrAbsolute), 10, 6);
		VisualDiagnostics.RegisterSourceInfo(sphygmomanometerDataView, new Uri("Views/SphygmomanometerDataView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(SphygmomanometerDataView));
		grid = this.FindByName<FlexGrid>("grid");
	}
}
